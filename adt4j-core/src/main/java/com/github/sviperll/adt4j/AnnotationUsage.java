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
package com.github.sviperll.adt4j;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JAnnotationArrayMember;
import com.helger.jcodemodel.JAnnotationUse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class AnnotationUsage {
    private static final Map<JAnnotationUse, AnnotationUsage> bridge = new HashMap<JAnnotationUse, AnnotationUsage>();

    static AnnotationUsage getInstance(JAnnotationUse annotationUse) {
        AnnotationUsage result = bridge.get(annotationUse);
        if (result == null) {
            result = new AnnotationUsage(annotationUse);
            bridge.put(annotationUse, result);
        }
        return result;
    }

    private final JAnnotationUse annotationUse;
    private final Map<String, Object> values = new TreeMap<String, Object>();

    private AnnotationUsage(JAnnotationUse annotationUse) {
        this.annotationUse = annotationUse;
    }

    void param(String name, String value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }

    void param(String name, int value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }

    void param(String name, long value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }
    void param(String name, short value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }
    void param(String name, float value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }
    void param(String name, double value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }
    void param(String name, byte value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }
    void param(String name, char value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }

    void param(String name, Class<?> value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }

    void param(String name, Enum<?> value) {
        annotationUse.param(name, value);
        values.put(name, value);
    }

    private void genericParam(String name, Object value) {
        if (value instanceof String)
            param(name, (String)value);
        else if (value instanceof Integer)
            param(name, (Integer)value);
        else if (value instanceof Long)
            param(name, (Long)value);
        else if (value instanceof Short)
            param(name, (Short)value);
        else if (value instanceof Float)
            param(name, (Float)value);
        else if (value instanceof Double)
            param(name, (Double)value);
        else if (value instanceof Byte)
            param(name, (Byte)value);
        else if (value instanceof Character)
            param(name, (Character)value);
        else if (value instanceof Class)
            param(name, (Class)value);
        else if (value instanceof Enum)
            param(name, (Enum)value);
        else if (value instanceof String[])
            param(name, (String[])value);
        else if (value instanceof int[])
            param(name, (int[])value);
        else if (value instanceof long[])
            param(name, (long[])value);
        else if (value instanceof short[])
            param(name, (short[])value);
        else if (value instanceof float[])
            param(name, (float[])value);
        else if (value instanceof double[])
            param(name, (double[])value);
        else if (value instanceof byte[])
            param(name, (byte[])value);
        else if (value instanceof char[])
            param(name, (char[])value);
        else if (value instanceof Class[])
            param(name, (Class[])value);
        else if (value instanceof Enum[])
            param(name, (Enum[])value);
        else
            throw new IllegalArgumentException("Unsupported annotation param type " + value.getClass());
    }

    void param(String name, Annotation value) {
        AnnotationUsage usage = AnnotationUsage.getInstance(annotationUse.annotationParam(name, value.annotationType()));
        for (Method method: value.annotationType().getMethods()) {
            try {
                usage.genericParam(method.getName(), method.invoke(name));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
        values.put(name, value);
    }

    void param(String name, String[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }

    void param(String name, int[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }

    void param(String name, long[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }
    void param(String name, short[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }
    void param(String name, float[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }
    void param(String name, double[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }
    void param(String name, byte[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }
    void param(String name, char[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }

    void param(String name, Class<?>[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }

    void param(String name, Enum<?>[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            array.param(value[i]);
        }
        values.put(name, value);
    }

    void param(String name, Annotation[] value) {
        JAnnotationArrayMember array = annotationUse.paramArray(name);
        for (int i = 0; i < value.length; i++) {
            AnnotationUsage usage = AnnotationUsage.getInstance(array.annotate(value[i].annotationType()));
            for (Method method: value[i].annotationType().getMethods()) {
                try {
                    usage.genericParam(method.getName(), method.invoke(name));
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        values.put(name, value);
    }

    AbstractJClass getAnnotationClass() {
        return annotationUse.getAnnotationClass();
    }

    Object getParameterValue(String parameterName) {
        return values.get(parameterName);
    }
}
