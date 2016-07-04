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
import javax.annotation.Nonnull;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public abstract class Residence implements Renderable, ResidenceProvider, Model {

    @Nonnull
    static Residence packageLevel(final PackageLevelResidence details) {
        return new PackageLevelResidenceWrapper(details);
    }

    @Nonnull
    static Residence nested(Nesting details) {
        return new NestedResidenceWrapper(details);
    }

    @Nonnull
    static Residence local(MethodLocalResidence details) {
        return new MethodLocalResidenceWrapper(details);
    }

    private Residence() {
    }

    @Nonnull
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

    /**
     * Details about package top-level definitions.
     * Throws UnsupportedOperationException for other than top-level definitions.
     * @see Residence#isPackageLevel()
     * @throws UnsupportedOperationException
     * @return Details about package top-level definitions.
     */
    @Nonnull
    public PackageLevelResidence getPackageLevelDetails() {
        throw new UnsupportedOperationException("Package level residence expected. Use isPackageLevel method for check.");
    }

    /**
     * Details about nested definitions.
     * Throws UnsupportedOperationException for other than nested definitions (i. e. top-level and method local).
     * @see Residence#isNested()
     * @throws UnsupportedOperationException
     * @return Details about nested definitions..
     */
    @Nonnull
    public Nesting getNesting() {
        throw new UnsupportedOperationException("Nested residence expected. Use isNested method for check.");
    }

    /**
     * Details about method-local definitions.
     * Throws UnsupportedOperationException for other than method-local definitions (i. e. top-level and nested).
     * @see Residence#isLocal()
     * @throws UnsupportedOperationException
     * @return Details about method-local definitions.
     */
    @Nonnull
    public MethodLocalResidence getLocalDetails() {
        throw new UnsupportedOperationException("Local residence expected. Use isLocal method for check.");
    }

    @Nonnull
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

    public boolean hasContextDefintion() {
        return isLocal() || (isNested() && !getNesting().isStatic());
    }

    /**
     * Enclosing definition that provide effective context for current definition.
     * Throws UnsupportedOperationException for top-level and static residence.
     * @see Residence#hasContextDefintion()
     * @throws UnsupportedOperationException
     * @return Enclosing definition
     */
    @Nonnull
    public GenericDefinition<?, ?> getContextDefinition() {
        if (isNested() && !getNesting().isStatic())
            return getNesting().parent();
        else if (isLocal())
            return getLocalDetails().parent();
        else
            throw new UnsupportedOperationException("No enclosing context here. Use hasContextDefintion method for check.");
    }

    @Override
    public Renderer createRenderer(RendererContext context) {
        if (isLocal()) {
            return getLocalDetails().createRenderer(context);
        } else if (isPackageLevel()) {
            return getPackageLevelDetails().createRenderer(context);
        } else if (isNested()) {
            return getNesting().createRenderer(context);
        } else
            throw new IllegalStateException("Rendering unsupported residence " + kind());
    }

    Renderable forObjectKind(ObjectKind kind) {
        if (isLocal()) {
            return getLocalDetails().forObjectKind(kind);
        } else if (isPackageLevel()) {
            return getPackageLevelDetails().forObjectKind(kind);
        } else if (isNested()) {
            return getNesting().forObjectKind(kind);
        } else
            throw new IllegalStateException("Rendering unsupported residence " + kind());
    }

    @Override
    public Residence residence() {
        return this;
    }

    @Override
    public CodeModel getCodeModel() {
        return getPackage().getCodeModel();
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
