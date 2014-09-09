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
import javax.annotation.Nullable;

@GenerateValueClassForVisitor(resultVariableName = "R",
                              valueClassHashCodeBase = 49)
public interface RecordVisitor<R> {
    R valueOf(@Getter("getBool") @Updater("withBool") boolean bool,
              @Getter("getB") @Updater("withB") byte b,
              @Getter("getC") @Updater("withC") char c,
              @Getter("getI") @Updater("withI") int i,
              @Getter("getL") @Updater("withL") long l,
              @Getter("getF") @Updater("withF") float f,
              @Getter("getD") @Updater("withD") double d,
              @Getter("getO") @Updater("withO") Object result,
              @Getter("getBoola") @Updater("withBoola") @Nullable boolean[] boola,
              @Getter("getBa") @Updater("withBa") byte[][] ba,
              @Getter("getCa") @Updater("withCa") char[] ca,
              @Getter("getIa") @Updater("withIa") int[] ia,
              @Getter("getLa") @Updater("withLa") long[] la,
              @Getter("getFa") @Updater("withFa") float[] fa,
              @Getter("getDa") @Updater("withDa") double[] da,
              @Getter("getOa") @Updater("withOa") @Nullable Object[] newValue
              );
}
