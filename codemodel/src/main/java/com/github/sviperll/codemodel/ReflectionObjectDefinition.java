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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class ReflectionObjectDefinition<T> extends ObjectDefinition {
    private final CodeModel codeModel;
    private final Residence residence;
    private final Class<T> klass;
    private List<ObjectDefinition> innerClasses = null;
    private List<MethodDefinition> methods = null;
    private final TypeParameters typeParameters;
    private List<ObjectType> implementsInterfaces = null;
    private ObjectType extendsClass = null;

    ReflectionObjectDefinition(CodeModel codeModel, Residence residence, Class<T> klass) {
        this.codeModel = codeModel;
        this.residence = residence;
        this.klass = klass;
        typeParameters = new ReflectedTypeParameters<>(this, klass.getTypeParameters());
    }

    @Override
    public boolean isFinal() {
        return (klass.getModifiers() & Modifier.FINAL) != 0;
    }

    @Override
    public ObjectKind kind() {
        if (klass.isInterface()) {
            return ObjectKind.INTERFACE;
        } else if (klass.isEnum()) {
            return ObjectKind.ENUM;
        } else if (klass.isAnnotation()) {
            return ObjectKind.ANNOTATION;
        } else {
            return ObjectKind.CLASS;
        }
    }

    @Override
    public ObjectType extendsClass() {
        if (isJavaLangObject())
            throw new UnsupportedOperationException("java.lang.Object super class is undefined");
        if (extendsClass == null) {
            if (kind().isInterface())
                extendsClass = codeModel.objectType();
            else
                extendsClass = codeModel.readReflectedType(klass.getGenericSuperclass()).getObjectDetails();
        }
        return extendsClass;
    }

    @Override
    public List<? extends ObjectType> implementsInterfaces() {
        if (implementsInterfaces == null) {
            implementsInterfaces = new ArrayList<>();
            for (java.lang.reflect.Type reflectedInterface: klass.getGenericInterfaces()) {
                implementsInterfaces.add(codeModel.readReflectedType(reflectedInterface).getObjectDetails());
            }
            implementsInterfaces = Collections.unmodifiableList(implementsInterfaces);
        }
        return implementsInterfaces;
    }

    @Override
    public List<? extends MethodDefinition> methods() {
        if (methods == null) {
            methods = new ArrayList<>();
            for (final Method method: klass.getDeclaredMethods()) {
                Nesting methodResidence = new ReflectedNesting(method.getModifiers(), this);
                ReflectedExecutableDefinitionImplementation executable = new ReflectedExecutableDefinitionImplementation(codeModel, methodResidence, method);
                methods.add(new ReflectedMethodDefinition(codeModel, executable, method));
            }
            methods = Collections.unmodifiableList(methods);
        }
        return methods;
    }

    @Override
    public Collection<? extends ObjectDefinition> innerClasses() {
        if (innerClasses == null) {
            innerClasses = new ArrayList<>();
            for (final Class<?> innerClass: klass.getDeclaredClasses()) {
                Residence innerClassResidence = new ReflectedNesting(innerClass.getModifiers(), this).residence();
                innerClasses.add(new ReflectionObjectDefinition<>(codeModel, innerClassResidence, innerClass));
            }
            innerClasses = Collections.unmodifiableList(innerClasses);
        }
        return innerClasses;
    }

    @Override
    public Collection<? extends FieldDeclaration> fields() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String simpleTypeName() {
        return klass.getSimpleName();
    }

    @Override
    public Residence residence() {
        return residence;
    }

    @Override
    public CodeModel getCodeModel() {
        return codeModel;
    }

    @Override
    List<? extends ObjectInitializationElement> staticInitializationElements() {
        return Collections.emptyList();
    }

    @Override
    List<? extends ObjectInitializationElement> instanceInitializationElements() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends ConstructorDefinition> constructors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<? extends EnumConstant> enumConstants() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public TypeParameters typeParameters() {
        return typeParameters;
    }

    private static class ReflectedTypeParameters<T extends java.lang.reflect.GenericDeclaration>
            extends TypeParameters {
        private List<TypeParameter> allTypeParameters = null;
        private final GenericDefinition<?, ?> definition;
        private final TypeVariable<T>[] reflectedTypeParameters;

        ReflectedTypeParameters(GenericDefinition<?, ?> definition, TypeVariable<T>[] reflectedTypeParameters) {
            this.definition = definition;
            this.reflectedTypeParameters = reflectedTypeParameters;
        }

        @Override
        public List<? extends TypeParameter> all() {
            if (allTypeParameters == null) {
                allTypeParameters = new ArrayList<>();
                for (final TypeVariable<T> reflectedTypeParameter: reflectedTypeParameters) {
                    TypeParameter parameter = new ReflectedTypeParameter<>(definition, reflectedTypeParameter);
                    allTypeParameters.add(parameter);
                }
                allTypeParameters = Collections.unmodifiableList(allTypeParameters);
            }
            return allTypeParameters;
        }

        @Override
        public Residence residence() {
            return definition.residence();
        }
    }

    private static class ReflectedTypeParameter<T extends java.lang.reflect.GenericDeclaration>
            extends TypeParameter {

        private final GenericDefinition<?, ?> declaredIn;
        private final TypeVariable<T> reflectedTypeParameter;

        private AnyType bound = null;

        ReflectedTypeParameter(GenericDefinition<?, ?> declaredIn, TypeVariable<T> reflectedTypeParameter) {
            this.declaredIn = declaredIn;
            this.reflectedTypeParameter = reflectedTypeParameter;
        }

        @Override
        public String name() {
            return reflectedTypeParameter.getName();
        }

        @Override
        public AnyType bound() {
            if (bound == null) {
                java.lang.reflect.Type[] reflectedBounds = reflectedTypeParameter.getBounds();
                if (reflectedBounds.length == 1)
                    bound = declaredIn.getCodeModel().readReflectedType(reflectedBounds[0]);
                else {
                    List<ObjectType> bounds = new ArrayList<>();
                    for (java.lang.reflect.Type reflectedBound: reflectedBounds) {
                        ObjectType partialBound = declaredIn.getCodeModel().readReflectedType(reflectedBound).getObjectDetails();
                        bounds.add(partialBound);
                    }
                    bound = new IntersectionType(bounds).asAny();
                }
            }
            return bound;
        }

        @Override
        public GenericDefinition<?, ?> declaredIn() {
            return declaredIn;
        }
    }

    private static class ReflectedNesting extends Nesting {

        private final int modifiers;
        private final ObjectDefinition parent;

        ReflectedNesting(int modifiers, ObjectDefinition parent) {
            this.modifiers = modifiers;
            this.parent = parent;
        }

        @Override
        public MemberAccess accessLevel() {
            if ((modifiers & Modifier.PUBLIC) != 0)
                return MemberAccess.PUBLIC;
            else if ((modifiers & Modifier.PROTECTED) != 0)
                return MemberAccess.PROTECTED;
            else if ((modifiers & Modifier.PRIVATE) != 0)
                return MemberAccess.PRIVATE;
            else
                return MemberAccess.PACKAGE;
        }

        @Override
        public boolean isStatic() {
            return (modifiers & Modifier.STATIC) != 0;
        }

        @Override
        public ObjectDefinition parent() {
            return parent;
        }
    }

    private static class ReflectedMethodDefinition extends MethodDefinition {
        private final CodeModel codeModel;
        private final Method method;
        private AnyType returnType = null;

        ReflectedMethodDefinition(CodeModel codeModel, ReflectedExecutableDefinitionImplementation executable, Method method) {
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
    }
    private static class ReflectedExecutableDefinitionImplementation implements ExecutableDefinition.Implementation<MethodType, MethodDefinition> {

        private static final Renderable body = new RenderableUnaccessibleCode();
        private final CodeModel codeModel;
        private final Nesting nesting;
        private final Method method;
        private List<VariableDeclaration> parameters = null;
        private List<AnyType> throwsList = null;

        private ReflectedExecutableDefinitionImplementation(CodeModel codeModel, Nesting nesting, Method method) {
            this.codeModel = codeModel;
            this.nesting = nesting;
            this.method = method;
        }

        @Override
        public TypeParameters typeParameters(ExecutableDefinition<MethodType, MethodDefinition> definition) {
            return new ReflectedTypeParameters<>(definition, method.getTypeParameters());
        }

        @Override
        public List<? extends VariableDeclaration> parameters() {
            if (parameters == null) {
                parameters = new ArrayList<>();
                Parameter[] reflectedParameters = method.getParameters();
                for (Parameter parameter: reflectedParameters) {
                    parameters.add(new ReflectedParameter(codeModel, parameter));
                }
                parameters = Collections.unmodifiableList(parameters);
            }
            return parameters;
        }

        @Override
        public List<? extends AnyType> throwsList() {
            if (throwsList == null) {
                throwsList = new ArrayList<>();
                for (java.lang.reflect.Type exceptionType: method.getGenericExceptionTypes()) {
                    throwsList.add(codeModel.readReflectedType(exceptionType));
                }
                throwsList = Collections.unmodifiableList(throwsList);
            }
            return throwsList;
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

        private final CodeModel codeModel;
        private final Parameter parameter;
        private AnyType type = null;

        ReflectedParameter(CodeModel codeModel, Parameter parameter) {
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
