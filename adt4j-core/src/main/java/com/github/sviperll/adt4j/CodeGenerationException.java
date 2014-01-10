/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.JClassAlreadyExistsException;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class CodeGenerationException extends Exception {

    public CodeGenerationException(JClassAlreadyExistsException ex) {
        super(ex);
    }

}
