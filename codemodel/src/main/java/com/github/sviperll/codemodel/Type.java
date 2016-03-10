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

package com.github.sviperll.codemodel;

import java.util.Collection;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class Type {

    private static final Type VOID = new VoidType();

    public static Type variable(String name) {
        return new TypeVariable(name);
    }

    public static Type voidType() {
        return VOID;
    }

    Type() {
    }

    public abstract Kind kind();

    public abstract boolean isVoid();
    public abstract boolean isObjectType();
    public abstract boolean isPrimitive();
    public abstract boolean isArray();
    public abstract boolean isTypeVariable();
    public abstract boolean isWildcard();
    public abstract boolean isIntersection();

    /**
     * Casts this type to ObjectType.
     *
     * Throws UnsupportedOperationException if type is not object
     * (class, interface, enum, annotation).
     *
     * @return this type casted to ObjectType.
     *
     * @throws UnsupportedOperationException
     */
    public abstract ObjectType asObjectType();

    /**
     * Type of array element.
     *
     * Throws UnsupportedOperationException if type is not array.
     *
     * @return Type of array element.
     * @throws UnsupportedOperationException
     */
    public abstract Wildcard asWildcard();

    /**
     * Primitive kind, specific primitive type.
     *
     * Throws UnsupportedOperationException if type is not primitive.
     *
     * @return Primitive kind, specific primitive type.
     * @throws UnsupportedOperationException
     */
    public abstract PrimitiveTypeKind getPrimitiveTypeKind();

    /**
     * Type of array element.
     *
     * Throws UnsupportedOperationException if type is not array.
     *
     * @return Type of array element.
     * @throws UnsupportedOperationException
     */
    public abstract Type getArrayElementType();

    /**
     * Name of type-variable.
     *
     * Throws UnsupportedOperationException if type is not type-variable.
     *
     * @return Name of type-variable.
     * @throws UnsupportedOperationException
     */
    public abstract String getTypeVariableName();

    /**
     * Collection of intersected types.
     *
     * If this type is not intersection,
     * collection with single element is returned.
     * The element is this type itself.
     *
     * @return Collection of intersected types
     */
    public abstract Collection<Type> intersectedTypes();

    public abstract boolean containsWildcards();

    public enum Kind {
        VOID, OBJECT, PRIMITIVE, ARRAY, TYPE_VARIABLE, WILDCARD, INTERSECTION
    }
}
