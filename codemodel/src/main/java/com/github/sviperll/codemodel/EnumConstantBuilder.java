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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class EnumConstantBuilder extends ObjectBuilder<NestingBuilder> {
    private final String name;
    private final List<Expression> constructorArguments;
    private EnumConstant constant = null;
    EnumConstantBuilder(String name, List<Expression> constructorArguments, NestingBuilder nesting) {
        super(ObjectKind.ENUM_CONSTANT, nesting);
        this.name = name;
        this.constructorArguments = Collections.unmodifiableList(new ArrayList<>(constructorArguments));
    }

    @Override
    ObjectDefinition createDefinition(TypeParameters typeParameters) {
        return new BuiltDefinition(typeParameters);
    }

    @Override
    public FieldBuilder field(Type type, String name) throws CodeModelException {
        return super.field(type, name);
    }

    @Override
    public ClassBuilder<NestingBuilder> innerClass(String name) throws CodeModelException {
        return super.innerClass(name);
    }

    @Override
    public MethodBuilder method(String name) throws CodeModelException {
        return super.method(name);
    }

    EnumConstant enumConstant() {
        if (constant == null) {
            ObjectDefinition enumDefinition = residence().residence().getNesting().parent();
            constant = new BuiltConstant(enumDefinition, name, constructorArguments, definition());
        }
        return constant;
    }

    private static class BuiltConstant extends EnumConstant {
        private final ObjectDefinition enumDefinition;
        private final String name;
        private final List<Expression> constructorArguments;
        private final ObjectDefinition members;
        BuiltConstant(ObjectDefinition enumDefinition, String name, List<Expression> constructorArguments, ObjectDefinition members) {
            this.enumDefinition = enumDefinition;
            this.name = name;
            this.constructorArguments = constructorArguments;
            this.members = members;
        }


        @Override
        public ObjectDefinition enumDefinition() {
            return enumDefinition;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        Renderable definition() {
            return new Renderable() {
                @Override
                public Renderer createRenderer(final RendererContext context) {
                    return new Renderer() {
                        @Override
                        public void render() {
                            context.appendText(name);
                            Iterator<Expression> iterator = constructorArguments.iterator();
                            if (iterator.hasNext()) {
                                context.appendText("(");
                                context.appendRenderable(iterator.next());
                                while (iterator.hasNext()) {
                                    context.appendText(", ");
                                    context.appendRenderable(iterator.next());
                                }
                                context.appendText(")");
                            }
                            if (!(members.fields().isEmpty() && members.methods().isEmpty())) {
                                context.appendWhiteSpace();
                                context.appendRenderable(members);
                            }
                        }
                    };
                }
            };
        }

    }

    class BuiltDefinition extends ObjectBuilder<NestingBuilder>.BuiltDefinition {
        BuiltDefinition(TypeParameters typeParameters) {
            super(typeParameters);
        }

        @Override
        public boolean isFinal() {
            return true;
        }

        @Override
        public ObjectType extendsClass() {
            return residence().getNesting().parent().rawType();
        }

        @Override
        public List<ObjectType> implementsInterfaces() {
            return Collections.<ObjectType>emptyList();
        }

        @Override
        public Collection<ConstructorDefinition> constructors() {
            throw new UnsupportedOperationException("Constructors are listed for class definitions only. Use kind() method to check for object kind.");
        }

        @Override
        public String simpleTypeName() {
            throw new UnsupportedOperationException("Enum constant definitions are nameless, but constants itself are not. Use enumConstants() method to get actual constants.");
        }

        @Override
        public boolean isAnonymous() {
            return true;
        }

        @Override
        public Collection<EnumConstant> enumConstants() {
            throw new UnsupportedOperationException("Enum constants are listed for enum definitions only. Use kind() method to check for object kind.");
        }
    }
}
