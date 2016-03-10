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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class BlockBuilder {
    static BlockBuilder createWithBracesForced(VariableScope scope) {
        return new BlockBuilder(scope, true);
    }
    static BlockBuilder createWithAutoBraces(VariableScope scope) {
        return new BlockBuilder(scope, false);
    }

    private List<Statement> statements = new ArrayList<>();
    private final VariableScope scope;
    private final boolean braces;
    private IfBuilder ifStatement = null;

    private BlockBuilder(VariableScope scope, boolean braces) {
        this.scope = scope;
        this.braces = braces;
    }

    Renderer createBlockRenderer(final RendererContext context) {
        return createBlockRenderer(context, false);
    }

    Renderer createBlockRendererWithBraces(final RendererContext context) {
        return createBlockRenderer(context, true);
    }

    private Renderer createBlockRenderer(final RendererContext context, final boolean forceBraces) {
        return new Renderer() {
            @Override
            public void render() {
                boolean useBraces = inBraces() || forceBraces;
                RendererContext statementContext;
                if (!useBraces) {
                    statementContext = context;
                } else {
                    context.append("{");
                    context.nextLine();
                    statementContext = context.indented();
                }
                Iterator<Statement> iterator = statements.iterator();
                if (!iterator.hasNext()) {
                    if (!useBraces)
                        statementContext.append(";");
                } else {
                    Statement statement = iterator.next();
                    Renderer statementRenderer = statement.createStatementRenderer(statementContext);
                    statementRenderer.render();
                    while (iterator.hasNext()) {
                        statementContext.nextLine();
                        statement = iterator.next();
                        statementRenderer = statement.createStatementRenderer(statementContext);
                        statementRenderer.render();
                    }
                }
                if (useBraces) {
                    context.nextLine();
                    context.append("}");
                }
            }
        };
    }

    public void variable(final Type type, String nameOrTemplate) throws CodeModelException {
        final String name = scope.makeIntroducable(nameOrTemplate);
        scope.introduce(name);
        statements.add(new Statement.Simple() {
            @Override
            Renderer createSimpleStatementRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendType(type);
                        context.append(" ");
                        context.append(name);
                    }
                };
            }
        });
    }

    public void variable(final Type type, String nameOrTemplate, final Expression initializer) throws CodeModelException {
        final String name = scope.makeIntroducable(nameOrTemplate);
        scope.introduce(name);
        statements.add(new Statement.Simple() {
            @Override
            Renderer createSimpleStatementRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendType(type); // type?
                        context.append(" ");
                        context.append(name);
                        context.append(" = ");
                        Renderer initializerRenderer = initializer.createRenderer(context);
                        initializerRenderer.render();
                    }
                };
            }
        });
    }

    public void finalVariable(final Type type, String nameOrTemplate) throws CodeModelException {
        final String name = scope.makeIntroducable(nameOrTemplate);
        scope.introduce(name);
        statements.add(new Statement.Simple() {
            @Override
            Renderer createSimpleStatementRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.append("final ");
                        context.appendType(type); // type?
                        context.append(" ");
                        context.append(name);
                    }
                };
            }
        });
    }

    public void finalVariable(final Type type, String nameOrTemplate, final Expression initializer) throws CodeModelException {
        final String name = scope.makeIntroducable(nameOrTemplate);
        scope.introduce(name);
        statements.add(new Statement.Simple() {
            @Override
            Renderer createSimpleStatementRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.append("final ");
                        context.appendType(type); // type?
                        context.append(" ");
                        context.append(name);
                        context.append(" = ");
                        Renderer initializerRenderer = initializer.createRenderer(context);
                        initializerRenderer.render();
                    }
                };
            }
        });
    }

    public void assignment(final String name, final Expression expression) throws CodeModelException {
        statements.add(new Statement.Simple() {
            @Override
            Renderer createSimpleStatementRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.append(name);
                        context.append(" = ");
                        Renderer expressionRenderer = expression.createRenderer(context);
                        expressionRenderer.render();
                    }
                };
            }
        });
    }

    public void assignment(final Expression lvalue, final Expression expression) throws CodeModelException {
        statements.add(new Statement.Simple() {
            @Override
            Renderer createSimpleStatementRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        Renderer lvalueRenderer = lvalue.createRenderer(context);
                        lvalueRenderer.render();
                        context.append(" = ");
                        Renderer expressionRenderer = expression.createRenderer(context);
                        expressionRenderer.render();
                    }
                };
            }
        });
    }

    public IfBuilder ifStatement(final Expression condition) throws CodeModelException {
        VariableScope thenScope = scope.createNested();
        VariableScope elseScope = scope.createNested();
        IfBuilder result = new IfBuilder(condition, thenScope, elseScope);
        ifStatement = result;
        statements.add(result.statement());
        return result;
    }

    public ForBuilder forLoop() {
        throw new UnsupportedOperationException();
    }

    boolean isEmpty() {
        return statements.isEmpty();
    }

    boolean isSingleIf() {
        return ifStatement != null && statements.size() == 1;
    }

    IfBuilder getSingleIfStatement() {
        if (!isSingleIf())
            throw new IllegalStateException("Block is not single if statement");
        return ifStatement;
    }

    boolean inBraces() {
        return braces || statements.size() > 1;
    }
}
