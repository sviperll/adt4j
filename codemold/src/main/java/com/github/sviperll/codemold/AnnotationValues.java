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
public class AnnotationValues {
    static AnyAnnotationValue ofObject(Object defaultValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static PrimitiveAnnotationValue of(byte value) {
        return PrimitiveAnnotationValue.of(value);
    }

    public static PrimitiveAnnotationValue of(short value) {
        return PrimitiveAnnotationValue.of(value);
    }

    public static PrimitiveAnnotationValue of(int value) {
        return PrimitiveAnnotationValue.of(value);
    }

    public static PrimitiveAnnotationValue of(long value) {
        return PrimitiveAnnotationValue.of(value);
    }

    public static PrimitiveAnnotationValue of(float value) {
        return PrimitiveAnnotationValue.of(value);
    }

    public static PrimitiveAnnotationValue of(double value) {
        return PrimitiveAnnotationValue.of(value);
    }

    public static PrimitiveAnnotationValue of(char value) {
        return PrimitiveAnnotationValue.of(value);
    }

    public static PrimitiveAnnotationValue of(boolean value) {
        return PrimitiveAnnotationValue.of(value);
    }

    public static StringAnnotationValue of(String value) {
        return new StringAnnotationValue(value);
    }

    public static AnnotationValue of(EnumConstant constant) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static AnnotationValue of(ObjectDefinition definition) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static AnnotationValue of(Annotation definition) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private AnnotationValues() {
    }
}
