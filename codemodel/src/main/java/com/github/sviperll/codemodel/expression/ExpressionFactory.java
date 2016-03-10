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
import com.github.sviperll.codemodel.render.RendererContext;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class ExpressionFactory {
    public static final ExpressionFactory TOP = new ExpressionFactory(0);

    private static RendererFactory createRendererFactory(final int priority) {
        return new RendererFactory() {
            @Override
            public Renderer createRenderer(RendererContext context, Expression expression) {
                return expression.definition().createExpressionRenderer(priority, context);
            }
        };
    }

    private static BinaryRenderable createBinaryRenderable(final Expression leftExpression, final String op, final Expression rightExpression) {
        return new BinaryRenderable() {
            @Override
            public Renderer createRenderer(final RendererContext context, final RendererFactory leftFactory, final RendererFactory rightFactory) {
                final Renderer leftRenderer = leftFactory.createRenderer(context, leftExpression);
                final Renderer rightRenderer = rightFactory.createRenderer(context, rightExpression);
                return new Renderer() {
                    @Override
                    public void render() {
                        leftRenderer.render();
                        context.append(" ");
                        context.append(op);
                        context.append(" ");
                        rightRenderer.render();
                    }
                };
            }
        };
    }

    private static Renderable createRenderable(final BinaryRenderable renderable, final RendererFactory leftFactory, final RendererFactory rightFactory) {
        return new Renderable() {
            @Override
            public Renderer createRenderer(RendererContext context) {
                return renderable.createRenderer(context, leftFactory, rightFactory);
            }
        };
    }

    private final int priority;

    private ExpressionFactory(int priority) {
        this.priority = priority;
    }

    public ExpressionFactory next() {
        return new ExpressionFactory(priority + 1);
    }

    public Expression createExpression(final Renderable renderable) {
        return new Expression(new ExpressionDefinition() {
            @Override
            Renderer createExpressionRenderer(final int enclosingPriority, final RendererContext context) {
                final Renderer effectiveRenderer = renderable.createRenderer(context);
                return new Renderer() {
                    @Override
                    public void render() {
                        if (enclosingPriority >= priority) {
                            effectiveRenderer.render();
                        } else {
                            context.append("(");
                            effectiveRenderer.render();
                            context.append(")");
                        }
                    }
                };
            }
        });
    }

    public Expression createLeftAssociativeExpression(final Expression leftExpression, final String op, final Expression rightExpression) {
        return createLeftAssociativeExpression(createBinaryRenderable(leftExpression, op, rightExpression));
    }
    private Expression createLeftAssociativeExpression(final BinaryRenderable renderable) {
        return createExpression(createRenderable(renderable, createRendererFactory(priority), createRendererFactory(priority - 1)));
    }

    public Expression createRightAssociativeExpression(final Expression leftExpression, final String op, final Expression rightExpression) {
        return createRightAssociativeExpression(createBinaryRenderable(leftExpression, op, rightExpression));
    }
    private Expression createRightAssociativeExpression(final BinaryRenderable renderable) {
        return createExpression(createRenderable(renderable, createRendererFactory(priority - 1), createRendererFactory(priority)));
    }

    public static abstract class ExpressionDefinition {
        ExpressionDefinition() {
        }
        abstract Renderer createExpressionRenderer(int enclosingPriority, RendererContext context);

        public Renderer createTopLevelExpressionRenderer(RendererContext context) {
            return createExpressionRenderer(Integer.MAX_VALUE, context);
        }
    }

    public interface RendererFactory {

        Renderer createRenderer(RendererContext context, Expression expression);
    }

    public interface Renderable {

        Renderer createRenderer(RendererContext context);
    }

    public interface BinaryRenderable {

        Renderer createRenderer(RendererContext context, RendererFactory leftSubexpressionRenderer, RendererFactory rightSubexpressionRenderer);
    }

}
