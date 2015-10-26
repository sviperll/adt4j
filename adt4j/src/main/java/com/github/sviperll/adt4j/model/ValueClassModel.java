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
package com.github.sviperll.adt4j.model;

import com.github.sviperll.adt4j.Caching;
import com.github.sviperll.adt4j.model.util.Serialization;
import com.github.sviperll.adt4j.model.util.Source;
import com.github.sviperll.adt4j.model.util.Types;
import com.github.sviperll.adt4j.model.util.ValueVisitorInterfaceModel;
import com.github.sviperll.adt4j.model.util.VariableNameSource;
import com.github.sviperll.meta.SourceCodeValidationException;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JAnnotationArrayMember;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JOp;
import com.helger.jcodemodel.JSynchronizedBlock;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

class ValueClassModel {
    static final IJExpression FLOAT_EPSILON = JExpr.lit(0.000001f);
    static final IJExpression DOUBLE_EPSILON = JExpr.lit(0.000000000001);

    private final JDefinedClass valueClass;
    private final JDefinedClass acceptingInterface;
    private final ValueVisitorInterfaceModel visitorInterface;
    private final Types types;

    ValueClassModel(JDefinedClass valueClass, JDefinedClass acceptingInterface, ValueVisitorInterfaceModel visitorInterface, Types modelTypes) {
        this.valueClass = valueClass;
        this.acceptingInterface = acceptingInterface;
        this.visitorInterface = visitorInterface;
        this.types = modelTypes;
    }

    JDefinedClass getJDefinedClass() {
        return valueClass;
    }

    String hashCodeAcceptorMethodName() {
        return Source.decapitalize(valueClass.name()) + "HashCode";
    }

    MethodBuilder createMethodBuilder(Serialization serialization) throws JClassAlreadyExistsException {
        JFieldVar acceptorField = buildAcceptorField();
        Map<String, JDefinedClass> caseClasses = buildCaseClasses(serialization);
        Caching hashCode = visitorInterface.hashCodeCaching();
        if (!hashCode.enabled())
            return new MethodBuilder(caseClasses, acceptorField);
        else {
            JFieldVar hashCodeField = buildHashCodeCachedValueField(serialization);
            return new MethodBuilder(caseClasses, acceptorField, hashCodeField);
        }
    }

    private JFieldVar buildAcceptorField() {
        AbstractJType usedAcceptingInterfaceType = acceptingInterface.narrow(valueClass.typeParams());
        return valueClass.field(JMod.PRIVATE | JMod.FINAL, usedAcceptingInterfaceType, "acceptor");
    }

    JMethod buildFactory(Map<String, JMethod> constructorMethods) throws JClassAlreadyExistsException {
        JDefinedClass factory = buildFactoryClass(constructorMethods);

        JFieldVar factoryField = valueClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, factory, "FACTORY");
        JAnnotationUse fieldAnnotationUse = factoryField.annotate(SuppressWarnings.class);
        JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
        paramArray.param("unchecked");
        paramArray.param("rawtypes");

        factoryField.init(JExpr._new(factory));
        JMethod factoryMethod = valueClass.method(Source.toJMod(visitorInterface.factoryMethodAccessLevel()) | JMod.STATIC, types._void, "factory");
        Source.annotateNonnull(factoryMethod);
        JAnnotationUse methodAnnotationUse = factoryMethod.annotate(SuppressWarnings.class);
        methodAnnotationUse.param("value", "unchecked");
        for (JTypeVar visitorTypeParameter: visitorInterface.getValueTypeParameters()) {
            Types.generifyWithBoundsFrom(factoryMethod, visitorTypeParameter.name(), visitorTypeParameter);
        }
        AbstractJClass usedValueClassType = valueClass.narrow(factoryMethod.typeParams());
        factoryMethod.type(visitorInterface.narrowed(usedValueClassType, usedValueClassType, types._RuntimeException));
        factoryMethod.body()._return(factoryField);
        return factoryMethod;
    }

    private JDefinedClass buildFactoryClass(Map<String, JMethod> constructorMethods) throws JClassAlreadyExistsException {
        JDefinedClass factoryClass = valueClass._class(JMod.PRIVATE | JMod.STATIC, valueClass.name() + "Factory", EClassType.CLASS);
        for (JTypeVar visitorTypeParameter: visitorInterface.getValueTypeParameters()) {
            Types.generifyWithBoundsFrom(factoryClass, visitorTypeParameter.name(), visitorTypeParameter);
        }
        AbstractJClass usedValueClassType = valueClass.narrow(factoryClass.typeParams());
        factoryClass._implements(visitorInterface.narrowed(usedValueClassType, usedValueClassType, types._RuntimeException));
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod factoryMethod = factoryClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT, usedValueClassType, interfaceMethod.name());
            Source.annotateNonnull(factoryMethod);
            factoryMethod.annotate(Override.class);

            JMethod constructorMethod = constructorMethods.get(interfaceMethod.name());
            JInvocation staticInvoke = valueClass.staticInvoke(constructorMethod);
            for (JTypeVar typeArgument: factoryClass.typeParams())
                staticInvoke.narrow(typeArgument);
            for (JVar param: interfaceMethod.params()) {
                AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException));
                JVar argument = factoryMethod.param(param.mods().getValue(), argumentType, param.name());
                staticInvoke.arg(argument);
            }
            JVar param = interfaceMethod.varParam();
            if (param != null) {
                AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(param.type().elementType(), usedValueClassType, usedValueClassType, types._RuntimeException));
                JVar argument = factoryMethod.varParam(param.mods().getValue(), argumentType, param.name());
                staticInvoke.arg(argument);
            }
            factoryMethod.body()._return(staticInvoke);
        }
        return factoryClass;
    }

    private Map<String, JDefinedClass> buildCaseClasses(Serialization serialization) throws JClassAlreadyExistsException {
        Map<String, JDefinedClass> caseClasses = new TreeMap<String, JDefinedClass>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JDefinedClass caseClass = buildCaseClass(interfaceMethod, serialization);
            caseClasses.put(interfaceMethod.name(), caseClass);
        }
        return caseClasses;
    }

    private JDefinedClass buildCaseClass(JMethod interfaceMethod, Serialization serialization) throws JClassAlreadyExistsException {
        JDefinedClass caseClass = valueClass._class(JMod.PRIVATE | JMod.STATIC, Source.capitalize(interfaceMethod.name()) + "Case" + acceptingInterface.name());
        for (JTypeVar visitorTypeParameter: acceptingInterface.typeParams()) {
            Types.generifyWithBoundsFrom(caseClass, visitorTypeParameter.name(), visitorTypeParameter);
        }

        AbstractJClass usedAcceptingInterfaceType = acceptingInterface.narrow(caseClass.typeParams());
        AbstractJClass usedValueClassType = valueClass.narrow(caseClass.typeParams());
        caseClass._implements(usedAcceptingInterfaceType);

        if (serialization.isSerializable()) {
            caseClass._implements(types._Serializable);
            caseClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, types._long, "serialVersionUID", JExpr.lit(serialization.serialVersionUIDForGeneratedCode()));
        }

        JMethod constructor = caseClass.constructor(JMod.NONE);
        for (JVar param: interfaceMethod.params()) {
            AbstractJType paramType = Source.toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException));
            JFieldVar field = caseClass.field(JMod.PRIVATE | JMod.FINAL, paramType, param.name());
            JVar argument = constructor.param(paramType, param.name());
            constructor.body().assign(JExpr._this().ref(field), argument);
        }
        JVar param = interfaceMethod.varParam();
        if (param != null) {
            AbstractJType paramType = Source.toDeclarable(visitorInterface.narrowType(param.type().elementType(), usedValueClassType, usedValueClassType, types._RuntimeException));
            JFieldVar field = caseClass.field(JMod.PRIVATE | JMod.FINAL, paramType.array(), param.name());
            JVar argument = constructor.varParam(paramType, param.name());
            constructor.body().assign(JExpr._this().ref(field), argument);
        }

        JMethod acceptMethod = caseClass.method(JMod.PUBLIC, types._void, visitorInterface.acceptMethodName());
        acceptMethod.annotate(Override.class);

        JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
        JTypeVar resultType = Types.generifyWithBoundsFrom(acceptMethod, visitorResultType.name(), visitorResultType);
        acceptMethod.type(resultType);

        JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            exceptionType = Types.generifyWithBoundsFrom(acceptMethod, visitorExceptionType.name(), visitorExceptionType);
            acceptMethod._throws(exceptionType);
        }

        AbstractJClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
        JInvocation invocation = JExpr.invoke(JExpr.ref("visitor"), interfaceMethod.name());
        for (JVar param1: interfaceMethod.params()) {
            invocation.arg(JExpr._this().ref(param1.name()));
        }
        JVar param1 = interfaceMethod.varParam();
        if (param1 != null) {
            invocation.arg(JExpr._this().ref(param1.name()));
        }
        acceptMethod.body()._return(invocation);

        return caseClass;
    }

    Map<String, FieldConfiguration> getGettersConfigutation() throws SourceCodeValidationException {
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> gettersMap = new TreeMap<String, FieldConfiguration>();
        FieldReader reader = new FieldReader(gettersMap);
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            for (JVar param: interfaceMethod.params()) {
                AbstractJType paramType = Source.toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException));
                reader.readGetter(interfaceMethod, param, paramType, false);
            }
            JVar param = interfaceMethod.varParam();
            if (param != null) {
                AbstractJType paramType = Source.toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException));
                reader.readGetter(interfaceMethod, param, paramType, true);
            }
        }
        return gettersMap;
    }

    Map<String, FieldConfiguration> getUpdatersConfiguration() throws SourceCodeValidationException {
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> updatersMap = new TreeMap<String, FieldConfiguration>();
        FieldReader reader = new FieldReader(updatersMap);
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            for (JVar param: interfaceMethod.params()) {
                AbstractJType paramType = Source.toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException));
                reader.readUpdater(interfaceMethod, param, paramType, false);
            }
            JVar param = interfaceMethod.varParam();
            if (param != null) {
                AbstractJType paramType = Source.toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException));
                reader.readUpdater(interfaceMethod, param, paramType, true);
            }
        }
        return updatersMap;
    }

    Map<String, PredicateConfigutation> getPredicates() throws SourceCodeValidationException {
        Map<String, PredicateConfigutation> predicates = new TreeMap<String, PredicateConfigutation>();
        PredicatesReader predicatesReader = new PredicatesReader(predicates);
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            for (JAnnotationUse annotationUsage: interfaceMethod.annotations()) {
                predicatesReader.read(interfaceMethod, annotationUsage);
            }
        }
        return predicates;
    }

    private JFieldVar buildHashCodeCachedValueField(Serialization serialization) {
        if (!visitorInterface.hashCodeCaching().enabled())
            throw new IllegalStateException("Unsupported method evaluation to cache hash code: " + visitorInterface.hashCodeCaching());
        else {
            boolean isSerializable = serialization.isSerializable();
            boolean precomputes = visitorInterface.hashCodeCaching() == Caching.PRECOMPUTE;
            int mods = JMod.PRIVATE;
            mods = !isSerializable ? mods : mods | JMod.TRANSIENT;
            if (!precomputes)
                return valueClass.field(mods, types._int, "hashCodeCachedValue", JExpr.lit(0));
            else {
                mods = isSerializable ? mods : mods | JMod.FINAL;
                return valueClass.field(mods, types._int, "hashCodeCachedValue");
            }
        }
    }

    class MethodBuilder {
        private final Map<String, JDefinedClass> caseClasses;
        private final JFieldVar acceptorField;
        private final JFieldVar hashCodeCachedValueField;

        private MethodBuilder(Map<String, JDefinedClass> caseClasses, JFieldVar acceptorField, JFieldVar hashCodeCachedValueField) {
            this.caseClasses = caseClasses;
            this.acceptorField = acceptorField;
            this.hashCodeCachedValueField = hashCodeCachedValueField;
        }

        private MethodBuilder(Map<String, JDefinedClass> caseClasses, JFieldVar acceptorField) {
            this(caseClasses, acceptorField, null);
        }

        void buildPrivateConstructor() {
            JMethod constructor = valueClass.constructor(JMod.PRIVATE);
            JVar acceptorParam = constructor.param(acceptorField.type(), acceptorField.name());
            if (visitorInterface.hashCodeCaching() == Caching.PRECOMPUTE) {
                JInvocation invocation = acceptorParam.invoke(hashCodeAcceptorMethodName());
                constructor.body().assign(JExpr.refthis(hashCodeCachedValueField), invocation);
            }
            constructor.body().assign(JExpr.refthis(acceptorField.name()), acceptorParam);
        }

        void buildProtectedConstructor(Serialization serialization) throws JClassAlreadyExistsException {
            JMethod constructor = valueClass.constructor(JMod.PROTECTED);
            JAnnotationUse annotation = constructor.annotate(SuppressWarnings.class);
            annotation.paramArray("value", "null");
            AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
            JVar param = constructor.param(usedValueClassType, "implementation");
            Source.annotateNonnull(param);
            JConditional nullCheck = constructor.body()._if(JExpr.ref("implementation").eq(JExpr._null()));
            JInvocation nullPointerExceptionConstruction = JExpr._new(types._NullPointerException);
            nullPointerExceptionConstruction.arg(JExpr.lit("Argument shouldn't be null: 'implementation' argument in class constructor invocation: " + valueClass.fullName()));
            nullCheck._then()._throw(nullPointerExceptionConstruction);

            if (visitorInterface.hashCodeCaching().enabled())
                constructor.body().assign(JExpr.refthis(hashCodeCachedValueField), param.ref(hashCodeCachedValueField));
            constructor.body().assign(JExpr.refthis(acceptorField), param.ref(acceptorField));
        }

        void buildAcceptMethod() {
            JMethod acceptMethod = valueClass.method(Source.toJMod(visitorInterface.acceptMethodAccessLevel()) | JMod.FINAL, types._void, visitorInterface.acceptMethodName());

            JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
            JTypeVar resultType = Types.generifyWithBoundsFrom(acceptMethod, visitorResultType.name(), visitorResultType);
            acceptMethod.type(resultType);

            JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
            JTypeVar exceptionType = null;
            if (visitorExceptionType != null) {
                exceptionType = Types.generifyWithBoundsFrom(acceptMethod, visitorExceptionType.name(), visitorExceptionType);
                acceptMethod._throws(exceptionType);
            }

            AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
            AbstractJClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
            acceptMethod.param(usedVisitorType, "visitor");
            JInvocation invocation = acceptorField.invoke(visitorInterface.acceptMethodName());
            invocation.arg(JExpr.ref("visitor"));
            acceptMethod.body()._return(invocation);
        }

        Map<String, JMethod> buildConstructorMethods(Serialization serialization) throws JClassAlreadyExistsException, SourceCodeValidationException {
            Map<String, JMethod> constructorMethods = new TreeMap<String, JMethod>();
            for (JMethod interfaceMethod: visitorInterface.methods()) {
                JMethod constructorMethod = valueClass.method(Source.toJMod(visitorInterface.factoryMethodAccessLevel()) | JMod.STATIC, types._void, interfaceMethod.name());
                Source.annotateNonnull(constructorMethod);
                for (JTypeVar visitorTypeParameter: visitorInterface.getValueTypeParameters()) {
                    Types.generifyWithBoundsFrom(constructorMethod, visitorTypeParameter.name(), visitorTypeParameter);
                }
                AbstractJClass usedValueClassType = valueClass.narrow(constructorMethod.typeParams());
                constructorMethod.type(usedValueClassType);
                for (JVar param: interfaceMethod.params()) {
                    AbstractJType paramType = Source.toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException));
                    JVar constructorMethodParam = constructorMethod.param(param.mods().getValue(), paramType, param.name());
                    if (param.type().isReference()) {
                        if (Source.isNullable(param))
                            Source.annotateNullable(constructorMethodParam);
                        else
                            Source.annotateNonnull(constructorMethodParam);
                    }
                }
                JVar param = interfaceMethod.varParam();
                if (param != null) {
                    AbstractJType paramType = Source.toDeclarable(visitorInterface.narrowType(param.type().elementType(), usedValueClassType, usedValueClassType, types._RuntimeException));
                    JVar constructorMethodParam = constructorMethod.varParam(param.mods().getValue(), paramType, param.name());
                    if (param.type().isReference())
                        if (Source.isNullable(param))
                            Source.annotateNullable(constructorMethodParam);
                        else
                            Source.annotateNonnull(constructorMethodParam);
                }

                AbstractJClass usedCaseClassType = caseClasses.get(interfaceMethod.name()).narrow(constructorMethod.typeParams());
                if (!interfaceMethod.params().isEmpty() || interfaceMethod.hasVarArgs()) {
                    boolean hasNullChecks = false;
                    for (JVar param1: interfaceMethod.params()) {
                        if (param1.type().isReference() && !Source.isNullable(param1)) {
                            JConditional nullCheck = constructorMethod.body()._if(JExpr.ref(param1.name()).eq(JExpr._null()));
                            JInvocation nullPointerExceptionConstruction = JExpr._new(types._NullPointerException);
                            nullPointerExceptionConstruction.arg(JExpr.lit(MessageFormat.format("Argument shouldn''t be null: ''{0}'' argument in static method invocation: ''{1}'' in class {2}",
                                                                                                param1.name(),
                                                                                                constructorMethod.name(),
                                                                                                valueClass.fullName())));
                            nullCheck._then()._throw(nullPointerExceptionConstruction);
                            hasNullChecks = true;
                        }
                    }
                    JVar param1 = interfaceMethod.varParam();
                    if (param1 != null) {
                        if (param1.type().isReference() && !Source.isNullable(param1)) {
                            JConditional nullCheck = constructorMethod.body()._if(JExpr.ref(param1.name()).eq(JExpr._null()));
                            JInvocation nullPointerExceptionConstruction = JExpr._new(types._NullPointerException);
                            nullPointerExceptionConstruction.arg(JExpr.lit(MessageFormat.format("Argument shouldn''t be null: ''{0}'' argument in static method invocation: ''{1}'' in class {2}",
                                                                                                param1.name(),
                                                                                                constructorMethod.name(),
                                                                                                valueClass.fullName())));
                            nullCheck._then()._throw(nullPointerExceptionConstruction);
                            hasNullChecks = true;
                        }
                    }
                    if (hasNullChecks) {
                        JAnnotationUse annotation = constructorMethod.annotate(SuppressWarnings.class);
                        annotation.paramArray("value", "null");
                    }

                    JInvocation caseClassConstructorInvocation = JExpr._new(usedCaseClassType);
                    for (JVar param2: interfaceMethod.params()) {
                        caseClassConstructorInvocation.arg(JExpr.ref(param2.name()));
                    }
                    JVar param2 = interfaceMethod.varParam();
                    if (param2 != null) {
                        caseClassConstructorInvocation.arg(JExpr.ref(param2.name()));
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
                                                                        interfaceMethod.name().toUpperCase(Locale.US),
                                                                        initializer);
                    JAnnotationUse fieldAnnotationUse = singletonInstanceField.annotate(SuppressWarnings.class);
                    JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
                    paramArray.param("unchecked");
                    paramArray.param("rawtypes");

                    JAnnotationUse methodAnnotationUse = constructorMethod.annotate(SuppressWarnings.class);
                    methodAnnotationUse.param("value", "unchecked");
                    constructorMethod.body()._return(singletonInstanceField);
                }
                constructorMethods.put(interfaceMethod.name(), constructorMethod);
            }
            return constructorMethods;
        }

        void buildHashCodeMethod(int hashCodeBase) throws SourceCodeValidationException {
            String hashCodeMethodName = hashCodeAcceptorMethodName();
            JMethod hashCodeMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._int, "hashCode");
            hashCodeMethod.annotate(Override.class);

            if (visitorInterface.hashCodeCaching() == Caching.NONE) {
                JInvocation invocation = JExpr.refthis(acceptorField).invoke(hashCodeMethodName);
                hashCodeMethod.body()._return(invocation);
            } else if (visitorInterface.hashCodeCaching() == Caching.PRECOMPUTE) {
                hashCodeMethod.body()._return(hashCodeCachedValueField);
            } else if (visitorInterface.hashCodeCaching() == Caching.SIMPLE) {
                VariableNameSource nameSource = new VariableNameSource();
                JFieldRef hashCodeField = JExpr.refthis(hashCodeCachedValueField);
                JVar code = hashCodeMethod.body().decl(types._int, nameSource.get("code"), hashCodeField);
                JConditional _if = hashCodeMethod.body()._if(code.eq0());
                JInvocation invocation = JExpr.refthis(acceptorField).invoke(hashCodeMethodName);
                _if._then().assign(code, invocation);
                _if._then().assign(code, JOp.cond(code.ne0(), code, JExpr.lit(Integer.MIN_VALUE)));
                _if._then().assign(hashCodeField, code);
                hashCodeMethod.body()._return(code);
            } else if (visitorInterface.hashCodeCaching() == Caching.SYNCRONIZED) {
                VariableNameSource nameSource = new VariableNameSource();
                JFieldRef hashCodeField = JExpr.refthis(hashCodeCachedValueField);
                JFieldRef lockField = JExpr.refthis(acceptorField);
                JVar code = hashCodeMethod.body().decl(types._int, nameSource.get("code"), hashCodeField);
                JConditional _if1 = hashCodeMethod.body()._if(code.eq0());
                JSynchronizedBlock synchronizedBlock = _if1._then().synchronizedBlock(lockField);
                synchronizedBlock.body().assign(code, hashCodeField);
                JConditional _if2 = synchronizedBlock.body()._if(code.eq0());
                JInvocation invocation = JExpr.refthis(acceptorField).invoke(hashCodeMethodName);
                _if2._then().assign(code, invocation);
                _if2._then().assign(code, JOp.cond(code.ne0(), code, JExpr.lit(Integer.MIN_VALUE)));
                _if2._then().assign(hashCodeField, code);
                hashCodeMethod.body()._return(code);
            } else
                throw new IllegalStateException("Unsupported hashCodeCaching: " + visitorInterface.hashCodeCaching());

            acceptingInterface.method(JMod.PUBLIC, types._int, hashCodeMethodName);

            int tag = 1;
            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                JMethod caseHashCodeMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._int, hashCodeMethodName);
                caseHashCodeMethod.annotate(Override.class);

                VariableNameSource nameSource = new VariableNameSource();
                List<JFieldVar> arguments = new ArrayList<JFieldVar>();
                JFieldVar varArgument = null;
                for (JVar param: interfaceMethod1.params()) {
                    arguments.add(caseClass.fields().get(param.name()));
                }
                JVar param = interfaceMethod1.varParam();
                if (param != null) {
                    varArgument = caseClass.fields().get(param.name());
                }

                HashCodeMethod methodModel = new HashCodeMethod(types, hashCodeBase, caseHashCodeMethod.body(), nameSource);
                HashCodeMethod.Body body = methodModel.createBody(tag);
                for (int i = 0; i < arguments.size(); i++) {
                    param = interfaceMethod1.params().get(i);
                    JFieldVar argument = arguments.get(i);
                    if (Source.isNullable(param))
                        body.appendNullableValue(argument.type(), JExpr.refthis(argument));
                    else
                        body.appendNotNullValue(argument.type(), JExpr.refthis(argument));
                }
                if (varArgument != null) {
                    if (Source.isNullable(param))
                        body.appendNullableValue(varArgument.type(), JExpr.refthis(varArgument));
                    else
                        body.appendNotNullValue(varArgument.type(), JExpr.refthis(varArgument));
                }
                caseHashCodeMethod.body()._return(body.result());
                tag++;
            }
        }

        void buildToStringMethod() throws SourceCodeValidationException {
            JMethod toStringMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._String, "toString");
            toStringMethod.annotate(Override.class);
            Source.annotateNonnull(toStringMethod);
            JInvocation invocation1 = JExpr.refthis(acceptorField).invoke("toString");
            toStringMethod.body()._return(invocation1);

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                JMethod caseToStringMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._String, "toString");
                caseToStringMethod.annotate(Override.class);
                Source.annotateNonnull(caseToStringMethod);

                VariableNameSource nameSource = new VariableNameSource();
                List<JFieldVar> arguments = new ArrayList<JFieldVar>();
                JFieldVar varArgument = null;
                for (JVar param: interfaceMethod1.params()) {
                    arguments.add(caseClass.fields().get(param.name()));
                }
                JVar param = interfaceMethod1.varParam();
                if (param != null) {
                    varArgument = caseClass.fields().get(param.name());
                }

                JVar result = caseToStringMethod.body().decl(types._StringBuilder, nameSource.get("result"), JExpr._new(types._StringBuilder));
                JInvocation invocation = caseToStringMethod.body().invoke(result, "append");
                invocation.arg(valueClass.name() + "." + Source.capitalize(interfaceMethod1.name()) + "{");
                ToStringMethodBody body = new ToStringMethodBody(types, caseToStringMethod.body(), result);
                if (!arguments.isEmpty()) {
                    JFieldVar argument = arguments.get(0);
                    body.appendParam(argument.type(), interfaceMethod1.params().get(0).name(), JExpr.refthis(argument));
                    for (int i = 1; i < arguments.size(); i++) {
                        invocation = caseToStringMethod.body().invoke(result, "append");
                        invocation.arg(", ");
                        argument = arguments.get(i);
                        body.appendParam(argument.type(), interfaceMethod1.params().get(i).name(), JExpr.refthis(argument));
                    }
                }
                if (varArgument != null) {
                    if (!arguments.isEmpty()) {
                        invocation = caseToStringMethod.body().invoke(result, "append");
                        invocation.arg(", ");
                    }
                    body.appendParam(varArgument.type(), interfaceMethod1.varParam().name(), JExpr.refthis(varArgument));
                }
                invocation = caseToStringMethod.body().invoke(result, "append");
                invocation.arg("}");
                caseToStringMethod.body()._return(result.invoke("toString"));
            }
        }

        void generateGetter(FieldConfiguration configuration) {
            String getterName = configuration.name();
            JMethod getterMethod = acceptingInterface.method(JMod.PUBLIC, configuration.type(), getterName);
            if (configuration.type().isReference()) {
                if (configuration.isNullable())
                    Source.annotateNullable(getterMethod);
                else
                    Source.annotateNonnull(getterMethod);
            }

            getterMethod = valueClass.method(Source.toJMod(configuration.accessLevel()) | JMod.FINAL, configuration.type(), getterName);
            if (configuration.type().isReference()) {
                if (configuration.isNullable())
                    Source.annotateNullable(getterMethod);
                else
                    Source.annotateNonnull(getterMethod);
            }
            JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(getterName);
            getterMethod.body()._return(invocation1);

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                getterMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, configuration.type(), getterName);
                getterMethod.annotate(Override.class);
                if (configuration.type().isReference()) {
                    if (configuration.isNullable())
                        Source.annotateNullable(getterMethod);
                    else
                        Source.annotateNonnull(getterMethod);
                }
                boolean isGettable = false;
                for (JVar param: interfaceMethod1.params()) {
                    JFieldVar field = caseClass.fields().get(param.name());
                    if (configuration.isFieldValue(interfaceMethod1, param.name())) {
                        getterMethod.body()._return(field);
                        isGettable = true;
                    }
                }
                JVar param = interfaceMethod1.varParam();
                if (param != null) {
                    JFieldVar field = caseClass.fields().get(param.name());
                    if (configuration.isFieldValue(interfaceMethod1, param.name())) {
                        getterMethod.body()._return(field);
                        isGettable = true;
                    }
                }
                if (!isGettable) {
                    JInvocation exceptionInvocation = JExpr._new(types._IllegalStateException);
                    exceptionInvocation.arg(configuration.name() + " is not accessible in this case: " + interfaceMethod1.name());
                    getterMethod.body()._throw(exceptionInvocation);
                }
            }
        }

        void generateUpdater(FieldConfiguration configuration) throws SourceCodeValidationException {
            VariableNameSource nameSource = new VariableNameSource();
            String updaterName = configuration.name();
            AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());

            JMethod updaterMethod = acceptingInterface.method(JMod.PUBLIC, usedValueClassType, updaterName);
            Source.annotateNonnull(updaterMethod);
            JVar newValueParam;
            if (configuration.isVarArg())
                newValueParam = updaterMethod.varParam(configuration.type().elementType(), nameSource.get("newValue"));
            else
                newValueParam = updaterMethod.param(configuration.type(), nameSource.get("newValue"));
            if (configuration.type().isReference()) {
                if (configuration.isNullable()) {
                    Source.annotateNullable(newValueParam);
                } else {
                    Source.annotateNonnull(newValueParam);
                }
            }

            nameSource = new VariableNameSource();
            updaterMethod = valueClass.method(Source.toJMod(configuration.accessLevel()) | JMod.FINAL, usedValueClassType, updaterName);
            Source.annotateNonnull(updaterMethod);
            JVar newValue;
            if (configuration.isVarArg())
                newValue = updaterMethod.varParam(configuration.type().elementType(), nameSource.get("newValue"));
            else
                newValue = updaterMethod.param(configuration.type(), nameSource.get("newValue"));
            if (configuration.type().isReference()) {
                if (configuration.isNullable()) {
                    Source.annotateNullable(newValue);
                } else {
                    Source.annotateNonnull(newValue);
                }
            }
            JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(updaterName);
            invocation1.arg(newValue);
            updaterMethod.body()._return(invocation1);

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                nameSource = new VariableNameSource();
                updaterMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, usedValueClassType, updaterName);
                Source.annotateNonnull(updaterMethod);
                updaterMethod.annotate(Override.class);
                if (configuration.isVarArg())
                    newValue = updaterMethod.varParam(configuration.type().elementType(), nameSource.get("newValue"));
                else
                    newValue = updaterMethod.param(configuration.type(), nameSource.get("newValue"));
                if (configuration.type().isReference()) {
                    if (configuration.isNullable()) {
                        Source.annotateNullable(newValue);
                    } else {
                        Source.annotateNonnull(newValue);
                    }
                }
                JInvocation invocation = valueClass.staticInvoke(interfaceMethod1.name());
                for (JTypeVar typeArgument: valueClass.typeParams())
                    invocation.narrow(typeArgument);
                for (JVar param: interfaceMethod1.params()) {
                    JFieldVar argument = caseClass.fields().get(param.name());
                    if (configuration.isFieldValue(interfaceMethod1, param.name())) {
                        invocation.arg(newValue);
                    } else {
                        invocation.arg(JExpr.refthis(argument));
                    }
                }
                JVar param = interfaceMethod1.varParam();
                if (param != null) {
                    JFieldVar argument = caseClass.fields().get(param.name());
                    if (configuration.isFieldValue(interfaceMethod1, param.name())) {
                        invocation.arg(newValue);
                    } else {
                        invocation.arg(JExpr.refthis(argument));
                    }
                }
                updaterMethod.body()._return(invocation);
            }
        }

        void generatePredicate(String name, PredicateConfigutation configuration) {
            acceptingInterface.method(JMod.PUBLIC, types._boolean, name);

            JMethod predicateMethod = valueClass.method(Source.toJMod(configuration.accessLevel()) | JMod.FINAL, types._boolean, name);
            predicateMethod.body()._return(JExpr.refthis(acceptorField).invoke(name));

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                predicateMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._boolean, name);
                predicateMethod.annotate(Override.class);

                boolean result = configuration.isTrueFor(interfaceMethod1);
                predicateMethod.body()._return(JExpr.lit(result));
            }

        }

        void buildEqualsMethod() throws SourceCodeValidationException, JClassAlreadyExistsException {
            AbstractJClass[] typeParams = new AbstractJClass[valueClass.typeParams().length];
            for (int i = 0; i < typeParams.length; i++)
                typeParams[i] = valueClass.owner().wildcard();
            AbstractJClass usedValueClassType = valueClass.narrow(typeParams);
            AbstractJClass usedAcceptorType = acceptingInterface.narrow(typeParams);
            String equalsImplementationMethodName = Source.decapitalize(valueClass.name()) + "Equals";
            JMethod equalsImplementationMethod = acceptingInterface.method(JMod.PUBLIC, types._boolean, equalsImplementationMethodName);
            VariableNameSource nameSource = new VariableNameSource();
            equalsImplementationMethod.param(usedAcceptorType, nameSource.get("thatAcceptor"));

            JMethod equalsMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._boolean, "equals");
            nameSource = new VariableNameSource();
            equalsMethod.annotate(Override.class);
            JVar thatObject = equalsMethod.param(types._Object, nameSource.get("thatObject"));
            JConditional _if = equalsMethod.body()._if(JExpr._this().eq(thatObject));
            _if._then()._return(JExpr.TRUE);
            JConditional elseif = _if._elseif(thatObject._instanceof(valueClass).not());
            elseif._then()._return(JExpr.FALSE);
            JBlock _else = elseif._else();
            JVar that = _else.decl(usedValueClassType, nameSource.get("that"), JExpr.cast(usedValueClassType, thatObject));
            JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(equalsImplementationMethod);
            invocation1.arg(that.ref(acceptorField));
            IJExpression hashCodeResult = invocation1;
            if (visitorInterface.hashCodeCaching() == Caching.PRECOMPUTE) {
                hashCodeResult = JExpr.refthis(hashCodeCachedValueField).eq(that.ref(hashCodeCachedValueField)).cand(invocation1);
            }
            _else._return(hashCodeResult);

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                equalsImplementationMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._boolean, equalsImplementationMethod.name());
                equalsImplementationMethod.annotate(Override.class);
                nameSource = new VariableNameSource();
                JVar thatAcceptor = equalsImplementationMethod.param(usedAcceptorType, nameSource.get("thatAcceptor"));

                String equalsCaseMethodName = equalsImplementationMethod.name() + Source.capitalize(interfaceMethod1.name());
                JMethod equalsCaseMethod = acceptingInterface.method(JMod.PUBLIC, types._boolean, equalsCaseMethodName);
                nameSource = new VariableNameSource();

                JInvocation equalsCaseInvocation = thatAcceptor.invoke(equalsCaseMethod);
                for (JVar param1: interfaceMethod1.params()) {
                    AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(param1.type(), usedValueClassType, types._Boolean, types._RuntimeException));
                    equalsCaseMethod.param(param1.mods().getValue(), argumentType, nameSource.get(param1.name()));
                    equalsCaseInvocation.arg(JExpr.refthis(caseClass.fields().get(param1.name())));
                }
                JVar varParam1 = interfaceMethod1.varParam();
                if (varParam1 != null) {
                    AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(varParam1.type().elementType(), usedValueClassType, types._Boolean, types._RuntimeException));
                    equalsCaseMethod.varParam(varParam1.mods().getValue(), argumentType, nameSource.get(varParam1.name()));
                    equalsCaseInvocation.arg(JExpr.refthis(caseClass.fields().get(varParam1.name())));
                }
                equalsImplementationMethod.body()._return(equalsCaseInvocation);

                for (JMethod interfaceMethod2: visitorInterface.methods()) {
                    caseClass = caseClasses.get(interfaceMethod2.name());
                    equalsCaseMethod = caseClass.method(JMod.PUBLIC, types._boolean, equalsCaseMethodName);
                    equalsCaseMethod.annotate(Override.class);
                    nameSource = new VariableNameSource();

                    boolean isSameCase = interfaceMethod1.name().equals(interfaceMethod2.name());
                    EqualsMethod body = new EqualsMethod(types, equalsCaseMethod.body(), nameSource);

                    int i = 0;
                    boolean generatedReturn = false;
                    JVar varParam = interfaceMethod1.varParam();
                    for (JVar param: interfaceMethod1.params()) {
                        AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, types._Boolean, types._RuntimeException));
                        JVar argument1 = equalsCaseMethod.param(param.mods().getValue(), argumentType, nameSource.get(param.name()));
                        if (isSameCase) {
                            JFieldVar argument2 = caseClass.fields().get(param.name());
                            boolean isLast = varParam == null && i == interfaceMethod1.params().size() - 1;
                            if (!isLast) {
                                if (Source.isNullable(param))
                                    body.appendNullableValue(argumentType, argument1, JExpr.refthis(argument2));
                                else
                                    body.appendNotNullValue(argumentType, argument1, JExpr.refthis(argument2));
                            } else {
                                if (Source.isNullable(param))
                                    body.appendNullableValueAndReturn(argumentType, argument1, JExpr.refthis(argument2));
                                else
                                    body.appendNotNullValueAndReturn(argumentType, argument1, JExpr.refthis(argument2));
                                generatedReturn = true;
                            }
                        }
                        i++;
                    }
                    if (varParam != null) {
                        AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(varParam.type().elementType(), usedValueClassType, types._Boolean, types._RuntimeException));
                        JVar varArgument1 = equalsCaseMethod.varParam(varParam.mods().getValue(), argumentType, nameSource.get(varParam.name()));
                        if (isSameCase) {
                            JFieldVar varArgument2 = caseClass.fields().get(varParam.name());
                            if (Source.isNullable(varParam))
                                body.appendNullableValueAndReturn(varArgument1.type(), varArgument1, JExpr.refthis(varArgument2));
                            else
                                body.appendNotNullValueAndReturn(varArgument1.type(), varArgument1, JExpr.refthis(varArgument2));
                            generatedReturn = true;
                        }
                    }
                    if (!generatedReturn)
                        equalsCaseMethod.body()._return(isSameCase ? JExpr.TRUE : JExpr.FALSE);
                }
            }
        }

        void buildCompareTo() throws SourceCodeValidationException, JClassAlreadyExistsException {
            AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
            AbstractJClass usedAcceptorType = acceptingInterface.narrow(valueClass.typeParams());
            String compareToMethodImplementationString = Source.decapitalize(valueClass.name()) + "ComapareTo";
            JMethod compareToMethodImplementation = acceptingInterface.method(JMod.PUBLIC, types._int, compareToMethodImplementationString);
            VariableNameSource nameSource = new VariableNameSource();
            compareToMethodImplementation.param(usedAcceptorType, nameSource.get("thatAcceptor"));

            JMethod compareToMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._int, "compareTo");
            compareToMethod.annotate(Override.class);
            nameSource = new VariableNameSource();
            JVar that = compareToMethod.param(usedValueClassType, nameSource.get("that"));
            JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(compareToMethodImplementation);
            invocation1.arg(that.ref(acceptorField));
            compareToMethod.body()._return(invocation1);

            JMethod[] methods = new JMethod[visitorInterface.methods().size()];
            methods = visitorInterface.methods().toArray(methods);
            for (int interfaceMethod1Index = 0; interfaceMethod1Index < methods.length; interfaceMethod1Index++) {
                JMethod interfaceMethod1 = methods[interfaceMethod1Index];
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                compareToMethodImplementation = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._int, compareToMethodImplementation.name());
                compareToMethodImplementation.annotate(Override.class);
                nameSource = new VariableNameSource();
                JVar thatAcceptor = compareToMethodImplementation.param(usedAcceptorType, nameSource.get("thatAcceptor"));

                String compareToCaseMethodName = compareToMethodImplementation.name() + Source.capitalize(interfaceMethod1.name());
                JMethod compareToCaseMethod = acceptingInterface.method(JMod.PUBLIC, types._int, compareToCaseMethodName);
                nameSource = new VariableNameSource();

                JInvocation compareToCaseInvocation = thatAcceptor.invoke(compareToCaseMethod);
                for (JVar param1: interfaceMethod1.params()) {
                    AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(param1.type(), usedValueClassType, types._Integer, types._RuntimeException));
                    compareToCaseMethod.param(param1.mods().getValue(), argumentType, nameSource.get(param1.name()));
                    compareToCaseInvocation.arg(JExpr.refthis(caseClass.fields().get(param1.name())));
                }
                JVar varParam1 = interfaceMethod1.varParam();
                if (varParam1 != null) {
                    AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(varParam1.type().elementType(), usedValueClassType, types._Integer, types._RuntimeException));
                    compareToCaseMethod.varParam(varParam1.mods().getValue(), argumentType, nameSource.get(varParam1.name()));
                    compareToCaseInvocation.arg(JExpr.refthis(caseClass.fields().get(varParam1.name())));
                }
                compareToMethodImplementation.body()._return(compareToCaseInvocation);

                for (int interfaceMethod2Index = 0; interfaceMethod2Index < methods.length; interfaceMethod2Index++) {
                    JMethod interfaceMethod2 = methods[interfaceMethod2Index];
                    caseClass = caseClasses.get(interfaceMethod2.name());
                    compareToCaseMethod = caseClass.method(JMod.PUBLIC, types._int, compareToCaseMethod.name());
                    compareToCaseMethod.annotate(Override.class);
                    nameSource = new VariableNameSource();

                    boolean isSameCase = interfaceMethod1Index == interfaceMethod2Index;
                    CompareToMethod compareToMethodModel = new CompareToMethod(types, compareToCaseMethod.body(), nameSource);
                    CompareToMethod.Body body = null;

                    JVar varParam = interfaceMethod1.varParam();
                    for (JVar param: interfaceMethod1.params()) {
                        AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, types._Integer, types._RuntimeException));
                        JVar argument1 = compareToCaseMethod.param(param.mods().getValue(), argumentType, nameSource.get(param.name()));
                        if (isSameCase) {
                            if (body == null)
                                body = compareToMethodModel.createBody();
                            JFieldVar argument2 = caseClass.fields().get(param.name());
                            if (Source.isNullable(param))
                                body.appendNullableValue(argumentType, argument1, JExpr.refthis(argument2));
                            else
                                body.appendNotNullValue(argumentType, argument1, JExpr.refthis(argument2));
                        }
                    }
                    if (varParam != null) {
                        AbstractJType argumentType = Source.toDeclarable(visitorInterface.narrowType(varParam.type().elementType(), usedValueClassType, types._Boolean, types._RuntimeException));
                        JVar varArgument1 = compareToCaseMethod.varParam(varParam.mods().getValue(), argumentType, nameSource.get(varParam.name()));
                        if (isSameCase) {
                            if (body == null)
                                body = compareToMethodModel.createBody();
                            JFieldVar varArgument2 = caseClass.fields().get(varParam.name());
                            if (Source.isNullable(varParam))
                                body.appendNullableValue(varArgument1.type(), varArgument1, JExpr.refthis(varArgument2));
                            else
                                body.appendNotNullValue(varArgument1.type(), varArgument1, JExpr.refthis(varArgument2));
                        }
                    }
                    int result = interfaceMethod1Index < interfaceMethod2Index ? -1 : (interfaceMethod1Index > interfaceMethod2Index ? 1 : 0);
                    compareToCaseMethod.body()._return(JExpr.lit(result));
                }
            }
        }

        void buildReadObjectMethod() {
            if (visitorInterface.hashCodeCaching() == Caching.PRECOMPUTE) {
                JMethod method = valueClass.method(JMod.PRIVATE, types._void, "readObject");
                method._throws(types._IOException);
                method._throws(types._ClassNotFoundException);
                VariableNameSource variableNameSource = new VariableNameSource();
                JVar inputStream = method.param(types._ObjectInputStream, variableNameSource.get("input"));
                JBlock body = method.body();
                body.invoke(inputStream, "defaultReadObject");
                JInvocation invocation = JExpr.refthis(acceptorField).invoke(hashCodeAcceptorMethodName());
                body.assign(JExpr.refthis(hashCodeCachedValueField), invocation);
            }
        }

    }
}
