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

package com.github.sviperll.codemodel.expression;

import com.github.sviperll.codemodel.render.Renderer;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class Precedence {
    public static final Precedence TOP = new Precedence(0);

    private final int precedence;

    private Precedence(int precedence) {
        this.precedence = precedence;
    }

    public Precedence next() {
        return new Precedence(precedence + 1);
    }

    public PrecedenceRenderable createRenderable(final PrecedenceAwareRenderable renderable) {
        return new PrecedenceRenderable(renderable, precedence);
    }

    public PrecedenceRenderable createLeftAssociativeRenderable(final PrecedenceRenderable left, final String op, final PrecedenceRenderable right) {
        return createRenderable(new LeftAssociativeExpressionRenderable(left, op, right));
    }

    public PrecedenceRenderable createRightAssociativeRenderable(PrecedenceRenderable left, String op, PrecedenceRenderable right) {
        return createRenderable(new RightAssociativeExpressionRenderable(left, op, right));
    }

    private static class LeftAssociativeExpressionRenderable implements PrecedenceAwareRenderable {

        private final PrecedenceRenderable left;
        private final String op;
        private final PrecedenceRenderable right;

        public LeftAssociativeExpressionRenderable(PrecedenceRenderable left, String op, PrecedenceRenderable right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public Renderer createPrecedenceAwareRenderer(final PrecedenceAwareRendererContext context) {
            return new Renderer() {
                @Override
                public void render() {
                    context.appendSamePrecedenceRenderable(left);
                    context.appendWhiteSpace();
                    context.appendText(op);
                    context.appendWhiteSpace();
                    context.appendHigherPrecedenceRenderable(right);
                }
            };
        }
    }


    private static class RightAssociativeExpressionRenderable implements PrecedenceAwareRenderable {

        private final PrecedenceRenderable left;
        private final String op;
        private final PrecedenceRenderable right;

        public RightAssociativeExpressionRenderable(PrecedenceRenderable left, String op, PrecedenceRenderable right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public Renderer createPrecedenceAwareRenderer(final PrecedenceAwareRendererContext context) {
            return new Renderer() {
                @Override
                public void render() {
                    context.appendHigherPrecedenceRenderable(left);
                    context.appendWhiteSpace();
                    context.appendText(op);
                    context.appendWhiteSpace();
                    context.appendSamePrecedenceRenderable(right);
                }
            };
        }
    }
}
