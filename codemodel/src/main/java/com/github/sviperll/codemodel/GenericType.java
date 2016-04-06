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
import javax.annotation.Nullable;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 * @param <D>
 */
public abstract class GenericType<T extends Generic, D extends GenericDefinition<T, D>> {
    static <T extends Generic, D extends GenericDefinition<T, D>> T createRawType(final Factory<T, D> factory) {
        return factory.createGenericType(new Raw<>(factory, null));
    }
    static <T extends Generic, D extends GenericDefinition<T, D>> T createRawTypeDetails(GenericType<?, ?> capturedEnclosingType, final Factory<T, D> factory) {
        return factory.createGenericType(new Raw<>(factory, capturedEnclosingType));
    }

    private final Implementation<T, D> implementation;
    private Substitution definitionEnvironment = null;
    GenericType(Implementation<T, D> implementation) {
        this.implementation = implementation;
    }

    public abstract D definition();

    public final T erasure() {
        return implementation.erasure(this);
    }

    public final boolean isNarrowed() {
        return implementation.isNarrowed();
    }

    public final boolean isRaw() {
        return implementation.isRaw();
    }

    public final T narrow(List<Type> typeArguments) throws CodeModelException {
        return implementation.narrow(this, typeArguments);
    }

    public final List<Type> typeArguments() {
        return implementation.typeArguments(this);
    }

    public abstract T asType();

    /**
     * Type of enclosing definition that defines a context for current type.
     *
     * @return Type of enclosing definition or null for types with package-level or static member definitions.
     */
    @Nullable
    public final GenericType<?, ?> capturedEnclosingType() {
        return implementation.capturedEnclosingType();
    }

    final T substitute(Substitution substitution) {
        return implementation.createGenericType(implementation.substitute(substitution));
    }

    final Substitution definitionEnvironment() {
        if (definitionEnvironment == null) {
            GenericType<?, ?> enclosingType = capturedEnclosingType();
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
            result = enclosingType == null ? result : result.andThen(enclosingType.definitionEnvironment());
            definitionEnvironment = result;
        }
        return definitionEnvironment;
    }

    interface Factory<T extends Generic, D extends GenericDefinition<T, D>> {
        T createGenericType(Implementation<T, D> implementation);
    }
    static abstract class Implementation<T extends Generic, D extends GenericDefinition<T, D>> {
        private final Factory<T, D> factory;
        private Implementation(Factory<T, D> factory) {
            this.factory = factory;
        }
        abstract T narrow(GenericType<T, D> thisGenericType, List<Type> typeArguments) throws CodeModelException;
        abstract T erasure(GenericType<T, D> thisGenericType);
        abstract boolean isRaw();
        abstract boolean isNarrowed();
        abstract List<Type> typeArguments(GenericType<T, D> thisGenericType);
        abstract GenericType<?, ?> capturedEnclosingType();

        Implementation<T, D> substitute(Substitution nextSubstitution) {
            return new SubstitutedArgumentsImplementation<>(factory(), this, nextSubstitution);
        }

        final Factory<T, D> factory() {
            return factory;
        }
        final T createGenericType(Implementation<T, D> implementation) {
            return factory.createGenericType(implementation);
        }
    }
    private static class SubstitutedArgumentsImplementation<T extends Generic, D extends GenericDefinition<T, D>>
            extends Implementation<T, D> {

        private final Implementation<T, D> original;
        private final Substitution substitution;
        private List<Type> typeArguments = null;
        SubstitutedArgumentsImplementation(Factory<T, D> factory, Implementation<T, D> original, Substitution substitution) {
            super(factory);
            this.original = original;
            this.substitution = substitution;
        }

        @Override
        T narrow(GenericType<T, D> thisGenericType, List<Type> typeArguments) throws CodeModelException {
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
        GenericType<?, ?> capturedEnclosingType() {
            return original.capturedEnclosingType();
        }

        @Override
        Implementation<T, D> substitute(Substitution nextSubstitution) {
            return new SubstitutedArgumentsImplementation<>(factory(), original, substitution.andThen(nextSubstitution));
        }
    }
    private static class Raw<T extends Generic, D extends GenericDefinition<T, D>> extends Implementation<T, D> {
        private List<Type> typeArguments = null;
        private final GenericType<?, ?> capturedEnclosingType;

        Raw(Factory<T, D> factory, GenericType<?, ?> capturedEnclosingType) {
            super(factory);
            this.capturedEnclosingType = capturedEnclosingType;
        }

        @Override
        final T narrow(GenericType<T, D> thisGenericType, List<Type> typeArguments) throws CodeModelException {
            for (Type type: typeArguments) {
                if (!type.isArray() && !type.isWildcard() && !type.isObjectType() && !type.isTypeVariable())
                    throw new CodeModelException("Only array, wildcard, object type or type-variable can be used as type argument: found " + type.kind());
            }
            if (typeArguments.size() != thisGenericType.definition().typeParameters().all().size())
                throw new CodeModelException("Type-argument list and type-parameter list differ in size");
            return createGenericType(new Narrowed<>(factory(), thisGenericType, typeArguments));
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
                        lowerRawBound = thisGenericType.definition().getCodeModel().objectType();
                    }
                    typeArguments.add(lowerRawBound);
                }
                typeArguments = Collections.unmodifiableList(typeArguments);
            }
            return typeArguments;
        }

        @Override
        T erasure(GenericType<T, D> thisGenericType) {
            return thisGenericType.asType();
        }

        @Override
        GenericType<?, ?> capturedEnclosingType() {
            return capturedEnclosingType;
        }
    }

    private static class Narrowed<T extends Generic, D extends GenericDefinition<T, D>> extends Implementation<T, D> {

        private final GenericType<T, D> erasure;
        private final List<Type> arguments;
        Narrowed(Factory<T, D> factory, GenericType<T, D> erasure, List<Type> arguments) throws CodeModelException {
            super(factory);
            if (arguments.isEmpty())
                throw new CodeModelException("Type arguments shouldn't be empty");
            this.erasure = erasure;
            this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        }

        @Override
        public T erasure(GenericType<T, D> thisGenericType) {
            return erasure.asType();
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
        public T narrow(GenericType<T, D> thisGenericType, List<Type> typeArguments) throws CodeModelException {
            throw new UnsupportedOperationException("Raw type expected");
        }

        @Override
        public List<Type> typeArguments(GenericType<T, D> thisGenericType) {
            return arguments;
        }

        @Override
        GenericType<?, ?> capturedEnclosingType() {
            return erasure.capturedEnclosingType();
        }
    }
}
