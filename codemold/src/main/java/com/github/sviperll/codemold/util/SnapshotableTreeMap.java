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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
 class SnapshotableTreeMap<K, V> extends AbstractMap<K, V> {

    private Map<K, V> map;
    private Set<Entry<K, V>> entrySet = null;
    private boolean shouldCopyOnWrite = false;

    SnapshotableTreeMap() {
        map = new TreeMap<>();
    }

    SnapshotableTreeMap(Map<? extends K, ? extends V> m) {
        map = new TreeMap<>(m);
    }

    Map<? extends K, ? extends V> snapshot() {
        if (!shouldCopyOnWrite) {
            map = Snapshot.markedAsKnownToBeImmutableMap(Collections.unmodifiableMap(map));
            entrySet = null;
            shouldCopyOnWrite = true;
        }
        return map;
    }

    private void copyOnWrite() {
        if (shouldCopyOnWrite) {
            map = new TreeMap<>(map);
            entrySet = null;
            shouldCopyOnWrite = false;
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet(map.entrySet());
        }
        return entrySet;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V remove(Object key) {
        copyOnWrite();
        return map.remove(key);
    }

    @Override
    public V put(K key, V value) {
        copyOnWrite();
        return map.put(key, value);
    }

    private class EntrySet extends AbstractSet<Entry<K, V>> {

        private final Set<Entry<K, V>> set;

        private EntrySet(Set<Entry<K, V>> entrySet) {
            this.set = entrySet;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntrySetIterator(set.iterator());
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public void clear() {
            if (!isEmpty()) {
                copyOnWrite();
                set.clear();
            }
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }

        @Override
        public boolean remove(Object o) {
            copyOnWrite();
            return set.remove(o);
        }

        @Override
        public boolean add(Entry<K, V> entry) {
            copyOnWrite();
            return set.add(entry);
        }

        private class EntrySetIterator implements Iterator<Entry<K, V>> {

            private final Iterator<Entry<K, V>> iterator;

            private EntrySetIterator(Iterator<Entry<K, V>> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry<K, V> next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                copyOnWrite();
                iterator.remove();
            }
        }
    }
}
