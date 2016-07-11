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

import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class PrimitiveAnnotationValue implements AnnotationValue {

    public static PrimitiveAnnotationValue of(byte value) {
        return new PrimitiveAnnotationValue(PrimitiveType.BYTE, value);
    }

    public static PrimitiveAnnotationValue of(short value) {
        return new PrimitiveAnnotationValue(PrimitiveType.SHORT, value);
    }

    public static PrimitiveAnnotationValue of(int value) {
        return new PrimitiveAnnotationValue(PrimitiveType.INT, value);
    }

    public static PrimitiveAnnotationValue of(long value) {
        return new PrimitiveAnnotationValue(PrimitiveType.LONG, value);
    }

    public static PrimitiveAnnotationValue of(float value) {
        return new PrimitiveAnnotationValue(PrimitiveType.FLOAT, value);
    }

    public static PrimitiveAnnotationValue of(double value) {
        return new PrimitiveAnnotationValue(PrimitiveType.DOUBLE, value);
    }

    public static PrimitiveAnnotationValue of(char value) {
        return new PrimitiveAnnotationValue(PrimitiveType.CHAR, value);
    }

    public static PrimitiveAnnotationValue of(boolean value) {
        return new PrimitiveAnnotationValue(PrimitiveType.BOOLEAN, value);
    }

    private final AnyAnnotationValue any = AnyAnnotationValue.wrapPrimitive(this);
    private final PrimitiveType type;
    private final Object value;

    private PrimitiveAnnotationValue(PrimitiveType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public boolean isByte() {
        return type == PrimitiveType.BYTE;
    }

    public boolean isShort() {
        return type == PrimitiveType.SHORT;
    }

    public boolean isInteger() {
        return type == PrimitiveType.INT;
    }

    public boolean isLong() {
        return type == PrimitiveType.LONG;
    }

    public boolean isFloat() {
        return type == PrimitiveType.FLOAT;
    }

    public boolean isDouble() {
        return type == PrimitiveType.DOUBLE;
    }

    public boolean isBoolean() {
        return type == PrimitiveType.BOOLEAN;
    }

    public boolean isCharacter() {
        return type == PrimitiveType.CHAR;
    }

    public byte getByte() {
        if (!isByte())
            throw new UnsupportedOperationException("Is not byte");
        return (Byte)value;
    }

    public short getShort() {
        if (!isShort())
            throw new UnsupportedOperationException("Is not short");
        return (Short)value;
    }

    public int getInteger() {
        if (!isInteger())
            throw new UnsupportedOperationException("Is not int");
        return (Integer)value;
    }

    public long getLong() {
        if (!isLong())
            throw new UnsupportedOperationException("Is not long");
        return (Long)value;
    }

    public float getFloat() {
        if (!isFloat())
            throw new UnsupportedOperationException("Is not float");
        return (Float)value;
    }

    public double getDouble() {
        if (!isDouble())
            throw new UnsupportedOperationException("Is not double");
        return (Double)value;
    }

    public boolean getBoolean() {
        if (!isBoolean())
            throw new UnsupportedOperationException("Is not boolean");
        return (Boolean)value;
    }

    public char getCharacter() {
        if (!isCharacter())
            throw new UnsupportedOperationException("Is not character");
        return (Character)value;
    }

    @Override
    public AnyAnnotationValue asAny() {
        return any;
    }
}
