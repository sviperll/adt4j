/*
 * Copyright (c) 2016, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
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

package com.github.sviperll.codemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class MethodBuilder
        implements MethodLikeBuilder {
    private final BuiltDefinition definition = new BuiltDefinition();
    private final BuiltType type = new BuiltType();
    private final GenericsConfigBuilder<NestedResidence> generics = new GenericsConfigBuilder<>(definition);
    private final NestedResidenceBuilder residence;
    private final String name;
    private final CallableBuilder callable = new CallableBuilder();
    private boolean isFinal;
    private Type resultType = Type.voidType();

    MethodBuilder(NestedResidenceBuilder residence, String name) {
        this.residence = residence;
        this.name = name;
    }

    @Override
    public MethodDefinition definition() {
        return definition;
    }

    @Override
    public CodeModel getCodeModel() {
        return residence.getCodeModel();
    }

    @Override
    public NestedResidenceBuilder residence() {
        return residence;
    }

    @Override
    public GenericsConfigBuilder<NestedResidence> generics() {
        return generics;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public void resultType(Type resultType) {
        this.resultType = resultType;
    }

    @Override
    public CallableBuilder callable() {
        return callable;
    }

    private class BuiltDefinition extends MethodDefinition {

        @Override
        public boolean isConstructor() {
            return false;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isFinal() {
            return isFinal;
        }

        @Override
        public Type returnType() {
            return resultType;
        }

        @Override
        CallableDefinition callable() {
            return callable.definition();
        }

        @Override
        public NestedResidence residence() {
            return residence.residence();
        }

        @Override
        public GenericsConfig generics() {
            return generics.generics();
        }

        @Override
        public CodeModel getCodeModel() {
            return residence.getCodeModel();
        }

        @Override
        public MethodType toType() {
            return type;
        }
    }

    private class BuiltType extends RawMethodType {

        @Override
        public MethodDefinition definition() {
            return definition;
        }
    }
}
