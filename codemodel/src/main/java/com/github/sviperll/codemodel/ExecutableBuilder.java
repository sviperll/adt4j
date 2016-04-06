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
import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class ExecutableBuilder extends GenericDefinitionBuilder<NestingBuilder> {
    private final VariableScope scope = VariableScope.createTopLevel();
    private final BlockBuilder body = BlockBuilder.createWithBracesForced(scope.createNested());
    private final List<VariableDeclaration> parameters = new ArrayList<>();
    private final List<Type> throwsList = new ArrayList<>();
    private final NestingBuilder residence;

    ExecutableBuilder(NestingBuilder residence) {
        super(residence);
        this.residence = residence;
        
    }

    @Override
    public abstract ExecutableDefinition<? extends ExecutableType<?, ?>> definition();

    public void addParameter(Type type, String name) throws CodeModelException {
        name = scope.makeIntroducable(name);
        scope.introduce(name);
        Parameter parameter = new Parameter(false, type, name);
        parameters.add(parameter);
    }

    public void addFinalParameter(Type type, String name) throws CodeModelException {
        name = scope.makeIntroducable(name);
        scope.introduce(name);
        Parameter parameter = new Parameter(true, type, name);
        parameters.add(parameter);
    }

    public void throwsException(Type type) throws CodeModelException {
        if (!type.isObjectType())
            throw new CodeModelException("Only object type can be an exception");
        if (type.getObjectDetails().definition().isGeneric())
            throw new CodeModelException("Generic class can't be used as throwable exception");
        throwsList.add(type);
    }

    public BlockBuilder body() {
        return body;
    }

    ExecutableDefinition.Implementation implementExecutableDefinition() {
        return new BuiltExecutableDefinition(createTypeParameters());
    }

    <T extends Generic, D extends ExecutableDefinition<T>> ExecutableType.Implementation<T, D> createExecutableTypeImplementation(GenericType.Implementation<T, D> genericTypeImplementation) {
        return new BuiltExecutableType<>(genericTypeImplementation);
    }

    private class BuiltExecutableDefinition implements ExecutableDefinition.Implementation {

        final TypeParameters typeParameters;
        BuiltExecutableDefinition(TypeParameters typeParameters) {
            this.typeParameters = typeParameters;
        }
        @Override
        public final List<VariableDeclaration> parameters() {
            return Collections.unmodifiableList(parameters);
        }

        @Override
        public final List<Type> throwsList() {
            return Collections.unmodifiableList(throwsList);
        }

        @Override
        public final Renderable body() {
            return body;
        }

        @Override
        public final Residence residence() {
            return residence.residence();
        }

        @Override
        public final CodeModel getCodeModel() {
            return residence.getCodeModel();
        }

        @Override
        public TypeParameters typeParameters() {
            return typeParameters;
        }

    }

    private class BuiltExecutableType<T extends Generic, D extends ExecutableDefinition<T>>
            implements ExecutableType.Implementation<T, D> {

        private final GenericType.Implementation<T, D> genericTypeImplementation;
        BuiltExecutableType(GenericType.Implementation<T, D> genericTypeImplementation) {
            this.genericTypeImplementation = genericTypeImplementation;
        }

        @Override
        public List<VariableDeclaration> parameters(ExecutableType<T, D> instance) {
            List<VariableDeclaration> result = new ArrayList<>();
            for (VariableDeclaration declaration: definition().parameters()) {
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

    private static class Parameter extends VariableDeclaration {

        private final boolean isFinal;
        private final Type type;
        private final String name;

        public Parameter(boolean isFinal, Type type, String name) throws CodeModelException {
            if (type.isVoid())
                throw new CodeModelException("void is not allowed here");
            this.isFinal = isFinal;
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean isFinal() {
            return isFinal;
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean isInitialized() {
            return false;
        }

        @Override
        Expression getInitialValue() {
            throw new UnsupportedOperationException();
        }
    }

}
