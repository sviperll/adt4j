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

import com.github.sviperll.codemodel.AnyType;
import com.github.sviperll.codemodel.render.Renderable;
import com.github.sviperll.codemodel.render.Renderer;
import com.github.sviperll.codemodel.render.RendererContext;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class PrecedenceAwareRendererContext {

    private final RendererContext context;
    private final int precedence;

    PrecedenceAwareRendererContext(RendererContext context, int precedence) {
        this.context = context;
        this.precedence = precedence;
    }

    public void appendSamePrecedenceRenderable(PrecedenceRenderable expression) {
        Renderer renderer = expression.createKnownPrecedenceRenderer(this);
        renderer.render();
    }

    public void appendHigherPrecedenceRenderable(PrecedenceRenderable expression) {
        Renderer renderer = expression.createKnownPrecedenceRenderer(withPrecedence(precedence - 1));
        renderer.render();
    }

    public void appendText(String text) {
        context.appendText(text);
    }

    public void nextLine() {
        context.appendLineBreak();
    }

    public PrecedenceAwareRendererContext indented() {
        return new PrecedenceAwareRendererContext(context.indented(), precedence);
    }

    PrecedenceAwareRendererContext withPrecedence(int newPrecedence) {
        return new PrecedenceAwareRendererContext(context, newPrecedence);
    }

    int precedence() {
        return precedence;
    }

    public void appendWhiteSpace() {
        context.appendWhiteSpace();
    }

    public void appendFreeStandingRenderable(Renderable renderable) {
        context.appendRenderable(renderable);
    }
}
