/*
 * Copyright (c) 2015, Victor Nazarov <asviraspossible@gmail.com>
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
package com.github.sviperll.adt4j.model.util;

import com.github.sviperll.adt4j.MemberAccess;
import com.github.sviperll.adt4j.model.config.VariableDeclaration;
import com.github.sviperll.adt4j.model.config.VisitorDefinition;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJAnnotatable;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JNarrowedClass;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JTypeWildcard;
import com.helger.jcodemodel.JVar;
import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class Source {
    public static int toJMod(MemberAccess accessLevel) {
        switch (accessLevel) {
            case PRIVATE:
                return JMod.PRIVATE;
            case PACKAGE:
                return JMod.NONE;
            case PROTECTED:
                return JMod.PROTECTED;
            case PUBLIC:
                return JMod.PUBLIC;
            default:
                throw new IllegalStateException("Unsupported AccessLevel: " + accessLevel);
        }
    }

    public static String capitalize(String s) {
        if (s.length() >= 2
            && Character.isHighSurrogate(s.charAt(0))
            && Character.isLowSurrogate(s.charAt(1))) {
            return s.substring(0, 2).toUpperCase(Locale.US) + s.substring(2);
        } else {
            return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1);
        }
    }

    public static String decapitalize(String s) {
        if (s.length() >= 2
            && Character.isHighSurrogate(s.charAt(0))
            && Character.isLowSurrogate(s.charAt(1))) {
            return s.substring(0, 2).toLowerCase(Locale.US) + s.substring(2);
        } else {
            return s.substring(0, 1).toLowerCase(Locale.US) + s.substring(1);
        }
    }

    public static AbstractJType substitute(AbstractJType type, JTypeVar typeVariable, AbstractJType variableValue) {
        if (type == typeVariable)
            return variableValue;
        else if (!(type instanceof AbstractJClass)) {
            return type;
        } else {
            if (type.isArray())
                return substitute(type.elementType(), typeVariable, variableValue).array();
            else if (type instanceof JTypeWildcard) {
                JTypeWildcard wildcard = (JTypeWildcard)type;
                AbstractJClass bound = (AbstractJClass)substitute(wildcard.bound(), typeVariable, variableValue);
                return bound.wildcard(wildcard.boundMode());
            } else {
                /*
                 * When we get type with type-parameters we should substitute
                 * type-parameters.
                 */

                AbstractJClass genericType = (AbstractJClass)type;
                if (genericType.getTypeParameters().isEmpty()) {
                    return genericType;
                } else {
                    AbstractJClass result = genericType.erasure();
                    for (AbstractJClass typeArgument: genericType.getTypeParameters()) {
                        result = result.narrow(substitute(typeArgument, typeVariable, variableValue));
                    }
                    return result;
                }
            }
        }
    }

    public static boolean isNullable(JVar param) {
        return getNullability(param).result();
    }

    public static boolean isNullable(VariableDeclaration param) {
        return getNullability(param).result();
    }

    public static GenerationResult<Boolean> getNullability(JVar param) {
        return getNullability(param.type(), param.name(), param.annotations());
    }

    public static GenerationResult<Boolean> getNullability(VariableDeclaration param) {
        return getNullability(param.type(), param.name(), param.annotations());
    }

    public static GenerationResult<Boolean> getNullability(AbstractJType type, String name, Collection<? extends JAnnotationUse> annotations) {
        boolean hasNonnull = false;
        boolean hasNullable = false;
        for (JAnnotationUse annotationUse: annotations) {
            AbstractJClass annotationClass = annotationUse.getAnnotationClass();
            if (!annotationClass.isError()) {
                String annotationClassName = annotationClass.fullName();
                if (annotationClassName != null) {
                    if (annotationClassName.equals("javax.annotation.Nonnull")) {
                        hasNonnull = true;
                    }
                    if (annotationClassName.equals("javax.annotation.Nullable")) {
                        hasNullable = true;
                    }
                }
            }
        }
        if (hasNonnull && hasNullable)
            return new GenerationResult<>(false, Collections.singletonList(MessageFormat.format("Parameter {0} is declared as both @Nullable and @Nonnull",
                                                           name)));
        if (!type.isReference() && hasNullable)
            return new GenerationResult<>(false, Collections.singletonList(MessageFormat.format("Parameter {0} is non-reference, but declared as @Nullable",
                                                           name)));
        return new GenerationResult<>(hasNullable, Collections.<String>emptyList());
    }

    @SuppressWarnings("unchecked")
    public static void annotateNonnull(IJAnnotatable element) {
        try {
            element.annotate((Class<? extends Annotation>)Class.forName("javax.annotation.Nonnull"));
        } catch (ClassNotFoundException ex) {
            // Skip if no JSR-305 implementation present
        }
    }

    @SuppressWarnings("unchecked")
    public static void annotateNullable(IJAnnotatable element) {
        try {
            element.annotate((Class<? extends Annotation>)Class.forName("javax.annotation.Nullable"));
        } catch (ClassNotFoundException ex) {
            // Skip if no JSR-305 implementation present
        }
    }

    @SuppressWarnings("unchecked")
    public static void annotateParametersAreNonnullByDefault(IJAnnotatable element) {
        try {
            element.annotate((Class<? extends Annotation>)Class.forName("javax.annotation.ParametersAreNonnullByDefault"));
        } catch (ClassNotFoundException ex) {
            // Skip if no JSR-305 implementation present
        }
    }

    public static AbstractJClass narrowType(AbstractJClass type, AbstractJClass[] typeParams) {
        return typeParams.length == 0 ? type : type.narrow(typeParams);
    }

    private Source() {
    }
}
