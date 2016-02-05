/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
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

import com.helger.jcodemodel.AbstractCodeWriter;
import com.helger.jcodemodel.JPackage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Writes java classes through {@code Filer}.
 * <p>
 * {@code AbstractCodeWriter} to be used with {@code JCodeModel}.
 * <p>
 * This writer uses {@code Filer} to write actual java source files.
 * {@code Filer} object is provided by Java-compiler and is available to annotation processors.
 *
 * @see com.helger.jcodemodel.JCodeModel
 * @see javax.annotation.processing.AbstractProcessor
 * @see com.helger.jcodemodel.AbstractCodeWriter
 * @see javax.annotation.processing.Filer
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class FilerCodeWriter extends AbstractCodeWriter {
    private static final String JAVA_SOURCE_SUFFIX = ".java";
    private final Filer filer;
    private final List<OutputStream> closeables = new ArrayList<OutputStream>();
    private final Messager messager;

    /**
     * Creates new instance.
     *
     * @param filer {@code Filer} used to create java sources
     * @param messager is only used for error reporting
     */
    public FilerCodeWriter(Filer filer, Messager messager) {
        super(Charset.defaultCharset(), System.lineSeparator());
        this.filer = filer;
        this.messager = messager;
    }

    @Override
    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        String className = fileName.substring(0, fileName.length() - JAVA_SOURCE_SUFFIX.length());
        if (!fileName.endsWith(JAVA_SOURCE_SUFFIX))
            throw new IllegalStateException("Unexpected file name passed to code writer: " + fileName);
        JavaFileObject fileObject = filer.createSourceFile(pkg.name() + "." + className);
        OutputStream stream = fileObject.openOutputStream();
        closeables.add(stream);
        return stream;
    }

    @Override
    public void close() throws IOException {
        Exception exception = null;
        for (OutputStream stream: closeables) {
            try {
                stream.close();
            } catch (IOException ex) {
                if (exception != null)
                    messager.printMessage(Diagnostic.Kind.WARNING, Throwables.render(exception));
                exception = ex;
            } catch (RuntimeException ex) {
                if (exception != null)
                    messager.printMessage(Diagnostic.Kind.WARNING, Throwables.render(exception));
                exception = ex;
            }
        }
        closeables.clear();
        if (exception != null) {
            if (exception instanceof IOException) {
                throw (IOException)exception;
            } else if (exception instanceof RuntimeException) {
                throw (RuntimeException)exception;
            } else
                throw new IllegalStateException("Unexpected exception", exception);
        }
    }
}
