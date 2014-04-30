/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JTypeVar;
import com.sun.codemodel.JVar;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class ADTClassModel {
    private static final String VISITOR_SUFFIX = "Visitor";
    private static final String VALUE_SUFFIX = "Value";

    private static String capitalize(String s) {
        if (s.length() >= 2
            && Character.isHighSurrogate(s.charAt(0))
            && Character.isLowSurrogate(s.charAt(1))) {
            return s.substring(0, 2).toUpperCase() + s.substring(2);
        } else {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }
    private static String getValueName(ADTVisitorInterfaceModel visitorInterface) throws SourceException {
        String visitorName = visitorInterface.getSimpleName();
        String valueName;
        if (visitorName.endsWith(VISITOR_SUFFIX))
            valueName = visitorName.substring(0, visitorName.length() - VISITOR_SUFFIX.length());
        else
            valueName = visitorName + VALUE_SUFFIX;
        return valueName;
    }

    private static String pluralise(String s) throws SourceException {
        if (s.endsWith("y"))
            return s.substring(0, s.length() - "y".length()) + "ies";
        else
            return s + "s";
    }

    public static ADTClassModel createInstance(JCodeModel codeModel, ADTVisitorInterfaceModel visitorInterface) throws SourceException, CodeGenerationException {
        try {
            String acceptingInterfaceName = getValueName(visitorInterface);
            String utilsClassName = pluralise(acceptingInterfaceName);

            JDefinedClass acceptingInterface = codeModel._class(JMod.PUBLIC, visitorInterface.getPackageName() + "." + acceptingInterfaceName, ClassType.INTERFACE);
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeParameter = acceptingInterface.generify(visitorTypeParameter.name());
                typeParameter.bound(visitorTypeParameter._extends());
            }

            JDefinedClass utilsClass = codeModel._class(JMod.PUBLIC, visitorInterface.getPackageName() + "." + utilsClassName, ClassType.CLASS);

            ADTClassModel result = new ADTClassModel(acceptingInterface, utilsClass, visitorInterface);
            result.buildAcceptMethod();
            Map<String, JMethod> constructorMethods = result.buildConstructorMethods();
            result.buildFactory(constructorMethods);

            return result;
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
    }
    private final JDefinedClass acceptingInterface;
    private final JDefinedClass utilsClass;
    private final ADTVisitorInterfaceModel visitorInterface;

    private ADTClassModel(JDefinedClass acceptingInterface, JDefinedClass utilsClass, ADTVisitorInterfaceModel visitorInterface) {
        this.acceptingInterface = acceptingInterface;
        this.utilsClass = utilsClass;
        this.visitorInterface = visitorInterface;
    }

    private void buildAcceptMethod() {
        JMethod acceptMethod = acceptingInterface.method(JMod.PUBLIC, acceptingInterface.owner().VOID, "accept");

        JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
        JTypeVar resultType = acceptMethod.generify(visitorResultType.name());
        resultType.bound(visitorResultType._extends());
        acceptMethod.type(resultType);

        JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            exceptionType = acceptMethod.generify(visitorExceptionType.name());
            exceptionType.bound(visitorExceptionType._extends());
            acceptMethod._throws(exceptionType);
        }

        JClass usedAcceptingInterfaceType = JExprExt.narrow(acceptingInterface, acceptingInterface.typeParams());
        JClass usedVisitorType = visitorInterface.narrowed(usedAcceptingInterfaceType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
    }

    private JMethod buildFactory(Map<String, JMethod> constructorMethods) throws JClassAlreadyExistsException {
        JDefinedClass factory = buildFactoryClass(constructorMethods);

        JFieldVar factoryField = utilsClass.field(JMod.PRIVATE | JMod.STATIC, factory, "FACTORY");
        JAnnotationUse fieldAnnotationUse = factoryField.annotate(SuppressWarnings.class);
        JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
        paramArray.param("unchecked");
        paramArray.param("rawtypes");

        factoryField.init(JExpr._new(factory));
        JMethod factoryMethod = utilsClass.method(JMod.PUBLIC | JMod.STATIC, utilsClass.owner().VOID, "factory");
        JAnnotationUse methodAnnotationUse = factoryMethod.annotate(SuppressWarnings.class);
        methodAnnotationUse.param("value", "unchecked");
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = factoryMethod.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
        }
        JClass usedAcceptingInterfaceType = JExprExt.narrow(acceptingInterface, factoryMethod.typeParams());
        JClass usedFactoryType = JExprExt.narrow(factory, factoryMethod.typeParams());
        factoryMethod.type(visitorInterface.narrowed(usedAcceptingInterfaceType, usedAcceptingInterfaceType, utilsClass.owner().ref(RuntimeException.class)));
        JExpression result = JExpr.ref("FACTORY");
        result = usedFactoryType.getTypeParameters().isEmpty() ? result : JExpr.cast(usedFactoryType, result);
        factoryMethod.body()._return(result);
        return factoryMethod;
    }

    private JDefinedClass buildFactoryClass(Map<String, JMethod> constructorMethods) throws JClassAlreadyExistsException {
        JClass runtimeException = utilsClass.owner().ref(RuntimeException.class);
        JDefinedClass factoryClass = utilsClass._class(JMod.PRIVATE | JMod.STATIC, acceptingInterface.name() + "Factory", ClassType.CLASS);
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = factoryClass.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
        }
        JClass usedAcceptingInterfaceType = JExprExt.narrow(acceptingInterface, factoryClass.typeParams());
        factoryClass._implements(visitorInterface.narrowed(usedAcceptingInterfaceType, usedAcceptingInterfaceType, runtimeException));
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod factoryMethod = factoryClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT, usedAcceptingInterfaceType, interfaceMethod.name());
            factoryMethod.annotate(Override.class);

            JMethod constructorMethod = constructorMethods.get(interfaceMethod.name());
            JInvocation staticInvoke = utilsClass.staticInvoke(constructorMethod);
            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedAcceptingInterfaceType, usedAcceptingInterfaceType, runtimeException);
                factoryMethod.param(param.mods().getValue() | JMod.FINAL, paramType, param.name());
                staticInvoke.arg(JExpr.ref(param.name()));
            }
            factoryMethod.body()._return(staticInvoke);
        }
        return factoryClass;
    }

    private Map<String, JMethod> buildConstructorMethods() throws JClassAlreadyExistsException {
        Map<String, JDefinedClass> caseClasses = buildCaseClasses();

        Map<String, JMethod> constructorMethods = new TreeMap<>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod constructorMethod = utilsClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT | JMod.STATIC, utilsClass.owner().VOID, interfaceMethod.name());
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeParameter = constructorMethod.generify(visitorTypeParameter.name());
                typeParameter.bound(visitorTypeParameter._extends());
            }
            JClass usedAcceptingInterfaceType = JExprExt.narrow(acceptingInterface, constructorMethod.typeParams());
            JClass runtimeException = utilsClass.owner().ref(RuntimeException.class);
            constructorMethod.type(usedAcceptingInterfaceType);
            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedAcceptingInterfaceType, usedAcceptingInterfaceType, runtimeException);
                constructorMethod.param(param.mods().getValue(), paramType, param.name());
            }

            JClass usedCaseClassType = JExprExt.narrow(caseClasses.get(interfaceMethod.name()), constructorMethod.typeParams());
            if (!interfaceMethod.params().isEmpty()) {
                JInvocation constructorInvocation = JExpr._new(usedCaseClassType);
                for (JVar param: interfaceMethod.params())
                    constructorInvocation.arg(JExpr.ref(param.name()));
                constructorMethod.body()._return(constructorInvocation);
            } else {
                JFieldVar singletonInstanceField = utilsClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL,
                                                                    usedAcceptingInterfaceType.erasure(),
                                                                    interfaceMethod.name().toUpperCase(),
                                                                    JExpr._new(usedCaseClassType.erasure()));
                JAnnotationUse fieldAnnotationUse = singletonInstanceField.annotate(SuppressWarnings.class);
                JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
                paramArray.param("unchecked");
                paramArray.param("rawtypes");

                JAnnotationUse methodAnnotationUse = constructorMethod.annotate(SuppressWarnings.class);
                methodAnnotationUse.param("value", "unchecked");
                JExpression result = usedAcceptingInterfaceType.getTypeParameters().isEmpty() ? singletonInstanceField : JExpr.cast(usedAcceptingInterfaceType, singletonInstanceField);
                constructorMethod.body()._return(result);
            }
            constructorMethods.put(interfaceMethod.name(), constructorMethod);
        }
        return constructorMethods;
    }

    private Map<String, JDefinedClass> buildCaseClasses() throws JClassAlreadyExistsException {
        JDefinedClass baseCaseClass = utilsClass._class(JMod.ABSTRACT | JMod.PRIVATE | JMod.STATIC, "Base" + acceptingInterface.name(), ClassType.CLASS);
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = baseCaseClass.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
        }
        baseCaseClass._implements(JExprExt.narrow(acceptingInterface, baseCaseClass.typeParams()));

        BaseCaseClassBuiler baseCaseClassBuiler = new BaseCaseClassBuiler(baseCaseClass);
        baseCaseClassBuiler.buildEqualsMethod();
        baseCaseClassBuiler.buildHashCodeMethod();

        Map<String, JDefinedClass> caseClasses = new TreeMap<>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JDefinedClass caseClass = baseCaseClassBuiler.buildCaseClass(interfaceMethod);
            caseClasses.put(interfaceMethod.name(), caseClass);
        }
        return caseClasses;
    }

    private class BaseCaseClassBuiler {
        private final JDefinedClass baseCaseClass;
        public BaseCaseClassBuiler(JDefinedClass baseCaseClass) {
            this.baseCaseClass = baseCaseClass;
        }

        private JDefinedClass buildCaseClass(JMethod interfaceMethod) throws JClassAlreadyExistsException {
            JClass runtimeException = utilsClass.owner().ref(RuntimeException.class);
            JDefinedClass caseClass = utilsClass._class(JMod.PRIVATE | JMod.STATIC, capitalize(interfaceMethod.name()) + capitalize(acceptingInterface.name()));
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeArgument = caseClass.generify(visitorTypeParameter.name());
                typeArgument.bound(visitorTypeParameter._extends());
            }

            JClass usedBaseCaseClassType = JExprExt.narrow(baseCaseClass, caseClass.typeParams());
            JClass usedAcceptingInterfaceType = JExprExt.narrow(acceptingInterface, caseClass.typeParams());
            caseClass._extends(usedBaseCaseClassType);

            JMethod constructor = caseClass.constructor(JMod.NONE);
            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedAcceptingInterfaceType, usedAcceptingInterfaceType, runtimeException);
                caseClass.field(JMod.PRIVATE | JMod.FINAL, paramType, param.name());
                constructor.param(paramType, param.name());
                constructor.body().assign(JExpr._this().ref(param.name()), JExpr.ref(param.name()));
            }

            JMethod acceptMethod = caseClass.method(JMod.PUBLIC, utilsClass.owner().VOID, "accept");
            acceptMethod.annotate(Override.class);

            JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
            JTypeVar resultType = acceptMethod.generify(visitorResultType.name());
            resultType.bound(visitorResultType._extends());
            acceptMethod.type(resultType);

            JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
            JTypeVar exceptionType = null;
            if (visitorExceptionType != null) {
                exceptionType = acceptMethod.generify(visitorExceptionType.name());
                exceptionType.bound(visitorExceptionType._extends());
                acceptMethod._throws(exceptionType);
            }

            JClass usedVisitorType = visitorInterface.narrowed(usedAcceptingInterfaceType, resultType, exceptionType);
            acceptMethod.param(usedVisitorType, "visitor");
            JInvocation invocation = JExpr.invoke(JExpr.ref("visitor"), interfaceMethod.name());
            for (JVar param: interfaceMethod.params()) {
                invocation.arg(JExpr._this().ref(param.name()));
            }
            acceptMethod.body()._return(invocation);
            return caseClass;
        }

        private void buildEqualsMethod() {
            JType booleanType = baseCaseClass.owner()._ref(Boolean.class);
            JType runtimeException = baseCaseClass.owner()._ref(RuntimeException.class);
            JMethod equalsMethod = baseCaseClass.method(JMod.PUBLIC, baseCaseClass.owner().BOOLEAN, "equals");
            equalsMethod.annotate(Override.class);
            JAnnotationUse annotationUse = equalsMethod.annotate(SuppressWarnings.class);
            annotationUse.param("value", "unchecked");
            equalsMethod.param(baseCaseClass.owner()._ref(Object.class), "thatObject");
            JFieldRef thatObject = JExpr.ref("thatObject");
            JConditional _if = equalsMethod.body()._if(JExpr._this().eq(thatObject));
            _if._then()._return(JExpr.TRUE);
            JConditional elseif = _if._elseif(thatObject._instanceof(acceptingInterface).not());
            elseif._then()._return(JExpr.FALSE);
            JBlock _else = elseif._else();
            JClass usedAcceptingInterfaceType = JExprExt.narrow(acceptingInterface, baseCaseClass.typeParams());
            _else.decl(JMod.FINAL, usedAcceptingInterfaceType, "that", JExpr.cast(usedAcceptingInterfaceType, thatObject));
            JClass visitorType = visitorInterface.narrowed(usedAcceptingInterfaceType, booleanType, runtimeException);

            JDefinedClass anonymousClass1 = baseCaseClass.owner().anonymousClass(visitorType);
            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JMethod visitorMethod1 = anonymousClass1.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, booleanType, interfaceMethod1.name());
                visitorMethod1.annotate(Override.class);
                for (JVar param: interfaceMethod1.params()) {
                    JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedAcceptingInterfaceType, booleanType, runtimeException);
                    visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
                }

                JDefinedClass anonymousClass2 = baseCaseClass.owner().anonymousClass(visitorType);
                for (JMethod interfaceMethod2: visitorInterface.methods()) {
                    JMethod visitorMethod2 = anonymousClass2.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, booleanType, interfaceMethod2.name());
                    visitorMethod2.annotate(Override.class);
                    for (JVar param: interfaceMethod2.params()) {
                        JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedAcceptingInterfaceType, booleanType, runtimeException);
                        visitorMethod2.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "2");
                    }
                    if (!interfaceMethod1.name().equals(interfaceMethod2.name()))
                        visitorMethod2.body()._return(JExpr.FALSE);
                    else {
                        EqualsBody body = new EqualsBody(visitorMethod2.body());
                        for (JVar param: interfaceMethod1.params()) {
                            JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedAcceptingInterfaceType, booleanType, runtimeException);
                            JExpression field1 = JExpr.ref(param.name() + "1");
                            JExpression field2 = JExpr.ref(param.name() + "2");
                            body.appendValue(paramType, field1, field2);
                        }
                        visitorMethod2.body()._return(JExpr.TRUE);
                    }
                }

                JInvocation invocation2 = JExpr.ref("that").invoke("accept");
                invocation2.arg(JExpr._new(anonymousClass2));
                visitorMethod1.body()._return(invocation2);
            }
            JInvocation invocation1 = JExpr._this().invoke("accept");
            invocation1.arg(JExpr._new(anonymousClass1));
            _else._return(invocation1);
        }

        private void buildHashCodeMethod() {
            JType integerType = baseCaseClass.owner()._ref(Integer.class);
            JType runtimeException = baseCaseClass.owner()._ref(RuntimeException.class);
            JMethod hashCodeMethod = baseCaseClass.method(JMod.PUBLIC, baseCaseClass.owner().INT, "hashCode");
            hashCodeMethod.annotate(Override.class);
            JClass usedAcceptingInterfaceType = JExprExt.narrow(acceptingInterface, baseCaseClass.typeParams());
            JClass visitorType = visitorInterface.narrowed(usedAcceptingInterfaceType, integerType, runtimeException);

            JDefinedClass anonymousClass1 = baseCaseClass.owner().anonymousClass(visitorType);
            int tag = 1;
            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JMethod visitorMethod1 = anonymousClass1.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, integerType, interfaceMethod1.name());
                visitorMethod1.annotate(Override.class);
                JVar result = visitorMethod1.body().decl(baseCaseClass.owner().INT, "result", JExpr.lit(tag));
                HashCodeBody body = new HashCodeBody(visitorMethod1.body(), result);
                for (JVar param: interfaceMethod1.params()) {
                    JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedAcceptingInterfaceType, integerType, runtimeException);
                    JVar argument = visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
                    body.appendValue(paramType, argument);
                }
                visitorMethod1.body()._return(result);
                tag++;
            }
            JInvocation invocation1 = JExpr._this().invoke("accept");
            invocation1.arg(JExpr._new(anonymousClass1));
            hashCodeMethod.body()._return(invocation1);
        }

        private class EqualsBody {
            private final JBlock body;
            private EqualsBody(JBlock body) {
                this.body = body;
            }

            private void appendValue(JType type, JExpression value1, JExpression value2) {
                JCodeModel model = baseCaseClass.owner();
                JType intType = model.INT;
                if (type.isArray()) {
                    appendValue(model.INT, value1.ref("length"), value2.ref("length"));

                    JForLoop _for = body._for();
                    _for.init(intType, "i", JExpr.lit(0));
                    JFieldRef i = JExpr.ref("i");
                    _for.test(i.lt(value1.ref("length")));
                    _for.update(i.incr());
                    EqualsBody forBody = new EqualsBody(_for.body());
                    forBody.appendValue(type.elementType(), value1.component(i), value2.component(i));
                } else if (type.isPrimitive()) {
                    JConditional _if = body._if(value1.ne(value2));
                    _if._then()._return(JExpr.FALSE);
                } else {
                    JInvocation invocation = value1.invoke("equals");
                    invocation.arg(value2);
                    JConditional _if = body._if(invocation.not());
                    _if._then()._return(JExpr.FALSE);
                }
            }
        }

        private class HashCodeBody {
            private final JBlock body;
            private final JVar result;
            private HashCodeBody(JBlock body, JVar result) {
                this.body = body;
                this.result = result;
            }

            private void appendValue(JType type, JExpression value) {
                JCodeModel model = baseCaseClass.owner();
                JType intType = model.INT;
                if (type.isArray()) {
                    JForLoop _for = body._for();
                    _for.init(intType, "i", JExpr.lit(0));
                    JFieldRef i = JExpr.ref("i");
                    _for.test(i.lt(value.ref("length")));
                    _for.update(i.incr());
                    HashCodeBody forBody = new HashCodeBody(_for.body(), result);
                    forBody.appendValue(type.elementType(), value.component(i));
                } else if (!type.isPrimitive()) {
                    appendValue(intType, value.invoke("hashCode"));
                } else if (type.name().equals("double")) {
                    JInvocation invocation = model.ref(Double.class).staticInvoke("doubleToLongBits");
                    invocation.arg(value);
                    appendValue(baseCaseClass.owner().LONG, invocation);
                } else if (type.name().equals("float")) {
                    JInvocation invocation = model.ref(Float.class).staticInvoke("floatToIntBits");
                    invocation.arg(value);
                    appendValue(intType, invocation);
                } else if (type.name().equals("boolean")) {
                    appendValue(intType, JExprExt.ternary(value, JExpr.lit(0), JExpr.lit(1)));
                } else if (type.name().equals("long")) {
                    appendValue(intType, JExpr.cast(intType, value.xor(value.shrz(JExpr.lit(32)))));
                } else {
                    body.assign(result, result.mul(JExpr.lit(27)).plus(value));
                }
            }
        }
    }

}
