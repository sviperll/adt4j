/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class List<T> implements ListLike<T> {
    private static final List NIL = new List(ListLikes.nil());
    public static <T> List<T> nil() {
        return (List<T>)NIL;
    }

    public static <T> List<T> cons(T head, List<T> tail) {
        return new List<>(ListLikes.cons(head, tail));
    }

    public static <T> List<T> of(ListLike<T> list) {
        if (list instanceof List)
            return (List<T>)list;
        else
            return new List<>(list);
    }

    private final ListLike<T> list;
    private List(ListLike<T> list) {
        this.list = list;
    }

    @Override
    public <R> R accept(ListLikeVisitor<T, ListLike<T>, R> visitor) {
        return list.accept(visitor);
    }

    public <R> R accept(final ListVisitor<T, R> visitor) {
        return list.accept(new ListLikeVisitor<T, ListLike<T>, R>() {
            @Override
            public R cons(T head, ListLike<T> tail) {
                return visitor.cons(head, of(tail));
            }

            @Override
            public R nil() {
                return visitor.nil();
            }
        });
    }

    public List<T> append(final List<T> list2) {
        return this.accept(new ListVisitor<T, List<T>>() {
            @Override
            public List<T> cons(T head, List<T> tail) {
                return List.cons(head, tail.append(list2));
            }

            @Override
            public List<T> nil() {
                return list2;
            }
        });
    }

    public boolean isEmpty() {
        return this.accept(new ListVisitor<T, Boolean>() {
            @Override
            public Boolean cons(T head, List<T> tail) {
                return false;
            }

            @Override
            public Boolean nil() {
                return true;
            }
        });
    }

    public int length() {
        return this.accept(new ListVisitor<T, Integer>() {
            @Override
            public Integer cons(T head, List<T> tail) {
                return 1 + tail.accept(this);
            }

            @Override
            public Integer nil() {
                return 0;
            }
        });
    }

    public T head() {
        return this.accept(new ListVisitor<T, T>() {
            @Override
            public T cons(T head, List<T> tail) {
                return head;
            }

            @Override
            public T nil() {
                throw new NoSuchElementException();
            }
        });
    }

    public List<T> tail() {
        return this.accept(new ListVisitor<T, List<T>>() {
            @Override
            public List<T> cons(T head, List<T> tail) {
                return tail;
            }

            @Override
            public List<T> nil() {
                throw new NoSuchElementException();
            }
        });
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.list);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final List<T> other = (List<T>)obj;
        if (!Objects.equals(this.list, other.list))
            return false;
        return true;
    }

}
