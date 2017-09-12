/*
 * Copyright (c) 2016, vir
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
import com.github.sviperll.codemold.util.CMCollectors;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author vir
 */
class MirroredObjectDefinition extends ObjectDefinition {
    private final Mirror mirror;
    private final Residence residence;
    private final TypeElement element;
    private ObjectType extendsClass = null;
    private List<? extends ObjectType> interfaces = null;
    private List<? extends EnumConstant> enumConstants = null;
    private List<? extends ConstructorDefinition> constructors = null;

    MirroredObjectDefinition(Mirror mirror, ResidenceProvider residence, TypeElement element) {
        this.mirror = mirror;
        this.residence = residence.residence();
        this.element = element;
    }

    @Nonnull
    @Override
    public Residence residence() {
        return residence;
    }

    @Override
    public boolean isFinal() {
        return element.getModifiers().contains(Modifier.FINAL);
    }

    @Nonnull
    @Override
    public ObjectKind kind() {
        if (element.getKind() == ElementKind.INTERFACE) {
            return ObjectKind.INTERFACE;
        } else if (element.getKind() == ElementKind.ENUM) {
            return ObjectKind.ENUM;
        } else if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
            return ObjectKind.ANNOTATION;
        } else {
            return ObjectKind.CLASS;
        }
    }

    @Nonnull
    @Override
    public ObjectType extendsClass() {
        if (extendsClass == null) {
            TypeMirror superclass = element.getSuperclass();
            if (superclass.getKind() == TypeKind.NONE)
                extendsClass = mirror.getCodeMold().objectType();
            else
                extendsClass = mirror.readMirroredType(superclass);
        }
        return extendsClass;
    }

    @Nonnull
    @Override
    public List<? extends ObjectType> implementsInterfaces() {
        if (interfaces == null) {
            interfaces = element.getInterfaces().stream()
                    .map(mirror::readMirroredType)
                    .collect(CMCollectors.toImmutableList());
        }
        return interfaces;
    }

    @Nonnull
    @Override
    public List<? extends EnumConstant> enumConstants() {
        if (enumConstants == null) {
            enumConstants = element.getEnclosedElements().stream()
                    .filter(elem -> elem.getKind() == ElementKind.ENUM_CONSTANT)
                    .map(elem -> new MirroredEnumConstant(mirror, this, (TypeElement)elem))
                    .collect(CMCollectors.toImmutableList());
        }
        return enumConstants;
    }

    @Nonnull
    @Override
    public List<? extends ConstructorDefinition> constructors() {
        if (constructors == null) {
            constructors = element.getEnclosedElements().stream()
                    .filter(elem -> elem.getKind() == ElementKind.CONSTRUCTOR)
                    .map(elem -> MirroredConstructorDefinition.createInstance(mirror, (ExecutableElement)elem))
                    .collect(CMCollectors.toImmutableList());
        }
        return constructors;
    }

    @Nonnull
    @Override
    public List<? extends MethodDefinition> methods() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Nonnull
    @Override
    public Collection<? extends ObjectDefinition> innerClasses() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Nonnull
    @Override
    public Collection<? extends FieldDeclaration> fields() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Nonnull
    @Override
    public String simpleTypeName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAnonymous() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Nonnull
    @Override
    List<? extends Renderable> staticInitializationElements() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Nonnull
    @Override
    List<? extends Renderable> instanceInitializationElements() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Nonnull
    @Override
    public TypeParameters typeParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CodeMold getCodeMold() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Nonnull
    @Override
    public List<? extends Annotation> getAnnotation(ObjectDefinition definition) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Nonnull
    @Override
    public Collection<? extends Annotation> allAnnotations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
