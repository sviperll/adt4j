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
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;

@SupportedAnnotationTypes("com.github.sviperll.adt4j.GenerateValueClassForVisitor")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class GenerateValueClassForVisitorProcessor extends AbstractProcessor {

    private final Set<Element> remainingElements = new HashSet<Element>();

    private final List<TypeElement> generatedRootTypes = new ArrayList<TypeElement>();

    @Override
    public void init(ProcessingEnvironment environment) {
        super.init(environment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                for (Element element: remainingElements) {
                    System.out.println("Unable to process " + element);
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to process " + element);
                }
            }
            if (!remainingElements.isEmpty()) {
                generatedRootTypes.addAll(ElementFilter.typesIn(roundEnv.getRootElements()));
            }
            remainingElements.addAll(roundEnv.getElementsAnnotatedWith(GenerateValueClassForVisitor.class));
            final Set<Element> processed = processElements(remainingElements);
            remainingElements.removeAll(processed);
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
        }
        // Allows other processors to process 'our' annotation by not claiming it.
        return false;
    }

    private Set<Element> processElements(Set<? extends Element> elements) throws IOException {
        final Set<Element> processed = new HashSet<Element>();
        for (Element element: elements) {
            try {
                JCodeModel jCodeModel = new JCodeModel();
                GenerateValueClassForVisitor dataVisitor = element.getAnnotation(GenerateValueClassForVisitor.class);
                ValueClassModelBuilder builder = new ValueClassModelBuilder(jCodeModel, generatedRootTypes);
                builder.build(element, dataVisitor);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generated value class for " + element);
                FilerCodeWriter writer = new FilerCodeWriter(processingEnv.getFiler(), processingEnv.getMessager());
                try {
                    jCodeModel.build(writer);
                } finally {
                    writer.close();
                }
                processed.add(element);
            } catch (ErrorTypeFound ex) {
                // We abandoned this type for now, but it may still be correctly processed during next round.
            } catch (CodeGenerationException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, element + ": " + ex.getMessage());
            } catch (SourceException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, element + ": " + ex.getMessage());
            } catch (RuntimeException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, element + ": " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        }
        return processed;
    }
}
