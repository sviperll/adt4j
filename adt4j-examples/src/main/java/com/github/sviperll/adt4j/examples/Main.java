/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
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
package com.github.sviperll.adt4j.examples;

import java.io.IOException;

public class Main {
    public static void main(final String[] args) throws IOException, ClassNotFoundException {
        Tree<String> tree = Tree.<String>node(List.<Tree<String>>cons(Tree.<String>leaf("aaa"), List.<Tree<String>>nil()));
        System.out.println(tree);
        final GADT<Integer> a = GADT.number(1);
        final GADT<Integer> b = GADT.number(5);
        final GADT<Integer> c = GADT.number(7);
        GADT<Function<Integer, Integer>> f = GADT.lambda(new Function<Integer, GADT<Integer>>() {

            @Override
            public GADT<Integer> apply(Integer argument) {
                return GADT.if_(GADT.isLessOrEqual(GADT.number(argument), a), b, c);
            }
        });
        GADT<Integer> test1 = GADT.apply(f, GADT.number(1));
        System.out.println(test1 + " = " + test1.eval());

        GADT<Integer> test2 = GADT.apply(f, GADT.number(3));
        System.out.println(test2 + " = " + test2.eval());

        System.out.println("lookup(\"a\") -> " + toString(lookup("a")));
        System.out.println("lookup(\"b\") -> " + toString(lookup("b")));
        System.out.println("lookup(\"c\") -> " + toString(lookup("c")));
        System.out.println("lookup(\"d\") -> " + toString(lookup("d")));
        System.out.println("lookup2(\"a\") -> " + toString(lookup2("a")));
        System.out.println("lookup2(\"b\") -> " + toString(lookup2("b")));
        System.out.println("lookup2(\"c\") -> " + toString(lookup2("c")));
        System.out.println("lookup2(\"d\") -> " + toString(lookup2("d")));
        System.out.println("lookup3(\"a\") -> " + lookup3("a").toString());
        System.out.println("lookup3(\"b\") -> " + lookup3("b").toString());
        System.out.println("lookup3(\"c\") -> " + lookup3("c").toString());
        System.out.println("lookup3(\"d\") -> " + lookup3("d").toString());

        final Function<String, Integer> lengthOfStringFunction = new Function<String, Integer>() {
            @Override
            public Integer apply(String argument) {
                return argument.length();
            }
        };
        System.out.println("length map lookup3(\"a\") -> " + lookup3("a").map(lengthOfStringFunction).toString());

        String aa = "begin";
        Optional<String> oa = Optional.present(aa + "ning");
        Optional<String> ob = Optional.present("beginning");
        Optional<String> oc = ob;
        Optional<String> od = Optional.present("beginning!");
        Optional<String> oe = Optional.missing();
        System.out.println();
        System.out.println("oa ---> " + toString(oa));
        System.out.println("ob ---> " + toString(ob));
        System.out.println("oc ---> " + toString(oc));
        System.out.println("od ---> " + toString(od));
        System.out.println("oe ---> " + toString(oe));
        System.out.println("System.identityHashCode(oa) ---> " + System.identityHashCode(oa));
        System.out.println("System.identityHashCode(ob) ---> " + System.identityHashCode(ob));
        System.out.println("System.identityHashCode(oc) ---> " + System.identityHashCode(oc));
        System.out.println("System.identityHashCode(od) ---> " + System.identityHashCode(od));
        System.out.println("System.identityHashCode(oe) ---> " + System.identityHashCode(oe));
        System.out.println();
        System.out.println("oa == ob ---> " + (oa == ob));
        System.out.println("oa.equals(ob) ---> " + oa.equals(ob));
        System.out.println();
        System.out.println("ob == oc ---> " + (ob == oc));
        System.out.println("ob.equals(oc) ---> " + ob.equals(oc));
        System.out.println();
        System.out.println("oa == od ---> " + (oa == od));
        System.out.println("oa.equals(od) ---> " + ob.equals(od));
        System.out.println();
        System.out.println("oa == oe ---> " + (oa == oe));
        System.out.println("oa.equals(oe) ---> " + ob.equals(oe));
        System.out.println(List.cons("aaa", List.cons("bbb", List.<String>nil())));
        List<Integer> list = List.cons(1, List.cons(2, List.cons(3, List.<Integer>nil())));
        List<Integer> tail = list;
        int sum = 0;
        while (!tail.isEmpty()) {
            sum += tail.head();
            tail = tail.tail();
        }
        System.out.println(list + " sum is " + sum);
    }

    public static String toString(Optional<String> optional) {
        return optional.accept(new OptionalVisitor<String, String, RuntimeException>() {
            @Override
            public String missing() throws RuntimeException {
                return "Optional.missing()";
            }

            @Override
            public String present(String value) throws RuntimeException {
                return "Optional.present(\"" + value + "\")";
            }
        });
    }

    public static Optional<String> lookup(String name) {
        if ("a".equals(name))
            return Optional.present("b");
        if ("b".equals(name))
            return Optional.present("c");
        if ("c".equals(name))
            return Optional.present("d");
        return Optional.missing();
    }
    public static Optional<String> lookup2(String name1) {
        // Using Java 8 syntax:
        //
        //     return lookup(name1).flatMap(Main::lookup)
        //
        return lookup(name1).flatMap(new Function<String, Optional<String>>() {
            @Override
            public Optional<String> apply(String name2) {
                return lookup(name2);
            }
        });
    }

    public static BaseOptional<String> baseLookup(String name) {
        if ("a".equals(name))
            return BaseOptional.present("b");
        if ("b".equals(name))
            return BaseOptional.present("c");
        if ("c".equals(name))
            return BaseOptional.present("d");
        return BaseOptional.missing();
    }

    public static BaseOptional<String> lookup3(String name1) {
        // Using Java 8 syntax:
        //
        //     return lookup3(name1).flatMap(Main::lookup)
        //
        return baseLookup(name1).flatMap(new Function<String, BaseOptional<String>>() {
            @Override
            public BaseOptional<String> apply(String name2) {
                return baseLookup(name2);
            }
        });
    }
}
