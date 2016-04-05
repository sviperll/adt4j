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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
abstract class Substitution {
    public static final Substitution EMPTY = new EmptySubstitution();

    static Builder createBuilder() {
        return new Builder();
    }

    private Substitution() {
    }

    abstract Type get(String name);

    final Substitution andThen(Substitution that) {
        return new AndThenSubstitution(this, that);
    }

    static class Builder {
        private Map<String, Type> map = new TreeMap<>();
        private boolean copyOnWrite = false;

        private Builder() {
        }

        void put(String name, Type typeArgument) {
            if (copyOnWrite) {
                map = new TreeMap<>(map);
                copyOnWrite = false;
            }
            map.put(name, typeArgument);
        }

        Substitution build() {
            map = Collections.unmodifiableMap(map);
            copyOnWrite = true;
            return new MapSubstitution(map);
        }
    }

    private static class MapSubstitution extends Substitution {
        private final Map<String, Type> map;

        private MapSubstitution(Map<String, Type> map) {
            this.map = map;
        }
        @Override
        Type get(String name) {
            return map.get(name);
        }
    }

    private static class EmptySubstitution extends Substitution {

        public EmptySubstitution() {
        }

        @Override
        Type get(String name) {
            return null;
        }
    }
    private static class AndThenSubstitution extends Substitution {

        private final Substitution first;
        private final Substitution second;

        public AndThenSubstitution(Substitution first, Substitution second) {
            this.first = first;
            this.second = second;
        }

        @Override
        Type get(String name) {
            Type value = first.get(name);
            return value != null ? value : second.get(name);
        }
    }
}
