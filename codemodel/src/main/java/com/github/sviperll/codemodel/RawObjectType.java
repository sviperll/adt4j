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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
abstract class RawObjectType extends ObjectType {

    RawObjectType() {
    }

    @Override
    public final ObjectType narrow(List<Type> typeArguments) throws CodeModelException {
        for (Type type: typeArguments) {
            if (type.isVoid())
                throw new CodeModelException("Void can't be used as a type-argument");
            if (type.isPrimitive())
                throw new CodeModelException("Primitive type can't be used as a type-argument");
            if (type.isIntersection())
                throw new CodeModelException("Intersection can't be used as type-argument");
            if (!type.isArray() && !type.isWildcard() && !type.isObjectType())
                throw new CodeModelException("Only array, wildcard or object type can be used as type argument: found " + type.kind());
        }
        if (typeArguments.size() != definition().generics().typeParameters().size())
            throw new CodeModelException("Type-argument list and type-parameter list differ in size");
        return new NarrowedObjectType(this, typeArguments);
    }

    @Override
    public final ObjectType erasure() {
        return this;
    }

    @Override
    public final boolean isRaw() {
        return true;
    }

    @Override
    public final boolean isNarrowed() {
        return false;
    }

    @Override
    public final Wildcard asWildcard() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Collection<Type> intersectedTypes() {
        return Collections.<Type>singletonList(this);
    }

    @Override
    public final boolean containsWildcards() {
        return false;
    }

    @Override
    public final List<Type> typeArguments() {
        List<Type> result = new ArrayList<>(definition().generics().typeParameters().size());
        for (TypeParameter typeParameter: definition().generics().typeParameters()) {
            ObjectType lowerRawBound;
            try {
                lowerRawBound = definition().generics().lowerRawBound(typeParameter.name());
            } catch (CodeModelException ex) {
                lowerRawBound = definition().getCodeModel().objectType();
            }
            result.add(lowerRawBound);
        }
        return result;
    }
}
