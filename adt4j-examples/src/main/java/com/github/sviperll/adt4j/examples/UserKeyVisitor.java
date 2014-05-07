/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

import com.github.sviperll.adt4j.ValueVisitor;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@ValueVisitor(resultVariableName = "R")
public interface UserKeyVisitor<R> {
    R valueOf(int key);
}
