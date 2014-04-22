/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Lists {
    public static <T> int length(List<T> list) {
        return list.acceptRecursive(ListVisitors.<T>lengthRecursive());
    }

    public static <T> List<T> append(List<T> list1, List<T> list2) {
        return list1.acceptRecursive(ListVisitors.appendRecursive(list2, List.<T>factory()));
    }

    private Lists() {
    }
}
