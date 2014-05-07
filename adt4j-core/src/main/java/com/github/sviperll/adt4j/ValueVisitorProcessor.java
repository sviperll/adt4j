/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.JCodeModel;
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

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@SupportedAnnotationTypes("com.github.sviperll.adt4j.ValueVisitor")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ValueVisitorProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        try {
            for (Element elem : roundEnv.getElementsAnnotatedWith(ValueVisitor.class)) {
                try {
                    ValueVisitor dataVisitor = elem.getAnnotation(ValueVisitor.class);
                    processElement(elem, dataVisitor);
                } catch (CodeGenerationException | SourceException ex) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
                }
            }
            return true;
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
            return false;
        }
    }

    private void processElement(Element visitorElement, ValueVisitor dataVisitor) throws CodeGenerationException, IOException, SourceException {
        JCodeModel jCodeModel = new JCodeModel();
        ValueClassModelBuilder builder = new ValueClassModelBuilder(jCodeModel);
        ValueClassModel definedClass = builder.build(visitorElement, dataVisitor);

        jCodeModel.build(new FilerCodeWriter(processingEnv.getFiler()));
    }
}
