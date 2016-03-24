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

import com.github.sviperll.codemodel.render.RendererContexts;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class ObjectDefinitionTest {
    /**
     * Test of isFinal method, of class ObjectDefinition.
     */
    @Test
    public void smokePrettyPrinting() throws CodeModelException {
        ObjectDefinition test1 = buildClass();
        String result =
            "class Test1<T extends java.lang.Object> extends java.lang.Object {\n" +
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
            "}\n";
        StringBuilder builder = new StringBuilder();
        RendererContexts.createInstance(builder).appendRenderable(test1);
        assertEquals(result, builder.toString());
    }

    @Test
    public void smokeRawTypes() throws CodeModelException {
        ObjectDefinition test1 = buildClass();
        CodeModel codeModel = test1.getCodeModel();

        Type test1Type = test1.rawType();
        assertTrue(test1Type.isObjectType());
        ObjectTypeDetails details = test1Type.getObjectDetails();
        assertEquals(test1, details.definition());
        assertTrue(details.isRaw());

        Type typeArgument = details.typeArguments().get(0);
        assertTrue(typeArgument.isObjectType());
        ObjectTypeDetails typeArgumentDetails = typeArgument.getObjectDetails();
        assertEquals(codeModel.objectType().getObjectDetails().definition(), typeArgumentDetails.definition());
    }

    @Test
    public void smokeNarrowedTypes() throws CodeModelException {
        ObjectDefinition test1 = buildClass();
        CodeModel codeModel = test1.getCodeModel();
        ObjectDefinition stringDefinition = codeModel.importTopLevelClass(String.class);
        Type stringType = stringDefinition.rawType();

        Type test1Type = test1.rawType().getObjectDetails().narrow(Collections.singletonList(stringType));
        assertTrue(test1Type.isObjectType());
        ObjectTypeDetails details = test1Type.getObjectDetails();
        assertEquals(test1, details.definition());
        assertFalse(details.isRaw());
        assertTrue(details.isNarrowed());

        Type typeArgument = details.typeArguments().get(0);
        assertTrue(typeArgument.isObjectType());
        ObjectTypeDetails typeArgumentDetails = typeArgument.getObjectDetails();
        assertEquals(stringDefinition, typeArgumentDetails.definition());
    }

    @Test
    public void smokeRawMethodTypes() throws CodeModelException {
        ObjectDefinition test1 = buildClass();
        CodeModel codeModel = test1.getCodeModel();

        Type test1Type = test1.rawType();
        List<Type> methods = test1Type.getObjectDetails().methods();
        for (Type type: methods) {
            ExecutableTypeDetails executableDetails = type.getExecutableDetails();
            if (executableDetails.definition().getMethodDetails().name().equals("test2")) {
                assertEquals(codeModel.objectType().getObjectDetails().definition(), executableDetails.returnType().getObjectDetails().definition());
            }
        }
    }

    @Test
    public void smokeNarrowedMethodTypes() throws CodeModelException {
        ObjectDefinition test1 = buildClass();
        CodeModel codeModel = test1.getCodeModel();
        ObjectDefinition stringDefinition = codeModel.importTopLevelClass(String.class);
        Type stringType = stringDefinition.rawType();

        Type test1Type = test1.rawType().getObjectDetails().narrow(Collections.singletonList(stringType));
        List<Type> methods = test1Type.getObjectDetails().methods();
        for (Type type: methods) {
            ExecutableTypeDetails executableDetails = type.getExecutableDetails();
            if (executableDetails.definition().getMethodDetails().name().equals("test2")) {
                assertEquals(stringType.getObjectDetails().definition(), executableDetails.returnType().getObjectDetails().definition());
            }
        }
    }

    private ObjectDefinition buildClass() throws CodeModelException {
        CodeModel codeModel = new CodeModel();
        Package pkg = codeModel.getPackage("com.github.sviperll.codemodel.test");
        ObjectBuilder<PackageLevelBuilder> test1 = pkg.createClass(ObjectKind.CLASS, "Test1");
        test1.typeParameter("T");
        FieldBuilder field1 = test1.field(Type.intType(), "field1");
        field1.residence().setAccessLevel(MemberAccess.PRIVATE);
        FieldBuilder field2 = test1.field(Type.variable("T"), "field2");
        field2.residence().setAccessLevel(MemberAccess.PROTECTED);
        MethodBuilder method = test1.method("test");
        method.residence().setAccessLevel(MemberAccess.PUBLIC);
        method.resultType(Type.intType());
        method.addParameter(Type.intType(), "param1");
        method.body().returnStatement(Expression.variable("param1").plus(Expression.variable("field1")));
        MethodBuilder method2 = test1.method("test2");
        method2.residence().setAccessLevel(MemberAccess.PUBLIC);
        method2.resultType(Type.variable("T"));
        method2.addParameter(Type.variable("T"), "param1");
        method2.body().returnStatement(Expression.variable("field2"));
        return test1.definition();
    }
}
