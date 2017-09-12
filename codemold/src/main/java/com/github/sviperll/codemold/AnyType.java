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
import com.github.sviperll.codemold.render.Renderer;
import com.github.sviperll.codemold.render.RendererContext;
import com.github.sviperll.codemold.util.CMCollectors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class AnyType implements Renderable, Type {
    private static final VoidType VOID = new VoidType();

    @Nonnull
    static AnyType voidType() {
        return VOID;
    }

    @Nonnull
    static AnyType wrapVariableType(TypeVariable.Wrappable wrappable) {
        return new TypeVariableWrapper(wrappable.value());
    }

    @Nonnull
    static AnyType wrapIntersectionType(IntersectionType.Wrappable wrappable) {
        return new IntersectionTypeWrapper(wrappable.value());
    }

    @Nonnull
    static AnyType wrapObjectType(ObjectType.Wrappable wrappable) {
        return new ObjectTypeWrapper(wrappable.value());
    }

    @Nonnull
    static AnyType wrapArrayType(ArrayType.Wrappable wrappable) {
        return new ArrayTypeWrapper(wrappable.value());
    }

    @Nonnull
    static AnyType wrapWildcardType(WildcardType.Wrappable wrappable) {
        return new WildcardTypeWrapper(wrappable.value());
    }

    @Nonnull
    static AnyType wrapPrimitiveType(PrimitiveType.Wrappable wrappable) {
        return new PrimitiveTypeWrapper(wrappable.value());
    }

    private AnyType() {
    }
    @Nonnull
    AnyType substitute(Substitution environment) {
        if (isTypeVariable()) {
            return environment.get(getVariableDetails().name()).orElse(this);
        } else if (isObjectType()) {
            return getObjectDetails().substitute(environment).asAny();
        } else if (isArray()) {
            return getArrayDetails().substitute(environment);
        } else if (isIntersection()) {
            return getIntersectionDetails().substitute(environment);
        } else if (isWildcard()) {
            return getWildcardDetails().substitute(environment);
        } else
            return this;
    }

    @Nonnull
    public abstract Kind kind();

    @Nonnull
    @Override
    public final AnyType asAny() {
        return this;
    }

    public final boolean isVoid() {
        return kind() == Kind.VOID;
    }

    public final boolean isObjectType() {
        return kind() == Kind.OBJECT;
    }

    public final boolean isPrimitive() {
        return kind() == Kind.PRIMITIVE;
    }

    public final boolean isArray() {
        return kind() == Kind.ARRAY;
    }

    public final boolean isTypeVariable() {
        return kind() == Kind.TYPE_VARIABLE;
    }

    public final boolean isWildcard() {
        return kind() == Kind.WILDCARD;
    }

    public final boolean isIntersection() {
        return kind() == Kind.INTERSECTION;
    }

    public final boolean canBeTypeArgument() {
        return isArray() || isWildcard() || isObjectType() || isTypeVariable();
    }

    public final boolean canBeMethodResult() {
        return isArray() || isObjectType() || isPrimitive() || isTypeVariable() || isVoid();
    }

    public final boolean canBeDeclaredVariableType() {
        return isArray() || isObjectType() || isPrimitive() || isTypeVariable();
    }

    public final boolean canBeTypeVariableBound() {
        return isIntersection() || isObjectType() || isTypeVariable();
    }

    /**
     * Object-type details.
     * Throws UnsupportedOperationException for other types than object-types
     * @see AnyType#isObjectType()
     * @throws UnsupportedOperationException
     * @return Object-type details.
     */
    @Nonnull
    public ObjectType getObjectDetails() {
        throw new UnsupportedOperationException("Object type expected");
    }

    /**
     * Wildcard-type details.
     * Throws UnsupportedOperationException for other types than Wildcard-types
     * @see AnyType#isWildcard()
     * @throws UnsupportedOperationException
     * @return Wildcard-type details.
     */
    @Nonnull
    public WildcardType getWildcardDetails() {
        throw new UnsupportedOperationException("Wildcard type expected");
    }

    /**
     * Primitive-type details.
     * Throws UnsupportedOperationException for other types than Primitive-types
     * @see AnyType#isPrimitive()
     * @throws UnsupportedOperationException
     * @return Primitive-type details.
     */
    @Nonnull
    public PrimitiveType getPrimitiveDetails() {
        throw new UnsupportedOperationException("Primitive type expected");
    }

    /**
     * Array-type details.
     * Throws UnsupportedOperationException for other types than Array-types
     * @see AnyType#isArray()
     * @throws UnsupportedOperationException
     * @return Array-type details.
     */
    @Nonnull
    public ArrayType getArrayDetails() {
        throw new UnsupportedOperationException("Array type expected");
    }

    /**
     * AnyType-variable details.
     * Throws UnsupportedOperationException for other types than AnyType-variables
     * @see AnyType#isTypeVariable()
     * @throws UnsupportedOperationException
     * @return AnyType-variable details.
     */
    @Nonnull
    public TypeVariable getVariableDetails() {
        throw new UnsupportedOperationException("Type variable expected");
    }

    /**
     * Intersection-type details.
     * Throws UnsupportedOperationException for other types than Intersection-types
     * @see AnyType#isIntersection()
     * @throws UnsupportedOperationException
     * @return Intersection-type details.
     */
    @Nonnull
    public IntersectionType getIntersectionDetails() {
        throw new UnsupportedOperationException("Intersection type expected");
    }

    public boolean containsWildcards() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Nonnull
    public Collection<? extends AnyType> toListOfIntersectedTypes() {
        if (!isIntersection())
            return Collections.singletonList(this);
        else {
            return getIntersectionDetails().intersectedTypes()
                    .stream()
                    .map(ObjectType::asAny)
                    .collect(CMCollectors.toImmutableList());
        }
    }

    @Nonnull
    @Override
    public Renderer createRenderer(final RendererContext context) {
        return () -> {
            if (isArray()) {
                context.appendRenderable(getArrayDetails());
            } else if (isIntersection()) {
                context.appendRenderable(getIntersectionDetails());
            } else if (isVoid()) {
                context.appendText("void");
            } else if (isPrimitive()) {
                context.appendRenderable(getPrimitiveDetails());
            } else if (isTypeVariable()) {
                context.appendRenderable(getVariableDetails());
            } else if (isWildcard()) {
                context.appendRenderable(getWildcardDetails());
            } else if (isObjectType()) {
                context.appendRenderable(getObjectDetails());
            }
        };
    }

    public enum Kind {
        VOID, OBJECT, PRIMITIVE, ARRAY, TYPE_VARIABLE, WILDCARD, INTERSECTION
    }

    private static class VoidType extends AnyType {
        @Nonnull
        @Override
        public Kind kind() {
            return Kind.VOID;
        }
    }

    private static class TypeVariableWrapper extends AnyType {

        private final TypeVariable details;

        TypeVariableWrapper(TypeVariable details) {
            this.details = details;
        }

        @Nonnull
        @Override
        public Kind kind() {
            return Kind.TYPE_VARIABLE;
        }

        @Nonnull
        @Override
        public TypeVariable getVariableDetails() {
            return details;
        }
    }
    private static class IntersectionTypeWrapper extends AnyType {

        private final IntersectionType details;

        IntersectionTypeWrapper(IntersectionType details) {
            this.details = details;
        }

        @Nonnull
        @Override
        public Kind kind() {
            return Kind.INTERSECTION;
        }

        @Nonnull
        @Override
        public IntersectionType getIntersectionDetails() {
            return details;
        }
    }

    private static class ObjectTypeWrapper extends AnyType {

        private final ObjectType details;

        ObjectTypeWrapper(ObjectType details) {
            this.details = details;
        }

        @Nonnull
        @Override
        public Kind kind() {
            return Kind.OBJECT;
        }

        @Nonnull
        @Override
        public ObjectType getObjectDetails() {
            return details;
        }
    }

    private static class PrimitiveTypeWrapper extends AnyType {

        private final PrimitiveType details;

        PrimitiveTypeWrapper(PrimitiveType details) {
            this.details = details;
        }

        @Nonnull
        @Override
        public Kind kind() {
            return Kind.PRIMITIVE;
        }
        @Nonnull
        @Override
        public PrimitiveType getPrimitiveDetails() {
            return details;
        }
    }

    private static class ArrayTypeWrapper extends AnyType {

        private final ArrayType details;

        ArrayTypeWrapper(ArrayType details) {
            this.details = details;
        }

        @Nonnull
        @Override
        public Kind kind() {
            return Kind.ARRAY;
        }

        @Nonnull
        @Override
        public ArrayType getArrayDetails() {
            return details;
        }
    }

    private static class WildcardTypeWrapper extends AnyType {

        private final WildcardType details;

        WildcardTypeWrapper(WildcardType details) {
            this.details = details;
        }

        @Nonnull
        @Override
        public Kind kind() {
            return Kind.WILDCARD;
        }

        @Nonnull
        @Override
        public WildcardType getWildcardDetails() {
            return details;
        }
    }


}
