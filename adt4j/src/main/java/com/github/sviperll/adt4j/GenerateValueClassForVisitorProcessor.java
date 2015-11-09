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

import com.github.sviperll.Throwables;
import com.github.sviperll.adt4j.model.ValueClassModelFactory;
import com.github.sviperll.meta.FilerCodeWriter;
import com.github.sviperll.meta.ElementMessage;
import com.github.sviperll.meta.ElementMessager;
import com.github.sviperll.meta.SourceCodeValidationException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.meta.JCodeModelJavaxLangModelAdapter;
import com.helger.jcodemodel.meta.CodeModelBuildingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.github.sviperll.adt4j.GenerateValueClassForVisitor")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class GenerateValueClassForVisitorProcessor extends AbstractProcessor {
    private final Set<String> remainingElements = new HashSet<String>();
    private final List<ElementMessage> errors = new ArrayList<ElementMessage>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                for (String qualifiedName: remainingElements) {
                    errors.add(ElementMessage.of(processingEnv.getElementUtils().getTypeElement(qualifiedName), "Unable to process"));
                }
                for (ElementMessage error: errors) {
                    TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(error.qualifiedElementName());
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error.message(), typeElement);
                }
            } else {
                Set<TypeElement> elements = new HashSet<TypeElement>();
                for (Element element: roundEnv.getElementsAnnotatedWith(GenerateValueClassForVisitor.class)) {
                    elements.add((TypeElement)element);
                }
                for (String path: remainingElements) {
                    elements.add(processingEnv.getElementUtils().getTypeElement(path));
                }
                remainingElements.clear();
                processElements(elements);
            }
        } catch (Exception ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, Throwables.render(ex));
        }
        return true;
    }
    private void processElements(Set<? extends TypeElement> elements) {
        for (TypeElement element: elements) {
            try {
                JCodeModel jCodeModel = new JCodeModel();
                Visitor visitorAnnotation = element.getAnnotation(Visitor.class);
                if (visitorAnnotation == null)
                    throw new SourceCodeValidationException("No " + Visitor.class.getName() + " annotation for " + element.getQualifiedName() + " class annotated with " + GenerateValueClassForVisitor.class.getName() + " annotation");
                JCodeModelJavaxLangModelAdapter adapter = new JCodeModelJavaxLangModelAdapter(jCodeModel, processingEnv.getElementUtils());
                JDefinedClass visitorModel = adapter.getClassWithErrorTypes(element);
                JDefinedClass valueClass = ValueClassModelFactory.createValueClass(visitorModel, visitorAnnotation);
                if (jCodeModel.buildsErrorTypeRefs()) {
                    remainingElements.add(element.getQualifiedName().toString());
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generated value class " + valueClass.fullName() + " for " + element + " visitor interface", element);
                    FilerCodeWriter writer = new FilerCodeWriter(processingEnv.getFiler(), new ElementMessager(processingEnv.getMessager(), element));
                    try {
                        jCodeModel.build(writer);
                    } finally {
                        try {
                            writer.close();
                        } catch (Exception ex) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, Throwables.render(ex));
                        }
                    }
                }
            } catch (SourceCodeValidationException ex) {
                errors.add(ElementMessage.of(element, ex.toString()));
            } catch (CodeModelBuildingException ex) {
                errors.add(ElementMessage.of(element, ex.toString()));
            } catch (IOException ex) {
                errors.add(ElementMessage.of(element, Throwables.render(ex)));
            } catch (RuntimeException ex) {
                errors.add(ElementMessage.of(element, Throwables.render(ex)));
            }
        }
    }
}
