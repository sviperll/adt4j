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

import com.github.sviperll.codemodel.render.Renderable;
import com.github.sviperll.codemodel.render.Renderer;
import com.github.sviperll.codemodel.render.RendererContext;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 */
public abstract class ExecutableDefinition<T extends Generic> extends GenericDefinition<T> {

    private final ExecutableDefinitionSubstance definition;
    ExecutableDefinition(ExecutableDefinitionSubstance definition) {
        super(definition.typeParameters());
        this.definition = definition;
    }

    public abstract boolean isConstructor();

    public abstract boolean isMethod();

    public abstract MethodDefinition getMethodDetails();

    public final List<VariableDeclaration> parameters() {
        return definition.parameters();
    }

    public final List<Type> throwsList() {
        return definition.throwsList();
    }

    final Renderable body() {
        return definition.body();
    }

    @Override
    public final Residence residence() {
        return definition.residence();
    }

    @Override
    public final CodeModel getCodeModel() {
        return definition.getCodeModel();
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                context.appendRenderable(residence());
                context.appendWhiteSpace();
                if (!isConstructor() && getMethodDetails().isFinal()) {
                    context.appendText("final");
                }
                context.appendWhiteSpace();
                context.appendRenderable(typeParameters());
                context.appendWhiteSpace();
                if (!isConstructor()) {
                    context.appendRenderable(getMethodDetails().returnType());
                    context.appendWhiteSpace();
                    context.appendText(getMethodDetails().name());
                } else {
                    context.appendText(residence().getNesting().parent().simpleName());
                }
                context.appendText("(");
                Iterator<VariableDeclaration> parameters = parameters().iterator();
                if (parameters.hasNext()) {
                    VariableDeclaration parameter = parameters.next();
                    context.appendRenderable(parameter);
                    while (parameters.hasNext()) {
                        context.appendText(", ");
                        parameter = parameters.next();
                        context.appendRenderable(parameter);
                    }
                }
                context.appendText(")");
                Iterator<Type> throwsExceptions = throwsList().iterator();
                if (throwsExceptions.hasNext()) {
                    Type exceptionType = throwsExceptions.next();
                    context.appendWhiteSpace();
                    context.appendText("throws");
                    context.appendWhiteSpace();
                    context.appendRenderable(exceptionType);
                    while (throwsExceptions.hasNext()) {
                        exceptionType = throwsExceptions.next();
                        context.appendText(", ");
                        context.appendRenderable(exceptionType);
                    }
                }
                context.appendWhiteSpace();
                context.appendRenderable(body());
                context.appendLineBreak();
            }
        };
    }

}
