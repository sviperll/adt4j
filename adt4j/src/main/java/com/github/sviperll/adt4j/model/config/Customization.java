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
package com.github.sviperll.adt4j.model.config;

import com.github.sviperll.adt4j.MemberAccess;
import com.github.sviperll.adt4j.Caching;
import com.helger.jcodemodel.AbstractJClass;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class Customization {
    private final String className;
    private final APICustomization api;
    private final ImplementationCustomization implementation;
    private final AbstractJClass valueClassExtends;
    Customization(String className, AbstractJClass valueClassExtends, APICustomization api, ImplementationCustomization implementation) {
        this.className = className;
        this.valueClassExtends = valueClassExtends;
        this.api = api;
        this.implementation = implementation;
    }

    String className() {
        return className;
    }

    String acceptMethodName() {
        return api.acceptMethodName();
    }

    boolean isValueClassPublic() {
        return api.isPublic();
    }

    MemberAccess acceptMethodAccessLevel() {
        return api.acceptMethodAccessLevel();
    }

    Caching hashCodeCaching() {
        return implementation.hashCodeCaching();
    }

    boolean isValueClassSerializable() {
        return api.isSerializable();
    }

    boolean isValueClassComparable() {
        return api.isComparable();
    }

    AbstractJClass[] implementsInterfaces() {
        return api.interfaces();
    }

    int hashCodeBase() {
        return implementation.hashCodeBase();
    }

    Serialization serialization() {
        return api.serialization();
    }

    long serialVersionUIDForGeneratedCode() {
        return api.serialVersionUIDForGeneratedCode();
    }

    AbstractJClass valueClassExtends() {
        return valueClassExtends;
    }
}
