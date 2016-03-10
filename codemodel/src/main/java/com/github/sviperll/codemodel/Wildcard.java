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
import java.util.Collections;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public final class Wildcard extends Type {

    private final BoundKind boundKind;
    private final Type bound;
    Wildcard(BoundKind boundKind, Type bound) {
        this.boundKind = boundKind;
        this.bound = bound;
    }

    public BoundKind getWildcardBoundKind() {
        return boundKind;
    }

    public Type getWildcardBound() {
        return bound;
    }

    @Override
    public boolean isObjectType() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isTypeVariable() {
        return false;
    }

    @Override
    public boolean isWildcard() {
        return true;
    }

    @Override
    public boolean isIntersection() {
        return false;
    }

    @Override
    public ObjectType asObjectType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Wildcard asWildcard() {
        return this;
    }

    @Override
    public PrimitiveTypeKind getPrimitiveTypeKind() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getArrayElementType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTypeVariableName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Type> intersectedTypes() {
        return Collections.<Type>singletonList(this);
    }

    @Override
    public boolean containsWildcards() {
        return true;
    }

    @Override
    public Kind kind() {
        return Kind.WILDCARD;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    public enum BoundKind {
        SUPER, EXTENDS
    }
}
