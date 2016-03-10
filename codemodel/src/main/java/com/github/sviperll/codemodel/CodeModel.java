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

import java.util.Map;
import java.util.TreeMap;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public final class CodeModel {
    static void validateSimpleName(String name) throws CodeModelException {
        if (!name.matches("[_A-Za-z][_A-Za-z0-9]")) {
            throw new CodeModelException(name + " is not allowed Java identifier");
        }
    }

    private final Package defaultPackage = new Package(this, "");
    private final Map<String, Package> packages = new TreeMap<>();

    public ObjectType objectType() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Package getPackage(String qualifiedName) throws CodeModelException {
        int index = qualifiedName.indexOf('.');
        if (index == 0)
            throw new CodeModelException(qualifiedName + " illegal qualified name");
        boolean isTopLevelPackage = index < 0;
        String topLevelName = isTopLevelPackage ? qualifiedName : qualifiedName.substring(0, index);
        Package topLevelPackage = packages.get(topLevelName);
        if (topLevelPackage == null) {
            validateSimpleName(topLevelName);
            topLevelPackage = new Package(this, topLevelName);
            packages.put(topLevelName, topLevelPackage);
        }
        if (isTopLevelPackage)
            return topLevelPackage;
        else
            return topLevelPackage.getChildPackageBySuffix(qualifiedName.substring(index + 1));
    }

    public ObjectDefinitionBuilder<PackageLevelResidence, PackageLevelResidenceBuilder> createDefaultPackageClass(ObjectKind kind, String name) throws CodeModelException {
        return defaultPackage.createClass(kind, name);
    }

    public ObjectDefinition<?> importClass(Class<?> klass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ObjectDefinition<?> importClass(Element element, Elements elementUtils) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
