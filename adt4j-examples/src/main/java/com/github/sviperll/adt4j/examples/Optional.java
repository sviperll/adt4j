/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Optional<T> extends OptionalValue<T> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private final static Optional MISSING = new Optional(OptionalValue.missing());

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> Optional<T> missing() {
        return MISSING;
    }

    public static <T> Optional<T> present(T value) {
        return new Optional<>(OptionalValue.present(value));
    }

    private Optional(OptionalValue<T> value) {
        super(value);
    }

    //
    // equals and hashCode are correctly inherited from OptionalValue
    //

    public <U> Optional<U> flatMap(final Function<T, Optional<U>> function) {
        return accept(new OptionalVisitor<T, Optional<U>, RuntimeException>() {
            @Override
            public Optional<U> missing() {
                return Optional.missing();
            }

            @Override
            public Optional<U> present(T value) {
                return function.apply(value);
            }
        });
    }
}
