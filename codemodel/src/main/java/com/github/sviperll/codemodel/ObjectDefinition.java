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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class ObjectDefinition implements GenericDefinition {

    ObjectDefinition() {
    }


    public abstract boolean isFinal();

    public abstract ObjectKind kind();

    public abstract Type extendsClass();

    public abstract List<Type> implementsInterfaces();

    public abstract Collection<ExecutableDefinition> constructors();

    public abstract Collection<ExecutableDefinition> methods();

    public abstract Collection<ObjectDefinition> innerClasses();

    public abstract Collection<FieldDeclaration> fields();

    public abstract String simpleName();

    abstract List<ObjectInitializationElement> staticInitializationElements();

    abstract List<ObjectInitializationElement> instanceInitializationElements();

    public final String qualifiedName() {
        return residence().getPackage().qualifiedName() + "." + simpleName();
    }

    public final boolean extendsOrImplements(ObjectDefinition objectDefinition) {
        if (this.extendsClass().getObjectDetails().definition() == objectDefinition
                || this.extendsClass().getObjectDetails().definition().extendsOrImplements(objectDefinition))
            return true;
        else {
            for (Type implementedInterface: implementsInterfaces()) {
                if (implementedInterface.getObjectDetails().definition() == objectDefinition
                        || implementedInterface.getObjectDetails().definition().extendsOrImplements(objectDefinition))
                    return true;
            }
            return false;
        }
    }

    @Override
    public final Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                context.appendRenderable(residence());
                context.appendWhiteSpace();
                if (isFinal())
                    context.appendText("final");
                context.appendWhiteSpace();
                if (kind() == ObjectKind.ANNOTATION)
                    context.appendText("@interface");
                else
                    context.appendText(kind().name().toLowerCase(Locale.US));
                context.appendWhiteSpace();
                context.appendText(simpleName());
                context.appendRenderable(generics());
                context.appendText(" extends ");
                context.appendRenderable(extendsClass());
                Iterator<Type> interfaces = implementsInterfaces().iterator();
                if (interfaces.hasNext()) {
                    context.appendText(" implements ");
                    Type implementedInterface = interfaces.next();
                    context.appendRenderable(implementedInterface);
                    while (interfaces.hasNext()) {
                        context.appendText(", ");
                        implementedInterface = interfaces.next();
                        context.appendRenderable(implementedInterface);
                    }
                }
                context.appendWhiteSpace();
                context.appendText("{");
                context.appendLineBreak();
                RendererContext nestedContext = context.indented();

                for (ObjectInitializationElement element: staticInitializationElements()) {
                    nestedContext.appendRenderable(element);
                }

                for (ExecutableDefinition method: methods()) {
                    if (method.residence().getNesting().isStatic()) {
                        nestedContext.appendEmptyLine();
                        nestedContext.appendRenderable(method);
                    }
                }

                for (ObjectInitializationElement element: instanceInitializationElements()) {
                    nestedContext.appendRenderable(element);
                }

                for (ExecutableDefinition constructor: constructors()) {
                    nestedContext.appendEmptyLine();
                    nestedContext.appendRenderable(constructor);
                }

                for (ExecutableDefinition method: methods()) {
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
}
