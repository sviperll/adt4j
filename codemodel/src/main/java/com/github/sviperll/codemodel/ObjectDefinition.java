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

import com.github.sviperll.codemodel.render.Renderer;
import com.github.sviperll.codemodel.render.RendererContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class ObjectDefinition extends GenericDefinition<ObjectType, ObjectDefinition> {

    ObjectDefinition() {
    }

    public abstract boolean isFinal();

    public abstract ObjectKind kind();

    public abstract ObjectType extendsClass();

    public abstract List<ObjectType> implementsInterfaces();

    public abstract Collection<ConstructorDefinition> constructors();

    public abstract Collection<MethodDefinition> methods();

    public abstract Collection<ObjectDefinition> innerClasses();

    public abstract Collection<FieldDeclaration> fields();

    public abstract String simpleName();

    public abstract boolean isAnonymous();

    abstract List<ObjectInitializationElement> staticInitializationElements();

    abstract List<ObjectInitializationElement> instanceInitializationElements();

    public final String qualifiedName() {
        return residence().getPackage().qualifiedName() + "." + simpleName();
    }

    public final boolean isJavaLangObject() {
        return this == getCodeModel().objectType().definition();
    }

    @Override
    final ObjectType createType(GenericType.Implementation<ObjectType, ObjectDefinition> implementation) {
        return new DefinedType(implementation);
    }

    public final boolean extendsOrImplements(ObjectDefinition objectDefinition) {
        if (this.extendsClass().definition() == objectDefinition
                || this.extendsClass().definition().extendsOrImplements(objectDefinition))
            return true;
        else {
            for (ObjectType implementedInterface: implementsInterfaces()) {
                if (implementedInterface.definition() == objectDefinition
                        || implementedInterface.definition().extendsOrImplements(objectDefinition))
                    return true;
            }
            return false;
        }
    }

    final ObjectDefinition reference(String relativelyQualifiedName) {
        int index = relativelyQualifiedName.indexOf('.');
        if (index == 0)
            throw new IllegalArgumentException(relativelyQualifiedName + " illegal name");
        boolean needsToGoDeeper = index >= 0;
        String simpleName = !needsToGoDeeper ? relativelyQualifiedName : relativelyQualifiedName.substring(0, index);
        for (ObjectDefinition innerClass: innerClasses()) {
            if (innerClass.simpleName().equals(simpleName)) {
                if (!needsToGoDeeper)
                    return innerClass;
                else
                    return innerClass.reference(relativelyQualifiedName.substring(simpleName.length() + 1));
            }
        }
        return null;
    }

    @Override
    public final Renderer createRenderer(final RendererContext context) {
        if (isJavaLangObject())
            throw new IllegalStateException("java.lang.Object class definition is not renderable");
        return new Renderer() {
            @Override
            public void render() {
                if (!kind().implicitlyStatic() || !residence().isNested())
                    context.appendRenderable(residence());
                else {
                    Nesting nesting = residence().getNesting();
                    context.appendRenderable(nesting.accessLevel());
                }
                context.appendWhiteSpace();
                if (!kind().implicitlyFinal() && isFinal())
                    context.appendText("final");
                context.appendWhiteSpace();
                context.appendRenderable(kind());
                context.appendWhiteSpace();
                context.appendText(simpleName());
                context.appendRenderable(typeParameters());
                if (kind().extendsSomeClass() && !extendsClass().isJavaLangObject()) {
                    context.appendText(" extends ");
                    context.appendRenderable(extendsClass());
                }
                if (kind().implementsSomeInterfaces()) {
                    Iterator<ObjectType> interfaces = implementsInterfaces().iterator();
                    if (interfaces.hasNext()) {
                        if (kind().isInterface())
                            context.appendText(" extends ");
                        else
                            context.appendText(" implements ");
                        ObjectType implementedInterface = interfaces.next();
                        context.appendRenderable(implementedInterface);
                        while (interfaces.hasNext()) {
                            context.appendText(", ");
                            implementedInterface = interfaces.next();
                            context.appendRenderable(implementedInterface);
                        }
                    }
                }
                context.appendWhiteSpace();
                context.appendText("{");
                context.appendLineBreak();
                RendererContext nestedContext = context.indented();

                for (ObjectInitializationElement element: staticInitializationElements()) {
                    nestedContext.appendRenderable(element);
                }

                for (MethodDefinition method: methods()) {
                    if (method.residence().getNesting().isStatic()) {
                        nestedContext.appendEmptyLine();
                        nestedContext.appendRenderable(method);
                    }
                }

                for (ObjectInitializationElement element: instanceInitializationElements()) {
                    nestedContext.appendRenderable(element);
                }

                for (ConstructorDefinition constructor: constructors()) {
                    nestedContext.appendEmptyLine();
                    nestedContext.appendRenderable(constructor);
                }

                for (MethodDefinition method: methods()) {
                    if (!method.residence().getNesting().isStatic()) {
                        nestedContext.appendEmptyLine();
                        nestedContext.appendRenderable(method);
                    }
                }

                for (ObjectDefinition innerClass: innerClasses()) {
                    if (!innerClass.residence().getNesting().isStatic()) {
                        nestedContext.appendEmptyLine();
                        nestedContext.appendRenderable(innerClass);
                    }
                }

                for (ObjectDefinition innerClass: innerClasses()) {
                    if (innerClass.residence().getNesting().isStatic()) {
                        nestedContext.appendEmptyLine();
                        nestedContext.appendRenderable(innerClass);
                    }
                }

                context.appendText("}");
                context.appendLineBreak();
            }

        };
    }

    private class DefinedType extends ObjectType {
        DefinedType(GenericType.Implementation<ObjectType, ObjectDefinition> implementation) {
            super(implementation);
        }

        @Override
        public ObjectDefinition definition() {
            return ObjectDefinition.this;
        }

    }


}
