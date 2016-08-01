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

import com.github.sviperll.codemold.render.Renderable;
import com.github.sviperll.codemold.render.Renderer;
import com.github.sviperll.codemold.render.RendererContext;
import com.github.sviperll.codemold.util.CMCollections;
import com.github.sviperll.codemold.util.Snapshot;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class Annotation implements Renderable {
    public static Annotation createInstance(ObjectDefinition definition, CompileTimeValue value) {
        Builder builder = createBuilder(definition);
        builder.set("value", value);
        return builder.build();
    }

    public static Builder createBuilder(ObjectDefinition definition) {
        return new Builder(definition);
    }

    public static Annotation createInstance(ObjectDefinition definition) {
        Builder builder = createBuilder(definition);
        return builder.build();
    }

    private final ObjectDefinition definition;
    private final Map<? extends String, ? extends AnyCompileTimeValue> valueMap;

    private Annotation(ObjectDefinition definition, Map<String, AnyCompileTimeValue> valueMap) {
        this.definition = definition;
        this.valueMap = Snapshot.of(valueMap);
    }

    public ObjectDefinition definition() {
        return definition;
    }

    public Optional<AnyCompileTimeValue> getValue(String name) {
        Optional<AnyCompileTimeValue> value = Optional.ofNullable(valueMap.get(name));
        if (value.isPresent())
            return value;
        else {
            Optional<? extends MethodDefinition> method = definition.methods().stream().filter(m -> m.name().equals(name)).findFirst();
            return method.flatMap(m -> !m.hasDefaultValue() ? Optional.empty() : Optional.of(m.defaultValue()));
        }
    }

    @Override
    public Renderer createRenderer(RendererContext context) {
        return () -> {
            context.appendText("@");
            context.appendQualifiedClassName(definition.qualifiedTypeName());
            if (valueMap.size() == 1 && valueMap.containsKey("value")) {
                context.appendText("(");
                context.appendRenderable(valueMap.get("value"));
                context.appendText(")");
            } else {
                Iterator<? extends Map.Entry<? extends String, ? extends AnyCompileTimeValue>> iterator;
                iterator = valueMap.entrySet().iterator();
                if (iterator.hasNext()) {
                    context.appendText("(");
                    Map.Entry<? extends String, ? extends AnyCompileTimeValue> entry = iterator.next();
                    context.appendText(entry.getKey());
                    context.appendText(" = ");
                    context.appendRenderable(entry.getValue());
                    while (iterator.hasNext()) {
                        context.appendText(", ");
                        entry = iterator.next();
                        context.appendText(entry.getKey());
                        context.appendText(" = ");
                        context.appendRenderable(entry.getValue());
                    }
                    context.appendText(")");
                }
            }
        };
    }

    public static class Builder {

        private final ObjectDefinition definition;
        private final Map<String, AnyCompileTimeValue> valueMap = CMCollections.newTreeMap();

        private Builder(ObjectDefinition definition) {
            if (!definition.kind().isAnnotation())
                throw new IllegalArgumentException(MessageFormat.format("{0} definition is not annotation", definition));
            this.definition = definition;
        }

        public void set(String name, CompileTimeValue value) {
            valueMap.put(name, value.asAny());
        }

        public Annotation build() {
            return new Annotation(definition, valueMap);
        }
    }
}
