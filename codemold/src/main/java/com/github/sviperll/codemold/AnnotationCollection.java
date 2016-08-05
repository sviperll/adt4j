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

import com.github.sviperll.codemold.util.CMCollections;
import com.github.sviperll.codemold.util.CMCollectors;
import com.github.sviperll.codemold.util.Snapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
class AnnotationCollection implements Annotated {
    public static Builder createBuilder() {
        return new Builder();
    }

    private final Map<? extends String, ? extends List<? extends Annotation>> annotationMap;
    private AnnotationCollection(Map<? extends String, ? extends List<? extends Annotation>> annotationMap) {
        this.annotationMap = Snapshot.of(annotationMap);
    }

    @Override
    public List<? extends Annotation> getAnnotation(ObjectDefinition definition) {
        return Optional.ofNullable(annotationMap.get(definition.qualifiedTypeName())).map(Snapshot::of).orElseGet(Collections::emptyList);
    }

    @Override
    public Collection<? extends Annotation> allAnnotations() {
        return annotationMap.values().stream().flatMap(list -> list.stream()).collect(CMCollectors.toImmutableList());
    }

    static class Builder {
        private final Map<String, List<Annotation>> annotationMap = CMCollections.newTreeMap();
        private Builder() {
        }

        public void annotate(Annotation annotation) {
            String key = annotation.definition().qualifiedTypeName();
            Optional<List<Annotation>> current = Optional.ofNullable(annotationMap.get(key));
            List<Annotation> value = current.orElseGet(CMCollections::newArrayList);
            value.add(annotation);
            if (!current.isPresent())
                annotationMap.put(key, value);
        }

        public AnnotationCollection build() {
            return new AnnotationCollection(Snapshot.of(annotationMap));
        }
    }
}
