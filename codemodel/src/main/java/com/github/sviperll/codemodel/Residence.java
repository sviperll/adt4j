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

package com.github.sviperll.codemodel;

import com.github.sviperll.codemodel.render.Renderable;
import com.github.sviperll.codemodel.render.Renderer;
import com.github.sviperll.codemodel.render.RendererContext;
import java.util.Locale;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public abstract class Residence implements Renderable {

    static Residence packageLevel(final PackageLevelDetails details) {
        return new PackageLevelResidence(details);
    }

    static Residence nested(Nesting details) {
        return new NestedResidence(details);
    }
    private Residence() {
    }

    public abstract boolean isPackageLevel();
    public abstract boolean isNested();

    public abstract PackageLevelDetails getPackageLevelDetails();
    public abstract Nesting getNesting();

    public Package getPackage() {
        if (isPackageLevel())
            return getPackageLevelDetails().getPackage();
        else {
            return getNesting().parent().residence().getPackage();
        }
    }

    @Override
    public Renderer createRenderer(final RendererContext context) {
        return new Renderer() {
            @Override
            public void render() {
                if (isPackageLevel()) {
                    if (getPackageLevelDetails().isPublic())
                        context.appendText("public");
                } else {
                    Nesting nesting = getNesting();
                    MemberAccess accessLevel = nesting.accessLevel();
                    if (accessLevel != MemberAccess.PACKAGE)
                        context.appendText(accessLevel.name().toLowerCase(Locale.US));
                    context.appendWhiteSpace();
                    if (nesting.isStatic())
                        context.appendText("static");
                }
            }

        };
    }

    private static class PackageLevelResidence extends Residence {

        private final PackageLevelDetails details;

        public PackageLevelResidence(PackageLevelDetails details) {
            this.details = details;
        }

        @Override
        public boolean isPackageLevel() {
            return true;
        }

        @Override
        public boolean isNested() {
            return false;
        }

        @Override
        public PackageLevelDetails getPackageLevelDetails() {
            return details;
        }

        @Override
        public Nesting getNesting() {
            throw new UnsupportedOperationException();
        }
    }

    private static class NestedResidence extends Residence {

        private final Nesting details;

        public NestedResidence(Nesting details) {
            this.details = details;
        }

        @Override
        public boolean isPackageLevel() {
            return false;
        }

        @Override
        public boolean isNested() {
            return true;
        }

        @Override
        public PackageLevelDetails getPackageLevelDetails() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Nesting getNesting() {
            return details;
        }
    }
}
