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

import com.github.sviperll.codemold.util.CMCollections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <B>
 * @param <T>
 * @param <D>
 */
@ParametersAreNonnullByDefault
public abstract class GenericDefinitionBuilder<B extends ResidenceProvider, T extends GenericType<T, D>, D extends GenericDefinition<T, D>>
        implements Model {

    private final List<TypeParameter> typeParameters = CMCollections.newArrayList();
    private final Map<String, TypeParameter> typeParametersMap = new TreeMap<>();
    private final B residence;
    private D definition = null;

    GenericDefinitionBuilder(B residence) {
        this.residence = residence;
    }

    @Override
    public final CodeMold getCodeMold() {
        return residence.residence().getCodeMold();
    }

    @Nonnull
    protected D definition() {
        if (definition == null)
            definition = createDefinition(new BuiltTypeParameters());
        return definition;
    }

    @Nonnull
    abstract D createDefinition(TypeParameters typeParameters);

    @Nonnull
    protected TypeParameterBuilder typeParameter(String name) throws CodeMoldException {
        if (typeParametersMap.containsKey(name)) {
            throw new CodeMoldException(name + " type-parameter already defined");
        }
        TypeParameterBuilder result = new TypeParameterBuilder(definition(), name);
        typeParametersMap.put(name, result.declaration());
        typeParameters.add(result.declaration());
        return result;
    }

    @Nonnull
    protected B residence() {
        return residence;
    }

    private class BuiltTypeParameters extends TypeParameters {

        @Nonnull
        @Override
        public final List<? extends TypeParameter> all() {
            return typeParameters;
        }

        @Nonnull
        @Override
        public final Optional<TypeParameter>get(String name) {
            return Optional.ofNullable(typeParametersMap.get(name))
                    .map(Optional::of)
                    .orElseGet(() -> {
                        Residence builtResidence = residence();
                        if (!builtResidence.hasContextDefintion())
                            return Optional.empty();
                        else
                            return builtResidence.getContextDefinition().typeParameters().get(name);
                    });
        }

        @Nonnull
        @Override
        final Residence residence() {
            return residence.residence();
        }
    }
}
