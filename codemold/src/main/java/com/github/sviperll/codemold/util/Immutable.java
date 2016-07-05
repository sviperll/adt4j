

package com.github.sviperll.codemold.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class Immutable {
    public static <T> List<? extends T> copyOf(List<? extends T> argument) {
        return captureCopy(argument);
    }

    public static <T> Set<? extends T> copyOf(Set<? extends T> argument) {
        if (argument instanceof ImmutableSet)
            return argument;
        else
            return new ImmutableSet<>(Collections.unmodifiableSet(new HashSet<>(argument)));
    }

    public static <T> Collection<? extends T> copyOf(Collection<? extends T> argument) {
        return captureCopy(argument);
    }

    private static <T> Collection<? extends T> captureCopy(Collection<T> argument) {
        if (argument instanceof List)
            return copyOf((List<T>)argument);
        else if (argument instanceof Set)
            return copyOf((Set<T>)argument);
        else {
            return new ImmutableList<>(Collections.unmodifiableList(new ArrayList<>(argument)));
        }
    }

    private static <T> List<? extends T> captureCopy(List<T> argument) {
        if (argument instanceof ImmutableList) {
            return argument;
        } else if (argument instanceof Collections2.ArrayListWrapper) {
            Collections2.ArrayListWrapper<T> wrapper = (Collections2.ArrayListWrapper<T>)argument;
            return new ImmutableList<>(wrapper.unmodifiable());
        } else {
            return new ImmutableList<>(Collections.unmodifiableList(new ArrayList<>(argument)));
        }
    }

    private static class ImmutableList<T> implements List<T> {
        private final List<T> list;
        private ImmutableList(List<T> list) {
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
            return list.subList(fromIndex, toIndex);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + Objects.hashCode(this.list);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ImmutableList<?> other = (ImmutableList<?>) obj;
            return Objects.equals(this.list, other.list);
        }

        @Override
        public String toString() {
            return "ImmutableList{" + "list=" + list + '}';
        }

    }

    private static class ImmutableSet<T> implements Set<T> {

        private final Set<T> set;
        private ImmutableSet(Set<T> set) {
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

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + Objects.hashCode(this.set);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ImmutableSet<?> other = (ImmutableSet<?>) obj;
            return Objects.equals(this.set, other.set);
        }

        @Override
        public String toString() {
            return "ImmutableSet{" + "set=" + set + '}';
        }

    }

    private Immutable() {
    }
}
