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

import com.github.sviperll.codemold.util.Snapshot;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class PrimitiveArrayAnnotationValue implements AnnotationValue {
    static PrimitiveArrayAnnotationValue ofBytes(List<? extends Byte> bytes) {
        return new PrimitiveArrayAnnotationValue(PrimitiveType.BYTE, bytes);
    }

    static PrimitiveArrayAnnotationValue ofShorts(List<? extends Short> shorts) {
        return new PrimitiveArrayAnnotationValue(PrimitiveType.SHORT, shorts);
    }

    static PrimitiveArrayAnnotationValue ofIntegers(List<? extends Integer> integes) {
        return new PrimitiveArrayAnnotationValue(PrimitiveType.INT, integes);
    }

    static PrimitiveArrayAnnotationValue ofLongs(List<? extends Long> longs) {
        return new PrimitiveArrayAnnotationValue(PrimitiveType.LONG, longs);
    }

    static PrimitiveArrayAnnotationValue ofFloats(List<? extends Float> floats) {
        return new PrimitiveArrayAnnotationValue(PrimitiveType.FLOAT, floats);
    }

    static PrimitiveArrayAnnotationValue ofDoubles(List<? extends Double> doubles) {
        return new PrimitiveArrayAnnotationValue(PrimitiveType.DOUBLE, doubles);
    }

    static PrimitiveArrayAnnotationValue ofBooleans(List<? extends Boolean> booleans) {
        return new PrimitiveArrayAnnotationValue(PrimitiveType.BOOLEAN, booleans);
    }

    static PrimitiveArrayAnnotationValue ofCharacters(List<? extends Character> characters) {
        return new PrimitiveArrayAnnotationValue(PrimitiveType.CHAR, characters);
    }

    private final ArrayAnnotationValue array = ArrayAnnotationValue.wrapPrimitive(this);
    private final PrimitiveType elementType;
    private final List<?> elements;

    private PrimitiveArrayAnnotationValue(PrimitiveType elementType, List<?> elements) {
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
    public AnyAnnotationValue asAny() {
        return array.asAny();
    }
}
