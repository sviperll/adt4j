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

import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class MethodSignature {
    private static int hashCodeType(ObjectType type) {
        return type.definition().hashCode();
    }

    private static int hashCodeType(AnyType type) {
        AnyType.Kind kind = type.kind();
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(kind);
        switch (kind) {
            case VOID:
                break;
            case ARRAY:
                hash = 31 * hash + hashCodeType(type.getArrayDetails().elementType());
                break;
            case OBJECT:
                hash = 31 * hash + hashCodeType(type.getObjectDetails());
                break;
            case PRIMITIVE:
                hash = 31 * hash + Objects.hashCode(type.getPrimitiveDetails());
                break;
            case TYPE_VARIABLE:
                hash = 31 * hash + Objects.hashCode(type.getVariableDetails().name());
                break;
            case WILDCARD:
                hash = 31 * hash + hashCodeType(type.getWildcardDetails().bound());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported type kind in hashCode" + kind);
        }
        return hash;
    }

    private static boolean equalsType(ObjectType type1, ObjectType type2) {
        return type1.definition().equals(type2.definition());
    }

    private static boolean equalsType(AnyType type1, AnyType type2) {
        AnyType.Kind kind = type1.kind();
        if (!Objects.equals(kind, type2.kind()))
            return false;
        switch (kind) {
            case VOID:
                break;
            case ARRAY:
                if (!equalsType(type1.getArrayDetails().elementType(), type2.getArrayDetails().elementType()))
                    return false;
                break;
            case OBJECT:
                if (!equalsType(type1.getObjectDetails(), type2.getObjectDetails()))
                    return false;
                break;
            case PRIMITIVE:
                if (!Objects.equals(type1.getPrimitiveDetails(), type2.getPrimitiveDetails()))
                    return false;
                break;
            case TYPE_VARIABLE:
                if (!Objects.equals(type1.getVariableDetails().name(), type2.getVariableDetails().name()))
                    return false;
                break;
            case WILDCARD:
                if (!equalsType(type1.getWildcardDetails().bound(), type2.getWildcardDetails().bound()))
                    return false;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported type kind in hashCode" + kind);
        }
        return true;
    }

    private final String name;
    private final List<? extends AnyType> parameterTypes;
    MethodSignature(String name, List<? extends AnyType> parameterTypes) {
        this.name = name;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.name);
        for (AnyType type: parameterTypes) {
            hash = 31 * hash + hashCodeType(type);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MethodSignature other = (MethodSignature) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.parameterTypes.size() != other.parameterTypes.size())
            return false;
        for (int i = 0; i < this.parameterTypes.size(); i++)
            if (!equalsType(this.parameterTypes.get(i), other.parameterTypes.get(i)))
                return false;
        return true;
    }
}
