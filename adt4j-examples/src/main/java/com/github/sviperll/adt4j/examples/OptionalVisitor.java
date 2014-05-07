/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

import com.github.sviperll.adt4j.ValueVisitor;
import javax.annotation.Nonnull;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@ValueVisitor(resultVariableName = "R",
              exceptionVariableName = "E",
              valueClassName = "OptionalValue")
public interface OptionalVisitor<T, R, E extends Exception> {
    R missing() throws E;
    R present(@Nonnull T value) throws E;
}
