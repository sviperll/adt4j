

package com.github.sviperll.codemold.util;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class Snapshot {
    public static <T> List<? extends T> of(List<? extends T> argument) {
        return preciseTypeSnapshotOf(argument);
    }

    public static <K, V> Map<? extends K, ? extends V> of(Map<? extends K, ? extends V> argument) {
        return preciseTypeSnapshotOf(argument);
    }

    public static <T> Set<? extends T> of(Set<? extends T> argument) {
        if (isKnownToBeImmutable(argument))
            return argument;
        else
            return markedAsKnownToBeImmutableSet(Collections.unmodifiableSet(new HashSet<>(argument)));
    }

    public static <T> Collection<? extends T> of(Collection<? extends T> argument) {
        return preciseTypeSnapshotOf(argument);
    }

    static <T> List<T> markedAsKnownToBeImmutableList(List<T> argument) {
        return new KnownToBeImmutableList<>(argument);
    }

    static <T> Set<T> markedAsKnownToBeImmutableSet(Set<T> argument) {
        return new KnownToBeImmutableSet<>(argument);
    }

    static <T> Collection<T> markedAsKnownToBeImmutableCollection(Collection<T> argument) {
        return new KnownToBeImmutableCollection<>(argument);
    }

    static <K, V> Map<K, V> markedAsKnownToBeImmutableMap(Map<K, V> argument) {
        return new KnownToBeImmutableMap<>(argument);
    }

    static <T> boolean isKnownToBeImmutable(Collection<T> argument) {
        return argument instanceof KnownToBeImmutableCollection
                || argument instanceof KnownToBeImmutableSet
                || argument instanceof KnownToBeImmutableList;
    }

    static <K, V> boolean isKnownToBeImmutable(Map<K, V> argument) {
        return argument instanceof KnownToBeImmutableMap;
    }

    private static <T> Collection<? extends T> preciseTypeSnapshotOf(Collection<T> argument) {
        if (isKnownToBeImmutable(argument))
            return argument;
        else if (argument instanceof List)
            return Snapshot.of((List<T>)argument);
        else if (argument instanceof Set)
            return Snapshot.of((Set<T>)argument);
        else
            return markedAsKnownToBeImmutableList(Collections.unmodifiableList(new ArrayList<>(argument)));
    }

    private static <T> List<? extends T> preciseTypeSnapshotOf(List<T> argument) {
        if (isKnownToBeImmutable(argument)) {
            return argument;
        } else if (argument instanceof SnapshotableArrayList) {
            SnapshotableArrayList<T> snapshotable = (SnapshotableArrayList<T>)argument;
            return snapshotable.snapshot();
        } else {
            return markedAsKnownToBeImmutableList(Collections.unmodifiableList(new ArrayList<>(argument)));
        }
    }

    private static <K, V> Map<? extends K, ? extends V> preciseTypeSnapshotOf(Map<K, V> argument) {
        if (isKnownToBeImmutable(argument)) {
            return argument;
        } else if (argument instanceof SnapshotableHashMap) {
            SnapshotableHashMap<K, V> snapshotable = (SnapshotableHashMap<K, V>)argument;
            return snapshotable.snapshot();
        } else {
            return markedAsKnownToBeImmutableMap(Collections.unmodifiableMap(new HashMap<>(argument)));
        }
    }

    private static class KnownToBeImmutableList<T> extends AbstractList<T> {
        private final List<T> list;
        private KnownToBeImmutableList(List<T> list) {
            this.list = list;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return list.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }

        @Override
        public Object[] toArray() {
            return list.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return list.toArray(a);
        }

        @Override
        public boolean add(T e) {
            return list.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return list.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return list.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            return list.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            return list.addAll(index, c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return list.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return list.retainAll(c);
        }

        @Override
        public void clear() {
            list.clear();
        }

        @Override
        public T get(int index) {
            return list.get(index);
        }

        @Override
        public T set(int index, T element) {
            return list.set(index, element);
        }

        @Override
        public void add(int index, T element) {
            list.add(index, element);
        }

        @Override
        public T remove(int index) {
            return list.remove(index);
        }

        @Override
        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        @Override
        public ListIterator<T> listIterator() {
            return list.listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return list.listIterator();
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return markedAsKnownToBeImmutableList(list.subList(fromIndex, toIndex));
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            list.subList(fromIndex, toIndex).clear();
        }
    }

    private static class KnownToBeImmutableSet<T> extends AbstractSet<T> {
        private final Set<T> set;
        private KnownToBeImmutableSet(Set<T> set) {
            this.set = set;
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean isEmpty() {
            return set.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return set.iterator();
        }

        @Override
        public Object[] toArray() {
            return set.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return set.toArray(a);
        }

        @Override
        public boolean add(T e) {
            return set.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return set.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            return set.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return set.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return set.removeAll(c);
        }

        @Override
        public void clear() {
            set.clear();
        }
    }

    private static class KnownToBeImmutableCollection<T> extends AbstractCollection<T> {
        private final Collection<T> set;
        private KnownToBeImmutableCollection(Collection<T> set) {
            this.set = set;
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean isEmpty() {
            return set.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return set.iterator();
        }

        @Override
        public Object[] toArray() {
            return set.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return set.toArray(a);
        }

        @Override
        public boolean add(T e) {
            return set.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return set.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            return set.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return set.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return set.removeAll(c);
        }

        @Override
        public void clear() {
            set.clear();
        }
    }

    private static class KnownToBeImmutableMap<K, V> extends AbstractMap<K, V> {
        private final Map<K, V> map;
        private Set<K> keySet = null;
        private Set<Entry<K, V>> entrySet = null;
        private Collection<V> values = null;
        private KnownToBeImmutableMap(Map<K, V> map) {
            this.map = map;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return map.get(key);
        }

        @Override
        public V put(K key, V value) {
            return map.put(key, value);
        }

        @Override
        public V remove(Object key) {
            return map.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            map.putAll(m);
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public Set<K> keySet() {
            if (keySet == null)
                keySet = markedAsKnownToBeImmutableSet(map.keySet());
            return keySet;
        }

        @Override
        public Collection<V> values() {
            if (values == null)
                values = markedAsKnownToBeImmutableCollection(map.values());
            return values;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            if (entrySet == null)
                entrySet= markedAsKnownToBeImmutableSet(map.entrySet());
            return entrySet;
        }
    }

    private Snapshot() {
    }
}
