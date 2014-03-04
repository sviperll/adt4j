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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class DefinedClass {
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
    private static String getQualifiedName(DefinedVisitorInterface visitorInterface) throws SourceException {
        String visitorName = visitorInterface.getSimpleName();
        String valueName;
        if (visitorName.endsWith(VISITOR_SUFFIX))
            valueName = visitorName.substring(0, visitorName.length() - VISITOR_SUFFIX.length());
        else
            valueName = visitorName + VALUE_SUFFIX;
        String packageName = visitorInterface.getPackageName();
        return packageName + "." + valueName;
    }

    public static DefinedClass createInstance(JCodeModel codeModel, DefinedVisitorInterface visitorInterface) throws SourceException, CodeGenerationException {
        try {
            String qualifiedName = getQualifiedName(visitorInterface);
            JDefinedClass definedClass = codeModel._class(JMod.ABSTRACT | JMod.PUBLIC, qualifiedName, ClassType.CLASS);
            List<JClass> typeParameters = new ArrayList<>();
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeParameter = definedClass.generify(visitorTypeParameter.name());
                typeParameter.bound(visitorTypeParameter._extends());
                typeParameters.add(typeParameter);
            }
            JClass usedDataType = JExprExt.narrow(definedClass, typeParameters);
            DefinedClass result = new DefinedClass(definedClass, visitorInterface, usedDataType);
            result.buildAcceptMethod();
            result.buildAcceptRecursiveMethod();

            Map<String, JDefinedClass> caseClasses = new TreeMap<>();
            for (JMethod interfaceMethod: visitorInterface.methods()) {
                JDefinedClass caseClass = result.buildCaseClass(interfaceMethod);
                caseClasses.put(interfaceMethod.name(), caseClass);
            }

            result.buildConstructorMethods(caseClasses);
            JDefinedClass factoryClass = result.buildFactoryClass(caseClasses);
            result.buildFactoryInstanceGetter(factoryClass);
            result.buildEqualsMethod();
            result.buildHashCodeMethod();

            return result;
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
    }

    private final JDefinedClass definedClass;
    private final DefinedVisitorInterface visitorInterface;
    private final JClass usedDataType;

    DefinedClass(JDefinedClass definedClass, DefinedVisitorInterface visitorInterface, JClass usedDataType) {
        this.definedClass = definedClass;
        this.visitorInterface = visitorInterface;
        this.usedDataType = usedDataType;
    }

    String getQualifiedName() {
        return definedClass.fullName();
    }

    JCodeModel getCodeModel() {
        return definedClass.owner();
    }

    void buildAcceptMethod() {
        JMethod acceptMethod = definedClass.method(JMod.ABSTRACT | JMod.PUBLIC, definedClass.owner().VOID, "accept");

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

        JClass usedVisitorType = visitorInterface.narrowed(usedDataType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
    }

    void buildAcceptRecursiveMethod() {
        JMethod acceptRecursiveMethod = definedClass.method(JMod.PUBLIC, definedClass.owner().VOID, "acceptRecursive");

        JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
        JTypeVar resultType = acceptRecursiveMethod.generify(visitorResultType.name());
        resultType.bound(visitorResultType._extends());
        acceptRecursiveMethod.type(resultType);

        JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            exceptionType = acceptRecursiveMethod.generify(visitorExceptionType.name());
            exceptionType.bound(visitorExceptionType._extends());
            acceptRecursiveMethod._throws(exceptionType);
        }

        JClass usedVisitorType = visitorInterface.narrowedForSelf(usedDataType, resultType, exceptionType, resultType);
        JVar visitorParam = acceptRecursiveMethod.param(JMod.FINAL, usedVisitorType, "visitor");
        JInvocation acceptInvocation = JExpr._this().invoke("accept");
        if (!visitorInterface.hasSelfTypeParameter()) {
            acceptInvocation.arg(JExpr.ref("visitor"));
        } else {
            JDefinedClass anonymousClass = definedClass.owner().anonymousClass(visitorInterface.narrowed(usedDataType, resultType, exceptionType));
            for (JMethod interfaceMethod: visitorInterface.methods()) {
                JMethod adaptorMethod = anonymousClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT, resultType, interfaceMethod.name());
                adaptorMethod.annotate(Override.class);

                if (visitorExceptionType != null) {
                    adaptorMethod._throws(exceptionType);
                }

                JInvocation invocation = JExpr.ref(visitorParam.name()).invoke(adaptorMethod.name());
                for (JVar param: interfaceMethod.params()) {
                    JType paramType = visitorInterface.narrowed(param.type(), usedDataType, resultType, exceptionType);
                    adaptorMethod.param(param.mods().getValue() | JMod.FINAL, paramType, param.name());

                    JType outerVisitorParamType = visitorInterface.narrowed(param.type(), resultType, resultType, exceptionType);

                    if (paramType.equals(outerVisitorParamType))
                        invocation.arg(JExpr.ref(param.name()));
                    else {
                        JInvocation argument = param.invoke("acceptRecursive");
                        argument.arg(JExpr.ref("visitor"));
                        invocation.arg(argument);
                    }
                }
                adaptorMethod.body()._return(invocation);
            }
            acceptInvocation.arg(JExpr._new(anonymousClass));
        }
        acceptRecursiveMethod.body()._return(acceptInvocation);
    }

    JMethod buildFactoryInstanceGetter(JDefinedClass factory) {
        JFieldVar factoryField = definedClass.field(JMod.PRIVATE | JMod.STATIC, factory, "FACTORY");
        JAnnotationUse fieldAnnotationUse = factoryField.annotate(SuppressWarnings.class);
        JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
        paramArray.param("unchecked");
        paramArray.param("rawtypes");

        factoryField.init(JExpr._new(factory));
        JMethod factoryMethod = definedClass.method(JMod.PUBLIC | JMod.STATIC, definedClass.owner().VOID, "factory");
        JAnnotationUse methodAnnotationUse = factoryMethod.annotate(SuppressWarnings.class);
        methodAnnotationUse.param("value", "unchecked");
        List<JClass> typeArguments = new ArrayList<>();
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = factoryMethod.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
            typeArguments.add(typeParameter);
        }
        JClass staticUsedDataType = JExprExt.narrow(definedClass, typeArguments);
        JClass usedFactoryType = JExprExt.narrow(factory, typeArguments);
        factoryMethod.type(visitorInterface.narrowed(staticUsedDataType, staticUsedDataType, definedClass.owner().ref(RuntimeException.class)));
        JExpression result = JExpr.ref("FACTORY");
        result = usedFactoryType.getTypeParameters().isEmpty() ? result : JExpr.cast(usedFactoryType, result);
        factoryMethod.body()._return(result);
        return factoryMethod;
    }

    JDefinedClass buildFactoryClass(Map<String, JDefinedClass> caseClasses) throws JClassAlreadyExistsException {
        JClass runtimeException = definedClass.owner().ref(RuntimeException.class);
        JDefinedClass factoryClass = definedClass._class(JMod.PRIVATE | JMod.STATIC, definedClass.name() + "Factory", ClassType.CLASS);
        List<JClass> typeArguments = new ArrayList<>();
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = factoryClass.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
            typeArguments.add(typeParameter);
        }
        JClass staticUsedDataType = JExprExt.narrow(definedClass, typeArguments);
        factoryClass._implements(visitorInterface.narrowed(staticUsedDataType, staticUsedDataType, runtimeException));
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod factoryMethod = factoryClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT, staticUsedDataType, interfaceMethod.name());
            factoryMethod.annotate(Override.class);

            JInvocation staticInvoke = definedClass.staticInvoke(interfaceMethod.name());
            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.narrowed(param.type(), staticUsedDataType, staticUsedDataType, runtimeException);
                factoryMethod.param(param.mods().getValue() | JMod.FINAL, paramType, param.name());
                staticInvoke.arg(JExpr.ref(param.name()));
            }
            factoryMethod.body()._return(staticInvoke);
        }
        return factoryClass;
    }

    private JDefinedClass buildCaseClass(JMethod interfaceMethod) throws JClassAlreadyExistsException {
        JClass runtimeException = definedClass.owner().ref(RuntimeException.class);
        JDefinedClass caseClass = definedClass._class(JMod.PRIVATE | JMod.STATIC, capitalize(interfaceMethod.name()) + capitalize(definedClass.name()));
        List<JClass> typeParameters = new ArrayList<>();
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = caseClass.generify(visitorTypeParameter.name());
            typeParameters.add(typeParameter);
        }

        JClass staticUsedDataType = JExprExt.narrow(definedClass, typeParameters);
        caseClass._extends(staticUsedDataType);

        JMethod constructor = caseClass.constructor(JMod.NONE);
        for (JVar param: interfaceMethod.params()) {
            JType paramType = visitorInterface.narrowed(param.type(), staticUsedDataType, staticUsedDataType, runtimeException);
            caseClass.field(JMod.PRIVATE | JMod.FINAL, paramType, param.name());
            constructor.param(paramType, param.name());
            constructor.body().assign(JExpr._this().ref(param.name()), JExpr.ref(param.name()));
        }

        JMethod acceptMethod = caseClass.method(JMod.PUBLIC, definedClass.owner().VOID, "accept");
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

        JClass usedVisitorType = visitorInterface.narrowed(staticUsedDataType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
        JInvocation invocation = JExpr.invoke(JExpr.ref("visitor"), interfaceMethod.name());
        for (JVar param: interfaceMethod.params()) {
            invocation.arg(JExpr._this().ref(param.name()));
        }
        acceptMethod.body()._return(invocation);
        return caseClass;
    }

    void buildConstructorMethods(Map<String, JDefinedClass> caseClasses) {
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod constructorMethod = definedClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT | JMod.STATIC, definedClass.owner().VOID, interfaceMethod.name());
            List<JClass> typeArguments = new ArrayList<>();
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeParameter = constructorMethod.generify(visitorTypeParameter.name());
                typeParameter.bound(visitorTypeParameter._extends());
                typeArguments.add(typeParameter);
            }
            JClass staticUsedDataType = JExprExt.narrow(definedClass, typeArguments);
            JClass runtimeException = definedClass.owner().ref(RuntimeException.class);
            constructorMethod.type(staticUsedDataType);
            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.narrowed(param.type(), staticUsedDataType, staticUsedDataType, runtimeException);
                constructorMethod.param(param.mods().getValue(), paramType, param.name());
            }

            JClass caseClass = JExprExt.narrow(caseClasses.get(interfaceMethod.name()), typeArguments);
            if (!interfaceMethod.params().isEmpty()) {
                JInvocation constructorInvocation = JExpr._new(caseClass);
                for (JVar param: interfaceMethod.params())
                    constructorInvocation.arg(JExpr.ref(param.name()));
                constructorMethod.body()._return(constructorInvocation);
            } else {
                JFieldVar singletonInstanceField = definedClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL,
                                                                      staticUsedDataType.erasure(),
                                                                      interfaceMethod.name().toUpperCase(),
                                                                      JExpr._new(caseClass.erasure()));
                JAnnotationUse fieldAnnotationUse = singletonInstanceField.annotate(SuppressWarnings.class);
                JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
                paramArray.param("unchecked");
                paramArray.param("rawtypes");

                JAnnotationUse methodAnnotationUse = constructorMethod.annotate(SuppressWarnings.class);
                methodAnnotationUse.param("value", "unchecked");
                JExpression result = staticUsedDataType.getTypeParameters().isEmpty() ? singletonInstanceField : JExpr.cast(staticUsedDataType, singletonInstanceField);
                constructorMethod.body()._return(result);
            }
        }
    }

    private void buildEqualsMethod() {
        JType booleanType = definedClass.owner()._ref(Boolean.class);
        JType runtimeException = definedClass.owner()._ref(RuntimeException.class);
        JMethod equalsMethod = definedClass.method(JMod.PUBLIC, definedClass.owner().BOOLEAN, "equals");
        equalsMethod.annotate(Override.class);
        JAnnotationUse annotationUse = equalsMethod.annotate(SuppressWarnings.class);
        annotationUse.param("value", "unchecked");
        equalsMethod.param(definedClass.owner()._ref(Object.class), "thatObject");
        JFieldRef thatObject = JExpr.ref("thatObject");
        JConditional _if = equalsMethod.body()._if(JExpr._this().eq(thatObject));
        _if._then()._return(JExpr.TRUE);
        JConditional elseif = _if._elseif(thatObject._instanceof(definedClass).not());
        elseif._then()._return(JExpr.FALSE);
        JBlock _else = elseif._else();
        _else.decl(JMod.FINAL, usedDataType, "that", JExpr.cast(usedDataType, thatObject));
        JClass visitorType = visitorInterface.narrowed(usedDataType, booleanType, runtimeException);

        JDefinedClass anonymousClass1 = definedClass.owner().anonymousClass(visitorType);
        for (JMethod interfaceMethod1: visitorInterface.methods()) {
            JMethod visitorMethod1 = anonymousClass1.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, booleanType, interfaceMethod1.name());
            visitorMethod1.annotate(Override.class);
            for (JVar param: interfaceMethod1.params()) {
                JType paramType = visitorInterface.narrowed(param.type(), usedDataType, booleanType, runtimeException);
                visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
            }

            JDefinedClass anonymousClass2 = definedClass.owner().anonymousClass(visitorType);
            for (JMethod interfaceMethod2: visitorInterface.methods()) {
                JMethod visitorMethod2 = anonymousClass2.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, booleanType, interfaceMethod2.name());
                visitorMethod2.annotate(Override.class);
                for (JVar param: interfaceMethod2.params()) {
                    JType paramType = visitorInterface.narrowed(param.type(), usedDataType, booleanType, runtimeException);
                    visitorMethod2.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "2");
                }
                if (!interfaceMethod1.name().equals(interfaceMethod2.name()))
                    visitorMethod2.body()._return(JExpr.FALSE);
                else {
                    EqualsBody body = new EqualsBody(visitorMethod2.body());
                    for (JVar param: interfaceMethod1.params()) {
                        JType paramType = visitorInterface.narrowed(param.type(), usedDataType, booleanType, runtimeException);
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
        JType integerType = definedClass.owner()._ref(Integer.class);
        JType runtimeException = definedClass.owner()._ref(RuntimeException.class);
        JMethod hashCodeMethod = definedClass.method(JMod.PUBLIC, definedClass.owner().INT, "hashCode");
        hashCodeMethod.annotate(Override.class);
        JClass visitorType = visitorInterface.narrowed(usedDataType, integerType, runtimeException);

        JDefinedClass anonymousClass1 = definedClass.owner().anonymousClass(visitorType);
        int tag = 1;
        for (JMethod interfaceMethod1: visitorInterface.methods()) {
            JMethod visitorMethod1 = anonymousClass1.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, integerType, interfaceMethod1.name());
            visitorMethod1.annotate(Override.class);
            JVar result = visitorMethod1.body().decl(definedClass.owner().INT, "result", JExpr.lit(tag));
            HashCodeBody body = new HashCodeBody(visitorMethod1.body(), result);
            for (JVar param: interfaceMethod1.params()) {
                JType paramType = visitorInterface.narrowed(param.type(), usedDataType, integerType, runtimeException);
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
            JCodeModel model = definedClass.owner();
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
            JCodeModel model = definedClass.owner();
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
                appendValue(definedClass.owner().LONG, invocation);
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
