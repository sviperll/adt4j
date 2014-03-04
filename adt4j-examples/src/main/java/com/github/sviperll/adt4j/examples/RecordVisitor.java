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
public interface RecordVisitor<R> {
    R valueOf(boolean bool, byte b, char c, int i, long l, float f, double d, Object o, boolean[] boola, byte[] ba, char[] ca, int[] ia, long[] la, float[] fa, double[] da, Object[] oa);
}
