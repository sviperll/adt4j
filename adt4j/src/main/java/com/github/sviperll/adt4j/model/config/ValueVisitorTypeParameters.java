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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JTypeWildcard;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class ValueVisitorTypeParameters {
    private final JTypeVar resultTypeParameter;
    private final JTypeVar exceptionTypeParameter;
    private final JTypeVar selfTypeParameter;
    private final List<JTypeVar> valueTypeParameters;

    ValueVisitorTypeParameters(JTypeVar resultTypeParameter,
                                      JTypeVar exceptionTypeParameter,
                                      JTypeVar selfTypeParameter,
                                      List<JTypeVar> valueTypeParameters) {
        this.resultTypeParameter = resultTypeParameter;
        this.exceptionTypeParameter = exceptionTypeParameter;
        this.selfTypeParameter = selfTypeParameter;
        this.valueTypeParameters = valueTypeParameters;
    }

    JTypeVar getResultTypeParameter() {
        return resultTypeParameter;
    }

    JTypeVar getExceptionTypeParameter() {
        return exceptionTypeParameter;
    }

    JTypeVar getSelfTypeParameter() {
        return selfTypeParameter;
    }

    List<JTypeVar> getValueTypeParameters() {
        return valueTypeParameters;
    }

    boolean isSpecial(AbstractJType type) {
        return type != null
               && (isSelf(type)
                   || isResult(type)
                   || isException(type));
    }

    /**
     * Substitutes special type-variables with provided types.
     * 
     * Substitution is deep and is performed through out type's type-variables.
     * <p>
     * For example {@code Tree<R>} is replaced with {@code Tree<String>}
     * when R is special result-type-variable and String is provided as
     * result-type.
     * 
     * @param type type to substitute
     * @param selfType special self-type to replace self-type-variables with
     * @param resultType special result-type to replace result-type-variables with
     * @param exceptionType special exception-type to replace exception-type-variables with
     * @return resulting substitution
     */
    AbstractJType substituteSpecialType(AbstractJType type,
                                        AbstractJClass selfType,
                                        AbstractJClass resultType,
                                        AbstractJClass exceptionType) {
        if (type instanceof AbstractJClass)
            return substituteSpecialType((AbstractJClass)type, selfType, resultType, exceptionType);
        else
            return type;
    }

    /**
     * Substitutes special type-variables with provided types.
     * 
     * Substitution is deep and is performed through out type's type-variables.
     * <p>
     * For example {@code Tree<R>} is replaced with {@code Tree<String>}
     * when R is special result-type-variable and String is provided as
     * result-type.
     * 
     * @param type type to substitute
     * @param selfType special self-type to replace self-type-variables with
     * @param resultType special result-type to replace result-type-variables with
     * @param exceptionType special exception-type to replace exception-type-variables with
     * @return resulting substitution
     */
    AbstractJClass substituteSpecialType(AbstractJClass type,
                                         AbstractJClass selfType,
                                         AbstractJClass resultType,
                                         AbstractJClass exceptionType) {
        if (isException(type))
            return exceptionType;
        else if (isResult(type))
            return resultType;
        else if (isSelf(type))
            return selfType;
        else {
            if (type.isArray()) {
                return substituteSpecialType(type.elementType(), selfType, resultType, exceptionType).array();
            } else if (type instanceof JTypeWildcard) {
                JTypeWildcard wildcard = (JTypeWildcard)type;
                AbstractJClass bound = substituteSpecialType(wildcard.bound(), selfType, resultType, exceptionType);
                return bound.wildcard(wildcard.boundMode());
            } else {
                List<AbstractJClass> typeArguments = new ArrayList<>();
                for (AbstractJClass originalArgument: type.getTypeParameters()) {
                    typeArguments.add(substituteSpecialType(originalArgument, selfType, resultType, exceptionType));
                }
                return typeArguments.isEmpty() ? type : type.erasure().narrow(typeArguments);
            }
        }
    }

    boolean isSelf(AbstractJType type) {
        return type == selfTypeParameter;
    }

    boolean isResult(AbstractJType type) {
        return type == resultTypeParameter;
    }

    boolean isException(AbstractJType type) {
        return type == exceptionTypeParameter;
    }
}
