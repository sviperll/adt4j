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

import com.github.sviperll.codemold.util.Collections2;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Facade-class for codemold library.
 *
 * Use CodeMold#createBuilder() method to build new CodeMold instance.
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public final class CodeMold {
    static void validateSimpleName(String name) throws CodeMoldException {
        if (!name.matches("[_A-Za-z][_A-Za-z0-9]*")) {
            throw new CodeMoldException(name + " is not allowed Java identifier");
        }
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    private final Package defaultPackage = Package.createTopLevelPackage(this);
    private ObjectType objectType = null;

    private CodeMold() {
    }

    @Nonnull
    public ObjectType objectType() {
        if (objectType == null) {
            ObjectDefinition javaLangObjectDefinition = getReference(Object.class.getName()).orElseThrow(() -> new IllegalStateException("java.lang.Object is not loadable class!"));
            objectType = javaLangObjectDefinition.rawType();
        }
        return objectType;
    }

    @Nonnull
    public Package getPackage(String qualifiedName) throws CodeMoldException {
        return defaultPackage.getChildPackageBySuffix(qualifiedName);
    }

    @Nonnull
    public Package defaultPackage() {
        return defaultPackage;
    }

    public Optional<ObjectDefinition> getReference(String qualifiedName) {
        return defaultPackage.getReference(qualifiedName);
    }

    @Nonnull
    AnyType readReflectedType(java.lang.reflect.Type genericReflectedType) {
        if (genericReflectedType instanceof ParameterizedType) {
            ParameterizedType reflectedType = (ParameterizedType)genericReflectedType;
            ObjectType rawType = readReflectedType(reflectedType.getRawType()).getObjectDetails();
            List<AnyType> arguments = Collections2.newArrayList();
            for (java.lang.reflect.Type reflectedArgumentType: reflectedType.getActualTypeArguments()) {
                arguments.add(readReflectedType(reflectedArgumentType));
            }
            return rawType.narrow(arguments).asAny();
        } else if (genericReflectedType instanceof GenericArrayType) {
            GenericArrayType reflectedType = (GenericArrayType)genericReflectedType;
            AnyType componentType = readReflectedType(reflectedType.getGenericComponentType());
            return Types.arrayOf(componentType).asAny();
        } else if (genericReflectedType instanceof java.lang.reflect.WildcardType) {
            java.lang.reflect.WildcardType reflectedType = (java.lang.reflect.WildcardType)genericReflectedType;
            java.lang.reflect.Type[] reflectedLowerBounds = reflectedType.getLowerBounds();
            if (reflectedLowerBounds.length != 0) {
                AnyType bound = readReflectedType(reflectedLowerBounds[0]);
                return Types.wildcardSuper(bound).asAny();
            } else {
                java.lang.reflect.Type[] reflectedUpperBounds = reflectedType.getUpperBounds();
                AnyType bound = readReflectedType(reflectedUpperBounds[0]);
                return Types.wildcardExtends(bound).asAny();
            }
        } else if (genericReflectedType instanceof java.lang.reflect.TypeVariable) {
            java.lang.reflect.TypeVariable<?> reflectedType = (java.lang.reflect.TypeVariable<?>)genericReflectedType;
            return Types.variable(reflectedType.getName()).asAny();
        } else if (genericReflectedType instanceof Class) {
            Class<?> reflectedType = (Class<?>)genericReflectedType;
            if (reflectedType.isPrimitive()) {
                String name = reflectedType.getName();
                if (name.equals("void"))
                    return AnyType.voidType();
                else
                    return PrimitiveType.valueOf(name.toUpperCase(Locale.US)).asAny();
            } else if (reflectedType.isArray()) {
                return Types.arrayOf(readReflectedType(reflectedType.getComponentType())).asAny();
            } else {
                ObjectDefinition definition = getReference(reflectedType.getName()).orElseThrow(() -> new IllegalStateException("java.lang.reflect.Type references unexisting type: " + reflectedType.getName()));
                return definition.rawType().asAny();
            }
        } else
            throw new UnsupportedOperationException("Can't read " + genericReflectedType);
    }

    public static class Builder {
        public Builder() {
        }

        @Nonnull
        public CodeMold build() {
            return new CodeMold();
        }
    }

}
