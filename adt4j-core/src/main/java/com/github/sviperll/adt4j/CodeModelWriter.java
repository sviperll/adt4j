/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import java.io.IOException;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class CodeModelWriter {
    private final JCodeModel codeModel;
    private final CodeWriter codeWriter;

    public CodeModelWriter(JCodeModel codeModel, CodeWriter codeWriter) {
        this.codeModel = codeModel;
        this.codeWriter = codeWriter;
    }

    void write() throws IOException {
        codeModel.build(codeWriter);
    }

}
