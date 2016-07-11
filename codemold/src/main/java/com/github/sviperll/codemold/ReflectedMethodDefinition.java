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
import com.github.sviperll.codemold.render.Renderer;
import com.github.sviperll.codemold.render.RendererContext;
import com.github.sviperll.codemold.util.Collections2;
import com.github.sviperll.codemold.util.Snapshot;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
 class ReflectedMethodDefinition extends MethodDefinition {

    static ReflectedMethodDefinition createInstance(CodeMold codeModel, Nesting nesting, Method method) {
        ReflectedExecutableDefinitionImplementation executable = new ReflectedExecutableDefinitionImplementation(codeModel, nesting, method);
        return executable.definition();
    }
    private final CodeMold codeModel;
    private final Method method;
    private AnyType returnType = null;

    ReflectedMethodDefinition(CodeMold codeModel, ReflectedExecutableDefinitionImplementation executable, Method method) {
        super(executable);
        this.codeModel = codeModel;
        this.method = method;
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(method.getModifiers());
    }

    @Override
    public AnyType returnType() {
        if (returnType == null) {
            returnType = codeModel.readReflectedType(method.getGenericReturnType());
        }
        return returnType;
    }

    @Override
    public String name() {
        return method.getName();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(method.getModifiers());
    }

    @Override
    public boolean hasDefaultValue() {
        return method.getDefaultValue() != null;
    }

    @Override
    public AnyAnnotationValue defaultValue() {
        if (!hasDefaultValue()) {
            throw new UnsupportedOperationException("No default value");
        }
        return AnnotationValues.ofObject(method.getDefaultValue());
    }

    private static class ReflectedExecutableDefinitionImplementation implements ExecutableDefinition.Implementation<MethodType, MethodDefinition> {

        private static final Renderable body = new RenderableUnaccessibleCode();
        private final CodeMold codeModel;
        private final Nesting nesting;
        private final Method method;
        private ReflectedMethodDefinition definition = null;
        private List<? extends VariableDeclaration> parameters = null;
        private List<? extends AnyType> throwsList = null;

        private ReflectedExecutableDefinitionImplementation(CodeMold codeModel, Nesting nesting, Method method) {
            this.codeModel = codeModel;
            this.nesting = nesting;
            this.method = method;
        }

        ReflectedMethodDefinition definition() {
            if (definition == null) {
                definition = new ReflectedMethodDefinition(codeModel, this, method);
            }
            return definition;
        }

        @Override
        public TypeParameters typeParameters() {
            return new ReflectedTypeParameters<>(definition, method.getTypeParameters());
        }

        @Override
        public List<? extends VariableDeclaration> parameters() {
            if (parameters == null) {
                List<VariableDeclaration> parametersBuilder = Collections2.newArrayList();
                Parameter[] reflectedParameters = method.getParameters();
                for (Parameter parameter : reflectedParameters) {
                    parametersBuilder.add(new ReflectedParameter(codeModel, parameter));
                }
                parameters = Snapshot.of(parametersBuilder);
            }
            return Snapshot.of(parameters);
        }

        @Override
        public List<? extends AnyType> throwsList() {
            if (throwsList == null) {
                List<AnyType> throwsListBuilder = Collections2.newArrayList();
                for (java.lang.reflect.Type exceptionType : method.getGenericExceptionTypes()) {
                    throwsListBuilder.add(codeModel.readReflectedType(exceptionType));
                }
                throwsList = Snapshot.of(throwsListBuilder);
            }
            return Snapshot.of(throwsList);
        }

        @Override
        public Renderable body() {
            return body;
        }

        @Override
        public Nesting nesting() {
            return nesting;
        }
    }

    private static class ReflectedParameter extends VariableDeclaration {

        private final CodeMold codeModel;
        private final Parameter parameter;
        private AnyType type = null;

        ReflectedParameter(CodeMold codeModel, Parameter parameter) {
            this.codeModel = codeModel;
            this.parameter = parameter;
        }

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public AnyType type() {
            if (type == null) {
                type = codeModel.readReflectedType(parameter.getParameterizedType());
            }
            return type;
        }

        @Override
        public String name() {
            return parameter.getName();
        }

        @Override
        public boolean isInitialized() {
            return false;
        }

        @Override
        Renderable getInitialValue() {
            throw new UnsupportedOperationException();
        }
    }

    private static class RenderableUnaccessibleCode implements Renderable {

        RenderableUnaccessibleCode() {
        }

        @Override
        public Renderer createRenderer(final RendererContext context) {
            return new UnaccessibleCodeRenderer(context);
        }

        private static class UnaccessibleCodeRenderer implements Renderer {

            private final RendererContext context;

            UnaccessibleCodeRenderer(RendererContext context) {
                this.context = context;
            }

            @Override
            public void render() {
                context.appendText("{");
                context.appendLineBreak();
                context.indented().appendText("// Inaccessible code");
                context.appendLineBreak();
                context.indented().appendText("throw new java.lang.UnsupportedOperationException(\"Attempt to execute inaccessible code\");");
                context.appendLineBreak();
                context.appendText("}");
            }
        }
    }

}
