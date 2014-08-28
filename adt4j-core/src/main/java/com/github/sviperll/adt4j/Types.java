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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JPrimitiveType;
import com.helger.jcodemodel.JTypeVar;
import java.io.Serializable;

class Types {
    public static AbstractJClass narrow(JDefinedClass definedClass, JTypeVar[] typeParams) {
        return typeParams.length == 0 ? definedClass : definedClass.narrow(typeParams);
    }

    public static Types createInstance(JCodeModel codeModel) {
        return new Types(codeModel);
    }

    private final JPrimitiveType _void;
    private final JPrimitiveType _long;
    private final JPrimitiveType _boolean;
    private final JPrimitiveType _int;
    private final JPrimitiveType _float;
    private final JPrimitiveType _double;
    private final AbstractJClass _Object;
    private final AbstractJClass _Boolean;
    private final AbstractJClass _Integer;
    private final AbstractJClass _Double;
    private final AbstractJClass _Float;
    private final AbstractJClass _RuntimeException;
    private final AbstractJClass _Long;
    private final AbstractJClass _NullPointerException;
    private final AbstractJClass _Serializable;

    private Types(JCodeModel codeModel) {
        _void = codeModel.VOID;
        _long = codeModel.LONG;
        _boolean = codeModel.BOOLEAN;
        _int = codeModel.INT;
        _float = codeModel.FLOAT;
        _double = codeModel.DOUBLE;

        _Object = codeModel.ref(Object.class);
        _Boolean = codeModel.ref(Boolean.class);
        _Integer = codeModel.ref(Integer.class);
        _Long = codeModel.ref(Long.class);
        _Double = codeModel.ref(Double.class);
        _Float = codeModel.ref(Float.class);
        _RuntimeException = codeModel.ref(RuntimeException.class);
        _NullPointerException = codeModel.ref(NullPointerException.class);
        _Serializable = codeModel.ref(Serializable.class);
    }

    public JPrimitiveType _void() {
        return _void;
    }

    public JPrimitiveType _int() {
        return _int;
    }

    public JPrimitiveType _long() {
        return _long;
    }

    public JPrimitiveType _boolean() {
        return _boolean;
    }

    public JPrimitiveType _float() {
        return _float;
    }

    public JPrimitiveType _double() {
        return _double;
    }

    public AbstractJClass _Boolean() {
        return _Boolean;
    }

    public AbstractJClass _Object() {
        return _Object;
    }

    public AbstractJClass _RuntimeException() {
        return _RuntimeException;
    }

    public AbstractJClass _Integer() {
        return _Integer;
    }

    public AbstractJClass _Double() {
        return _Double;
    }

    public AbstractJClass _Float() {
        return _Float;
    }

    public AbstractJClass _Long() {
        return _Long;
    }

    public AbstractJClass _NullPointerException() {
        return _NullPointerException;
    }

    public AbstractJClass _Serializable() {
        return _Serializable;
    }

    public boolean isSerializable(AbstractJType type) throws SourceException {
        if (type.isPrimitive() || type.isArray())
            return type.isPrimitive() || type.isArray() && isSerializable(type.elementType());
        else {
            AbstractJClass klass = (AbstractJClass)type.erasure();
            return _Serializable.isAssignableFrom(klass);
        }
    }
}
