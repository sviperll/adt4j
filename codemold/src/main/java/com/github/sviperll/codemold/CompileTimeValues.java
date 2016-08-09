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

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class CompileTimeValues {

    public static PrimitiveCompileTimeValue of(byte value) {
        return PrimitiveCompileTimeValue.of(value);
    }

    public static PrimitiveCompileTimeValue of(short value) {
        return PrimitiveCompileTimeValue.of(value);
    }

    public static PrimitiveCompileTimeValue of(int value) {
        return PrimitiveCompileTimeValue.of(value);
    }

    public static PrimitiveCompileTimeValue of(long value) {
        return PrimitiveCompileTimeValue.of(value);
    }

    public static PrimitiveCompileTimeValue of(float value) {
        return PrimitiveCompileTimeValue.of(value);
    }

    public static PrimitiveCompileTimeValue of(double value) {
        return PrimitiveCompileTimeValue.of(value);
    }

    public static PrimitiveCompileTimeValue of(char value) {
        return PrimitiveCompileTimeValue.of(value);
    }

    public static PrimitiveCompileTimeValue of(boolean value) {
        return PrimitiveCompileTimeValue.of(value);
    }

    public static AnyCompileTimeValue of(String value) {
        return AnyCompileTimeValue.of(value);
    }

    public static AnyCompileTimeValue of(EnumConstant constant) {
        return AnyCompileTimeValue.of(constant);
    }

    public static AnyCompileTimeValue of(ObjectDefinition definition) {
        return AnyCompileTimeValue.of(definition);
    }

    public static AnyCompileTimeValue of(Annotation annotation) {
        return AnyCompileTimeValue.of(annotation);
    }

    public static PrimitiveArrayCompileTimeValue ofBytes(List<? extends Byte> bytes) {
        return ArrayCompileTimeValue.ofBytes(bytes);
    }

    public static PrimitiveArrayCompileTimeValue ofShorts(List<? extends Short> shorts) {
        return ArrayCompileTimeValue.ofShorts(shorts);
    }

    public static PrimitiveArrayCompileTimeValue ofIntegers(List<? extends Integer> integes) {
        return ArrayCompileTimeValue.ofIntegers(integes);
    }

    public static PrimitiveArrayCompileTimeValue ofLongs(List<? extends Long> longs) {
        return ArrayCompileTimeValue.ofLongs(longs);
    }

    public static PrimitiveArrayCompileTimeValue ofFloats(List<? extends Float> floats) {
        return ArrayCompileTimeValue.ofFloats(floats);
    }

    public static PrimitiveArrayCompileTimeValue ofDoubles(List<? extends Double> doubles) {
        return ArrayCompileTimeValue.ofDoubles(doubles);
    }

    public static PrimitiveArrayCompileTimeValue ofBooleans(List<? extends Boolean> booleans) {
        return ArrayCompileTimeValue.ofBooleans(booleans);
    }

    public static PrimitiveArrayCompileTimeValue ofCharacters(List<? extends Character> characters) {
        return ArrayCompileTimeValue.ofCharacters(characters);
    }

    public static ArrayCompileTimeValue ofStrings(List<? extends String> strings) {
        return ArrayCompileTimeValue.ofStrings(strings);
    }

    public static ArrayCompileTimeValue ofEnumConstants(List<? extends EnumConstant> enumConstants) {
        return ArrayCompileTimeValue.ofEnumConstants(enumConstants);
    }

    public static ArrayCompileTimeValue ofObjectDefinitions(List<? extends ObjectDefinition> objectDefinitions) {
        return ArrayCompileTimeValue.ofObjectDefinitions(objectDefinitions);
    }

    public static ArrayCompileTimeValue ofAnnotations(List<? extends Annotation> annotations) {
        return ArrayCompileTimeValue.ofAnnotations(annotations);
    }


    private CompileTimeValues() {
    }
}
