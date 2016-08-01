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

import java.text.MessageFormat;
import java.util.Arrays;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class MethodBuilder extends CallableDefinitionBuilder {
    private boolean isFinal;
    private boolean isAbstract;

    MethodBuilder(NestingBuilder residence, String name) {
        super(residence, name);
    }

    @Override
    public TypeParameterBuilder typeParameter(String name) throws CodeMoldException {
        return super.typeParameter(name);
    }

    @Override
    public VariableDeclaration addParameter(Type type, String name) throws CodeMoldException {
        return super.addParameter(type, name);
    }

    @Override
    public VariableDeclaration addFinalParameter(Type type, String name) throws CodeMoldException {
        return super.addFinalParameter(type, name);
    }

    @Override
    public void throwsException(ObjectType type) throws CodeMoldException {
        super.throwsException(type);
    }

    @Override
    public void throwsException(TypeVariable typeVariable) throws CodeMoldException {
        super.throwsException(typeVariable);
    }

    @Override
    public BlockBuilder body() {
        return super.body();
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
        isAbstract = isAbstract && !isFinal;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        isFinal = isFinal && !isAbstract;
    }

    public void annotateOverride() {
        annotate(Annotation.createInstance(getCodeMold().getReference(Override.class)));
    }

    @Override
    MethodDefinition createDefinition(ExecutableDefinition.Implementation<MethodType, MethodDefinition> implementation) {
        return new BuiltDefinition(implementation);
    }

    private class BuiltDefinition extends CallableDefinitionBuilder.BuiltDefinition {
        BuiltDefinition(ExecutableDefinition.Implementation<MethodType, MethodDefinition> implementation) {
            super(implementation);
        }

        @Override
        public boolean isFinal() {
            return isFinal;
        }

        @Override
        public boolean isAbstract() {
            return isAbstract;
        }

        @Override
        public boolean hasDefaultValue() {
            return false;
        }

        @Override
        public AnyCompileTimeValue defaultValue() {
            throw new UnsupportedOperationException("Method has no default value. Use hasDefaultValue to check");
        }
    }
}
