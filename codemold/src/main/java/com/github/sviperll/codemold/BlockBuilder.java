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
import com.github.sviperll.codemold.util.CMCollections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class BlockBuilder implements Renderable, ExpressionContext {
    static BlockBuilder createWithBracesForced(ExpressionContextDefinition expressionContext, VariableScope scope) {
        return new BlockBuilder(expressionContext, scope, true);
    }
    static BlockBuilder createWithAutoBraces(ExpressionContextDefinition expressionContext, VariableScope scope) {
        return new BlockBuilder(expressionContext, scope, false);
    }

    private final List<Statement> statements = CMCollections.newArrayList();
    private final ExpressionContextDefinition expressionContext;
    private final VariableScope scope;
    private final boolean braces;
    private IfBuilder ifStatement = null;

    private BlockBuilder(ExpressionContextDefinition expressionContext, VariableScope scope, boolean braces) {
        this.expressionContext = expressionContext;
        this.scope = scope;
        this.braces = braces;
    }

    @Nonnull
    @Override
    public Renderer createRenderer(final RendererContext context) {
        return createBlockRenderer(context, false);
    }

    @Nonnull
    @Override
    public ExpressionContextDefinition expressionContext() {
        return expressionContext;
    }

    @Nonnull
    Renderable withBraces() {
        return (RendererContext context) -> createBlockRenderer(context, braces);
    }

    @Nonnull
    private Renderer createBlockRenderer(final RendererContext context, final boolean forceBraces) {
        return () -> {
            boolean useBraces = inBraces() || forceBraces;
            RendererContext statementContext;
            if (!useBraces) {
                statementContext = context;
            } else {
                context.appendText("{");
                context.appendLineBreak();
                statementContext = context.indented();
            }
            Iterator<Statement> iterator = statements.iterator();
            if (!iterator.hasNext()) {
                if (!useBraces)
                    statementContext.appendText(";");
            } else {
                Statement statement = iterator.next();
                Renderer statementRenderer = statement.createStatementRenderer(statementContext);
                statementRenderer.render();
                while (iterator.hasNext()) {
                    statementContext.appendLineBreak();
                    statement = iterator.next();
                    statementRenderer = statement.createStatementRenderer(statementContext);
                    statementRenderer.render();
                }
            }
            if (useBraces) {
                context.appendLineBreak();
                context.appendText("}");
            }
        };
    }

    @Nonnull
    public VariableDeclaration variable(final Type type, String nameOrTemplate) throws CodeMoldException {
        final String name = scope.makeIntroducable(nameOrTemplate);
        scope.introduce(name);
        Statement.StatementVariableDeclaration statement = new Statement.StatementVariableDeclaration(false, type.asAny(), name);
        statements.add(statement);
        return statement.declaration();
    }

    @Nonnull
    public VariableDeclaration variable(final Type type, String nameOrTemplate, final Expression initializer) throws CodeMoldException {
        final String name = scope.makeIntroducable(nameOrTemplate);
        scope.introduce(name);
        Statement.StatementVariableDeclaration statement = new Statement.StatementVariableDeclaration(false, type.asAny(), name, initializer);
        statements.add(statement);
        return statement.declaration();
    }

    @Nonnull
    public VariableDeclaration finalVariable(final Type type, String nameOrTemplate) throws CodeMoldException {
        final String name = scope.makeIntroducable(nameOrTemplate);
        scope.introduce(name);
        Statement.StatementVariableDeclaration statement = new Statement.StatementVariableDeclaration(true, type.asAny(), name);
        statements.add(statement);
        return statement.declaration();
    }

    @Nonnull
    public VariableDeclaration finalVariable(final Type type, String nameOrTemplate, final Expression initializer) throws CodeMoldException {
        final String name = scope.makeIntroducable(nameOrTemplate);
        scope.introduce(name);
        Statement.StatementVariableDeclaration statement = new Statement.StatementVariableDeclaration(true, type.asAny(), name, initializer);
        statements.add(statement);
        return statement.declaration();
    }

    public void expression(final Expression expression) throws CodeMoldException {
        statements.add(new ExpressionStatement(expression));
    }

    public void assignment(final String name, final Expression expression) throws CodeMoldException {
        expression(Expression.variable(name).assignment(expression));
    }

    public void assignment(final Expression lvalue, final Expression expression) throws CodeMoldException {
        expression(lvalue.assignment(expression));
    }

    @Nonnull
    public IfBuilder ifStatement(final Expression condition) throws CodeMoldException {
        BlockBuilder thenBlock = BlockBuilder.createWithAutoBraces(expressionContext, scope.createNested());
        BlockBuilder elseBlock = BlockBuilder.createWithAutoBraces(expressionContext, scope.createNested());
        IfBuilder result = new IfBuilder(condition, thenBlock, elseBlock);
        ifStatement = result;
        statements.add(result.statement());
        return result;
    }

    @Nonnull
    public ForBuilder forLoop() {
        return new ForBuilder();
    }

    boolean isEmpty() {
        return statements.isEmpty();
    }

    boolean isSingleIf() {
        return ifStatement != null && statements.size() == 1;
    }

    @Nonnull
    IfBuilder getSingleIfStatement() {
        if (!isSingleIf())
            throw new IllegalStateException("Block is not single if statement");
        return ifStatement;
    }

    boolean inBraces() {
        return braces || statements.size() > 1;
    }

    public void returnStatement(final Expression result) {
        statements.add(new ReturnStatement(result));
    }

    private static class ExpressionStatement extends Statement.Simple {
        private final Expression expression;

        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        @Nonnull
        @Override
        Renderer createSimpleStatementRenderer(final RendererContext context) {
            return expression.createRenderer(context);
        }
    }

    private static class ReturnStatement extends Statement.Simple {
        private final Expression result;

        public ReturnStatement(Expression result) {
            this.result = result;
        }

        @Nonnull
        @Override
        Renderer createSimpleStatementRenderer(final RendererContext context) {
            return () -> {
                context.appendText("return");
                context.appendWhiteSpace();
                context.appendRenderable(result);
            };
        }
    }
}
