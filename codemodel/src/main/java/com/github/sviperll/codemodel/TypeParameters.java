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
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class TypeParameters implements Renderable {
    TypeParameters() {
    }

    public abstract List<TypeParameter> all();

    public abstract TypeParameter get(String name);

    abstract List<Type> asInternalTypeArguments();

    final TypeParameters preventCycle(String name) {
        return new PreventCycleTypeParameters(this, name);
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                Iterator<TypeParameter> typeParameters = all().iterator();
                if (typeParameters.hasNext()) {
                    context.appendText("<");
                    TypeParameter typeParameter = typeParameters.next();
                    context.appendText(typeParameter.name());
                    context.appendText(" extends ");
                    context.appendRenderable(typeParameter.bound());
                    while (typeParameters.hasNext()) {
                        context.appendText(", ");
                        typeParameter = typeParameters.next();
                        context.appendText(typeParameter.name());
                        context.appendText(" extends ");
                        context.appendRenderable(typeParameter.bound());
                    }
                    context.appendText(">");
                }
            }
        };
    }

    private static class PreventCycleTypeParameters extends TypeParameters {
        private final TypeParameters parameters;
        private final String name;

        PreventCycleTypeParameters(TypeParameters parent, String name) {
            this.parameters = parent;
            this.name = name;
        }

        @Override
        public List<TypeParameter> all() {
            return parameters.all();
        }

        @Override
        public TypeParameter get(String name) {
            try {
                if (!name.equals(this.name))
                    return parameters.get(name);
                else {
                    throw new CodeModelException("Cyclic definition: " + name);
                }
            } catch (CodeModelException ex) {
                throw new RuntimeCodeModelException(ex);
            }
        }

        @Override
        List<Type> asInternalTypeArguments() {
            return parameters.asInternalTypeArguments();
        }
    }

}
