/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.writer.FileCodeWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class ClassWriter {
    private final Filer filer;
    private final Path sourcesDirectory;

    ClassWriter(Filer filer, Path sourcesDirectory) {
        this.filer = filer;
        this.sourcesDirectory = sourcesDirectory;
    }

    void write(DefinedClass definedClass) throws IOException {
        CodeWriter codeWriter = new FileCodeWriter(sourcesDirectory.toFile());
        try {
            CodeModelWriter codeModelWriter = new CodeModelWriter(definedClass.getCodeModel(), codeWriter);
            codeModelWriter.write();
        } finally {
            codeWriter.close();
        }
        
        String quelifiedName = definedClass.getQualifiedName();

        JavaFileObject jfo = filer.createSourceFile(quelifiedName);
        try (OutputStream outputStream = jfo.openOutputStream()) {
            Path classSource = sourcesDirectory.resolve(quelifiedName.replace(".", File.separator) + ".java");
            Files.copy(classSource, outputStream);
        }

    }
}
