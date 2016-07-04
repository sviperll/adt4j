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
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class FieldBuilder implements ExpressionContext {

    private final FieldDeclaration declaration = new BuiltFieldDeclaration();
    private final NestingBuilder residence;
    private final AnyType type;
    private final String name;
    private final boolean isFinal;
    private boolean isInitialized = false;
    private Expression initializer = null;
    private ExpressionContextDefinition initializationContext = null;

    FieldBuilder(NestingBuilder residence, boolean isFinal, AnyType type, String name) {
        if (!(type.isArray() || type.isObjectType() || type.isPrimitive() || type.isTypeVariable()))
            throw new IllegalArgumentException(type.kind() + " is not allowed here");
        this.residence = residence;
        this.isFinal = isFinal;
        this.type = type;
        this.name = name;
    }

    public void setAccessLevel(MemberAccess accessLevel) {
        residence.setAccessLevel(accessLevel);
    }

    @Nonnull
    public FieldDeclaration declaration() {
        return declaration;
    }

    public void initialize(Expression expression) {
        if (isInitialized)
            throw new IllegalStateException("Field already initialized");
        isInitialized = true;
        initializer = expression;
    }

    @Override
    public ExpressionContextDefinition expressionContext() {
        if (initializationContext == null) {
            Nesting nesting = residence.residence().getNesting();
            NestingBuilder nestingBuilder = new NestingBuilder(nesting.isStatic(), nesting.parent());
            nestingBuilder.setAccessLevel(MemberAccess.PRIVATE);
            initializationContext = new ExpressionContextDefinition(nestingBuilder.residence());
        }
        return initializationContext;
    }

    private class BuiltFieldDeclaration extends FieldDeclaration {
        @Override
        public String name() {
            return name;
        }

        @Override
        public AnyType type() {
            return type;
        }

        @Override
        public Nesting nesting() {
            return residence.residence().getNesting();
        }

        @Override
        public boolean isInitialized() {
            return isInitialized;
        }

        @Override
        public boolean isFinal() {
            return isFinal;
        }

        @Override
        Renderable getInitialValue() {
            if (!isInitialized)
                throw new UnsupportedOperationException("Field is not initialized. Use isInitialized method for check");
            else
                return initializer;
        }
    }
}
