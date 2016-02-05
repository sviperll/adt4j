/*
 * Copyright (c) 2015, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
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

package com.github.sviperll.adt4j.examples;

import com.github.sviperll.adt4j.WrapsGeneratedValueClass;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@WrapsGeneratedValueClass(visitor = WrappedListVisitor.class)
public class WrappedList<T> extends WrappedListBase<T> {
    @SuppressWarnings("rawtypes")
    private static final long serialVersionUID = 1L;

    public static <T> WrappedList<T> join(WrappedList<WrappedList<T>> lists) {
        return lists.accept(new WrappedListVisitor<WrappedList<T>, WrappedList<T>>() {
            @Override
            public WrappedList<T> empty() {
                return WrappedList.<T>empty();
            }

            @Override
            public WrappedList<T> prepend(WrappedList<T> head, WrappedList<WrappedList<T>> tail) {
                return head.append(join(tail));
            }
        });
    }

    WrappedList(WrappedListBase<T> value) {
         super(value);
    }

    public WrappedList<T> append(final WrappedList<T> last) {
        return accept(new WrappedListVisitor<T, WrappedList<T>>() {
            @Override
            public WrappedList<T> empty() {
                return last;
            }

            @Override
            public WrappedList<T> prepend(T head, WrappedList<T> tail) {
                return WrappedList.prepend(head, tail.append(last));
            }
        });
    }

    public <U> WrappedList<U> map(final Function<T, U> function) {
        return accept(new WrappedListVisitor<T, WrappedList<U>>() {
            @Override
            public WrappedList<U> empty() {
                return WrappedList.empty();
            }

            @Override
            public WrappedList<U> prepend(T head, WrappedList<T> tail) {
                return WrappedList.prepend(function.apply(head), tail.map(function));
            }
        });
    }
}
