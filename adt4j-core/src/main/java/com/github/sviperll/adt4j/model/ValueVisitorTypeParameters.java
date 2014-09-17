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
import com.helger.jcodemodel.JTypeVar;
import java.util.List;
import javax.annotation.Nullable;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class ValueVisitorTypeParameters {
    private final JTypeVar resultTypeParameter;
    private final @Nullable JTypeVar exceptionTypeParameter;
    private final @Nullable JTypeVar selfTypeParameter;
    private final List<JTypeVar> valueTypeParameters;

    ValueVisitorTypeParameters(JTypeVar resultTypeParameter,
                                      @Nullable JTypeVar exceptionTypeParameter,
                                      @Nullable JTypeVar selfTypeParameter,
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

    boolean isSpecial(AbstractJType typeVariable) {
        return typeVariable != null
               && (isSelf(typeVariable)
                   || isResult(typeVariable)
                   || isException(typeVariable));
    }

    AbstractJType substituteSpecialType(AbstractJType typeVariable, AbstractJClass selfType, AbstractJType resultType,
                                        AbstractJType exceptionType) {
        if (isException(typeVariable))
            return exceptionType;
        else if (isResult(typeVariable))
            return resultType;
        else if (isSelf(typeVariable))
            return selfType;
        else {
            return typeVariable;
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
