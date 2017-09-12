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
import com.github.sviperll.codemold.util.CMCollectors;
import com.github.sviperll.codemold.util.Snapshot;
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
    static <T extends GenericType<T, D>, D extends GenericDefinition<T, D>> T createRawType(D definition) {
        return createRawType(definition, null);
    }
    @Nonnull
    static <T extends GenericType<T, D>, D extends GenericDefinition<T, D>> T createRawType(D definition, @Nullable GenericType<?, ?> capturedEnclosingType) {
        Raw<T, D> raw = new Raw<>(definition, capturedEnclosingType);
        return raw.erasure();
    }

    private final Implementation<T, D> implementation;
    private Substitution definitionEnvironment = null;
    GenericType(Implementation<T, D> implementation) {
        this.implementation = implementation;
    }

    @Nonnull
    public final D definition() {
        return implementation.definition();
    }

    @Nonnull
    public final T erasure() {
        return implementation.erasure();
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
    public final T narrow(List<? extends Type> typeArguments) {
        return implementation.narrow(typeArguments);
    }

    @Nonnull
    public final List<? extends AnyType> typeArguments() {
        return implementation.typeArguments();
    }

    final boolean hasCapturedEnclosingType() {
        return implementation.hasCapturedEnclosingType();
    }

    /**
     * AnyType of enclosing definition that defines a context for current type.
     * Throws UnsupportedOperationException when there is no captured enclosing type.
     * @see GenericType#hasCapturedEnclosingType()
     * @throws UnsupportedOperationException
     * @return AnyType of enclosing definition
     */
    @Nonnull
    final GenericType<?, ?> getCapturedEnclosingType() {
        return implementation.getCapturedEnclosingType();
    }

    @Nonnull
    final T substitute(Substitution substitution) {
        return definition().createType(implementation.substitute(substitution));
    }

    @Nonnull
    final Substitution definitionEnvironment() {
        if (definitionEnvironment == null) {
            Substitution.Builder builder;
            builder = Substitution.createBuilder();
            D definition = definition();
            Iterator<? extends TypeParameter> typeParameters = definition.typeParameters().all().iterator();
            Iterator<? extends AnyType> typeArguments = typeArguments().iterator();
            while (typeParameters.hasNext() && typeArguments.hasNext()) {
                TypeParameter typeParameter = typeParameters.next();
                AnyType typeArgument = typeArguments.next();
                builder.put(typeParameter.name(), typeArgument);
            }
            Substitution result = builder.build();
            result = !hasCapturedEnclosingType() ? result : result.andThen(getCapturedEnclosingType().definitionEnvironment());
            definitionEnvironment = result;
        }
        return definitionEnvironment;
    }

    static abstract class Implementation<T extends GenericType<T, D>, D extends GenericDefinition<T, D>> {
        private final D definition;
        private Implementation(D definition) {
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
        abstract T narrow(List<? extends Type> typeArguments);

        @Nonnull
        abstract T erasure();
        abstract boolean isRaw();
        abstract boolean isNarrowed();

        @Nonnull
        abstract List<? extends AnyType> typeArguments();

        /**
         * AnyType of enclosing definition that defines a context for current type.
         * Throws UnsupportedOperationException when there is no captured enclosing type.
         * @see Implementation#hasCapturedEnclosingType()
         * @throws UnsupportedOperationException
         * @return AnyType of enclosing definition
         */
        @Nonnull
        abstract GenericType<?, ?> getCapturedEnclosingType();

        abstract boolean hasCapturedEnclosingType();

        @Nonnull
        Implementation<T, D> substitute(Substitution nextSubstitution) {
            return new SubstitutedArgumentsImplementation<>(definition(), this, nextSubstitution);
        }

        @Nonnull
        final D definition() {
            return definition;
        }
    }
    private static class SubstitutedArgumentsImplementation<T extends GenericType<T, D>, D extends GenericDefinition<T, D>>
            extends Implementation<T, D> {

        private final Implementation<T, D> original;
        private final Substitution substitution;
        private List<? extends AnyType> typeArguments = null;
        SubstitutedArgumentsImplementation(D factory, Implementation<T, D> original, Substitution substitution) {
            super(factory);
            this.original = original;
            this.substitution = substitution;
        }

        @Nonnull
        @Override
        T narrow(List<? extends Type> typeArguments) {
            return original.narrow(typeArguments);
        }

        @Nonnull
        @Override
        T erasure() {
            return original.erasure();
        }

        @Override
        boolean isRaw() {
            return original.isRaw();
        }

        @Override
        boolean isNarrowed() {
            return original.isNarrowed();
        }

        @Nonnull
        @Override
        List<? extends AnyType> typeArguments() {
            if (typeArguments == null) {
                typeArguments = original.typeArguments().stream()
                        .map(ta -> ta.substitute(substitution))
                        .collect(CMCollectors.toImmutableList());
            }
            return Snapshot.of(typeArguments);
        }

        @Override
        boolean hasCapturedEnclosingType() {
            return original.hasCapturedEnclosingType();
        }

        @Nonnull
        @Override
        GenericType<?, ?> getCapturedEnclosingType() {
            return original.getCapturedEnclosingType();
        }

        @Nonnull
        @Override
        Implementation<T, D> substitute(Substitution nextSubstitution) {
            return new SubstitutedArgumentsImplementation<>(definition(), original, substitution.andThen(nextSubstitution));
        }
    }
    private static class Raw<T extends GenericType<T, D>, D extends GenericDefinition<T, D>> extends Implementation<T, D> {
        private List<? extends AnyType> typeArguments = null;
        private final GenericType<?, ?> capturedEnclosingType;
        private T instance = null;

        Raw(D factory, @Nullable GenericType<?, ?> capturedEnclosingType) {
            super(factory);
            this.capturedEnclosingType = capturedEnclosingType;
        }

        @Nonnull
        @Override
        final T narrow(List<? extends Type> typeArguments) {
            List<AnyType> castedTypeArguments = CMCollections.newArrayList();
            for (Type type: typeArguments) {
                AnyType anyType = type.asAny();
                if (!anyType.canBeTypeArgument())
                    throw new IllegalArgumentException(anyType.kind() + "can't be used as type-argument");
                castedTypeArguments.add(anyType);
            }
            if (typeArguments.size() != definition().typeParameters().all().size())
                throw new IllegalArgumentException("Type-argument list and type-parameter list differ in size");
            if (typeArguments.isEmpty())
                return instance;
            else
                return definition().createType(new Narrowed<>(definition(), instance, Snapshot.of(castedTypeArguments)));
        }

        @Override
        final boolean isRaw() {
            return true;
        }

        @Override
        final boolean isNarrowed() {
            return false;
        }

        @Nonnull
        @Override
        final List<? extends AnyType> typeArguments() {
            if (typeArguments == null) {
                typeArguments = definition().typeParameters().all().stream()
                        .map((TypeParameter typeParameter) -> {
                            AnyType lowerRawBound;
                            try {
                                return typeParameter.lowerRawBound();
                            } catch (CodeMoldException ex) {
                                return definition().residence().getCodeMold().objectType().asAny();
                            }
                        }).collect(CMCollectors.toImmutableList());
            }
            return Snapshot.of(typeArguments);
        }

        @Nonnull
        @Override
        T erasure() {
            if (instance == null) {
                this.instance = definition().createType(this);
            }
            return instance;
        }

        @Override
        boolean hasCapturedEnclosingType() {
            return capturedEnclosingType != null;
        }

        @Nonnull
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
        private final List<? extends AnyType> arguments;
        Narrowed(D factory, T erasure, List<? extends AnyType> arguments) {
            super(factory);
            if (arguments.isEmpty())
                throw new IllegalArgumentException("Type arguments shouldn't be empty");
            for (AnyType type: arguments) {
                if (!type.canBeTypeArgument())
                    throw new IllegalArgumentException(type.kind() + "can't be used as type-argument");
            }
            this.erasure = erasure;
            this.arguments = Snapshot.of(arguments);
        }

        @Nonnull
        @Override
        public T erasure() {
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

        @Nonnull
        @Override
        public T narrow(List<? extends Type> typeArguments) {
            throw new UnsupportedOperationException("Raw type expected");
        }

        @Nonnull
        @Override
        public List<? extends AnyType> typeArguments() {
            return arguments;
        }

        @Override
        boolean hasCapturedEnclosingType() {
            return erasure.hasCapturedEnclosingType();
        }

        @Nonnull
        @Override
        GenericType<?, ?> getCapturedEnclosingType() {
            return erasure.getCapturedEnclosingType();
        }
    }
}
