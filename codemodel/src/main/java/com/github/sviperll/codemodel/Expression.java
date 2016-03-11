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

import com.github.sviperll.codemodel.expression.ExpressionFactory;
import com.github.sviperll.codemodel.expression.ExpressionFactory.ExpressionDefinition;
import com.github.sviperll.codemodel.render.Renderer;
import com.github.sviperll.codemodel.render.RendererContext;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class Expression {
    private static final ExpressionFactory LITERAL = ExpressionFactory.TOP;
    private static final ExpressionFactory POSTFIX = LITERAL.next();
    private static final ExpressionFactory UNARY = POSTFIX.next();
    private static final ExpressionFactory MULTIPLICATIVE = UNARY.next();
    private static final ExpressionFactory ADDITIVE = MULTIPLICATIVE.next();
    private static final ExpressionFactory SHIFT = ADDITIVE.next();
    private static final ExpressionFactory RELATIONAL = SHIFT.next();
    private static final ExpressionFactory EQUALITY = RELATIONAL.next();
    private static final ExpressionFactory BITWISE_AND = EQUALITY.next();
    private static final ExpressionFactory BITWISE_OR = BITWISE_AND.next();
    private static final ExpressionFactory LOGICAL_AND = BITWISE_OR.next();
    private static final ExpressionFactory LOGICAL_OR = LOGICAL_AND.next();
    private static final ExpressionFactory TERNARY = LOGICAL_OR.next();
    private static final ExpressionFactory ASSIGNMENT = TERNARY.next();

    public static final Expression literal(final String s) {
        return LITERAL.createExpression(new ExpressionFactory.Renderable() {
            @Override
            public Renderer createRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.append("\"");
                        context.append(s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n"));
                        context.append("\"");
                    }
                };
            }
        });
    }

    public static final Expression literal(final int i) {
        return LITERAL.createExpression(new ExpressionFactory.Renderable() {
            @Override
            public Renderer createRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.append(Integer.toString(i));
                    }
                };
            }
        });
    }

    public static final Expression literal(final long i) {
        return LITERAL.createExpression(new ExpressionFactory.Renderable() {
            @Override
            public Renderer createRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.append(Long.toString(i));
                        context.append("L");
                    }
                };
            }
        });
    }

    public static final Expression literal(final double i) {
        return LITERAL.createExpression(new ExpressionFactory.Renderable() {
            @Override
            public Renderer createRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.append(Double.toString(i));
                    }
                };
            }
        });
    }

    public static final Expression literal(final float f) {
        return LITERAL.createExpression(new ExpressionFactory.Renderable() {
            @Override
            public Renderer createRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.append(Float.toString(f));
                        context.append("F");
                    }
                };
            }
        });
    }

    private final ExpressionDefinition definition;
    public Expression(ExpressionDefinition definition) {
        this.definition = definition;
    }

    public ExpressionDefinition definition() {
        return definition;
    }

    public Renderer createTopLevelExpressionRenderer(RendererContext context) {
        return definition.createTopLevelExpressionRenderer(context);
    }

    public Expression plus(Expression expression) {
        return ADDITIVE.createLeftAssociativeExpression(this, "+", expression);
    }
    public Expression minus(Expression expression) {
        return ADDITIVE.createLeftAssociativeExpression(this, "-", expression);
    }
    public Expression div(Expression expression) {
        return MULTIPLICATIVE.createLeftAssociativeExpression(this, "/", expression);
    }
    public Expression mod(Expression expression) {
        return MULTIPLICATIVE.createLeftAssociativeExpression(this, "%", expression);
    }
    public Expression times(Expression expression) {
        return MULTIPLICATIVE.createLeftAssociativeExpression(this, "*", expression);
    }
    public Expression or(Expression expression) {
        return LOGICAL_OR.createLeftAssociativeExpression(this, "%", expression);
    }
    public Expression and(Expression expression) {
        return LOGICAL_AND.createLeftAssociativeExpression(this, "*", expression);
    }
    public Expression gt(Expression expression) {
        return RELATIONAL.createLeftAssociativeExpression(this, ">", expression);
    }
    public Expression lt(Expression expression) {
        return RELATIONAL.createLeftAssociativeExpression(this, "<", expression);
    }
    public Expression ge(Expression expression) {
        return RELATIONAL.createLeftAssociativeExpression(this, ">=", expression);
    }
    public Expression le(Expression expression) {
        return RELATIONAL.createLeftAssociativeExpression(this, "<=", expression);
    }
    public Expression instanceofOp(Type type) throws CodeModelException {
        if (!type.isObjectType() || !type.getObjectDetails().isRaw())
            throw new CodeModelException("Only raw object types allowed here");
        return RELATIONAL.createExpression(new ExpressionFactory.Renderable() {
            @Override
            public Renderer createRenderer(RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        });
    }
    public Expression eq(Expression expression) {
        return EQUALITY.createLeftAssociativeExpression(this, "==", expression);
    }
    public Expression ne(Expression expression) {
        return EQUALITY.createLeftAssociativeExpression(this, "!=", expression);
    }
}
