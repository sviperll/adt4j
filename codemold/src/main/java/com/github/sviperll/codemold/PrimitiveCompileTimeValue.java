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
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class PrimitiveCompileTimeValue implements CompileTimeValue, Renderable {

    public static PrimitiveCompileTimeValue of(byte value) {
        return new PrimitiveCompileTimeValue(PrimitiveType.BYTE, value);
    }

    public static PrimitiveCompileTimeValue of(short value) {
        return new PrimitiveCompileTimeValue(PrimitiveType.SHORT, value);
    }

    public static PrimitiveCompileTimeValue of(int value) {
        return new PrimitiveCompileTimeValue(PrimitiveType.INT, value);
    }

    public static PrimitiveCompileTimeValue of(long value) {
        return new PrimitiveCompileTimeValue(PrimitiveType.LONG, value);
    }

    public static PrimitiveCompileTimeValue of(float value) {
        return new PrimitiveCompileTimeValue(PrimitiveType.FLOAT, value);
    }

    public static PrimitiveCompileTimeValue of(double value) {
        return new PrimitiveCompileTimeValue(PrimitiveType.DOUBLE, value);
    }

    public static PrimitiveCompileTimeValue of(char value) {
        return new PrimitiveCompileTimeValue(PrimitiveType.CHAR, value);
    }

    public static PrimitiveCompileTimeValue of(boolean value) {
        return new PrimitiveCompileTimeValue(PrimitiveType.BOOLEAN, value);
    }

    private AnyCompileTimeValue any = null;
    private final PrimitiveType type;
    private final Object value;

    private PrimitiveCompileTimeValue(PrimitiveType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public PrimitiveType type() {
        return type;
    }

    public byte getByte() {
        if (!type.isByte())
            throw new UnsupportedOperationException("Is not byte");
        return (Byte)value;
    }

    public short getShort() {
        if (!type.isShort())
            throw new UnsupportedOperationException("Is not short");
        return (Short)value;
    }

    public int getInteger() {
        if (!type.isInteger())
            throw new UnsupportedOperationException("Is not int");
        return (Integer)value;
    }

    public long getLong() {
        if (!type.isLong())
            throw new UnsupportedOperationException("Is not long");
        return (Long)value;
    }

    public float getFloat() {
        if (!type.isFloat())
            throw new UnsupportedOperationException("Is not float");
        return (Float)value;
    }

    public double getDouble() {
        if (!type.isDouble())
            throw new UnsupportedOperationException("Is not double");
        return (Double)value;
    }

    public boolean getBoolean() {
        if (!type.isBoolean())
            throw new UnsupportedOperationException("Is not boolean");
        return (Boolean)value;
    }

    public char getCharacter() {
        if (!type.isCharacter())
            throw new UnsupportedOperationException("Is not character");
        return (Character)value;
    }

    @Override
    public AnyCompileTimeValue asAny() {
        if (any == null)
            any = AnyCompileTimeValue.wrapPrimitive(new Wrappable());
        return any;
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return () -> {
            if (type.isCharacter())
                context.appendText(Characters.quote(getCharacter()));
            else
                context.appendText(value.toString());
        };
    }

    class Wrappable {
        private Wrappable() {
        }
        PrimitiveCompileTimeValue value() {
            return PrimitiveCompileTimeValue.this;
        }
    }
}
