/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

import com.github.sviperll.adt4j.DataVisitor;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@DataVisitor(
        result = "R",
        self = "S"
        )
public interface ListLikeVisitor<T, S, R> {
    R cons(T head, S tail);
    R nil();
}
