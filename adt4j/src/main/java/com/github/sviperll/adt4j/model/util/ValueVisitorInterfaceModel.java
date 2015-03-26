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
package com.github.sviperll.adt4j.model.util;

import com.github.sviperll.meta.MemberAccess;
import com.github.sviperll.adt4j.GenerateValueClassForVisitor;
import com.github.sviperll.meta.SourceCodeValidationException;
import com.github.sviperll.meta.Visitor;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JTypeVar;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;

public class ValueVisitorInterfaceModel {
    public static ValueVisitorInterfaceModel createInstance(JDefinedClass jVisitorModel, Visitor visitorAnnotation, GenerateValueClassForVisitor annotation) throws SourceCodeValidationException {
        ValueVisitorTypeParameters typeParameters = createValueVisitorTypeParameters(jVisitorModel, visitorAnnotation);
        Map<String, JMethod> methods = createMethodMap(jVisitorModel, typeParameters);
        APICustomization apiCustomization = new APICustomization(annotation.acceptMethodName(), annotation.acceptMethodAccess(), annotation.isPublic());
        return new ValueVisitorInterfaceModel(jVisitorModel, typeParameters, methods, apiCustomization);
    }

    private static ValueVisitorTypeParameters createValueVisitorTypeParameters(JDefinedClass jVisitorModel,
                                                                               Visitor annotation)
            throws SourceCodeValidationException {
        JTypeVar resultType = null;
        @Nullable JTypeVar exceptionType = null;
        @Nullable JTypeVar selfType = null;
        List<JTypeVar> valueClassTypeParameters = new ArrayList<JTypeVar>();
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
            throw new SourceCodeValidationException(MessageFormat.format("Result type-variable is not found for {0} visitor, expecting: {1}",
                                                                     jVisitorModel, annotation.resultVariableName()));
        }
        if (exceptionType == null && !annotation.exceptionVariableName().equals(":none")) {
            throw new SourceCodeValidationException(MessageFormat.format("Exception type-variable is not found for {0} visitor, expecting: {1}",
                                                                     jVisitorModel, annotation.exceptionVariableName()));
        }
        if (selfType == null && !annotation.selfReferenceVariableName().equals(":none")) {
            throw new SourceCodeValidationException(MessageFormat.format("Self reference type-variable is not found for {0} visitor, expecting: {1}",
                                                                     jVisitorModel,
                                                                     annotation.selfReferenceVariableName()));
        }
        return new ValueVisitorTypeParameters(resultType, exceptionType, selfType, valueClassTypeParameters);
    }

    private static Map<String, JMethod> createMethodMap(JDefinedClass jVisitorModel,
                                                        ValueVisitorTypeParameters typeParameters) throws
                                                                                                          SourceCodeValidationException {
        Map<String, JMethod> methods = new TreeMap<String, JMethod>();
        for (JMethod method: jVisitorModel.methods()) {
            if (!typeParameters.isResult(method.type())) {
                throw new SourceCodeValidationException(MessageFormat.format("Visitor methods are only allowed to return type declared as a result type of visitor: {0}: expecting {1}, found: {2}",
                                                                         method.name(),
                                                                         typeParameters.getResultTypeParameter(),
                                                                         method.type()));
            }

            Collection<AbstractJClass> exceptions = method.getThrows();
            if (exceptions.size() > 1)
                throw new SourceCodeValidationException(MessageFormat.format("Visitor methods are allowed to throw no exceptions or throw single exception, declared as type-variable: {0}",
                                                                         method.name()));
            else if (exceptions.size() == 1) {
                AbstractJClass exception = exceptions.iterator().next();
                if (!typeParameters.isException(exception))
                    throw new SourceCodeValidationException(MessageFormat.format("Visitor methods throws exception, not declared as type-variable: {0}: {1}",
                                                                             method.name(), exception));
            }

            JMethod exitingValue = methods.put(method.name(), method);
            if (exitingValue != null) {
                throw new SourceCodeValidationException(MessageFormat.format("Method overloading is not supported for visitor interfaces: two methods with the same name: {0}",
                                                                         method.name()));
            }
        }
        return methods;
    }

    private final AbstractJClass visitorInterfaceModel;
    private final ValueVisitorTypeParameters typeParameters;
    private final Map<String, JMethod> methods;
    private final APICustomization apiCustomization;

    private ValueVisitorInterfaceModel(AbstractJClass visitorInterfaceModel, ValueVisitorTypeParameters typeParameters, Map<String, JMethod> methods, APICustomization apiCustomization) {
        this.visitorInterfaceModel = visitorInterfaceModel;
        this.typeParameters = typeParameters;
        this.methods = methods;
        this.apiCustomization = apiCustomization;
    }

    public JTypeVar getResultTypeParameter() {
        return typeParameters.getResultTypeParameter();
    }

    @Nullable
    public JTypeVar getExceptionTypeParameter() {
        return typeParameters.getExceptionTypeParameter();
    }

    @Nullable
    public JTypeVar getSelfTypeParameter() {
        return typeParameters.getSelfTypeParameter();
    }

    public List<JTypeVar> getValueTypeParameters() {
        return typeParameters.getValueTypeParameters();
    }

    public AbstractJClass narrowed(AbstractJClass usedDataType, AbstractJClass resultType, AbstractJClass exceptionType) {
        return narrowed(usedDataType, resultType, exceptionType, usedDataType);
    }

    private AbstractJClass narrowed(AbstractJClass usedDataType, AbstractJClass resultType, AbstractJClass exceptionType, AbstractJClass selfType) {
        AbstractJClass result = visitorInterfaceModel;
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            result = result.narrow(typeParameters.substituteSpecialType(typeVariable, selfType, resultType, exceptionType));
        }
        return result;
    }

    public Collection<JMethod> methods() {
        return methods.values();
    }

    public AbstractJType narrowType(AbstractJType typeVariable, AbstractJClass usedDataType, AbstractJClass resultType, AbstractJClass exceptionType) {
        return narrowType(typeVariable, usedDataType, resultType, exceptionType, usedDataType);
    }

    public AbstractJType narrowType(AbstractJType typeVariable, AbstractJClass usedDataType, AbstractJClass resultType, AbstractJClass exceptionType, AbstractJClass selfType) {
        typeVariable = typeParameters.substituteSpecialType(typeVariable, selfType, resultType, exceptionType);
        List<? extends AbstractJClass> dataTypeArguments = usedDataType.getTypeParameters();
        for (int i = 0; i < visitorInterfaceModel.typeParams().length; i++) {
            JTypeVar typeParameter = visitorInterfaceModel.typeParams()[i];
            if (typeVariable == typeParameter)
                return dataTypeArguments.get(i);
        }
        if (!(typeVariable instanceof AbstractJClass)) {
            return typeVariable;
        } else {
            AbstractJClass narrowedType = (AbstractJClass)typeVariable;
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
        return apiCustomization.acceptMethodName();
    }

    public MemberAccess factoryMethodAccessLevel() {
        return apiCustomization.isValueClassPublic() ? MemberAccess.PUBLIC : MemberAccess.PACKAGE;
    }

    public MemberAccess acceptMethodAccessLevel() {
        return apiCustomization.acceptMethodAccessLevel();
    }
}
