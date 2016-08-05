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

package com.github.sviperll.codemold.test;

import com.github.sviperll.codemold.Type;
import com.github.sviperll.codemold.AnyType;
import com.github.sviperll.codemold.CodeMold;
import com.github.sviperll.codemold.CodeMoldException;
import com.github.sviperll.codemold.ConstructorDefinition;
import com.github.sviperll.codemold.MethodDefinition;
import com.github.sviperll.codemold.ObjectDefinition;
import com.github.sviperll.codemold.TypeParameter;
import com.github.sviperll.codemold.render.RendererContexts;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class ReflectionTest {
    @Test
    public void smokeReflectedObject() throws CodeMoldException {
        CodeMold.Builder builder = CodeMold.createBuilder();
        CodeMold codeModel = builder.build();
        ObjectDefinition stringDefinition = codeModel.getReference(String.class);
        StringBuilder stringBuilder = new StringBuilder();
        RendererContexts.createInstance(stringBuilder).appendRenderable(stringDefinition);

        assertTrue(stringDefinition.kind().isClass());

        assertTrue(stringDefinition.typeParameters().all().size() == 0);

        Predicate<MethodDefinition> isValueOfIntMethod = (definition) -> {
            return definition.name().equals("valueOf")
                        && definition.isStatic()
                        && definition.parameters().size() == 1
                        && isInt(definition.parameters().get(0).type());
        };
        Optional<? extends MethodDefinition> optionalValueOfDefinition;
        optionalValueOfDefinition = stringDefinition.methods().stream().filter(isValueOfIntMethod).findFirst();
        assertTrue(optionalValueOfDefinition.isPresent());
        MethodDefinition valueOfDefinition = optionalValueOfDefinition.orElseThrow(() -> new IllegalStateException());
        assertTrue(isString(valueOfDefinition.returnType()));

        Predicate<ConstructorDefinition> isConstructorOfCharArray = (constructor) -> {
            return constructor.parameters().size() == 1
                    && isArrayOfChars(constructor.parameters().get(0).type());
        };
        Optional<? extends ConstructorDefinition> constructorOfCharArray;
        constructorOfCharArray = stringDefinition.constructors().stream().filter(isConstructorOfCharArray).findFirst();
        assertTrue(constructorOfCharArray.isPresent());

        Predicate<MethodDefinition> isCharAt = (definition) -> {
            return definition.name().equals("charAt")
                        && !definition.isStatic()
                        && definition.parameters().size() == 1
                        && isInt(definition.parameters().get(0).type());
        };
        Optional<? extends MethodDefinition> optionalCharAtDefinition;
        optionalCharAtDefinition = stringDefinition.methods().stream().filter(isCharAt).findFirst();
        assertTrue(optionalCharAtDefinition.isPresent());
        MethodDefinition charAtDefinition = optionalCharAtDefinition.orElseThrow(() -> new IllegalStateException());
        assertTrue(isChar(charAtDefinition.returnType()));
    }

    @Test
    public void smokeReflectedInterface() throws CodeMoldException {
        CodeMold.Builder builder = CodeMold.createBuilder();
        CodeMold codeModel = builder.build();
        ObjectDefinition comparableDefinition = codeModel.getReference(Comparable.class);
        StringBuilder stringBuilder = new StringBuilder();
        RendererContexts.createInstance(stringBuilder).appendRenderable(comparableDefinition);
        System.out.println(stringBuilder.toString());

        assertTrue(comparableDefinition.kind().isInterface());

        assertTrue(comparableDefinition.typeParameters().all().size() == 1);
        TypeParameter typeParameter = comparableDefinition.typeParameters().all().get(0);
        Predicate<Type> isUsedTypeParameter = (type) -> {
            AnyType any = type.asAny();
            return any.isTypeVariable() && any.getVariableDetails().name().equals(typeParameter.name());
        };
        Predicate<MethodDefinition> isCompareTo = (definition) -> {
            return definition.name().equals("compareTo")
                        && !definition.isStatic()
                        && definition.parameters().size() == 1
                        && isUsedTypeParameter.test(definition.parameters().get(0).type());
        };
        Optional<? extends MethodDefinition> optionalCompareToDefinition;
        optionalCompareToDefinition = comparableDefinition.methods().stream().filter(isCompareTo).findFirst();
        assertTrue(optionalCompareToDefinition.isPresent());
        MethodDefinition compareToDefinition = optionalCompareToDefinition.orElseThrow(() -> new IllegalStateException());
        assertTrue(isInt(compareToDefinition.returnType()));
    }

    private boolean isInt(Type type) {
        AnyType any = type.asAny();
        return any.isPrimitive() && any.getPrimitiveDetails().isInteger();
    }

    private boolean isChar(Type type) {
        AnyType any = type.asAny();
        return any.isPrimitive() && any.getPrimitiveDetails().isCharacter();
    }

    private boolean isArrayOfChars(Type type) {
        AnyType any = type.asAny();
        return any.isArray() && isChar(any.getArrayDetails().elementType());
    }

    private boolean isString(Type type) {
        AnyType any = type.asAny();
        if (!any.isObjectType())
            return false;
        else {
            ObjectDefinition definition = any.getObjectDetails().definition();
            return definition == definition.getCodeMold().getReference(String.class);
        }
    }

}
