/*
 * Copyright (c) 2015, Victor Nazarov <asviraspossible@gmail.com>
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

import com.github.sviperll.adt4j.Getter;
import com.github.sviperll.adt4j.Updater;
import com.github.sviperll.adt4j.model.util.Source;
import com.github.sviperll.adt4j.MemberAccess;
import com.github.sviperll.meta.SourceCodeValidationException;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;
import java.text.MessageFormat;
import java.util.Map;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class FieldReader {
    private final Map<String, FieldConfiguration> fieldMap;

    FieldReader(Map<String, FieldConfiguration> gettersMap) {
        this.fieldMap = gettersMap;
    }

    void readGetter(JMethod interfaceMethod, JVar param, AbstractJType paramType, boolean isVarArg) throws SourceCodeValidationException {
        for (JAnnotationUse annotationUsage: param.annotations()) {
            String annotationClassName = annotationUsage.getAnnotationClass().fullName();
            if (annotationClassName != null && annotationClassName.equals(Getter.class.getName())) {
                String getterName = Source.getAnnotationArgument(annotationUsage, "name", String.class);
                if (getterName.equals(":auto"))
                    getterName = param.name();
                MemberAccess accessLevel = Source.getAnnotationArgument(annotationUsage, "access", MemberAccess.class);
                boolean isNullable = Source.isNullable(param);
                FieldFlags flags = new FieldFlags(isNullable, isVarArg, accessLevel);
                FieldConfiguration configuration = new FieldConfiguration(getterName, paramType, flags);
                read(interfaceMethod, param, configuration);
            }
        }
    }

    void readUpdater(JMethod interfaceMethod, JVar param, AbstractJType paramType, boolean isVarArg) throws SourceCodeValidationException {
        for (JAnnotationUse annotationUsage: param.annotations()) {
            String annotationClassName = annotationUsage.getAnnotationClass().fullName();
            if (annotationClassName != null && annotationClassName.equals(Updater.class.getName())) {
                String updaterName = Source.getAnnotationArgument(annotationUsage, "name", String.class);
                if (updaterName.equals(":auto"))
                    updaterName = "with" + Source.capitalize(param.name());
                MemberAccess accessLevel = Source.getAnnotationArgument(annotationUsage, "access", MemberAccess.class);
                boolean isNullable = Source.isNullable(param);
                FieldFlags flags = new FieldFlags(isNullable, isVarArg, accessLevel);
                FieldConfiguration configuration = new FieldConfiguration(updaterName, paramType, flags);
                read(interfaceMethod, param, configuration);
            }
        }
    }

    private void read(JMethod interfaceMethod, JVar param, FieldConfiguration configuration) throws SourceCodeValidationException {
        FieldConfiguration existingConfiguration = fieldMap.get(configuration.name());
        if (existingConfiguration == null) {
            existingConfiguration = configuration;
            fieldMap.put(configuration.name(), configuration);
        }
        try {
            existingConfiguration.merge(interfaceMethod, param.name(), configuration);
        } catch (FieldConfigurationException ex) {
            throw new SourceCodeValidationException(MessageFormat.format("Unable to configure {0} getter: {1}",
                                                                                 configuration.name(), ex.getMessage()), ex);
        }
    }

}
