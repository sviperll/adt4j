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

package com.github.sviperll.codemodel.api;

import com.github.sviperll.codemodel.AnonymousClassBuilder;
import com.github.sviperll.codemodel.BlockBuilder;
import com.github.sviperll.codemodel.ClassBuilder;
import com.github.sviperll.codemodel.CodeModel;
import com.github.sviperll.codemodel.CodeModelException;
import com.github.sviperll.codemodel.Consumer;
import com.github.sviperll.codemodel.Expression;
import com.github.sviperll.codemodel.MemberAccess;
import com.github.sviperll.codemodel.MethodBuilder;
import com.github.sviperll.codemodel.MethodType;
import com.github.sviperll.codemodel.ObjectDefinition;
import com.github.sviperll.codemodel.ObjectType;
import com.github.sviperll.codemodel.Package;
import com.github.sviperll.codemodel.PackageLevelBuilder;
import com.github.sviperll.codemodel.AnyType;
import com.github.sviperll.codemodel.Types;
import com.github.sviperll.codemodel.VariableDeclaration;
import com.github.sviperll.codemodel.render.RendererContexts;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class ExpressionContextTest {
    @Test
    public void smokeAnonymousClass() throws CodeModelException {
        CodeModel.Builder builder = CodeModel.createBuilder();
        CodeModel codeModel = builder.build();
        final ObjectType stringType = codeModel.getReferenceOrDefault(String.class.getName(), null).rawType();
        ObjectDefinition comparatorDefinition = codeModel.getReferenceOrDefault(Comparator.class.getName(), null);
        ObjectType stringComparatorType = comparatorDefinition.rawType().narrow(Arrays.asList(stringType));

        Package pkg = codeModel.getPackage("com.github.sviperll.test.generated");
        ClassBuilder<PackageLevelBuilder> testA = pkg.createClass("TestA");
        MethodBuilder method = testA.method("test1");
        BlockBuilder body = method.body();
        body.variable(stringComparatorType, "comparator", stringComparatorType.instantiation(Expression.emptyList(), body, new Consumer<AnonymousClassBuilder>() {
            @Override
            public void accept(AnonymousClassBuilder builder) {
                try {
                    MethodBuilder compareMethod = builder.method("compare");
                    VariableDeclaration a = compareMethod.addParameter(stringType, "a");
                    VariableDeclaration b = compareMethod.addParameter(stringType, "b");
                    compareMethod.resultType(Types.intType());
                    compareMethod.setAccessLevel(MemberAccess.PUBLIC);
                    MethodType compareToMethod = null;
                    for (MethodType method: stringType.methods()) {
                        if (method.name().equals("compareTo"))
                            compareToMethod = method;
                    }
                    compareMethod.body().returnStatement(Expression.variable(a.name()).invocation(compareToMethod, Arrays.asList(Expression.variable(b.name()))));
                } catch (CodeModelException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }));
        String expected =
            "class TestA {\n"
         +  "\n"
         +  "    void test1() {\n"
         +  "        java.util.Comparator<java.lang.String> comparator = new java.util.Comparator<java.lang.String>() {\n"
         +  "\n"
         +  "            public int compare(java.lang.String a, java.lang.String b) {\n"
         +  "                return a.compareTo(b);\n"
         +  "            }\n"
         +  "        };\n"
         +  "    }\n"
         +  "}";
        StringBuilder stringBuilder = new StringBuilder();
        RendererContexts.createInstance(stringBuilder).appendRenderable(testA.definition());
        assertEquals(expected, stringBuilder.toString());
    }

}
