/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JTypeVar;
import com.sun.codemodel.JVar;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class DefinedClass {
    private final JDefinedClass definedClass;
    private final DefinedVisitorInterface visitorInterface;
    private final JClass usedDataType;

    DefinedClass(JDefinedClass definedClass, DefinedVisitorInterface visitorInterface, JClass usedDataType) {
        this.definedClass = definedClass;
        this.visitorInterface = visitorInterface;
        this.usedDataType = usedDataType;
    }

    String getQualifiedName() {
        return definedClass.fullName();
    }

    JCodeModel getCodeModel() {
        return definedClass.owner();
    }

    void buildAcceptMethod() {
        JMethod acceptMethod = definedClass.method(JMod.ABSTRACT | JMod.PUBLIC, definedClass.owner().VOID, "accept");

        JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
        JTypeVar resultType = acceptMethod.generify(visitorResultType.name());
        resultType.bound(visitorResultType._extends());
        acceptMethod.type(resultType);

        JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            exceptionType = acceptMethod.generify(visitorExceptionType.name());
            exceptionType.bound(visitorExceptionType._extends());
            acceptMethod._throws(exceptionType);
        }

        JClass usedVisitorType = visitorInterface.narrowed(usedDataType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
    }

    void buildAcceptRecursiveMethod() {
        JMethod acceptRecursiveMethod = definedClass.method(JMod.PUBLIC, definedClass.owner().VOID, "acceptRecursive");

        JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
        JTypeVar resultType = acceptRecursiveMethod.generify(visitorResultType.name());
        resultType.bound(visitorResultType._extends());
        acceptRecursiveMethod.type(resultType);

        JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            exceptionType = acceptRecursiveMethod.generify(visitorExceptionType.name());
            exceptionType.bound(visitorExceptionType._extends());
            acceptRecursiveMethod._throws(exceptionType);
        }

        JClass usedVisitorType = visitorInterface.narrowedForSelf(usedDataType, resultType, exceptionType, resultType);
        JVar visitorParam = acceptRecursiveMethod.param(JMod.FINAL, usedVisitorType, "visitor");

        JDefinedClass anonymousClass = definedClass.owner().anonymousClass(visitorInterface.narrowed(usedDataType, resultType, exceptionType));
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod adaptorMethod = anonymousClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT, resultType, interfaceMethod.name());
            adaptorMethod.annotate(Override.class);

            if (visitorExceptionType != null) {
                adaptorMethod._throws(exceptionType);
            }

            JInvocation invocation = JExpr.ref(visitorParam.name()).invoke(adaptorMethod.name());
            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.narrowed(param.type(), usedDataType, resultType, exceptionType);
                adaptorMethod.param(param.mods().getValue() | JMod.FINAL, paramType, param.name());

                JType outerVisitorParamType = visitorInterface.narrowed(param.type(), resultType, resultType, exceptionType);

                if (paramType.equals(outerVisitorParamType))
                    invocation.arg(JExpr.ref(param.name()));
                else {
                    JInvocation argument = param.invoke("acceptRecursive");
                    argument.arg(JExpr.ref("visitor"));
                    invocation.arg(argument);
                }
            }
            adaptorMethod.body()._return(invocation);
        }
        JInvocation acceptInvocation = JExpr._this().invoke("accept");
        acceptInvocation.arg(JExpr._new(anonymousClass));
        acceptRecursiveMethod.body()._return(acceptInvocation);
    }

    JMethod buildFactoryInstanceGetter(JDefinedClass factory) {
        JFieldVar factoryField = definedClass.field(JMod.PRIVATE | JMod.STATIC, factory, "FACTORY");
        factoryField.init(JExpr._new(factory));
        JMethod factoryMethod = definedClass.method(JMod.PUBLIC | JMod.STATIC, definedClass.owner().VOID, "factory");
        JAnnotationUse annotationUse = factoryMethod.annotate(SuppressWarnings.class);
        annotationUse.param("value", "unchecked");
        List<JClass> typeArguments = new ArrayList<>();
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = factoryMethod.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
            typeArguments.add(typeParameter);
        }
        JClass staticUsedDataType = definedClass.narrow(typeArguments);
        JClass factoryUsedType = factory.narrow(typeArguments);
        factoryMethod.type(visitorInterface.narrowed(staticUsedDataType, staticUsedDataType, definedClass.owner().ref(RuntimeException.class)));
        factoryMethod.body()._return(JExpr.cast(factoryUsedType, JExpr.ref("FACTORY")));
        return factoryMethod;
    }

    JDefinedClass buildFactoryClass() throws JClassAlreadyExistsException {
        JClass runtimeException = definedClass.owner().ref(RuntimeException.class);
        JDefinedClass factoryClass = definedClass._class(JMod.PRIVATE | JMod.STATIC, definedClass.name() + "Factory", ClassType.CLASS);
        List<JClass> typeArguments = new ArrayList<>();
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = factoryClass.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
            typeArguments.add(typeParameter);
        }
        JClass staticUsedDataType = definedClass.narrow(typeArguments);
        factoryClass._implements(visitorInterface.narrowed(staticUsedDataType, staticUsedDataType, runtimeException));
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod factoryMethod = factoryClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT, staticUsedDataType, interfaceMethod.name());
            factoryMethod.annotate(Override.class);

            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.narrowed(param.type(), staticUsedDataType, staticUsedDataType, runtimeException);
                factoryMethod.param(param.mods().getValue() | JMod.FINAL, paramType, param.name());
            }


            if (!interfaceMethod.params().isEmpty()) {
                JDefinedClass anonymousClass = createFactoryAnonymousClass(interfaceMethod, staticUsedDataType);
                factoryMethod.body()._return(JExpr._new(anonymousClass));
            } else {
                JMethod singletonFactoryMethod = createSingletonFactoryMethod(interfaceMethod, factoryClass);
                JFieldVar singletonInstanceField = factoryClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL,
                                                                      staticUsedDataType.erasure(),
                                                                      factoryMethod.name().toUpperCase(),
                                                                      factoryClass.staticInvoke(singletonFactoryMethod.name()));
                JAnnotationUse annotationUse = factoryMethod.annotate(SuppressWarnings.class);
                annotationUse.param("value", "unchecked");
                factoryMethod.body()._return(JExpr.cast(staticUsedDataType, singletonInstanceField));
            }
        }
        return factoryClass;
    }

    private JMethod createSingletonFactoryMethod(JMethod interfaceMethod, JDefinedClass factoryClass) {
        int mods = interfaceMethod.mods().getValue() & ~JMod.ABSTRACT & ~JMod.PUBLIC & ~JMod.PROTECTED;
        mods = mods | JMod.STATIC | JMod.PRIVATE;
        JMethod result = factoryClass.method(mods,
                                                             definedClass.owner().VOID,
                                                             "init" + interfaceMethod.name());
        List<JClass> typeArguments = new ArrayList<>();
        for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
            JTypeVar typeParameter = result.generify(visitorTypeParameter.name());
            typeParameter.bound(visitorTypeParameter._extends());
            typeArguments.add(typeParameter);
        }
        JClass staticUsedDataType = definedClass.narrow(typeArguments);
        result.type(staticUsedDataType);
        JDefinedClass anonymousClass = createFactoryAnonymousClass(interfaceMethod, staticUsedDataType);
        result.body()._return(JExpr._new(anonymousClass));
        return result;
    }

    private JDefinedClass createFactoryAnonymousClass(JMethod interfaceMethod, JClass staticUsedDataType) {
        JDefinedClass anonymousClass = definedClass.owner().anonymousClass(staticUsedDataType);
        JMethod acceptMethod = anonymousClass.method(JMod.PUBLIC, definedClass.owner().VOID, "accept");
        acceptMethod.annotate(Override.class);

        JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
        JTypeVar resultType = acceptMethod.generify(visitorResultType.name());
        resultType.bound(visitorResultType._extends());
        acceptMethod.type(resultType);

        JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            exceptionType = acceptMethod.generify(visitorExceptionType.name());
            exceptionType.bound(visitorExceptionType._extends());
            acceptMethod._throws(exceptionType);
        }

        JClass usedVisitorType = visitorInterface.narrowed(staticUsedDataType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");
        JInvocation invocation = JExpr.invoke(JExpr.ref("visitor"), interfaceMethod.name());
        for (JVar param: interfaceMethod.params()) {
            invocation.arg(JExpr.ref(param.name()));
        }
        acceptMethod.body()._return(invocation);
        return anonymousClass;
    }

    void buildConstructorMethods(JMethod factoryInstanceGetterMethod) {
        for (JMethod interfaceMethod: visitorInterface.methods()) {
            JMethod constructorMethod = definedClass.method(interfaceMethod.mods().getValue() & ~JMod.ABSTRACT | JMod.STATIC, definedClass.owner().VOID, interfaceMethod.name());
            List<JClass> typeArguments = new ArrayList<>();
            for (JTypeVar visitorTypeParameter: visitorInterface.getDataTypeParameters()) {
                JTypeVar typeParameter = constructorMethod.generify(visitorTypeParameter.name());
                typeParameter.bound(visitorTypeParameter._extends());
                typeArguments.add(typeParameter);
            }
            JClass staticUsedDataType = definedClass.narrow(typeArguments);
            JClass runtimeException = definedClass.owner().ref(RuntimeException.class);
            constructorMethod.type(staticUsedDataType);
            for (JVar param: interfaceMethod.params()) {
                JType paramType = visitorInterface.narrowed(param.type(), staticUsedDataType, staticUsedDataType, runtimeException);
                constructorMethod.param(param.mods().getValue(), paramType, param.name());
            }
            // JExpression factoryExpression = JExpr.cast(factoryUsedType, JExpr.ref("FACTORY"));
            StringBuilder factoryMethodNameInInvokation = new StringBuilder();
            Iterator<JClass> typeArgumentsIterator = typeArguments.iterator();
            if (typeArgumentsIterator.hasNext()) {
                JClass typeArgument = typeArgumentsIterator.next();
                factoryMethodNameInInvokation.append("<");
                factoryMethodNameInInvokation.append(typeArgument.name());
                while (typeArgumentsIterator.hasNext()) {
                    typeArgument = typeArgumentsIterator.next();
                    factoryMethodNameInInvokation.append(", ");
                    factoryMethodNameInInvokation.append(typeArgument.name());
                }
                factoryMethodNameInInvokation.append(">");
            }
            factoryMethodNameInInvokation.append(factoryInstanceGetterMethod.name());

            JInvocation factoryExpression = definedClass.staticInvoke(factoryMethodNameInInvokation.toString());
            JInvocation invocation = JExpr.invoke(factoryExpression, interfaceMethod.name());
            for (JVar param: interfaceMethod.params()) {
                invocation.arg(JExpr.ref(param.name()));
            }
            constructorMethod.body()._return(invocation);
        }
    }

}
