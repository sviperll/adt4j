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

import com.github.sviperll.codemodel.expression.Precedence;
import com.github.sviperll.codemodel.render.Renderer;
import com.github.sviperll.codemodel.expression.PrecedenceAwareRendererContext;
import com.github.sviperll.codemodel.expression.PrecedenceRenderable;
import com.github.sviperll.codemodel.render.RendererContext;
import javax.annotation.ParametersAreNonnullByDefault;
import com.github.sviperll.codemodel.render.Renderable;
import com.github.sviperll.codemodel.expression.PrecedenceAwareRenderable;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class Expression implements Renderable {
    private static final Precedence TOP = Precedence.TOP;
    private static final Precedence POSTFIX = TOP.next();
    private static final Precedence UNARY = POSTFIX.next();
    private static final Precedence MULTIPLICATIVE = UNARY.next();
    private static final Precedence ADDITIVE = MULTIPLICATIVE.next();
    private static final Precedence SHIFT = ADDITIVE.next();
    private static final Precedence RELATIONAL = SHIFT.next();
    private static final Precedence EQUALITY = RELATIONAL.next();
    private static final Precedence BITWISE_AND = EQUALITY.next();
    private static final Precedence BITWISE_OR = BITWISE_AND.next();
    private static final Precedence LOGICAL_AND = BITWISE_OR.next();
    private static final Precedence LOGICAL_OR = LOGICAL_AND.next();
    private static final Precedence TERNARY = LOGICAL_OR.next();
    private static final Precedence ASSIGNMENT = TERNARY.next();

    public static final Expression literal(final String s) {
        return new Expression(TOP.createRenderable(new PrecedenceAwareRenderable() {
            @Override
            public Renderer createPrecedenceAwareRenderer(final PrecedenceAwareRendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendText("\"");
                        context.appendText(s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n"));
                        context.appendText("\"");
                    }
                };
            }
        }));
    }

    public static final Expression literal(final int i) {
        return new Expression(TOP.createRenderable(new PrecedenceAwareRenderable() {
            @Override
            public Renderer createPrecedenceAwareRenderer(final PrecedenceAwareRendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendText(Integer.toString(i));
                    }
                };
            }
        }));
    }

    public static final Expression literal(final long i) {
        return new Expression(TOP.createRenderable(new PrecedenceAwareRenderable() {
            @Override
            public Renderer createPrecedenceAwareRenderer(final PrecedenceAwareRendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendText(Long.toString(i));
                        context.appendText("L");
                    }
                };
            }
        }));
    }

    public static final Expression literal(final double i) {
        return new Expression(TOP.createRenderable(new PrecedenceAwareRenderable() {
            @Override
            public Renderer createPrecedenceAwareRenderer(final PrecedenceAwareRendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendText(Double.toString(i));
                    }
                };
            }
        }));
    }

    public static final Expression literal(final float f) {
        return new Expression(TOP.createRenderable(new PrecedenceAwareRenderable() {
            @Override
            public Renderer createPrecedenceAwareRenderer(final PrecedenceAwareRendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendText(Float.toString(f));
                        context.appendText("F");
                    }
                };
            }
        }));
    }

    static Expression variable(final String name) throws CodeModelException {
        CodeModel.validateSimpleName(name);
        return new Expression(TOP.createRenderable(new PrecedenceAwareRenderable() {
            @Override
            public Renderer createPrecedenceAwareRenderer(final PrecedenceAwareRendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendText(name);
                    }
                };
            }
        }));
    }
    private final PrecedenceRenderable renderable;

    public Expression(PrecedenceRenderable precedence) {
        this.renderable = precedence;
    }

    @Override
    public Renderer createRenderer(RendererContext context) {
        return renderable.createFreeStandingRenderer(context);
    }

    public Expression plus(Expression that) {
        return new Expression(ADDITIVE.createLeftAssociativeRenderable(this.renderable, "+", that.renderable));
    }
    public Expression minus(Expression that) {
        return new Expression(ADDITIVE.createLeftAssociativeRenderable(this.renderable, "-", that.renderable));
    }
    public Expression div(Expression that) {
        return new Expression(MULTIPLICATIVE.createLeftAssociativeRenderable(this.renderable, "/", that.renderable));
    }
    public Expression mod(Expression that) {
        return new Expression(MULTIPLICATIVE.createLeftAssociativeRenderable(this.renderable, "%", that.renderable));
    }
    public Expression times(Expression that) {
        return new Expression(MULTIPLICATIVE.createLeftAssociativeRenderable(this.renderable, "*", that.renderable));
    }
    public Expression or(Expression that) {
        return new Expression(LOGICAL_OR.createLeftAssociativeRenderable(this.renderable, "%", that.renderable));
    }
    public Expression and(Expression that) {
        return new Expression(LOGICAL_AND.createLeftAssociativeRenderable(this.renderable, "*", that.renderable));
    }
    public Expression gt(Expression that) {
        return new Expression(RELATIONAL.createLeftAssociativeRenderable(this.renderable, ">", that.renderable));
    }
    public Expression lt(Expression that) {
        return new Expression(RELATIONAL.createLeftAssociativeRenderable(this.renderable, "<", that.renderable));
    }
    public Expression ge(Expression that) {
        return new Expression(RELATIONAL.createLeftAssociativeRenderable(this.renderable, ">=", that.renderable));
    }
    public Expression le(Expression that) {
        return new Expression(RELATIONAL.createLeftAssociativeRenderable(this.renderable, "<=", that.renderable));
    }
    public Expression instanceofOp(final Type type) throws CodeModelException {
        if (!type.isObjectType() || !type.getObjectDetails().isRaw())
            throw new CodeModelException("Only raw object types allowed here");
        return new Expression(RELATIONAL.createRenderable(new PrecedenceAwareRenderable() {
            @Override
            public Renderer createPrecedenceAwareRenderer(final PrecedenceAwareRendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendHigherPrecedenceRenderable(renderable);
                        context.appendText(" instanceof ");
                        context.appendFreeStandingRenderable(type);
                    }
                };
            }
        }));
    }
    public Expression eq(Expression that) {
        return new Expression(EQUALITY.createLeftAssociativeRenderable(this.renderable, "==", that.renderable));
    }
    public Expression ne(Expression that) {
        return new Expression(EQUALITY.createLeftAssociativeRenderable(this.renderable, "!=", that.renderable));
    }

    Expression assignment(Expression that) {
        return new Expression(ASSIGNMENT.createRightAssociativeRenderable(this.renderable, "=", that.renderable));
    }
}
