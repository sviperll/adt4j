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

import com.github.sviperll.codemold.AnonymousClassBuilder;
import com.github.sviperll.codemold.AnyType;
import com.github.sviperll.codemold.ClassBuilder;
import com.github.sviperll.codemold.CodeMold;
import com.github.sviperll.codemold.CodeMoldException;
import com.github.sviperll.codemold.EnumBuilder;
import com.github.sviperll.codemold.Expression;
import com.github.sviperll.codemold.FieldBuilder;
import com.github.sviperll.codemold.InterfaceBuilder;
import com.github.sviperll.codemold.MemberAccess;
import com.github.sviperll.codemold.MethodBuilder;
import com.github.sviperll.codemold.MethodType;
import com.github.sviperll.codemold.ObjectDefinition;
import com.github.sviperll.codemold.ObjectType;
import com.github.sviperll.codemold.Package;
import com.github.sviperll.codemold.PackageLevelBuilder;
import com.github.sviperll.codemold.Types;
import com.github.sviperll.codemold.render.RendererContexts;
import com.github.sviperll.codemold.util.Consumer;
import com.github.sviperll.codemold.util.OnMissing;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class ObjectDefinitionTest {
    @Test
    public void smokeReflectedObjectShouldBePrintable() throws CodeMoldException {
        CodeMold.Builder builder = CodeMold.createBuilder();
        CodeMold codeModel = builder.build();
        StringBuilder builder1 = new StringBuilder();
        RendererContexts.createInstance(builder1).appendRenderable(codeModel.getReference(String.class.getName(), OnMissing.<ObjectDefinition>throwNullPointerException()));
    }

    @Test
    public void smokePrettyPrintingClass() throws CodeMoldException {
        ObjectDefinition test1 = buildClass();
        String result =
            "class Test1<T> {\n" +
            "    private int field1;\n" +
            "    protected T field2;\n" +
            "\n" +
            "    public int test(int param1) {\n" +
            "        return param1 + field1;\n" +
            "    }\n" +
            "\n" +
            "    public T test2(T param1) {\n" +
            "        return field2;\n" +
            "    }\n" +
            "\n" +
            "    public com.github.sviperll.codemodel.test.Test1<T> test3(T param1) {\n" +
            "        return null;\n" +
            "    }\n" +
            "}";
        StringBuilder builder = new StringBuilder();
        RendererContexts.createInstance(builder).appendRenderable(test1);
        assertEquals(result, builder.toString());
    }

    @Test
    public void smokePrettyPrintingInterface() throws CodeMoldException {
        CodeMold.Builder builder = CodeMold.createBuilder();
        CodeMold codeModel = builder.build();
        Package pkg = codeModel.getPackage("com.github.sviperll.codemodel.test");
        InterfaceBuilder<PackageLevelBuilder> test1 = pkg.createInterface("Test1");
        test1.typeParameter("T");

        FieldBuilder field1 = test1.staticField(Types.intType(), "field1");
        field1.setAccessLevel(MemberAccess.PRIVATE);

        MethodBuilder method = test1.method("test");
        method.setAccessLevel(MemberAccess.PUBLIC);
        method.resultType(Types.intType());
        method.addParameter(Types.intType(), "param1");

        MethodBuilder method2 = test1.method("test2");
        method2.setAccessLevel(MemberAccess.PUBLIC);
        method2.resultType(Types.variable("T"));
        method2.addParameter(Types.variable("T"), "param1");

        String result =
            "interface Test1<T> {\n" +
            "    private static int field1;\n" +
            "\n" +
            "    public int test(int param1);\n" +
            "\n" +
            "    public T test2(T param1);\n" +
            "}";
        StringBuilder stringBuilder = new StringBuilder();
        RendererContexts.createInstance(stringBuilder).appendRenderable(test1.definition());
        assertEquals(result, stringBuilder.toString());
    }


    @Test
    public void smokePrettyPrintingEnum() throws CodeMoldException {
        CodeMold.Builder builder = CodeMold.createBuilder();
        CodeMold codeModel = builder.build();
        Package pkg = codeModel.getPackage("com.github.sviperll.codemodel.test");
        EnumBuilder<PackageLevelBuilder> test1 = pkg.createEnum("Test1");

        FieldBuilder field1 = test1.field(Types.intType(), "field1");
        field1.setAccessLevel(MemberAccess.PRIVATE);

        MethodBuilder method = test1.method("test");
        method.setAccessLevel(MemberAccess.PUBLIC);
        method.resultType(Types.intType());
        method.addParameter(Types.intType(), "param1");
        method.body().returnStatement(Expression.variable("param1").plus(Expression.variable("field1")));

        test1.constant("TEST1_1", new Test11EnumConstantBlueprint());
        test1.constant("TEST1_2", new Test12EnumConstantBlueprint());

        String result =
            "enum Test1 {\n"
          + "    TEST1_1 {\n"
          + "\n"
          + "        public int test(int param1) {\n"
          + "            return param1 + field1 + 1;\n"
          + "        }\n"
          + "    }, TEST1_2 {\n"
          + "\n"
          + "        public int test(int param1) {\n"
          + "            return param1 + field1 + 2;\n"
          + "        }\n"
          + "    };\n"
          + "    private int field1;\n"
          + "\n"
          + "    public int test(int param1) {\n"
          + "        return param1 + field1;\n"
          + "    }\n"
          + "}";
        StringBuilder stringBuilder = new StringBuilder();
        RendererContexts.createInstance(stringBuilder).appendRenderable(test1.definition());
        assertEquals(result, stringBuilder.toString());
    }

    @Test
    public void smokeRawTypes() throws CodeMoldException {
        ObjectDefinition test1 = buildClass();
        CodeMold codeModel = test1.getCodeModel();

        ObjectType test1Type = test1.rawType();
        assertEquals(test1, test1Type.definition());
        assertTrue(test1Type.isRaw());

        AnyType typeArgument = test1Type.typeArguments().get(0);
        assertTrue(typeArgument.isObjectType());
        ObjectType typeArgumentDetails = typeArgument.getObjectDetails();
        assertEquals(codeModel.objectType().definition(), typeArgumentDetails.definition());
    }

    @Test
    public void smokeNarrowedTypes() throws CodeMoldException {
        ObjectDefinition test1 = buildClass();
        CodeMold codeModel = test1.getCodeModel();
        ObjectDefinition stringDefinition = codeModel.getReference(String.class.getName(), OnMissing.<ObjectDefinition>throwNullPointerException());
        ObjectType stringType = stringDefinition.rawType();

        ObjectType test1Type = test1.rawType().narrow(Collections.singletonList(stringType));
        assertEquals(test1, test1Type.definition());
        assertFalse(test1Type.isRaw());
        assertTrue(test1Type.isNarrowed());

        AnyType typeArgument = test1Type.typeArguments().get(0);
        assertTrue(typeArgument.isObjectType());
        ObjectType typeArgumentDetails = typeArgument.getObjectDetails();
        assertEquals(stringDefinition, typeArgumentDetails.definition());
    }

    @Test
    public void smokeRawMethodTypes() throws CodeMoldException {
        ObjectDefinition test1 = buildClass();
        CodeMold codeModel = test1.getCodeModel();

        ObjectType test1Type = test1.rawType();
        for (MethodType method: test1Type.methods()) {
            if (method.definition().name().equals("test2")) {
                assertTrue(codeModel.objectType().sameDefinition(method.returnType().getObjectDetails()));
            }
            if (method.definition().name().equals("test3")) {
                assertTrue(codeModel.objectType().sameDefinition(method.returnType().getObjectDetails().typeArguments().get(0).getObjectDetails()));
            }
        }
    }

    @Test
    public void smokeNarrowedMethodTypes() throws CodeMoldException {
        ObjectDefinition test1 = buildClass();
        CodeMold codeModel = test1.getCodeModel();
        ObjectDefinition stringDefinition = codeModel.getReference(String.class.getName(), OnMissing.<ObjectDefinition>throwNullPointerException());
        ObjectType stringType = stringDefinition.rawType();

        ObjectType test1Type = test1.rawType().narrow(Collections.singletonList(stringType));
        for (MethodType method: test1Type.methods()) {
            if (method.definition().name().equals("test2")) {
                assertTrue(stringType.sameDefinition(method.returnType().getObjectDetails()));
            }
            if (method.definition().name().equals("test3")) {
                assertTrue(stringType.sameDefinition(method.returnType().getObjectDetails().typeArguments().get(0).getObjectDetails()));
            }
        }
    }

    private ObjectDefinition buildClass() throws CodeMoldException {
        CodeMold.Builder builder = CodeMold.createBuilder();
        CodeMold codeModel = builder.build();
        Package pkg = codeModel.getPackage("com.github.sviperll.codemodel.test");
        ClassBuilder<PackageLevelBuilder> test1 = pkg.createClass("Test1");
        test1.typeParameter("T");

        FieldBuilder field1 = test1.field(Types.intType(), "field1");
        field1.setAccessLevel(MemberAccess.PRIVATE);

        FieldBuilder field2 = test1.field(Types.variable("T"), "field2");
        field2.setAccessLevel(MemberAccess.PROTECTED);

        MethodBuilder method = test1.method("test");
        method.setAccessLevel(MemberAccess.PUBLIC);
        method.resultType(Types.intType());
        method.addParameter(Types.intType(), "param1");
        method.body().returnStatement(Expression.variable("param1").plus(Expression.variable("field1")));

        MethodBuilder method2 = test1.method("test2");
        method2.setAccessLevel(MemberAccess.PUBLIC);
        method2.resultType(Types.variable("T"));
        method2.addParameter(Types.variable("T"), "param1");
        method2.body().returnStatement(Expression.variable("field2"));

        MethodBuilder method3 = test1.method("test3");
        method3.setAccessLevel(MemberAccess.PUBLIC);
        method3.resultType(test1.definition().internalType());
        method3.addParameter(Types.variable("T"), "param1");
        method3.body().returnStatement(Expression.nullExpression());

        return test1.definition();
    }

    private static class Test11EnumConstantBlueprint implements Consumer<AnonymousClassBuilder> {
        @Override
        public void accept(AnonymousClassBuilder builder) {
            try {
                MethodBuilder method1 = builder.method("test");
                method1.setAccessLevel(MemberAccess.PUBLIC);
                method1.resultType(Types.intType());
                method1.addParameter(Types.intType(), "param1");
                method1.body().returnStatement(Expression.variable("param1").plus(Expression.variable("field1")).plus(Expression.literal(1)));
            } catch (CodeMoldException ex) {
                Logger.getLogger(ObjectDefinitionTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class Test12EnumConstantBlueprint implements Consumer<AnonymousClassBuilder> {
        @Override
        public void accept(AnonymousClassBuilder builder) {
            try {
                MethodBuilder method1 = builder.method("test");
                method1.setAccessLevel(MemberAccess.PUBLIC);
                method1.resultType(Types.intType());
                method1.addParameter(Types.intType(), "param1");
                method1.body().returnStatement(Expression.variable("param1").plus(Expression.variable("field1")).plus(Expression.literal(2)));
            } catch (CodeMoldException ex) {
                Logger.getLogger(ObjectDefinitionTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
