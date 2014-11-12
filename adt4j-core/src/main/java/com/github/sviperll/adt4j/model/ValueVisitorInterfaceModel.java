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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JTypeVar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

class ValueVisitorInterfaceModel {
    private final AbstractJClass visitorInterfaceModel;
    private final ValueVisitorTypeParameters typeParameters;
    private final Map<String, JMethod> methods;

    ValueVisitorInterfaceModel(AbstractJClass visitorInterfaceModel, ValueVisitorTypeParameters typeParameters, Map<String, JMethod> methods) {
        this.visitorInterfaceModel = visitorInterfaceModel;
        this.typeParameters = typeParameters;
        this.methods = methods;
    }

    JTypeVar getResultTypeParameter() {
        return typeParameters.getResultTypeParameter();
    }

    @Nullable
    JTypeVar getExceptionTypeParameter() {
        return typeParameters.getExceptionTypeParameter();
    }

    @Nullable
    JTypeVar getSelfTypeParameter() {
        return typeParameters.getSelfTypeParameter();
    }

    List<JTypeVar> getValueTypeParameters() {
        return typeParameters.getValueTypeParameters();
    }

    AbstractJClass narrowed(AbstractJClass usedDataType, AbstractJClass resultType, AbstractJClass exceptionType) {
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

    Collection<JMethod> methods() {
        return methods.values();
    }

    AbstractJType substituteSpecialType(AbstractJType typeVariable, AbstractJClass selfType, AbstractJClass resultType, AbstractJClass exceptionType) {
        return typeParameters.substituteSpecialType(typeVariable, selfType, resultType, exceptionType);
    }

    boolean isSelf(AbstractJType type) {
        return typeParameters.isSelf(type);
    }

    boolean isResult(AbstractJType type) {
        return typeParameters.isResult(type);
    }

    boolean isException(AbstractJType type) {
        return typeParameters.isException(type);
    }
}
