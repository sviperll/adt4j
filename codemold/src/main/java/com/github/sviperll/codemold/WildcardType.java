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
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public final class WildcardType implements Renderable, Type {

    private final AnyType type;
    private final BoundKind boundKind;
    private final AnyType bound;
    WildcardType(BoundKind boundKind, Type bound) {
        if (!bound.asAny().canBeTypeVariableBound())
            throw new IllegalArgumentException(bound.asAny().kind() + " can't be wildcard bound");
        this.boundKind = boundKind;
        this.bound = bound.asAny();
        this.type = AnyType.wrapWildcardType(new Wrappable());
    }

    @Nonnull
    public BoundKind boundKind() {
        return boundKind;
    }

    @Nonnull
    public AnyType bound() {
        return bound;
    }

    @Nonnull
    AnyType substitute(Substitution environment) {
        return new WildcardType(boundKind, bound.substitute(environment)).asAny();
    }

    @Nonnull
    @Override
    public AnyType asAny() {
        return type;
    }

    @Nonnull
    @Override
    public Renderer createRenderer(final RendererContext context) {
        return () -> {
            context.appendText("?");
            context.appendWhiteSpace();
            context.appendText(boundKind().name().toLowerCase(Locale.US));
            context.appendWhiteSpace();
            context.appendRenderable(bound());
        };
    }

    public enum BoundKind {
        SUPER, EXTENDS
    }

    class Wrappable {
        private Wrappable() {
        }
        @Nonnull
        WildcardType value() {
            return WildcardType.this;
        }
    }

}
