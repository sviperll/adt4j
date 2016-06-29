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
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <B>
 */
@ParametersAreNonnullByDefault
public class EnumBuilder<B extends ResidenceBuilder> extends AbstractClassBuilder<B> {
    private static final NoOpConsumer NO_OP_CONSUMER = new NoOpConsumer();
    private final List<EnumConstant> constants = new ArrayList<>();
    public EnumBuilder(B residence, String name) {
        super(ObjectKind.ENUM, residence, name);
    }

    public void constant(final String name, final List<? extends Expression> constructorArguments, Consumer<EnumConstantBuilder> customization) throws CodeModelException {
        NestingBuilder nestingBuilder = new NestingBuilder(false, definition());
        nestingBuilder.setAccessLevel(MemberAccess.PRIVATE);
        EnumConstantBuilder builder = new EnumConstantBuilder(name, constructorArguments, nestingBuilder);
        customization.accept(builder);
        EnumConstant constant = builder.enumConstant();
        constants.add(constant);
    }

    public void constant(final String name, final List<? extends Expression> constructorArguments) throws CodeModelException {
        constant(name, constructorArguments, NO_OP_CONSUMER);
    }

    public void constant(final String name, Consumer<EnumConstantBuilder> customization) throws CodeModelException {
        constant(name, Collections.<Expression>emptyList(), customization);
    }

    public void constant(String name) throws CodeModelException {
        constant(name, Collections.<Expression>emptyList());
    }

    @Override
    ObjectDefinition createDefinition(TypeParameters typeParameters) {
        return new BuiltDefinition(typeParameters);
    }

    private class BuiltDefinition extends AbstractClassBuilder<B>.BuiltDefinition {
        BuiltDefinition(TypeParameters typeParameters) {
            super(typeParameters);
        }

        @Override
        public boolean isFinal() {
            return true;
        }

        @Override
        public ObjectType extendsClass() {
            return getCodeModel().objectType();
        }

        @Override
        public List<? extends EnumConstant> enumConstants() {
            return Collections.unmodifiableList(constants);
        }
    }

    private static class NoOpConsumer implements Consumer<EnumConstantBuilder> {

        public NoOpConsumer() {
        }

        @Override
        public void accept(EnumConstantBuilder value) {
        }
    }
}
