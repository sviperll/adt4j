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
import javax.annotation.Nonnull;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 * @param <D>
 */
public abstract class GenericDefinition<T extends GenericType<T, D>, D extends GenericDefinition<T, D>>
        implements Settled, Renderable, Model {

    private T rawType;
    GenericDefinition() {
    }

    @Nonnull
    public abstract TypeParameters typeParameters();

    @Nonnull
    abstract D fromGenericDefinition();

    @Nonnull
    abstract T createType(GenericType.Implementation<T, D> implementation);

    public final boolean isGeneric() {
        if (!typeParameters().all().isEmpty())
            return true;
        else {
            return residence().hasContextDefintion() && residence().getContextDefinition().isGeneric();
        }
    }

    /**
     * Raw type defined by this definition.
     * Throws UnsupportedOperationException if used for definitions that require context: inner classes.
     * @see Residence#hasContextDefintion()
     * @throws UnsupportedOperationException
     * @return Raw type defined by this definition.
     */
    @Nonnull
    public final T rawType() {
        if (residence().hasContextDefintion()) {
            throw new UnsupportedOperationException("Parent instance type is required");
        } else {
            if (rawType == null)
                rawType = GenericType.createRawType(fromGenericDefinition());
            return rawType;
        }
    }

    /**
     * Raw type defined by this definition.
     * @param capturedEnclosingType type of enclosing definition that is required to form a type of inner class.
     * Throws UnsupportedOperationException if this definition doesn't use any context: is static or top-level.
     * @see Residence#hasContextDefintion()
     * @throws UnsupportedOperationException
     * @return Raw type defined by this definition.
     */
    @Nonnull
    public final T rawType(GenericType<?, ?> capturedEnclosingType) {
        if (!residence().hasContextDefintion()) {
            throw new UnsupportedOperationException("Type is static memeber, no parent is expected.");
        } else {
            return GenericType.createRawType(fromGenericDefinition(), capturedEnclosingType);
        }
    }

    /**
     * Type of this definition usable inside definition.
     * @return type usable inside it's own definition.
     */
    @Nonnull
    public final T internalType() {
        T internalRawType;
        if (!residence().hasContextDefintion()) {
            internalRawType = rawType();
        } else {
            internalRawType = rawType(residence().getContextDefinition().internalType());
        }
        List<? extends Type> internalTypeArguments = typeParameters().asInternalTypeArguments();
        return internalRawType.narrow(internalTypeArguments);
    }
}
