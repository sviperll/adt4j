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
import javax.annotation.Nullable;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public abstract class Residence implements Renderable {

    static Residence packageLevel(final PackageLevelResidence details) {
        return new PackageLevelResidenceWrapper(details);
    }

    static Residence nested(Nesting details) {
        return new NestedResidenceWrapper(details);
    }

    static Residence local(MethodLocalResidence details) {
        return new MethodLocalResidenceWrapper(details);
    }

    private Residence() {
    }

    public abstract Kind kind();
    public final boolean isPackageLevel() {
        return kind() == Kind.PACKAGE_LEVEL;
    }
    public final boolean isNested() {
        return kind() == Kind.NESTED;
    }
    public final boolean isLocal() {
        return kind() == Kind.LOCAL;
    }

    public PackageLevelResidence getPackageLevelDetails() {
        throw new UnsupportedOperationException("Package level residence expected");
    }
    public Nesting getNesting() {
        throw new UnsupportedOperationException("Nested residence expected");
    }
    public MethodLocalResidence getLocalDetails() {
        throw new UnsupportedOperationException("Local residence expected");
    }

    public Package getPackage() {
        if (isPackageLevel()) {
            return getPackageLevelDetails().getPackage();
        } else if (isNested()) {
            return getNesting().parent().residence().getPackage();
        } else if (isLocal()) {
            return getLocalDetails().parent().residence().getPackage();
        } else {
            throw new UnsupportedOperationException("Unsupported residence: " + kind());
        }
    }

    /**
     * Enclosing definition that provide effective context for current definition.
     *
     * @return Enclosing definition or null for package-level definitions or static members
     */
    @Nullable
    public GenericDefinition<?, ?> contextDefinition() {
        if (isPackageLevel()) {
            return null;
        } else if (isNested()) {
            return getNesting().isStatic() ? null : getNesting().parent();
        } else if (isNested()) {
            return getLocalDetails().parent();
        } else {
            throw new UnsupportedOperationException("Unsupported residence: " + kind());
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

    public enum Kind {
        PACKAGE_LEVEL, NESTED, LOCAL;
    }

    private static class PackageLevelResidenceWrapper extends Residence {

        private final PackageLevelResidence details;

        PackageLevelResidenceWrapper(PackageLevelResidence details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.PACKAGE_LEVEL;
        }

        @Override
        public PackageLevelResidence getPackageLevelDetails() {
            return details;
        }
    }

    private static class NestedResidenceWrapper extends Residence {

        private final Nesting details;

        NestedResidenceWrapper(Nesting details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.NESTED;
        }

        @Override
        public Nesting getNesting() {
            return details;
        }
    }

    private static class MethodLocalResidenceWrapper extends Residence {

        private final MethodLocalResidence details;

        MethodLocalResidenceWrapper(MethodLocalResidence details) {
            this.details = details;
        }

        @Override
        public Kind kind() {
            return Kind.LOCAL;
        }

        @Override
        public MethodLocalResidence getLocalDetails() {
            return details;
        }

    }


}
