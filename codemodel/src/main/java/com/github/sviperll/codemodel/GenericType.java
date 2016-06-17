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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 * @param <D>
 */
@ParametersAreNonnullByDefault
public abstract class GenericType<T extends GenericType<T, D>, D extends GenericDefinition<T, D>> {
    @Nonnull
    static <T extends GenericType<T, D>, D extends GenericDefinition<T, D>> T createRawType(GenericDefinition<T, D> definition) {
        return definition.createType(new Raw<>(definition, null));
    }
    @Nonnull
    static <T extends GenericType<T, D>, D extends GenericDefinition<T, D>> T createRawType(GenericDefinition<T, D> definition, GenericType<?, ?> capturedEnclosingType) {
        return definition.createType(new Raw<>(definition, capturedEnclosingType));
    }

    private final Implementation<T, D> implementation;
    private Substitution definitionEnvironment = null;
    GenericType(Implementation<T, D> implementation) {
        this.implementation = implementation;
    }

    @Nonnull
    public abstract D definition();

    @Nonnull
    public final T erasure() {
        return implementation.erasure(this);
    }

    public final boolean isNarrowed() {
        return implementation.isNarrowed();
    }

    public final boolean isRaw() {
        return implementation.isRaw();
    }

    /**
     * Narrowed type for given raw-type.
     * Throws UnsupportedOperationException when applied to already narrowed type. Use with raw-types only.
     * @see GenericType#isNarrowed()
     * @see GenericType#isRaw()
     * @throws UnsupportedOperationException
     * @param typeArguments
     * @return Narrowed type for this raw-type.
     */
    @Nonnull
    public final T narrow(List<Type> typeArguments) {
        return implementation.narrow(this, typeArguments);
    }

    @Nonnull
    public final List<Type> typeArguments() {
        return implementation.typeArguments(this);
    }

    @Nonnull
    public abstract T asSpecificType();

    public final boolean hasCapturedEnclosingType() {
        return implementation.hasCapturedEnclosingType();
    }

    /**
     * Type of enclosing definition that defines a context for current type.
     * Throws UnsupportedOperationException when there is no captured enclosing type.
     * @see GenericType#hasCapturedEnclosingType()
     * @throws UnsupportedOperationException
     * @return Type of enclosing definition
     */
    @Nonnull
    public final GenericType<?, ?> getCapturedEnclosingType() {
        return implementation.getCapturedEnclosingType();
    }

    @Nonnull
    final T substitute(Substitution substitution) {
        return implementation.createGenericType(this, implementation.substitute(substitution));
    }

    @Nonnull
    final Substitution definitionEnvironment() {
        if (definitionEnvironment == null) {
            Substitution.Builder builder;
            builder = Substitution.createBuilder();
            D definition = definition();
            Iterator<TypeParameter> typeParameters = definition.typeParameters().all().iterator();
            Iterator<Type> typeArguments = typeArguments().iterator();
            while (typeParameters.hasNext() && typeArguments.hasNext()) {
                TypeParameter typeParameter = typeParameters.next();
                Type typeArgument = typeArguments.next();
                builder.put(typeParameter.name(), typeArgument);
            }
            Substitution result = builder.build();
            result = !hasCapturedEnclosingType() ? result : result.andThen(getCapturedEnclosingType().definitionEnvironment());
            definitionEnvironment = result;
        }
        return definitionEnvironment;
    }

    static abstract class Implementation<T extends GenericType<T, D>, D extends GenericDefinition<T, D>> {
        private final GenericDefinition<T, D> definition;
        private Implementation(GenericDefinition<T, D> definition) {
            this.definition = definition;
        }

        /**
         * Narrowed type for given raw-type.
         * Throws UnsupportedOperationException when applied to already narrowed type. Use with raw-types only.
         * @see Implementation#isNarrowed()
         * @see Implementation#isRaw()
         * @throws UnsupportedOperationException
         * @param thisGenericType raw-type to narrow
         * @param typeArguments
         * @return Narrowed type for given raw-type.
         */
        @Nonnull
        abstract T narrow(GenericType<T, D> thisGenericType, List<Type> typeArguments);

        @Nonnull
        abstract T erasure(GenericType<T, D> thisGenericType);
        abstract boolean isRaw();
        abstract boolean isNarrowed();

        @Nonnull
        abstract List<Type> typeArguments(GenericType<T, D> thisGenericType);

        /**
         * Type of enclosing definition that defines a context for current type.
         * Throws UnsupportedOperationException when there is no captured enclosing type.
         * @see Implementation#hasCapturedEnclosingType()
         * @throws UnsupportedOperationException
         * @return Type of enclosing definition
         */
        @Nonnull
        abstract GenericType<?, ?> getCapturedEnclosingType();

        abstract boolean hasCapturedEnclosingType();

        @Nonnull
        Implementation<T, D> substitute(Substitution nextSubstitution) {
            return new SubstitutedArgumentsImplementation<>(definition(), this, nextSubstitution);
        }

        @Nonnull
        final GenericDefinition<T, D> definition() {
            return definition;
        }

        @Nonnull
        final T createGenericType(GenericType<T, D> thisGenericType, Implementation<T, D> implementation) {
            return definition.createType(implementation);
        }
    }
    private static class SubstitutedArgumentsImplementation<T extends GenericType<T, D>, D extends GenericDefinition<T, D>>
            extends Implementation<T, D> {

        private final Implementation<T, D> original;
        private final Substitution substitution;
        private List<Type> typeArguments = null;
        SubstitutedArgumentsImplementation(GenericDefinition<T, D> factory, Implementation<T, D> original, Substitution substitution) {
            super(factory);
            this.original = original;
            this.substitution = substitution;
        }

        @Override
        T narrow(GenericType<T, D> thisGenericType, List<Type> typeArguments) {
            return original.narrow(thisGenericType, typeArguments);
        }

        @Override
        T erasure(GenericType<T, D> thisGenericType) {
            return original.erasure(thisGenericType);
        }

        @Override
        boolean isRaw() {
            return original.isRaw();
        }

        @Override
        boolean isNarrowed() {
            return original.isNarrowed();
        }

        @Override
        List<Type> typeArguments(GenericType<T, D> thisGenericType) {
            if (typeArguments == null) {
                typeArguments = new ArrayList<>();
                for (Type type: original.typeArguments(thisGenericType)) {
                    typeArguments.add(type.substitute(substitution));
                }
                typeArguments = Collections.unmodifiableList(typeArguments);
            }
            return typeArguments;
        }

        @Override
        boolean hasCapturedEnclosingType() {
            return original.hasCapturedEnclosingType();
        }

        @Override
        GenericType<?, ?> getCapturedEnclosingType() {
            return original.getCapturedEnclosingType();
        }

        @Override
        Implementation<T, D> substitute(Substitution nextSubstitution) {
            return new SubstitutedArgumentsImplementation<>(definition(), original, substitution.andThen(nextSubstitution));
        }
    }
    private static class Raw<T extends GenericType<T, D>, D extends GenericDefinition<T, D>> extends Implementation<T, D> {
        private List<Type> typeArguments = null;
        private final GenericType<?, ?> capturedEnclosingType;

        Raw(GenericDefinition<T, D> factory, @Nullable GenericType<?, ?> capturedEnclosingType) {
            super(factory);
            this.capturedEnclosingType = capturedEnclosingType;
        }

        @Override
        final T narrow(GenericType<T, D> thisGenericType, List<Type> typeArguments) {
            for (Type type: typeArguments) {
                if (!type.canBeTypeArgument())
                    throw new IllegalArgumentException(type.kind() + "can't be used as type-argument");
            }
            if (typeArguments.size() != thisGenericType.definition().typeParameters().all().size())
                throw new IllegalArgumentException("Type-argument list and type-parameter list differ in size");
            if (typeArguments.isEmpty())
                return thisGenericType.asSpecificType();
            else
                return createGenericType(thisGenericType, new Narrowed<>(definition(), thisGenericType.asSpecificType(), typeArguments));
        }

        @Override
        final boolean isRaw() {
            return true;
        }

        @Override
        final boolean isNarrowed() {
            return false;
        }

        @Override
        final List<Type> typeArguments(GenericType<T, D> thisGenericType) {
            if (typeArguments == null) {
                typeArguments = new ArrayList<>(thisGenericType.definition().typeParameters().all().size());
                for (TypeParameter typeParameter: thisGenericType.definition().typeParameters().all()) {
                    Type lowerRawBound;
                    try {
                        lowerRawBound = typeParameter.lowerRawBound();
                    } catch (CodeModelException ex) {
                        lowerRawBound = thisGenericType.definition().getCodeModel().objectType().asType();
                    }
                    typeArguments.add(lowerRawBound);
                }
                typeArguments = Collections.unmodifiableList(typeArguments);
            }
            return typeArguments;
        }

        @Override
        T erasure(GenericType<T, D> thisGenericType) {
            return thisGenericType.asSpecificType();
        }

        @Override
        boolean hasCapturedEnclosingType() {
            return capturedEnclosingType != null;
        }

        @Override
        GenericType<?, ?> getCapturedEnclosingType() {
            if (capturedEnclosingType == null)
                throw new UnsupportedOperationException("No captured enclosing type. See ");
            else
                return capturedEnclosingType;
        }
    }

    private static class Narrowed<T extends GenericType<T, D>, D extends GenericDefinition<T, D>> extends Implementation<T, D> {

        private final T erasure;
        private final List<Type> arguments;
        Narrowed(GenericDefinition<T, D> factory, T erasure, List<Type> arguments) {
            super(factory);
            if (arguments.isEmpty())
                throw new IllegalArgumentException("Type arguments shouldn't be empty");
            for (Type type: arguments) {
                if (!type.canBeTypeArgument())
                    throw new IllegalArgumentException(type.kind() + "can't be used as type-argument");
            }
            this.erasure = erasure;
            this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        }

        @Override
        public T erasure(GenericType<T, D> thisGenericType) {
            return erasure;
        }

        @Override
        public boolean isNarrowed() {
            return true;
        }

        @Override
        public boolean isRaw() {
            return false;
        }

        @Override
        public T narrow(GenericType<T, D> thisGenericType, List<Type> typeArguments) {
            throw new UnsupportedOperationException("Raw type expected");
        }

        @Override
        public List<Type> typeArguments(GenericType<T, D> thisGenericType) {
            return arguments;
        }

        @Override
        boolean hasCapturedEnclosingType() {
            return erasure.hasCapturedEnclosingType();
        }

        @Override
        GenericType<?, ?> getCapturedEnclosingType() {
            return erasure.getCapturedEnclosingType();
        }
    }
}
