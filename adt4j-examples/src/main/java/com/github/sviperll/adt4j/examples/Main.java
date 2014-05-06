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
        System.out.println("lookup(\"a\") -> " + toString(lookup("a")));
        System.out.println("lookup(\"b\") -> " + toString(lookup("b")));
        System.out.println("lookup(\"c\") -> " + toString(lookup("c")));
        System.out.println("lookup(\"d\") -> " + toString(lookup("d")));
        System.out.println("lookup2(\"a\") -> " + toString(lookup2("a")));
        System.out.println("lookup2(\"b\") -> " + toString(lookup2("b")));
        System.out.println("lookup2(\"c\") -> " + toString(lookup2("c")));
        System.out.println("lookup2(\"d\") -> " + toString(lookup2("d")));

        String a = "begin";
        Optional<String> oa = Optional.present(a + "ning");
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
        switch (name) {
            case "a":
                return Optional.present("b");
            case "b":
                return Optional.present("c");
            case "c":
                return Optional.present("d");
            default:
                return Optional.missing();
        }
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
}
