/*
 * Copyright (c) 2016, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
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

package com.github.sviperll.codemold.util;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.Nonnull;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class CMArrays {
    public static List<Byte> asList(byte... values) {
        List<Byte> result = CMCollections.newArrayList();
        for (byte value: values) {
            result.add(value);
        }
        return result;
    }
    public static List<Short> asList(short... values) {
        List<Short> result = CMCollections.newArrayList();
        for (short value: values) {
            result.add(value);
        }
        return result;
    }
    public static List<Integer> asList(int... values) {
        List<Integer> result = CMCollections.newArrayList();
        for (int value: values) {
            result.add(value);
        }
        return result;
    }
    public static List<Long> asList(long... values) {
        List<Long> result = CMCollections.newArrayList();
        for (long value: values) {
            result.add(value);
        }
        return result;
    }
    public static List<Float> asList(float... values) {
        List<Float> result = CMCollections.newArrayList();
        for (float value: values) {
            result.add(value);
        }
        return result;
    }
    public static List<Double> asList(double... values) {
        List<Double> result = CMCollections.newArrayList();
        for (double value: values) {
            result.add(value);
        }
        return result;
    }
    public static List<Character> asList(char... values) {
        List<Character> result = CMCollections.newArrayList();
        for (char value: values) {
            result.add(value);
        }
        return result;
    }
    public static List<Boolean> asList(boolean... values) {
        List<Boolean> result = CMCollections.newArrayList();
        for (boolean value: values) {
            result.add(value);
        }
        return result;
    }
    private CMArrays() {
    }
}
