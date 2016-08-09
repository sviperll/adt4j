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

import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
abstract class NamedObjectBuilder<B extends ResidenceProvider, MB extends ExecutableBuilder<MethodType, MethodDefinition>>
        extends ObjectBuilder<B, MB>
        implements AnnotatableBuilder {
    private final String name;
    private final AnnotationCollection.Builder annotations = AnnotationCollection.createBuilder();

    NamedObjectBuilder(ObjectKind kind, B residence, String name) {
        super(kind, residence);
        this.name = name;
    }

    @Override
    public final B residence() {
        return super.residence();
    }

    @Override
    public final ObjectDefinition definition() {
        return super.definition();
    }

    @Override
    public ClassBuilder<NestingBuilder> staticNestedClass(String name) throws CodeMoldException {
        return super.staticNestedClass(name);
    }

    @Override
    public InterfaceBuilder<NestingBuilder> nestedInterface(String name) throws CodeMoldException {
        return super.nestedInterface(name);
    }

    @Override
    public AnnotationDefinitionBuilder<NestingBuilder> nestedAnnotationDefinition(String name) throws CodeMoldException {
        return super.nestedAnnotationDefinition(name);
    }

    @Override
    public EnumBuilder<NestingBuilder> nestedEnum(String name) throws CodeMoldException {
        return super.nestedEnum(name);
    }

    @Override
    public void annotate(Annotation annotation) {
        annotations.annotate(annotation);
    }

    /**
     * Annotate with @lit{@}Generated annotation
     * @param generatorName
     */
    public void annotateGenerated(String generatorName) {
        annotations.annotate(Annotation.createInstance(
                getCodeMold().getReference(Generated.class),
                CompileTimeValues.of(generatorName)));
    }

    abstract class BuiltDefinition
            extends ObjectBuilder<B, MB>.BuiltDefinition {
        BuiltDefinition(TypeParameters typeParameters) {
            super(typeParameters);
        }

        @Override
        public final String simpleTypeName() {
            return name;
        }

        @Override
        public final boolean isAnonymous() {
            return false;
        }

        @Override
        public List<? extends Annotation> getAnnotation(ObjectDefinition definition) {
            return annotations.build().getAnnotation(definition);
        }

        @Override
        public Collection<? extends Annotation> allAnnotations() {
            return annotations.build().allAnnotations();
        }
    }

}
