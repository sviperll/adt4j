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

import com.github.sviperll.adt4j.AccessLevel;
import com.github.sviperll.adt4j.GeneratePredicate;
import com.github.sviperll.adt4j.Getter;
import com.github.sviperll.adt4j.Updater;
import com.github.sviperll.adt4j.model.util.Serialization;
import com.github.sviperll.adt4j.model.util.SourceException;
import com.github.sviperll.adt4j.model.util.Types;
import com.github.sviperll.adt4j.model.util.ValueVisitorInterfaceModel;
import com.github.sviperll.adt4j.model.util.VariableNameSource;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JAnnotationArrayMember;
import com.helger.jcodemodel.JAnnotationStringValue;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JTypeWildcard;
import com.helger.jcodemodel.JVar;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class ValueClassModel {
    @SuppressWarnings("unchecked")
    private static <T> T getAnnotationArgument(JAnnotationUse annotation, String name, Class<T> klass) {
        JAnnotationStringValue stringValue = annotation.getConstantParam(name);
        if (stringValue != null)
            return (T)stringValue.nativeValue();
        else {
            throw new NoSuchElementException(MessageFormat.format("{0} annotation argument not found for {1} annotation",
                                                                  name, annotation));
        }
    }

    private static int toJMod(AccessLevel accessLevel) {
        switch (accessLevel) {
            case PRIVATE:
                return JMod.PRIVATE;
            case PACKAGE:
                return JMod.NONE;
            case PROTECTED:
                return JMod.PROTECTED;
            case PUBLIC:
                return JMod.PUBLIC;
            default:
                throw new IllegalStateException("Unsupported AccessLevel: " + accessLevel);
        }
    }

    private static AbstractJType toDeclarable(AbstractJType type) {
        if (type instanceof JTypeWildcard) {
            JTypeWildcard wild = (JTypeWildcard)type;
            return wild.bound();
        }
        return type;
    }

    private static String capitalize(String s) {
        if (s.length() >= 2
            && Character.isHighSurrogate(s.charAt(0))
            && Character.isLowSurrogate(s.charAt(1))) {
            return s.substring(0, 2).toUpperCase(Locale.US) + s.substring(2);
        } else {
            return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1);
        }
    }

    private static String decapitalize(String s) {
        if (s.length() >= 2
            && Character.isHighSurrogate(s.charAt(0))
            && Character.isLowSurrogate(s.charAt(1))) {
            return s.substring(0, 2).toLowerCase(Locale.US) + s.substring(2);
        } else {
            return s.substring(0, 1).toLowerCase(Locale.US) + s.substring(1);
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
            throw new SourceException(MessageFormat.format("Parameter {0} is declared as both @Nullable and @Nonnull",
                                                           param.name()));
        if (!param.type().isReference() && hasNullable)
            throw new SourceException(MessageFormat.format("Parameter {0} is non-reference, but declared as @Nullable",
                                                           param.name()));
        return hasNullable;
    }

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

    MethodBuilder createMethodBuilder(Serialization serialization) throws JClassAlreadyExistsException {
        JFieldVar acceptorField = buildAcceptorField();
        Map<String, JDefinedClass> caseClasses = buildCaseClasses(serialization);
        return new MethodBuilder(caseClasses, acceptorField, serialization);
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
        JMethod factoryMethod = valueClass.method(toJMod(visitorInterface.factoryMethodAccessLevel()) | JMod.STATIC, types._void, "factory");
        factoryMethod.annotate(Nonnull.class);
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
            factoryMethod.annotate(Nonnull.class);
            factoryMethod.annotate(Override.class);

            JMethod constructorMethod = constructorMethods.get(interfaceMethod.name());
            JInvocation staticInvoke = valueClass.staticInvoke(constructorMethod);
            for (JTypeVar typeArgument: factoryClass.typeParams())
                staticInvoke.narrow(typeArgument);
            for (JVar param: interfaceMethod.params()) {
                AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException));
                JVar argument = factoryMethod.param(param.mods().getValue(), argumentType, param.name());
                staticInvoke.arg(argument);
            }
            JVar param = interfaceMethod.listVarParam();
            if (param != null) {
                AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(param.type().elementType(), usedValueClassType, usedValueClassType, types._RuntimeException));
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
        JDefinedClass caseClass = valueClass._class(JMod.PRIVATE | JMod.STATIC, capitalize(interfaceMethod.name()) + "Case" + acceptingInterface.name());
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
            AbstractJType paramType = toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException));
            JFieldVar field = caseClass.field(JMod.PRIVATE | JMod.FINAL, paramType, param.name());
            JVar argument = constructor.param(paramType, param.name());
            constructor.body().assign(JExpr._this().ref(field), argument);
        }
        JVar param = interfaceMethod.listVarParam();
        if (param != null) {
            AbstractJType paramType = toDeclarable(visitorInterface.narrowType(param.type().elementType(), usedValueClassType, usedValueClassType, types._RuntimeException));
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
        JVar param1 = interfaceMethod.listVarParam();
        if (param1 != null) {
            invocation.arg(JExpr._this().ref(param1.name()));
        }
        acceptMethod.body()._return(invocation);

        return caseClass;
    }

    Map<String, FieldConfiguration> getGettersConfigutation() throws SourceException {
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> gettersMap = new TreeMap<String, FieldConfiguration>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            for (JVar param: interfaceMethod.params()) {
                for (JAnnotationUse annotationUsage: param.annotations()) {
                    if (annotationUsage.getAnnotationClass().fullName().equals(Getter.class.getName())) {
                        AbstractJType paramType = toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException));
                        String getterName = getAnnotationArgument(annotationUsage, "value", String.class);
                        AccessLevel accessLevel = getAnnotationArgument(annotationUsage, "access", AccessLevel.class);
                        boolean isNullable = isNullable(param);
                        FieldConfiguration configuration = gettersMap.get(getterName);
                        if (configuration == null) {
                            configuration = new FieldConfiguration(getterName, paramType, accessLevel);
                            gettersMap.put(getterName, configuration);
                        }
                        try {
                            configuration.put(paramType, interfaceMethod, param.name(), new FieldFlags(isNullable, false, accessLevel));
                        } catch (FieldConfigurationException ex) {
                            throw new SourceException(MessageFormat.format("Unable to configure {0} getter: {1}",
                                                                           getterName, ex.getMessage()), ex);
                        }
                    }
                }
            }
            JVar param = interfaceMethod.listVarParam();
            if (param != null) {
                for (JAnnotationUse annotationUsage: param.annotations()) {
                    if (annotationUsage.getAnnotationClass().fullName().equals(Getter.class.getName())) {
                        AbstractJType paramType = toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException));
                        String getterName = getAnnotationArgument(annotationUsage, "value", String.class);
                        AccessLevel accessLevel = getAnnotationArgument(annotationUsage, "access", AccessLevel.class);
                        boolean isNullable = isNullable(param);
                        FieldConfiguration configuration = gettersMap.get(getterName);
                        if (configuration == null) {
                            configuration = new FieldConfiguration(getterName, paramType, accessLevel);
                            gettersMap.put(getterName, configuration);
                        }
                        try {
                            configuration.put(paramType, interfaceMethod, param.name(), new FieldFlags(isNullable, true, accessLevel));
                        } catch (FieldConfigurationException ex) {
                            throw new SourceException(MessageFormat.format("Unable to configure {0} getter: {1}",
                                                                           getterName, ex.getMessage()), ex);
                        }
                    }
                }
            }
        }
        return gettersMap;
    }

    Map<String, FieldConfiguration> getUpdatersConfiguration() throws SourceException {
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> updatersMap = new TreeMap<String, FieldConfiguration>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            for (JVar param: interfaceMethod.params()) {
                for (JAnnotationUse annotationUsage: param.annotations()) {
                    if (annotationUsage.getAnnotationClass().fullName().equals(Updater.class.getName())) {
                        AbstractJType paramType = toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException));
                        String updaterName = getAnnotationArgument(annotationUsage, "value", String.class);
                        AccessLevel accessLevel = getAnnotationArgument(annotationUsage, "access", AccessLevel.class);
                        boolean isNullable = isNullable(param);
                        FieldConfiguration configuration = updatersMap.get(updaterName);
                        if (configuration == null) {
                            configuration = new FieldConfiguration(updaterName, paramType, accessLevel);
                            updatersMap.put(updaterName, configuration);
                        }
                        try {
                            configuration.put(paramType, interfaceMethod, param.name(), new FieldFlags(isNullable, false, accessLevel));
                        } catch (FieldConfigurationException ex) {
                            throw new SourceException(MessageFormat.format("Unable to configure {0} updater: {1}",
                                                                           updaterName, ex.getMessage()), ex);
                        }
                    }
                }
            }
            JVar param = interfaceMethod.listVarParam();
            if (param != null) {
                for (JAnnotationUse annotationUsage: param.annotations()) {
                    if (annotationUsage.getAnnotationClass().fullName().equals(Updater.class.getName())) {
                        AbstractJType paramType = toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, visitorInterface.getResultTypeParameter(), types._RuntimeException));
                        String updaterName = getAnnotationArgument(annotationUsage, "value", String.class);
                        AccessLevel accessLevel = getAnnotationArgument(annotationUsage, "access", AccessLevel.class);
                        boolean isNullable = isNullable(param);
                        FieldConfiguration configuration = updatersMap.get(updaterName);
                        if (configuration == null) {
                            configuration = new FieldConfiguration(updaterName, paramType, accessLevel);
                            updatersMap.put(updaterName, configuration);
                        }
                        try {
                            configuration.put(paramType, interfaceMethod, param.name(), new FieldFlags(isNullable, true, accessLevel));
                        } catch (FieldConfigurationException ex) {
                            throw new SourceException(MessageFormat.format("Unable to configure {0} updater: {1}",
                                                                           updaterName, ex.getMessage()), ex);
                        }
                    }
                }
            }
        }
        return updatersMap;
    }

    Map<String, AccessLevel> getPredicates() throws SourceException {
        Map<String, AccessLevel> predicates = new TreeMap<String, AccessLevel>();
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            for (JAnnotationUse annotationUsage: interfaceMethod.annotations()) {
                if (annotationUsage.getAnnotationClass().fullName().equals(GeneratePredicate.class.getName())) {
                    String predicateName = getAnnotationArgument(annotationUsage, "value", String.class);
                    AccessLevel accessLevel = getAnnotationArgument(annotationUsage, "access", AccessLevel.class);
                    AccessLevel knownAccessLevel = predicates.get(predicateName);
                    if (knownAccessLevel == null) {
                        predicates.put(predicateName, accessLevel);
                    } else if (knownAccessLevel != accessLevel) {
                        throw new SourceException(MessageFormat.format("Unable to generate {0} predicate: inconsistent access levels",
                                                                       predicateName));
                    }
                }
            }
        }
        return predicates;
    }

    class MethodBuilder {
        private final Map<String, JDefinedClass> caseClasses;
        private final JFieldVar acceptorField;
        private final Serialization serialization;

        private MethodBuilder(Map<String, JDefinedClass> caseClasses, JFieldVar acceptorField, Serialization serialization) {
            this.caseClasses = caseClasses;
            this.acceptorField = acceptorField;
            this.serialization = serialization;
        }

        void buildPrivateConstructor() {
            JMethod constructor = valueClass.constructor(JMod.PRIVATE);
            constructor.param(acceptorField.type(), acceptorField.name());
            constructor.body().assign(JExpr.refthis(acceptorField.name()), JExpr.ref(acceptorField.name()));
        }

        void buildProtectedConstructor(Serialization serialization) throws JClassAlreadyExistsException {
            JMethod constructor = valueClass.constructor(JMod.PROTECTED);
            JAnnotationUse annotation = constructor.annotate(SuppressWarnings.class);
            annotation.paramArray("value", "null");
            AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
            JVar param = constructor.param(usedValueClassType, "implementation");
            param.annotate(Nonnull.class);
            JConditional nullCheck = constructor.body()._if(JExpr.ref("implementation").eq(JExpr._null()));
            JInvocation nullPointerExceptionConstruction = JExpr._new(types._NullPointerException);
            nullPointerExceptionConstruction.arg(JExpr.lit("Argument shouldn't be null: 'implementation' argument in class constructor invocation: " + valueClass.fullName()));
            nullCheck._then()._throw(nullPointerExceptionConstruction);

            constructor.body().assign(JExpr.refthis(acceptorField), param.ref(acceptorField));
        }

        void buildAcceptMethod() {
            JMethod acceptMethod = valueClass.method(toJMod(visitorInterface.acceptMethodAccessLevel()) | JMod.FINAL, types._void, visitorInterface.acceptMethodName());

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

        Map<String, JMethod> buildConstructorMethods(Serialization serialization) throws JClassAlreadyExistsException, SourceException {
            Map<String, JMethod> constructorMethods = new TreeMap<String, JMethod>();
            for (JMethod interfaceMethod: visitorInterface.methods()) {
                JMethod constructorMethod = valueClass.method(toJMod(visitorInterface.factoryMethodAccessLevel()) | JMod.STATIC, types._void, interfaceMethod.name());
                constructorMethod.annotate(Nonnull.class);
                for (JTypeVar visitorTypeParameter: visitorInterface.getValueTypeParameters()) {
                    Types.generifyWithBoundsFrom(constructorMethod, visitorTypeParameter.name(), visitorTypeParameter);
                }
                AbstractJClass usedValueClassType = valueClass.narrow(constructorMethod.typeParams());
                constructorMethod.type(usedValueClassType);
                for (JVar param: interfaceMethod.params()) {
                    AbstractJType paramType = toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, usedValueClassType, types._RuntimeException));
                    JVar constructorMethodParam = constructorMethod.param(param.mods().getValue(), paramType, param.name());
                    if (param.type().isReference())
                        constructorMethodParam.annotate(isNullable(param) ? Nullable.class : Nonnull.class);
                }
                JVar param = interfaceMethod.listVarParam();
                if (param != null) {
                    AbstractJType paramType = toDeclarable(visitorInterface.narrowType(param.type().elementType(), usedValueClassType, usedValueClassType, types._RuntimeException));
                    JVar constructorMethodParam = constructorMethod.varParam(param.mods().getValue(), paramType, param.name());
                    if (param.type().isReference())
                        constructorMethodParam.annotate(isNullable(param) ? Nullable.class : Nonnull.class);
                }

                AbstractJClass usedCaseClassType = caseClasses.get(interfaceMethod.name()).narrow(constructorMethod.typeParams());
                if (!interfaceMethod.params().isEmpty() || interfaceMethod.hasVarArgs()) {
                    boolean hasNullChecks = false;
                    for (JVar param1: interfaceMethod.params()) {
                        if (param1.type().isReference() && !isNullable(param1)) {
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
                    JVar param1 = interfaceMethod.listVarParam();
                    if (param1 != null) {
                        if (param1.type().isReference() && !isNullable(param1)) {
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
                    JVar param2 = interfaceMethod.listVarParam();
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

        void buildHashCodeMethod(int hashCodeBase) throws SourceException {
            String hashCodeMethodName = decapitalize(valueClass.name()) + "HashCode";
            JMethod hashCodeMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._int, "hashCode");
            hashCodeMethod.annotate(Override.class);
            JInvocation invocation = JExpr.refthis(acceptorField).invoke(hashCodeMethodName);
            hashCodeMethod.body()._return(invocation);

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
                JVar param = interfaceMethod1.listVarParam();
                if (param != null) {
                    varArgument = caseClass.fields().get(param.name());
                }

                HashCodeMethod methodModel = new HashCodeMethod(types, hashCodeBase, caseHashCodeMethod.body(), nameSource);
                HashCodeMethod.Body body = methodModel.createBody(tag);
                for (int i = 0; i < arguments.size(); i++) {
                    param = interfaceMethod1.params().get(i);
                    JFieldVar argument = arguments.get(i);
                    if (isNullable(param))
                        body.appendNullableValue(argument.type(), JExpr.refthis(argument));
                    else
                        body.appendNotNullValue(argument.type(), JExpr.refthis(argument));
                }
                if (varArgument != null) {
                    if (isNullable(param))
                        body.appendNullableValue(varArgument.type(), JExpr.refthis(varArgument));
                    else
                        body.appendNotNullValue(varArgument.type(), JExpr.refthis(varArgument));
                }
                caseHashCodeMethod.body()._return(body.result());
                tag++;
            }
        }

        void buildToStringMethod() throws SourceException {
            JMethod toStringMethod = valueClass.method(JMod.PUBLIC | JMod.FINAL, types._String, "toString");
            toStringMethod.annotate(Override.class);
            toStringMethod.annotate(Nonnull.class);
            JInvocation invocation1 = JExpr.refthis(acceptorField).invoke("toString");
            toStringMethod.body()._return(invocation1);

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                JMethod caseToStringMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._String, "toString");
                caseToStringMethod.annotate(Override.class);
                caseToStringMethod.annotate(Nonnull.class);

                VariableNameSource nameSource = new VariableNameSource();
                List<JFieldVar> arguments = new ArrayList<JFieldVar>();
                JFieldVar varArgument = null;
                for (JVar param: interfaceMethod1.params()) {
                    arguments.add(caseClass.fields().get(param.name()));
                }
                JVar param = interfaceMethod1.listVarParam();
                if (param != null) {
                    varArgument = caseClass.fields().get(param.name());
                }

                JVar result = caseToStringMethod.body().decl(types._StringBuilder, nameSource.get("result"), JExpr._new(types._StringBuilder));
                JInvocation invocation = caseToStringMethod.body().invoke(result, "append");
                invocation.arg(valueClass.name() + "." + capitalize(interfaceMethod1.name()) + "{");
                ToStringMethodBody body = new ToStringMethodBody(caseToStringMethod.body(), result);
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
                    body.appendParam(varArgument.type(), interfaceMethod1.listVarParam().name(), JExpr.refthis(varArgument));
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
                    getterMethod.annotate(Nullable.class);
                else
                    getterMethod.annotate(Nonnull.class);
            }

            getterMethod = valueClass.method(toJMod(configuration.accessLevel()) | JMod.FINAL, configuration.type(), getterName);
            if (configuration.type().isReference()) {
                if (configuration.isNullable())
                    getterMethod.annotate(Nullable.class);
                else
                    getterMethod.annotate(Nonnull.class);
            }
            JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(getterName);
            getterMethod.body()._return(invocation1);

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                JMethod geterMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, configuration.type(), getterName);
                geterMethod.annotate(Override.class);
                if (configuration.type().isReference()) {
                    if (configuration.isNullable())
                        geterMethod.annotate(Nullable.class);
                    else
                        geterMethod.annotate(Nonnull.class);
                }
                boolean isGettable = false;
                for (JVar param: interfaceMethod1.params()) {
                    JFieldVar field = caseClass.fields().get(param.name());
                    if (configuration.isFieldValue(interfaceMethod1, param.name())) {
                        geterMethod.body()._return(field);
                        isGettable = true;
                    }
                }
                JVar param = interfaceMethod1.listVarParam();
                if (param != null) {
                    JFieldVar field = caseClass.fields().get(param.name());
                    if (configuration.isFieldValue(interfaceMethod1, param.name())) {
                        geterMethod.body()._return(field);
                        isGettable = true;
                    }
                }
                if (!isGettable) {
                    JInvocation exceptionInvocation = JExpr._new(types._IllegalStateException);
                    exceptionInvocation.arg(configuration.name() + " is not accessible in this case: " + interfaceMethod1.name());
                    geterMethod.body()._throw(exceptionInvocation);
                }
            }
        }

        void generateUpdater(FieldConfiguration configuration) throws SourceException {
            VariableNameSource nameSource = new VariableNameSource();
            String updaterName = configuration.name();
            AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());

            JMethod updaterMethod = acceptingInterface.method(JMod.PUBLIC, usedValueClassType, updaterName);
            updaterMethod.annotate(Nonnull.class);
            JVar newValueParam;
            if (configuration.isVarArg())
                newValueParam = updaterMethod.varParam(configuration.type().elementType(), nameSource.get("newValue"));
            else
                newValueParam = updaterMethod.param(configuration.type(), nameSource.get("newValue"));
            if (configuration.type().isReference()) {
                if (configuration.isNullable()) {
                    newValueParam.annotate(Nullable.class);
                } else {
                    newValueParam.annotate(Nonnull.class);
                }
            }

            nameSource = new VariableNameSource();
            updaterMethod = valueClass.method(toJMod(configuration.accessLevel()) | JMod.FINAL, usedValueClassType, updaterName);
            updaterMethod.annotate(Nonnull.class);
            JVar newValue;
            if (configuration.isVarArg())
                newValue = updaterMethod.varParam(configuration.type().elementType(), nameSource.get("newValue"));
            else
                newValue = updaterMethod.param(configuration.type(), nameSource.get("newValue"));
            if (configuration.type().isReference()) {
                if (configuration.isNullable()) {
                    newValue.annotate(Nullable.class);
                } else {
                    newValue.annotate(Nonnull.class);
                }
            }
            JInvocation invocation1 = JExpr.refthis(acceptorField).invoke(updaterName);
            invocation1.arg(newValue);
            updaterMethod.body()._return(invocation1);

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                nameSource = new VariableNameSource();
                updaterMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, usedValueClassType, updaterName);
                updaterMethod.annotate(Nonnull.class);
                updaterMethod.annotate(Override.class);
                if (configuration.isVarArg())
                    newValue = updaterMethod.varParam(configuration.type().elementType(), nameSource.get("newValue"));
                else
                    newValue = updaterMethod.param(configuration.type(), nameSource.get("newValue"));
                if (configuration.type().isReference()) {
                    if (configuration.isNullable()) {
                        newValue.annotate(Nullable.class);
                    } else {
                        newValue.annotate(Nonnull.class);
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
                JVar param = interfaceMethod1.listVarParam();
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

        void generatePredicate(String name, AccessLevel accessLevel) {
            acceptingInterface.method(JMod.PUBLIC, types._boolean, name);

            JMethod predicateMethod = valueClass.method(toJMod(accessLevel) | JMod.FINAL, types._boolean, name);
            predicateMethod.body()._return(JExpr.refthis(acceptorField).invoke(name));

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                predicateMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._boolean, name);
                predicateMethod.annotate(Override.class);

                boolean result = false;
                for (JAnnotationUse annotationUsage: interfaceMethod1.annotations()) {
                    if (annotationUsage.getAnnotationClass().fullName().equals(GeneratePredicate.class.getName())) {
                        String predicateName = getAnnotationArgument(annotationUsage, "value", String.class);
                        if (predicateName.equals(name)) {
                            result = true;
                            break;
                        }
                    }
                }
                predicateMethod.body()._return(JExpr.lit(result));
            }

        }

        void buildEqualsMethod() throws SourceException, JClassAlreadyExistsException {
            AbstractJClass[] typeParams = new AbstractJClass[valueClass.typeParams().length];
            for (int i = 0; i < typeParams.length; i++)
                typeParams[i] = valueClass.owner().wildcard();
            AbstractJClass usedValueClassType = valueClass.narrow(typeParams);
            AbstractJClass usedAcceptorType = acceptingInterface.narrow(typeParams);
            String equalsImplementationMethodName = decapitalize(valueClass.name()) + "Equals";
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
            _else._return(invocation1);

            for (JMethod interfaceMethod1: visitorInterface.methods()) {
                JDefinedClass caseClass = caseClasses.get(interfaceMethod1.name());
                equalsImplementationMethod = caseClass.method(JMod.PUBLIC | JMod.FINAL, types._boolean, equalsImplementationMethod.name());
                equalsImplementationMethod.annotate(Override.class);
                nameSource = new VariableNameSource();
                JVar thatAcceptor = equalsImplementationMethod.param(usedAcceptorType, nameSource.get("thatAcceptor"));

                String equalsCaseMethodName = equalsImplementationMethod.name() + capitalize(interfaceMethod1.name());
                JMethod equalsCaseMethod = acceptingInterface.method(JMod.PUBLIC, types._boolean, equalsCaseMethodName);
                nameSource = new VariableNameSource();

                JInvocation equalsCaseInvocation = thatAcceptor.invoke(equalsCaseMethod);
                for (JVar param1: interfaceMethod1.params()) {
                    AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(param1.type(), usedValueClassType, types._Boolean, types._RuntimeException));
                    equalsCaseMethod.param(param1.mods().getValue(), argumentType, nameSource.get(param1.name()));
                    equalsCaseInvocation.arg(JExpr.refthis(caseClass.fields().get(param1.name())));
                }
                JVar varParam1 = interfaceMethod1.listVarParam();
                if (varParam1 != null) {
                    AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(varParam1.type().elementType(), usedValueClassType, types._Boolean, types._RuntimeException));
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
                    JVar varParam = interfaceMethod1.listVarParam();
                    for (JVar param: interfaceMethod1.params()) {
                        AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, types._Boolean, types._RuntimeException));
                        JVar argument1 = equalsCaseMethod.param(param.mods().getValue(), argumentType, nameSource.get(param.name()));
                        if (isSameCase) {
                            JFieldVar argument2 = caseClass.fields().get(param.name());
                            boolean isLast = varParam == null && i == interfaceMethod1.params().size() - 1;
                            if (isNullable(param))
                                body.appendNullableValue(argumentType, argument1, JExpr.refthis(argument2), isLast);
                            else
                                body.appendNotNullValue(argumentType, argument1, JExpr.refthis(argument2), isLast);
                        }
                        i++;
                    }
                    if (varParam != null) {
                        AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(varParam.type().elementType(), usedValueClassType, types._Boolean, types._RuntimeException));
                        JVar varArgument1 = equalsCaseMethod.varParam(varParam.mods().getValue(), argumentType, nameSource.get(varParam.name()));
                        if (isSameCase) {
                            JFieldVar varArgument2 = caseClass.fields().get(varParam.name());
                            if (isNullable(varParam))
                                body.appendNullableValue(varArgument1.type(), varArgument1, JExpr.refthis(varArgument2), true);
                            else
                                body.appendNotNullValue(varArgument1.type(), varArgument1, JExpr.refthis(varArgument2), true);
                        }
                    }
                    boolean isEmpty = i == 0 && varParam == null;
                    if (!isSameCase)
                        equalsCaseMethod.body()._return(JExpr.FALSE);
                    else if (isSameCase && isEmpty)
                        equalsCaseMethod.body()._return(JExpr.TRUE);
                }
            }
        }

        void buildCompareTo() throws SourceException, JClassAlreadyExistsException {
            AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
            AbstractJClass usedAcceptorType = acceptingInterface.narrow(valueClass.typeParams());
            JMethod compareToMethodImplementation = acceptingInterface.method(JMod.PUBLIC, types._int, "compareTo");
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

                String compareToCaseMethodName = compareToMethodImplementation.name() + capitalize(interfaceMethod1.name());
                JMethod compareToCaseMethod = acceptingInterface.method(JMod.PUBLIC, types._int, compareToCaseMethodName);
                nameSource = new VariableNameSource();

                JInvocation compareToCaseInvocation = thatAcceptor.invoke(compareToCaseMethod);
                for (JVar param1: interfaceMethod1.params()) {
                    AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(param1.type(), usedValueClassType, types._Integer, types._RuntimeException));
                    compareToCaseMethod.param(param1.mods().getValue(), argumentType, nameSource.get(param1.name()));
                    compareToCaseInvocation.arg(JExpr.refthis(caseClass.fields().get(param1.name())));
                }
                JVar varParam1 = interfaceMethod1.listVarParam();
                if (varParam1 != null) {
                    AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(varParam1.type().elementType(), usedValueClassType, types._Integer, types._RuntimeException));
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

                    JVar varParam = interfaceMethod1.listVarParam();
                    for (JVar param: interfaceMethod1.params()) {
                        AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(param.type(), usedValueClassType, types._Integer, types._RuntimeException));
                        JVar argument1 = compareToCaseMethod.param(param.mods().getValue(), argumentType, nameSource.get(param.name()));
                        if (isSameCase) {
                            if (body == null)
                                body = compareToMethodModel.createBody();
                            JFieldVar argument2 = caseClass.fields().get(param.name());
                            if (isNullable(param))
                                body.appendNullableValue(argumentType, argument1, JExpr.refthis(argument2));
                            else
                                body.appendNotNullValue(argumentType, argument1, JExpr.refthis(argument2));
                        }
                    }
                    if (varParam != null) {
                        AbstractJType argumentType = toDeclarable(visitorInterface.narrowType(varParam.type().elementType(), usedValueClassType, types._Boolean, types._RuntimeException));
                        JVar varArgument1 = compareToCaseMethod.varParam(varParam.mods().getValue(), argumentType, nameSource.get(varParam.name()));
                        if (isSameCase) {
                            if (body == null)
                                body = compareToMethodModel.createBody();
                            JFieldVar varArgument2 = caseClass.fields().get(varParam.name());
                            if (isNullable(varParam))
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
}
