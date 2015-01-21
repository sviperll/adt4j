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
package com.github.sviperll.adt4j.model.util;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJGenerifiable;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JPrimitiveType;
import com.helger.jcodemodel.JTypeVar;
import java.io.Serializable;
import java.util.Iterator;

public class Types {
    public static Types createInstance(JCodeModel codeModel) {
        return new Types(codeModel);
    }

    public static JTypeVar generifyWithBoundsFrom(IJGenerifiable generifiable, String typeParameterName, AbstractJClass typeParameterBounds) {
        JTypeVar result = generifiable.generify(typeParameterName);
        result.bound(typeParameterBounds._extends());
        Iterator<AbstractJClass> iterator = typeParameterBounds._implements();
        while (iterator.hasNext())
            result.bound(iterator.next());
        return result;
    }

    public final JPrimitiveType _void;
    public final JPrimitiveType _long;
    public final JPrimitiveType _boolean;
    public final JPrimitiveType _int;
    public final JPrimitiveType _float;
    public final JPrimitiveType _double;
    public final AbstractJClass _Object;
    public final AbstractJClass _Boolean;
    public final AbstractJClass _Integer;
    public final AbstractJClass _Double;
    public final AbstractJClass _Float;
    public final AbstractJClass _RuntimeException;
    public final AbstractJClass _Long;
    public final AbstractJClass _NullPointerException;
    public final AbstractJClass _Serializable;
    public final AbstractJClass _Comparable;
    public final AbstractJClass _String;
    public final AbstractJClass _StringBuilder;
    public final AbstractJClass _IllegalStateException;
    public final AbstractJClass _Math;

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
        _Comparable = codeModel.ref(Comparable.class);
        _String = codeModel.ref(String.class);
        _StringBuilder = codeModel.ref(StringBuilder.class);
        _IllegalStateException = codeModel.ref(IllegalStateException.class);
        _Math = codeModel.ref(Math.class);
    }

    public boolean isSerializable(AbstractJType type) throws SourceException {
        if (type.isPrimitive() || type.isArray())
            return type.isPrimitive() || type.isArray() && isSerializable(type.elementType());
        else {
            return _Serializable.isAssignableFrom(type);
        }
    }

    public boolean isComparable(AbstractJType type) throws SourceException {
        if (type.isPrimitive() || type.isArray())
            return type.isPrimitive() || type.isArray() && isComparable(type.elementType());
        else if (type instanceof AbstractJClass) {
            AbstractJClass klass = (AbstractJClass)type;
            boolean result = _Comparable.narrow(klass.wildcardSuper()).isAssignableFrom(klass);
            return result;
        } else
            throw new IllegalStateException("Unexpected jcodemodel type: " + type);
    }
}
