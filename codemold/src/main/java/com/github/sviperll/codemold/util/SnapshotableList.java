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

import java.text.MessageFormat;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class SnapshotableList<T> extends AbstractList<T> {

    private List<T> list;
    private boolean shouldCopyOnWrite = false;
    private ListFactory<T> factory;

    SnapshotableList(ListFactory<T> factory) {
        this.factory = factory;
        list = factory.createInitialList();
    }

    SnapshotableList(ListFactory<T> factory, Collection<? extends T> c) {
        list = factory.createCopyOf(c);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    List<? extends T> snapshot() {
        if (!shouldCopyOnWrite) {
            list = Snapshot.markedAsKnownToBeImmutableList(Collections.unmodifiableList(list));
            shouldCopyOnWrite = true;
        }
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
        if (!(0 <= index && index <= size())) {
            throw new ArrayIndexOutOfBoundsException(index);
        } else {
            copyOnWrite();
            list.add(index, element);
        }
    }

    @Override
    public T remove(int index) {
        if (!(0 <= index && index < size())) {
            throw new ArrayIndexOutOfBoundsException(index);
        } else {
            copyOnWrite();
            return list.remove(index);
        }
    }

    @Override
    public T set(int index, T element) {
        if (!(0 <= index && index < size())) {
            throw new ArrayIndexOutOfBoundsException(index);
        } else {
            copyOnWrite();
            return list.set(index, element);
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        if (!(0 <= index && index <= size())) {
            throw new ArrayIndexOutOfBoundsException(index);
        } else {
            copyOnWrite();
            return list.addAll(index, c);
        }
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            copyOnWrite();
            list.clear();
        }
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        if (!(0 <= fromIndex && fromIndex <= size())) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        } else if (!(0 <= toIndex && toIndex <= size())) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        } else if (fromIndex > toIndex) {
            throw new IllegalArgumentException(MessageFormat.format("fromIndex {0} is greater than toIndex {1}", fromIndex, toIndex));
        } else if (fromIndex != toIndex) {
            copyOnWrite();
            list.subList(fromIndex, toIndex).clear();
        }
    }

    private void copyOnWrite() {
        if (shouldCopyOnWrite) {
            list = factory.createCopyOf(list);
            shouldCopyOnWrite = false;
        }
    }

}
