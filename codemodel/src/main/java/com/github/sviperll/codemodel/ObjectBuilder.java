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
 * @param <B>
 */
@ParametersAreNonnullByDefault
public abstract class ObjectBuilder<B extends ResidenceBuilder> extends GenericDefinitionBuilder<B> {
    private final List<ExecutableDefinition> methods = new ArrayList<>();
    private final Map<String, FieldDeclaration> fields = new TreeMap<>();
    private final List<ObjectInitializationElement> staticInitOrdering = new ArrayList<>();
    private final List<ObjectInitializationElement> instanceInitOrdering = new ArrayList<>();
    private final Map<String, ObjectDefinition> innerClasses = new TreeMap<>();

    private final TypeContainer typeContainer;
    private final B residence;
    private final ObjectKind kind;

    ObjectBuilder(ObjectKind kind, B residence) throws CodeModelException {
        super(residence);
        if ((kind == ObjectKind.INTERFACE || kind == ObjectKind.ENUM || kind == ObjectKind.ANNOTATION)
                && residence.residence().isNested()
                && !residence.residence().getNesting().isStatic()) {
            throw new CodeModelException("Interface, enum or annotation should always be static when nested in other class");
        }
        if (residence.residence().contextDefinition() == null) {
            typeContainer = new TypeContainer(null);
        } else {
            typeContainer = null;
        }
        this.residence = residence;
        this.kind = kind;
    }

    @Override
    public abstract ObjectDefinition definition();

    public FieldBuilder staticField(Type type, String name) throws CodeModelException {
        if (fields.containsKey(name)) {
            throw new CodeModelException(definition().qualifiedName() + "." + name + " already defined");
        }
        NestingBuilder membership = new NestingBuilder(true, definition());
        FieldBuilder result = new FieldBuilder(membership, type, name);
        fields.put(name, result.declaration());
        staticInitOrdering.add(new ObjectInitializationElement(result.declaration()));
        return result;
    }

    public FieldBuilder field(Type type, String name) throws CodeModelException {
        if (fields.containsKey(name)) {
            throw new CodeModelException(definition().qualifiedName() + "." + name + " already defined");
        }
        NestingBuilder membership = new NestingBuilder(false, definition());
        FieldBuilder result = new FieldBuilder(membership, type, name);
        fields.put(name, result.declaration());
        instanceInitOrdering.add(new ObjectInitializationElement(result.declaration()));
        return result;
    }

    public NamedObjectBuilder<NestingBuilder> staticNestedClass(ObjectKind kind, String name) throws CodeModelException {
        if (innerClasses.containsKey(name))
            throw new CodeModelException(definition().qualifiedName() + "." + name + " already defined");
        NestingBuilder classResidence = new NestingBuilder(true, definition());
        NamedObjectBuilder<NestingBuilder> result = new NamedObjectBuilder<>(kind, classResidence, name);
        innerClasses.put(name, result.definition());
        return result;
    }

    public NamedObjectBuilder<NestingBuilder> innerClass(ObjectKind kind, String name) throws CodeModelException {
        if (innerClasses.containsKey(name))
            throw new CodeModelException(definition().qualifiedName() + "." + name + " already defined");
        NestingBuilder classResidence = new NestingBuilder(false, definition());
        NamedObjectBuilder<NestingBuilder> result = new NamedObjectBuilder<>(kind, classResidence, name);
        innerClasses.put(name, result.definition());
        return result;
    }

    public MethodBuilder method(String name) throws CodeModelException {
        NestingBuilder methodResidence = new NestingBuilder(false, definition());
        MethodBuilder result = new MethodBuilder(methodResidence, name);
        methods.add(result.definition());
        return result;
    }

    public MethodBuilder staticMethod(String name) throws CodeModelException {
        NestingBuilder methodResidence = new NestingBuilder(true, definition());
        MethodBuilder result = new MethodBuilder(methodResidence, name);
        methods.add(result.definition());
        return result;
    }

    abstract class BuiltDefinition extends GenericDefinitionBuilder<B>.BuiltObjectDefinition {

        @Override
        public final Collection<ExecutableDefinition> methods() {
            return Collections.unmodifiableList(methods);
        }

        @Override
        public final Collection<ObjectDefinition> innerClasses() {
            return innerClasses.values();
        }

        @Override
        public final Collection<FieldDeclaration> fields() {
            return fields.values();
        }

        @Override
        public final Residence residence() {
            return residence.residence();
        }

        @Override
        public final CodeModel getCodeModel() {
            return residence.getCodeModel();
        }

        @Override
        public final ObjectKind kind() {
            return kind;
        }

        @Override
        public final Type rawType() {
            if (residence.residence().contextDefinition() == null) {
                return typeContainer.rawTypeDetails.asType();
            } else {
                throw new UnsupportedOperationException("Parent instance type is required");
            }
        }

        @Override
        public final Type rawType(Type parentInstanceType) {
            if (residence.residence().contextDefinition() == null) {
                throw new UnsupportedOperationException("Type is static memeber, no parent is expected.");
            } else {
                TypeContainer typeContainer = new TypeContainer(parentInstanceType.getObjectDetails());
                return typeContainer.rawTypeDetails.asType();
            }
        }

        @Override
        public final Type internalType() {
            Type rawType;
            if (residence.residence().contextDefinition() == null) {
                rawType = rawType();
            } else {
                rawType = rawType(residence.residence().contextDefinition().internalType());
            }
            List<Type> internalTypeArguments = typeParameters().asInternalTypeArguments();
            try {
                return rawType.getExecutableDetails().narrow(internalTypeArguments);
            } catch (CodeModelException ex) {
                throw new RuntimeException("No parameter-argument mismatch is guaranteed to ever happen", ex);
            }
        }

        @Override
        final List<ObjectInitializationElement> staticInitializationElements() {
            return staticInitOrdering;
        }

        @Override
        final List<ObjectInitializationElement> instanceInitializationElements() {
            return instanceInitOrdering;
        }
    }

    private class TypeContainer {
        private final ObjectTypeDetails parentInstanceType;
        private final ObjectTypeDetails rawTypeDetails = GenericTypeDetails.createRawTypeDetails(new GenericTypeDetails.Factory<ObjectTypeDetails>() {
            @Override
            public ObjectTypeDetails createGenericTypeDetails(GenericTypeDetails.Parametrization implementation) {
                return new BuiltTypeDetails(implementation);
            }
        });

        TypeContainer(ObjectTypeDetails parentInstanceType) {
            this.parentInstanceType = parentInstanceType;
        }

        private class BuiltTypeDetails extends ObjectTypeDetails {
            private Type type = Type.createObjectType(this);
            BuiltTypeDetails(GenericTypeDetails.Parametrization implementation) {
                super(implementation);
            }

            @Override
            public ObjectDefinition definition() {
                return ObjectBuilder.this.definition();
            }

            @Override
            public Type asType() {
                return type;
            }

            @Override
            public Type capturedEnclosingType() {
                return parentInstanceType == null ? null : parentInstanceType.asType();
            }
        }
    }

}
