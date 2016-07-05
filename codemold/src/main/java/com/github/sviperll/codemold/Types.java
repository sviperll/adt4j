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

import java.util.Collection;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.Nonnull;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class Types {
    private static final PrimitiveType BYTE = PrimitiveType.BYTE;
    private static final PrimitiveType SHORT = PrimitiveType.SHORT;
    private static final PrimitiveType INT = PrimitiveType.INT;
    private static final PrimitiveType LONG = PrimitiveType.LONG;
    private static final PrimitiveType FLOAT = PrimitiveType.FLOAT;
    private static final PrimitiveType DOUBLE = PrimitiveType.DOUBLE;
    private static final PrimitiveType CHAR = PrimitiveType.CHAR;
    private static final PrimitiveType BOOLEAN = PrimitiveType.BOOLEAN;

    public static TypeVariable variable(String name) {
        return new TypeVariable(name);
    }

    @Nonnull
    public static AnyType voidType() {
        return AnyType.voidType();
    }

    @Nonnull
    public static PrimitiveType byteType() {
        return BYTE;
    }

    @Nonnull
    public static PrimitiveType shortType() {
        return SHORT;
    }

    @Nonnull
    public static PrimitiveType intType() {
        return INT;
    }

    @Nonnull
    public static PrimitiveType longType() {
        return LONG;
    }

    @Nonnull
    public static PrimitiveType floatType() {
        return FLOAT;
    }

    @Nonnull
    public static PrimitiveType doubleType() {
        return DOUBLE;
    }

    @Nonnull
    public static PrimitiveType charType() {
        return CHAR;
    }

    @Nonnull
    public static PrimitiveType booleanType() {
        return BOOLEAN;
    }

    @Nonnull
    public static IntersectionType intersection(Collection<? extends ObjectType> bounds) {
        return new IntersectionType(bounds);
    }
    @Nonnull
    public static ArrayType arrayOf(AnyType componentType) {
        return new ArrayType(componentType);
    }

    @Nonnull
    public static WildcardType wildcardExtends(AnyType bound) {
        return new WildcardType(WildcardType.BoundKind.EXTENDS, bound);
    }

    @Nonnull
    public static WildcardType wildcardSuper(AnyType bound) {
        return new WildcardType(WildcardType.BoundKind.SUPER, bound);
    }


    private Types() {
    }
}
