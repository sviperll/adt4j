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
import com.github.sviperll.codemold.render.Renderer;
import com.github.sviperll.codemold.render.RendererContext;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class VariableDeclaration implements Renderable {
    VariableDeclaration() {
    }

    public abstract boolean isFinal();

    @Nonnull
    public abstract AnyType type();

    @Nonnull
    public abstract String name();

    public abstract boolean isInitialized();

    /**
     * Variable initialization value.
     * Throws UnsupportedOperationException for uninitialized fields
     * @throws UnsupportedOperationException
     * @see VariableDeclaration#isInitialized()
     * @return Variable initialization value.
     */
    @Nonnull
    abstract Renderable getInitialValue();

    @Nonnull
    @Override
    public Renderer createRenderer(final RendererContext context) {
        return () -> {
            if (isFinal()) {
                context.appendText("final");
                context.appendWhiteSpace();
            }
            context.appendRenderable(type());
            context.appendWhiteSpace();
            context.appendText(name());
            if (isInitialized()) {
                context.appendText(" = ");
                context.appendRenderable(getInitialValue());
            }
        };
    }

    @Nonnull
    final VariableDeclaration substitute(Substitution environment) {
        return new VariableDeclarationWithEnvironment(this, environment);
    }

    private static class VariableDeclarationWithEnvironment extends VariableDeclaration {

        private final VariableDeclaration original;
        private final Substitution environment;

        private VariableDeclarationWithEnvironment(VariableDeclaration original, Substitution environment) {
            this.original = original;
            this.environment = environment;
        }

        @Override
        public boolean isFinal() {
            return original.isFinal();
        }

        @Nonnull
        @Override
        public AnyType type() {
            return original.type().substitute(environment);
        }

        @Nonnull
        @Override
        public String name() {
            return original.name();
        }

        @Override
        public boolean isInitialized() {
            return original.isInitialized();
        }

        @Nonnull
        @Override
        Renderable getInitialValue() {
            return original.getInitialValue();
        }
    }
}
