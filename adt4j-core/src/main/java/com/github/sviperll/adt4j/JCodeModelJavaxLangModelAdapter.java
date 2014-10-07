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

import com.github.sviperll.adt4j.model.RuntimeSourceException;
import com.github.sviperll.adt4j.model.RuntimeErrorTypeFound;
import com.github.sviperll.adt4j.model.RuntimeCodeGenerationException;
import com.github.sviperll.adt4j.model.CodeGenerationException;
import com.github.sviperll.adt4j.model.ErrorTypeFound;
import com.github.sviperll.adt4j.model.SourceException;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.IJAnnotatable;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JTypeWildcard;
import com.helger.jcodemodel.JVar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor6;

class JCodeModelJavaxLangModelAdapter {
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

    private final JCodeModel codeModel;

    JCodeModelJavaxLangModelAdapter(JCodeModel codeModel) {
        this.codeModel = codeModel;
    }

    JDefinedClass _class(TypeElement element) throws JClassAlreadyExistsException, SourceException, ErrorTypeFound, CodeGenerationException {
        return _class(element, new TypeEnvironment());
    }

    private JDefinedClass _class(TypeElement element, TypeEnvironment environment) throws JClassAlreadyExistsException, SourceException, ErrorTypeFound, CodeGenerationException {
        EClassType classType = toClassType(element.getKind());
        int modifiers = toJMod(element.getModifiers());
        if (classType.equals(EClassType.INTERFACE))
            modifiers = modifiers & ~JMod.ABSTRACT;

        JDefinedClass newClass = codeModel._class(modifiers, element.getQualifiedName().toString(), classType);
        newClass.hide();
        Annotator classAnnotator = new Annotator(newClass, environment);
        classAnnotator.annotate(element.getAnnotationMirrors());
        for (TypeParameterElement parameter: element.getTypeParameters()) {
            JTypeVar typeVariable = newClass.generify(parameter.getSimpleName().toString());
            environment.put(typeVariable.name(), typeVariable);
            for (TypeMirror type: parameter.getBounds()) {
                typeVariable.bound((AbstractJClass)toJType(type, environment));
            }
        }
        TypeMirror superclass = element.getSuperclass();
        if (superclass != null && superclass.getKind() != TypeKind.NONE) {
            newClass._extends((AbstractJClass)toJType(superclass, environment));
        }
        for (TypeMirror iface: element.getInterfaces()) {
            newClass._implements((AbstractJClass)toJType(iface, environment));
        }
        for (Element enclosedElement: element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.METHOD)) {
                ExecutableElement executable = (ExecutableElement)enclosedElement;
                JMethod method = newClass.method(toJMod(executable.getModifiers()), codeModel.VOID, executable.getSimpleName().toString());
                TypeEnvironment methodEnvironment = environment.enclosed();
                Annotator methodAnnotator = new Annotator(method, environment);
                methodAnnotator.annotate(executable.getAnnotationMirrors());
                for (TypeParameterElement parameter: executable.getTypeParameters()) {
                    JTypeVar typeVariable = method.generify(parameter.getSimpleName().toString());
                    methodEnvironment.put(typeVariable.name(), typeVariable);
                    for (TypeMirror type: parameter.getBounds()) {
                        typeVariable.bound((AbstractJClass)toJType(type, methodEnvironment));
                    }
                }
                method.type(toJType(executable.getReturnType(), methodEnvironment));
                for (TypeMirror type: executable.getThrownTypes()) {
                    AbstractJClass throwable = (AbstractJClass)toJType(type, methodEnvironment);
                    method._throws(throwable);
                }

                for (VariableElement variable: executable.getParameters()) {
                    String parameterName = variable.getSimpleName().toString();
                    TypeMirror parameterTypeMirror = variable.asType();

                    AbstractJType parameterType = toJType(parameterTypeMirror, methodEnvironment);
                    JVar param = method.param(toJMod(variable.getModifiers()), parameterType, parameterName);
                    Annotator parametorAnnotator = new Annotator(param, methodEnvironment);
                    parametorAnnotator.annotate(variable.getAnnotationMirrors());
                }
            }
        }
        return newClass;
    }

    AbstractJClass ref(TypeElement element) throws CodeGenerationException, SourceException, ErrorTypeFound {
        try {
            Class<?> klass = Class.forName(element.getQualifiedName().toString());
            AbstractJType declaredClass = codeModel.ref(klass);
            return (AbstractJClass)declaredClass;
        } catch (ClassNotFoundException ex) {
            try {
                AbstractJClass result = codeModel._getClass(element.getQualifiedName().toString());
                if (result != null)
                    return result;
                else {
                    JDefinedClass jclass = _class(element, new TypeEnvironment());
                    jclass.hide();
                    return jclass;
                }
            } catch (JClassAlreadyExistsException ex1) {
                throw new RuntimeException(ex1);
            }
        }
    }

    private AbstractJType toJType(TypeMirror type, final TypeEnvironment environment) throws ErrorTypeFound, SourceException, CodeGenerationException {
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
                        AbstractJType componentType = toJType(t.getComponentType(), environment);
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
                        AbstractJClass _class = ref(element);
                        for (TypeMirror typeArgument: t.getTypeArguments()) {
                            _class = _class.narrow(toJType(typeArgument, environment));
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
                    try {
                        throw new ErrorTypeFound();
                    } catch (ErrorTypeFound ex) {
                        throw new RuntimeErrorTypeFound(ex);
                    }
                }

                @Override
                public AbstractJType visitTypeVariable(TypeVariable t, Void p) {
                    return environment.get(t.asElement().getSimpleName().toString());
                }

                @Override
                public AbstractJType visitWildcard(WildcardType t, Void p) {
                    try {
                        TypeMirror extendsBoundMirror = t.getExtendsBound();
                        if (extendsBoundMirror != null) {
                            AbstractJClass extendsBound = (AbstractJClass)toJType(extendsBoundMirror, environment);
                            return extendsBound.wildcard(JTypeWildcard.EBoundMode.EXTENDS);
                        }
                        TypeMirror superBoundMirror = t.getSuperBound();
                        if (superBoundMirror != null) {
                            AbstractJClass superBound = (AbstractJClass)toJType(superBoundMirror, environment);
                            return superBound.wildcard(JTypeWildcard.EBoundMode.SUPER);
                        }
                        return codeModel.wildcard();
                    } catch (CodeGenerationException ex) {
                        throw new RuntimeCodeGenerationException(ex);
                    } catch (SourceException ex) {
                        throw new RuntimeSourceException(ex);
                    } catch (ErrorTypeFound ex) {
                        throw new RuntimeErrorTypeFound(ex);
                    }
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

    private class Annotator {
        private final IJAnnotatable annotatable;
        private final TypeEnvironment typeEnvironment;
        public Annotator(IJAnnotatable annotatable, TypeEnvironment typeEnvironment) {
            this.annotatable = annotatable;
            this.typeEnvironment = typeEnvironment;
        }

        private void annotate(List<? extends AnnotationMirror> annotationMirrors) throws ErrorTypeFound, SourceException, CodeGenerationException {
            for (AnnotationMirror annotation: annotationMirrors) {
                JAnnotationUse annotationUse = annotatable.annotate((AbstractJClass)toJType(annotation.getAnnotationType(), typeEnvironment));
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationValueAssignment: annotation.getElementValues().entrySet()) {
                    String name = annotationValueAssignment.getKey().getSimpleName().toString();
                    Object value = annotationValueAssignment.getValue().getValue();
                    if (value instanceof String)
                        annotationUse.param(name, (String)value);
                    else if (value instanceof Integer)
                        annotationUse.param(name, (Integer)value);
                    else if (value instanceof Long)
                        annotationUse.param(name, (Long)value);
                    else if (value instanceof Short)
                        annotationUse.param(name, (Short)value);
                    else if (value instanceof Float)
                        annotationUse.param(name, (Float)value);
                    else if (value instanceof Double)
                        annotationUse.param(name, (Double)value);
                    else if (value instanceof Byte)
                        annotationUse.param(name, (Byte)value);
                    else if (value instanceof Character)
                        annotationUse.param(name, (Character)value);
                    else if (value instanceof Class)
                        annotationUse.param(name, (Class)value);
                    else if (value instanceof Enum)
                        annotationUse.param(name, (Enum)value);
                    else if (value instanceof String[])
                        annotationUse.paramArray(name, (String[])value);
                    else if (value instanceof int[])
                        annotationUse.paramArray(name, (int[])value);
                    else if (value instanceof long[])
                        annotationUse.paramArray(name, (long[])value);
                    else if (value instanceof short[])
                        annotationUse.paramArray(name, (short[])value);
                    else if (value instanceof float[])
                        annotationUse.paramArray(name, (float[])value);
                    else if (value instanceof double[])
                        annotationUse.paramArray(name, (double[])value);
                    else if (value instanceof byte[])
                        annotationUse.paramArray(name, (byte[])value);
                    else if (value instanceof char[])
                        annotationUse.paramArray(name, (char[])value);
                    else if (value instanceof Class[])
                        annotationUse.paramArray(name, (Class[])value);
                    else if (value instanceof Enum[])
                        annotationUse.paramArray(name, (Enum[])value);
                }
            }
        }
    }
}
