/*
 * Copyright (c) 2016, vir
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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author vir
 */
public class MapFactories {
    @SuppressWarnings("rawtypes")
    private static final MapFactory TREE_MAP = new TreeMapFactory();

    @SuppressWarnings("rawtypes")
    private static final MapFactory HASH_MAP = new HashMapFactory();

    @SuppressWarnings("unchecked")
    public static <K extends Comparable<? super K>, V> MapFactory<K, V> treeMap() {
        return TREE_MAP;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> MapFactory<K, V> hashMap() {
        return HASH_MAP;
    }
    private MapFactories() {
    }

    private static class TreeMapFactory<K extends Comparable<? super K>, V> implements MapFactory<K, V> {
        @Override
        public Map<K, V> createInitialMap() {
            return new TreeMap<>();
        }

        @Override
        public Map<K, V> createCopyOf(Map<? extends K, ? extends V> values) {
            return new TreeMap<>(values);
        }
    }
    private static class HashMapFactory<K, V> implements MapFactory<K, V> {
        @Override
        public Map<K, V> createInitialMap() {
            return new HashMap<>();
        }

        @Override
        public Map<K, V> createCopyOf(Map<? extends K, ? extends V> values) {
            return new HashMap<>(values);
        }
    }
}
