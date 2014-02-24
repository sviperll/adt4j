/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JTypeVar;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class DefinedVisitorInterface {
    private final JDefinedClass visitorInterfaceModel;
    private final DataVisitor dataVisitor;

    DefinedVisitorInterface(JDefinedClass visitorInterfaceModel, DataVisitor dataVisitor) {
        this.visitorInterfaceModel = visitorInterfaceModel;
        this.dataVisitor = dataVisitor;
    }

    String getSimpleName() {
        return visitorInterfaceModel.name();
    }

    String getPackageName() {
        return visitorInterfaceModel._package().name();
    }

    Iterable<JTypeVar> getDataTypeParameters() {
        List<JTypeVar> result = new ArrayList<>();
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (!shouldBeOverridenOnInvokation(typeVariable.name()) && !isSelf(typeVariable.name()))
                result.add(typeVariable);
        }
        return result;
    }

    Iterable<JTypeVar> getVisitorInvokationTypeParameters() {
        List<JTypeVar> result = new ArrayList<>();
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (shouldBeOverridenOnInvokation(typeVariable.name()))
                result.add(typeVariable);
        }
        return result;
    }

    private boolean shouldBeOverridenOnInvokation(String name) {
        return name.equals(dataVisitor.result()) || name.equals(dataVisitor.exception());
    }

    public boolean isSelf(String name) {
        return name.equals(dataVisitor.self());
    }

    JTypeVar getResultTypeParameter() {
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeVariable.name().equals(dataVisitor.result()))
                return typeVariable;
        }
        return null;
    }

    JTypeVar getExceptionTypeParameter() {
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeVariable.name().equals(dataVisitor.exception()))
                return typeVariable;
        }
        return null;
    }

    JClass narrowed(JClass usedDataType, JClass resultType, JClass exceptionType) {
        return narrowedForSelf(usedDataType, resultType, exceptionType, usedDataType);
    }

    JClass narrowedForSelf(JClass usedDataType, JClass resultType, JClass exceptionType, JClass selfType) {
        System.out.println("Narrowing visitor interface with " + visitorInterfaceModel.typeParams().length + " parameters");
        Iterator<JClass> dataTypeArgumentIterator = usedDataType.getTypeParameters().iterator();
        List<JClass> arguments = new ArrayList<>();
        for (JTypeVar typeVariable: visitorInterfaceModel.typeParams()) {
            if (typeVariable.name().equals(dataVisitor.exception()))
                arguments.add(exceptionType);
            else if (typeVariable.name().equals(dataVisitor.result()))
                arguments.add(resultType);
            else if (typeVariable.name().equals(dataVisitor.self()))
                arguments.add(selfType);
            else
                arguments.add(dataTypeArgumentIterator.next());
        }
        return visitorInterfaceModel.narrow(arguments);
    }

    public Collection<JMethod> methods() {
        return visitorInterfaceModel.methods();
    }

    @Override
    public String toString() {
        StringWriter sb = new StringWriter();
        visitorInterfaceModel.generate(new JFormatter(sb));
        return sb.toString();
    }

    JType narrowed(JType type, JClass usedDataType, JClass resultType, JClass exceptionType) {
        if (type.name().equals(dataVisitor.exception()))
            return exceptionType;
        else if (type.name().equals(dataVisitor.result()))
            return resultType;
        else if (type.name().equals(dataVisitor.self()))
            return usedDataType;
        else
            return type;
    }
}
