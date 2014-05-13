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

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JTypeVar;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ValueVisitorInterfaceModel {
    private static final String VISITOR_SUFFIX = "Visitor";
    private static final String VALUE_SUFFIX = "Value";

    private final JDefinedClass visitorInterfaceModel;
    private final ValueVisitor dataVisitor;

    ValueVisitorInterfaceModel(JDefinedClass visitorInterfaceModel, ValueVisitor dataVisitor) {
        this.visitorInterfaceModel = visitorInterfaceModel;
        this.dataVisitor = dataVisitor;
    }

    String getPackageName() {
        return visitorInterfaceModel._package().name();
    }

    String getValueClassName() {
        if (!dataVisitor.valueClassName().equals(":auto")) {
            return dataVisitor.valueClassName();
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
        return dataVisitor.valueClassIsPublic();
    }

    int hashCodeBase() {
        return dataVisitor.valueClassHashCodeBase();
    }

    Collection<JTypeVar> getDataTypeParameters() {
        List<JTypeVar> result = new ArrayList<>();
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (!shouldBeOverridenOnInvocation(typeVariable.name()) && !isSelf(typeVariable.name()))
                result.add(typeVariable);
        }
        return result;
    }

    private boolean shouldBeOverridenOnInvocation(String name) {
        return name.equals(dataVisitor.resultVariableName()) || name.equals(dataVisitor.exceptionVariableName());
    }

    private boolean isSelf(String name) {
        return name.equals(dataVisitor.selfReferenceVariableName());
    }

    JTypeVar getResultTypeParameter() {
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeVariable.name().equals(dataVisitor.resultVariableName()))
                return typeVariable;
        }
        return null;
    }

    JTypeVar getExceptionTypeParameter() {
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeVariable.name().equals(dataVisitor.exceptionVariableName()))
                return typeVariable;
        }
        return null;
    }

    private JTypeVar getSelfTypeParameter() {
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (isSelf(typeVariable.name()))
                return typeVariable;
        }
        return null;
    }

    JClass narrowed(JClass usedDataType, JType resultType, JType exceptionType) {
        return narrowed(usedDataType, resultType, exceptionType, usedDataType);
    }

    JClass narrowed(JClass usedDataType, JType resultType, JType exceptionType, JType selfType) {
        Iterator<JClass> dataTypeArgumentIterator = usedDataType.getTypeParameters().iterator();
        JClass result = visitorInterfaceModel;
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeVariable.name().equals(dataVisitor.exceptionVariableName()))
                result = result.narrow(exceptionType);
            else if (typeVariable.name().equals(dataVisitor.resultVariableName()))
                result = result.narrow(resultType);
            else if (typeVariable.name().equals(dataVisitor.selfReferenceVariableName()))
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

    JType substituteTypeParameter(JType type, JClass usedDataType, JType resultType, JType exceptionType) {
        if (type.name().equals(dataVisitor.exceptionVariableName()))
            return exceptionType;
        else if (type.name().equals(dataVisitor.resultVariableName()))
            return resultType;
        else if (type.name().equals(dataVisitor.selfReferenceVariableName()))
            return usedDataType;
        else
            return type;
    }

    boolean hasSelfTypeParameter() {
        return getSelfTypeParameter() != null;
    }
}
