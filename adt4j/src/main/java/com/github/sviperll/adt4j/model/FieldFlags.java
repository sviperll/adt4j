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

import com.github.sviperll.adt4j.AccessLevel;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class FieldFlags {
    private final boolean isNullable;
    private final boolean isVarArg;
    private final AccessLevel accessLevel;

    FieldFlags(AccessLevel accessLevel) {
        this(true, true, accessLevel);
    }

    FieldFlags(boolean isNullable, boolean isVarArg, AccessLevel accessLevel) {
        this.isNullable = isNullable;
        this.isVarArg = isVarArg;
        this.accessLevel = accessLevel;
    }

    FieldFlags join(FieldFlags that) throws FieldFlagsException {
        if (this.accessLevel != that.accessLevel)
            throw new FieldFlagsException("Inconsitent access levels");
        return new FieldFlags(this.isNullable && that.isNullable, this.isVarArg && that.isVarArg, this.accessLevel);
    }

    boolean isNullable() {
        return isNullable;
    }

    boolean isVarArg() {
        return isVarArg;
    }

    AccessLevel accessLevel() {
        return accessLevel;
    }

}
