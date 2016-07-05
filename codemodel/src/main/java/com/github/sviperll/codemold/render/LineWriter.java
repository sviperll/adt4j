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

package com.github.sviperll.codemold.render;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
 class LineWriter {

    private final String indentation;
    private final ClassAwareWriter writer;
    private boolean isStartOfLine = true;
    private boolean wasWhiteSpace = true;
    private boolean wasEmptyLine = true;

    LineWriter(String indentation, ClassAwareWriter writer) {
        this.indentation = indentation;
        this.writer = writer;
    }

    public void writeText(int identationLevel, String s) {
        if (isStartOfLine) {
            for (int i = 0; i < identationLevel; i++) {
                writer.writeText(indentation);
            }
            isStartOfLine = false;
        }
        writer.writeText(s);
        wasWhiteSpace = false;
        wasEmptyLine = false;
    }

    public void writeLineBreak() {
        if (isStartOfLine)
            wasEmptyLine = true;
        writer.writeText("\n");
        isStartOfLine = true;
        wasWhiteSpace = true;
    }

    public void writeQualifiedTypeName(int identationLevel, String name) {
        if (isStartOfLine) {
            for (int i = 0; i < identationLevel; i++) {
                writer.writeText(indentation);
            }
            isStartOfLine = false;
        }
        writer.writeQualifiedClassName(name);
        wasWhiteSpace = false;
        wasEmptyLine = false;
    }

    void writeWhiteSpace() {
        if (!wasWhiteSpace) {
            writer.writeText(" ");
            wasWhiteSpace = true;
            wasEmptyLine = false;
        }
    }

    void appendEmptyLine() {
        if (!wasEmptyLine) {
            if (!isStartOfLine)
                writeLineBreak();
            writeLineBreak();
        }
    }
}
