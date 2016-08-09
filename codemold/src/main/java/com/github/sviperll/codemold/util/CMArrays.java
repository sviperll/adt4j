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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class CMArrays {
    public static List<Byte> asList(byte... values) {
        return new ByteArrayAsList(values);
    }
    public static List<Short> asList(short... values) {
        return new ShortArrayAsList(values);
    }
    public static List<Integer> asList(int... values) {
        return new IntArrayAsList(values);
    }
    public static List<Long> asList(long... values) {
        return new LongArrayAsList(values);
    }
    public static List<Float> asList(float... values) {
        return new FloatArrayAsList(values);
    }
    public static List<Double> asList(double... values) {
        return new DoubleArrayAsList(values);
    }
    public static List<Character> asList(char... values) {
        return new CharArrayAsList(values);
    }
    public static List<Boolean> asList(boolean... values) {
        return new BooleanArrayAsList(values);
    }
    private CMArrays() {
    }

    private static class ByteArrayAsList extends AbstractList<Byte>
            implements RandomAccess {

        private final byte[] values;
        ByteArrayAsList(byte[] values) {
            this.values = values;
        }

        @Override
        public Byte get(int index) {
            return values[index];
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public Byte set(int index, Byte newValue) {
            Byte oldValue = values[index];
            values[index] = newValue;
            return oldValue;
        }

        @Override
        public int indexOf(Object o) {
            if (o == null || !(o instanceof Byte))
                return -1;
            else {
                byte v = (Byte)o;
                for (int i = 0; i < values.length; i++)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }

        @Override
        public int lastIndexOf(Object o) {
            if (o == null || !(o instanceof Byte))
                return -1;
            else {
                byte v = (Byte)o;
                for (int i = values.length - 1; i >= 0; i--)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }
    }

    private static class ShortArrayAsList extends AbstractList<Short>
            implements RandomAccess {

        private final short[] values;
        ShortArrayAsList(short[] values) {
            this.values = values;
        }

        @Override
        public Short get(int index) {
            return values[index];
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public Short set(int index, Short newValue) {
            Short oldValue = values[index];
            values[index] = newValue;
            return oldValue;
        }

        @Override
        public int indexOf(Object o) {
            if (o == null || !(o instanceof Short))
                return -1;
            else {
                short v = (Short)o;
                for (int i = 0; i < values.length; i++)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }

        @Override
        public int lastIndexOf(Object o) {
            if (o == null || !(o instanceof Short))
                return -1;
            else {
                short v = (Short)o;
                for (int i = values.length - 1; i >= 0; i--)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }
    }

    private static class IntArrayAsList extends AbstractList<Integer>
            implements RandomAccess {

        private final int[] values;
        IntArrayAsList(int[] values) {
            this.values = values;
        }

        @Override
        public Integer get(int index) {
            return values[index];
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public Integer set(int index, Integer newValue) {
            Integer oldValue = values[index];
            values[index] = newValue;
            return oldValue;
        }

        @Override
        public int indexOf(Object o) {
            if (o == null || !(o instanceof Integer))
                return -1;
            else {
                int v = (Integer)o;
                for (int i = 0; i < values.length; i++)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }

        @Override
        public int lastIndexOf(Object o) {
            if (o == null || !(o instanceof Integer))
                return -1;
            else {
                int v = (Integer)o;
                for (int i = values.length - 1; i >= 0; i--)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }
    }

    private static class LongArrayAsList extends AbstractList<Long>
            implements RandomAccess {

        private final long[] values;
        LongArrayAsList(long[] values) {
            this.values = values;
        }

        @Override
        public Long get(int index) {
            return values[index];
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public Long set(int index, Long newValue) {
            Long oldValue = values[index];
            values[index] = newValue;
            return oldValue;
        }

        @Override
        public int indexOf(Object o) {
            if (o == null || !(o instanceof Long))
                return -1;
            else {
                long v = (Long)o;
                for (int i = 0; i < values.length; i++)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }

        @Override
        public int lastIndexOf(Object o) {
            if (o == null || !(o instanceof Long))
                return -1;
            else {
                long v = (Long)o;
                for (int i = values.length - 1; i >= 0; i--)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }
    }


    private static class FloatArrayAsList extends AbstractList<Float>
            implements RandomAccess {

        private final float[] values;
        FloatArrayAsList(float[] values) {
            this.values = values;
        }

        @Override
        public Float get(int index) {
            return values[index];
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public Float set(int index, Float newValue) {
            Float oldValue = values[index];
            values[index] = newValue;
            return oldValue;
        }

        @SuppressFBWarnings(
            value="FE_FLOATING_POINT_EQUALITY",
            justification="Required by indexOf sematics")
        @Override
        public int indexOf(Object o) {
            if (o == null || !(o instanceof Float))
                return -1;
            else {
                float v = (Float)o;
                for (int i = 0; i < values.length; i++)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }

        @SuppressFBWarnings(
            value="FE_FLOATING_POINT_EQUALITY",
            justification="Required by indexOf sematics")
        @Override
        public int lastIndexOf(Object o) {
            if (o == null || !(o instanceof Float))
                return -1;
            else {
                float v = (Float)o;
                for (int i = values.length - 1; i >= 0; i--)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }
    }

    private static class DoubleArrayAsList extends AbstractList<Double>
            implements RandomAccess {

        private final double[] values;
        DoubleArrayAsList(double[] values) {
            this.values = values;
        }

        @Override
        public Double get(int index) {
            return values[index];
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public Double set(int index, Double newValue) {
            Double oldValue = values[index];
            values[index] = newValue;
            return oldValue;
        }

        @SuppressFBWarnings(
            value="FE_FLOATING_POINT_EQUALITY",
            justification="Required by indexOf sematics")
        @Override
        public int indexOf(Object o) {
            if (o == null || !(o instanceof Double))
                return -1;
            else {
                double v = (Double)o;
                for (int i = 0; i < values.length; i++)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }

        @SuppressFBWarnings(
            value="FE_FLOATING_POINT_EQUALITY",
            justification="Required by indexOf sematics")
        @Override
        public int lastIndexOf(Object o) {
            if (o == null || !(o instanceof Double))
                return -1;
            else {
                double v = (Double)o;
                for (int i = values.length - 1; i >= 0; i--)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }
    }

    private static class CharArrayAsList extends AbstractList<Character>
            implements RandomAccess {

        private final char[] values;
        CharArrayAsList(char[] values) {
            this.values = values;
        }

        @Override
        public Character get(int index) {
            return values[index];
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public Character set(int index, Character newValue) {
            Character oldValue = values[index];
            values[index] = newValue;
            return oldValue;
        }

        @Override
        public int indexOf(Object o) {
            if (o == null || !(o instanceof Character))
                return -1;
            else {
                char v = (Character)o;
                for (int i = 0; i < values.length; i++)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }

        @Override
        public int lastIndexOf(Object o) {
            if (o == null || !(o instanceof Character))
                return -1;
            else {
                char v = (Character)o;
                for (int i = values.length - 1; i >= 0; i--)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }
    }

    private static class BooleanArrayAsList extends AbstractList<Boolean>
            implements RandomAccess {

        private final boolean[] values;
        BooleanArrayAsList(boolean[] values) {
            this.values = values;
        }

        @Override
        public Boolean get(int index) {
            return values[index];
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        @Override
        public Boolean set(int index, Boolean newValue) {
            Boolean oldValue = values[index];
            values[index] = newValue;
            return oldValue;
        }

        @Override
        public int indexOf(Object o) {
            if (o == null || !(o instanceof Boolean))
                return -1;
            else {
                boolean v = (Boolean)o;
                for (int i = 0; i < values.length; i++)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }

        @Override
        public int lastIndexOf(Object o) {
            if (o == null || !(o instanceof Boolean))
                return -1;
            else {
                boolean v = (Boolean)o;
                for (int i = values.length - 1; i >= 0; i--)
                    if (v == values[i])
                        return i;
                return -1;
            }
        }
    }
}
