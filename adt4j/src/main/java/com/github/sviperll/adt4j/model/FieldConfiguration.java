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
package com.github.sviperll.adt4j.model;

import com.github.sviperll.metachicory.AccessLevel;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JMethod;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class FieldConfiguration {
    private final Map<String, String> map = new TreeMap<String, String>();
    private final AbstractJType type;
    private final String name;
    private FieldFlags flags;

    FieldConfiguration(String name, AbstractJType paramType, AccessLevel accessLevel) {
        this.type = paramType;
        this.name = name;
        this.flags = new FieldFlags(accessLevel);
    }

    void put(AbstractJType paramType, JMethod method, String paramName, FieldFlags flags) throws FieldConfigurationException {
        if (!type.equals(paramType))
            throw new FieldConfigurationException(MessageFormat.format("Unable to config {0} field: inconsitent field types",
                                                                       name));
        String oldField = map.put(method.name(), paramName);
        if (oldField != null)
            throw new FieldConfigurationException(MessageFormat.format("Unable to config {0} field: both {1} and {2} parameters of {3} are referenced as the single {4} field",
                                                                       name, oldField, paramName, method.name(), name));
        try {
            this.flags = this.flags.join(flags);
        } catch (FieldFlagsException ex) {
            throw new FieldConfigurationException(MessageFormat.format("Unable to config {0} field: {1}", name,
                                                                       ex.getMessage()), ex);
        }
    }

    AbstractJType type() {
        return type;
    }

    String name() {
        return name;
    }

    AccessLevel accessLevel() {
        return flags.accessLevel();
    }

    boolean isNullable() {
        return flags.isNullable();
    }

    boolean isFieldValue(JMethod method, String paramName) {
        String getterParamName = map.get(method.name());
        return getterParamName != null && getterParamName.equals(paramName);
    }

    boolean isVarArg() {
        return flags.isVarArg();
    }

}
