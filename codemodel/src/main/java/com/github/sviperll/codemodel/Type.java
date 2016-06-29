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
import com.github.sviperll.codemodel.render.Renderer;
import com.github.sviperll.codemodel.render.RendererContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class Type implements Renderable {
    private static final VoidType VOID = new VoidType();
    private static final Type BYTE = PrimitiveType.BYTE.asType();
    private static final Type SHORT = PrimitiveType.SHORT.asType();
    private static final Type INT = PrimitiveType.INT.asType();
    private static final Type LONG = PrimitiveType.LONG.asType();
    private static final Type FLOAT = PrimitiveType.FLOAT.asType();
    private static final Type DOUBLE = PrimitiveType.DOUBLE.asType();
    private static final Type CHAR = PrimitiveType.CHAR.asType();
    private static final Type BOOLEAN = PrimitiveType.BOOLEAN.asType();

    public static TypeVariable variable(String name) {
        return new TypeVariable(name);
    }

    @Nonnull
    public static Type voidType() {
        return VOID;
    }

    @Nonnull
    public static Type byteType() {
        return BYTE;
    }

    @Nonnull
    public static Type shortType() {
        return SHORT;
    }

    @Nonnull
    public static Type intType() {
        return INT;
    }

    @Nonnull
    public static Type longType() {
        return LONG;
    }

    @Nonnull
    public static Type floatType() {
        return FLOAT;
    }

    @Nonnull
    public static Type doubleType() {
        return DOUBLE;
    }

    @Nonnull
    public static Type charType() {
        return CHAR;
    }

    @Nonnull
    public static Type booleanType() {
        return BOOLEAN;
    }

    @Nonnull
    public static IntersectionType intersection(Collection<? extends ObjectType> bounds) {
        return new IntersectionType(bounds);
    }

    @Nonnull
    static Type wrapVariableType(TypeVariable details) {
        return new TypeVariableWrapper(details);
    }

    @Nonnull
    static Type wrapIntersectionType(IntersectionType details) {
        return new IntersectionTypeWrapper(details);
    }

    @Nonnull
    static Type wrapObjectType(ObjectType typeDetails) {
        return new ObjectTypeWrapper(typeDetails);
    }

    @Nonnull
    static Type wrapArrayType(ArrayType details) {
        return new ArrayTypeWrapper(details);
    }

    @Nonnull
    static Type wrapWildcardType(WildcardType details) {
        return new WildcardTypeWrapper(details);
    }

    @Nonnull
    static Type wrapPrimitiveType(final PrimitiveType details) {
        return new PrimitiveTypeWrapper(details);
    }

    @Nonnull
    public static ArrayType arrayOf(Type componentType) {
        return new ArrayType(componentType);
    }

    @Nonnull
    public static WildcardType wildcardExtends(Type bound) {
        return new WildcardType(WildcardType.BoundKind.EXTENDS, bound);
    }

    @Nonnull
    public static WildcardType wildcardSuper(Type bound) {
        return new WildcardType(WildcardType.BoundKind.SUPER, bound);
    }

    private Type() {
    }
    @Nonnull
    Type substitute(Substitution environment) {
        if (isTypeVariable()) {
            Type replacement = environment.getOrDefault(getVariableDetails().name(), null);
            if (replacement != null)
                return replacement;
            else
                return this;
        } else if (isObjectType()) {
            return getObjectDetails().substitute(environment).asType();
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
     * @see Type#isObjectType()
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
     * @see Type#isWildcard()
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
     * @see Type#isPrimitive()
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
     * @see Type#isArray()
     * @throws UnsupportedOperationException
     * @return Array-type details.
     */
    @Nonnull
    public ArrayType getArrayDetails() {
        throw new UnsupportedOperationException("Array type expected");
    }

    /**
     * Type-variable details.
     * Throws UnsupportedOperationException for other types than Type-variables
     * @see Type#isTypeVariable()
     * @throws UnsupportedOperationException
     * @return Type-variable details.
     */
    @Nonnull
    public TypeVariable getVariableDetails() {
        throw new UnsupportedOperationException("Type variable expected");
    }

    /**
     * Intersection-type details.
     * Throws UnsupportedOperationException for other types than Intersection-types
     * @see Type#isIntersection()
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
    public Collection<? extends Type> toListOfIntersectedTypes() {
        if (!isIntersection())
            return Collections.singletonList(this);
        else {
            List<Type> result = new ArrayList<>();
            for (ObjectType type: getIntersectionDetails().intersectedTypes())
                result.add(type.asType());
            return Collections.unmodifiableList(result);
        }
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
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
            }
        };
    }

    public enum Kind {
        VOID, OBJECT, PRIMITIVE, ARRAY, TYPE_VARIABLE, WILDCARD, INTERSECTION
    }

    private static class VoidType extends Type {
        @Override
        public Kind kind() {
            return Kind.VOID;
        }
    }

    private static class TypeVariableWrapper extends Type {

        private final TypeVariable details;

        TypeVariableWrapper(TypeVariable details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.TYPE_VARIABLE;
        }

        @Override
        public TypeVariable getVariableDetails() {
            return details;
        }
    }
    private static class IntersectionTypeWrapper extends Type {

        private final IntersectionType details;

        IntersectionTypeWrapper(IntersectionType details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.INTERSECTION;
        }

        @Override
        public IntersectionType getIntersectionDetails() {
            return details;
        }
    }

    private static class ObjectTypeWrapper extends Type {

        private final ObjectType details;

        ObjectTypeWrapper(ObjectType details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.OBJECT;
        }

        @Override
        public ObjectType getObjectDetails() {
            return details;
        }
    }

    private static class PrimitiveTypeWrapper extends Type {

        private final PrimitiveType details;

        PrimitiveTypeWrapper(PrimitiveType details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.PRIMITIVE;
        }
        @Override
        public PrimitiveType getPrimitiveDetails() {
            return details;
        }
    }

    private static class ArrayTypeWrapper extends Type {

        private final ArrayType details;

        ArrayTypeWrapper(ArrayType details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.ARRAY;
        }

        @Override
        public ArrayType getArrayDetails() {
            return details;
        }
    }

    private static class WildcardTypeWrapper extends Type {

        private final WildcardType details;

        WildcardTypeWrapper(WildcardType details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.WILDCARD;
        }

        @Override
        public WildcardType getWildcardDetails() {
            return details;
        }
    }


}
