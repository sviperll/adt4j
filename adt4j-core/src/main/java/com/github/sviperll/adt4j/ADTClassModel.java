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
import com.sun.codemodel.JOp;
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
    private static String capitalize(String s) {
        if (s.length() >= 2
            && Character.isHighSurrogate(s.charAt(0))
            && Character.isLowSurrogate(s.charAt(1))) {
            return s.substring(0, 2).toUpperCase() + s.substring(2);
        } else {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }

    private static JDefinedClass createAcceptingInterface(JDefinedClass valueClass,
                                                          ADTVisitorInterfaceModel visitorInterface,
                                                          Types types) throws JClassAlreadyExistsException {

        JDefinedClass acceptingInterface = valueClass._class(JMod.PUBLIC, valueClass.name() + "Acceptor", ClassType.INTERFACE);

        // Hack to overcome bug in codeModel. We want private interface!!! Not public.
        acceptingInterface.mods().setPrivate();

        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = acceptingInterface.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
        }

        JMethod acceptMethod = acceptingInterface.method(JMod.PUBLIC, types._void(), "accept");

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

        JClass usedValueClassType = Types.narrow(valueClass, valueClass.typeParams());
        JClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");

        return acceptingInterface;
    }

    public static ADTClassModel createInstance(JCodeModel codeModel, ADTVisitorInterfaceModel visitorInterface) throws SourceException, CodeGenerationException {
        try {
            Types types = Types.createInstance(codeModel);

            String valueClassName = visitorInterface.getValueClassName();

            int mods = visitorInterface.generatesPublicClass() ? JMod.PUBLIC: JMod.NONE;
            JDefinedClass valueClass = codeModel._class(mods, visitorInterface.getPackageName() + "." + valueClassName, ClassType.CLASS);
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeParameter = valueClass.generify(visitorTypeParameter.name());
                typeParameter.bound(visitorTypeParameter._extends());
            }

            JDefinedClass acceptingInterface = createAcceptingInterface(valueClass, visitorInterface, types);

            ADTClassModel result = new ADTClassModel(valueClass, acceptingInterface, visitorInterface, types);
            JFieldVar acceptorField = result.buildAcceptorField();
            result.buildPrivateConstructor(acceptorField);
            result.buildProtectedConstructor(acceptorField);
            result.buildAcceptMethod(acceptorField);
            result.buildEqualsMethod();
            result.buildHashCodeMethod();
            Map<String, JMethod> constructorMethods = result.buildConstructorMethods();
            result.buildFactory(constructorMethods);

            return result;
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
    }
    private final JDefinedClass valueClass;
    private final JDefinedClass acceptingInterface;
    private final ADTVisitorInterfaceModel visitorInterface;
    private final Types types;

    private ADTClassModel(JDefinedClass valueClass, JDefinedClass acceptingInterface, ADTVisitorInterfaceModel visitorInterface, Types modelTypes) {
        this.valueClass = valueClass;
        this.acceptingInterface = acceptingInterface;
        this.visitorInterface = visitorInterface;
        this.types = modelTypes;
    }

    private JFieldVar buildAcceptorField() {
        JType usedAcceptingInterfaceType = Types.narrow(acceptingInterface, valueClass.typeParams());
        return valueClass.field(JMod.PRIVATE | JMod.FINAL, usedAcceptingInterfaceType, "acceptor");
    }

    private void buildPrivateConstructor(JFieldVar acceptorField) {
        JMethod constructor = valueClass.constructor(JMod.PRIVATE);
        constructor.param(acceptorField.type(), acceptorField.name());
        constructor.body().assign(JExpr.refthis(acceptorField.name()), JExpr.ref(acceptorField.name()));
    }

    private void buildProtectedConstructor(JFieldVar acceptorField) throws JClassAlreadyExistsException {
        JMethod constructor = valueClass.constructor(JMod.PROTECTED);
        JClass usedValueClassType = Types.narrow(valueClass, valueClass.typeParams());
        constructor.param(JMod.FINAL, usedValueClassType, "implementation");
        JDefinedClass proxyClass = createProxyClass();
        JClass usedProxyClassType = Types.narrow(proxyClass, valueClass.typeParams());
        JInvocation construction = JExpr._new(usedProxyClassType);
        construction.arg(JExpr.ref("implementation"));
        constructor.body().assign(JExpr.refthis(acceptorField.name()), construction);
    }

    private JDefinedClass createProxyClass() throws JClassAlreadyExistsException {
        JDefinedClass proxyClass = valueClass._class(JMod.PRIVATE | JMod.STATIC, "Proxy" + acceptingInterface.name(), ClassType.CLASS);
        for (JTypeVar visitorTypeParameter: acceptingInterface.typeParams()) {
            JTypeVar typeArgument = proxyClass.generify(visitorTypeParameter.name());
            typeArgument.bound(visitorTypeParameter._extends());
        }
        JClass usedAcceptingInterfaceType = Types.narrow(acceptingInterface, proxyClass.typeParams());
        proxyClass._implements(usedAcceptingInterfaceType);

        JMethod constructor = proxyClass.constructor(JMod.NONE);
        JClass usedValueClassType = Types.narrow(valueClass, proxyClass.typeParams());
        proxyClass.field(JMod.PRIVATE | JMod.FINAL, usedValueClassType, "implementation");
        constructor.param(usedValueClassType, "implementation");
        constructor.body().assign(JExpr._this().ref("implementation"), JExpr.ref("implementation"));

        JMethod acceptMethod = proxyClass.method(JMod.PUBLIC, types._void(), "accept");
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

        JClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
        JInvocation invocation = JExpr.ref("implementation").invoke("accept");
        invocation.arg(JExpr.ref("visitor"));
        acceptMethod.body()._return(invocation);
        return proxyClass;
    }

    private void buildAcceptMethod(JFieldVar acceptorField) {
        JMethod acceptMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._void(), "accept");

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

        JClass usedValueClassType = Types.narrow(valueClass, valueClass.typeParams());
        JClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
        JInvocation invocation = acceptorField.invoke("accept");
        invocation.arg(JExpr.ref("visitor"));
        acceptMethod.body()._return(invocation);
    }

    private JMethod buildFactory(Map<String, JMethod> constructorMethods) throws JClassAlreadyExistsException {
        JDefinedClass factory = buildFactoryClass(constructorMethods);

        JFieldVar factoryField = valueClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, factory, "FACTORY");
        JAnnotationUse fieldAnnotationUse = factoryField.annotate(SuppressWarnings.class);
        JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
        paramArray.param("unchecked");
        paramArray.param("rawtypes");

        factoryField.init(JExpr._new(factory));
        JMethod factoryMethod = valueClass.method(JMod.PUBLIC | JMod.STATIC, types._void(), "factory");
        JAnnotationUse methodAnnotationUse = factoryMethod.annotate(SuppressWarnings.class);
        methodAnnotationUse.param("value", "unchecked");
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = factoryMethod.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
        }
        JClass usedValueClassType = Types.narrow(valueClass, factoryMethod.typeParams());
        JClass usedFactoryType = Types.narrow(factory, factoryMethod.typeParams());
        factoryMethod.type(visitorInterface.narrowed(usedValueClassType, usedValueClassType, types._RuntimeException()));
        JExpression result = JExpr.ref("FACTORY");
        result = usedFactoryType.getTypeParameters().isEmpty() ? result : JExpr.cast(usedFactoryType, result);
        factoryMethod.body()._return(result);
        return factoryMethod;
    }

    private JDefinedClass buildFactoryClass(Map<String, JMethod> constructorMethods) throws JClassAlreadyExistsException {
        JType runtimeException = types._RuntimeException();
        JDefinedClass factoryClass = valueClass._class(JMod.PRIVATE | JMod.STATIC, valueClass.name() + "Factory", ClassType.CLASS);
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = factoryClass.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
        }
        JClass usedValueClassType = Types.narrow(valueClass, factoryClass.typeParams());
        factoryClass._implements(visitorInterface.narrowed(usedValueClassType, usedValueClassType, runtimeException));
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod factoryMethod = factoryClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT, usedValueClassType, interfaceMethod.name());
            factoryMethod.annotate(Override.class);

            JMethod constructorMethod = constructorMethods.get(interfaceMethod.name());
            JInvocation staticInvoke = valueClass.staticInvoke(constructorMethod);
            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, usedValueClassType, runtimeException);
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
            JMethod constructorMethod = valueClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT | JMod.STATIC, types._void(), interfaceMethod.name());
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeParameter = constructorMethod.generify(visitorTypeParameter.name());
                typeParameter.bound(visitorTypeParameter._extends());
            }
            JClass usedValueClassType = Types.narrow(valueClass, constructorMethod.typeParams());
            constructorMethod.type(usedValueClassType);
            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException());
                constructorMethod.param(param.mods().getValue(), paramType, param.name());
            }

            JClass usedCaseClassType = Types.narrow(caseClasses.get(interfaceMethod.name()), constructorMethod.typeParams());
            if (!interfaceMethod.params().isEmpty()) {
                JInvocation caseClassConstructorInvocation = JExpr._new(usedCaseClassType);
                for (JVar param: interfaceMethod.params())
                    caseClassConstructorInvocation.arg(JExpr.ref(param.name()));
                JInvocation constructorInvocation = JExpr._new(usedValueClassType);
                constructorInvocation.arg(caseClassConstructorInvocation);
                constructorMethod.body()._return(constructorInvocation);
            } else {
                JInvocation caseClassConstructorInvocation = JExpr._new(usedCaseClassType.erasure());
                JInvocation initializer = JExpr._new(usedValueClassType.erasure());
                initializer.arg(caseClassConstructorInvocation);
                JFieldVar singletonInstanceField = valueClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL,
                                                                    usedValueClassType.erasure(),
                                                                    interfaceMethod.name().toUpperCase(),
                                                                    initializer);
                JAnnotationUse fieldAnnotationUse = singletonInstanceField.annotate(SuppressWarnings.class);
                JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
                paramArray.param("unchecked");
                paramArray.param("rawtypes");

                JAnnotationUse methodAnnotationUse = constructorMethod.annotate(SuppressWarnings.class);
                methodAnnotationUse.param("value", "unchecked");
                JExpression result = usedValueClassType.getTypeParameters().isEmpty() ? singletonInstanceField : JExpr.cast(usedValueClassType, singletonInstanceField);
                constructorMethod.body()._return(result);
            }
            constructorMethods.put(interfaceMethod.name(), constructorMethod);
        }
        return constructorMethods;
    }

    private Map<String, JDefinedClass> buildCaseClasses() throws JClassAlreadyExistsException {
        Map<String, JDefinedClass> caseClasses = new TreeMap<>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JDefinedClass caseClass = buildCaseClass(interfaceMethod);
            caseClasses.put(interfaceMethod.name(), caseClass);
        }
        return caseClasses;
    }

    private JDefinedClass buildCaseClass(JMethod interfaceMethod) throws JClassAlreadyExistsException {
        JDefinedClass caseClass = valueClass._class(JMod.PRIVATE | JMod.STATIC, capitalize(interfaceMethod.name()) + "Case" + acceptingInterface.name());
        for (JTypeVar visitorTypeParameter: acceptingInterface.typeParams()) {
            JTypeVar typeArgument = caseClass.generify(visitorTypeParameter.name());
            typeArgument.bound(visitorTypeParameter._extends());
        }

        JClass usedAcceptingInterfaceType = Types.narrow(acceptingInterface, caseClass.typeParams());
        JClass usedValueClassType = Types.narrow(valueClass, caseClass.typeParams());
        caseClass._implements(usedAcceptingInterfaceType);

        JMethod constructor = caseClass.constructor(JMod.NONE);
        for (JVar param: interfaceMethod.params()) {
            JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException());
            caseClass.field(JMod.PRIVATE | JMod.FINAL, paramType, param.name());
            constructor.param(paramType, param.name());
            constructor.body().assign(JExpr._this().ref(param.name()), JExpr.ref(param.name()));
        }

        JMethod acceptMethod = caseClass.method(JMod.PUBLIC, types._void(), "accept");
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

        JClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
        JInvocation invocation = JExpr.invoke(JExpr.ref("visitor"), interfaceMethod.name());
        for (JVar param: interfaceMethod.params()) {
            invocation.arg(JExpr._this().ref(param.name()));
        }
        acceptMethod.body()._return(invocation);
        return caseClass;
    }

    private void buildEqualsMethod() {
        JMethod equalsMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._boolean(), "equals");
        equalsMethod.annotate(Override.class);
        JAnnotationUse annotationUse = equalsMethod.annotate(SuppressWarnings.class);
        annotationUse.param("value", "unchecked");
        equalsMethod.param(types._Object(), "thatObject");
        JFieldRef thatObject = JExpr.ref("thatObject");
        JConditional _if = equalsMethod.body()._if(JExpr._this().eq(thatObject));
        _if._then()._return(JExpr.TRUE);
        JConditional elseif = _if._elseif(thatObject._instanceof(valueClass).not());
        elseif._then()._return(JExpr.FALSE);
        JBlock _else = elseif._else();
        JClass usedValueClassType = Types.narrow(valueClass, valueClass.typeParams());
        _else.decl(JMod.FINAL, usedValueClassType, "that", JExpr.cast(usedValueClassType, thatObject));
        JClass visitorType = visitorInterface.narrowed(usedValueClassType, types._Boolean(), types._RuntimeException());

        JDefinedClass anonymousClass1 = valueClass.owner().anonymousClass(visitorType);
        for (JMethod interfaceMethod1: visitorInterface.methods()) {
            JMethod visitorMethod1 = anonymousClass1.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, types._Boolean(), interfaceMethod1.name());
            visitorMethod1.annotate(Override.class);
            for (JVar param: interfaceMethod1.params()) {
                JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Boolean(), types._RuntimeException());
                visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
            }

            JDefinedClass anonymousClass2 = valueClass.owner().anonymousClass(visitorType);
            for (JMethod interfaceMethod2: visitorInterface.methods()) {
                JMethod visitorMethod2 = anonymousClass2.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, types._Boolean(), interfaceMethod2.name());
                visitorMethod2.annotate(Override.class);
                for (JVar param: interfaceMethod2.params()) {
                    JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Boolean(), types._RuntimeException());
                    visitorMethod2.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "2");
                }
                if (!interfaceMethod1.name().equals(interfaceMethod2.name()))
                    visitorMethod2.body()._return(JExpr.FALSE);
                else {
                    EqualsBody body = new EqualsBody(visitorMethod2.body());
                    for (JVar param: interfaceMethod1.params()) {
                        JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Boolean(), types._RuntimeException());
                        JExpression field1 = JExpr.ref(param.name() + "1");
                        JExpression field2 = JExpr.ref(param.name() + "2");
                        body.appendNullableValue(paramType, field1, field2);
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
        JMethod hashCodeMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._int(), "hashCode");
        hashCodeMethod.annotate(Override.class);
        JClass usedValueClassType = Types.narrow(valueClass, valueClass.typeParams());
        JClass visitorType = visitorInterface.narrowed(usedValueClassType, types._Integer(), types._RuntimeException());

        JDefinedClass anonymousClass1 = valueClass.owner().anonymousClass(visitorType);
        int tag = 1;
        for (JMethod interfaceMethod1: visitorInterface.methods()) {
            JMethod visitorMethod1 = anonymousClass1.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, types._Integer(), interfaceMethod1.name());
            visitorMethod1.annotate(Override.class);
            JVar result = visitorMethod1.body().decl(types._int(), "result", JExpr.lit(tag));
            HashCodeBody body = new HashCodeBody(visitorMethod1.body(), result, visitorInterface.hashCodeBase());
            for (JVar param: interfaceMethod1.params()) {
                JType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Integer(), types._RuntimeException());
                JVar argument = visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
                body.appendNullableValue(paramType, argument);
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

        private void appendNullableValue(JType type, JExpression value1, JExpression value2) {
            if (type.isPrimitive()) {
                appendNotNullValue(type, value1, value2);
            } else {
                JConditional _if = body._if(value1.ne(value2));
                JExpression isNull = value1.eq(JExpr._null()).cor(value2.eq(JExpr._null()));
                JConditional _if1 = _if._then()._if(isNull);
                _if1._then()._return(JExpr.FALSE);
                EqualsBody innerBody = new EqualsBody(_if._then());
                innerBody.appendNotNullValue(type, value1, value2);
            }
        }

        private void appendNotNullValue(JType type, JExpression value1, JExpression value2) {
            if (type.isArray()) {
                appendNotNullValue(types._int(), value1.ref("length"), value2.ref("length"));

                JForLoop _for = body._for();
                _for.init(types._int(), "i", JExpr.lit(0));
                JFieldRef i = JExpr.ref("i");
                _for.test(i.lt(value1.ref("length")));
                _for.update(i.incr());
                EqualsBody forBody = new EqualsBody(_for.body());
                forBody.appendNullableValue(type.elementType(), value1.component(i), value2.component(i));
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
        private final int hashCodeBase;
        private HashCodeBody(JBlock body, JVar result, int hashCodeBase) {
            this.body = body;
            this.result = result;
            this.hashCodeBase = hashCodeBase;
        }

        private void appendNullableValue(JType type, JExpression value) {
            if (type.isPrimitive())
                appendNotNullValue(type, value);
            else {
                JConditional _if = body._if(value.eq(JExpr._null()));
                HashCodeBody thenBody = new HashCodeBody(_if._then(), result, hashCodeBase);
                thenBody.appendNotNullValue(types._int(), JExpr.lit(0));
                HashCodeBody elseBody = new HashCodeBody(_if._else(), result, hashCodeBase);
                elseBody.appendNotNullValue(type, value);
            }
        }

        private void appendNotNullValue(JType type, JExpression value) {
            if (type.isArray()) {
                JForLoop _for = body._for();
                _for.init(types._int(), "i", JExpr.lit(0));
                JFieldRef i = JExpr.ref("i");
                _for.test(i.lt(value.ref("length")));
                _for.update(i.incr());
                HashCodeBody forBody = new HashCodeBody(_for.body(), result, hashCodeBase);
                forBody.appendNullableValue(type.elementType(), value.component(i));
            } else if (!type.isPrimitive()) {
                appendNotNullValue(types._int(), value.invoke("hashCode"));
            } else if (type.name().equals("double")) {
                JInvocation invocation = types._Double().staticInvoke("doubleToLongBits");
                invocation.arg(value);
                appendNotNullValue(types._long(), invocation);
            } else if (type.name().equals("float")) {
                JInvocation invocation = types._Float().staticInvoke("floatToIntBits");
                invocation.arg(value);
                appendNotNullValue(types._int(), invocation);
            } else if (type.name().equals("boolean")) {
                appendNotNullValue(types._int(), JOp.cond(value, JExpr.lit(0), JExpr.lit(1)));
            } else if (type.name().equals("long")) {
                appendNotNullValue(types._int(), JExpr.cast(types._int(), value.xor(value.shrz(JExpr.lit(32)))));
            } else {
                body.assign(result, result.mul(JExpr.lit(hashCodeBase)).plus(value));
            }
        }
    }

}
