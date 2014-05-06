/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JTypeVar;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class Types {
    static JClass narrow(JDefinedClass definedClass, JTypeVar[] typeParams) {
        return typeParams.length == 0 ? definedClass : definedClass.narrow(typeParams);
    }

    static Types createInstance(JCodeModel codeModel) {
        Types modelTypes = new Types();
        modelTypes._void = codeModel.VOID;
        modelTypes._long = codeModel.LONG;
        modelTypes._boolean = codeModel.BOOLEAN;
        modelTypes._int = codeModel.INT;
        modelTypes._float = codeModel.FLOAT;
        modelTypes._double = codeModel.DOUBLE;

        modelTypes._Object = codeModel.ref(Object.class);
        modelTypes._Boolean = codeModel.ref(Boolean.class);
        modelTypes._Integer = codeModel.ref(Integer.class);
        modelTypes._Long = codeModel.ref(Long.class);
        modelTypes._Double = codeModel.ref(Double.class);
        modelTypes._Float = codeModel.ref(Float.class);
        modelTypes._RuntimeException = codeModel.ref(RuntimeException.class);
        return modelTypes;
    }

    private JPrimitiveType _void;
    private JPrimitiveType _long;
    private JPrimitiveType _boolean;
    private JPrimitiveType _int;
    private JPrimitiveType _float;
    private JPrimitiveType _double;
    private JClass _Object;
    private JClass _Boolean;
    private JClass _Integer;
    private JClass _Double;
    private JClass _Float;
    private JClass _RuntimeException;
    private JClass _Long;

    private Types() {
    }

    public JPrimitiveType _void() {
        return _void;
    }

    public JPrimitiveType _int() {
        return _int;
    }

    public JPrimitiveType _long() {
        return _long;
    }

    public JPrimitiveType _boolean() {
        return _boolean;
    }

    public JPrimitiveType _float() {
        return _float;
    }

    public JPrimitiveType _double() {
        return _double;
    }

    public JType _Boolean() {
        return _Boolean;
    }

    public JClass _Object() {
        return _Object;
    }

    public JClass _RuntimeException() {
        return _RuntimeException;
    }

    public JClass _Integer() {
        return _Integer;
    }

    public JClass _Double() {
        return _Double;
    }

    public JClass _Float() {
        return _Float;
    }

    public JClass _Long() {
        return _Long;
    }
}
