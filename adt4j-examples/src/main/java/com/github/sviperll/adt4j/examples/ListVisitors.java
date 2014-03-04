/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class ListVisitors {
    public static <T> ListVisitor<T, Integer, Integer> lengthRecursive() {
        return new ListVisitor<T, Integer, Integer>() {
            @Override
            public Integer cons(T head, Integer tail) {
                return 1 + tail;
            }

            @Override
            public Integer nil() {
                return 0;
            }
        };
    }

    public static <T> ListVisitor<T, ?, T> head() {
        return new ListVisitor<T, Object, T>() {
            @Override
            public T cons(T head, Object tail) {
                return head;
            }

            @Override
            public T nil() {
                throw new UnsupportedOperationException("Empty List");
            }
        };
    }

    public static <L, T> ListVisitor<T, L, L> tail() {
        return new ListVisitor<T, L, L>() {
            @Override
            public L cons(T head, L tail) {
                return tail;
            }

            @Override
            public L nil() {
                throw new UnsupportedOperationException("Empty List");
            }
        };
    }

    public static <L, T> ListVisitor<T, L, L> appendRecursive(final L list, final ListVisitor<T, L, L> factory) {
        return new ListVisitor<T, L, L>() {
            @Override
            public L cons(T head, L tail) {
                return factory.cons(head, tail);
            }

            @Override
            public L nil() {
                return list;
            }
        };
    }
}
