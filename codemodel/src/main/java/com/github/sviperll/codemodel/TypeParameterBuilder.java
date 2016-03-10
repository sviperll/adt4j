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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class TypeParameterBuilder {
    private final BuiltTypeParameter declaration = new BuiltTypeParameter();
    private final GenericDefinition<?> declaredIn;
    private final String name;
    private List<Type> bounds = new ArrayList<>();
    private boolean copyBoundsOnWrite = false;

    TypeParameterBuilder(GenericDefinition<?> declaredIn, String name) {
        this.declaredIn = declaredIn;
        this.name = name;
    }

    TypeParameter declaration() {
        return declaration;
    }

    void addAllBounds(Collection<? extends Type> bounds) throws CodeModelException {
        for (Type type: bounds) {
            addBound(type);
        }
    }
    void addBound(Type bound) throws CodeModelException {
        if (bound.isPrimitive())
            throw new CodeModelException("Primitive type can't be used as type-variable bound");
        if (bound.isArray())
            throw new CodeModelException("Array type can't be used as type-variable bound");
        if (bound.isWildcard())
            throw new CodeModelException("Wildcard type can't be used as type-variable bound");
        if (bounds.size() == 1 && bounds.get(0).isTypeVariable()
                || !bounds.isEmpty() && bound.isTypeVariable())
            throw new CodeModelException("Mixing type-variables with other bounds is not allowed");
        if (!bound.isIntersection()) {
            if (copyBoundsOnWrite) {
                bounds = new ArrayList<>(bounds);
                copyBoundsOnWrite = false;
            }
            bounds.add(bound);
        } else {
            addAllBounds(bound.intersectedTypes());
        }
    }
    private class BuiltTypeParameter extends TypeParameter {

        @Override
        public String name() {
            return name;
        }

        @Override
        public Type bound() {
            if (bounds.isEmpty()) {
                return declaredIn.getCodeModel().objectType();
            } else if (bounds.size() == 1) {
                return bounds.get(0);
            } else {
                bounds = Collections.unmodifiableList(bounds);
                copyBoundsOnWrite = true;
                try {
                    return new IntersectionType(bounds);
                } catch (CodeModelException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }

        @Override
        public GenericDefinition<?> declaredIn() {
            return declaredIn;
        }
    }
}
