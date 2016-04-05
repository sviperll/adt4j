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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class Type implements Renderable, Generic {
    private static final VoidType VOID = new VoidType();
    private static final Type BYTE = Type.primitive(PrimitiveType.BYTE);
    private static final Type SHORT = Type.primitive(PrimitiveType.SHORT);
    private static final Type INT = Type.primitive(PrimitiveType.INT);
    private static final Type LONG = Type.primitive(PrimitiveType.LONG);
    private static final Type FLOAT = Type.primitive(PrimitiveType.FLOAT);
    private static final Type DOUBLE = Type.primitive(PrimitiveType.DOUBLE);
    private static final Type CHAR = Type.primitive(PrimitiveType.CHAR);
    private static final Type BOOLEAN = Type.primitive(PrimitiveType.BOOLEAN);

    public static Type variable(String name) {
        return variable(new TypeVariable(name));
    }

    public static Type voidType() {
        return VOID;
    }

    public static Type byteType() {
        return BYTE;
    }

    public static Type shortType() {
        return SHORT;
    }

    public static Type intType() {
        return INT;
    }

    public static Type longType() {
        return LONG;
    }

    public static Type floatType() {
        return FLOAT;
    }

    public static Type doubleType() {
        return DOUBLE;
    }

    public static Type charType() {
        return CHAR;
    }

    public static Type booleanType() {
        return BOOLEAN;
    }

    public static Type intersection(Collection<Type> bounds) throws CodeModelException {
        return intersection(new IntersectionType(bounds));
    }

    static Type variable(TypeVariable details) {
        return new TypeVariableWrapper(details);
    }

    static Type intersection(IntersectionType details) {
        return new IntersectionTypeWrapper(details);
    }

    static Type createObjectType(ObjectType typeDetails) {
        return new ObjectTypeWrapper(typeDetails);
    }

    static Type array(ArrayType details) {
        return new ArrayTypeWrapper(details);
    }

    static Type wildcard(WildcardType details) {
        return new WildcardTypeWrapper(details);
    }

    private static Type primitive(final PrimitiveType details) {
        return new PrimitiveTypeWrapper(details);
    }

    private Type() {
    }

    Type substitute(Substitution environment) {
        if (isTypeVariable()) {
            Type replacement = environment.get(getVariableDetails().name());
            if (replacement != null)
                return replacement;
            else
                return this;
        } else if (isObjectType()) {
            return getObjectDetails().substitute(environment);
        } else if (isArray()) {
            return getArrayDetails().substitute(environment);
        } else if (isIntersection()) {
            return getIntersectionDetails().substitute(environment);
        } else if (isWildcard()) {
            return getWildcardDetails().substitute(environment);
        } else
            return this;
    }

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

    public ObjectType getObjectDetails() {
        throw new UnsupportedOperationException("Object type expected");
    }

    public WildcardType getWildcardDetails() {
        throw new UnsupportedOperationException("Wildcard type expected");
    }

    public PrimitiveType getPrimitiveDetails() {
        throw new UnsupportedOperationException("Primitive type expected");
    }

    public ArrayType getArrayDetails() {
        throw new UnsupportedOperationException("Array type expected");
    }

    public TypeVariable getVariableDetails() {
        throw new UnsupportedOperationException("Type variable expected");
    }

    public IntersectionType getIntersectionDetails() {
        throw new UnsupportedOperationException("Intersection type expected");
    }

    public MethodType getExecutableDetails() {
        throw new UnsupportedOperationException("Executable type expected");
    }

    public boolean containsWildcards() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Collection<Type> asListOfIntersectedTypes() {
        return isIntersection() ? getIntersectionDetails().intersectedTypes() : Collections.singletonList(this);
    }

    @Override
    public GenericType<?, ?> getGenericDetails() {
        if (isObjectType())
            return getObjectDetails();
        else
            throw new UnsupportedOperationException("Generic type expected");
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                if (isArray()) {
                    context.appendRenderable(getArrayDetails().elementType());
                    context.appendText("[]");
                } else if (isIntersection()) {
                    Iterator<Type> iterator = getIntersectionDetails().intersectedTypes().iterator();
                    if (iterator.hasNext()) {
                        context.appendRenderable(iterator.next());
                        while (iterator.hasNext()) {
                            context.appendText(" & ");
                            context.appendRenderable(iterator.next());
                        }
                    }
                } else if (isVoid()) {
                    context.appendText("void");
                } else if (isPrimitive()) {
                    context.appendText(getPrimitiveDetails().name().toLowerCase(Locale.US));
                } else if (isTypeVariable()) {
                    context.appendText(getVariableDetails().name());
                } else if (isWildcard()) {
                    WildcardType wildcard = getWildcardDetails();
                    context.appendText("?");
                    context.appendWhiteSpace();
                    context.appendText(wildcard.boundKind().name().toLowerCase(Locale.US));
                    context.appendWhiteSpace();
                    context.appendRenderable(wildcard.bound());
                } else if (isObjectType()) {
                    ObjectType objectType = getObjectDetails();
                    if (objectType.isRaw())
                        context.appendQualifiedClassName(objectType.definition().qualifiedName());
                    else {
                        context.appendRenderable(objectType.erasure());
                        Iterator<Type> iterator = objectType.typeArguments().iterator();
                        if (iterator.hasNext()) {
                            context.appendText("<");
                            context.appendRenderable(iterator.next());
                            while (iterator.hasNext()) {
                                context.appendText(", ");
                                context.appendRenderable(iterator.next());
                            }
                            context.appendText(">");
                        }
                    }
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
