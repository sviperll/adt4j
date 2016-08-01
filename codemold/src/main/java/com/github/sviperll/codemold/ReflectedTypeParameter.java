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

import com.github.sviperll.codemold.util.CMCollections;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
 class ReflectedTypeParameter<T extends java.lang.reflect.GenericDeclaration> extends TypeParameter {

    private final GenericDefinition<?, ?> declaredIn;
    private final TypeVariable<T> reflectedTypeParameter;
    private AnyType bound = null;

    ReflectedTypeParameter(GenericDefinition<?, ?> declaredIn, TypeVariable<T> reflectedTypeParameter) {
        this.declaredIn = declaredIn;
        this.reflectedTypeParameter = reflectedTypeParameter;
    }

    @Override
    public String name() {
        return reflectedTypeParameter.getName();
    }

    @Override
    public AnyType bound() {
        if (bound == null) {
            java.lang.reflect.Type[] reflectedBounds = reflectedTypeParameter.getBounds();
            if (reflectedBounds.length == 1) {
                bound = declaredIn.getCodeMold().readReflectedType(reflectedBounds[0]);
            } else {
                List<ObjectType> bounds = CMCollections.newArrayList();
                for (java.lang.reflect.Type reflectedBound : reflectedBounds) {
                    ObjectType partialBound = declaredIn.getCodeMold().readReflectedType(reflectedBound).getObjectDetails();
                    bounds.add(partialBound);
                }
                bound = new IntersectionType(bounds).asAny();
            }
        }
        return bound;
    }

    @Override
    public GenericDefinition<?, ?> declaredIn() {
        return declaredIn;
    }

}
