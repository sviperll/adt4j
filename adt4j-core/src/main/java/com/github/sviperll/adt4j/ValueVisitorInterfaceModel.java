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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFormatter;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JTypeVar;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class ValueVisitorInterfaceModel {
    private static final String VISITOR_SUFFIX = "Visitor";
    private static final String VALUE_SUFFIX = "Value";

    private final JDefinedClass visitorInterfaceModel;
    private final GenerateValueClassForVisitor annotationInstance;

    ValueVisitorInterfaceModel(JDefinedClass visitorInterfaceModel, GenerateValueClassForVisitor dataVisitor) {
        this.visitorInterfaceModel = visitorInterfaceModel;
        this.annotationInstance = dataVisitor;
    }

    String getPackageName() {
        return visitorInterfaceModel._package().name();
    }

    String getValueClassName() {
        if (!annotationInstance.valueClassName().equals(":auto")) {
            return annotationInstance.valueClassName();
        } else {
            String visitorName = visitorInterfaceModel.name();
            String valueName;
            if (visitorName.endsWith(VISITOR_SUFFIX))
                valueName = visitorName.substring(0, visitorName.length() - VISITOR_SUFFIX.length());
            else
                valueName = visitorName + VALUE_SUFFIX;
            return valueName;
        }
    }

    boolean generatesPublicClass() {
        return annotationInstance.valueClassIsPublic();
    }

    int hashCodeBase() {
        return annotationInstance.valueClassHashCodeBase();
    }

    Collection<JTypeVar> getDataTypeParameters() {
        List<JTypeVar> result = new ArrayList<JTypeVar>();
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (!shouldBeOverridenOnInvocation(typeVariable.name()) && !isSelf(typeVariable))
                result.add(typeVariable);
        }
        return result;
    }

    private boolean shouldBeOverridenOnInvocation(String name) {
        return name.equals(annotationInstance.resultVariableName()) || name.equals(annotationInstance.exceptionVariableName());
    }

    boolean isSelf(AbstractJType type) {
        return type.fullName().equals(annotationInstance.selfReferenceVariableName());
    }

    JTypeVar getResultTypeParameter() {
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeVariable.name().equals(annotationInstance.resultVariableName()))
                return typeVariable;
        }
        return null;
    }

    JTypeVar getExceptionTypeParameter() {
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeVariable.name().equals(annotationInstance.exceptionVariableName()))
                return typeVariable;
        }
        return null;
    }

    private JTypeVar getSelfTypeParameter() {
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (isSelf(typeVariable))
                return typeVariable;
        }
        return null;
    }

    AbstractJClass narrowed(AbstractJClass usedDataType, AbstractJType resultType, AbstractJType exceptionType) {
        return narrowed(usedDataType, resultType, exceptionType, usedDataType);
    }

    AbstractJClass narrowed(AbstractJClass usedDataType, AbstractJType resultType, AbstractJType exceptionType, AbstractJType selfType) {
        Iterator<? extends AbstractJClass> dataTypeArgumentIterator = usedDataType.getTypeParameters().iterator();
        AbstractJClass result = visitorInterfaceModel;
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeVariable.name().equals(annotationInstance.exceptionVariableName()))
                result = result.narrow(exceptionType);
            else if (typeVariable.name().equals(annotationInstance.resultVariableName()))
                result = result.narrow(resultType);
            else if (isSelf(typeVariable))
                result = result.narrow(selfType);
            else {
                result = result.narrow(dataTypeArgumentIterator.next());
            }
        }
        return result;
    }

    Collection<JMethod> methods() {
        return visitorInterfaceModel.methods();
    }

    @Override
    public String toString() {
        StringWriter sb = new StringWriter();
        visitorInterfaceModel.generate(new JFormatter(sb));
        return sb.toString();
    }

    AbstractJType substituteTypeParameter(AbstractJType type, AbstractJClass usedDataType, AbstractJType resultType, AbstractJType exceptionType) {
        if (type.fullName().equals(annotationInstance.exceptionVariableName()))
            return exceptionType;
        else if (type.fullName().equals(annotationInstance.resultVariableName()))
            return resultType;
        else if (isSelf(type))
            return usedDataType;
        else
            return type;
    }

    boolean hasSelfTypeParameter() {
        return getSelfTypeParameter() != null;
    }

    boolean shouldBeSerializable() {
        return annotationInstance.valueClassIsSerializable();
    }
}
