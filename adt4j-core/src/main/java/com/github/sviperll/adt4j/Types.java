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
    public static JClass narrow(JDefinedClass definedClass, JTypeVar[] typeParams) {
        return typeParams.length == 0 ? definedClass : definedClass.narrow(typeParams);
    }

    public static Types createInstance(JCodeModel codeModel) {
        return new Types(codeModel);
    }

    private final JPrimitiveType _void;
    private final JPrimitiveType _long;
    private final JPrimitiveType _boolean;
    private final JPrimitiveType _int;
    private final JPrimitiveType _float;
    private final JPrimitiveType _double;
    private final JClass _Object;
    private final JClass _Boolean;
    private final JClass _Integer;
    private final JClass _Double;
    private final JClass _Float;
    private final JClass _RuntimeException;
    private final JClass _Long;
    private final JClass _NullPointerException;

    private Types(JCodeModel codeModel) {
        _void = codeModel.VOID;
        _long = codeModel.LONG;
        _boolean = codeModel.BOOLEAN;
        _int = codeModel.INT;
        _float = codeModel.FLOAT;
        _double = codeModel.DOUBLE;

        _Object = codeModel.ref(Object.class);
        _Boolean = codeModel.ref(Boolean.class);
        _Integer = codeModel.ref(Integer.class);
        _Long = codeModel.ref(Long.class);
        _Double = codeModel.ref(Double.class);
        _Float = codeModel.ref(Float.class);
        _RuntimeException = codeModel.ref(RuntimeException.class);
        _NullPointerException = codeModel.ref(NullPointerException.class);
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

    public JClass _NullPointerException() {
        return _NullPointerException;
    }
}
