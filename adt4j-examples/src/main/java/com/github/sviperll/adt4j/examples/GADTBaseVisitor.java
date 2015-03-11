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

import com.github.sviperll.adt4j.GenerateValueClassForVisitor;
import com.github.sviperll.meta.Visitor;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */

@GenerateValueClassForVisitor(className = "GADTBase")
@Visitor(resultVariableName = "R")
interface GADTBaseVisitor<A, SF, SA, SB, SI, SBOOL, CAST_F_TO_T, CAST_B_TO_T, CAST_INT_TO_T, CAST_BOOL_TO_T, ST, R> {
    R lambda(Function<A, SB> function, CAST_F_TO_T cast);
    R apply(SF function, SA argument, CAST_B_TO_T cast);
    R number(int n, CAST_INT_TO_T cast);
    R plus(SI a, SI b, CAST_INT_TO_T cast);
    R isLessOrEqual(SI a, SI b, CAST_BOOL_TO_T cast);
    R if_(SBOOL condition, ST iftrue, ST iffalse);
}
