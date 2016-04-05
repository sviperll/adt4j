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

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class MethodBuilder extends ExecutableBuilder {
    private final MethodDefinition definition = new BuiltDefinition(createExecutableDefinitionSubstance());
    private final String name;
    private final NestingBuilder residence;
    private boolean isFinal;
    private Type resultType = Type.voidType();
    private final TypeContainer typeContainer;

    MethodBuilder(NestingBuilder residence, String name) {
        super(residence);
        this.residence = residence;
        this.name = name;
        if (residence.residence().getNesting().isStatic()) {
            typeContainer = new TypeContainer(null);
        } else {
            typeContainer = null;
        }
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public void resultType(Type resultType) {
        this.resultType = resultType;
    }

    @Override
    public MethodDefinition definition() {
        return definition;
    }

    private class BuiltDefinition extends MethodDefinition {
        BuiltDefinition(ExecutableDefinitionSubstance executableDefinition) {
            super(executableDefinition);
        }

        @Override
        public boolean isConstructor() {
            return false;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean isFinal() {
            return isFinal;
        }

        @Override
        public Type returnType() {
            return resultType;
        }

        @Override
        public boolean isMethod() {
            return true;
        }

        @Override
        public MethodDefinition getMethodDetails() {
            return this;
        }

        @Override
        public final MethodType rawType() {
            if (residence.residence().getNesting().isStatic()) {
                return typeContainer.rawType;
            } else {
                throw new UnsupportedOperationException("Parent instance type is required");
            }
        }

        @Override
        public final MethodType rawType(GenericType<?, ?> parentInstanceType) {
            if (residence.residence().getNesting().isStatic()) {
                throw new UnsupportedOperationException("Type is static memeber, no parent is expected.");
            } else {
                TypeContainer typeContainer = new TypeContainer(parentInstanceType);
                return typeContainer.rawType;
            }
        }

        @Override
        public final MethodType internalType() {
            MethodType rawType;
            if (residence.residence().getNesting().isStatic()) {
                rawType = rawType();
            } else {
                rawType = rawType(residence.residence().getNesting().parent().internalType().getObjectDetails());
            }
            List<Type> internalTypeArguments = typeParameters().asInternalTypeArguments();
            try {
                return rawType.narrow(internalTypeArguments);
            } catch (CodeModelException ex) {
                throw new RuntimeException("No parameter-argument mismatch is guaranteed to ever happen", ex);
            }
        }
    }

    private class TypeContainer {
        private final GenericType<?, ?> parentInstanceType;
        private final ExecutableTypeSubstance executableSubstance;
        private final MethodType rawType = GenericType.createRawTypeDetails(new GenericType.Factory<MethodType, MethodDefinition>() {
            @Override
            public MethodType createGenericType(GenericType.Implementation<MethodType, MethodDefinition> parametrization) {
                return new BuiltTypeDetails(parametrization, executableSubstance);
            }
        });
        TypeContainer(GenericType<?, ?> parentInstanceType) {
            this.parentInstanceType = parentInstanceType;
            this.executableSubstance = createExecutableTypeSubstance(parentInstanceType);
        }

        private class BuiltTypeDetails extends MethodType {
            BuiltTypeDetails(GenericType.Implementation<MethodType, MethodDefinition> implementation, ExecutableTypeSubstance executableSubstance) {
                super(implementation, executableSubstance);
            }

            @Override
            public MethodType asType() {
                return this;
            }

            @Override
            public MethodDefinition definition() {
                return MethodBuilder.this.definition();
            }

            @Override
            public Type returnType() {
                return definition().returnType().inEnvironment(definitionEnvironment());
            }

            @Override
            public GenericType<?, ?> capturedEnclosingType() {
                return parentInstanceType == null ? null : parentInstanceType;
            }


        }
    }

}
