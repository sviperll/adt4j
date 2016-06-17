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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
class VariableScope {
    static VariableScope createTopLevel() {
        return new VariableScope();
    }

    private final List<VariableScope> childScopes = new ArrayList<>();
    private final Set<String> variables = new TreeSet<>();
    private final VariableScope parent;
    private VariableScope() {
        this(null);
    }

    private VariableScope(VariableScope parent) {
        this.parent = parent;
    }

    void introduce(String name) throws CodeModelException {
        CodeModel.validateSimpleName(name);
        if (isDefinedInScope(name))
            throw new CodeModelException("Variable already defined in current scope");
        if (isReservedByChildScope(name))
            throw new CodeModelException("Variable is reserved by enclosed scope");
        variables.add(name);
    }

    @Nonnull
    String makeIntroducable(String name) {
        if (!name.endsWith("%")) {
            return name;
        } else {
            String prefix = name.substring(0, name.length() - 1);
            if (isIntroduceable(prefix))
                return prefix;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                String candidate = prefix + i;
                if (isIntroduceable(candidate))
                    return candidate;
            }
            throw new IllegalStateException("Unintroducable variable: " + name);
        }
    }

    @Nonnull
    VariableScope createNested() {
        VariableScope result = new VariableScope(this);
        childScopes.add(result);
        return result;
    }

    private boolean isDefinedInScope(String name) {
        return variables.contains(name) || (parent != null && parent.isDefinedInScope(name));
    }

    private boolean isReservedByChildScope(String name) {
        for (VariableScope child: childScopes) {
            if (child.variables.contains(name) || child.isReservedByChildScope(name))
                return true;
        }
        return false;
    }

    private boolean isIntroduceable(String name) {
        return !isDefinedInScope(name) && !isReservedByChildScope(name);
    }
}
