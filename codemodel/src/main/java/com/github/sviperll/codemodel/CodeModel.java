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

import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public final class CodeModel {
    static void validateSimpleName(String name) throws CodeModelException {
        if (!name.matches("[_A-Za-z][_A-Za-z0-9]*")) {
            throw new CodeModelException(name + " is not allowed Java identifier");
        }
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    private final boolean includesLoadableClasses;
    private final Package defaultPackage = Package.createTopLevelPackage(this);
    private Type objectType = null;

    private CodeModel(boolean includesLoadableClasses) {
        this.includesLoadableClasses = includesLoadableClasses;
    }

    public Type objectType() {
        if (objectType == null) {
            objectType = reference(Object.class.getName()).rawType();
            if (objectType == null) {
                try {
                    Package pkg = getPackage("java.lang");
                    NamedObjectBuilder<PackageLevelBuilder> builder = pkg.createClass(ObjectKind.CLASS, "Object");
                    objectType = builder.definition().rawType();
                    builder.residence().setPublic(true);
                    MethodBuilder equals = builder.method("equals");
                    equals.resultType(Type.booleanType());
                    equals.addParameter(objectType, "that");
                } catch (CodeModelException ex) {
                    throw new RuntimeException("Should never happen");
                }
            }
        }
        return objectType;
    }

    public Package getPackage(String qualifiedName) throws CodeModelException {
        return defaultPackage.getChildPackageBySuffix(qualifiedName);
    }

    public ObjectBuilder<PackageLevelBuilder> createDefaultPackageClass(ObjectKind kind, String name) throws CodeModelException {
        return defaultPackage.createClass(kind, name);
    }

    public ObjectDefinition reference(String qualifiedName) {
        return defaultPackage.reference(qualifiedName);
    }

    boolean includesLoadableClasses() {
        return includesLoadableClasses;
    }

    public static class Builder {
        private boolean includesLoadableClasses = true;

        public Builder() {
        }
        public void includeLoadableClasses() {
            includesLoadableClasses = true;
        }
        public void doNotIncludeLoadableClasses() {
            includesLoadableClasses = false;
        }
        public CodeModel build() {
            return new CodeModel(includesLoadableClasses);
        }
    }

}
