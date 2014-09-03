/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.adt4j;

import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JAnnotationArrayMember;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JForLoop;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JOp;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class ValueClassModel {
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
                                                          ValueVisitorInterfaceModel visitorInterface,
                                                          Types types) throws JClassAlreadyExistsException {

        JDefinedClass acceptingInterface = valueClass._class(JMod.PUBLIC, valueClass.name() + "Acceptor", EClassType.INTERFACE);

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

        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        AbstractJClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");

        return acceptingInterface;
    }

    public static ValueClassModel createInstance(JCodeModel codeModel, ValueVisitorInterfaceModel visitorInterface) throws SourceException, CodeGenerationException {
        try {
            Types types = Types.createInstance(codeModel);
            boolean isSerializable = visitorInterface.shouldBeSerializable();
            if (isSerializable) {
                for (JMethod interfaceMethod: visitorInterface.methods()) {
                    for (JVar param: interfaceMethod.params()) {
                        AbstractJType type = param.type();
                        if (!visitorInterface.isSelf(type) && !types.isSerializable(type))
                            throw new SourceException("Value class can't be serializable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not serializable");
                    }
                }
            }

            String valueClassName = visitorInterface.getValueClassName();

            int mods = visitorInterface.generatesPublicClass() ? JMod.PUBLIC: JMod.NONE;
            JDefinedClass valueClass = codeModel._class(mods, visitorInterface.getPackageName() + "." + valueClassName, EClassType.CLASS);
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeParameter = valueClass.generify(visitorTypeParameter.name());
                typeParameter.bound(visitorTypeParameter._extends());
            }
            if (isSerializable) {
                valueClass._implements(types._Serializable());
                valueClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, types._long(), "serialVersionUID", JExpr.lit(visitorInterface.serialVersionUID()));
            }

            JDefinedClass acceptingInterface = createAcceptingInterface(valueClass, visitorInterface, types);
            if (isSerializable) {
                acceptingInterface._extends(types._Serializable());
            }

            ValueClassModel result = new ValueClassModel(valueClass, acceptingInterface, visitorInterface, types);
            JFieldVar acceptorField = result.buildAcceptorField();
            result.buildPrivateConstructor(acceptorField);
            result.buildProtectedConstructor(acceptorField);
            result.buildAcceptMethod(acceptorField);
            result.buildEqualsMethod();
            result.buildHashCodeMethod();
            result.buildToStringMethod();
            result.buildGetters();
            result.buildUpdaters();
            Map<String, JMethod> constructorMethods = result.buildConstructorMethods();
            result.buildFactory(constructorMethods);

            return result;
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
    }

    private static boolean isNullable(JVar param) throws SourceException {
        boolean hasNonnull = false;
        boolean hasNullable = false;
        for (JAnnotationUse annotationUse: param.annotations()) {
            if (annotationUse.getAnnotationClass().fullName().equals("javax.annotation.Nonnull")) {
                hasNonnull = true;
            }
            if (annotationUse.getAnnotationClass().fullName().equals("javax.annotation.Nullable")) {
                hasNullable = true;
            }
        }
        if (hasNonnull && hasNullable)
            throw new SourceException("Parameter " + param.name() + " is declared as both @Nullable and @Nonnull");
        if (!param.type().isReference() && hasNullable)
            throw new SourceException("Parameter " + param.name() + " is non-reference, but declared as @Nullable");
        return hasNullable;
    }

    private final JDefinedClass valueClass;
    private final JDefinedClass acceptingInterface;
    private final ValueVisitorInterfaceModel visitorInterface;
    private final Types types;

    private ValueClassModel(JDefinedClass valueClass, JDefinedClass acceptingInterface, ValueVisitorInterfaceModel visitorInterface, Types modelTypes) {
        this.valueClass = valueClass;
        this.acceptingInterface = acceptingInterface;
        this.visitorInterface = visitorInterface;
        this.types = modelTypes;
    }

    private JFieldVar buildAcceptorField() {
        AbstractJType usedAcceptingInterfaceType = acceptingInterface.narrow(valueClass.typeParams());
        return valueClass.field(JMod.PRIVATE | JMod.FINAL, usedAcceptingInterfaceType, "acceptor");
    }

    private void buildPrivateConstructor(JFieldVar acceptorField) {
        JMethod constructor = valueClass.constructor(JMod.PRIVATE);
        constructor.param(acceptorField.type(), acceptorField.name());
        constructor.body().assign(JExpr.refthis(acceptorField.name()), JExpr.ref(acceptorField.name()));
    }

    private void buildProtectedConstructor(JFieldVar acceptorField) throws JClassAlreadyExistsException {
        JMethod constructor = valueClass.constructor(JMod.PROTECTED);
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        JVar param = constructor.param(JMod.FINAL, usedValueClassType, "implementation");
        param.annotate(Nonnull.class);
        JConditional nullCheck = constructor.body()._if(JExpr.ref("implementation").eq(JExpr._null()));
        JInvocation nullPointerExceptionConstruction = JExpr._new(types._NullPointerException());
        nullPointerExceptionConstruction.arg(JExpr.lit("Argument shouldn't be null: 'implementation' argument in class constructor invocation: " + valueClass.fullName()));
        nullCheck._then()._throw(nullPointerExceptionConstruction);

        JDefinedClass proxyClass = createProxyClass();
        AbstractJClass usedProxyClassType = proxyClass.narrow(valueClass.typeParams());
        JInvocation construction = JExpr._new(usedProxyClassType);
        construction.arg(JExpr.ref("implementation"));
        constructor.body().assign(JExpr.refthis(acceptorField.name()), construction);
    }

    private JDefinedClass createProxyClass() throws JClassAlreadyExistsException {
        JDefinedClass proxyClass = valueClass._class(JMod.PRIVATE | JMod.STATIC, "Proxy" + acceptingInterface.name(), EClassType.CLASS);
        for (JTypeVar visitorTypeParameter: acceptingInterface.typeParams()) {
            JTypeVar typeArgument = proxyClass.generify(visitorTypeParameter.name());
            typeArgument.bound(visitorTypeParameter._extends());
        }
        AbstractJClass usedAcceptingInterfaceType = acceptingInterface.narrow(proxyClass.typeParams());
        proxyClass._implements(usedAcceptingInterfaceType);

        if (visitorInterface.shouldBeSerializable()) {
            proxyClass._implements(types._Serializable());
            proxyClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, types._long(), "serialVersionUID", JExpr.lit(visitorInterface.serialVersionUID()));
        }

        JMethod constructor = proxyClass.constructor(JMod.NONE);
        AbstractJClass usedValueClassType = valueClass.narrow(proxyClass.typeParams());
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

        AbstractJClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
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

        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        AbstractJClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
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
        AbstractJClass usedValueClassType = valueClass.narrow(factoryMethod.typeParams());
        AbstractJClass usedFactoryType = factory.narrow(factoryMethod.typeParams());
        factoryMethod.type(visitorInterface.narrowed(usedValueClassType, usedValueClassType, types._RuntimeException()));
        IJExpression result = JExpr.ref("FACTORY");
        result = usedFactoryType.getTypeParameters().isEmpty() ? result : JExpr.cast(usedFactoryType, result);
        factoryMethod.body()._return(result);
        return factoryMethod;
    }

    private JDefinedClass buildFactoryClass(Map<String, JMethod> constructorMethods) throws JClassAlreadyExistsException {
        AbstractJType runtimeException = types._RuntimeException();
        JDefinedClass factoryClass = valueClass._class(JMod.PRIVATE | JMod.STATIC, valueClass.name() + "Factory", EClassType.CLASS);
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = factoryClass.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
        }
        AbstractJClass usedValueClassType = valueClass.narrow(factoryClass.typeParams());
        factoryClass._implements(visitorInterface.narrowed(usedValueClassType, usedValueClassType, runtimeException));
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod factoryMethod = factoryClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT, usedValueClassType, interfaceMethod.name());
            factoryMethod.annotate(Override.class);

            JMethod constructorMethod = constructorMethods.get(interfaceMethod.name());
            JInvocation staticInvoke = valueClass.staticInvoke(constructorMethod);
            for (JVar param: interfaceMethod.params()) {
                AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, usedValueClassType, runtimeException);
                factoryMethod.param(param.mods().getValue() | JMod.FINAL, paramType, param.name());
                staticInvoke.arg(JExpr.ref(param.name()));
            }
            factoryMethod.body()._return(staticInvoke);
        }
        return factoryClass;
    }

    private Map<String, JMethod> buildConstructorMethods() throws JClassAlreadyExistsException, SourceException {
        Map<String, JDefinedClass> caseClasses = buildCaseClasses();

        Map<String, JMethod> constructorMethods = new TreeMap<String, JMethod>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod constructorMethod = valueClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT | JMod.STATIC, types._void(), interfaceMethod.name());
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeParameter = constructorMethod.generify(visitorTypeParameter.name());
                typeParameter.bound(visitorTypeParameter._extends());
            }
            AbstractJClass usedValueClassType = valueClass.narrow(constructorMethod.typeParams());
            constructorMethod.type(usedValueClassType);
            for (JVar param: interfaceMethod.params()) {
                AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException());
                JVar constructorMethodParam = constructorMethod.param(param.mods().getValue(), paramType, param.name());
                if (param.type().isReference())
                    constructorMethodParam.annotate(isNullable(param) ? Nullable.class : Nonnull.class);
            }

            AbstractJClass usedCaseClassType = caseClasses.get(interfaceMethod.name()).narrow(constructorMethod.typeParams());
            if (!interfaceMethod.params().isEmpty()) {
                for (JVar param: interfaceMethod.params()) {
                    if (param.type().isReference() && !isNullable(param)) {
                        JConditional nullCheck = constructorMethod.body()._if(JExpr.ref(param.name()).eq(JExpr._null()));
                        JInvocation nullPointerExceptionConstruction = JExpr._new(types._NullPointerException());
                        nullPointerExceptionConstruction.arg(JExpr.lit("Argument shouldn't be null: '" + param.name() + "' argument in static method invocation: '" + constructorMethod.name() + "' in class " + valueClass.fullName()));
                        nullCheck._then()._throw(nullPointerExceptionConstruction);
                    }
                }
                JInvocation caseClassConstructorInvocation = JExpr._new(usedCaseClassType);
                for (JVar param: interfaceMethod.params()) {
                    caseClassConstructorInvocation.arg(JExpr.ref(param.name()));
                }
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
                IJExpression result = usedValueClassType.getTypeParameters().isEmpty() ? singletonInstanceField : JExpr.cast(usedValueClassType, singletonInstanceField);
                constructorMethod.body()._return(result);
            }
            constructorMethods.put(interfaceMethod.name(), constructorMethod);
        }
        return constructorMethods;
    }

    private Map<String, JDefinedClass> buildCaseClasses() throws JClassAlreadyExistsException {
        Map<String, JDefinedClass> caseClasses = new TreeMap<String, JDefinedClass>();
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

        AbstractJClass usedAcceptingInterfaceType = acceptingInterface.narrow(caseClass.typeParams());
        AbstractJClass usedValueClassType = valueClass.narrow(caseClass.typeParams());
        caseClass._implements(usedAcceptingInterfaceType);

        if (visitorInterface.shouldBeSerializable()) {
            caseClass._implements(types._Serializable());
            caseClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, types._long(), "serialVersionUID", JExpr.lit(visitorInterface.serialVersionUID()));
        }

        JMethod constructor = caseClass.constructor(JMod.NONE);
        for (JVar param: interfaceMethod.params()) {
            AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException());
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

        AbstractJClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
        JInvocation invocation = JExpr.invoke(JExpr.ref("visitor"), interfaceMethod.name());
        for (JVar param: interfaceMethod.params()) {
            invocation.arg(JExpr._this().ref(param.name()));
        }
        acceptMethod.body()._return(invocation);
        return caseClass;
    }

    private void buildEqualsMethod() throws SourceException {
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
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        _else.decl(JMod.FINAL, usedValueClassType, "that", JExpr.cast(usedValueClassType, thatObject));
        AbstractJClass visitorType = visitorInterface.narrowed(usedValueClassType, types._Boolean(), types._RuntimeException());

        JDefinedClass anonymousClass1 = valueClass.owner().anonymousClass(visitorType);
        for (JMethod interfaceMethod1: visitorInterface.methods()) {
            JMethod visitorMethod1 = anonymousClass1.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, types._Boolean(), interfaceMethod1.name());
            visitorMethod1.annotate(Override.class);
            for (JVar param: interfaceMethod1.params()) {
                AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Boolean(), types._RuntimeException());
                visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
            }

            JDefinedClass anonymousClass2 = valueClass.owner().anonymousClass(visitorType);
            for (JMethod interfaceMethod2: visitorInterface.methods()) {
                JMethod visitorMethod2 = anonymousClass2.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, types._Boolean(), interfaceMethod2.name());
                visitorMethod2.annotate(Override.class);
                for (JVar param: interfaceMethod2.params()) {
                    AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Boolean(), types._RuntimeException());
                    visitorMethod2.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "2");
                }
                if (!interfaceMethod1.name().equals(interfaceMethod2.name()))
                    visitorMethod2.body()._return(JExpr.FALSE);
                else {
                    EqualsBody body = new EqualsBody(visitorMethod2.body());
                    for (JVar param: interfaceMethod1.params()) {
                        AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Boolean(), types._RuntimeException());
                        IJExpression field1 = JExpr.ref(param.name() + "1");
                        IJExpression field2 = JExpr.ref(param.name() + "2");
                        if (isNullable(param))
                            body.appendNullableValue(paramType, field1, field2);
                        else
                            body.appendNotNullValue(paramType, field1, field2);
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

    private void buildHashCodeMethod() throws SourceException {
        JMethod hashCodeMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._int(), "hashCode");
        hashCodeMethod.annotate(Override.class);
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        AbstractJClass visitorType = visitorInterface.narrowed(usedValueClassType, types._Integer(), types._RuntimeException());

        JDefinedClass anonymousClass1 = valueClass.owner().anonymousClass(visitorType);
        int tag = 1;
        for (JMethod interfaceMethod1: visitorInterface.methods()) {
            JMethod visitorMethod1 = anonymousClass1.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, types._Integer(), interfaceMethod1.name());
            visitorMethod1.annotate(Override.class);
            JVar result = visitorMethod1.body().decl(types._int(), "result", JExpr.lit(tag));
            HashCodeBody body = new HashCodeBody(visitorMethod1.body(), result, visitorInterface.hashCodeBase());
            for (JVar param: interfaceMethod1.params()) {
                AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Integer(), types._RuntimeException());
                JVar argument = visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
                if (isNullable(param))
                    body.appendNullableValue(paramType, argument);
                else
                    body.appendNotNullValue(paramType, argument);
            }
            visitorMethod1.body()._return(result);
            tag++;
        }
        JInvocation invocation1 = JExpr._this().invoke("accept");
        invocation1.arg(JExpr._new(anonymousClass1));
        hashCodeMethod.body()._return(invocation1);
    }

    private void buildToStringMethod() throws SourceException {
        JMethod toStringMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._String(), "toString");
        toStringMethod.annotate(Override.class);
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        AbstractJClass visitorType = visitorInterface.narrowed(usedValueClassType, types._String(), types._RuntimeException());

        JDefinedClass anonymousClass1 = valueClass.owner().anonymousClass(visitorType);
        for (JMethod interfaceMethod1: visitorInterface.methods()) {
            JMethod visitorMethod1 = anonymousClass1.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, types._String(), interfaceMethod1.name());
            visitorMethod1.annotate(Override.class);
            JVar result = visitorMethod1.body().decl(types._StringBuilder(), "result", JExpr._new(types._StringBuilder()));
            JInvocation invocation = visitorMethod1.body().invoke(result, "append");
            invocation.arg(valueClass.name() + "." + capitalize(interfaceMethod1.name()) + "{");
            ToStringBody body = new ToStringBody(visitorMethod1.body(), result);
            Iterator<JVar> iterator = interfaceMethod1.params().iterator();
            if (iterator.hasNext()) {
                JVar param = iterator.next();
                AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Integer(), types._RuntimeException());
                JVar argument = visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
                body.appendParam(paramType, param.name(), JExpr.ref(argument));
                while (iterator.hasNext()) {
                    param = iterator.next();
                    paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, types._Integer(), types._RuntimeException());
                    argument = visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
                    invocation = visitorMethod1.body().invoke(result, "append");
                    invocation.arg(", ");
                    body.appendParam(paramType, param.name(), JExpr.ref(argument));
                }
            }
            invocation = visitorMethod1.body().invoke(result, "append");
            invocation.arg("}");
            visitorMethod1.body()._return(result.invoke("toString"));
        }
        JInvocation invocation1 = JExpr._this().invoke("accept");
        invocation1.arg(JExpr._new(anonymousClass1));
        toStringMethod.body()._return(invocation1);
    }

    private void buildGetters() throws SourceException {
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> gettersMap = new TreeMap<String, FieldConfiguration>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            for (JVar param: interfaceMethod.params()) {
                for (JAnnotationUse annotationUsage: param.annotations()) {
                    if (annotationUsage.getAnnotationClass().fullName().equals(Getter.class.getName())) {
                        AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException());
                        String getterName = (String)annotationUsage.getConstantParam("value").nativeValue();
                        FieldConfiguration configuration = gettersMap.get(getterName);
                        if (configuration == null) {
                            gettersMap.put(getterName, new FieldConfiguration(getterName, interfaceMethod, paramType, param.name()));
                        } else {
                            configuration.put(interfaceMethod, paramType, param.name());
                        }
                    }
                }
            }
        }
        for (FieldConfiguration configuration: gettersMap.values()) {
            generateGetter(configuration);
        }
    }

    private void generateGetter(FieldConfiguration configuration) {
        String getterName = configuration.name();
        JMethod getterMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, configuration.type(), getterName);
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        AbstractJClass visitorType = visitorInterface.narrowed(usedValueClassType, configuration.type().boxify(), types._RuntimeException());

        JDefinedClass anonymousClass1 = valueClass.owner().anonymousClass(visitorType);
        GetterBody body = new GetterBody(configuration, anonymousClass1);
        for (JMethod interfaceMethod1: visitorInterface.methods()) {
            body.generateCase(interfaceMethod1);
        }
        JInvocation invocation1 = JExpr._this().invoke("accept");
        invocation1.arg(JExpr._new(anonymousClass1));
        getterMethod.body()._return(invocation1);
    }

    private void buildUpdaters() throws SourceException {
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> updatersMap = new TreeMap<String, FieldConfiguration>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            for (JVar param: interfaceMethod.params()) {
                for (JAnnotationUse annotationUsage: param.annotations()) {
                    if (annotationUsage.getAnnotationClass().fullName().equals(Updater.class.getName())) {
                        AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException());
                        String updaterName = (String)annotationUsage.getConstantParam("value").nativeValue();
                        FieldConfiguration configuration = updatersMap.get(updaterName);
                        if (configuration == null) {
                            updatersMap.put(updaterName, new FieldConfiguration(updaterName, interfaceMethod, paramType, param.name()));
                        } else {
                            configuration.put(interfaceMethod, paramType, param.name());
                        }
                    }
                }
            }
        }
        for (FieldConfiguration configuration: updatersMap.values()) {
            generateUpdater(configuration);
        }
    }

    private void generateUpdater(FieldConfiguration configuration) {
        String updaterName = configuration.name();
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        JMethod updaterMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, usedValueClassType, updaterName);
        JVar newValue = updaterMethod.param(JMod.FINAL, configuration.type(), "value");
        AbstractJClass visitorType = visitorInterface.narrowed(usedValueClassType, usedValueClassType, types._RuntimeException());

        JDefinedClass anonymousClass1 = valueClass.owner().anonymousClass(visitorType);
        UpdaterBody body = new UpdaterBody(configuration, anonymousClass1, newValue);
        for (JMethod interfaceMethod1: visitorInterface.methods()) {
            body.generateCase(interfaceMethod1);
        }
        JInvocation invocation1 = JExpr._this().invoke("accept");
        invocation1.arg(JExpr._new(anonymousClass1));
        updaterMethod.body()._return(invocation1);
    }

    private class GetterBody {
        JDefinedClass visitor;
        private final FieldConfiguration configuration;
        GetterBody(FieldConfiguration configuration, JDefinedClass visitor) {
            this.visitor = visitor;
            this.configuration = configuration;
        }

        private void generateCase(JMethod interfaceMethod1) {
            AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
            JMethod visitorMethod1 = visitor.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, configuration.type().boxify(), interfaceMethod1.name());
            visitorMethod1.annotate(Override.class);
            boolean isGettable = false;
            for (JVar param: interfaceMethod1.params()) {
                AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, configuration.type().boxify(), types._RuntimeException());
                JVar argument = visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
                if (configuration.isFieldValue(interfaceMethod1, param.name())) {
                    visitorMethod1.body()._return(argument);
                    isGettable = true;
                }
            }
            if (!isGettable) {
                JInvocation exceptionInvocation = JExpr._new(types._IllegalStateException());
                exceptionInvocation.arg(configuration.name() + " is not accessible in this case: " + interfaceMethod1.name());
                visitorMethod1.body()._throw(exceptionInvocation);
            }
        }
    }

    private class UpdaterBody {
        JDefinedClass visitor;
        private final FieldConfiguration configuration;
        private final JVar newValue;
        UpdaterBody(FieldConfiguration configuration, JDefinedClass visitor, JVar newValue) {
            this.visitor = visitor;
            this.configuration = configuration;
            this.newValue = newValue;
        }

        private void generateCase(JMethod interfaceMethod1) {
            AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
            JMethod visitorMethod1 = visitor.method(interfaceMethod1.mods().getValue() & ~JMod.ABSTRACT, usedValueClassType, interfaceMethod1.name());
            visitorMethod1.annotate(Override.class);
            JInvocation invocation = valueClass.staticInvoke(interfaceMethod1.name());
            for (JVar param: interfaceMethod1.params()) {
                AbstractJType paramType = visitorInterface.substituteTypeParameter(param.type(), usedValueClassType, configuration.type().boxify(), types._RuntimeException());
                JVar argument = visitorMethod1.param(param.mods().getValue() | JMod.FINAL, paramType, param.name() + "1");
                if (configuration.isFieldValue(interfaceMethod1, param.name())) {
                    invocation.arg(newValue);
                } else {
                    invocation.arg(argument);
                }
            }
            visitorMethod1.body()._return(invocation);
        }
    }

    private class EqualsBody {
        private final JBlock body;
        private EqualsBody(JBlock body) {
            this.body = body;
        }

        private void appendNullableValue(AbstractJType type, IJExpression value1, IJExpression value2) {
            if (!type.isReference()) {
                throw new AssertionError("appendNullableValue called for non-reference type");
            } else {
                JConditional _if = body._if(value1.ne(value2));
                IJExpression isNull = value1.eq(JExpr._null()).cor(value2.eq(JExpr._null()));
                JConditional _if1 = _if._then()._if(isNull);
                _if1._then()._return(JExpr.FALSE);
                EqualsBody innerBody = new EqualsBody(_if._then());
                innerBody.appendNotNullValue(type, value1, value2);
            }
        }

        private void appendNotNullValue(AbstractJType type, IJExpression value1, IJExpression value2) {
            if (type.isArray()) {
                appendNotNullValue(types._int(), value1.ref("length"), value2.ref("length"));

                JForLoop _for = body._for();
                _for.init(types._int(), "i", JExpr.lit(0));
                JFieldRef i = JExpr.ref("i");
                _for.test(i.lt(value1.ref("length")));
                _for.update(i.incr());
                EqualsBody forBody = new EqualsBody(_for.body());
                if (type.elementType().isReference())
                    forBody.appendNullableValue(type.elementType(), value1.component(i), value2.component(i));
                else
                    forBody.appendNotNullValue(type.elementType(), value1.component(i), value2.component(i));
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

        private void appendNullableValue(AbstractJType type, IJExpression value) {
            if (!type.isReference())
                throw new AssertionError("appendNullableValue called for non-reference type");
            else {
                JConditional _if = body._if(value.eq(JExpr._null()));
                HashCodeBody thenBody = new HashCodeBody(_if._then(), result, hashCodeBase);
                thenBody.appendNotNullValue(types._int(), JExpr.lit(0));
                HashCodeBody elseBody = new HashCodeBody(_if._else(), result, hashCodeBase);
                elseBody.appendNotNullValue(type, value);
            }
        }

        private void appendNotNullValue(AbstractJType type, IJExpression value) {
            if (type.isArray()) {
                JForLoop _for = body._for();
                _for.init(types._int(), "i", JExpr.lit(0));
                JFieldRef i = JExpr.ref("i");
                _for.test(i.lt(value.ref("length")));
                _for.update(i.incr());
                HashCodeBody forBody = new HashCodeBody(_for.body(), result, hashCodeBase);
                if (type.elementType().isReference())
                    forBody.appendNullableValue(type.elementType(), value.component(i));
                else
                    forBody.appendNotNullValue(type.elementType(), value.component(i));
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

    private class ToStringBody {
        private final JBlock body;
        private final JVar result;

        public ToStringBody(JBlock body, JVar result) {
            this.body = body;
            this.result = result;
        }

        private void appendParam(AbstractJType type, String name, IJExpression value) {
            JInvocation invocation = body.invoke(result, "append");
            invocation.arg(name + " = ");
            invocation = body.invoke(result, "append");
            invocation.arg(value);
        }
    }

    private static class FieldConfiguration {
        private final Map<String, String> map = new TreeMap<String, String>();
        private final AbstractJType type;
        private final String name;

        private FieldConfiguration(String name, JMethod method, AbstractJType paramType, String paramName) {
            this.type = paramType;
            map.put(method.name(), paramName);
            this.name = name;
        }

        private void put(JMethod method, AbstractJType paramType, String paramName) throws SourceException {
            if (!type.equals(paramType))
                throw new SourceException("Unable to generate " + name + " getter: inconsitent field types");
            String oldField = map.put(method.name(), paramName);
            if (oldField != null)
                throw new SourceException(oldField + " and " + paramName + " parameters of " + method.name() + " method are accessable with the same " + name + " getter");
        }

        private AbstractJType type() {
            return type;
        }

        private String name() {
            return name;
        }

        private boolean isFieldValue(JMethod method, String paramName) {
            String getterParamName = map.get(method.name());
            return getterParamName != null && getterParamName.equals(paramName);
        }
    }
}
