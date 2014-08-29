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

import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;
import java.util.Collection;
import java.util.List;
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
import javax.lang.model.type.TypeKind;
import static javax.lang.model.type.TypeKind.CHAR;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor6;

class ValueClassModelBuilder {
    private static TypeElement toTypeElement(Element element) throws SourceException {
        if (!(element instanceof TypeElement))
            throw new SourceException("DataVisitor annotation is only allowed to interfaces");
        else
            return (TypeElement)element;
    }
    private final JCodeModel codeModel;

    private final List<TypeElement> generatedRootTypes;

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

    private static EClassType toClassType(ElementKind kind) {
        switch (kind) {
            case CLASS:
                return EClassType.CLASS;
            case ENUM:
                return EClassType.ENUM;
            case INTERFACE:
                return EClassType.INTERFACE;
            case ANNOTATION_TYPE:
                return EClassType.ANNOTATION_TYPE_DECL;
            default:
                throw new UnsupportedOperationException("Unsupported ElementKind: " + kind);
        }
    }

    public ValueClassModelBuilder(JCodeModel codeModel, List<TypeElement> generatedRootTypes) {
        this.codeModel = codeModel;
        this.generatedRootTypes = generatedRootTypes;
    }

    public ValueClassModel build(Element visitorElement, GenerateValueClassForVisitor dataVisitor) throws SourceException, CodeGenerationException, ErrorTypeFound {
        ValueVisitorInterfaceModel visitorInterfaceModel = buildVisitorInterface(visitorElement, dataVisitor);
        ValueClassModel result = build(visitorInterfaceModel);
        return result;
    }

    private ValueVisitorInterfaceModel buildVisitorInterface(Element visitorElement, GenerateValueClassForVisitor dataVisitor) throws SourceException, CodeGenerationException, ErrorTypeFound {
        return buildVisitorInterface(toTypeElement(visitorElement), dataVisitor);
    }

    private ValueVisitorInterfaceModel buildVisitorInterface(TypeElement visitorElement, GenerateValueClassForVisitor dataVisitor) throws SourceException, CodeGenerationException, ErrorTypeFound {
        try {
            JDefinedClass visitorInterfaceModel = createJDefinedClass(visitorElement);
            return new ValueVisitorInterfaceModel(visitorInterfaceModel, dataVisitor);
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
    }

    ValueClassModel build(ValueVisitorInterfaceModel visitorInterface) throws SourceException, CodeGenerationException {
        return ValueClassModel.createInstance(codeModel, visitorInterface);
    }

    private JDefinedClass createJDefinedClass(TypeElement element) throws JClassAlreadyExistsException, SourceException, ErrorTypeFound, CodeGenerationException {
        EClassType classType = toClassType(element.getKind());
        int modifiers = toJMod(element.getModifiers());
        if (classType.equals(EClassType.INTERFACE))
            modifiers = modifiers & ~JMod.ABSTRACT;

        JDefinedClass newClass = codeModel._class(modifiers, element.getQualifiedName().toString(), classType);
        newClass.hide();
        for (TypeParameterElement parameter: element.getTypeParameters()) {
            JTypeVar typeVariable = newClass.generify(parameter.getSimpleName().toString());
            for (TypeMirror type: parameter.getBounds()) {
                typeVariable.bound((AbstractJClass)toJType(type));
            }
        }
        TypeMirror superclass = element.getSuperclass();
        if (superclass != null && superclass.getKind() != TypeKind.NONE) {
            newClass._extends((AbstractJClass)toJType(superclass));
        }
        for (TypeMirror iface: element.getInterfaces()) {
            newClass._implements((AbstractJClass)toJType(iface));
        }
        for (Element enclosedElement: element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.METHOD)) {
                ExecutableElement executable = (ExecutableElement)enclosedElement;
                JMethod method = newClass.method(toJMod(executable.getModifiers()), toJType(executable.getReturnType()), executable.getSimpleName().toString());
                for (TypeParameterElement parameter: executable.getTypeParameters()) {
                    JTypeVar typeVariable = method.generify(parameter.getSimpleName().toString());
                    for (TypeMirror type: parameter.getBounds()) {
                        typeVariable.bound((AbstractJClass)toJType(type));
                    }
                }
                for (TypeMirror type: executable.getThrownTypes()) {
                    AbstractJClass throwable = (AbstractJClass)toJType(type);
                    method._throws(throwable);
                }

                for (VariableElement variable: executable.getParameters()) {
                    String parameterName = variable.getSimpleName().toString();
                    TypeMirror parameterTypeMirror = variable.asType();

                    AbstractJType parameterType = toJType(parameterTypeMirror);
                    JVar param = method.param(toJMod(variable.getModifiers()), parameterType, parameterName);
                    for (AnnotationMirror annotation: variable.getAnnotationMirrors()) {
                        param.annotate((AbstractJClass)toJType(annotation.getAnnotationType()));
                    }
                }
            }
        }
        return newClass;
    }

    private AbstractJClass toJClass(TypeElement element) throws CodeGenerationException, SourceException, ErrorTypeFound {
        try {
            Class<?> klass = Class.forName(element.getQualifiedName().toString());
            AbstractJType declaredClass = codeModel._ref(klass);
            return (AbstractJClass)declaredClass;
        } catch (ClassNotFoundException ex) {
            try {
                AbstractJClass result = codeModel._getClass(element.getQualifiedName().toString());
                if (result == null)
                    result = createJDefinedClass(element);
                return result;
            } catch (JClassAlreadyExistsException ex1) {
                throw new RuntimeException(ex1);
            }
        }
    }

    private AbstractJType toJType(TypeMirror type) throws ErrorTypeFound, SourceException, CodeGenerationException {
        try {
            return type.accept(new AbstractTypeVisitor6<AbstractJType, Void>() {

                @Override
                public AbstractJType visitPrimitive(PrimitiveType t, Void p) {
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
                public AbstractJType visitNull(NullType t, Void p) {
                    throw new IllegalArgumentException("null can't be JClass."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public AbstractJType visitArray(ArrayType t, Void p) {
                    try {
                        AbstractJType componentType = toJType(t.getComponentType());
                        return componentType.array();
                    } catch (ErrorTypeFound ex) {
                        throw new RuntimeErrorTypeFound(ex);
                    } catch (CodeGenerationException ex) {
                        throw new RuntimeCodeGenerationException(ex);
                    } catch (SourceException ex) {
                        throw new RuntimeSourceException(ex);
                    }
                }

                @Override
                public AbstractJType visitDeclared(DeclaredType t, Void p) {
                    try {
                        TypeElement element = (TypeElement)t.asElement();
                        AbstractJClass _class = toJClass(element);
                        for (TypeMirror typeArgument: t.getTypeArguments()) {
                            _class = _class.narrow(toJType(typeArgument));
                        }
                        return _class;
                    } catch (CodeGenerationException ex) {
                        throw new RuntimeCodeGenerationException(ex);
                    } catch (SourceException ex) {
                        throw new RuntimeSourceException(ex);
                    } catch (ErrorTypeFound ex) {
                        throw new RuntimeErrorTypeFound(ex);
                    }
                }

                @Override
                public AbstractJType visitError(ErrorType t, Void p) {
                    // We heuristicaly search among types generated by previous rounds:
                    String partialTypeName = t.toString();
                    if (partialTypeName != null && !partialTypeName.isEmpty()) {
                        final String dot = ".";
                        if (!partialTypeName.contains(dot)) {
                            // prepend '.' to avoid potential mismatches
                            partialTypeName = dot.concat(partialTypeName);
                        }
                        for (TypeElement generatedType: generatedRootTypes) {
                            if (generatedType.getQualifiedName().toString().endsWith(partialTypeName)) {
                                try {
                                    return toJClass(generatedType);
                                } catch (CodeGenerationException ex) {
                                    throw new RuntimeCodeGenerationException(ex);
                                } catch (SourceException ex) {
                                    throw new RuntimeSourceException(ex);
                                } catch (ErrorTypeFound ex) {
                                    throw new RuntimeErrorTypeFound(ex);
                                }
                            }
                        }
                    }
                    throw new RuntimeErrorTypeFound(new ErrorTypeFound());
                }

                @Override
                public AbstractJType visitTypeVariable(TypeVariable t, Void p) {
                    return codeModel.ref(t.asElement().getSimpleName().toString());
                }

                @Override
                public AbstractJType visitWildcard(WildcardType t, Void p) {
                    throw new UnsupportedOperationException("wildcards are not supported in convertion to JClass."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public AbstractJType visitExecutable(ExecutableType t, Void p) {
                    throw new IllegalArgumentException("executable can't be JClass."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public AbstractJType visitNoType(NoType t, Void p) {
                    throw new IllegalArgumentException("'no type' can't be JClass."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public AbstractJType visitUnknown(TypeMirror t, Void p) {
                    throw new IllegalArgumentException("unknown can't be JClass."); //To change body of generated methods, choose Tools | Templates.
                }
            }, null);
        } catch (RuntimeErrorTypeFound ex) {
            throw ex.getCause();
        } catch (RuntimeSourceException ex) {
            throw ex.getCause();
        } catch (RuntimeCodeGenerationException ex) {
            throw ex.getCause();
        }
    }
}
