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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class CMCollections {
    public static <T> List<T> newArrayList() {
        return new SnapshotableList<>(ListFactories.arrayList());
    }

    public static <T> List<T> newArrayList(Collection<? extends T> c) {
        return new SnapshotableList<>(ListFactories.arrayList(), c);
    }

    public static <K, V> Map<K, V> newHashMap() {
        return new SnapshotableMap<>(MapFactories.hashMap());
    }

    public static <K, V> Map<K, V> newHashMap(Map<? extends K, ? extends V> m) {
        return new SnapshotableMap<>(MapFactories.hashMap(), m);
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> newTreeMap() {
        return new SnapshotableMap<>(MapFactories.<K, V>treeMap());
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> newTreeMap(Map<? extends K, ? extends V> m) {
        return new SnapshotableMap<>(MapFactories.treeMap(), m);
    }

    public static <E> List<? extends E> listOf() {
        return Snapshot.markedAsKnownToBeImmutableList(Collections.<E>emptyList());
    }

    public static <E> List<? extends E> listOf(E e1) {
        return Snapshot.markedAsKnownToBeImmutableList(Collections.singletonList(e1));
    }

    public static <E> List<? extends E> listOf(E e1, E e2) {
        List<E> list = new ArrayList<>(2);
        list.add(e1);
        list.add(e2);
        return Snapshot.markedAsKnownToBeImmutableList(Collections.unmodifiableList(list));
    }

    public static <E> List<? extends E> listOf(E e1, E e2, E e3) {
        List<E> list = new ArrayList<>(3);
        list.add(e1);
        list.add(e2);
        list.add(e3);
        return Snapshot.markedAsKnownToBeImmutableList(Collections.unmodifiableList(list));
    }

    public static <E> List<? extends E> listOf(E e1, E e2, E e3, E e4) {
        List<E> list = new ArrayList<>(4);
        list.add(e1);
        list.add(e2);
        list.add(e3);
        list.add(e4);
        return Snapshot.markedAsKnownToBeImmutableList(Collections.unmodifiableList(list));
    }

    public static <E> List<? extends E> listOf(E e1, E e2, E e3, E e4, E e5) {
        List<E> list = new ArrayList<>(5);
        list.add(e1);
        list.add(e2);
        list.add(e3);
        list.add(e4);
        list.add(e5);
        return Snapshot.markedAsKnownToBeImmutableList(Collections.unmodifiableList(list));
    }

    private CMCollections() {
    }
}
