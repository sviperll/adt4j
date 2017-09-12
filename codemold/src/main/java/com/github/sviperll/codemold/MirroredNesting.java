package com.github.sviperll.codemold;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@ParametersAreNonnullByDefault
class MirroredNesting extends Nesting {
    private final Mirror mirror;
    private final Element element;

    public MirroredNesting(Mirror mirror, Element element) {
        this.mirror = mirror;
        this.element = element;
    }

    @Nonnull
    @Override
    public MemberAccess accessLevel() {
        if (element.getModifiers().contains(Modifier.PUBLIC))
            return MemberAccess.PUBLIC;
        else if (element.getModifiers().contains(Modifier.PROTECTED))
            return MemberAccess.PROTECTED;
        else if (element.getModifiers().contains(Modifier.PRIVATE))
            return MemberAccess.PRIVATE;
        else
            return MemberAccess.PACKAGE;
    }

    @Override
    public boolean isStatic() {
        return element.getModifiers().contains(Modifier.STATIC);
    }

    @Nonnull
    @Override
    public ObjectDefinition parent() {
        TypeElement enclosingElement = (TypeElement)element.getEnclosingElement();
        return mirror.getCodeMold().getReference(enclosingElement.getQualifiedName().toString())
                .orElseThrow(() -> new IllegalStateException("Parent object should exists"));
    }
}
