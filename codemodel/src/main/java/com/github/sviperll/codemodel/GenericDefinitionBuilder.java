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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <B>
 */
@ParametersAreNonnullByDefault
public abstract class GenericDefinitionBuilder<B extends ResidenceBuilder> implements SettledBuilder<B>, Model {
    private final List<TypeParameter> typeParameters = new ArrayList<>();
    private final List<Type> typeParametersAsInternalTypeArguments = new ArrayList<>();
    private final Map<String, TypeParameter> typeParametersMap = new TreeMap<>();
    private final B residence;

    GenericDefinitionBuilder(B residence) {
        this.residence = residence;
    }

    public abstract GenericDefinition<?, ?> definition();

    public TypeParameterBuilder typeParameter(String name) throws CodeModelException {
        if (typeParametersMap.containsKey(name)) {
            throw new CodeModelException(name + " type-parameter already defined");
        }
        TypeParameterBuilder result = new TypeParameterBuilder(definition(), name);
        typeParametersMap.put(name, result.declaration());
        typeParameters.add(result.declaration());
        typeParametersAsInternalTypeArguments.add(Type.variable(name));
        return result;
    }

    @Override
    final public B residence() {
        return residence;
    }

    @Override
    final public CodeModel getCodeModel() {
        return residence.getCodeModel();
    }

    TypeParameters createTypeParameters() {
        return new BuiltTypeParameters();
    }

    private class BuiltTypeParameters extends TypeParameters {

        @Override
        public final List<TypeParameter> all() {
            return typeParameters;
        }

        @Override
        public final TypeParameter get(String name) {
            TypeParameter result = typeParametersMap.get(name);
            if (result != null)
                return result;
            else {
                if (residence.residence().contextDefinition() == null)
                    return null;
                else {
                    return residence.residence().contextDefinition().typeParameters().get(name);
                }
            }
        }

        @Override
        final List<Type> asInternalTypeArguments() {
            return typeParametersAsInternalTypeArguments;
        }
    }
}
