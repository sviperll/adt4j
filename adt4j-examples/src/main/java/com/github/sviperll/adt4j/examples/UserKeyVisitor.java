/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j.examples;

import com.github.sviperll.adt4j.DataVisitor;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@DataVisitor(result = "R")
public interface UserKeyVisitor<R> {
    R valueOf(int key);
}
