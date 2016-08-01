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
import com.github.sviperll.codemold.util.CMCollections;
import com.github.sviperll.codemold.util.Snapshot;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class ReflectedObjectDefinition<T> extends ObjectDefinition {
    private final CodeMold codeModel;
    private final Residence residence;
    private final Class<T> klass;
    private List<? extends ObjectDefinition> innerClasses = null;
    private List<? extends MethodDefinition> methods = null;
    private final TypeParameters typeParameters;
    private List<? extends ObjectType> implementsInterfaces = null;
    private ObjectType extendsClass = null;

    ReflectedObjectDefinition(CodeMold codeModel, Residence residence, Class<T> klass) {
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
        if (klass.isInterface() && !klass.isAnnotation()) {
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
            List<ObjectType> implementsInterfacesBuilder = CMCollections.newArrayList();
            for (java.lang.reflect.Type reflectedInterface: klass.getGenericInterfaces()) {
                implementsInterfacesBuilder.add(codeModel.readReflectedType(reflectedInterface).getObjectDetails());
            }
            implementsInterfaces = Snapshot.of(implementsInterfacesBuilder);
        }
        return Snapshot.of(implementsInterfaces);
    }

    @Override
    public List<? extends MethodDefinition> methods() {
        if (methods == null) {
            List<MethodDefinition> methodsBuilder = CMCollections.newArrayList();
            for (final Method method: klass.getDeclaredMethods()) {
                Nesting methodResidence = new ReflectedNesting(method.getModifiers(), this);
                methodsBuilder.add(ReflectedMethodDefinition.createInstance(codeModel, methodResidence, method));
            }
            methods = Snapshot.of(methodsBuilder);
        }
        return Snapshot.of(methods);
    }

    @Override
    public Collection<? extends ObjectDefinition> innerClasses() {
        if (innerClasses == null) {
            List<ObjectDefinition> innerClassesBuilder = CMCollections.newArrayList();
            for (final Class<?> innerClass: klass.getDeclaredClasses()) {
                Residence innerClassResidence = new ReflectedNesting(innerClass.getModifiers(), this).residence();
                innerClassesBuilder.add(new ReflectedObjectDefinition<>(codeModel, innerClassResidence, innerClass));
            }
            innerClasses = Snapshot.of(innerClassesBuilder);
        }
        return Snapshot.of(innerClasses);
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
    public CodeMold getCodeMold() {
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

    @Override
    public List<? extends Annotation> getAnnotation(ObjectDefinition definition) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends Annotation> allAnnotations() {
        throw new UnsupportedOperationException("Not supported yet.");
    }



}
