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
import com.github.sviperll.adt4j.Visitor;
import com.github.sviperll.adt4j.model.config.ValueClassConfiguration;
import com.github.sviperll.adt4j.model.config.VisitorModel;
import com.github.sviperll.adt4j.model.util.GenerationProcess;
import com.github.sviperll.adt4j.model.util.GenerationResult;
import com.github.sviperll.adt4j.model.util.Types;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JDefinedClass;

/**
 *
 * @author vir
 */
public class Stage0ValueClassModel {
    private final String error;
    private final JDefinedClass valueClass;

    Stage0ValueClassModel(String error) {
        this.error = error;
        this.valueClass = null;
    }

    Stage0ValueClassModel(JDefinedClass valueClass) {
        this.error = null;
        this.valueClass = valueClass;
    }

    public GenerationResult<Stage1ValueClassModel> createStage1Model(JDefinedClass jVisitorModel, Visitor visitorAnnotation) {
        GenerationProcess generation = new GenerationProcess();
        if (error != null) {
            generation.reportError(error);
            return generation.createGenerationResult(null);
        } else {
            JAnnotationUse annotation = null;
            for (JAnnotationUse anyAnnotation: jVisitorModel.annotations()) {
                AbstractJClass annotationClass = anyAnnotation.getAnnotationClass();
                if (!annotationClass.isError()) {
                    String fullName = annotationClass.fullName();
                    if (fullName != null && fullName.equals(GenerateValueClassForVisitor.class.getName()))
                        annotation = anyAnnotation;
                }
            }
            if (annotation == null)
                throw new IllegalStateException("ValueClassModelFactory can't be run for interface without " + GenerateValueClassForVisitor.class + " annotation");
            VisitorModel visitorModel = generation.processGenerationResult(VisitorModel.createInstance(jVisitorModel, visitorAnnotation));
            ValueClassConfiguration configuration = generation.processGenerationResult(ValueClassConfiguration.createInstance(visitorModel, annotation, valueClass));
            Stage1ValueClassModel result = createStage1Model(configuration);
            return generation.createGenerationResult(result);
        }
    }

    private Stage1ValueClassModel createStage1Model(ValueClassConfiguration visitorInterface) throws RuntimeException {
        Types types = Types.createInstance(valueClass.owner());
        Stage1ValueClassModel model = new Stage1ValueClassModel(valueClass, visitorInterface, types);
        model.fullySpecifyClassHeader();
        return model;  
    }
}
