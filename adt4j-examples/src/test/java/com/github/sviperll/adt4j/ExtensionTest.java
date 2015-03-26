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
package com.github.sviperll.adt4j;

import com.github.sviperll.adt4j.examples.Optional;
import com.github.sviperll.adt4j.examples.OptionalVisitor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class ExtensionTest {

    public ExtensionTest() {
    }

    @Test
    public void testVisitor() throws Exception {
        Optional<String> optional = Optional.<String>missing();

        String result = optional.accept(new OptionalVisitor<String, String, RuntimeException> () {
            @Override
            public String missing() throws RuntimeException {
                return "ok";
            }

            @Override
            public String present(String value) throws RuntimeException {
                return "shouldn't be called";
            }
        });
        assertEquals("ok", result);

        optional = Optional.<String>present("str1");

        result = optional.accept(new OptionalVisitor<String, String, RuntimeException> () {
            @Override
            public String missing() throws RuntimeException {
                return "shouldn't be called";
            }

            @Override
            public String present(String value) throws RuntimeException {
                assertEquals("str1", value);
                return "ok";
            }
        });
        assertEquals("ok", result);
    }

    @Test
    public void testGetters() throws Exception {
        Optional<String> optional = Optional.<String>present("str1");
        assertEquals("str1", optional.getValue());
    }

    @Test
    public void testEquals() {
        Optional<String> optional1 = Optional.<String>missing();
        Optional<String> optional2 = Optional.<String>missing();
        assertTrue("optional1.equals(optional2)", optional1.equals(optional2));

        optional1 = Optional.<String>missing();
        optional2 = Optional.<String>present("str1");

        assertFalse("optional1.equals(optional2)", optional1.equals(optional2));

        optional1 = Optional.<String>present("str1");
        optional2 = Optional.<String>present("str1");

        assertTrue("optional1.equals(optional2)", optional1.equals(optional2));

        optional1 = Optional.<String>present("str1");
        optional2 = Optional.<String>present("str2");

        assertFalse("optional1.equals(optional2)", optional1.equals(optional2));
    }
}
