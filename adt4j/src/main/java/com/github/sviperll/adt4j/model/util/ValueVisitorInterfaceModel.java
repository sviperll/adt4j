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

import com.github.sviperll.adt4j.GenerateValueClassForVisitor;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTypeVar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;

public class ValueVisitorInterfaceModel {
    public static ValueVisitorInterfaceModel createInstance(JDefinedClass jVisitorModel, GenerateValueClassForVisitor annotation) throws SourceException {
        ValueVisitorTypeParameters typeParameters = createValueVisitorTypeParameters(jVisitorModel, annotation);
        Map<String, JMethod> methods = createMethodMap(jVisitorModel, typeParameters);
        return new ValueVisitorInterfaceModel(jVisitorModel, typeParameters, methods, annotation.acceptMethodName());
    }

    private static ValueVisitorTypeParameters createValueVisitorTypeParameters(JDefinedClass jVisitorModel,
                                                                               GenerateValueClassForVisitor annotation)
            throws SourceException {
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
            throw new SourceException("Result type-variable is not found for " + jVisitorModel + " visitor, expecting: " + annotation.resultVariableName());
        }
        if (exceptionType == null && !annotation.exceptionVariableName().equals(":none")) {
            throw new SourceException("Exception type-variable is not found for " + jVisitorModel + " visitor, expecting: " + annotation.exceptionVariableName());
        }
        if (selfType == null && !annotation.selfReferenceVariableName().equals(":none")) {
            throw new SourceException("Self reference type-variable is not found for " + jVisitorModel + " visitor, expecting: " + annotation.selfReferenceVariableName());
        }
        return new ValueVisitorTypeParameters(resultType, exceptionType, selfType, valueClassTypeParameters);
    }

    private static Map<String, JMethod> createMethodMap(JDefinedClass jVisitorModel,
                                                        ValueVisitorTypeParameters typeParameters) throws
                                                                                                          SourceException {
        Map<String, JMethod> methods = new TreeMap<String, JMethod>();
        for (JMethod method: jVisitorModel.methods()) {
            if (!typeParameters.isResult(method.type())) {
                throw new SourceException("Visitor methods are only allowed to return type"
                                          + " declared as a result type of visitor: " + method.name()
                                          + ": expecting " + typeParameters.getResultTypeParameter()
                                          + ", found: " + method.type());
            }

            Collection<AbstractJClass> exceptions = method.getThrows();
            if (exceptions.size() > 1)
                throw new SourceException("Visitor methods are allowed to throw no exceptions or throw single exception, declared as type-variable: " + method.name());
            else if (exceptions.size() == 1) {
                AbstractJClass exception = exceptions.iterator().next();
                if (!typeParameters.isException(exception))
                    throw new SourceException("Visitor methods throws exception, not declared as type-variable: " + method.name() + ": " + exception);
            }

            JMethod exitingValue = methods.put(method.name(), method);
            if (exitingValue != null) {
                throw new SourceException("Method overloading is not supported for visitor interfaces: two methods with the same name: " + method.name());
            }
        }
        return methods;
    }

    private final AbstractJClass visitorInterfaceModel;
    private final ValueVisitorTypeParameters typeParameters;
    private final Map<String, JMethod> methods;
    private final String acceptMethodName;

    private ValueVisitorInterfaceModel(AbstractJClass visitorInterfaceModel, ValueVisitorTypeParameters typeParameters, Map<String, JMethod> methods, String acceptMethodName) {
        this.visitorInterfaceModel = visitorInterfaceModel;
        this.typeParameters = typeParameters;
        this.methods = methods;
        this.acceptMethodName = acceptMethodName;
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
        Iterator<? extends AbstractJClass> dataTypeArgumentIterator = usedDataType.getTypeParameters().iterator();
        AbstractJClass result = visitorInterfaceModel;
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeParameters.isSpecial(typeVariable))
                result = result.narrow(typeParameters.substituteSpecialType(typeVariable, usedDataType, resultType, exceptionType));
            else
                result = result.narrow(dataTypeArgumentIterator.next());
        }
        return result;
    }

    public Collection<JMethod> methods() {
        return methods.values();
    }

    public AbstractJType substituteSpecialType(AbstractJType typeVariable, AbstractJClass selfType, AbstractJClass resultType, AbstractJClass exceptionType) {
        return typeParameters.substituteSpecialType(typeVariable, selfType, resultType, exceptionType);
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
        return acceptMethodName;
    }

    public int factoryMethodsModifiers() {
        return JMod.PUBLIC;
    }

    public int acceptMethodModifiers() {
        return JMod.PUBLIC;
    }
}
