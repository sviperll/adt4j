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

import com.github.sviperll.codemodel.render.Renderable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 * @param <D>
 */
public abstract class ExecutableDefinition<T extends ExecutableType<T, D>, D extends ExecutableDefinition<T, D>>
        extends GenericDefinition<T, D> {

    private final Implementation<T, D> implementation;
    ExecutableDefinition(Implementation<T, D> implementation) {
        this.implementation = implementation;
    }

    abstract T createType(ExecutableType.Implementation<T, D> implementation);

    @Override
    public final TypeParameters typeParameters() {
        return implementation.typeParameters(this);
    }

    public final List<VariableDeclaration> parameters() {
        return implementation.parameters();
    }

    public final List<Type> throwsList() {
        return implementation.throwsList();
    }

    final Renderable body() {
        return implementation.body();
    }

    @Override
    public final Residence residence() {
        return implementation.residence();
    }

    public boolean isStatic(){
        return residence().getNesting().isStatic();
    }

    public MemberAccess accessLevel(){
        return residence().getNesting().accessLevel();
    }

    public ObjectDefinition parent(){
        return residence().getNesting().parent();
    }

    @Override
    public final CodeModel getCodeModel() {
        return implementation.getCodeModel();
    }

    @Override
    final T createType(GenericType.Implementation<T, D> implementation) {
        return createType(new DefinedType(implementation));
    }

    interface Implementation<T extends ExecutableType<T, D>, D extends ExecutableDefinition<T, D>> {

        TypeParameters typeParameters(ExecutableDefinition<T, D> thisDefinition);

        List<VariableDeclaration> parameters();

        List<Type> throwsList();

        Renderable body();

        Residence residence();

        CodeModel getCodeModel();
    }

    private class DefinedType implements ExecutableType.Implementation<T, D> {

        private final GenericType.Implementation<T, D> genericTypeImplementation;
        DefinedType(GenericType.Implementation<T, D> genericTypeImplementation) {
            this.genericTypeImplementation = genericTypeImplementation;
        }

        @Override
        public List<VariableDeclaration> parameters(ExecutableType<T, D> instance) {
            List<VariableDeclaration> result = new ArrayList<>();
            for (VariableDeclaration declaration: instance.definition().parameters()) {
                result.add(declaration.substitute(instance.definitionEnvironment()));
            }
            return result;
        }

        @Override
        public List<Type> throwsList(ExecutableType<T, D> instance) {
            List<Type> result = new ArrayList<>();
            for (Type type: instance.definition().throwsList()) {
                result.add(type.substitute(instance.definitionEnvironment()));
            }
            return result;
        }

        @Override
        public GenericType.Implementation<T, D> genericTypeImplementation() {
            return genericTypeImplementation;
        }
    }


}
