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

import com.github.sviperll.codemodel.ClassBuilder;
import com.github.sviperll.codemodel.CodeModel;
import com.github.sviperll.codemodel.CodeModelException;
import com.github.sviperll.codemodel.Consumer;
import com.github.sviperll.codemodel.EnumBuilder;
import com.github.sviperll.codemodel.EnumConstantBuilder;
import com.github.sviperll.codemodel.Expression;
import com.github.sviperll.codemodel.FieldBuilder;
import com.github.sviperll.codemodel.InterfaceBuilder;
import com.github.sviperll.codemodel.MemberAccess;
import com.github.sviperll.codemodel.MethodBuilder;
import com.github.sviperll.codemodel.MethodType;
import com.github.sviperll.codemodel.ObjectDefinition;
import com.github.sviperll.codemodel.ObjectType;
import com.github.sviperll.codemodel.Package;
import com.github.sviperll.codemodel.PackageLevelBuilder;
import com.github.sviperll.codemodel.Type;
import com.github.sviperll.codemodel.render.RendererContexts;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class ObjectDefinitionTest {
    @Test
    public void smokeReflectedObjectShouldBePrintable() throws CodeModelException {
        CodeModel.Builder builder = CodeModel.createBuilder();
        CodeModel codeModel = builder.build();
        StringBuilder builder1 = new StringBuilder();
        RendererContexts.createInstance(builder1).appendRenderable(codeModel.getReference(String.class.getName()));
        System.out.println(builder1.toString());
    }

    @Test
    public void smokePrettyPrintingClass() throws CodeModelException {
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
    public void smokePrettyPrintingInterface() throws CodeModelException {
        CodeModel.Builder builder = CodeModel.createBuilder();
        CodeModel codeModel = builder.build();
        Package pkg = codeModel.getPackage("com.github.sviperll.codemodel.test");
        InterfaceBuilder<PackageLevelBuilder> test1 = pkg.createInterface("Test1");
        test1.typeParameter("T");

        FieldBuilder field1 = test1.staticField(Type.intType(), "field1");
        field1.residence().setAccessLevel(MemberAccess.PRIVATE);

        MethodBuilder method = test1.method("test");
        method.residence().setAccessLevel(MemberAccess.PUBLIC);
        method.resultType(Type.intType());
        method.addParameter(Type.intType(), "param1");

        MethodBuilder method2 = test1.method("test2");
        method2.residence().setAccessLevel(MemberAccess.PUBLIC);
        method2.resultType(Type.variable("T").asType());
        method2.addParameter(Type.variable("T").asType(), "param1");

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
    public void smokePrettyPrintingEnum() throws CodeModelException {
        CodeModel.Builder builder = CodeModel.createBuilder();
        CodeModel codeModel = builder.build();
        Package pkg = codeModel.getPackage("com.github.sviperll.codemodel.test");
        EnumBuilder<PackageLevelBuilder> test1 = pkg.createEnum("Test1");

        FieldBuilder field1 = test1.field(Type.intType(), "field1");
        field1.residence().setAccessLevel(MemberAccess.PRIVATE);

        MethodBuilder method = test1.method("test");
        method.residence().setAccessLevel(MemberAccess.PUBLIC);
        method.resultType(Type.intType());
        method.addParameter(Type.intType(), "param1");
        method.body().returnStatement(Expression.variable("param1").plus(Expression.variable("field1")));

        test1.constant("TEST1_1", new Consumer<EnumConstantBuilder>() {
            @Override
            public void accept(EnumConstantBuilder value) {
                try {
                    MethodBuilder method1 = value.method("test");
                    method1.residence().setAccessLevel(MemberAccess.PUBLIC);
                    method1.resultType(Type.intType());
                    method1.addParameter(Type.intType(), "param1");
                    method1.body().returnStatement(Expression.variable("param1").plus(Expression.variable("field1")).plus(Expression.literal(1)));
                } catch (CodeModelException ex) {
                    Logger.getLogger(ObjectDefinitionTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        test1.constant("TEST1_2", new Consumer<EnumConstantBuilder>() {
            @Override
            public void accept(EnumConstantBuilder value) {
                try {
                    MethodBuilder method1 = value.method("test");
                    method1.residence().setAccessLevel(MemberAccess.PUBLIC);
                    method1.resultType(Type.intType());
                    method1.addParameter(Type.intType(), "param1");
                    method1.body().returnStatement(Expression.variable("param1").plus(Expression.variable("field1")).plus(Expression.literal(2)));
                } catch (CodeModelException ex) {
                    Logger.getLogger(ObjectDefinitionTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

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
    public void smokeRawTypes() throws CodeModelException {
        ObjectDefinition test1 = buildClass();
        CodeModel codeModel = test1.getCodeModel();

        ObjectType test1Type = test1.rawType();
        assertEquals(test1, test1Type.definition());
        assertTrue(test1Type.isRaw());

        Type typeArgument = test1Type.typeArguments().get(0);
        assertTrue(typeArgument.isObjectType());
        ObjectType typeArgumentDetails = typeArgument.getObjectDetails();
        assertEquals(codeModel.objectType().definition(), typeArgumentDetails.definition());
    }

    @Test
    public void smokeNarrowedTypes() throws CodeModelException {
        ObjectDefinition test1 = buildClass();
        CodeModel codeModel = test1.getCodeModel();
        ObjectDefinition stringDefinition = codeModel.getReference(String.class.getName());
        ObjectType stringType = stringDefinition.rawType();

        ObjectType test1Type = test1.rawType().narrow(Collections.singletonList(stringType.asType()));
        assertEquals(test1, test1Type.definition());
        assertFalse(test1Type.isRaw());
        assertTrue(test1Type.isNarrowed());

        Type typeArgument = test1Type.typeArguments().get(0);
        assertTrue(typeArgument.isObjectType());
        ObjectType typeArgumentDetails = typeArgument.getObjectDetails();
        assertEquals(stringDefinition, typeArgumentDetails.definition());
    }

    @Test
    public void smokeRawMethodTypes() throws CodeModelException {
        ObjectDefinition test1 = buildClass();
        CodeModel codeModel = test1.getCodeModel();

        ObjectType test1Type = test1.rawType();
        List<MethodType> methods = test1Type.methods();
        for (MethodType method: methods) {
            if (method.definition().name().equals("test2")) {
                assertTrue(codeModel.objectType().sameDefinition(method.returnType().getObjectDetails()));
            }
            if (method.definition().name().equals("test3")) {
                assertTrue(codeModel.objectType().sameDefinition(method.returnType().getObjectDetails().typeArguments().get(0).getObjectDetails()));
            }
        }
    }

    @Test
    public void smokeNarrowedMethodTypes() throws CodeModelException {
        ObjectDefinition test1 = buildClass();
        CodeModel codeModel = test1.getCodeModel();
        ObjectDefinition stringDefinition = codeModel.getReference(String.class.getName());
        ObjectType stringType = stringDefinition.rawType();

        ObjectType test1Type = test1.rawType().narrow(Collections.singletonList(stringType.asType()));
        List<MethodType> methods = test1Type.methods();
        for (MethodType method: methods) {
            if (method.definition().name().equals("test2")) {
                assertTrue(stringType.sameDefinition(method.returnType().getObjectDetails()));
            }
            if (method.definition().name().equals("test3")) {
                assertTrue(stringType.sameDefinition(method.returnType().getObjectDetails().typeArguments().get(0).getObjectDetails()));
            }
        }
    }

    private ObjectDefinition buildClass() throws CodeModelException {
        CodeModel.Builder builder = CodeModel.createBuilder();
        CodeModel codeModel = builder.build();
        Package pkg = codeModel.getPackage("com.github.sviperll.codemodel.test");
        ClassBuilder<PackageLevelBuilder> test1 = pkg.createClass("Test1");
        test1.typeParameter("T");

        FieldBuilder field1 = test1.field(Type.intType(), "field1");
        field1.residence().setAccessLevel(MemberAccess.PRIVATE);

        FieldBuilder field2 = test1.field(Type.variable("T").asType(), "field2");
        field2.residence().setAccessLevel(MemberAccess.PROTECTED);

        MethodBuilder method = test1.method("test");
        method.residence().setAccessLevel(MemberAccess.PUBLIC);
        method.resultType(Type.intType());
        method.addParameter(Type.intType(), "param1");
        method.body().returnStatement(Expression.variable("param1").plus(Expression.variable("field1")));

        MethodBuilder method2 = test1.method("test2");
        method2.residence().setAccessLevel(MemberAccess.PUBLIC);
        method2.resultType(Type.variable("T").asType());
        method2.addParameter(Type.variable("T").asType(), "param1");
        method2.body().returnStatement(Expression.variable("field2"));

        MethodBuilder method3 = test1.method("test3");
        method3.residence().setAccessLevel(MemberAccess.PUBLIC);
        method3.resultType(test1.definition().internalType().asType());
        method3.addParameter(Type.variable("T").asType(), "param1");
        method3.body().returnStatement(Expression.nullExpression());

        return test1.definition();
    }
}
