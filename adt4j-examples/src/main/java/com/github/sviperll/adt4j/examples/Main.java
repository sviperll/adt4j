/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Main {
    public static void main(String[] args) {
        List<Integer> list1 = List.cons(1, List.cons(2, List.cons(3, List.<Integer>nil())));
        List<Integer> list2 = List.cons(4, List.cons(5, List.cons(6, List.<Integer>nil())));

        printList(list1.append(list2));
    }

    private static void printList(List<Integer> list) {
        list.accept(new ListVisitor<Integer, Void>() {
            @Override
            public Void cons(Integer head, List<Integer> tail) {
                System.out.println(head);
                printList(tail);
                return null;
            }

            @Override
            public Void nil() {
                return null;
            }
        });
    }
}
