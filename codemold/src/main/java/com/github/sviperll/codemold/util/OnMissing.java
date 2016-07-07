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

import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class OnMissing {
    @SuppressWarnings("rawtypes")
    private static final ReturnNullOptionality RETURN_NULL_OPTIONALITY = new ReturnNullOptionality();

    @SuppressWarnings("rawtypes")
    private static final ThrowNullPointerExceptionOptionality THROW_NULL_POINTER_EXCEPTION_OPTIONALITY = new ThrowNullPointerExceptionOptionality();

    @SuppressWarnings("unchecked")
    public static <T> Optionality<T, T> returnNull() {
        return RETURN_NULL_OPTIONALITY;
    }

    @SuppressWarnings("unchecked")
    public static <T> Optionality<T, T> throwNullPointerException() {
        return THROW_NULL_POINTER_EXCEPTION_OPTIONALITY;
    }

    public static <T> Optionality<T, T> throwUnsupportedOperationException(final String message) {
        return new ThrowUnsupportedOperationExceptionOptionality<>(message);
    }

    public static <T> Optionality<T, T> throwIllegalStateException(final String message) {
        return new ThrowIllegalStateExceptionOptionality<>(message);
    }

    public static <T> Optionality<T, T> returnDefault(final T defaultValue) {
        return new ReturnDefaultOptionality<>(defaultValue);
    }

    private OnMissing() {
    }

    private static class ReturnNullOptionality<T> implements Optionality<T, T> {
        @Override
        public T present(T value) {
            return value;
        }

        @Override
        public T missing() {
            return null;
        }
    }

    private static class ThrowNullPointerExceptionOptionality<T> implements Optionality<T, T> {
        @Override
        public T present(T value) {
            return value;
        }

        @Override
        public T missing() {
            throw new NullPointerException();
        }
    }

    private static class ThrowUnsupportedOperationExceptionOptionality<T> implements Optionality<T, T> {

        private final String message;

        ThrowUnsupportedOperationExceptionOptionality(String message) {
            this.message = message;
        }

        @Override
        public T present(T value) {
            return value;
        }

        @Override
        public T missing() {
            throw new UnsupportedOperationException(message);
        }
    }

    private static class ThrowIllegalStateExceptionOptionality<T> implements Optionality<T, T> {

        private final String message;

        ThrowIllegalStateExceptionOptionality(String message) {
            this.message = message;
        }

        @Override
        public T present(T value) {
            return value;
        }

        @Override
        public T missing() {
            throw new IllegalStateException(message);
        }
    }

    private static class ReturnDefaultOptionality<T> implements Optionality<T, T> {

        private final T defaultValue;

        ReturnDefaultOptionality(T defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public T present(T value) {
            return value;
        }

        @Override
        public T missing() {
            return defaultValue;
        }
    }
}
