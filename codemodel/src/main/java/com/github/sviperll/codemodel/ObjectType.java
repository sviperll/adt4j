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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class ObjectType extends GenericType<ObjectType, ObjectDefinition>
        implements Renderable, Type {
    private final AnyType type = AnyType.wrapObjectType(this);
    private Map<MethodSignature, MethodType> methods = null;
    private Map<ObjectDefinition, ObjectType> interfaces = null;
    private List<ConstructorType> constructors = null;
    private List<ObjectType> supertypes = null;
    private ObjectType superClass = null;

    ObjectType(GenericType.Implementation<ObjectType, ObjectDefinition> implementation) {
        super(implementation);
    }

    @Override
    public final AnyType asAny() {
        return type;
    }

    @Nonnull
    public final IntersectionType intersection(ObjectType that) {
        return new IntersectionType(Arrays.asList(this, that));
    }

    @Nonnull
    public final Expression instanceofOp(Expression expression) throws CodeModelException {
        return expression.instanceofOp(this);
    }

    final boolean containsWildcards() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final boolean sameDefinition(ObjectType that) {
        return definition() == that.definition();
    }

    /**
     * All methods supported by this object.
     * Inherited methods are included.
     * @return all methods supported by this object
     */
    @Nonnull
    public final Collection<? extends MethodType> methods() {
        if (methods == null) {
            methods = new HashMap<>();
            for (ObjectType supertype: interfaces()) {
                for (MethodType method: supertype.methods())
                    methods.put(method.signature(), method);
            }
            if (!isJavaLangObject()) {
                for (MethodType method: superClass().methods()) {
                    methods.put(method.signature(), method);
                }
            }
            for (MethodDefinition definition: definition().methods()) {
                MethodType method = definition.isStatic() ? definition.rawType() : definition.rawType(this);
                methods.put(method.signature(), method);
            }
            methods = Collections.unmodifiableMap(methods);
        }
        return methods.values();
    }

    /**
     * All interfaces supported by this object.
     * Interfaces are listed regardless wheather they are explicitly implemented or inherited somehow.
     * @return All interfaces supported by this object.
     */
    public Collection<? extends ObjectType> interfaces() {
        if (interfaces == null) {
            interfaces = new HashMap<>();
            if (!isJavaLangObject()) {
                for (ObjectType iface: superClass().interfaces()) {
                    interfaces.put(iface.definition(), iface);
                }
            }
            for (ObjectType declaredInterface: definition().implementsInterfaces()) {
                ObjectType iface = declaredInterface.substitute(definitionEnvironment());
                interfaces.put(iface.definition(), iface);
                for (ObjectType subinterface: iface.interfaces()) {
                    interfaces.put(subinterface.definition(), subinterface);
                }
            }
            interfaces = Collections.unmodifiableMap(interfaces);
        }
        return interfaces.values();
    }

    /**
     * Directly extended class.
     * For interfaces superClass is java.lang.Object.
     * @return Directly extended class
     */
    public ObjectType superClass() {
        if (superClass == null) {
            superClass = definition().extendsClass().substitute(definitionEnvironment());
        }
        return superClass;
    }

    /**
     * All directly extended/implemented types.
     * For interfaces #supertypes() is a list of extended interfaces or
     * single java.lang.Object type when no interfaces are extended.
     * @return All directly extended/implemented types.
     */
    public Collection<? extends ObjectType> supertypes() {
        if (supertypes == null) {
            supertypes = new ArrayList<>();
            if (definition().kind().isInterface() && definition().implementsInterfaces().isEmpty()) {
                supertypes.add(definition().getCodeModel().objectType());
            } else {
                if (!isJavaLangObject()) {
                    supertypes.add(superClass());
                }
                for (ObjectType iface: definition().implementsInterfaces()) {
                    supertypes.add(iface.substitute(definitionEnvironment()));
                }
            }
            supertypes = Collections.unmodifiableList(supertypes);
        }
        return supertypes;
    }

    @Nonnull
    public final Collection<? extends ConstructorType> constructors() {
        if (constructors == null) {
            constructors = new ArrayList<>(definition().constructors().size());
            for (final ConstructorDefinition definition: definition().constructors()) {
                constructors.add(definition.rawType(this));
            }
            constructors = Collections.unmodifiableList(constructors);
        }
        return constructors;
    }

    @Nonnull
    public Expression instantiation(final List<? extends Expression> arguments) {
        return Expression.instantiation(this, arguments);
    }

    @Nonnull
    public Expression instantiation(final List<? extends Expression> arguments, ExpressionContext context, Consumer<? super AnonymousClassBuilder> anonymousClassDefinition) {
        return Expression.instantiation(this, arguments, context, anonymousClassDefinition);
    }

    @Nonnull
    public Expression rawInstantiation(final List<? extends Expression> arguments) {
        return Expression.rawInstantiation(this, arguments);
    }

    public boolean isJavaLangObject() {
        return definition().isJavaLangObject();
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                if (isRaw())
                    context.appendQualifiedClassName(definition().qualifiedTypeName());
                else {
                    context.appendRenderable(erasure());
                    Iterator<? extends AnyType> iterator = typeArguments().iterator();
                    if (iterator.hasNext()) {
                        context.appendText("<");
                        context.appendRenderable(iterator.next());
                        while (iterator.hasNext()) {
                            context.appendText(", ");
                            context.appendRenderable(iterator.next());
                        }
                        context.appendText(">");
                    }
                }
            }
        };
    }

}
