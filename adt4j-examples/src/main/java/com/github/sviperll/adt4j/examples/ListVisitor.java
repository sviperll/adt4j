/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

import com.github.sviperll.adt4j.ValueVisitor;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@ValueVisitor(resultVariableName = "R",
              selfReferenceVariableName = "S",
              valueClassIsPublic = true)
public interface ListVisitor<T, S, R> {
    R cons(T head, S tail);
    R nil();
}
