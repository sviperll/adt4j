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

import com.github.sviperll.codemold.render.Renderable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 * @param <D>
 */
@ParametersAreNonnullByDefault
public abstract class ExecutableDefinition<T extends ExecutableType<T, D>, D extends ExecutableDefinition<T, D>>
        extends GenericDefinition<T, D>
        implements Annotated {

    private final Implementation<T, D> implementation;
    ExecutableDefinition(Implementation<T, D> implementation) {
        this.implementation = implementation;
    }

    @Override
    public final TypeParameters typeParameters() {
        return implementation.typeParameters();
    }

    @Nonnull
    public final List<? extends VariableDeclaration> parameters() {
        return implementation.parameters();
    }

    @Nonnull
    public final List<? extends AnyType> throwsList() {
        return implementation.throwsList();
    }

    @Nonnull
    final Renderable body() {
        return implementation.body();
    }

    @Override
    final Residence residence() {
        return nesting().residence();
    }

    public final CodeMold getCodeMold() {
        return residence().getCodeMold();
    }

    @Nonnull
    final Nesting nesting() {
        return implementation.nesting();
    }

    public boolean isStatic(){
        return nesting().isStatic();
    }

    @Nonnull
    public MemberAccess accessLevel(){
        return nesting().accessLevel();
    }

    @Nonnull
    public ObjectDefinition parent(){
        return nesting().parent();
    }

    @Override
    public List<? extends Annotation> getAnnotation(ObjectDefinition definition) {
        return implementation.getAnnotation(definition);
    }

    @Override
    public Collection<? extends Annotation> allAnnotations() {
        return implementation.allAnnotations();
    }

    interface Implementation<T extends ExecutableType<T, D>, D extends ExecutableDefinition<T, D>>
            extends Annotated {

        @Nonnull
        TypeParameters typeParameters();

        @Nonnull
        List<? extends VariableDeclaration> parameters();

        @Nonnull
        List<? extends AnyType> throwsList();

        @Nonnull
        Renderable body();

        @Nonnull
        Nesting nesting();
    }
}
