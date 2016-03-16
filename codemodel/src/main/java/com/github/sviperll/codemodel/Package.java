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

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public final class Package implements Model {

    private final CodeModel codeModel;
    private final String name;
    private final Package parent;
    private final Map<String, ObjectDefinition> classes = new TreeMap<>();
    private final Map<String, Package> packages = new TreeMap<>();
    Package(CodeModel codeModel, String name) {
        this(codeModel, name, null);
    }

    private Package(CodeModel codeModel, String name, Package parent) {
        this.codeModel = codeModel;
        this.name = name;
        this.parent = parent;
    }

    public Package getParent() {
        return parent;
    }

    public ObjectDefinitionBuilder createClass(ObjectKind kind, String className) throws CodeModelException {
        if (classes.containsKey(className))
            throw new CodeModelException(name + "." + className + " already defined");
        PackageLevelResidenceBuilder membershipBuilder = new PackageLevelResidenceBuilder(this);
        ObjectDefinitionBuilder result = new ObjectDefinitionBuilder(kind, membershipBuilder, className);
        classes.put(className, result.definition());
        return result;
    }

    String qualifiedName() {
        return name;
    }

    @Override
    public CodeModel getCodeModel() {
        return codeModel;
    }

    Package getChildPackageBySuffix(String suffix) throws CodeModelException {
        int index = suffix.indexOf('.');
        if (index == 0)
            throw new CodeModelException(name + "." + suffix + " illegal package name");
        boolean isChild = index < 0;
        String childSuffix = isChild ? suffix : suffix.substring(0, index);
        if (classes.containsKey(childSuffix))
            throw new CodeModelException(name + "." + childSuffix + " is a class, but package expected");
        Package childPackage = packages.get(childSuffix);
        if (childPackage == null) {
            CodeModel.validateSimpleName(childSuffix);
            childPackage = new Package(codeModel, name + "." + childSuffix, this);
            packages.put(childSuffix, childPackage);
        }
        if (isChild)
            return childPackage;
        else
            return childPackage.getChildPackageBySuffix(suffix.substring(index + 1));
    }
}
