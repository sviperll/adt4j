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
        List<Integer> list1 = Lists.cons(1, Lists.cons(2, Lists.cons(3, Lists.<Integer>nil())));
        List<Integer> list2 = Lists.cons(4, Lists.cons(5, Lists.cons(6, Lists.<Integer>nil())));
        list1.accept(new ListVisitor<Integer, List<Integer>, Void>() {
            @Override
            public Void cons(Integer head, List<Integer> tail) {
                System.out.println(head);
                tail.accept(this);
                return null;
            }

            @Override
            public Void nil() {
                return null;
            }
        });
    }
}
