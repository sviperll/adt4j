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
import com.github.sviperll.codemold.util.CMCollectors;
import com.github.sviperll.codemold.util.Snapshot;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class IntersectionType implements Renderable, Type {

    private AnyType type = null;
    private final Collection<? extends ObjectType> bounds;

    IntersectionType(Collection<? extends ObjectType> bounds) {
        this.bounds = Snapshot.of(bounds);
    }

    @Nonnull
    public Collection<? extends ObjectType> intersectedTypes() {
        return Snapshot.of(bounds);
    }

    @Override
    public AnyType asAny() {
        if (type == null)
            type = AnyType.wrapIntersectionType(new Wrappable());
        return type;
    }

    @Nonnull
    AnyType substitute(Substitution environment) {
        List<? extends ObjectType> substituted = bounds.stream()
                .map(bound -> bound.substitute(environment))
                .collect(CMCollectors.toImmutableList());
        return new IntersectionType(substituted).asAny();
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return () -> {
            Iterator<? extends ObjectType> iterator = intersectedTypes().iterator();
            if (iterator.hasNext()) {
                context.appendRenderable(iterator.next());
                while (iterator.hasNext()) {
                    context.appendText(" & ");
                    context.appendRenderable(iterator.next());
                }
            }
        };
    }

    class Wrappable {
        private Wrappable() {
        }
        IntersectionType value() {
            return IntersectionType.this;
        }
    }
}
