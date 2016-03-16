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

import com.github.sviperll.codemodel.Expression;
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

    public PrecedenceRendering createExpression(final ExpressionRendering renderable) {
        return new PrecedenceRendering() {
            @Override
            Renderer createExpressionRenderer(final ExpressionRendererContext context) {
                final Renderer effectiveRenderer = renderable.createExpressionRenderer(context.withPrecedence(precedence));
                return new Renderer() {
                    @Override
                    public void render() {
                        if (context.precedence() >= precedence) {
                            effectiveRenderer.render();
                        } else {
                            context.append("(");
                            effectiveRenderer.render();
                            context.append(")");
                        }
                    }
                };
            }
        };
    }

    public PrecedenceRendering createLeftAssociativeExpression(final Expression left, final String op, final Expression right) {
        return createExpression(new BinaryOperationExpressionRendering(left, op, right));
    }

    private static class BinaryOperationExpressionRendering implements ExpressionRendering {

        private final Expression left;
        private final String op;
        private final Expression right;

        public BinaryOperationExpressionRendering(Expression left, String op, Expression right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public Renderer createExpressionRenderer(final ExpressionRendererContext context) {
            return new Renderer() {
                @Override
                public void render() {
                    context.appendSamePrecedenceExpression(left);
                    context.append(" ");
                    context.append(op);
                    context.append(" ");
                    context.appendHigherPrecedenceExpression(right);
                }
            };
        }
    }


}
