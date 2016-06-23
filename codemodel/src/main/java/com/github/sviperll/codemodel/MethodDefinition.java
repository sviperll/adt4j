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

import com.github.sviperll.codemodel.render.Renderer;
import com.github.sviperll.codemodel.render.RendererContext;
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class MethodDefinition extends ExecutableDefinition<MethodType, MethodDefinition> {
    MethodDefinition(ExecutableDefinition.Implementation<MethodType, MethodDefinition> implementation) {
        super(implementation);
    }

    public abstract boolean isFinal();

    public abstract boolean isAbstract();

    @Nonnull
    public abstract Type returnType();

    @Nonnull
    public abstract String name();

    @Override
    final MethodType createType(ExecutableType.Implementation<MethodType, MethodDefinition> implementation) {
        return new DefinedType(implementation);
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                context.appendRenderable(residence());
                context.appendWhiteSpace();
                if (isFinal()) {
                    context.appendText("final");
                }
                context.appendWhiteSpace();
                if (isAbstract() && !(parent().kind().isInterface() || parent().kind().isAnnotation())) {
                    context.appendText("abstract");
                } else if (!isAbstract() && (parent().kind().isInterface() || parent().kind().isAnnotation())) {
                    context.appendText("default");
                }
                context.appendWhiteSpace();
                context.appendRenderable(typeParameters());
                context.appendWhiteSpace();
                context.appendRenderable(returnType());
                context.appendWhiteSpace();
                context.appendText(name());
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
                if (isAbstract())
                    context.appendText(";");
                else {
                    context.appendWhiteSpace();
                    context.appendRenderable(body());
                }
                context.appendLineBreak();
            }
        };
    }

    private class DefinedType extends MethodType {
        DefinedType(ExecutableType.Implementation<MethodType, MethodDefinition> implementation) {
            super(implementation);
        }

        @Override
        public MethodDefinition definition() {
            return MethodDefinition.this;
        }

        @Override
        public Type returnType() {
            return definition().returnType().substitute(definitionEnvironment());
        }
    }

}
