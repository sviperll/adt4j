/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
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
package com.github.sviperll.adt4j;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JTypeVar;
import com.sun.codemodel.JVar;
import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.element.ElementKind.INTERFACE;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.NATIVE;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.Modifier.SYNCHRONIZED;
import static javax.lang.model.element.Modifier.TRANSIENT;
import static javax.lang.model.element.Modifier.VOLATILE;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import static javax.lang.model.type.TypeKind.CHAR;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

class ValueClassModelBuilder {
    private static TypeElement toTypeElement(Element element) throws SourceException {
        if (!(element instanceof TypeElement))
            throw new SourceException("DataVisitor annotation is only allowed to interfaces");
        else
            return (TypeElement)element;
    }
    private final JCodeModel codeModel;

    private static int toJMod(Collection<Modifier> modifierCollection) {
        int modifiers = 0;
        for (Modifier modifier: modifierCollection) {
            modifiers = modifiers | toJMod(modifier);
        }
        return modifiers;
    }

    private static int toJMod(Modifier modifier) {
        switch (modifier) {
            case ABSTRACT:
                return JMod.ABSTRACT;
            case FINAL:
                return JMod.FINAL;
            case NATIVE:
                return JMod.NATIVE;
            case PRIVATE:
                return JMod.PRIVATE;
            case PROTECTED:
                return JMod.PROTECTED;
            case PUBLIC:
                return JMod.PUBLIC;
            case STATIC:
                return JMod.STATIC;
            case SYNCHRONIZED:
                return JMod.SYNCHRONIZED;
            case TRANSIENT:
                return JMod.TRANSIENT;
            case VOLATILE:
                return JMod.VOLATILE;
            default:
                throw new UnsupportedOperationException("Unsupported modifier: " + modifier);
        }
    }

    private static ClassType toClassType(ElementKind kind) {
        switch (kind) {
            case CLASS:
                return ClassType.CLASS;
            case ENUM:
                return ClassType.ENUM;
            case INTERFACE:
                return ClassType.INTERFACE;
            case ANNOTATION_TYPE:
                return ClassType.ANNOTATION_TYPE_DECL;
            default:
                throw new UnsupportedOperationException("Unsupported ElementKind: " + kind);
        }
    }

    public ValueClassModelBuilder(JCodeModel codeModel) {
        this.codeModel = codeModel;
    }

    public ValueClassModel build(Element visitorElement, ValueVisitor dataVisitor) throws SourceException, CodeGenerationException {
        ValueVisitorInterfaceModel visitorInterfaceModel = buildVisitorInterface(visitorElement, dataVisitor);
        ValueClassModel result = build(visitorInterfaceModel);
        return result;
    }

    private ValueVisitorInterfaceModel buildVisitorInterface(Element visitorElement, ValueVisitor dataVisitor) throws SourceException, CodeGenerationException {
        return buildVisitorInterface(toTypeElement(visitorElement), dataVisitor);
    }

    private ValueVisitorInterfaceModel buildVisitorInterface(TypeElement visitorElement, ValueVisitor dataVisitor) throws SourceException, CodeGenerationException {
        try {
            JDefinedClass visitorInterfaceModel = createJDefinedClass(visitorElement);
            for (Element element: visitorElement.getEnclosedElements()) {
                if (element.getKind().equals(ElementKind.METHOD)) {
                    ExecutableElement executable = (ExecutableElement)element;
                    JMethod method = visitorInterfaceModel.method(toJMod(executable.getModifiers()), toJType(executable.getReturnType()), executable.getSimpleName().toString());
                    for (TypeParameterElement parameter: executable.getTypeParameters()) {
                        JTypeVar typeVariable = method.generify(parameter.getSimpleName().toString());
                        for (TypeMirror type: parameter.getBounds()) {
                            typeVariable.bound((JClass)toJType(type));
                        }
                    }
                    for (TypeMirror type: executable.getThrownTypes()) {
                        JClass throwable = (JClass)toJType(type);
                        method._throws(throwable);
                    }

                    for (VariableElement variable: executable.getParameters()) {
                        JVar param = method.param(toJMod(variable.getModifiers()), toJType(variable.asType()), variable.getSimpleName().toString());
                        for (AnnotationMirror annotation: variable.getAnnotationMirrors()) {
                            param.annotate((JClass)toJType(annotation.getAnnotationType()));
                        }
                    }
                }
            }
            return new ValueVisitorInterfaceModel(visitorInterfaceModel, dataVisitor);
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
    }

    ValueClassModel build(ValueVisitorInterfaceModel visitorInterface) throws SourceException, CodeGenerationException {
        return ValueClassModel.createInstance(codeModel, visitorInterface);
    }

    private JDefinedClass createJDefinedClass(TypeElement element) throws JClassAlreadyExistsException {
        ClassType classType = toClassType(element.getKind());
        int modifiers = toJMod(element.getModifiers());
        if (classType.equals(ClassType.INTERFACE))
            modifiers = modifiers & ~JMod.ABSTRACT;

        JDefinedClass newClass = codeModel._class(modifiers, element.getQualifiedName().toString(), classType);
        newClass.hide();
        for (TypeParameterElement parameter: element.getTypeParameters()) {
            JTypeVar typeVariable = newClass.generify(parameter.getSimpleName().toString());
            for (TypeMirror type: parameter.getBounds()) {
                typeVariable.bound((JClass)toJType(type));
            }
        }
        return newClass;
    }

    private JClass toJClass(TypeElement element) throws CodeGenerationException {
        try {
            Class<?> klass = Class.forName(element.getQualifiedName().toString());
            JType declaredClass = codeModel._ref(klass);
            return (JClass)declaredClass;
        } catch (ClassNotFoundException ex) {
            throw new CodeGenerationException(ex);
        }
    }

    private JType toJType(TypeMirror type) {
        return type.accept(new TypeVisitor<JType, Void>() {
            @Override
            public JType visit(TypeMirror t, Void p) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JType visit(TypeMirror t) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JType visitPrimitive(PrimitiveType t, Void p) {
                switch (t.getKind()) {
                    case BOOLEAN:
                        return codeModel.BOOLEAN;
                    case BYTE:
                        return codeModel.BYTE;
                    case CHAR:
                        return codeModel.CHAR;
                    case INT:
                        return codeModel.INT;
                    case LONG:
                        return codeModel.LONG;
                    case FLOAT:
                        return codeModel.FLOAT;
                    case DOUBLE:
                        return codeModel.DOUBLE;
                    case SHORT:
                        return codeModel.SHORT;
                    default:
                        throw new IllegalArgumentException("Unrecognized primitive " + t.getKind());
                }
            }

            @Override
            public JType visitNull(NullType t, Void p) {
                throw new IllegalArgumentException("null can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JType visitArray(ArrayType t, Void p) {
                JType componentType = toJType(t.getComponentType());
                return componentType.array();
            }

            @Override
            public JType visitDeclared(DeclaredType t, Void p) {
                try {
                    TypeElement element = (TypeElement)t.asElement();
                    JClass _class = toJClass(element);
                    for (TypeMirror typeArgument: t.getTypeArguments()) {
                        _class = _class.narrow(toJType(typeArgument));
                    }
                    return _class;
                } catch (CodeGenerationException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public JType visitError(ErrorType t, Void p) {
                throw new IllegalArgumentException("error can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JType visitTypeVariable(TypeVariable t, Void p) {
                return codeModel.directClass(t.asElement().getSimpleName().toString());
            }

            @Override
            public JType visitWildcard(WildcardType t, Void p) {
                throw new UnsupportedOperationException("wildcards are not supported in convertion to JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JType visitExecutable(ExecutableType t, Void p) {
                throw new IllegalArgumentException("executable can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JType visitNoType(NoType t, Void p) {
                throw new IllegalArgumentException("'no type' can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JType visitUnknown(TypeMirror t, Void p) {
                throw new IllegalArgumentException("unknown can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JType visitUnion(UnionType t, Void p) {
                throw new IllegalArgumentException("union type can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }
        }, null);
    }

}
