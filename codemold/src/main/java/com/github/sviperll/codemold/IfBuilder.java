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

import com.github.sviperll.codemold.render.RendererContext;
import com.github.sviperll.codemold.render.Renderer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class IfBuilder {

    private final BlockBuilder thenBlock;
    private final BlockBuilder elseBlock;
    private final Expression condition;

    IfBuilder(Expression condition, BlockBuilder thenBlock, BlockBuilder elseBlock) {
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        this.condition = condition;
    }

    @Nonnull
    public BlockBuilder thenBlock() {
        return thenBlock;
    }

    @Nonnull
    public BlockBuilder elseBlock() {
        return elseBlock;
    }

    @Nonnull
    Statement statement() {
        return new Statement() {
            @Override
            Renderer createStatementRenderer(final RendererContext context) {
                return new Renderer() {
                    @Override
                    public void render() {
                        context.appendText("if (");
                        context.appendRenderable(condition);
                        context.appendText(")");
                        if (thenBlock.inBraces()
                                || (thenBlock.isSingleIf() && thenBlock.getSingleIfStatement().elseBlock().isEmpty() && !elseBlock.isEmpty())) {
                            context.appendText(" ");
                            context.appendRenderable(thenBlock.withBraces());
                        } else {
                            context.appendLineBreak();
                            context.indented().appendRenderable(thenBlock);
                        }
                        if (!elseBlock.isEmpty()) {
                            context.appendText(" else");
                            if (elseBlock.inBraces())
                                context.appendText(" ");
                            else
                                context.appendLineBreak();
                            if (elseBlock.inBraces() || elseBlock.isSingleIf())
                                context.appendRenderable(elseBlock);
                            else
                                context.indented().appendRenderable(elseBlock);
                        }
                    }
                };
            }
        };
    }
}
