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
package com.github.sviperll.adt4j;

import com.helger.jcodemodel.JCodeModel;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import com.sun.source.util.Trees;
import java.util.HashSet;
import javax.annotation.processing.ProcessingEnvironment;

@SupportedAnnotationTypes("com.github.sviperll.adt4j.GenerateValueClassForVisitor")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class GenerateValueClassForVisitorProcessor extends AbstractProcessor {
    private Trees trees;
    private Set<TreePath> remainingElements = new HashSet<TreePath>();

    @Override
    public void init(ProcessingEnvironment environment) {
        super.init(environment);
        trees = Trees.instance(environment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                for (TreePath path: remainingElements) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to process " + trees.getElement(path));
                }
            } else {
                Set<Element> elements = new HashSet<Element>();
                elements.addAll(roundEnv.getElementsAnnotatedWith(GenerateValueClassForVisitor.class));
                for (TreePath path: remainingElements) {
                    elements.add(trees.getElement(path));
                }
                remainingElements.clear();
                processElements(elements);
            }
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
        }
        return true;
    }

    private void processElements(Set<Element> elements) throws IOException {
        for (Element element: elements) {
            try {
                JCodeModel jCodeModel = new JCodeModel();
                GenerateValueClassForVisitor dataVisitor = element.getAnnotation(GenerateValueClassForVisitor.class);
                ValueClassModelBuilder builder = new ValueClassModelBuilder(jCodeModel);
                builder.build(element, dataVisitor);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generated value class for " + element);
                FilerCodeWriter writer = new FilerCodeWriter(processingEnv.getFiler(), processingEnv.getMessager());
                try {
                    jCodeModel.build(writer);
                } finally {
                    writer.close();
                }
            } catch (ErrorTypeFound ex) {
                remainingElements.add(trees.getPath(element));
            } catch (CodeGenerationException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, element + ": " + ex.getMessage());
            } catch (SourceException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, element + ": " + ex.getMessage());
            } catch (RuntimeException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, element + ": " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        }
    }
}
