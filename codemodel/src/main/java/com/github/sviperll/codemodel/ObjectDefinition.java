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

import java.util.Collection;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 */
@ParametersAreNonnullByDefault
public abstract class ObjectDefinition<T extends Residence>
        implements Settled<T>, Model, GenericDefinition<T>, TypeDefinition<ObjectType> {

    ObjectDefinition() {
    }


    public abstract boolean isFinal();

    public abstract ObjectKind kind();

    public abstract ObjectType extendsClass();

    public abstract List<ObjectType> implementsInterfaces();

    public abstract Collection<MethodDefinition> methods();

    public abstract Collection<ObjectDefinition<NestedResidence>> innerClasses();

    public abstract Collection<FieldDeclaration> fields();

    public abstract String simpleName();

    public final String qualifiedName() {
        return residence().getPackage().qualifiedName() + "." + simpleName();
    }

    public final boolean extendsOrImplements(ObjectType objectType) {
        if (this.extendsClass().definition() == objectType.definition()
                || this.extendsClass().definition().extendsOrImplements(objectType))
            return true;
        else {
            for (ObjectType implementedInterface: implementsInterfaces()) {
                if (implementedInterface.definition() == objectType.definition()
                        || implementedInterface.definition().extendsOrImplements(objectType))
                    return true;
            }
            return false;
        }
    }
}
