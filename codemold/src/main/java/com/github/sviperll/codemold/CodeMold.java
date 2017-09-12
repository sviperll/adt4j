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

package com.github.sviperll.codemold;

import java.text.MessageFormat;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Facade-class for codemold library.
 *
 * Use CodeMold#createBuilder() method to build new CodeMold instance.
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public final class CodeMold {
    static void validateSimpleName(String name) throws CodeMoldException {
        if (!name.matches("[_A-Za-z][_A-Za-z0-9]*")) {
            throw new CodeMoldException(name + " is not allowed Java identifier");
        }
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    private final Optional<Elements> elements;
    private Package defaultPackage = null;
    private ObjectType objectType = null;

    private CodeMold(Optional<Elements> elements) {
        this.elements = elements;
    }

    @Nonnull
    public ObjectType objectType() {
        if (objectType == null) {
            ObjectDefinition javaLangObjectDefinition = getReference(Object.class.getName()).orElseThrow(() -> {
                return new IllegalStateException("java.lang.Object is not loadable class!");
            });
            objectType = javaLangObjectDefinition.rawType();
        }
        return objectType;
    }

    @Nonnull
    public Package getPackage(String qualifiedName) throws CodeMoldException {
        return defaultPackage().getChildPackageBySuffix(qualifiedName);
    }

    @Nonnull
    public Package defaultPackage() {
        if (defaultPackage == null)
            defaultPackage = Package.createTopLevelPackage(this);
        return defaultPackage;
    }

    @Nonnull
    public Optional<ObjectDefinition> getReference(String qualifiedName) {
        return defaultPackage().getReference(qualifiedName);
    }

    @Nonnull
    public ObjectDefinition getReference(Class<?> klass) {
        if (klass.isPrimitive() || klass.isArray())
            throw new IllegalArgumentException(MessageFormat.format("{0} class should be object definition", klass));
        try {
            return defaultPackage().getReference(klass.getName()).orElseThrow(() -> {
                return new IllegalStateException(MessageFormat.format("{0} class is not accessible as object definition", klass));
            });
        } catch (AssertionError error) {
            throw new AssertionError(MessageFormat.format("Unable to read reflected class: {0}", klass.getName()), error);
        }
    }

    @Nonnull
    Optional<Mirror> createMirror() {
        return elements.map(elems -> new Mirror(this, elems));
    }

    public static class Builder {
        private Elements elements = null;
        public Builder() {
        }

        public void enableAccessToProcesseableElements(Elements elements) {
            this.elements = elements;
        }

        @Nonnull
        public CodeMold build() {
            return new CodeMold(Optional.ofNullable(elements));
        }
    }

}
