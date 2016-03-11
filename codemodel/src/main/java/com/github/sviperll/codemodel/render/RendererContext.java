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

import com.github.sviperll.codemodel.ObjectTypeDetails;
import com.github.sviperll.codemodel.Type;
import com.github.sviperll.codemodel.WildcardTypeDetails;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class RendererContext {
    public static RendererContext createInstance(final StringBuilder stringBuilder) {
        return createInstance(new TypeAwareWriter() {
            @Override
            public void writeQualifiedTypeName(String name) {
                stringBuilder.append(name);
            }

            @Override
            public void writeText(String text) {
                stringBuilder.append(text);
            }
        });
    }
    public static RendererContext createInstance(TypeAwareWriter writer) {
        return new RendererContext(new LineWriter("    ", writer));
    }
    private final LineWriter implementation;
    private final int identationLevel;

    private RendererContext(LineWriter implementation) {
        this(implementation, 0);
    }
    private RendererContext(LineWriter implementation, int identationLevel) {
        this.implementation = implementation;
        this.identationLevel = identationLevel;
    }
    public void append(String s) {
        implementation.writeText(identationLevel, s);
    }
    public void nextLine() {
        implementation.nextLine();
    }

    public void appendType(Type type) {
        if (type.isArray()) {
            appendType(type.getArrayDetails().elementType());
            append("[]");
        } else if (type.isIntersection()) {
            Iterator<Type> iterator = type.getIntersectionDetails().intersectedTypes().iterator();
            if (iterator.hasNext()) {
                appendType(iterator.next());
                while (iterator.hasNext()) {
                    append(" & ");
                    appendType(iterator.next());
                }
            }
        } else if (type.isVoid()) {
            append("void");
        } else if (type.isPrimitive()) {
            append(type.getPrimitiveDetails().name().toLowerCase(Locale.US));
        } else if (type.isTypeVariable()) {
            append(type.getVariableDetails().name());
        } else if (type.isWildcard()) {
            WildcardTypeDetails wildcard = type.getWildcardDetails();
            append("?");
            append(wildcard.boundKind() == WildcardTypeDetails.BoundKind.SUPER ? " super " : " extends ");
            appendType(wildcard.bound());
        } else if (type.isObjectType()) {
            ObjectTypeDetails objectType = type.getObjectDetails();
            if (objectType.isRaw())
                implementation.writeQualifiedTypeName(identationLevel, objectType.definition().qualifiedName());
            else {
                appendType(objectType.erasure());
                Iterator<Type> iterator = objectType.typeArguments().iterator();
                if (iterator.hasNext()) {
                    append("<");
                    appendType(iterator.next());
                    while (iterator.hasNext()) {
                        append(", ");
                        appendType(iterator.next());
                    }
                    append(">");
                }
            }
        }
    }

    public RendererContext indented() {
        return new RendererContext(implementation, identationLevel + 1);
    }


}
