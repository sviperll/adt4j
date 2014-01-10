/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import com.sun.codemodel.JCodeModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@SupportedAnnotationTypes("com.github.sviperll.adt4j.DataVisitor")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DataVisitorProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        try {
            for (Element elem : roundEnv.getElementsAnnotatedWith(DataVisitor.class)) {
                try {
                    DataVisitor dataVisitor = elem.getAnnotation(DataVisitor.class);
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

    private void processElement(Element visitorElement, DataVisitor dataVisitor) throws CodeGenerationException, IOException, SourceException {
        Path temporaryDirectory = Files.createTempDirectory("datavisitorcodemodel");
        ClassWriter definedClassWriter = new ClassWriter(processingEnv.getFiler(), temporaryDirectory);
        ClassBuilder builder = new ClassBuilder(new JCodeModel());
        DefinedClass definedClass = builder.build(visitorElement, dataVisitor);
        definedClassWriter.write(definedClass);
    }
}
