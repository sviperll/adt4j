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

package com.github.sviperll.codemold.util;

import com.github.sviperll.codemold.AnyType;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.Nonnull;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class Collections2 {
    public static <T> List<T> newArrayList() {
        return new ArrayListWrapper<>();
    }

    public static <T> List<T> newArrayList(int size) {
        return new ArrayListWrapper<>(size);
    }

    public static <T> List<T> newArrayList(Collection<? extends T> c) {
        return new ArrayListWrapper<>(c);
    }

    static class ArrayListWrapper<T> extends AbstractList<T> {
        private List<T> list;
        private boolean copyOnWrite = false;

        ArrayListWrapper() {
            list = new ArrayList<>();
        }
        ArrayListWrapper(int size) {
            list = new ArrayList<>(size);
        }
        ArrayListWrapper(Collection<? extends T> c) {
            list = new ArrayList<>(c);
        }

        List<? extends T> unmodifiable() {
            list = Collections.unmodifiableList(list);
            copyOnWrite = true;
            return list;
        }

        @Override
        public T get(int index) {
            return list.get(index);
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public void add(int index, T element) {
            copyOnWrite();
            list.add(index, element);
        }

        @Override
        public T remove(int index) {
            copyOnWrite();
            return list.remove(index);
        }

        @Override
        public T set(int index, T element) {
            copyOnWrite();
            return list.set(index, element);
        }

        private void copyOnWrite() {
            if (copyOnWrite) {
                list = new ArrayList<>(list);
                copyOnWrite = false;
            }
        }
    }
}
