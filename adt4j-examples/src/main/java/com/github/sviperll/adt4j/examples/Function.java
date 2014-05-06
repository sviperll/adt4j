package com.github.sviperll.adt4j.examples;

/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public interface Function<T, U> {
    U apply(T argument);
}
