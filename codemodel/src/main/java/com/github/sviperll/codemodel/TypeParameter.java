/*
 * Copyright (c) 2016, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
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

package com.github.sviperll.codemodel;

import java.text.MessageFormat;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class TypeParameter {
    TypeParameter() {
    }

    @Nonnull
    public abstract String name();

    /**
     * Return bound or bounds of this type-wrapVariableType as single type.
     *
     * If there are several bounds then IntersectionType type is returned.
     * 
     * @return bound or bounds of this type-variable as single type.
     */
    @Nonnull
    public abstract AnyType bound();

    @Nonnull
    public abstract GenericDefinition<?, ?> declaredIn();

    @Nonnull
    final AnyType lowerRawBound() throws CodeModelException {
        TypeParameters environment = declaredIn().typeParameters().preventCycle(name());
        AnyType bound = bound();
        if (bound.isTypeVariable()) {
            TypeParameter typeParameter = environment.getOrDefault(bound.getVariableDetails().name(), null);
            if (typeParameter != null)
                return typeParameter.lowerRawBound();
            else
                throw new IllegalStateException(
                        MessageFormat.format(
                                "Type parameter {0} is bound by unknown type-variable: {1}",
                                name(),
                                bound.getVariableDetails().name()));
        } else {
            ObjectType lower = null;
            for (AnyType type: bound.toListOfIntersectedTypes()) {
                ObjectType object = type.getObjectDetails();
                if (lower == null || lower.definition().extendsOrImplements(object.definition()))
                    lower = object;
            }
            if (lower == null)
                throw new CodeModelException("Empty bounds found for variable");
            return lower.asAny();
        }
    }
}
