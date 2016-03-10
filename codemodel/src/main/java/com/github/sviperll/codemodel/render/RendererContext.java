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

package com.github.sviperll.codemodel.render;

import com.github.sviperll.codemodel.Type;
import java.io.IOException;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class RendererContext {
    public static RendererContext createInstance(Appendable appendable) {
        return new RendererContext(new RendererContextImplementation("    ", appendable));
    }
    private final RendererContextImplementation implementation;
    private final int identationLevel;

    private RendererContext(RendererContextImplementation implementation) {
        this(implementation, 0);
    }
    private RendererContext(RendererContextImplementation implementation, int identationLevel) {
        this.implementation = implementation;
        this.identationLevel = identationLevel;
    }
    public void append(String s) {
        implementation.append(identationLevel, s);
    }
    public void nextLine() {
        implementation.nextLine();
    }

    public void appendType(Type type) {
        implementation.appendType(identationLevel, type);
    }

    public RendererContext indented() {
        return new RendererContext(implementation, identationLevel + 1);
    }

    private static class RendererContextImplementation {
        private final String indentation;
        private final Appendable appendable;
        private boolean isStartOfLine = true;

        private RendererContextImplementation(String indentation, Appendable appendable) {
            this.indentation = indentation;
            this.appendable = appendable;
        }

        public void append(int identationLevel, String s) {
            try {
                if (isStartOfLine) {
                    for (int i = 0; i < identationLevel; i++) {
                        appendable.append(indentation);
                    }
                    isStartOfLine = false;
                }
                appendable.append(s);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void nextLine() {
            try {
                appendable.append("\n");
                isStartOfLine = false;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void appendType(int identationLevel, Type type) {
            append(identationLevel, type.toString());
        }
    }
}
