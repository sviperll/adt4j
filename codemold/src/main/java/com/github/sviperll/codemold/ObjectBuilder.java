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
import com.github.sviperll.codemold.util.Snapshot;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <B>
 */
@ParametersAreNonnullByDefault
abstract class ObjectBuilder<B extends ResidenceProvider> extends GenericDefinitionBuilder<B, ObjectType, ObjectDefinition> {
    private final List<MethodDefinition> methods = Collections2.newArrayList();
    private final Map<String, FieldDeclaration> fields = new TreeMap<>();
    private final List<ObjectInitializationElement> staticInitOrdering = Collections2.newArrayList();
    private final List<ObjectInitializationElement> instanceInitOrdering = Collections2.newArrayList();
    private final Map<String, ObjectDefinition> innerClasses = new TreeMap<>();

    private final B residence;
    private final ObjectKind kind;

    ObjectBuilder(ObjectKind kind, B residence) {
        super(residence);
        if ((kind == ObjectKind.INTERFACE || kind == ObjectKind.ENUM || kind == ObjectKind.ANNOTATION)
                && residence.residence().isNested()
                && !residence.residence().getNesting().isStatic()) {
            throw new IllegalArgumentException("Interface, enum or annotation should always be static when nested in other class");
        }
        this.residence = residence;
        this.kind = kind;
    }

    @Nonnull
    protected FieldBuilder staticField(Type type, String name) throws CodeMoldException {
        if (fields.containsKey(name)) {
            throw new CodeMoldException(definition().qualifiedTypeName() + "." + name + " already defined");
        }
        NestingBuilder membership = new NestingBuilder(true, definition());
        FieldBuilder result = new FieldBuilder(membership, false, type.asAny(), name);
        fields.put(name, result.declaration());
        staticInitOrdering.add(new ObjectInitializationElement(result.declaration()));
        return result;
    }

    @Nonnull
    protected FieldBuilder staticFinalField(Type type, String name) throws CodeMoldException {
        if (fields.containsKey(name)) {
            throw new CodeMoldException(definition().qualifiedTypeName() + "." + name + " already defined");
        }
        NestingBuilder membership = new NestingBuilder(true, definition());
        FieldBuilder result = new FieldBuilder(membership, true, type.asAny(), name);
        fields.put(name, result.declaration());
        staticInitOrdering.add(new ObjectInitializationElement(result.declaration()));
        return result;
    }

    // Should be exposed as public by ClassBuilder and EnumBuilder subclasses that want to allow nonstatic field definition
    @Nonnull
    protected FieldBuilder field(Type type, String name) throws CodeMoldException {
        if (fields.containsKey(name)) {
            throw new CodeMoldException(definition().qualifiedTypeName() + "." + name + " already defined");
        }
        NestingBuilder membership = new NestingBuilder(false, definition());
        FieldBuilder result = new FieldBuilder(membership, false, type.asAny(), name);
        fields.put(name, result.declaration());
        instanceInitOrdering.add(new ObjectInitializationElement(result.declaration()));
        return result;
    }

    // Should be exposed as public by ClassBuilder and EnumBuilder subclasses that want to allow nonstatic field definition
    @Nonnull
    protected FieldBuilder finalField(Type type, String name) throws CodeMoldException {
        if (fields.containsKey(name)) {
            throw new CodeMoldException(definition().qualifiedTypeName() + "." + name + " already defined");
        }
        NestingBuilder membership = new NestingBuilder(false, definition());
        FieldBuilder result = new FieldBuilder(membership, true, type.asAny(), name);
        fields.put(name, result.declaration());
        instanceInitOrdering.add(new ObjectInitializationElement(result.declaration()));
        return result;
    }

    @Nonnull
    protected ClassBuilder<NestingBuilder> staticNestedClass(String name) throws CodeMoldException {
        if (innerClasses.containsKey(name))
            throw new CodeMoldException(definition().qualifiedTypeName() + "." + name + " already defined");
        NestingBuilder classResidence = new NestingBuilder(true, definition());
        ClassBuilder<NestingBuilder> result = new ClassBuilder<>(classResidence, name);
        innerClasses.put(name, result.definition());
        return result;
    }

    // Should be exposed as public by ClassBuilder and EnumBuilder subclasses that want to allow nonstatic field definition
    @Nonnull
    protected ClassBuilder<NestingBuilder> innerClass(String name) throws CodeMoldException {
        if (innerClasses.containsKey(name))
            throw new CodeMoldException(definition().qualifiedTypeName() + "." + name + " already defined");
        NestingBuilder classResidence = new NestingBuilder(false, definition());
        ClassBuilder<NestingBuilder> result = new ClassBuilder<>(classResidence, name);
        innerClasses.put(name, result.definition());
        return result;
    }

    @Nonnull
    protected InterfaceBuilder<NestingBuilder> nestedInterface(String name) throws CodeMoldException {
        if (innerClasses.containsKey(name))
            throw new CodeMoldException(definition().qualifiedTypeName() + "." + name + " already defined");
        NestingBuilder classResidence = new NestingBuilder(true, definition());
        InterfaceBuilder<NestingBuilder> result = new InterfaceBuilder<>(classResidence, name);
        innerClasses.put(name, result.definition());
        return result;
    }

    @Nonnull
    protected EnumBuilder<NestingBuilder> nestedEnum(String name) throws CodeMoldException {
        if (innerClasses.containsKey(name))
            throw new CodeMoldException(definition().qualifiedTypeName() + "." + name + " already defined");
        NestingBuilder classResidence = new NestingBuilder(true, definition());
        EnumBuilder<NestingBuilder> result = new EnumBuilder<>(classResidence, name);
        innerClasses.put(name, result.definition());
        return result;
    }

    @Nonnull
    protected MethodBuilder method(String name) throws CodeMoldException {
        NestingBuilder methodResidence = new NestingBuilder(false, definition());
        MethodBuilder result = new MethodBuilder(methodResidence, name);
        methods.add(result.definition());
        return result;
    }

    @Nonnull
    protected MethodBuilder staticMethod(String name) throws CodeMoldException {
        NestingBuilder methodResidence = new NestingBuilder(true, definition());
        MethodBuilder result = new MethodBuilder(methodResidence, name);
        methods.add(result.definition());
        return result;
    }

    abstract class BuiltDefinition extends ObjectDefinition {
        private final TypeParameters typeParameters;

        BuiltDefinition(TypeParameters typeParameters) {
            this.typeParameters = typeParameters;
        }

        @Override
        public final TypeParameters typeParameters() {
            return typeParameters;
        }

        @Override
        public final List<? extends MethodDefinition> methods() {
            return Snapshot.of(methods);
        }

        @Override
        public final Collection<? extends ObjectDefinition> innerClasses() {
            return innerClasses.values();
        }

        @Override
        public final Collection<? extends FieldDeclaration> fields() {
            return fields.values();
        }

        @Override
        public final Residence residence() {
            return residence.residence();
        }

        @Override
        public final CodeMold getCodeModel() {
            return residence().getCodeModel();
        }

        @Override
        public final ObjectKind kind() {
            return kind;
        }

        @Override
        final List<? extends ObjectInitializationElement> staticInitializationElements() {
            return staticInitOrdering;
        }

        @Override
        final List<? extends ObjectInitializationElement> instanceInitializationElements() {
            return instanceInitOrdering;
        }


    }
}
