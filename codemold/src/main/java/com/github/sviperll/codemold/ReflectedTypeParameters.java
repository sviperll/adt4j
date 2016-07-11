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

package com.github.sviperll.codemold;

import com.github.sviperll.codemold.util.Collections2;
import com.github.sviperll.codemold.util.Snapshot;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class ReflectedTypeParameters<T extends java.lang.reflect.GenericDeclaration> extends TypeParameters {

    private List<? extends TypeParameter> allTypeParameters = null;
    private final GenericDefinition<?, ?> definition;
    private final TypeVariable<T>[] reflectedTypeParameters;

    ReflectedTypeParameters(GenericDefinition<?, ?> definition, TypeVariable<T>[] reflectedTypeParameters) {
        this.definition = definition;
        this.reflectedTypeParameters = reflectedTypeParameters;
    }

    @Override
    public List<? extends TypeParameter> all() {
        if (allTypeParameters == null) {
            List<TypeParameter> allTypeParametersBuilder = Collections2.newArrayList();
            for (final TypeVariable<T> reflectedTypeParameter : reflectedTypeParameters) {
                TypeParameter parameter = new ReflectedTypeParameter<>(definition, reflectedTypeParameter);
                allTypeParametersBuilder.add(parameter);
            }
            allTypeParameters = Snapshot.of(allTypeParametersBuilder);
        }
        return Snapshot.of(allTypeParameters);
    }

    @Override
    public Residence residence() {
        return definition.residence();
    }

}
