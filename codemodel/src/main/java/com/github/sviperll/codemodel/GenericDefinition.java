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

import com.github.sviperll.codemodel.render.Renderable;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 * @param <D>
 */
public abstract class GenericDefinition<T extends Generic<T>, D extends GenericDefinition<T, D>>
        implements Settled, Renderable, Model {

    private T rawType;
    GenericDefinition() {
    }

    public abstract TypeParameters typeParameters();
    abstract T createType(GenericType.Implementation<T, D> implementation);

    public final boolean isGeneric() {
        if (!typeParameters().all().isEmpty())
            return true;
        else {
            GenericDefinition<?, ?> context = residence().contextDefinition();
            return context != null && context.isGeneric();
        }
    }

    public final T rawType() {
        if (residence().contextDefinition() == null) {
            if (rawType == null)
                rawType = GenericType.createRawType(this);
            return rawType;
        } else {
            throw new UnsupportedOperationException("Parent instance type is required");
        }
    }

    public final T rawType(GenericType<?, ?> capturedEnclosingType) {
        if (residence().contextDefinition() == null) {
            throw new UnsupportedOperationException("Type is static memeber, no parent is expected.");
        } else {
            return GenericType.createRawType(this, capturedEnclosingType);
        }
    }

    /**
     * Type of this definition usable inside definition.
     * @return type usable inside it's own definition.
     */
    public final T internalType() {
        T internalRawType;
        if (residence().contextDefinition() == null) {
            internalRawType = rawType();
        } else {
            internalRawType = rawType(residence().contextDefinition().internalType().getGenericDetails());
        }
        List<Type> internalTypeArguments = typeParameters().asInternalTypeArguments();
        try {
            return internalRawType.getGenericDetails().narrow(internalTypeArguments);
        } catch (CodeModelException ex) {
            throw new RuntimeException("No parameter-argument mismatch is guaranteed to ever happen", ex);
        }
    }
}
