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
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
final class NarrowedExecutableTypeDetails extends ExecutableTypeDetails {

    private final Type type = Type.executable(this);
    private final RawExecutableTypeDetails erasure;
    private final List<Type> arguments;
    NarrowedExecutableTypeDetails(RawExecutableTypeDetails erasure, List<Type> arguments) throws CodeModelException {
        if (arguments.isEmpty())
            throw new CodeModelException("Type arguments shouldn't be empty");
        this.erasure = erasure;
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
    }

    @Override
    public Type erasure() {
        return erasure.asType();
    }

    @Override
    public boolean isNarrowed() {
        return true;
    }

    @Override
    public boolean isRaw() {
        return false;
    }

    @Override
    public Type narrow(List<Type> typeArguments) throws CodeModelException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Type> typeArguments() {
        return arguments;
    }

    @Override
    public ExecutableDefinition definition() {
        return erasure.definition();
    }

    @Override
    public Type asType() {
        return type;
    }

    @Override
    public Type enclosingType() {
        return erasure.enclosingType();
    }

    @Override
    public List<VariableDeclaration> parameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Type> throwsList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Type returnType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
