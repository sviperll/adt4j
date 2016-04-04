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
public abstract class Type implements Renderable {
    private static final VoidType VOID = new VoidType();
    private static final Type BYTE = Type.primitive(PrimitiveTypeDetails.BYTE);
    private static final Type SHORT = Type.primitive(PrimitiveTypeDetails.SHORT);
    private static final Type INT = Type.primitive(PrimitiveTypeDetails.INT);
    private static final Type LONG = Type.primitive(PrimitiveTypeDetails.LONG);
    private static final Type FLOAT = Type.primitive(PrimitiveTypeDetails.FLOAT);
    private static final Type DOUBLE = Type.primitive(PrimitiveTypeDetails.DOUBLE);
    private static final Type CHAR = Type.primitive(PrimitiveTypeDetails.CHAR);
    private static final Type BOOLEAN = Type.primitive(PrimitiveTypeDetails.BOOLEAN);

    public static Type variable(String name) {
        return variable(new TypeVariableDetails(name));
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
        return intersection(new IntersectionTypeDetails(bounds));
    }

    static Type variable(TypeVariableDetails details) {
        return new TypeVariable(details);
    }

    static Type intersection(IntersectionTypeDetails details) {
        return new IntersectionType(details);
    }

    static Type createObjectType(ObjectTypeDetails typeDetails) {
        return new ObjectType(typeDetails);
    }

    static Type executable(ExecutableTypeDetails details) {
        return new ExecutableType(details);
    }

    static Type array(ArrayTypeDetails details) {
        return new ArrayType(details);
    }

    static Type wildcard(WildcardTypeDetails details) {
        return new WildcardType(details);
    }

    private static Type primitive(final PrimitiveTypeDetails details) {
        return new PrimitiveType(details);
    }

    private Type() {
    }

    Type inEnvironment(TypeEnvironment environment) {
        if (isTypeVariable()) {
            Type replacement = environment.get(getVariableDetails().name());
            if (replacement != null)
                return replacement;
            else
                return this;
        } else if (isObjectType()) {
            return getObjectDetails().inEnvironment(environment).asType();
        } else if (isArray()) {
            return getArrayDetails().inEnvironment(environment).asType();
        } else if (isExecutable()) {
            return getExecutableDetails().inEnvironment(environment).asType();
        } else if (isIntersection()) {
            return getIntersectionDetails().inEnvironment(environment).asType();
        } else if (isWildcard()) {
            return getWildcardDetails().inEnvironment(environment).asType();
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

    public final boolean isExecutable() {
        return kind() == Kind.EXECUTABLE;
    }

    public ObjectTypeDetails getObjectDetails() {
        throw new UnsupportedOperationException("Object type expected");
    }

    public WildcardTypeDetails getWildcardDetails() {
        throw new UnsupportedOperationException("Wildcard type expected");
    }

    public PrimitiveTypeDetails getPrimitiveDetails() {
        throw new UnsupportedOperationException("Primitive type expected");
    }

    public ArrayTypeDetails getArrayDetails() {
        throw new UnsupportedOperationException("Array type expected");
    }

    public TypeVariableDetails getVariableDetails() {
        throw new UnsupportedOperationException("Type variable expected");
    }

    public IntersectionTypeDetails getIntersectionDetails() {
        throw new UnsupportedOperationException("Intersection type expected");
    }

    public ExecutableTypeDetails getExecutableDetails() {
        throw new UnsupportedOperationException("Executable type expected");
    }

    GenericTypeDetails<?> getGenericTypeDetails() {
        if (isObjectType())
            return getObjectDetails();
        else if (isExecutable())
            return getExecutableDetails();
        else
            throw new UnsupportedOperationException("Object or executable type expected.");
    }

    public boolean containsWildcards() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Collection<Type> asListOfIntersectedTypes() {
        return isIntersection() ? getIntersectionDetails().intersectedTypes() : Collections.singletonList(this);
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
                    WildcardTypeDetails wildcard = getWildcardDetails();
                    context.appendText("?");
                    context.appendWhiteSpace();
                    context.appendText(wildcard.boundKind().name().toLowerCase(Locale.US));
                    context.appendWhiteSpace();
                    context.appendRenderable(wildcard.bound());
                } else if (isObjectType()) {
                    ObjectTypeDetails objectType = getObjectDetails();
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
                } else if (isExecutable()) {
                    context.appendText("<executable>");
                }
            }
        };
    }

    public enum Kind {
        VOID, OBJECT, PRIMITIVE, ARRAY, TYPE_VARIABLE, WILDCARD, INTERSECTION, EXECUTABLE
    }

    private static class VoidType extends Type {
        @Override
        public Kind kind() {
            return Kind.VOID;
        }
    }

    private static class TypeVariable extends Type {

        private final TypeVariableDetails details;

        TypeVariable(TypeVariableDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.TYPE_VARIABLE;
        }

        @Override
        public TypeVariableDetails getVariableDetails() {
            return details;
        }
    }
    private static class IntersectionType extends Type {

        private final IntersectionTypeDetails details;

        IntersectionType(IntersectionTypeDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.INTERSECTION;
        }

        @Override
        public IntersectionTypeDetails getIntersectionDetails() {
            return details;
        }
    }

    private static class ObjectType extends Type {

        private final ObjectTypeDetails details;

        ObjectType(ObjectTypeDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.OBJECT;
        }

        @Override
        public ObjectTypeDetails getObjectDetails() {
            return details;
        }
    }

    private static class PrimitiveType extends Type {

        private final PrimitiveTypeDetails details;

        PrimitiveType(PrimitiveTypeDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.PRIMITIVE;
        }
        @Override
        public PrimitiveTypeDetails getPrimitiveDetails() {
            return details;
        }
    }
    private static class ExecutableType extends Type {

        private final ExecutableTypeDetails details;

        ExecutableType(ExecutableTypeDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.EXECUTABLE;
        }
        @Override
        public ExecutableTypeDetails getExecutableDetails() {
            return details;
        }
    }

    private static class ArrayType extends Type {

        private final ArrayTypeDetails details;

        ArrayType(ArrayTypeDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.ARRAY;
        }

        @Override
        public ArrayTypeDetails getArrayDetails() {
            return details;
        }
    }

    private static class WildcardType extends Type {

        private final WildcardTypeDetails details;

        WildcardType(WildcardTypeDetails details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.WILDCARD;
        }

        @Override
        public WildcardTypeDetails getWildcardDetails() {
            return details;
        }
    }


}
