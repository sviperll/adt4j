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
import com.github.sviperll.codemold.util.Snapshot;
import com.github.sviperll.codemold.util.Strings;
import java.util.Iterator;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class ArrayCompileTimeValue implements CompileTimeValue, Renderable {
    static PrimitiveArrayCompileTimeValue ofBytes(List<? extends Byte> bytes) {
        return PrimitiveArrayCompileTimeValue.ofBytes(bytes);
    }

    static PrimitiveArrayCompileTimeValue ofShorts(List<? extends Short> shorts) {
        return PrimitiveArrayCompileTimeValue.ofShorts(shorts);
    }

    static PrimitiveArrayCompileTimeValue ofIntegers(List<? extends Integer> integes) {
        return PrimitiveArrayCompileTimeValue.ofIntegers(integes);
    }

    static PrimitiveArrayCompileTimeValue ofLongs(List<? extends Long> longs) {
        return PrimitiveArrayCompileTimeValue.ofLongs(longs);
    }

    static PrimitiveArrayCompileTimeValue ofFloats(List<? extends Float> floats) {
        return PrimitiveArrayCompileTimeValue.ofFloats(floats);
    }

    static PrimitiveArrayCompileTimeValue ofDoubles(List<? extends Double> doubles) {
        return PrimitiveArrayCompileTimeValue.ofDoubles(doubles);
    }

    static PrimitiveArrayCompileTimeValue ofBooleans(List<? extends Boolean> booleans) {
        return PrimitiveArrayCompileTimeValue.ofBooleans(booleans);
    }

    static PrimitiveArrayCompileTimeValue ofCharacters(List<? extends Character> characters) {
        return PrimitiveArrayCompileTimeValue.ofCharacters(characters);
    }

    static ArrayCompileTimeValue ofStrings(List<? extends String> strings) {
        return new StringArrayAnnotationValueWrappper(strings);
    }

    static ArrayCompileTimeValue ofEnumConstants(List<? extends EnumConstant> enumConstants) {
        return new EnumConstantArrayAnnotationValueWrappper(enumConstants);
    }

    static ArrayCompileTimeValue ofObjectDefinitions(List<? extends ObjectDefinition> objectDefinitions) {
        return new ObjectDefinitionArrayAnnotationValueWrappper(objectDefinitions);
    }

    static ArrayCompileTimeValue ofAnnotations(List<? extends Annotation> annotations) {
        return new AnnotationArrayAnnotationValueWrappper(annotations);
    }

    static ArrayCompileTimeValue wrapPrimitive(PrimitiveArrayCompileTimeValue.Wrappable wrappable) {
        return new PrimitiveArrayAnnotationValueWrapper(wrappable.value());
    }

    private AnyCompileTimeValue any = null;
    private final AnyCompileTimeValue.Kind elementKind;

    private ArrayCompileTimeValue(AnyCompileTimeValue.Kind elementKind) {
        this.elementKind = elementKind;
    }

    public AnyCompileTimeValue.Kind elementKind() {
        return elementKind;
    }

    public PrimitiveArrayCompileTimeValue getPrimitives() {
        throw new UnsupportedOperationException("Is not primitive");
    }

    public List<? extends String> getStrings() {
        throw new UnsupportedOperationException("Is not string");
    }

    public List<? extends EnumConstant> getEnumConstants() {
        throw new UnsupportedOperationException("Is not EnumConstant");
    }

    public List<? extends ObjectDefinition> getObjectDefinitions() {
        throw new UnsupportedOperationException("Is not ObjectDefinition");
    }

    public List<? extends Annotation> getAnnotations() {
        throw new UnsupportedOperationException("Is not Annotation");
    }

    @Override
    public AnyCompileTimeValue asAny() {
        if (any == null)
            any = AnyCompileTimeValue.wrapArray(new Wrappable());
        return any;
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        if (elementKind.isPrimitive())
            return () -> context.appendRenderable(getPrimitives());
        else if (elementKind.isAnnotation()) {
            return new ArrayRenderer<Annotation>(context) {
                @Override
                List<? extends Annotation> getList() {
                    return getAnnotations();
                }
                @Override
                void renderValue(Annotation value) {
                    context.appendRenderable(value);
                }

            };
        } else if (elementKind.isArray()) {
            throw new IllegalStateException("Arrays of arrays are not supported in annotations");
        } else if (elementKind.isEnumConstant()) {
            return new ArrayRenderer<EnumConstant>(context) {
                @Override
                List<? extends EnumConstant> getList() {
                    return getEnumConstants();
                }
                @Override
                void renderValue(EnumConstant value) {
                    context.appendRenderable(value.enumDefinition().rawType());
                    context.appendText(".");
                    context.appendText(value.name());
                }
            };
        } else if (elementKind.isObjectDefinition()) {
            return new ArrayRenderer<ObjectDefinition>(context) {
                @Override
                List<? extends ObjectDefinition> getList() {
                    return getObjectDefinitions();
                }
                @Override
                void renderValue(ObjectDefinition value) {
                    context.appendRenderable(value.classLiteral());
                }
            };
        } else if (elementKind.isString()) {
            return new ArrayRenderer<String>(context) {
                @Override
                List<? extends String> getList() {
                    return getStrings();
                }
                @Override
                void renderValue(String value) {
                    context.appendText(Strings.quote(value));
                }
            };
        } else
            throw new UnsupportedOperationException("Unsupported kind " + elementKind);
    }

    private static abstract class ArrayRenderer<T> implements Renderer {

        private final RendererContext context;
        ArrayRenderer(RendererContext context) {
            this.context = context;
        }

        abstract List<? extends T> getList();
        abstract void renderValue(T value);

        @Override
        public void render() {
            List<? extends T> list = getList();
            if (list.size() != 1)
                context.appendText("{");
            Iterator<? extends T> iterator = list.iterator();
            if (iterator.hasNext()) {
                T value = iterator.next();
                renderValue(value);
                while (iterator.hasNext()) {
                    context.appendText(", ");
                    value = iterator.next();
                    renderValue(value);
                }
            }
            if (list.size() != 1)
                context.appendText("}");
        }
    }

    private static class PrimitiveArrayAnnotationValueWrapper extends ArrayCompileTimeValue {

        private final PrimitiveArrayCompileTimeValue value;

        public PrimitiveArrayAnnotationValueWrapper(PrimitiveArrayCompileTimeValue value) {
            super(AnyCompileTimeValue.Kind.PRIMITIVE);
            this.value = value;
        }

        @Override
        public PrimitiveArrayCompileTimeValue getPrimitives() {
            return value;
        }
    }

    private static class StringArrayAnnotationValueWrappper extends ArrayCompileTimeValue {

        private final List<? extends String> strings;

        public StringArrayAnnotationValueWrappper(List<? extends String> strings) {
            super(AnyCompileTimeValue.Kind.STRING);
            this.strings = Snapshot.of(strings);
        }

        @Override
        public List<? extends String> getStrings() {
            return Snapshot.of(strings);
        }
    }

    private static class EnumConstantArrayAnnotationValueWrappper extends ArrayCompileTimeValue {

        private final List<? extends EnumConstant> enumConstants;

        public EnumConstantArrayAnnotationValueWrappper(List<? extends EnumConstant> enumConstants) {
            super(AnyCompileTimeValue.Kind.ENUM_CONSTANT);
            this.enumConstants = Snapshot.of(enumConstants);
        }

        @Override
        public List<? extends EnumConstant> getEnumConstants() {
            return Snapshot.of(enumConstants);
        }
    }

    private static class ObjectDefinitionArrayAnnotationValueWrappper extends ArrayCompileTimeValue {

        private final List<? extends ObjectDefinition> objectDefinitions;

        public ObjectDefinitionArrayAnnotationValueWrappper(List<? extends ObjectDefinition> objectDefinitions) {
            super(AnyCompileTimeValue.Kind.OBJECT_DEFINITION);
            this.objectDefinitions = Snapshot.of(objectDefinitions);
        }

        @Override
        public List<? extends ObjectDefinition> getObjectDefinitions() {
            return Snapshot.of(objectDefinitions);
        }
    }

    private static class AnnotationArrayAnnotationValueWrappper extends ArrayCompileTimeValue {

        private final List<? extends Annotation> annotations;

        public AnnotationArrayAnnotationValueWrappper(List<? extends Annotation> annotations) {
            super(AnyCompileTimeValue.Kind.ANNOTATION);
            this.annotations = Snapshot.of(annotations);
        }

        @Override
        public List<? extends Annotation> getAnnotations() {
            return Snapshot.of(annotations);
        }
    }

    class Wrappable {
        private Wrappable() {
        }
        ArrayCompileTimeValue value() {
            return ArrayCompileTimeValue.this;
        }
    }
}
