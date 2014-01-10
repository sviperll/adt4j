/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class DefinedClass {
    private final JDefinedClass definedClass;

    DefinedClass(JDefinedClass definedClass) {
        this.definedClass = definedClass;
    }

    String getQualifiedName() {
        return definedClass.fullName();
    }

    JCodeModel getCodeModel() {
        return definedClass.owner();
    }

}
