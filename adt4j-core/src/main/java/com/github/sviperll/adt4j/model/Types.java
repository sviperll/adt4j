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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJGenerifiable;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JNullType;
import com.helger.jcodemodel.JPrimitiveType;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JTypeWildcard;
import java.io.Serializable;
import java.util.Iterator;

class Types {
    public static Types createInstance(JCodeModel codeModel) {
        return new Types(codeModel);
    }

    static JTypeVar generifyWithBoundsFrom(IJGenerifiable generifiable, String typeParameterName, AbstractJClass typeParameterBounds) {
        JTypeVar result = generifiable.generify(typeParameterName);
        result.bound(typeParameterBounds._extends());
        Iterator<AbstractJClass> iterator = typeParameterBounds._implements();
        while (iterator.hasNext())
            result.bound(iterator.next());
        return result;
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
    private final AbstractJClass _Comparable;
    private final AbstractJClass _String;
    private final AbstractJClass _StringBuilder;
    private final AbstractJClass _IllegalStateException;
    private final AbstractJClass _Math;

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

    public AbstractJClass _Comparable() {
        return _Comparable;
    }

    public AbstractJClass _String() {
        return _String;
    }

    public AbstractJClass _StringBuilder() {
        return _StringBuilder;
    }

    public AbstractJClass _IllegalStateException() {
        return _IllegalStateException;
    }

    public AbstractJClass _Math() {
        return _Math;
    }

    public boolean isSerializable(AbstractJType type) throws SourceException {
        if (type.isPrimitive() || type.isArray())
            return type.isPrimitive() || type.isArray() && isSerializable(type.elementType());
        else {
            return isSubtype(_Serializable, type);
        }
    }

    public boolean isComparable(AbstractJType type) throws SourceException {
        if (type.isPrimitive() || type.isArray())
            return type.isPrimitive() || type.isArray() && isComparable(type.elementType());
        else {
            return isSubtype(_Comparable.narrow(type), type);
        }
    }

    private boolean isSubtype(AbstractJType type1, AbstractJType type2) {
        if (type1.isArray() && type2.isArray()) {
            return type1.elementType() == type2.elementType();
        } else if (type1.isPrimitive() && type2.isPrimitive()) {
            return type1 == type2;
        } else if (type1.isReference() && type2.isReference()) {
            AbstractJClass class1 = (AbstractJClass)type1;
            AbstractJClass class2 = (AbstractJClass)type2;

            if (class2 instanceof JNullType)
                return true;
            else if (class1 == _Object)
                return true;
            else if (class1 == class2)
                return true;
            else if (isUnifiable(class1, class2)) {
                return true;
            } else if (class1.erasure() == class2.erasure()) {
                if (!class1.isParameterized() || !class2.isParameterized())
                    return true;
                else {
                    for (int i = 0; i < class1.getTypeParameters().size(); i++) {
                        AbstractJClass parameter1 = class1.getTypeParameters().get(i);
                        AbstractJClass parameter2 = class2.getTypeParameters().get(i);
                        if (!isUnifiable(parameter1, parameter2)) {
                            return false;
                        }
                    }
                    return true;
                }
            } else {
                AbstractJClass class2Base = class2._extends();
                if (class2Base != null && isSubtype(class1, class2Base))
                    return true;
                else {
                    Iterator<AbstractJClass> i = class2._implements();
                    while (i.hasNext()) {
                        if (isSubtype(class1, i.next()))
                            return true;
                    }
                    return false;
                }
            }
        } else
            return false;
    }

    private boolean isUnifiable(AbstractJClass type1, AbstractJClass type2) {
        if (type1 == type2)
            return true;
        else if (type1 instanceof JTypeWildcard) {
            JTypeWildcard wild1 = (JTypeWildcard)type1;
            if (wild1.boundMode() == JTypeWildcard.EBoundMode.EXTENDS) {
                return isSubtype(wild1.bound(), type2);
            } else {
                return isSubtype(type2, wild1.bound());
            }
        } else if (type2 instanceof JTypeWildcard) {
            JTypeWildcard wild2 = (JTypeWildcard)type1;
            if (wild2.boundMode() == JTypeWildcard.EBoundMode.EXTENDS) {
                return isSubtype(wild2.bound(), type1);
            } else {
                return isSubtype(type1, wild2.bound());
            }
        } else
            return false;
    }
}
