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
public abstract class GenericType<T extends Generic, D extends GenericDefinition<T>> {
    static <T extends Generic> T createRawTypeDetails(final Factory<T> factory) {
        return factory.createGenericType(new Parametrization<T>() {
            @Override
            Implementation<T> createImplementation(GenericType<T, ?> typeDetails) {
                return new Raw<>(typeDetails, factory);
            }
        });
    }

    private final Implementation<T> implementation;
    GenericType(Parametrization<T> implementationFactory) {
        this.implementation = implementationFactory.createImplementation(this);
    }

    public abstract D definition();

    public final T erasure() {
        return implementation.erasure();
    }

    public final boolean isNarrowed() {
        return implementation.isNarrowed();
    }

    public final boolean isRaw() {
        return implementation.isRaw();
    }

    public final T narrow(List<Type> typeArguments) throws CodeModelException {
        return implementation.narrow(typeArguments);
    }

    public final List<Type> typeArguments() {
        return implementation.typeArguments();
    }

    public abstract T asType();

    /**
     * Type of enclosing definition that defines a context for current type.
     *
     * @return Type of enclosing definition or null for types with package-level or static member definitions.
     */
    @Nullable
    public abstract GenericType<?, ?> capturedEnclosingType();

    final TypeEnvironment definitionEnvironment() {
        GenericType<?, ?> enclosingType = capturedEnclosingType();
        TypeEnvironment.Builder builder;
        if (enclosingType == null)
            builder = TypeEnvironment.createBuilder();
        else
            builder = TypeEnvironment.createBuilder(enclosingType.definitionEnvironment());
        D definition = definition();
        Iterator<TypeParameter> typeParameters = definition.typeParameters().all().iterator();
        Iterator<Type> typeArguments = typeArguments().iterator();
        while (typeParameters.hasNext() && typeArguments.hasNext()) {
            TypeParameter typeParameter = typeParameters.next();
            Type typeArgument = typeArguments.next();
            builder.put(typeParameter.name(), typeArgument);
        }
        return builder.build();
    }

    interface Factory<T extends Generic> {
        T createGenericType(Parametrization<T> parametrization);
    }

    static abstract class Parametrization<T extends Generic> {
        private Parametrization() {
        }
        abstract Implementation<T> createImplementation(GenericType<T, ?> instance);
    }
    private static class IdentityImplementationFactory<T extends Generic> extends Parametrization<T> {

        private final Implementation<T> implementation;
        IdentityImplementationFactory(Implementation<T> implementation) {
            this.implementation = implementation;
        }
        @Override
        Implementation<T> createImplementation(GenericType<T, ?> instance) {
            return implementation;
        }
    }
    private static abstract class Implementation<T> {
        private Implementation() {
        }
        abstract T narrow(List<Type> typeArguments) throws CodeModelException;
        abstract T erasure();
        abstract boolean isRaw();
        abstract boolean isNarrowed();
        abstract List<Type> typeArguments();
    }
    private static class Raw<T extends Generic> extends Implementation<T> {
        private List<Type> typeArguments = null;
        private final GenericType<T, ?> genericTypeDetails;
        private final Factory<T> factory;

        Raw(GenericType<T, ?> genericTypeDetails, Factory<T> factory) {
            this.genericTypeDetails = genericTypeDetails;
            this.factory = factory;
        }

        @Override
        final T narrow(List<Type> typeArguments) throws CodeModelException {
            for (Type type: typeArguments) {
                if (type.isVoid())
                    throw new CodeModelException("Void can't be used as a type-argument");
                if (type.isPrimitive())
                    throw new CodeModelException("Primitive type can't be used as a type-argument");
                if (type.isIntersection())
                    throw new CodeModelException("Intersection can't be used as type-argument");
                if (!type.isArray() && !type.isWildcard() && !type.isObjectType())
                    throw new CodeModelException("Only array, wildcard or object type can be used as type argument: found " + type.kind());
            }
            if (typeArguments.size() != genericTypeDetails.definition().typeParameters().all().size())
                throw new CodeModelException("Type-argument list and type-parameter list differ in size");
            return factory.createGenericType(new IdentityImplementationFactory<>(new Narrowed<>(genericTypeDetails, typeArguments)));
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
        final List<Type> typeArguments() {
            if (typeArguments == null) {
                typeArguments = new ArrayList<>(genericTypeDetails.definition().typeParameters().all().size());
                for (TypeParameter typeParameter: genericTypeDetails.definition().typeParameters().all()) {
                    Type lowerRawBound;
                    try {
                        lowerRawBound = typeParameter.lowerRawBound();
                    } catch (CodeModelException ex) {
                        lowerRawBound = genericTypeDetails.definition().getCodeModel().objectType();
                    }
                    typeArguments.add(lowerRawBound);
                }
            }
            return typeArguments;
        }

        @Override
        T erasure() {
            return genericTypeDetails.asType();
        }
    }

    private static class Narrowed<T extends Generic> extends Implementation<T> {

        private final GenericType<T, ?> erasure;
        private final List<Type> arguments;
        Narrowed(GenericType<T, ?> erasure, List<Type> arguments) throws CodeModelException {
            if (arguments.isEmpty())
                throw new CodeModelException("Type arguments shouldn't be empty");
            this.erasure = erasure;
            this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        }

        @Override
        public T erasure() {
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
        public T narrow(List<Type> typeArguments) throws CodeModelException {
            throw new UnsupportedOperationException("Raw type expected");
        }

        @Override
        public List<Type> typeArguments() {
            return arguments;
        }

    }
}
