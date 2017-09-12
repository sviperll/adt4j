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

package com.github.sviperll.codemold;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class ReflectedNesting extends Nesting {

    private final int modifiers;
    private final ObjectDefinition parent;

    ReflectedNesting(int modifiers, ObjectDefinition parent) {
        this.modifiers = modifiers;
        this.parent = parent;
    }

    @Nonnull
    @Override
    public MemberAccess accessLevel() {
        if ((modifiers & Modifier.PUBLIC) != 0) {
            return MemberAccess.PUBLIC;
        } else if ((modifiers & Modifier.PROTECTED) != 0) {
            return MemberAccess.PROTECTED;
        } else if ((modifiers & Modifier.PRIVATE) != 0) {
            return MemberAccess.PRIVATE;
        } else {
            return MemberAccess.PACKAGE;
        }
    }

    @Override
    public boolean isStatic() {
        return (modifiers & Modifier.STATIC) != 0;
    }

    @Nonnull
    @Override
    public ObjectDefinition parent() {
        return parent;
    }

}
