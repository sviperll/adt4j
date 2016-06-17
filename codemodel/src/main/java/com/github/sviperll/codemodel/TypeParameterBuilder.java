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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class TypeParameterBuilder {
    private final BuiltTypeParameter declaration = new BuiltTypeParameter();
    private final GenericDefinition<?, ?> declaredIn;
    private final String name;
    private final List<ObjectType> bounds = new ArrayList<>();
    private Type effectiveBound;

    TypeParameterBuilder(GenericDefinition<?, ?> declaredIn, String name) {
        this.declaredIn = declaredIn;
        this.name = name;
        effectiveBound = declaredIn.getCodeModel().objectType().asType();
    }

    @Nonnull
    TypeParameter declaration() {
        return declaration;
    }

    public void addAllBounds(Collection<? extends Type> bounds) {
        for (Type type: bounds) {
            addBound(type);
        }
    }
    public void addBound(Type bound) {
        if (!(bound.canBeTypeVariableBound()))
            throw new IllegalArgumentException(bound.kind() + " can't be used as type-variable");
        if (effectiveBound != null && (effectiveBound.isTypeVariable() || bound.isTypeVariable()))
            throw new IllegalArgumentException("Mixing type-variables with other bounds is not allowed");
        if (bound.isIntersection()) {
            addAllBounds(bound.toListOfIntersectedTypes());
        } else {
            if (bound.isObjectType())
                bounds.add(bound.getObjectDetails());
            if (effectiveBound == null) {
                effectiveBound = bound;
            } else {
                effectiveBound = Type.intersection(bounds).asType();
            }
        }
    }
    private class BuiltTypeParameter extends TypeParameter {

        @Override
        public String name() {
            return name;
        }

        @Override
        public Type bound() {
            return effectiveBound;
        }

        @Override
        public GenericDefinition<?, ?> declaredIn() {
            return declaredIn;
        }
    }
}
