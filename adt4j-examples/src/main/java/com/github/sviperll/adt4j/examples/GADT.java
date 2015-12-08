/*
 * Copyright (c) 2015, Victor Nazarov <asviraspossible@gmail.com>
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

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class GADT<T> {
    public static <A, B> GADT<Function<A, B>> lambda(Function<A, GADT<B>> function) {
        return new GADT<Function<A, B>>(Wrapper.<A, B>wrapLambda(function));
    }

    public static <A, B> GADT<B> apply(GADT<Function<A, B>> function, GADT<A> argument) {
        return new GADT<B>(Wrapper.<A, B>wrapApply(function, argument));
    }

    public static GADT<Integer> number(int n) {
        return new GADT<Integer>(Wrapper.wrapNumber(n));
    }

    public static GADT<Integer> plus(GADT<Integer> a, GADT<Integer> b) {
        return new GADT<Integer>(Wrapper.wrapPlus(a, b));
    }

    public static GADT<Boolean> isLessOrEqual(GADT<Integer> a, GADT<Integer> b) {
        return new GADT<Boolean>(Wrapper.wrapIsLessOrEqual(a, b));
    }

    public static <T> GADT<T> if_(GADT<Boolean> condition, GADT<T> a, GADT<T> b) {
        return new GADT<T>(Wrapper.<T>wrapIf(condition, a, b));
    }

    private final Wrapper<?, ?, T> wrapper;
    private GADT(Wrapper<?, ?, T> wrapper) {
        this.wrapper = wrapper;
    }

    public T eval() {
        return wrapper.eval();
    }

    @Override
    public String toString() {
        return wrapper.toString();
    }

    @Override
    public int hashCode() {
        return wrapper.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GADT))
            return false;
        else {
            GADT<?> that = (GADT<?>)object;
            return this.wrapper.equals(that.wrapper);
        }
    }

    interface Visitor<A, B, T, R> extends GADTBaseVisitor<A, GADT<Function<A, B>>, GADT<A>, GADT<B>, GADT<Integer>, GADT<Boolean>, Function<Function<A, B>, T>, Function<B, T>, Function<Integer, T>, Function<Boolean, T>, GADT<T>, R> {
    }

    private static class Wrapper<A, B, T> extends GADTBase<A, GADT<Function<A, B>>, GADT<A>, GADT<B>, GADT<Integer>, GADT<Boolean>, Function<Function<A, B>, T>, Function<B, T>, Function<Integer, T>, Function<Boolean, T>, GADT<T>> {
        static <T> Function<T, T> id() {
            return new Function<T, T>() {
                @Override
                public T apply(T argument) {
                    return argument;
                }
            };
        }
        static <A, B> Wrapper<?, ?, Function<A, B>> wrapLambda(Function<A, GADT<B>> function) {
            Function<Function<A, B>, Function<A, B>> id = id();
            return new Wrapper<A, B, Function<A, B>>(GADTBase.<A, GADT<Function<A, B>>, GADT<A>, GADT<B>, GADT<Integer>, GADT<Boolean>, Function<Function<A, B>, Function<A, B>>, Function<B, Function<A, B>>, Function<Integer, Function<A, B>>, Function<Boolean, Function<A, B>>, GADT<Function<A, B>>>lambda(function, id));
        }

        static <A, B> Wrapper<?, ?, B> wrapApply(GADT<Function<A, B>> function, GADT<A> argument) {
            Function<B, B> id = id();
            return new Wrapper<A, B, B>(GADTBase.<A, GADT<Function<A, B>>, GADT<A>, GADT<B>, GADT<Integer>, GADT<Boolean>, Function<Function<A, B>, B>, Function<B, B>, Function<Integer, B>, Function<Boolean, B>, GADT<B>>apply(function, argument, id));
        }

        static Wrapper<?, ?, Integer> wrapNumber(int n) {
            Function<Integer, Integer> id = id();
            return new Wrapper<Object, Object, Integer>(GADTBase.<Object, GADT<Function<Object, Object>>, GADT<Object>, GADT<Object>, GADT<Integer>, GADT<Boolean>, Function<Function<Object, Object>, Integer>, Function<Object, Integer>, Function<Integer, Integer>, Function<Boolean, Integer>, GADT<Integer>>number(n, id));
        }

        static Wrapper<?, ?, Integer> wrapPlus(GADT<Integer> a, GADT<Integer> b) {
            Function<Integer, Integer> id = id();
            return new Wrapper<Object, Object, Integer>(GADTBase.<Object, GADT<Function<Object, Object>>, GADT<Object>, GADT<Object>, GADT<Integer>, GADT<Boolean>, Function<Function<Object, Object>, Integer>, Function<Object, Integer>, Function<Integer, Integer>, Function<Boolean, Integer>, GADT<Integer>>plus(a, b, id));
        }

        static Wrapper<?, ?, Boolean> wrapIsLessOrEqual(GADT<Integer> a, GADT<Integer> b) {
            Function<Boolean, Boolean> id = id();
            return new Wrapper<Object, Object, Boolean>(GADTBase.<Object, GADT<Function<Object, Object>>, GADT< Object>, GADT<Object>, GADT<Integer>, GADT<Boolean>, Function<Function<Object, Object>, Boolean>, Function<Object, Boolean>, Function<Integer, Boolean>, Function<Boolean, Boolean>, GADT<Boolean>>isLessOrEqual(a, b, id));
        }

        static <T> Wrapper<?, ?, T> wrapIf(GADT<Boolean> condition, GADT<T> iftrue, GADT<T> iffalse) {
            return new Wrapper<Object, Object, T>(GADTBase.<Object, GADT<Function<Object, Object>>, GADT<Object>, GADT<Object>, GADT<Integer>, GADT<Boolean>, Function<Function<Object, Object>, T>, Function<Object, T>, Function<Integer, T>, Function<Boolean, T>, GADT<T>>if_(condition, iftrue, iffalse));
        }

        private Wrapper(GADTBase<A, GADT<Function<A, B>>, GADT<A>, GADT<B>, GADT<Integer>, GADT<Boolean>, Function<Function<A, B>, T>, Function<B, T>, Function<Integer, T>, Function<Boolean, T>, GADT<T>> base) {
            super(base);
        }

        public <R> R accept(Visitor<A, B, T, R> visitor) {
            return super.accept(visitor);
        }

        T eval() {
            return accept(new Visitor<A, B, T, T>() {
                @Override
                public T lambda(final Function<A, GADT<B>> function, Function<Function<A, B>, T> cast) {
                    return cast.apply(new Function<A, B>() {
                        @Override
                        public B apply(A argument) {
                            return function.apply(argument).eval();
                        }
                    });
                }

                @Override
                public T apply(GADT<Function<A, B>> function, GADT<A> argument, Function<B, T> cast) {
                    return cast.apply(function.eval().apply(argument.eval()));
                }

                @Override
                public T number(int n, Function<Integer, T> cast) {
                    return cast.apply(n);
                }

                @Override
                public T plus(GADT<Integer> a, GADT<Integer> b, Function<Integer, T> cast) {
                    return cast.apply(a.eval() + b.eval());
                }

                @Override
                public T isLessOrEqual(GADT<Integer> a, GADT<Integer> b, Function<Boolean, T> cast) {
                    return cast.apply(a.eval() <= b.eval());
                }

                @Override
                public T if_(GADT<Boolean> condition, GADT<T> iftrue, GADT<T> iffalse) {
                    return condition.eval() ? iftrue.eval() : iffalse.eval();
                }
            });
        }
    }
}
