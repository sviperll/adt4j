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
 * @param <T>
 */
@ParametersAreNonnullByDefault
public class GenericsConfigBuilder<T extends Residence> {
    private final BuiltTypeParameterEnvironment typeParameterEnvironment = new BuiltTypeParameterEnvironment();
    private final GenericDefinition<?> declaredIn;
    private final List<TypeParameter> typeParameters = new ArrayList<>();
    private final Map<String, TypeParameter> typeParametersMap = new TreeMap<>();

    GenericsConfigBuilder(GenericDefinition<?> declaredIn) {
        this.declaredIn = declaredIn;
    }

    public TypeParameterBuilder typeParameter(String name) throws CodeModelException {
        if (typeParametersMap.containsKey(name)) {
            throw new CodeModelException(declaredIn + ": " + name + " type-parameter already defined");
        }
        TypeParameterBuilder result = new TypeParameterBuilder(declaredIn, name);
        typeParametersMap.put(name, result.declaration());
        typeParameters.add(result.declaration());
        return result;
    }

    GenericsConfig generics() {
        return typeParameterEnvironment;
    }

    private class BuiltTypeParameterEnvironment extends GenericsConfig {
        @Override
        public GenericsConfig parent() {
            Residence residence = declaredIn.residence();
            if (residence.isPackageLevel())
                return null;
            else {
                NestedResidence nesting = residence.asNested();
                return nesting.isStatic() ? null : nesting.parent().generics();
            }
        }

        @Override
        public List<TypeParameter> typeParameters() {
            return typeParameters;
        }

        @Override
        public TypeParameter get(String name) {
            TypeParameter result = typeParametersMap.get(name);
            if (result != null)
                return result;
            else {
                GenericsConfig parent = parent();
                if (parent == null)
                    return null;
                else {
                    return parent.get(name);
                }
            }
        }

    }



}
