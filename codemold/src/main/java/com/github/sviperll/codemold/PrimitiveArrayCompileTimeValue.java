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
import com.github.sviperll.codemold.util.Characters;
import com.github.sviperll.codemold.util.Snapshot;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class PrimitiveArrayCompileTimeValue implements CompileTimeValue, Renderable {
    static PrimitiveArrayCompileTimeValue ofBytes(List<? extends Byte> bytes) {
        return new PrimitiveArrayCompileTimeValue(PrimitiveType.BYTE, bytes);
    }

    static PrimitiveArrayCompileTimeValue ofShorts(List<? extends Short> shorts) {
        return new PrimitiveArrayCompileTimeValue(PrimitiveType.SHORT, shorts);
    }

    static PrimitiveArrayCompileTimeValue ofIntegers(List<? extends Integer> integes) {
        return new PrimitiveArrayCompileTimeValue(PrimitiveType.INT, integes);
    }

    static PrimitiveArrayCompileTimeValue ofLongs(List<? extends Long> longs) {
        return new PrimitiveArrayCompileTimeValue(PrimitiveType.LONG, longs);
    }

    static PrimitiveArrayCompileTimeValue ofFloats(List<? extends Float> floats) {
        return new PrimitiveArrayCompileTimeValue(PrimitiveType.FLOAT, floats);
    }

    static PrimitiveArrayCompileTimeValue ofDoubles(List<? extends Double> doubles) {
        return new PrimitiveArrayCompileTimeValue(PrimitiveType.DOUBLE, doubles);
    }

    static PrimitiveArrayCompileTimeValue ofBooleans(List<? extends Boolean> booleans) {
        return new PrimitiveArrayCompileTimeValue(PrimitiveType.BOOLEAN, booleans);
    }

    static PrimitiveArrayCompileTimeValue ofCharacters(List<? extends Character> characters) {
        return new PrimitiveArrayCompileTimeValue(PrimitiveType.CHAR, characters);
    }

    private ArrayCompileTimeValue array = null;
    private final PrimitiveType elementType;
    private final List<?> elements;

    private PrimitiveArrayCompileTimeValue(PrimitiveType elementType, List<?> elements) {
        this.elementType = elementType;
        this.elements = Snapshot.of(elements);
    }

    public PrimitiveType elementType() {
        return elementType;
    }

    @SuppressWarnings("unchecked")
    public List<? extends Byte> getBytes() {
        if (!elementType.isByte())
            throw new UnsupportedOperationException("Is not byte");
        return (List<Byte>)Snapshot.of(elements);
    }

    @SuppressWarnings("unchecked")
    public List<? extends Short> getShorts() {
        if (!elementType.isShort())
            throw new UnsupportedOperationException("Is not short");
        return (List<Short>)Snapshot.of(elements);
    }

    @SuppressWarnings("unchecked")
    public List<? extends Integer> getIntegers() {
        if (!elementType.isInteger())
            throw new UnsupportedOperationException("Is not int");
        return (List<Integer>)Snapshot.of(elements);
    }

    @SuppressWarnings("unchecked")
    public List<? extends Long> getLongs() {
        if (!elementType.isLong())
            throw new UnsupportedOperationException("Is not long");
        return (List<Long>)Snapshot.of(elements);
    }

    @SuppressWarnings("unchecked")
    public List<? extends Float> getFloats() {
        if (!elementType.isFloat())
            throw new UnsupportedOperationException("Is not float");
        return (List<Float>)Snapshot.of(elements);
    }

    @SuppressWarnings("unchecked")
    public List<? extends Double> getDoubles() {
        if (!elementType.isDouble())
            throw new UnsupportedOperationException("Is not double");
        return (List<Double>)Snapshot.of(elements);
    }

    @SuppressWarnings("unchecked")
    public List<? extends Boolean> getBooleans() {
        if (!elementType.isBoolean())
            throw new UnsupportedOperationException("Is not boolean");
        return (List<Boolean>)Snapshot.of(elements);
    }

    @SuppressWarnings("unchecked")
    public List<? extends Character> getCharacters() {
        if (!elementType.isCharacter())
            throw new UnsupportedOperationException("Is not character");
        return (List<Character>)Snapshot.of(elements);
    }

    @Override
    public AnyCompileTimeValue asAny() {
        if (array == null)
            array = ArrayCompileTimeValue.wrapPrimitive(new Wrappable());
        return array.asAny();
    }

    @Override
    public Renderer createRenderer(RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                if (elements.size() != 1)
                    context.appendText("{");
                Iterator<?> iterator = elements.iterator();
                if (iterator.hasNext()) {
                    Object value = iterator.next();
                    if (value instanceof Character)
                        context.appendText(Characters.quote((Character)value));
                    else
                        context.appendText(value.toString());
                    while (iterator.hasNext()) {
                        context.appendText(", ");
                        value = iterator.next();
                        if (value instanceof Character)
                            context.appendText(Characters.quote((Character)value));
                        else
                            context.appendText(value.toString());
                    }
                }
                if (elements.size() != 1)
                    context.appendText("}");
            }
        };
    }

    class Wrappable {
        private Wrappable() {
        }
        PrimitiveArrayCompileTimeValue value() {
            return PrimitiveArrayCompileTimeValue.this;
        }
    }
}
