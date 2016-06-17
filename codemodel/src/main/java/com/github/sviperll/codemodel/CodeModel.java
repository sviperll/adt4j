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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public final class CodeModel {
    static void validateSimpleName(String name) throws CodeModelException {
        if (!name.matches("[_A-Za-z][_A-Za-z0-9]*")) {
            throw new CodeModelException(name + " is not allowed Java identifier");
        }
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    private final Package defaultPackage = Package.createTopLevelPackage(this);
    private ObjectType objectType = null;

    private CodeModel() {
    }

    @Nonnull
    public ObjectType objectType() {
        if (objectType == null) {
            ObjectDefinition javaLangObjectDefinition = getReference(Object.class.getName());
            if (javaLangObjectDefinition == null) {
                throw new RuntimeException("java.lang.Object is not loadable class!");
            } else {
                objectType = javaLangObjectDefinition.rawType();
            }
        }
        return objectType;
    }

    @Nonnull
    public Package getPackage(String qualifiedName) throws CodeModelException {
        return defaultPackage.getChildPackageBySuffix(qualifiedName);
    }

    @Nonnull
    public Package defaultPackage() {
        return defaultPackage;
    }

    @Nullable
    public ObjectDefinition getReference(String qualifiedName) {
        return defaultPackage.getReference(qualifiedName);
    }

    @Nonnull
    Type readReflectedType(java.lang.reflect.Type genericReflectedType) {
        if (genericReflectedType instanceof ParameterizedType) {
            ParameterizedType reflectedType = (ParameterizedType)genericReflectedType;
            ObjectType rawType = readReflectedType(reflectedType.getRawType()).getObjectDetails();
            List<Type> arguments = new ArrayList<>();
            for (java.lang.reflect.Type reflectedArgumentType: reflectedType.getActualTypeArguments()) {
                arguments.add(readReflectedType(reflectedArgumentType));
            }
            return rawType.narrow(arguments).asType();
        } else if (genericReflectedType instanceof GenericArrayType) {
            GenericArrayType reflectedType = (GenericArrayType)genericReflectedType;
            Type componentType = readReflectedType(reflectedType.getGenericComponentType());
            return Type.arrayOf(componentType).asType();
        } else if (genericReflectedType instanceof java.lang.reflect.WildcardType) {
            java.lang.reflect.WildcardType reflectedType = (java.lang.reflect.WildcardType)genericReflectedType;
            java.lang.reflect.Type[] reflectedLowerBounds = reflectedType.getLowerBounds();
            if (reflectedLowerBounds.length != 0) {
                Type bound = readReflectedType(reflectedLowerBounds[0]);
                return Type.wildcardSuper(bound).asType();
            } else {
                java.lang.reflect.Type[] reflectedUpperBounds = reflectedType.getUpperBounds();
                Type bound = readReflectedType(reflectedUpperBounds[0]);
                return Type.wildcardExtends(bound).asType();
            }
        } else if (genericReflectedType instanceof java.lang.reflect.TypeVariable) {
            java.lang.reflect.TypeVariable<?> reflectedType = (java.lang.reflect.TypeVariable<?>)genericReflectedType;
            return Type.variable(reflectedType.getName()).asType();
        } else if (genericReflectedType instanceof Class) {
            Class<?> reflectedType = (Class<?>)genericReflectedType;
            if (reflectedType.isPrimitive()) {
                String name = reflectedType.getName();
                if (name.equals("void"))
                    return Type.voidType();
                else
                    return PrimitiveType.valueOf(name.toUpperCase(Locale.US)).asType();
            } else if (reflectedType.isArray()) {
                return Type.arrayOf(readReflectedType(reflectedType.getComponentType())).asType();
            } else {
                ObjectDefinition definition = getReference(reflectedType.getName());
                if (definition == null)
                    throw new IllegalStateException("java.lang.reflect.Type references unexisting type: " + reflectedType.getName());
                else
                    return definition.rawType().asType();
            }
        } else
            throw new UnsupportedOperationException("Can't read " + genericReflectedType);
    }

    public static class Builder {
        public Builder() {
        }

        @Nonnull
        public CodeModel build() {
            return new CodeModel();
        }
    }

}
