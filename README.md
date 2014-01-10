adt4j - Algebraic Data Types for Java
=====================================

This library implements Algebraic Data Types for Java.

Just add `adt4j-core` maven dependency to your project and you are ready to go.

Examples
--------

### Optional A. K. A. Maybe type ###

 1. Define an interface that will describe your Algebraic Data Type like this:

        interface OptionalVisitor<R, T> {
            R present(T value);
            R missing();
        }

    You must define a variation of visitor interface (see Visitor-pattern).
    This interface is a discription of your data-type.
    Each method represents one possible case or pattern.
    Arguments of methods represent data stored in your data-type.

    There are two cases in an example above:

     1. `present` - when optional data of type `T` is present.
     2. `missing` - when no data is present.

    All methods in your interface are to return the same type declared as type-variable
    (`R` in the example above).
    Methods can have any number of any arguments.
    Additional type variables are allowed.
    Methods should not throw any checked exceptions.

 2. Add a `@DataVisitor` annotation and specify special type-variable names in arguments to annotation

        @DataVisitor(result = "R")
        interface OptionalVisitor<R, T> {
            R present(T value);
            R missing();
        }

    Here we declare that type-variable `R` is used as a result-type of all interface methods.

 3. We are ready to go.

    New class `Optional` will be automatically generated when you compile your project.

    You can create instances of this class like this:

        Optional<String> optional1 = Optional.present("Test");
        Optional<String> optional2 = Optional.missing();

    You can use visitors as pattern-matching construct:

        OptionalVisitor<Void, String> printVisitor = new OptionalVisitor<>() {
           public Void present(String value) {
               System.out.println("present: " + value);
               return null;
           }
           public Void missing() {
               System.out.println("missing");
               return null;
           }
        }

        System.out.println("optional1:");
        optional1.accept(printVisitor);
        System.out.println("optional2:");
        optional2.accept(printVisitor);

    The result should be like this:

        optional1:
        present: Test
        optional2:
        missing

    You can use lots of combintators with optional types.

    If you have a lookup method:

        Optional<String> lookup(String key) {
            ...
        }

    And you want to define lookup2 method that will look given key up and then use the result of first lookup
    as a key for next one. You can do this with `flatMap` method.
    If you use Java 8 you can use lambda-expression:

        Optional<String> lookup2(String key) {
            return flatMap(lookup(key), (value) -> lookup(value));
        }

    With Java before 8 you can still do it with anonymous class:

        Optional<String> lookup2(String key) {
            return flatMap(lookup(key), new Function<String, Optional<String>>() {
                public String apply(String value) {
                    return lookup(value);
                }
            });
        }

    flatMap-method is not hard to define:

        <T, U> Optional<U> flatMap(final Optional<T> argument, final Function<T, Optional<U>> function) {
            return argument.accept(new OptionalVisitor<Optional<U>, T>() {
                public Optional<U> present(T value) {
                    return function.apply(value);
                }
                public Optional<U> missing() {
                    return Optional.missing();
                }
            });
        }

    See adt4j-examples project for more complete examples.

