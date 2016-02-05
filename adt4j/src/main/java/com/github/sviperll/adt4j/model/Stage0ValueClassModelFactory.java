/*
 * Copyright (c) 2015, vir
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
package com.github.sviperll.adt4j.model;

import com.github.sviperll.adt4j.GenerateValueClassForVisitor;
import com.github.sviperll.adt4j.GenerateValueClassForVisitorProcessor;
import com.github.sviperll.adt4j.Visitor;
import com.github.sviperll.adt4j.model.config.ValueClassConfiguration;
import com.github.sviperll.adt4j.model.config.VisitorModel;
import com.github.sviperll.adt4j.model.util.GenerationProcess;
import com.github.sviperll.adt4j.model.util.Source;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import javax.annotation.Generated;

/**
 *
 * @author vir
 */
public class Stage0ValueClassModelFactory {
    public static Stage0ValueClassModelFactory createFactory(JDefinedClassFactory factory) {
        return new Stage0ValueClassModelFactory(factory);
    }
    private final JDefinedClassFactory factory;

    private Stage0ValueClassModelFactory(JDefinedClassFactory factory) {
        this.factory = factory;
    }

    public Stage0ValueClassModel createStage0Model(JDefinedClass bootModel, Visitor visitorAnnotation) {
        GenerationProcess generation = new GenerationProcess();
        JAnnotationUse annotation = null;
        for (JAnnotationUse anyAnnotation: bootModel.annotations()) {
            AbstractJClass annotationClass = anyAnnotation.getAnnotationClass();
            if (!annotationClass.isError()) {
                String fullName = annotationClass.fullName();
                if (fullName != null && fullName.equals(GenerateValueClassForVisitor.class.getName()))
                    annotation = anyAnnotation;
            }
        }
        if (annotation == null)
            throw new IllegalStateException("ValueClassModelFactory can't be run for interface without " + GenerateValueClassForVisitor.class + " annotation");
        VisitorModel visitorModel = generation.processGenerationResult(VisitorModel.createInstance(bootModel, visitorAnnotation));
        ValueClassConfiguration configuration = generation.processGenerationResult(ValueClassConfiguration.createInstance(visitorModel, annotation));
        int mods = configuration.isValueClassPublic() ? JMod.PUBLIC: JMod.NONE;
        JDefinedClass valueClass;
        try {
            valueClass = factory.defineClass(bootModel._package().name(), mods, configuration.valueClassName());
        } catch (JClassAlreadyExistsException ex) {
            return new Stage0ValueClassModel("Class " + configuration.valueClassName() + " already exists");
        }
        JAnnotationUse generatedAnnotation = valueClass.annotate(Generated.class);
        generatedAnnotation.param("value", GenerateValueClassForVisitorProcessor.class.getName());
        Source.annotateParametersAreNonnullByDefault(valueClass);
        return new Stage0ValueClassModel(valueClass);
    }

    public interface JDefinedClassFactory {
        JDefinedClass defineClass(String packageName, int mods, String className) throws JClassAlreadyExistsException;
    }
}
