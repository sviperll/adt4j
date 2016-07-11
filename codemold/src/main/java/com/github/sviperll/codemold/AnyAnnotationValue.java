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
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class AnyAnnotationValue
        implements AnnotationValue, Renderable {

    static AnyAnnotationValue wrapPrimitive(PrimitiveAnnotationValue value) {
        return new PrimitiveAnnotationValueWrapper(value);
    }

    static AnyAnnotationValue wrapString(StringAnnotationValue value) {
        return new StringAnnotationValueWrapper(value);
    }

    private final Kind kind;

    private AnyAnnotationValue(Kind kind) {
        this.kind = kind;
    }

    public boolean isPrimitive() {
        return kind == Kind.PRIMITIVE;
    }

    public boolean isString() {
        return kind == Kind.STRING;
    }

    public boolean isEnumConstant() {
        return kind == Kind.ENUM_CONSTANT;
    }

    public boolean isObjectDefinition() {
        return kind == Kind.OBJECT_DEFINITION;
    }

    public boolean isAnnotation() {
        return kind == Kind.ANNOTATION;
    }

    public PrimitiveAnnotationValue getPrimitive() {
        throw new UnsupportedOperationException("Is not primitive");
    }

    public StringAnnotationValue getString() {
        throw new UnsupportedOperationException("Is not string");
    }

    @Override
    public AnyAnnotationValue asAny() {
        return this;
    }

    @Override
    public Renderer createRenderer(RendererContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public enum Kind {
        PRIMITIVE,
        STRING,
        ENUM_CONSTANT,
        OBJECT_DEFINITION,
        ANNOTATION,
        ARRAY;
    }

    private static class PrimitiveAnnotationValueWrapper extends AnyAnnotationValue {

        private final PrimitiveAnnotationValue value;

        PrimitiveAnnotationValueWrapper(PrimitiveAnnotationValue value) {
            super(Kind.PRIMITIVE);
            this.value = value;
        }

        @Override
        public PrimitiveAnnotationValue getPrimitive() {
            return value;
        }
    }

    private static class StringAnnotationValueWrapper extends AnyAnnotationValue {
        private final StringAnnotationValue value;

        public StringAnnotationValueWrapper(StringAnnotationValue value) {
            super(Kind.STRING);
            this.value = value;
        }

        @Override
        public StringAnnotationValue getString() {
            return value;
        }
    }

}
