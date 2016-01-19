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
import com.github.sviperll.adt4j.model.Stage0ValueClassModel;
import com.github.sviperll.adt4j.model.Stage0ValueClassModelFactory;
import com.github.sviperll.adt4j.model.Stage1ValueClassModel;
import com.github.sviperll.adt4j.model.util.GenerationProcess;
import com.github.sviperll.meta.FilerCodeWriter;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.meta.CodeModelBuildingException;
import com.helger.jcodemodel.meta.JCodeModelJavaxLangModelAdapter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
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
    private static final Visitor DEFAULT_VISITOR_IMPLEMENTATION;
    static {
        DEFAULT_VISITOR_IMPLEMENTATION = new Visitor() {
            @Override
            public String resultVariableName() {
                return "R";
            }

            @Override
            public String exceptionVariableName() {
                return ":none";
            }

            @Override
            public String selfReferenceVariableName() {
                return ":none";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Visitor.class;
            }
        };
    }

    private final Set<String> remainingElements = new HashSet<>();
    private final Map<String, List<String>> errorMap = new TreeMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                finishProcessing();
            } else {
                processRound(roundEnv);
            }
        } catch (RuntimeException ex) {
            String message = "Unexpected exception."
                    + " This seems like a bug in ADT4J,"
                    + " please report it at https://github.com/sviperll/adt4j/issues"
                    + " with the following details:\n" + Throwables.render(ex);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
        }
        return true;
    }

    private void processRound(RoundEnvironment roundEnv) {
        Set<TypeElement> elements = new HashSet<>();
        for (Element element: roundEnv.getElementsAnnotatedWith(GenerateValueClassForVisitor.class)) {
            elements.add((TypeElement)element);
        }
        Set<String> elementsFromPreviousRound = new HashSet<>(remainingElements);
        remainingElements.clear();
        for (String path: elementsFromPreviousRound) {
            TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(path);
            if (typeElement == null)
                remainingElements.add(path);
            else
                elements.add(typeElement);
        }
        ElementProcessor elementProcessor = new ElementProcessor(elements, new JCodeModel());
        elementProcessor.generateClassesWithoutErrors();
        elementProcessor.writeGeneratedCode();
    }

    private void finishProcessing() {
        Set<TypeElement> elements = new HashSet<>();
        for (String path: remainingElements) {
            TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(path);
            if (typeElement == null)
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to find type " + path);
            else
                elements.add(typeElement);
        }
        ElementProcessor elementProcessor = new ElementProcessor(elements, new JCodeModel());
        elementProcessor.generateClasses();
        elementProcessor.writeGeneratedCode();
        for (Entry<String, List<String>> errors: errorMap.entrySet()) {
            TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(errors.getKey());
            for (String error: errors.getValue()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error, typeElement);
            }
        }
    }

    private class ElementProcessor {
        private final Set<? extends TypeElement> elements;
        private final JCodeModel jCodeModel;
        ElementProcessor(Set<? extends TypeElement> elements, JCodeModel jCodeModel) {
            this.elements = elements;
            this.jCodeModel = jCodeModel;
        }

        private Map<String, TypeElement> generateClassesWithErrors() throws RuntimeException {
            Map<String, Stage0ValueClassModel> stage0 = processStage0();
            Map<String, Stage1ValueClassModel> stage1 = processStage1(stage0);
            Map<String, TypeElement> generatedClasses = processStage2(stage1);
            return generatedClasses;
        }

        void generateClasses() {
            Map<String, TypeElement> generatedClasses = generateClassesWithErrors();
            reportErrors(generatedClasses);
        }

        void generateClassesWithoutErrors() {
            Map<String, TypeElement> generatedClasses = generateClassesWithErrors();
            hideErrors(generatedClasses);
        }

        private void hideErrors(Map<String, TypeElement> generatedClasses) {
            Iterator<JPackage> iterator = jCodeModel.packages();
            while (iterator.hasNext()) {
                JPackage pkg = iterator.next();
                for (JDefinedClass klass: pkg.classes()) {
                    if (!klass.isHidden() && klass.containsErrorTypes() && generatedClasses.containsKey(klass.fullName())) {
                        TypeElement sourceElement = generatedClasses.get(klass.fullName());
                        remainingElements.add(sourceElement.getQualifiedName().toString());
                        klass.hide();
                    }
                }
            }
        }

        private void reportErrors(Map<String, TypeElement> generatedClasses) {
            Iterator<JPackage> iterator = jCodeModel.packages();
            while (iterator.hasNext()) {
                JPackage pkg = iterator.next();
                for (JDefinedClass klass: pkg.classes()) {
                    if (!klass.isHidden() && klass.containsErrorTypes() && generatedClasses.containsKey(klass.fullName())) {
                        TypeElement sourceElement = generatedClasses.get(klass.fullName());
                        List<String> errors = new ArrayList<>();
                        errors.addAll(errorMap.get(sourceElement.getQualifiedName().toString()));
                        errors.add("Unable to generate class, some references in source code are not resolved");
                        errorMap.put(sourceElement.getQualifiedName().toString(), errors);
                    }
                }
            }
        }

        void writeGeneratedCode() {
            try {
                FilerCodeWriter writer = new FilerCodeWriter(processingEnv.getFiler(), processingEnv.getMessager());
                try {
                    jCodeModel.build(writer);
                } finally {
                    try {
                        writer.close();
                    } catch (IOException | RuntimeException ex) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, Throwables.render(ex));
                    }
                }
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, Throwables.render(ex));
            }
        }

        private Map<String, Stage0ValueClassModel> processStage0() throws RuntimeException {
            Map<String, Stage0ValueClassModel> result = new TreeMap<>();
            JCodeModel bootJCodeModel = new JCodeModel();
            Stage0ValueClassModelFactory stage0Processor = Stage0ValueClassModelFactory.createFactory(jCodeModel);
            for (TypeElement element: elements) {
                Visitor visitorAnnotation = element.getAnnotation(Visitor.class);
                if (visitorAnnotation == null) {
                    visitorAnnotation = DEFAULT_VISITOR_IMPLEMENTATION;
                }
                JCodeModelJavaxLangModelAdapter adapter = new JCodeModelJavaxLangModelAdapter(bootJCodeModel, processingEnv.getElementUtils());
                JDefinedClass bootVisitorModel;
                try {
                    bootVisitorModel = adapter.getClassWithErrorTypes(element);
                } catch (CodeModelBuildingException ex) {
                    throw new RuntimeException("Unexpected exception", ex);
                }
                Stage0ValueClassModel model = stage0Processor.createStage0Model(bootVisitorModel, visitorAnnotation);
                result.put(element.getQualifiedName().toString(), model);
            }
            return result;
        }

        private Map<String, Stage1ValueClassModel> processStage1(Map<String, Stage0ValueClassModel> stage0Results) throws RuntimeException {
            Map<String, Stage1ValueClassModel> result = new TreeMap<>();
            for (TypeElement element: elements) {
                GenerationProcess generation = new GenerationProcess();
                Visitor visitorAnnotation = element.getAnnotation(Visitor.class);
                if (visitorAnnotation == null) {
                    visitorAnnotation = DEFAULT_VISITOR_IMPLEMENTATION;
                }
                JCodeModelJavaxLangModelAdapter adapter = new JCodeModelJavaxLangModelAdapter(jCodeModel, processingEnv.getElementUtils());
                JDefinedClass visitorModel;
                try {
                    visitorModel = adapter.getClassWithErrorTypes(element);
                } catch (CodeModelBuildingException ex) {
                    throw new RuntimeException("Unexpected exception", ex);
                }

                Stage0ValueClassModel stage0Model = stage0Results.get(element.getQualifiedName().toString());
                Stage1ValueClassModel model = generation.processGenerationResult(stage0Model.createStage1Model(visitorModel, visitorAnnotation));
                errorMap.put(element.getQualifiedName().toString(), generation.reportedErrors());
                if (model != null)
                    result.put(element.getQualifiedName().toString(), model);
            }
            return result;
        }

        private Map<String, TypeElement> processStage2(Map<String, Stage1ValueClassModel> stage1Results) {
            Map<String, TypeElement> result = new TreeMap<>();
            for (TypeElement element: elements) {
                GenerationProcess generation = new GenerationProcess();
                generation.reportAllErrors(errorMap.get(element.getQualifiedName().toString()));
                Stage1ValueClassModel stage1Model = stage1Results.get(element.getQualifiedName().toString());
                if (stage1Model != null) {
                    JDefinedClass model = generation.processGenerationResult(stage1Model.createResult());
                    errorMap.put(element.getQualifiedName().toString(), generation.reportedErrors());
                    result.put(model.fullName(), element);
                }
            }
            return result;
        }

    }
}
