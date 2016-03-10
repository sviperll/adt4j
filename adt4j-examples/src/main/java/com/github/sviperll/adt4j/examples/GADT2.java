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

package com.github.sviperll.adt4j.examples;

import com.github.sviperll.adt4j.GenerateValueClassForVisitor;
import com.github.sviperll.adt4j.Visitor;
import com.github.sviperll.adt4j.WrapsGeneratedValueClass;
import com.github.sviperll.adt4j.examples.GADT2.GADT2Visitor;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
@WrapsGeneratedValueClass(visitor = GADT2Visitor.class)
public class GADT2<T> extends GADT2Base<T> {
    // public static <A, B> GADT2<Function<A, B>> lambda(Function<A, GADT2<B>> function) {
    //     return GADT2Base.<Function<A, B>, A, B>lambda(function, Type.Equality.<Function<A, B>>obvious());
    // }
    public static GADT2<Integer> number(int n) {
        return GADT2Base.<Integer>number(n, TypeEquality.<Integer>obvious());
    }
    public static GADT2<Integer> plus(GADT2<Integer> a, GADT2<Integer> b) {
        return GADT2Base.<Integer>plus(a, b, TypeEquality.<Integer>obvious());
    }
    public static GADT2<Boolean> isLessOrEqual(GADT2<Integer> a, GADT2<Integer> b) {
        return GADT2Base.<Boolean>isLessOrEqual(a, b, TypeEquality.<Boolean>obvious());
    }

    GADT2(GADT2Base<T> base) {
        super(base);
    }

    T eval() {
        return accept(new GADT2Visitor<T, T>() {
            // @Override
            // public <A, B> T lambda(final Function<A, GADT2<B>> function, Type.Equality<T, Function<A, B>> evidence) {
            //    return evidence.cast(new Function<A, B>() {
            //        @Override
            //        public B apply(A argument) {
            //            return function.apply(argument).eval();
            //        }
            //    });
            // }

            // @Override
            // public <U> T apply(GADT2<Function<U, T>> function, GADT2<U> argument) {
            //     return function.eval().apply(argument.eval());
            // }

            @Override
            public T number(int n, TypeEquality<T, Integer> evidence) {
                return evidence.cast(n);
            }

            @Override
            public T plus(GADT2<Integer> a, GADT2<Integer> b, TypeEquality<T, Integer> evidence) {
                return evidence.cast(a.eval() + b.eval());
            }

            @Override
            public T isLessOrEqual(GADT2<Integer> a, GADT2<Integer> b, TypeEquality<T, Boolean> evidence) {
                return evidence.cast(a.eval() <= b.eval());
            }

            @Override
            public T if_(GADT2<Boolean> condition, GADT2<T> trueValue, GADT2<T> falseValue) {
                return condition.eval() ? trueValue.eval() : falseValue.eval();
            }
        });
    }

    GADT2<T> cloneGADT2() {
        return accept(new GADT2Visitor<T, GADT2<T>>() {
            // @Override
            // public <A, B> GADT2<T> lambda(Function<A, GADT2<B>> function, Type.Equality<T, Function<A, B>> evidence) {
            //     return evidence.<GADT2.TypeConstructor, GADT2<?>, GADT2<T>, GADT2<Function<A, B>>>toTypeConstructorApplication().cast(GADT2.<A, B>lambda(function));
            // }

            // @Override
            // public <U> GADT2<T> apply(GADT2<Function<U, T>> function, GADT2<U> argument) {
            //     return GADT2.apply(function, argument);
            // }

            @Override
            public GADT2<T> number(int n, TypeEquality<T, Integer> evidence) {
                return evidence.toGADT2().cast(GADT2.number(n));
            }

            @Override
            public GADT2<T> plus(GADT2<Integer> a, GADT2<Integer> b, TypeEquality<T, Integer> evidence) {
                return evidence.toGADT2().cast(GADT2.plus(a, b));
            }

            @Override
            public GADT2<T> isLessOrEqual(GADT2<Integer> a, GADT2<Integer> b, TypeEquality<T, Boolean> evidence) {
                return evidence.toGADT2().cast(GADT2.isLessOrEqual(a, b));
            }

            @Override
            public GADT2<T> if_(GADT2<Boolean> condition, GADT2<T> trueValue, GADT2<T> falseValue) {
                return GADT2.if_(condition, trueValue, falseValue);
            }
        });
    }

    @GenerateValueClassForVisitor(wrapperClass = GADT2.class)
    @Visitor(resultVariableName = "R")
    public interface GADT2Visitor<T, R> {
        // <A, B> R lambda(Function<A, GADT2<B>> function, Type.Equality<T, Function<A, B>> evidence);
        // <U> R apply(GADT2<Function<U, T>> function, GADT2<U> argument);
        R number(int n, TypeEquality<T, Integer> constr);
        R plus(GADT2<Integer> a, GADT2<Integer> b, TypeEquality<T, Integer> evidence);
        R isLessOrEqual(GADT2<Integer> a, GADT2<Integer> b, TypeEquality<T, Boolean> evidence);
        R if_(GADT2<Boolean> condition, GADT2<T> trueValue, GADT2<T> falseValue);
    }

    public static class TypeEquality<T, U> {
        @SuppressWarnings("rawtypes")
        private static final TypeEquality INSTANCE = new TypeEquality();

        @SuppressWarnings("unchecked")
        public static <T> TypeEquality<T, T> obvious() {
            return INSTANCE;
        }

        private TypeEquality() {
        }

        @SuppressWarnings("unchecked")
        public T cast(U u) {
            return (T)u;
        }

        @SuppressWarnings("unchecked")
        public TypeEquality<U, T> reverse() {
            return INSTANCE;
        }

        @SuppressWarnings("unchecked")
        public <V> TypeEquality<T, V> merge(TypeEquality<U, V> equality) {
            return INSTANCE;
        }

        @SuppressWarnings("unchecked")
        public TypeEquality<GADT2<T>, GADT2<U>> toGADT2() {
            return INSTANCE;
        }
    }
}
