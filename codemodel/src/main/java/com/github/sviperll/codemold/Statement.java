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

import com.github.sviperll.codemold.render.Renderable;
import com.github.sviperll.codemold.render.RendererContext;
import com.github.sviperll.codemold.render.Renderer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
abstract class Statement {
    Statement() {
    }

    @Nonnull
    abstract Renderer createStatementRenderer(RendererContext context);

    static abstract class Simple extends Statement {
        abstract Renderer createSimpleStatementRenderer(RendererContext context);

        @Override
        final Renderer createStatementRenderer(final RendererContext context) {
            return new Renderer() {
                @Override
                public void render() {
                    Renderer simple = createSimpleStatementRenderer(context);
                    simple.render();
                    context.appendText(";");
                }
            };
        }
    }

    static class StatementVariableDeclaration extends Simple {
        private final Declaration declaration = new Declaration();
        private final boolean isFinal;
        private final AnyType type;
        private final String name;
        private final Expression initializer;

        StatementVariableDeclaration(boolean isFinal, AnyType type, String name, @Nullable Expression initializer) {
            if (!(type.canBeDeclaredVariableType()))
                throw new IllegalArgumentException(type.kind() + " is not allowed here");
            this.isFinal = isFinal;
            this.type = type;
            this.name = name;
            this.initializer = initializer;
        }

        StatementVariableDeclaration(boolean isFinal, AnyType type, String name) {
            this(isFinal, type, name, null);
        }

        @Override
        Renderer createSimpleStatementRenderer(RendererContext context) {
            return declaration.createRenderer(context);
        }

        @Nonnull
        VariableDeclaration declaration() {
            return declaration;
        }

        private class Declaration extends VariableDeclaration {

            @Override
            public boolean isFinal() {
                return isFinal;
            }

            @Override
            public AnyType type() {
                return type;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public boolean isInitialized() {
                return initializer != null;
            }

            @Override
            Renderable getInitialValue() {
                if (initializer == null)
                    throw new UnsupportedOperationException("Variable is not initialized. Use isInitialized method for check.");
                else
                    return initializer;
            }

        }
    }
}
