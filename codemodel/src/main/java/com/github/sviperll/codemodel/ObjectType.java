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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class ObjectType extends GenericType<ObjectType, ObjectDefinition> implements Renderable {
    private final Type type = Type.wrapObjectType(this);
    private List<MethodType> methods = null;
    private List<ConstructorType> constructors = null;

    ObjectType(GenericType.Implementation<ObjectType, ObjectDefinition> implementation) {
        super(implementation);
    }

    @Override
    public abstract ObjectDefinition definition();

    @Override
    public final ObjectType asSpecificType() {
        return this;
    }

    @Nonnull
    public final Type asType() {
        return type;
    }

    @Nonnull
    public final IntersectionType intersection(ObjectType that) {
        return new IntersectionType(Arrays.asList(this, that));
    }

    @Nonnull
    public final Expression instanceofOp(Expression expression) throws CodeModelException {
        return expression.instanceofOp(this);
    }

    final boolean containsWildcards() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final boolean sameDefinition(ObjectType that) {
        return definition() == that.definition();
    }

    @Nonnull
    public final List<MethodType> methods() {
        if (methods == null) {
            methods = new ArrayList<>(definition().methods().size());
            for (final MethodDefinition definition: definition().methods()) {
                if (definition.isStatic())
                    methods.add(definition.rawType());
                else
                    methods.add(definition.rawType(this));
            }
            methods = Collections.unmodifiableList(methods);
        }
        return methods;
    }

    @Nonnull
    public final List<ConstructorType> constructors() {
        if (constructors == null) {
            constructors = new ArrayList<>(definition().constructors().size());
            for (final ConstructorDefinition definition: definition().constructors()) {
                constructors.add(definition.rawType(this));
            }
            constructors = Collections.unmodifiableList(constructors);
        }
        return constructors;
    }

    public boolean isJavaLangObject() {
        return definition().isJavaLangObject();
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                if (isRaw())
                    context.appendQualifiedClassName(definition().qualifiedName());
                else {
                    context.appendRenderable(erasure());
                    Iterator<Type> iterator = typeArguments().iterator();
                    if (iterator.hasNext()) {
                        context.appendText("<");
                        context.appendRenderable(iterator.next());
                        while (iterator.hasNext()) {
                            context.appendText(", ");
                            context.appendRenderable(iterator.next());
                        }
                        context.appendText(">");
                    }
                }
            }
        };
    }

}
