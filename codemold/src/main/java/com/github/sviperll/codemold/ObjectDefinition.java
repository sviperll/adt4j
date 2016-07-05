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

import com.github.sviperll.codemold.render.Renderer;
import com.github.sviperll.codemold.render.RendererContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class ObjectDefinition extends GenericDefinition<ObjectType, ObjectDefinition>
        implements Model {

    ObjectDefinition() {
    }

    @Override
    public abstract Residence residence();

    public abstract boolean isFinal();

    @Nonnull
    public abstract ObjectKind kind();

    @Nonnull
    public abstract ObjectType extendsClass();

    @Nonnull
    public abstract List<? extends ObjectType> implementsInterfaces();

    @Nonnull
    public abstract List<? extends EnumConstant> enumConstants();

    @Nonnull
    public abstract List<? extends ConstructorDefinition> constructors();

    @Nonnull
    public abstract List<? extends MethodDefinition> methods();

    @Nonnull
    public abstract Collection<? extends ObjectDefinition> innerClasses();

    @Nonnull
    public abstract Collection<? extends FieldDeclaration> fields();

    /**
     * Class' simple name.
     * Throws UnsupportedOperationException for anonymous classes.
     * @see ObjectDefinition#isAnonymous()
     * @throws UnsupportedOperationException
     * @return class' simple name
     */
    @Nonnull
    public abstract String simpleTypeName();

    public abstract boolean isAnonymous();

    @Nonnull
    abstract List<? extends ObjectInitializationElement> staticInitializationElements();

    @Nonnull
    abstract List<? extends ObjectInitializationElement> instanceInitializationElements();

    @Nonnull
    public final String qualifiedTypeName() {
        return residence().getPackage().qualifiedName() + "." + simpleTypeName();
    }

    public final boolean isJavaLangObject() {
        return this == getCodeModel().objectType().definition();
    }

    public boolean declaresConstructors() {
        return !isAnonymous() && kind().declaresConstructors();
    }

    @Override
    final ObjectType createType(GenericType.Implementation<ObjectType, ObjectDefinition> implementation) {
        return new ObjectType(implementation);
    }

    @Override
    final ObjectDefinition fromGenericDefinition() {
        return this;
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

    @Nullable
    final ObjectDefinition getReferenceOrDefault(String relativelyQualifiedName, @Nullable ObjectDefinition defaultValue) {
        int index = relativelyQualifiedName.indexOf('.');
        if (index == 0)
            throw new IllegalArgumentException(relativelyQualifiedName + " illegal name");
        boolean needsToGoDeeper = index >= 0;
        String simpleName = !needsToGoDeeper ? relativelyQualifiedName : relativelyQualifiedName.substring(0, index);
        for (ObjectDefinition innerClass: innerClasses()) {
            if (innerClass.simpleTypeName().equals(simpleName)) {
                if (!needsToGoDeeper)
                    return innerClass;
                else
                    return innerClass.getReferenceOrDefault(relativelyQualifiedName.substring(simpleName.length() + 1), defaultValue);
            }
        }
        return defaultValue;
    }

    @Override
    public final Renderer createRenderer(final RendererContext context) {
        if (isJavaLangObject())
            throw new IllegalStateException("java.lang.Object class definition is not renderable");
        return new Renderer() {
            @Override
            public void render() {
                if (!isAnonymous()) {
                    context.appendRenderable(residence().forObjectKind(kind()));
                    context.appendWhiteSpace();
                    if (!kind().implicitlyFinal() && isFinal())
                        context.appendText("final");
                    context.appendWhiteSpace();
                    context.appendRenderable(kind());
                    context.appendWhiteSpace();
                    context.appendText(simpleTypeName());
                    context.appendRenderable(typeParameters());
                    if (kind().extendsSomeClass() && !extendsClass().isJavaLangObject()) {
                        context.appendText(" extends ");
                        context.appendRenderable(extendsClass());
                    }
                    if (kind().implementsSomeInterfaces()) {
                        Iterator<? extends ObjectType> interfaces = implementsInterfaces().iterator();
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
                }
                context.appendText("{");
                context.appendLineBreak();
                RendererContext nestedContext = context.indented();

                if (kind().isEnum()) {
                    Iterator<? extends EnumConstant> iterator = enumConstants().iterator();
                    nestedContext.appendRenderable(iterator.next().definition());
                    while (iterator.hasNext()) {
                        nestedContext.appendText(", ");
                        nestedContext.appendRenderable(iterator.next().definition());
                    }
                    nestedContext.appendText(";");
                    nestedContext.appendLineBreak();
                }

                for (ObjectInitializationElement element: staticInitializationElements()) {
                    nestedContext.appendRenderable(element);
                }

                for (MethodDefinition method: methods()) {
                    if (method.isStatic()) {
                        nestedContext.appendEmptyLine();
                        nestedContext.appendRenderable(method);
                    }
                }

                for (ObjectInitializationElement element: instanceInitializationElements()) {
                    nestedContext.appendRenderable(element);
                }

                if (declaresConstructors()) {
                    for (ConstructorDefinition constructor: constructors()) {
                        nestedContext.appendEmptyLine();
                        nestedContext.appendRenderable(constructor);
                    }
                }

                for (MethodDefinition method: methods()) {
                    if (!method.isStatic()) {
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
            }
        };
    }
}
