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
package com.github.sviperll.adt4j.model.config;

import com.github.sviperll.adt4j.Caching;
import com.github.sviperll.adt4j.MemberAccess;
import com.github.sviperll.adt4j.Visitor;
import com.github.sviperll.adt4j.model.util.GenerationResult;
import com.github.sviperll.adt4j.model.util.Source;
import com.github.sviperll.adt4j.model.util.Types;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ValueVisitorInterfaceModel {
    private static final String VISITOR_SUFFIX = "Visitor";
    private static final String VALUE_SUFFIX = "Value";

    public static GenerationResult<ValueVisitorInterfaceModel> createInstance(JDefinedClass jVisitorModel, Visitor visitorAnnotation, JAnnotationUse annotation) {
        List<String> errors = new ArrayList<String>();
        GenerationResult<ValueVisitorTypeParameters> typeParametersResult = createValueVisitorTypeParameters(jVisitorModel, visitorAnnotation);
        errors.addAll(typeParametersResult.errors());
        GenerationResult<Map<String, JMethod>> methodsResult = createMethodMap(jVisitorModel, typeParametersResult.result());
        errors.addAll(methodsResult.errors());
        String acceptMethodName = annotation.getParam("acceptMethodName", String.class);
        MemberAccess acceptMethodAccess = annotation.getParam("acceptMethodAccess", MemberAccess.class);
        boolean isPublic = annotation.getParam("isPublic", Boolean.class);
        Caching hashCodeCaching = annotation.getParam("hashCodeCaching", Caching.class);
        int hashCodeBase = annotation.getParam("hashCodeBase", Integer.class);
        boolean isComparable = annotation.getParam("isComparable", Boolean.class);
        Serialization serialization = serialization(annotation);
        String valueClassName = valueClassName(jVisitorModel, annotation);
        AbstractJClass extendsClass = annotation.getParam("extendsClass", AbstractJClass.class);
        AbstractJClass wrapperClass = annotation.getParam("wrapperClass", AbstractJClass.class);
        if (wrapperClass.fullName().equals("java.lang.Object"))
            wrapperClass = null;
        AbstractJClass[] interfaces = annotation.getParam("implementsInterfaces", AbstractJClass[].class);

        AcceptMethodCustomization acceptMethodCustomization = new AcceptMethodCustomization(acceptMethodName, acceptMethodAccess);
        InterfacesCustomization interfaceCustomization = new InterfacesCustomization(isComparable, serialization, interfaces);
        APICustomization apiCustomization = new APICustomization(isPublic, acceptMethodCustomization, interfaceCustomization);
        ImplementationCustomization implementationCustomization = new ImplementationCustomization(hashCodeBase, hashCodeCaching);
        ClassCustomization classCustomization = new ClassCustomization(valueClassName, wrapperClass, extendsClass);
        Customization customiztion = new Customization(classCustomization, apiCustomization, implementationCustomization);
        return new GenerationResult<>(new ValueVisitorInterfaceModel(jVisitorModel, typeParametersResult.result(), methodsResult.result(), customiztion), errors);
    }

    private static Serialization serialization(JAnnotationUse annotation) {
        if (!annotation.getParam("isSerializable", Boolean.class))
            return Serialization.notSerializable();
        else
            return Serialization.serializable(annotation.getParam("serialVersionUID", Long.class));
    }

    private static String valueClassName(JDefinedClass jVisitorModel, JAnnotationUse annotation) {
        String className = annotation.getParam("className", String.class);
        if (!className.equals(":auto")) {
            return className;
        } else {
            String visitorName = jVisitorModel.name();
            if (visitorName == null)
                throw new IllegalStateException("Visitor interface without a name: " + jVisitorModel);
            else {
                if (visitorName.endsWith(VISITOR_SUFFIX))
                    return visitorName.substring(0, visitorName.length() - VISITOR_SUFFIX.length());
                else
                    return visitorName + VALUE_SUFFIX;
            }
        }
    }

    private static GenerationResult<ValueVisitorTypeParameters> createValueVisitorTypeParameters(JDefinedClass jVisitorModel,
                                                                               Visitor annotation) {
        List<String> errors = new ArrayList<>();
        JTypeVar resultType = null;
        JTypeVar exceptionType = null;
        JTypeVar selfType = null;
        List<JTypeVar> valueClassTypeParameters = new ArrayList<>();
        for (JTypeVar typeVariable: jVisitorModel.typeParams()) {
            if (typeVariable.name().equals(annotation.resultVariableName()))
                resultType = typeVariable;
            else if (typeVariable.name().equals(annotation.selfReferenceVariableName()))
                selfType = typeVariable;
            else if (typeVariable.name().equals(annotation.exceptionVariableName()))
                exceptionType = typeVariable;
            else
                valueClassTypeParameters.add(typeVariable);
        }
        if (resultType == null) {
            errors.add(MessageFormat.format("Result type-variable is not found for visitor, expecting: {0}",
                    annotation.resultVariableName()));
            resultType = jVisitorModel.typeParams().length == 0 ? null : jVisitorModel.typeParams()[0];
        }
        if (exceptionType == null && !annotation.exceptionVariableName().equals(":none")) {
            errors.add(MessageFormat.format("Exception type-variable is not found for visitor, expecting: {0}",
                    annotation.exceptionVariableName()));
        }
        if (selfType == null && !annotation.selfReferenceVariableName().equals(":none")) {
            errors.add(MessageFormat.format("Self reference type-variable is not found for visitor, expecting: {0}",
                    annotation.selfReferenceVariableName()));
        }
        return new GenerationResult<>(new ValueVisitorTypeParameters(resultType, exceptionType, selfType, valueClassTypeParameters), errors);
    }

    private static GenerationResult<Map<String, JMethod>> createMethodMap(JDefinedClass jVisitorModel,
                                                        ValueVisitorTypeParameters typeParameters) {
        List<String> errors = new ArrayList<>();
        Map<String, JMethod> methods = new TreeMap<>();
        for (JMethod method: jVisitorModel.methods()) {
            if (method.type().isError()) {
                errors.add(MessageFormat.format("Visitor method result type is erroneous: {0}", method.name()));
            } else if (!typeParameters.isResult(method.type())) {
                errors.add(MessageFormat.format("Visitor method is only allowed to return type declared as a result type of visitor: {0}: expecting {1}, found: {2}",
                        method.name(), typeParameters.getResultTypeParameter().name(), method.type().fullName()));
            }

            Collection<AbstractJClass> exceptions = method.getThrows();
            if (exceptions.size() > 1)
                errors.add(MessageFormat.format("Visitor method is allowed to throw no exceptions or throw single exception, declared as type-variable: {0}",
                        method.name()));
            else if (exceptions.size() == 1) {
                AbstractJClass exception = exceptions.iterator().next();
                if (exception.isError())
                    errors.add(MessageFormat.format("Visitor method exception type is erroneous: {0}", method.name()));
                else if (!typeParameters.isException(exception))
                    errors.add(MessageFormat.format("Visitor method throws exception, not declared as type-variable: {0}: {1}",
                        method.name(), exception.fullName()));
            }

            JMethod exitingValue = methods.put(method.name(), method);
            if (exitingValue != null) {
                errors.add(MessageFormat.format("Method overloading is not supported for visitor interfaces: two methods with the same name: {0}",
                                                    method.name()));
            }
            for (JVar param: method.params()) {
                GenerationResult<Boolean> nullability = Source.getNullability(param);
                errors.addAll(nullability.errors());
            }
        }
        return new GenerationResult<>(methods, errors);
    }

    private final AbstractJClass visitorInterfaceModel;
    private final ValueVisitorTypeParameters typeParameters;
    private final Map<String, JMethod> methods;
    private final Customization customization;

    private ValueVisitorInterfaceModel(JDefinedClass visitorInterfaceModel, ValueVisitorTypeParameters typeParameters,
                                       Map<String, JMethod> methods, Customization customiztion) {
        this.visitorInterfaceModel = visitorInterfaceModel;
        this.typeParameters = typeParameters;
        this.methods = methods;
        this.customization = customiztion;
    }

    public JTypeVar getResultTypeParameter() {
        return typeParameters.getResultTypeParameter();
    }

    public JTypeVar getExceptionTypeParameter() {
        return typeParameters.getExceptionTypeParameter();
    }

    public JTypeVar getSelfTypeParameter() {
        return typeParameters.getSelfTypeParameter();
    }

    public List<JTypeVar> getValueTypeParameters() {
        return typeParameters.getValueTypeParameters();
    }

    public AbstractJClass narrowed(AbstractJClass usedDataType, AbstractJClass resultType, AbstractJClass exceptionType) {
        AbstractJClass result = visitorInterfaceModel;
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            result = result.narrow(typeParameters.substituteSpecialType(typeVariable, usedDataType, resultType, exceptionType));
        }
        return result;
    }

    public Collection<JMethod> methods() {
        return methods.values();
    }

    public AbstractJType narrowType(AbstractJType type, AbstractJClass usedDataType, AbstractJClass resultType, AbstractJClass exceptionType) {
        return narrowType(type, usedDataType, resultType, exceptionType, usedDataType);
    }

    public AbstractJType narrowType(AbstractJType type, AbstractJClass usedDataType, AbstractJClass resultType, AbstractJClass exceptionType, AbstractJClass selfType) {
        type = typeParameters.substituteSpecialType(type, selfType, resultType, exceptionType);
        List<? extends AbstractJClass> dataTypeArguments = usedDataType.getTypeParameters();
        int dataTypeIndex = 0;
        for (int i = 0; i < visitorInterfaceModel.typeParams().length; i++) {
            JTypeVar typeParameter = visitorInterfaceModel.typeParams()[i];
            if (!typeParameters.isSpecial(typeParameter)) {
                if (type == typeParameter)
                    return dataTypeArguments.get(dataTypeIndex);
                dataTypeIndex++;
            }
        }

        if (!(type instanceof AbstractJClass)) {
            return type;
        } else {
            /*
             * When we get type with type-parameters we should narrow
             * type-parameters.
             * For example, we must replace Tree<T> to Tree<String> if
             * T type-variable is bound to String by usedDataType
             */

            AbstractJClass narrowedType = (AbstractJClass)type;
            if (narrowedType.getTypeParameters().isEmpty()) {
                return narrowedType;
            } else {
                AbstractJClass result = narrowedType.erasure();
                for (AbstractJClass typeArgument: narrowedType.getTypeParameters()) {
                    result = result.narrow(narrowType(typeArgument, usedDataType, resultType, exceptionType, selfType));
                }
                return result;
            }
        }
    }

    public boolean isSelf(AbstractJType type) {
        return typeParameters.isSelf(type);
    }

    public boolean isResult(AbstractJType type) {
        return typeParameters.isResult(type);
    }

    public boolean isException(AbstractJType type) {
        return typeParameters.isException(type);
    }

    public String acceptMethodName() {
        return customization.acceptMethodName();
    }

    public boolean isValueClassPublic() {
        return customization.isValueClassPublic();
    }

    public MemberAccess factoryMethodAccessLevel() {
        return customization.isValueClassPublic() ? MemberAccess.PUBLIC : MemberAccess.PACKAGE;
    }

    public MemberAccess acceptMethodAccessLevel() {
        return customization.acceptMethodAccessLevel();
    }

    public Caching hashCodeCaching() {
        return customization.hashCodeCaching();
    }

    public boolean isValueClassSerializable() {
        return customization.isValueClassSerializable();
    }

    public boolean isValueClassComparable() {
        return customization.isValueClassComparable();
    }

    public String valueClassName() {
        return customization.className();
    }

    public AbstractJClass[] implementsInterfaces() {
        return customization.implementsInterfaces();
    }

    public AbstractJClass valueClassExtends() {
        return customization.valueClassExtends();
    }

    public int hashCodeBase() {
        return customization.hashCodeBase();
    }

    public Serialization serialization() {
        return customization.serialization();
    }

    public long serialVersionUIDForGeneratedCode() {
        return customization.serialVersionUIDForGeneratedCode();
    }

    public GenerationResult<Map<String, FieldConfiguration>> getGettersConfigutation(JDefinedClass valueClass, Types types) {
        List<String> errors = new ArrayList<>();
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> gettersMap = new TreeMap<>();
        FieldReader reader = new FieldReader(gettersMap, errors);
        for (JMethod interfaceMethod: methods()) {
            for (JVar param: interfaceMethod.params()) {
                AbstractJType paramType = Source.toDeclarable(narrowType(param.type(), usedValueClassType, getResultTypeParameter(), types._RuntimeException));
                reader.readGetter(interfaceMethod, param, paramType, false);
            }
            JVar param = interfaceMethod.varParam();
            if (param != null) {
                AbstractJType paramType = Source.toDeclarable(narrowType(param.type(), usedValueClassType, getResultTypeParameter(), types._RuntimeException));
                reader.readGetter(interfaceMethod, param, paramType, true);
            }
        }
        return new GenerationResult<>(gettersMap, errors);
    }

    public GenerationResult<Map<String, FieldConfiguration>> getUpdatersConfiguration(JDefinedClass valueClass, Types types) {
        List<String> errors = new ArrayList<>();
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> updatersMap = new TreeMap<>();
        FieldReader reader = new FieldReader(updatersMap, errors);
        for (JMethod interfaceMethod: methods()) {
            for (JVar param: interfaceMethod.params()) {
                AbstractJType paramType = Source.toDeclarable(narrowType(param.type(), usedValueClassType, getResultTypeParameter(), types._RuntimeException));
                reader.readUpdater(interfaceMethod, param, paramType, false);
            }
            JVar param = interfaceMethod.varParam();
            if (param != null) {
                AbstractJType paramType = Source.toDeclarable(narrowType(param.type(), usedValueClassType, getResultTypeParameter(), types._RuntimeException));
                reader.readUpdater(interfaceMethod, param, paramType, true);
            }
        }
        return new GenerationResult<>(updatersMap, errors);
    }

    public GenerationResult<Map<String, PredicateConfigutation>> getPredicates() {
        List<String> errors = new ArrayList<>();
        Map<String, PredicateConfigutation> predicates = new TreeMap<>();
        PredicatesReader predicatesReader = new PredicatesReader(predicates, errors);
        for (JMethod interfaceMethod: methods()) {
            for (JAnnotationUse annotationUsage: interfaceMethod.annotations()) {
                predicatesReader.read(interfaceMethod, annotationUsage);
            }
        }
        return new GenerationResult<>(predicates, errors);
    }

    public AbstractJClass wrapValueClass(AbstractJClass valueClass) {
        AbstractJClass wrapperClass = customization.wrapperClass();
        return wrapperClass != null ? wrapperClass : valueClass;
    }

    public IJExpression wrapValue(AbstractJType usedWrappedClassType, IJExpression valueExpression) {
        AbstractJClass wrapperClass = customization.wrapperClass();
        if (wrapperClass == null)
            return valueExpression;
        else {
            JInvocation invocation1 = JExpr._new(usedWrappedClassType);
            invocation1.arg(valueExpression);
            return invocation1;
        }
    }

    public boolean wraps() {
        AbstractJClass wrapperClass = customization.wrapperClass();
        return wrapperClass != null;
    }


}
