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
import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.Nonnull;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
class Reflection implements Model {
    private static final Logger logger = Logger.getLogger(Reflection.class.getName());
    private static final Renderable RENDERABLE_UNACCESSIBLE_CODE = new RenderableUnaccessibleCode();

    static Renderable renderableUnaccessibleCode() {
        return RENDERABLE_UNACCESSIBLE_CODE;
    }

    private final CodeMold codeMold;
    Reflection(CodeMold codeMold) {
        this.codeMold = codeMold;

    }

    @Nonnull
    AnyType readReflectedType(java.lang.reflect.Type genericReflectedType) {
        if (genericReflectedType instanceof ParameterizedType) {
            ParameterizedType reflectedType = (ParameterizedType)genericReflectedType;
            ObjectType rawType = readReflectedType(reflectedType.getRawType()).getObjectDetails();
            List<AnyType> arguments = CMCollections.newArrayList();
            for (java.lang.reflect.Type reflectedArgumentType: reflectedType.getActualTypeArguments()) {
                arguments.add(readReflectedType(reflectedArgumentType));
            }
            return rawType.narrow(arguments).asAny();
        } else if (genericReflectedType instanceof GenericArrayType) {
            GenericArrayType reflectedType = (GenericArrayType)genericReflectedType;
            AnyType componentType = readReflectedType(reflectedType.getGenericComponentType());
            return Types.arrayOf(componentType).asAny();
        } else if (genericReflectedType instanceof java.lang.reflect.WildcardType) {
            java.lang.reflect.WildcardType reflectedType = (java.lang.reflect.WildcardType)genericReflectedType;
            java.lang.reflect.Type[] reflectedLowerBounds = reflectedType.getLowerBounds();
            if (reflectedLowerBounds.length != 0) {
                AnyType bound = readReflectedType(reflectedLowerBounds[0]);
                return Types.wildcardSuper(bound).asAny();
            } else {
                java.lang.reflect.Type[] reflectedUpperBounds = reflectedType.getUpperBounds();
                AnyType bound = readReflectedType(reflectedUpperBounds[0]);
                return Types.wildcardExtends(bound).asAny();
            }
        } else if (genericReflectedType instanceof java.lang.reflect.TypeVariable) {
            java.lang.reflect.TypeVariable<?> reflectedType = (java.lang.reflect.TypeVariable<?>)genericReflectedType;
            return Types.variable(reflectedType.getName()).asAny();
        } else if (genericReflectedType instanceof Class) {
            Class<?> reflectedType = (Class<?>)genericReflectedType;
            if (reflectedType.isPrimitive()) {
                String name = reflectedType.getName();
                if (name.equals("void"))
                    return AnyType.voidType();
                else
                    return PrimitiveType.valueOf(name.toUpperCase(Locale.US)).asAny();
            } else if (reflectedType.isArray()) {
                return Types.arrayOf(readReflectedType(reflectedType.getComponentType())).asAny();
            } else {
                return codeMold.getReference(reflectedType).rawType().asAny();
            }
        } else
            throw new UnsupportedOperationException("Can't read " + genericReflectedType);
    }

    List<? extends AnyType> buildTypesFromReflections(java.lang.reflect.Type[] types) {
        List<AnyType> throwsListBuilder = CMCollections.newArrayList();
        for (java.lang.reflect.Type exceptionType : types) {
            throwsListBuilder.add(readReflectedType(exceptionType));
        }
        return Snapshot.of(throwsListBuilder);
    }
    List<? extends VariableDeclaration> createParameterList(Parameter[] reflectedParameters) {
        List<VariableDeclaration> parametersBuilder = CMCollections.newArrayList();
        for (Parameter parameter : reflectedParameters) {
            parametersBuilder.add(new ReflectedParameter(this, parameter));
        }
        return Snapshot.of(parametersBuilder);
    }

    ObjectDefinition getReference(Class<? extends Annotation> klass) {
        return codeMold.getReference(klass);
    }

    @Override
    public CodeMold getCodeMold() {
        return codeMold;
    }

    AnnotationCollection readAnnotationCollection(java.lang.annotation.Annotation[] reflectionAnnotations) {
        AnnotationCollection.Builder collection = AnnotationCollection.createBuilder();
        for(java.lang.annotation.Annotation reflectionAnnotation: reflectionAnnotations) {
            Class<? extends java.lang.annotation.Annotation> reflectionAnnotationType = reflectionAnnotation.annotationType();
            ObjectDefinition annotationType = getReference(reflectionAnnotationType);
            com.github.sviperll.codemold.Annotation.Builder builder = com.github.sviperll.codemold.Annotation.createBuilder(annotationType);
            for (Method method: reflectionAnnotationType.getDeclaredMethods()) {
                String parameterName = method.getName();
                try {
                    Object reflectionValue = method.invoke(reflectionAnnotation);
                    builder.set(parameterName, CompileTimeValues.fromObject(reflectionValue));
                } catch (IllegalAccessException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalStateException(ex);
                } catch (InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            collection.annotate(builder.build());
        }
        return collection.build();
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
