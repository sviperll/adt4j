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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class ReflectionObjectDefinition extends ObjectDefinition {
    private final Type rawType = GenericType.createRawTypeDetails(new GenericType.Factory<Type, ObjectDefinition>() {
        @Override
        public Type createGenericType(GenericType.Implementation<Type, ObjectDefinition> implementation) {
            return new TypeDetails(implementation).asType();
        }
    });
    private final CodeModel codeModel;
    private final Residence residence;
    private final Class<?> klass;
    private Collection<ObjectDefinition> innerClasses = null;
    private Collection<MethodDefinition> methods = null;

    ReflectionObjectDefinition(CodeModel codeModel, Residence residence, Class<?> klass) {
        super(null);
        this.codeModel = codeModel;
        this.residence = residence;
        this.klass = klass;
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
    public Type extendsClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Type> implementsInterfaces() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<MethodDefinition> methods() {
        if (methods == null) {
            methods = new ArrayList<>();
            for (final Method method: klass.getDeclaredMethods()) {
                Residence methodResidence = Residence.nested(new ReflectedNesting(method.getModifiers(), this));
                methods.add(new ReflectionMethodDefinition(codeModel, methodResidence, method));
            }
        }
        return methods;
    }

    @Override
    public Collection<ObjectDefinition> innerClasses() {
        if (innerClasses == null) {
            innerClasses = new ArrayList<>();
            for (final Class<?> innerClass: klass.getDeclaredClasses()) {
                Residence innerClassResidence = Residence.nested(new ReflectedNesting(innerClass.getModifiers(), this));
                innerClasses.add(new ReflectionObjectDefinition(codeModel, innerClassResidence, innerClass));
            }
        }
        return innerClasses;
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
    public Residence residence() {
        return residence;
    }

    @Override
    public CodeModel getCodeModel() {
        return codeModel;
    }

    @Override
    public Type rawType() {
        return rawType;
    }

    @Override
    public Type rawType(GenericType<?, ?> parentInstanceType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    List<ObjectInitializationElement> staticInitializationElements() {
        return Collections.emptyList();
    }

    @Override
    List<ObjectInitializationElement> instanceInitializationElements() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ConstructorDefinition> constructors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Type internalType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAnonymous() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public class TypeDetails extends ObjectType {
        private final Type type = Type.createObjectType(this);
        TypeDetails(GenericType.Implementation<Type, ObjectDefinition> implementation) {
            super(implementation);
        }

        @Override
        public ObjectDefinition definition() {
            return ReflectionObjectDefinition.this;
        }

        @Override
        public Type asType() {
            return type;
        }

        @Override
        public GenericType<?, ?> capturedEnclosingType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class ReflectedNesting implements Nesting {

        private final int modifiers;
        private final ObjectDefinition parent;

        public ReflectedNesting(int modifiers, ObjectDefinition parent) {
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

    private static class ReflectionMethodDefinition extends MethodDefinition {
        private final CodeModel codeModel;
        private final Residence residence;
        private final Method method;

        public ReflectionMethodDefinition(CodeModel codeModel, Residence residence, Method method) {
            super(null);
            this.codeModel = codeModel;
            this.residence = residence;
            this.method = method;
        }

        @Override
        public boolean isConstructor() {
            return false;
        }

        @Override
        public boolean isMethod() {
            return true;
        }

        @Override
        public MethodDefinition getMethodDetails() {
            return this;
        }

        @Override
        public boolean isFinal() {
            return (method.getModifiers() & Modifier.FINAL) != 0;
        }

        @Override
        public Type returnType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String name() {
            return method.getName();
        }

        @Override
        public MethodType rawType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MethodType rawType(GenericType<?, ?> enclosingType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MethodType internalType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
