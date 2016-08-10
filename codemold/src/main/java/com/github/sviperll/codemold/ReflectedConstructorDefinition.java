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

import com.github.sviperll.codemold.render.Renderable;
import com.github.sviperll.codemold.util.Snapshot;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
class ReflectedConstructorDefinition<T> extends ConstructorDefinition {

    static <T> ReflectedConstructorDefinition<T> createInstance(Reflection reflection, Nesting nesting, Constructor<T> constructor) {
        ReflectedExecutableDefinitionImplementation<T> executable = new ReflectedExecutableDefinitionImplementation<>(reflection, nesting, constructor);
        return executable.definition();
    }
    private ReflectedConstructorDefinition(ReflectedExecutableDefinitionImplementation<T> executable) {
        super(executable);
    }

    private static class ReflectedExecutableDefinitionImplementation<T> implements ExecutableDefinition.Implementation<ConstructorType, ConstructorDefinition> {

        private ReflectedConstructorDefinition<T> definition = null;
        private final Reflection reflection;
        private final Nesting nesting;
        private final Constructor<T> constructor;
        private TypeParameters typeParameters = null;
        private List<? extends VariableDeclaration> parameters = null;
        private List<? extends AnyType> throwsList = null;
        private AnnotationCollection annotations = null;

        private ReflectedExecutableDefinitionImplementation(Reflection reflection, Nesting nesting, Constructor<T> constructor) {
            this.reflection = reflection;
            this.nesting = nesting;
            this.constructor = constructor;
        }

        private ReflectedConstructorDefinition<T> definition() {
            if (definition == null) {
                definition = new ReflectedConstructorDefinition<>(this);
            }
            return definition;
        }

        @Override
        public TypeParameters typeParameters() {
            if (typeParameters == null) {
                typeParameters = new ReflectedTypeParameters<>(reflection, definition(), constructor.getTypeParameters());
            }
            return typeParameters;
        }

        @Override
        public List<? extends VariableDeclaration> parameters() {
            if (parameters == null) {
                parameters = Snapshot.of(reflection.createParameterList(constructor.getParameters()));
            }
            return Snapshot.of(parameters);
        }

        @Override
        public List<? extends AnyType> throwsList() {
            if (throwsList == null) {
                throwsList = Snapshot.of(reflection.buildTypesFromReflections(constructor.getGenericExceptionTypes()));
            }
            return Snapshot.of(throwsList);
        }

        @Override
        public Renderable body() {
            return Reflection.renderableUnaccessibleCode();
        }

        @Override
        public Nesting nesting() {
            return nesting;
        }

        @Override
        public List<? extends Annotation> getAnnotation(ObjectDefinition definition) {
            initAnnotations();
            return annotations.getAnnotation(definition);
        }

        @Override
        public Collection<? extends Annotation> allAnnotations() {
            initAnnotations();
            return annotations.allAnnotations();
        }

        private void initAnnotations() {
            if (annotations == null) {
                annotations = reflection.readAnnotationCollection(constructor.getDeclaredAnnotations());
            }
        }

    }
}
