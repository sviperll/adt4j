/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFormatter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class JExprExt {
    public static JExpression ternary(JExpression test, JExpression iftrue, JExpression iffalse) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            JFormatter formatter = new JFormatter(printWriter);
            try {
                formatter.g(test);
                formatter.p(" ? ");
                formatter.g(iftrue);
                formatter.p(" : ");
                formatter.g(iffalse);
            } finally {
                formatter.close();
            }
        }
        return JExpr.direct(stringWriter.toString());
    }

    public static JClass narrow(JDefinedClass definedClass,
                                List<JClass> typeArguments) {
        return typeArguments.isEmpty() ? definedClass : definedClass.narrow(typeArguments);
    }
}
