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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 * @param <B>
 */
@ParametersAreNonnullByDefault
public final class ObjectDefinitionBuilder<T extends Residence, B extends ResidenceBuilder<T>>
        implements Model, SettledBuilder<T, B>, GenericDefinitionBuilder<T> {
    private final BuiltDefinition definition = new BuiltDefinition();
    private final GenericsConfigBuilder<T> generics = new GenericsConfigBuilder<>(definition);
    private final BuiltType type = new BuiltType();
    private final B residence;
    private final String name;
    private final ObjectKind kind;
    private final List<MethodDefinition> constructors = new ArrayList<>();
    private final List<MethodDefinition> methods = new ArrayList<>();
    private final Map<String, FieldDeclaration> fields = new TreeMap<>();
    private final List<InitElement> staticInitOrdering = new ArrayList<>();
    private final List<InitElement> instanceInitOrdering = new ArrayList<>();
    private final Map<String, ObjectDefinition<NestedResidence>> innerClasses = new TreeMap<>();
    private boolean isFinal = false;
    private ObjectType extendsClass = null;
    private List<ObjectType> interfaces = new ArrayList<>();

    ObjectDefinitionBuilder(ObjectKind kind, B residence, String name) throws CodeModelException {
        if ((kind == ObjectKind.INTERFACE || kind == ObjectKind.ENUM || kind == ObjectKind.ANNOTATION)
                && residence.residence().isNested()
                && !residence.residence().asNested().isStatic()) {
            throw new CodeModelException("Interface, enum or annotation should always be static when nested in other class");
        }
        this.residence = residence;
        this.name = name;
        this.kind = kind;
    }

    public ObjectDefinition<T> definition() {
        return definition;
    }

    public void setFinal(boolean value) {
        this.isFinal = value;
    }

    @Override
    public B residence() {
        return residence;
    }

    @Override
    public GenericsConfigBuilder<T> generics() {
        return generics;
    }

    public void extendsClass(ObjectType type) throws CodeModelException {
        if (this.extendsClass != null)
             throw new CodeModelException("Already extended");
        if (!type.definition().kind().isClass())
             throw new CodeModelException("Only classes can be extended");
        if (!type.definition().isFinal())
            throw new CodeModelException("Trying to extend final class");
        if (type.containsWildcards())
             throw new CodeModelException("Wildcards are not allowed in extends clause");
        this.extendsClass = type;
    }

    public void implementsInterface(ObjectType type) throws CodeModelException {
        if (!type.definition().kind().isInterface())
            throw new CodeModelException("Only interfaces can be implemented");
        if (type.containsWildcards())
             throw new CodeModelException("Wildcards are not allowed in implemenents clause");
        interfaces.add(type);
    }

    public FieldBuilder staticField(Type type, String name) throws CodeModelException {
        if (fields.containsKey(name)) {
            throw new CodeModelException(definition.qualifiedName() + "." + name + " already defined");
        }
        NestedResidenceBuilder membership = new NestedResidenceBuilder(true, definition);
        FieldBuilder result = new FieldBuilder(membership, type, name);
        fields.put(name, result.declaration());
        staticInitOrdering.add(new InitElement(result.declaration()));
        return result;
    }

    public FieldBuilder field(Type type, String name) throws CodeModelException {
        if (fields.containsKey(name)) {
            throw new CodeModelException(definition.qualifiedName() + "." + name + " already defined");
        }
        NestedResidenceBuilder membership = new NestedResidenceBuilder(false, definition);
        FieldBuilder result = new FieldBuilder(membership, type, name);
        fields.put(name, result.declaration());
        instanceInitOrdering.add(new InitElement(result.declaration()));
        return result;
    }

    public ObjectDefinitionBuilder<NestedResidence, NestedResidenceBuilder> staticNestedClass(ObjectKind kind, String name) throws CodeModelException {
        if (innerClasses.containsKey(name))
            throw new CodeModelException(definition.qualifiedName() + "." + name + " already defined");
        NestedResidenceBuilder classResidence = new NestedResidenceBuilder(true, definition);
        ObjectDefinitionBuilder<NestedResidence, NestedResidenceBuilder> result = new ObjectDefinitionBuilder<>(kind, classResidence, name);
        innerClasses.put(name, result.definition());
        return result;
    }

    public ObjectDefinitionBuilder<NestedResidence, NestedResidenceBuilder> innerClass(ObjectKind kind, String name) throws CodeModelException {
        if (innerClasses.containsKey(name))
            throw new CodeModelException(definition.qualifiedName() + "." + name + " already defined");
        NestedResidenceBuilder classResidence = new NestedResidenceBuilder(false, definition);
        ObjectDefinitionBuilder<NestedResidence, NestedResidenceBuilder> result = new ObjectDefinitionBuilder<>(kind, classResidence, name);
        innerClasses.put(name, result.definition());
        return result;
    }

    public MethodBuilder method(String name) throws CodeModelException {
        NestedResidenceBuilder methodResidence = new NestedResidenceBuilder(false, definition);
        MethodBuilder result = new MethodBuilder(methodResidence, name);
        methods.add(result.definition());
        return result;
    }

    public MethodBuilder staticMethod(String name) throws CodeModelException {
        NestedResidenceBuilder methodResidence = new NestedResidenceBuilder(true, definition);
        MethodBuilder result = new MethodBuilder(methodResidence, name);
        methods.add(result.definition());
        return result;
    }

    public ConstructorBuilder addConstructor() throws CodeModelException {
        NestedResidenceBuilder methodResidence = new NestedResidenceBuilder(false, definition);
        ConstructorBuilder result = new ConstructorBuilder(methodResidence);
        constructors.add(result.definition());
        return result;
    }

    @Override
    public CodeModel getCodeModel() {
        return residence.getCodeModel();
    }

    private static class InitElement {
        private final InitElement.Kind kind;
        private final FieldDeclaration field;
        InitElement(FieldDeclaration field) {
            kind = InitElement.Kind.FIELD;
            this.field = field;
        }
        private enum Kind {
            FIELD, INITIALIZER
        }
    }

    private class BuiltDefinition extends ObjectDefinition<T> {

        @Override
        public Collection<MethodDefinition> methods() {
            return Collections.unmodifiableList(methods);
        }

        @Override
        public Collection<ObjectDefinition<NestedResidence>> innerClasses() {
            return innerClasses.values();
        }

        @Override
        public Collection<FieldDeclaration> fields() {
            return fields.values();
        }

        @Override
        public String simpleName() {
            return name;
        }

        @Override
        public T residence() {
            return residence.residence();
        }

        @Override
        public CodeModel getCodeModel() {
            return residence.getCodeModel();
        }

        @Override
        public boolean isFinal() {
            return isFinal;
        }

        @Override
        public ObjectKind kind() {
            return kind;
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
        public ObjectType extendsClass() {
            return extendsClass != null ? extendsClass : getCodeModel().objectType();
        }

        @Override
        public List<ObjectType> implementsInterfaces() {
            return Collections.unmodifiableList(interfaces);
        }

        @Override
        public ObjectType toType() {
            return type;
        }

        @Override
        public GenericsConfig generics() {
            return generics.generics();
        }
    }

    private class BuiltType extends RawObjectType {
        @Override
        public ObjectDefinition<?> definition() {
            return definition;
        }
    }
}
