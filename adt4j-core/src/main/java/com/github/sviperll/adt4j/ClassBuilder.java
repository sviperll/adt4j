/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTypeVar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class ClassBuilder {
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

    public ClassBuilder(JCodeModel codeModel) {
        this.codeModel = codeModel;
    }

    public DefinedClass build(Element visitorElement, DataVisitor dataVisitor) throws SourceException, CodeGenerationException {
        return build(buildVisitorInterface(visitorElement, dataVisitor));
    }

    private DefinedVisitorInterface buildVisitorInterface(Element visitorElement, DataVisitor dataVisitor) throws SourceException, CodeGenerationException {
        return buildVisitorInterface(toTypeElement(visitorElement), dataVisitor);
    }

    private DefinedVisitorInterface buildVisitorInterface(TypeElement visitorElement, DataVisitor dataVisitor) throws SourceException, CodeGenerationException {
        try {
            JDefinedClass visitorInterfaceModel = createJDefinedClass(visitorElement);
            for (Element element: visitorElement.getEnclosedElements()) {
                if (element.getKind().equals(ElementKind.METHOD)) {
                    ExecutableElement executable = (ExecutableElement)element;
                    JMethod method = visitorInterfaceModel.method(toJMod(executable.getModifiers()), toJClass(executable.getReturnType()), executable.getSimpleName().toString());
                    for (TypeParameterElement parameter: executable.getTypeParameters()) {
                        JTypeVar typeVariable = method.generify(parameter.getSimpleName().toString());
                        for (TypeMirror type: parameter.getBounds()) {
                            typeVariable.bound(toJClass(type));
                        }
                    }
                    for (TypeMirror type: executable.getThrownTypes()) {
                        JClass throwable = toJClass(type);
                        method._throws(throwable);
                    }

                    for (VariableElement variable: executable.getParameters()) {
                        method.param(toJMod(variable.getModifiers()), toJClass(variable.asType()), variable.getSimpleName().toString());
                    }
                }
            }
            return new DefinedVisitorInterface(visitorInterfaceModel, dataVisitor);
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
    }

    DefinedClass build(DefinedVisitorInterface visitorInterface) throws SourceException, CodeGenerationException {
        return DefinedClass.createInstance(codeModel, visitorInterface);
    }

    private JDefinedClass createJDefinedClass(TypeElement element) throws JClassAlreadyExistsException {
        ClassType classType = toClassType(element.getKind());
        int modifiers = toJMod(element.getModifiers());
        if (classType.equals(ClassType.INTERFACE))
            modifiers = modifiers & ~JMod.ABSTRACT;

        JDefinedClass newClass = codeModel._class(modifiers, element.getQualifiedName().toString(), classType);
        for (TypeParameterElement parameter: element.getTypeParameters()) {
            JTypeVar typeVariable = newClass.generify(parameter.getSimpleName().toString());
            for (TypeMirror type: parameter.getBounds()) {
                typeVariable.bound(toJClass(type));
            }
        }
        return newClass;
    }

    private JClass toJClass(TypeElement element) throws CodeGenerationException {
        try {
            JClass declaredClass = codeModel._getClass(element.getQualifiedName().toString());
            if (declaredClass != null) {
                return declaredClass;
            } else {
                return createJDefinedClass(element);
            }
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
    }

    private JClass toJClass(TypeMirror type) {
        return type.accept(new TypeVisitor<JClass, Void>() {
            @Override
            public JClass visit(TypeMirror t, Void p) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JClass visit(TypeMirror t) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JClass visitPrimitive(PrimitiveType t, Void p) {
                throw new IllegalArgumentException("Primitive can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JClass visitNull(NullType t, Void p) {
                throw new IllegalArgumentException("null can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JClass visitArray(ArrayType t, Void p) {
                JClass componentType = toJClass(t.getComponentType());
                return componentType.array();
            }

            @Override
            public JClass visitDeclared(DeclaredType t, Void p) {
                try {
                    TypeElement element = (TypeElement)t.asElement();
                    JClass _class = toJClass(element);
                    List<JClass> typeParameters = new ArrayList<>();
                    for (TypeMirror typeArgument: t.getTypeArguments()) {
                        typeParameters.add(toJClass(typeArgument));
                    }
                    _class.narrow(typeParameters);
                    return _class;
                } catch (CodeGenerationException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public JClass visitError(ErrorType t, Void p) {
                throw new IllegalArgumentException("error can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JClass visitTypeVariable(TypeVariable t, Void p) {
                return codeModel.directClass(t.asElement().getSimpleName().toString());
            }

            @Override
            public JClass visitWildcard(WildcardType t, Void p) {
                throw new UnsupportedOperationException("wildcards are not supported in convertion to JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JClass visitExecutable(ExecutableType t, Void p) {
                throw new IllegalArgumentException("executable can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JClass visitNoType(NoType t, Void p) {
                throw new IllegalArgumentException("'no type' can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JClass visitUnknown(TypeMirror t, Void p) {
                throw new IllegalArgumentException("unknown can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public JClass visitUnion(UnionType t, Void p) {
                throw new IllegalArgumentException("union type can't be JClass."); //To change body of generated methods, choose Tools | Templates.
            }
        }, null);
    }

}
