/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.adt4j.examples;

import com.github.sviperll.adt4j.GenerateValueClassForVisitor;
import com.github.sviperll.adt4j.Getter;
import com.github.sviperll.adt4j.Updater;
import com.github.sviperll.meta.Visitor;
import javax.annotation.Nullable;

@GenerateValueClassForVisitor(hashCodeBase = 49)
@Visitor(resultVariableName = "R")
@ClassList(classes = Function.class)
public interface RecordVisitor<R> {
    R valueOf(@Getter(name = "getBool") @Updater(name = "withBool") boolean bool,
              @Getter(name = "getB") @Updater(name = "withB") byte b,
              @Getter(name = "getC") @Updater(name = "withC") char c,
              @Getter(name = "getI") @Updater(name = "withI") int i,
              @Getter(name = "getL") @Updater(name = "withL") long l,
              @Getter(name = "getF") @Updater(name = "withF") float f,
              @Getter(name = "getD") @Updater(name = "withD") double d,
              @Getter(name = "getO") @Updater(name = "withO") Object result,
              @Getter(name = "getBoola") @Updater(name = "withBoola") @Nullable boolean[] boola,
              @Getter(name = "getBa") @Updater(name = "withBa") byte[][] ba,
              @Getter(name = "getCa") @Updater(name = "withCa") char[] ca,
              @Getter(name = "getIa") @Updater(name = "withIa") int[] ia,
              @Getter(name = "getLa") @Updater(name = "withLa") long[] la,
              @Getter(name = "getFa") @Updater(name = "withFa") float[] fa,
              @Getter(name = "getDa") @Updater(name = "withDa") double[] da,
              @Getter(name = "getOa") @Updater(name = "withOa") @Nullable Object[] newValue
              );
}
