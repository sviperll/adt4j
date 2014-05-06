/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

import com.github.sviperll.adt4j.DataVisitor;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@DataVisitor(result = "R",
             exception = "E",
             className = "OptionalValue")
public interface OptionalVisitor<T, R, E extends Exception> {
    R missing() throws E;
    R present(T value) throws E;
}
