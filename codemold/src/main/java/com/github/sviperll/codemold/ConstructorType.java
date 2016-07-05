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

import com.github.sviperll.codemold.util.Consumer;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class ConstructorType extends ExecutableType<ConstructorType, ConstructorDefinition> {
    ConstructorType(ExecutableType.Implementation<ConstructorType, ConstructorDefinition> implementation) {
        super(implementation);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public ObjectType objectType() {
        return (ObjectType)getCapturedEnclosingType();
    }

    /**
     * Class instantiation.
     * @param arguments constructor arguments
     * @return Class instantiation
     */
    @Nonnull
    public Expression instantiation(final List<? extends Expression> arguments) {
        return Expression.instantiation(this, arguments);
    }

    /**
     * Anonymous class instantiation.
     *
     * Expression context is either a
     * com.github.sviperll.codemodel.BlockBuilder or
     * com.github.sviperll.codemodel.FieldBuilder.
     * It depends on where this anonymous class is going to be used.
     * com.github.sviperll.codemodel.FieldBuilder
     * should be used when anonymous class is used in field initializer.
     * <p>
     * Use anonymousClassDefinition argument to actually define anonymous class.
     *
     * @param arguments constructor arguments
     * @param context expression context
     * @param anonymousClassDefinition
     * @return Object instantiation (new-expression)
     */
    @Nonnull
    public Expression instantiation(final List<? extends Expression> arguments, ExpressionContext context, Consumer<? super AnonymousClassBuilder> anonymousClassDefinition) {
        return Expression.instantiation(this, arguments, context, anonymousClassDefinition);
    }
}
