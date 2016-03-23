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
public class GenericsConfigBuilder {
    static GenericsConfigBuilder objectDefinition(ObjectDefinition definition) {
        return new GenericsConfigBuilder(new ObjectBasicGenericsConfig(definition));
    }
    static GenericsConfigBuilder methodDefinition(ExecutableDefinition definition) {
        return new GenericsConfigBuilder(new MethodBasicGenericsConfig(definition));
    }
    private final GenericsConfig genericsConfig;
    private final List<TypeParameter> typeParameters = new ArrayList<>();
    private final List<Type> typeParametersAsInternalTypeArguments = new ArrayList<>();
    private final Map<String, TypeParameter> typeParametersMap = new TreeMap<>();

    private GenericsConfigBuilder(BasicGenericsConfig basicConfig) {
        this.genericsConfig = new BuiltGenericsConfig(basicConfig);
    }

    public TypeParameterBuilder typeParameter(String name) throws CodeModelException {
        if (typeParametersMap.containsKey(name)) {
            throw new CodeModelException(name + " type-parameter already defined");
        }
        TypeParameterBuilder result = new TypeParameterBuilder(genericsConfig, name);
        typeParametersMap.put(name, result.declaration());
        typeParameters.add(result.declaration());
        typeParametersAsInternalTypeArguments.add(Type.variable(name));
        return result;
    }

    GenericsConfig generics() {
        return genericsConfig;
    }

    private class BuiltGenericsConfig extends GenericsConfig implements BasicGenericsConfig {

        private final BasicGenericsConfig basic;
        BuiltGenericsConfig(BasicGenericsConfig basic) {
            this.basic = basic;
        }
        @Override
        public GenericsConfig parent() {
            return basic.parent();
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

        @Override
        public CodeModel getCodeModel() {
            return basic.getCodeModel();
        }

        @Override
        public boolean isMethod() {
            return basic.isMethod();
        }

        @Override
        public boolean isObject() {
            return basic.isObject();
        }

        @Override
        public ExecutableDefinition getMethod() {
            return basic.getMethod();
        }

        @Override
        public ObjectDefinition getObject() {
            return basic.getObject();
        }

        @Override
        List<Type> typeParametersAsInternalTypeArguments() {
            return typeParametersAsInternalTypeArguments;
        }
    }

    private interface BasicGenericsConfig {
        public GenericsConfig parent();
        public CodeModel getCodeModel();
        public boolean isMethod();
        public boolean isObject();
        public ExecutableDefinition getMethod();
        public ObjectDefinition getObject();
    }

    private static class MethodBasicGenericsConfig implements BasicGenericsConfig {

        private final ExecutableDefinition definition;
        MethodBasicGenericsConfig(ExecutableDefinition definition) {
            this.definition = definition;
        }

        @Override
        public GenericsConfig parent() {
            return definition.residence().getNesting().isStatic() ? null : definition.residence().getNesting().parent().generics();
        }

        @Override
        public CodeModel getCodeModel() {
            return definition.getCodeModel();
        }

        @Override
        public boolean isMethod() {
            return true;
        }

        @Override
        public boolean isObject() {
            return false;
        }

        @Override
        public ExecutableDefinition getMethod() {
            return definition;
        }

        @Override
        public ObjectDefinition getObject() {
            throw new UnsupportedOperationException();
        }
    }
    private static class ObjectBasicGenericsConfig implements BasicGenericsConfig {

        private final ObjectDefinition definition;
        ObjectBasicGenericsConfig(ObjectDefinition definition) {
            this.definition = definition;
        }

        @Override
        public GenericsConfig parent() {
            if (definition.residence().isPackageLevel())
                return null;
            else {
                Nesting residence = definition.residence().getNesting();
                return residence.isStatic() ? null : residence.parent().generics();
            }
        }

        @Override
        public CodeModel getCodeModel() {
            return definition.getCodeModel();
        }

        @Override
        public boolean isMethod() {
            return false;
        }

        @Override
        public boolean isObject() {
            return true;
        }

        @Override
        public ExecutableDefinition getMethod() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectDefinition getObject() {
            return definition;
        }
    }
}
