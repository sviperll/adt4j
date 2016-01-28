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
import com.github.sviperll.adt4j.model.config.FieldConfiguration;
import com.github.sviperll.adt4j.model.config.PredicateConfigutation;
import com.github.sviperll.adt4j.model.config.Serialization;
import com.github.sviperll.adt4j.model.config.ValueClassConfiguration;
import com.github.sviperll.adt4j.model.config.VisitorModel;
import com.github.sviperll.adt4j.model.util.Source;
import com.github.sviperll.adt4j.model.util.Types;
import com.github.sviperll.adt4j.model.util.VariableNameSource;
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

public class FinalValueClassModel {
    static FinalValueClassModel createErrorModel(FinalValueClassModelEnvironment environment, Types types) {
        return new FinalValueClassModel(environment, types, true);
    }

    static FinalValueClassModel createModel(FinalValueClassModelEnvironment environment, Types types) {
        return new FinalValueClassModel(environment, types, false);
    }

    private final FinalValueClassModelEnvironment environment;
    private final Types types;
    private final boolean isError;

    private FinalValueClassModel(FinalValueClassModelEnvironment environment, Types modelTypes, boolean isError) {
        this.types = modelTypes;
        this.isError = isError;
        this.environment = environment;
    }

    private String hashCodeAcceptorMethodName() {
        return Source.decapitalize(environment.valueClassName()) + "HashCode";
    }

    MethodBuilder createMethodBuilder(Serialization serialization) {
        if (isError)
            return new MethodBuilder(null, null);
        else {
            JFieldVar acceptorField = buildAcceptorField();
            Map<String, JDefinedClass> caseClasses;
            try {
                caseClasses = buildCaseClasses(serialization);
            } catch (JClassAlreadyExistsException ex) {
                throw new RuntimeException("Unexpected exception :)", ex);
            }
            Caching hashCode = environment.hashCodeCaching();
            if (!hashCode.enabled())
                return new MethodBuilder(caseClasses, acceptorField);
            else {
                JFieldVar hashCodeField = buildHashCodeCachedValueField(serialization);
                return new MethodBuilder(caseClasses, acceptorField, hashCodeField);
            }
        }
    }

    void buildSerialVersionUID() {
        if (environment.isValueClassSerializable())
            environment.buildValueClassField(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, types._long, "serialVersionUID", JExpr.lit(environment.serialVersionUIDForGeneratedCode()));
    }

    private JFieldVar buildAcceptorField() {
        return environment.buildValueClassField(JMod.PRIVATE | JMod.FINAL, environment.acceptingInterfaceTypeInsideValueClass(), "acceptor");
    }

    JMethod buildFactory(Map<String, JMethod> constructorMethods) throws JClassAlreadyExistsException {
        JDefinedClass factory = buildFactoryClass(constructorMethods);

        JFieldVar factoryField = environment.buildValueClassField(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, factory, "FACTORY");
        JAnnotationUse fieldAnnotationUse = factoryField.annotate(SuppressWarnings.class);
        JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
        paramArray.param("unchecked");
        paramArray.param("rawtypes");

        factoryField.init(JExpr._new(factory));
        JMethod factoryMethod = environment.buildValueClassMethod(Source.toJMod(environment.factoryMethodAccessLevel()) | JMod.STATIC, "factory");
        Source.annotateNonnull(factoryMethod);
        JAnnotationUse methodAnnotationUse = factoryMethod.annotate(SuppressWarnings.class);
        methodAnnotationUse.param("value", "unchecked");
        for (JTypeVar visitorTypeParameter: environment.getValueTypeParameters()) {
            JTypeVar typeParameter = factoryMethod.generify(visitorTypeParameter.name());
            typeParameter.boundLike(visitorTypeParameter);
        }
        AbstractJClass usedValueClassType = environment.wrappedValueClassType(factoryMethod.typeParams());
        factoryMethod.type(environment.visitor(usedValueClassType, usedValueClassType, types._RuntimeException).getVisitorType());
        factoryMethod.body()._return(factoryField);
        return factoryMethod;
    }

    private JDefinedClass buildFactoryClass(Map<String, JMethod> constructorMethods) throws JClassAlreadyExistsException {
        JDefinedClass factoryClass = environment.buildValueClassInnerClass(JMod.PRIVATE | JMod.STATIC, environment.valueClassName() + "Factory", EClassType.CLASS);
        for (JTypeVar visitorTypeParameter: environment.getValueTypeParameters()) {
            JTypeVar typeParameter = factoryClass.generify(visitorTypeParameter.name());
            typeParameter.boundLike(visitorTypeParameter);
        }
        AbstractJClass usedValueClassType = environment.wrappedValueClassType(factoryClass.typeParams());
        VisitorModel.NarrowedVisitor usedVisitor = environment.visitor(usedValueClassType, usedValueClassType, types._RuntimeException);
        factoryClass._implements(usedVisitor.getVisitorType());
        for (JMethod interfaceMethod: environment.visitorMethodDeclarations()) {
            JMethod factoryMethod = factoryClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT, usedValueClassType, interfaceMethod.name());
            Source.annotateNonnull(factoryMethod);
            factoryMethod.annotate(Override.class);

            JMethod constructorMethod = constructorMethods.get(interfaceMethod.name());
            JInvocation staticInvoke = environment.invokeValueClassStaticMethod(constructorMethod, factoryClass.typeParams());
            for (JVar param: interfaceMethod.params()) {
                AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(param.type()));
                JVar argument = factoryMethod.param(param.mods().getValue(), argumentType, param.name());
                staticInvoke.arg(argument);
            }
            JVar param = interfaceMethod.varParam();
            if (param != null) {
                AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(param.type().elementType()));
                JVar argument = factoryMethod.varParam(param.mods().getValue(), argumentType, param.name());
                staticInvoke.arg(argument);
            }
            factoryMethod.body()._return(staticInvoke);
        }
        return factoryClass;
    }

    private Map<String, JDefinedClass> buildCaseClasses(Serialization serialization) throws JClassAlreadyExistsException {
        Map<String, JDefinedClass> caseClasses = new TreeMap<>();
        for (JMethod interfaceMethod: environment.visitorMethodDeclarations()) {
            JDefinedClass caseClass = buildCaseClass(interfaceMethod, serialization);
            caseClasses.put(interfaceMethod.name(), caseClass);
        }
        return caseClasses;
    }

    private JDefinedClass buildCaseClass(JMethod interfaceMethod, Serialization serialization) throws JClassAlreadyExistsException {
        JDefinedClass caseClass = environment.buildValueClassInnerClass(JMod.PRIVATE | JMod.STATIC, Source.capitalize(interfaceMethod.name()) + "Case" + environment.acceptingInterfaceName(), EClassType.CLASS);
        for (JTypeVar visitorTypeParameter: environment.getValueTypeParameters()) {
            JTypeVar typeParameter = caseClass.generify(visitorTypeParameter.name());
            typeParameter.boundLike(visitorTypeParameter);
        }

        AbstractJClass usedAcceptingInterfaceType = environment.acceptingInterfaceType(caseClass.typeParams());
        AbstractJClass usedValueClassType = environment.wrappedValueClassType(caseClass.typeParams());
        caseClass._implements(usedAcceptingInterfaceType);

        if (serialization.isSerializable()) {
            caseClass._implements(types._Serializable);
            caseClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, types._long, "serialVersionUID", JExpr.lit(serialization.serialVersionUIDForGeneratedCode()));
        }

        JMethod constructor = caseClass.constructor(JMod.NONE);
        VisitorModel.NarrowedVisitor usedVisitor = environment.visitor(usedValueClassType, usedValueClassType, types._RuntimeException);
        for (JVar param: interfaceMethod.params()) {
            AbstractJType paramType = Source.toDeclarable(usedVisitor.getNarrowedType(param.type()));
            JFieldVar field = caseClass.field(JMod.PRIVATE | JMod.FINAL, paramType, param.name());
            JVar argument = constructor.param(paramType, param.name());
            constructor.body().assign(JExpr._this().ref(field), argument);
        }
        JVar param = interfaceMethod.varParam();
        if (param != null) {
            AbstractJType paramType = Source.toDeclarable(usedVisitor.getNarrowedType(param.type().elementType()));
            JFieldVar field = caseClass.field(JMod.PRIVATE | JMod.FINAL, paramType.array(), param.name());
            JVar argument = constructor.varParam(paramType, param.name());
            constructor.body().assign(JExpr._this().ref(field), argument);
        }

        JMethod acceptMethod = declareAcceptMethod(caseClass, usedValueClassType);
        JInvocation invocation = JExpr.invoke(acceptMethod.params().get(0), interfaceMethod.name());
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

    private JMethod declareAcceptMethod(JDefinedClass caseClass, AbstractJClass usedValueClassType) {
        JMethod acceptMethod = caseClass.method(JMod.PUBLIC, types._void, environment.acceptMethodName());
        acceptMethod.annotate(Override.class);
        JTypeVar visitorResultType = environment.getVisitorResultTypeParameter();
        AbstractJClass resultType;
        if (visitorResultType == null)
            resultType = types._Object;
        else {
            JTypeVar resultTypeVar = acceptMethod.generify(visitorResultType.name());
            resultTypeVar.boundLike(visitorResultType);
            resultType = resultTypeVar;
        }
        acceptMethod.type(resultType);
        JTypeVar visitorExceptionType = environment.getVisitorExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            JTypeVar exceptionTypeParameter = acceptMethod.generify(visitorExceptionType.name());
            exceptionTypeParameter.boundLike(visitorExceptionType);
            exceptionType = exceptionTypeParameter;
            acceptMethod._throws(exceptionType);
        }
        VisitorModel.NarrowedVisitor usedVisitorType = environment.visitor(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType.getVisitorType(), "visitor");
        return acceptMethod;
    }

    private JFieldVar buildHashCodeCachedValueField(Serialization serialization) {
        if (!environment.hashCodeCaching().enabled())
            throw new IllegalStateException("Unsupported method evaluation to cache hash code: " + environment.hashCodeCaching());
        else {
            boolean isSerializable = serialization.isSerializable();
            boolean precomputes = environment.hashCodeCaching() == Caching.PRECOMPUTE;
            int mods = JMod.PRIVATE;
            mods = !isSerializable ? mods : mods | JMod.TRANSIENT;
            if (!precomputes)
                return environment.buildValueClassField(mods, types._int, "hashCodeCachedValue", JExpr.lit(0));
            else {
                mods = isSerializable ? mods : mods | JMod.FINAL;
                return environment.buildValueClassField(mods, types._int, "hashCodeCachedValue");
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
            if (!isError) {
                JMethod constructor = environment.buildValueClassConstructor(JMod.PRIVATE);
                JVar acceptorParam = constructor.param(acceptorField.type(), acceptorField.name());
                if (environment.hashCodeCaching() == Caching.PRECOMPUTE) {
                    JInvocation invocation = acceptorParam.invoke(hashCodeAcceptorMethodName());
                    constructor.body().assign(JExpr.refthis(hashCodeCachedValueField), invocation);
                }
                constructor.body().assign(JExpr.refthis(acceptorField.name()), acceptorParam);
            }
        }

        void buildProtectedConstructor(Serialization serialization) {
            JMethod constructor = environment.buildValueClassConstructor(JMod.PROTECTED);
            JAnnotationUse annotation = constructor.annotate(SuppressWarnings.class);
            annotation.paramArray("value", "null");
            AbstractJClass unwrappedUsedValueClassType = environment.unwrappedValueClassTypeInsideValueClass();
            JVar param = constructor.param(unwrappedUsedValueClassType, "implementation");
            Source.annotateNonnull(param);
            if (isError) {
                constructor.body()._throw(JExpr._new(types._UnsupportedOperationException));
            } else {
                JConditional nullCheck = constructor.body()._if(JExpr.ref("implementation").eq(JExpr._null()));
                JInvocation nullPointerExceptionConstruction = JExpr._new(types._NullPointerException);
                nullPointerExceptionConstruction.arg(JExpr.lit("Argument shouldn't be null: 'implementation' argument in class constructor invocation: " + environment.valueClassQualifiedName()));
                nullCheck._then()._throw(nullPointerExceptionConstruction);

                if (environment.hashCodeCaching().enabled())
                    constructor.body().assign(JExpr.refthis(hashCodeCachedValueField), param.ref(hashCodeCachedValueField));
                constructor.body().assign(JExpr.refthis(acceptorField), param.ref(acceptorField));
            }
        }

        void buildAcceptMethod() {
            JMethod acceptMethod = environment.buildValueClassMethod(Source.toJMod(environment.acceptMethodAccessLevel()) | JMod.FINAL, environment.acceptMethodName());

            JTypeVar visitorResultType = environment.getVisitorResultTypeParameter();
            AbstractJClass resultType;
            if (visitorResultType == null)
                resultType = types._Object;
            else {
                JTypeVar resultTypeVar = acceptMethod.generify(visitorResultType.name());
                resultTypeVar.boundLike(visitorResultType);
                resultType = resultTypeVar;
            }
            acceptMethod.type(resultType);

            JTypeVar visitorExceptionType = environment.getVisitorExceptionTypeParameter();
            JTypeVar exceptionType = null;
            if (visitorExceptionType != null) {
                JTypeVar exceptionTypeVar = acceptMethod.generify(visitorExceptionType.name());
                exceptionTypeVar.boundLike(visitorExceptionType);
                exceptionType = exceptionTypeVar;
                acceptMethod._throws(exceptionType);
            }

            AbstractJClass usedValueClassType = environment.wrappedValueClassTypeInsideValueClass();
            VisitorModel.NarrowedVisitor usedVisitorType = environment.visitor(usedValueClassType, resultType, exceptionType);
            acceptMethod.param(usedVisitorType.getVisitorType(), "visitor");
            if (isError) {
                acceptMethod.body()._throw(JExpr._new(types._UnsupportedOperationException));
            } else {
                JInvocation invocation = acceptorField.invoke(environment.acceptMethodName());
                invocation.arg(JExpr.ref("visitor"));
                acceptMethod.body()._return(invocation);
            }
        }

        Map<String, JMethod> buildConstructorMethods(Serialization serialization) {
            Map<String, JMethod> constructorMethods = new TreeMap<>();
            for (JMethod interfaceMethod: environment.visitorMethodDeclarations()) {
                JMethod constructorMethod = environment.buildValueClassMethod(Source.toJMod(environment.factoryMethodAccessLevel()) | JMod.STATIC, interfaceMethod.name());
                Source.annotateNonnull(constructorMethod);
                for (JTypeVar visitorTypeParameter: environment.getValueTypeParameters()) {
                    JTypeVar typeParameter = constructorMethod.generify(visitorTypeParameter.name());
                    typeParameter.boundLike(visitorTypeParameter);
                }
                AbstractJClass unwrappedUsedValueClassType = environment.unwrappedValueClassType(constructorMethod.typeParams());
                AbstractJClass usedValueClassType = environment.wrappedValueClassType(constructorMethod.typeParams());
                constructorMethod.type(usedValueClassType);
                VisitorModel.NarrowedVisitor usedVisitor = environment.visitor(usedValueClassType, usedValueClassType, types._RuntimeException);
                for (JVar param: interfaceMethod.params()) {
                    AbstractJType paramType = Source.toDeclarable(usedVisitor.getNarrowedType(param.type()));
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
                    AbstractJType paramType = Source.toDeclarable(usedVisitor.getNarrowedType(param.type().elementType()));
                    JVar constructorMethodParam = constructorMethod.varParam(param.mods().getValue(), paramType, param.name());
                    if (param.type().isReference())
                        if (Source.isNullable(param))
                            Source.annotateNullable(constructorMethodParam);
                        else
                            Source.annotateNonnull(constructorMethodParam);
                }

                if (isError) {
                    constructorMethod.body()._throw(JExpr._new(types._UnsupportedOperationException));
                } else {
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
                                                                                                    environment.valueClassQualifiedName())));
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
                                                                                                    environment.valueClassQualifiedName())));
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
                        JInvocation constructorInvocation = JExpr._new(unwrappedUsedValueClassType);
                        constructorInvocation.arg(caseClassConstructorInvocation);
                        constructorMethod.body()._return(environment.wrappedValue(usedValueClassType, constructorInvocation));
                    } else {
                        JInvocation caseClassConstructorInvocation = JExpr._new(usedCaseClassType.erasure());
                        JInvocation initializer = JExpr._new(unwrappedUsedValueClassType.erasure());
                        initializer.arg(caseClassConstructorInvocation);
                        JFieldVar singletonInstanceField = environment.buildValueClassField(JMod.PRIVATE | JMod.STATIC | JMod.FINAL,
                                                                            usedValueClassType.erasure(),
                                                                            interfaceMethod.name().toUpperCase(Locale.US),
                                                                            environment.wrappedValue(usedValueClassType.erasure(), initializer));
                        JAnnotationUse fieldAnnotationUse = singletonInstanceField.annotate(SuppressWarnings.class);
                        JAnnotationArrayMember paramArray = fieldAnnotationUse.paramArray("value");
                        paramArray.param("unchecked");
                        paramArray.param("rawtypes");

                        JAnnotationUse methodAnnotationUse = constructorMethod.annotate(SuppressWarnings.class);
                        methodAnnotationUse.param("value", "unchecked");
                        constructorMethod.body()._return(singletonInstanceField);
                    }
                }
                constructorMethods.put(interfaceMethod.name(), constructorMethod);
            }
            return constructorMethods;
        }

        void buildHashCodeMethod(int hashCodeBase) {
            if (!isError) {
                String hashCodeMethodName = hashCodeAcceptorMethodName();
                JMethod hashCodeMethod = environment.buildValueClassMethod(JMod.PUBLIC | JMod.FINAL, "hashCode");
                hashCodeMethod.type(types._int);
                hashCodeMethod.annotate(Override.class);

                if (environment.hashCodeCaching() == Caching.NONE) {
                    JInvocation invocation = JExpr.refthis(acceptorField).invoke(hashCodeMethodName);
                    hashCodeMethod.body()._return(invocation);
                } else if (environment.hashCodeCaching() == Caching.PRECOMPUTE) {
                    hashCodeMethod.body()._return(hashCodeCachedValueField);
                } else if (environment.hashCodeCaching() == Caching.SIMPLE) {
                    VariableNameSource nameSource = new VariableNameSource();
                    JFieldRef hashCodeField = JExpr.refthis(hashCodeCachedValueField);
                    JVar code = hashCodeMethod.body().decl(types._int, nameSource.get("code"), hashCodeField);
                    JConditional _if = hashCodeMethod.body()._if(code.eq0());
                    JInvocation invocation = JExpr.refthis(acceptorField).invoke(hashCodeMethodName);
                    _if._then().assign(code, invocation);
                    _if._then().assign(code, JOp.cond(code.ne0(), code, JExpr.lit(Integer.MIN_VALUE)));
                    _if._then().assign(hashCodeField, code);
                    hashCodeMethod.body()._return(code);
                } else if (environment.hashCodeCaching() == Caching.SYNCRONIZED) {
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
                    throw new IllegalStateException("Unsupported hashCodeCaching: " + environment.hashCodeCaching());

                JMethod acceptingInterfaceMethod = environment.buildAcceptingInterfaceMethod(JMod.PUBLIC, hashCodeMethodName);
                acceptingInterfaceMethod.type(types._int);

                int tag = 1;
                for (JMethod interfaceMethod1: environment.visitorMethodDeclarations()) {
                    JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                    JMethod caseHashCodeMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._int, hashCodeMethodName);
                    caseHashCodeMethod.annotate(Override.class);

                    VariableNameSource nameSource = new VariableNameSource();
                    List<JFieldVar> arguments = new ArrayList<>();
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
        }

        void buildToStringMethod() {
            if (!isError) {
                JMethod toStringMethod = environment.buildValueClassMethod(JMod.PUBLIC | JMod.FINAL, "toString");
                toStringMethod.type(types._String);
                toStringMethod.annotate(Override.class);
                Source.annotateNonnull(toStringMethod);
                JInvocation invocation1 = JExpr.refthis(acceptorField).invoke("toString");
                toStringMethod.body()._return(invocation1);

                for (JMethod interfaceMethod1: environment.visitorMethodDeclarations()) {
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
                    invocation.arg(environment.valueClassName() + "." + Source.capitalize(interfaceMethod1.name()) + "{");
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
        }

        void generateGetter(FieldConfiguration field) {
            String getterName = field.name();
            JMethod getterMethod = environment.buildValueClassMethod(Source.toJMod(field.accessLevel()) | JMod.FINAL, getterName);
            getterMethod.type(field.type());
            if (field.type().isReference()) {
                if (field.isNullable())
                    Source.annotateNullable(getterMethod);
                else
                    Source.annotateNonnull(getterMethod);
            }
            if (isError) {
                getterMethod.body()._throw(JExpr._new(types._UnsupportedOperationException));
            } else {
                JMethod implementation = environment.buildAcceptingInterfaceMethod(JMod.PUBLIC, getterName);
                implementation.type(field.type());
                if (field.type().isReference()) {
                    if (field.isNullable())
                        Source.annotateNullable(implementation);
                    else
                        Source.annotateNonnull(implementation);
                }

                JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(implementation);
                getterMethod.body()._return(invocation1);

                for (JMethod interfaceMethod1: environment.visitorMethodDeclarations()) {
                    JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                    getterMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, field.type(), getterName);
                    getterMethod.annotate(Override.class);
                    if (field.type().isReference()) {
                        if (field.isNullable())
                            Source.annotateNullable(getterMethod);
                        else
                            Source.annotateNonnull(getterMethod);
                    }
                    boolean isGettable = false;
                    for (JVar param: interfaceMethod1.params()) {
                        JFieldVar jfield = caseClass.fields().get(param.name());
                        if (field.isFieldValue(interfaceMethod1, param.name())) {
                            getterMethod.body()._return(jfield);
                            isGettable = true;
                        }
                    }
                    JVar param = interfaceMethod1.varParam();
                    if (param != null) {
                        JFieldVar jfield = caseClass.fields().get(param.name());
                        if (field.isFieldValue(interfaceMethod1, param.name())) {
                            getterMethod.body()._return(jfield);
                            isGettable = true;
                        }
                    }
                    if (!isGettable) {
                        JInvocation exceptionInvocation = JExpr._new(types._IllegalStateException);
                        exceptionInvocation.arg(field.name() + " is not accessible in this case: " + interfaceMethod1.name());
                        getterMethod.body()._throw(exceptionInvocation);
                    }
                }
            }
        }

        void generateUpdater(FieldConfiguration field) {
            String updaterName = field.name();
            AbstractJClass usedValueClassType = environment.wrappedValueClassTypeInsideValueClass();
            AbstractJClass unwrappedUsedValueClassType = environment.unwrappedValueClassTypeInsideValueClass();

            VariableNameSource updaterNameSource = new VariableNameSource();
            JMethod updaterMethod = environment.buildValueClassMethod(Source.toJMod(field.accessLevel()) | JMod.FINAL, updaterName);
            updaterMethod.type(usedValueClassType);
            Source.annotateNonnull(updaterMethod);
            JVar newValue;
            if (field.isVarArg())
                newValue = updaterMethod.varParam(field.type().elementType(), updaterNameSource.get("newValue"));
            else
                newValue = updaterMethod.param(field.type(), updaterNameSource.get("newValue"));
            if (field.type().isReference()) {
                if (field.isNullable()) {
                    Source.annotateNullable(newValue);
                } else {
                    Source.annotateNonnull(newValue);
                }
            }
            if (isError) {
                updaterMethod.body()._throw(JExpr._new(types._UnsupportedOperationException));
            } else {
                VariableNameSource aiUpdaterNameSource = new VariableNameSource();
                AbstractJClass usedAcceptingInterfaceType = environment.acceptingInterfaceTypeInsideValueClass();
                JMethod acceptingInterfaceUpdaterMethod = environment.buildAcceptingInterfaceMethod(JMod.PUBLIC, updaterName);
                acceptingInterfaceUpdaterMethod.type(usedAcceptingInterfaceType);
                Source.annotateNonnull(acceptingInterfaceUpdaterMethod);
                JVar newValueParam;
                if (field.isVarArg())
                    newValueParam = acceptingInterfaceUpdaterMethod.varParam(field.type().elementType(), aiUpdaterNameSource.get("newValue"));
                else
                    newValueParam = acceptingInterfaceUpdaterMethod.param(field.type(), aiUpdaterNameSource.get("newValue"));
                if (field.type().isReference()) {
                    if (field.isNullable()) {
                        Source.annotateNullable(newValueParam);
                    } else {
                        Source.annotateNonnull(newValueParam);
                    }
                }

                JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(acceptingInterfaceUpdaterMethod);
                invocation1.arg(newValue);
                JVar newAcceptor = updaterMethod.body().decl(usedAcceptingInterfaceType, updaterNameSource.get("newAcceptor"), invocation1);
                JInvocation constructorInvocation = JExpr._new(unwrappedUsedValueClassType);
                constructorInvocation.arg(newAcceptor);
                JConditional _if = updaterMethod.body()._if(newAcceptor.ne(JExpr.refthis(acceptorField)));
                _if._then()._return(environment.wrappedValue(usedValueClassType, constructorInvocation));

                IJExpression thisResult;
                if (!environment.wrappingEnabled())
                    thisResult = JExpr._this();
                else
                    thisResult = JExpr.cond(JExpr._this()._instanceof(usedValueClassType.erasure()), JExpr.cast(usedValueClassType, JExpr._this()), environment.wrappedValue(usedValueClassType, JExpr._this()));
                _if._else()._return(thisResult);

                for (JMethod interfaceMethod1: environment.visitorMethodDeclarations()) {
                    JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                    AbstractJClass usedCaseClassType = caseClass.narrow(caseClass.typeParams());
                    VariableNameSource ccUpdaterNameSource = new VariableNameSource();
                    JMethod caseClassUpdaterMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, usedAcceptingInterfaceType, updaterName);
                    Source.annotateNonnull(caseClassUpdaterMethod);
                    caseClassUpdaterMethod.annotate(Override.class);
                    if (field.isVarArg())
                        newValue = caseClassUpdaterMethod.varParam(field.type().elementType(), ccUpdaterNameSource.get("newValue"));
                    else
                        newValue = caseClassUpdaterMethod.param(field.type(), ccUpdaterNameSource.get("newValue"));
                    if (field.type().isReference()) {
                        if (field.isNullable()) {
                            Source.annotateNullable(newValue);
                        } else {
                            Source.annotateNonnull(newValue);
                        }
                    }
                    boolean isChanged = false;
                    JInvocation invocation = JExpr._new(usedCaseClassType);
                    for (JVar param: interfaceMethod1.params()) {
                        JFieldVar argument = caseClass.fields().get(param.name());
                        if (field.isFieldValue(interfaceMethod1, param.name())) {
                            invocation.arg(newValue);
                            isChanged = true;
                        } else {
                            invocation.arg(JExpr.refthis(argument));
                        }
                    }
                    JVar param = interfaceMethod1.varParam();
                    if (param != null) {
                        JFieldVar argument = caseClass.fields().get(param.name());
                        if (field.isFieldValue(interfaceMethod1, param.name())) {
                            invocation.arg(newValue);
                            isChanged = true;
                        } else {
                            invocation.arg(JExpr.refthis(argument));
                        }
                    }
                    if (isChanged)
                        caseClassUpdaterMethod.body()._return(invocation);
                    else
                        caseClassUpdaterMethod.body()._return(JExpr._this());
                }
            }
        }

        void generatePredicate(String name, PredicateConfigutation predicate) {
            JMethod predicateMethod = environment.buildValueClassMethod(Source.toJMod(predicate.accessLevel()) | JMod.FINAL, name);
            predicateMethod.type(types._boolean);
            if (isError) {
                predicateMethod.body()._throw(JExpr._new(types._UnsupportedOperationException));
            } else {
                JMethod implementation = environment.buildAcceptingInterfaceMethod(JMod.PUBLIC, name);
                implementation.type(types._boolean);

                predicateMethod.body()._return(JExpr.refthis(acceptorField).invoke(implementation));

                for (JMethod interfaceMethod1: environment.visitorMethodDeclarations()) {
                    JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                    predicateMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._boolean, name);
                    predicateMethod.annotate(Override.class);

                    boolean result = predicate.isTrueFor(interfaceMethod1);
                    predicateMethod.body()._return(JExpr.lit(result));
                }
            }
        }

        void buildEqualsMethod() {
            if (!isError) {
                AbstractJClass[] typeParams = new AbstractJClass[environment.getValueTypeParameters().size()];
                for (int i = 0; i < typeParams.length; i++)
                    typeParams[i] = types.createWildcard();
                AbstractJClass usedValueClassType = environment.wrappedValueClassType(typeParams);
                VisitorModel.NarrowedVisitor usedVisitor = environment.visitor(usedValueClassType, types._Boolean, types._RuntimeException);
                AbstractJClass unwrappedUsedValueClassType = environment.unwrappedValueClassType(typeParams);
                AbstractJClass usedAcceptorType = environment.acceptingInterfaceType(typeParams);
                String equalsImplementationMethodName = Source.decapitalize(environment.valueClassName()) + "Equals";
                JMethod equalsImplementationMethod = environment.buildAcceptingInterfaceMethod(JMod.PUBLIC, equalsImplementationMethodName);
                equalsImplementationMethod.type(types._boolean);
                VariableNameSource nameSource = new VariableNameSource();
                equalsImplementationMethod.param(usedAcceptorType, nameSource.get("thatAcceptor"));

                JMethod equalsMethod = environment.buildValueClassMethod(JMod.PUBLIC | JMod.FINAL, "equals");
                equalsMethod.type(types._boolean);
                nameSource = new VariableNameSource();
                equalsMethod.annotate(Override.class);
                JVar thatObject = equalsMethod.param(types._Object, nameSource.get("thatObject"));
                JConditional _if = equalsMethod.body()._if(JExpr._this().eq(thatObject));
                _if._then()._return(JExpr.TRUE);
                JConditional elseif = _if._elseif(thatObject._instanceof(unwrappedUsedValueClassType.erasure()).not());
                elseif._then()._return(JExpr.FALSE);
                JBlock _else = elseif._else();
                JVar that = _else.decl(unwrappedUsedValueClassType, nameSource.get("that"), JExpr.cast(unwrappedUsedValueClassType, thatObject));
                JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(equalsImplementationMethod);
                invocation1.arg(that.ref(acceptorField));
                IJExpression hashCodeResult = invocation1;
                if (environment.hashCodeCaching() == Caching.PRECOMPUTE) {
                    hashCodeResult = JExpr.refthis(hashCodeCachedValueField).eq(that.ref(hashCodeCachedValueField)).cand(invocation1);
                }
                _else._return(hashCodeResult);

                for (JMethod interfaceMethod1: environment.visitorMethodDeclarations()) {
                    JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                    equalsImplementationMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._boolean, equalsImplementationMethod.name());
                    equalsImplementationMethod.annotate(Override.class);
                    nameSource = new VariableNameSource();
                    JVar thatAcceptor = equalsImplementationMethod.param(usedAcceptorType, nameSource.get("thatAcceptor"));

                    String equalsCaseMethodName = equalsImplementationMethod.name() + Source.capitalize(interfaceMethod1.name());
                    JMethod equalsCaseMethod = environment.buildAcceptingInterfaceMethod(JMod.PUBLIC, equalsCaseMethodName);
                    equalsCaseMethod.type(types._boolean);
                    nameSource = new VariableNameSource();

                    JInvocation equalsCaseInvocation = thatAcceptor.invoke(equalsCaseMethod);
                    for (JVar param1: interfaceMethod1.params()) {
                        AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(param1.type()));
                        equalsCaseMethod.param(param1.mods().getValue(), argumentType, nameSource.get(param1.name()));
                        equalsCaseInvocation.arg(JExpr.refthis(caseClass.fields().get(param1.name())));
                    }
                    JVar varParam1 = interfaceMethod1.varParam();
                    if (varParam1 != null) {
                        AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(varParam1.type().elementType()));
                        equalsCaseMethod.varParam(varParam1.mods().getValue(), argumentType, nameSource.get(varParam1.name()));
                        equalsCaseInvocation.arg(JExpr.refthis(caseClass.fields().get(varParam1.name())));
                    }
                    equalsImplementationMethod.body()._return(equalsCaseInvocation);

                    for (JMethod interfaceMethod2: environment.visitorMethodDeclarations()) {
                        caseClass = caseClasses.get(interfaceMethod2.name());
                        equalsCaseMethod = caseClass.method(JMod.PUBLIC, types._boolean, equalsCaseMethodName);
                        equalsCaseMethod.annotate(Override.class);
                        nameSource = new VariableNameSource();

                        boolean isSameCase = interfaceMethod1.name().equals(interfaceMethod2.name());
                        EqualsMethod body = new EqualsMethod(types, equalsCaseMethod.body(), nameSource, environment.floatCustomization());

                        int i = 0;
                        boolean generatedReturn = false;
                        JVar varParam = interfaceMethod1.varParam();
                        for (JVar param: interfaceMethod1.params()) {
                            AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(param.type()));
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
                            AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(varParam.type().elementType()));
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
        }

        void buildCompareTo() {
            JMethod compareToMethod = environment.buildValueClassMethod(JMod.PUBLIC | JMod.FINAL, "compareTo");
            compareToMethod.type(types._int);
            compareToMethod.annotate(Override.class);
            VariableNameSource compareToMethodNameSource = new VariableNameSource();
            AbstractJClass usedValueClassType = environment.wrappedValueClassTypeInsideValueClass();
            VisitorModel.NarrowedVisitor usedVisitor = environment.visitor(usedValueClassType, types._Integer, types._RuntimeException);
            AbstractJClass unwrappedUsedValueClassType = environment.unwrappedValueClassTypeInsideValueClass();
            JVar that = compareToMethod.param(usedValueClassType, compareToMethodNameSource.get("that"));

            if (isError) {
                compareToMethod.body()._throw(JExpr._new(types._UnsupportedOperationException));
            } else {
                AbstractJClass usedAcceptorType = environment.acceptingInterfaceTypeInsideValueClass();
                String compareToMethodImplementationString = Source.decapitalize(environment.valueClassName()) + "ComapareTo";
                JMethod compareToMethodImplementation = environment.buildAcceptingInterfaceMethod(JMod.PUBLIC, compareToMethodImplementationString);
                compareToMethodImplementation.type(types._int);
                VariableNameSource nameSource = new VariableNameSource();
                compareToMethodImplementation.param(usedAcceptorType, nameSource.get("thatAcceptor"));

                JVar unwrappedVariable = !environment.wrappingEnabled()? that : compareToMethod.body().decl(unwrappedUsedValueClassType, compareToMethodNameSource.get("unwrapped"), that);
                JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(compareToMethodImplementation);
                invocation1.arg(unwrappedVariable.ref(acceptorField));
                compareToMethod.body()._return(invocation1);

                JMethod[] methods = new JMethod[environment.visitorMethodDeclarations().size()];
                methods = environment.visitorMethodDeclarations().toArray(methods);
                for (int interfaceMethod1Index = 0; interfaceMethod1Index < methods.length; interfaceMethod1Index++) {
                    JMethod interfaceMethod1 = methods[interfaceMethod1Index];
                    JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                    compareToMethodImplementation = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._int, compareToMethodImplementation.name());
                    compareToMethodImplementation.annotate(Override.class);
                    nameSource = new VariableNameSource();
                    JVar thatAcceptor = compareToMethodImplementation.param(usedAcceptorType, nameSource.get("thatAcceptor"));

                    String compareToCaseMethodName = compareToMethodImplementation.name() + Source.capitalize(interfaceMethod1.name());
                    JMethod compareToCaseMethod = environment.buildAcceptingInterfaceMethod(JMod.PUBLIC, compareToCaseMethodName);
                    compareToCaseMethod.type(types._int);
                    nameSource = new VariableNameSource();

                    JInvocation compareToCaseInvocation = thatAcceptor.invoke(compareToCaseMethod);
                    for (JVar param1: interfaceMethod1.params()) {
                        AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(param1.type()));
                        compareToCaseMethod.param(param1.mods().getValue(), argumentType, nameSource.get(param1.name()));
                        compareToCaseInvocation.arg(JExpr.refthis(caseClass.fields().get(param1.name())));
                    }
                    JVar varParam1 = interfaceMethod1.varParam();
                    if (varParam1 != null) {
                        AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(varParam1.type().elementType()));
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
                        CompareToMethod compareToMethodModel = new CompareToMethod(types, compareToCaseMethod.body(), nameSource, environment.floatCustomization());
                        CompareToMethod.Body body = null;

                        JVar varParam = interfaceMethod1.varParam();
                        for (JVar param: interfaceMethod1.params()) {
                            AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(param.type()));
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
                            AbstractJType argumentType = Source.toDeclarable(usedVisitor.getNarrowedType(varParam.type().elementType()));
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
        }

        void buildReadObjectMethod() {
            if (!isError && environment.hashCodeCaching() == Caching.PRECOMPUTE) {
                JMethod method = environment.buildValueClassMethod(JMod.PRIVATE, "readObject");
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
