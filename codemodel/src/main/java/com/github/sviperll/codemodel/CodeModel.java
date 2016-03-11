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

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public final class CodeModel {
    static void validateSimpleName(String name) throws CodeModelException {
        if (!name.matches("[_A-Za-z][_A-Za-z0-9]")) {
            throw new CodeModelException(name + " is not allowed Java identifier");
        }
    }

    private final Package defaultPackage = new Package(this, "");
    private final Map<String, Package> packages = new TreeMap<>();
    private Type objectType = null;

    public Type objectType() {
        if (objectType == null)
            objectType = importClass(Object.class).toType();
        return objectType;
    }

    public Package getPackage(String qualifiedName) throws CodeModelException {
        int index = qualifiedName.indexOf('.');
        if (index == 0)
            throw new CodeModelException(qualifiedName + " illegal qualified name");
        boolean isTopLevelPackage = index < 0;
        String topLevelName = isTopLevelPackage ? qualifiedName : qualifiedName.substring(0, index);
        Package topLevelPackage = packages.get(topLevelName);
        if (topLevelPackage == null) {
            validateSimpleName(topLevelName);
            topLevelPackage = new Package(this, topLevelName);
            packages.put(topLevelName, topLevelPackage);
        }
        if (isTopLevelPackage)
            return topLevelPackage;
        else
            return topLevelPackage.getChildPackageBySuffix(qualifiedName.substring(index + 1));
    }

    public ObjectDefinitionBuilder<PackageLevelResidence, PackageLevelResidenceBuilder> createDefaultPackageClass(ObjectKind kind, String name) throws CodeModelException {
        return defaultPackage.createClass(kind, name);
    }

    public ObjectDefinition<?> importClass(Class<?> klass) {
        if (klass.isArray())
            throw new IllegalArgumentException("Arrays don't have class definition");
        return new ReflectionObjectDefinition(this, klass);
    }

    public ObjectDefinition<?> importClass(Element element, Elements elementUtils) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class ReflectionObjectDefinition<T extends Residence> extends ObjectDefinition<T> {
        private final Type type = Type.createObjectType(new TypeDetails());
        private final CodeModel codeModel;

        private final Class<?> klass;

        public ReflectionObjectDefinition(CodeModel codeModel, Class<?> klass) {
            this.codeModel = codeModel;
            this.klass = klass;
        }

        @Override
        public boolean isFinal() {
            return (klass.getModifiers() | Modifier.FINAL) != 0;
        }

        @Override
        public ObjectKind kind() {
            if (klass.isInterface())
                return ObjectKind.INTERFACE;
            else if (klass.isEnum())
                return ObjectKind.ENUM;
            else if (klass.isAnnotation())
                return ObjectKind.ANNOTATION;
            else
                return ObjectKind.CLASS;
        }

        @Override
        public ObjectTypeDetails extendsClass() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<ObjectTypeDetails> implementsInterfaces() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<MethodDefinition> methods() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<ObjectDefinition<NestedResidence>> innerClasses() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<FieldDeclaration> fields() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String simpleName() {
            return klass.getSimpleName();
        }

        @Override
        public T residence() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CodeModel getCodeModel() {
            return codeModel;
        }

        @Override
        public boolean isObjectDefinition() {
            return true;
        }

        @Override
        public boolean isMethodDefinition() {
            return false;
        }

        @Override
        public ObjectDefinition<?> asObjectDefinition() {
            return this;
        }

        @Override
        public MethodDefinition asMethodDefinition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public GenericsConfig generics() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Type toType() {
            return type;
        }

        public class TypeDetails extends RawObjectTypeDetails {

            @Override
            public ObjectDefinition<?> definition() {
                return ReflectionObjectDefinition.this;
            }

            @Override
            public Type asType() {
                return type;
            }

        }
    }
}
