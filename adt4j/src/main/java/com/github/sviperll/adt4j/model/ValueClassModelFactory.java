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
package com.github.sviperll.adt4j.model;

import com.github.sviperll.adt4j.GenerateValueClassForVisitor;
import com.github.sviperll.adt4j.GenerateValueClassForVisitorProcessor;
import com.github.sviperll.adt4j.model.util.Types;
import com.github.sviperll.adt4j.model.util.ValueVisitorInterfaceModel;
import com.github.sviperll.adt4j.Visitor;
import com.github.sviperll.adt4j.model.util.Source;
import com.github.sviperll.adt4j.model.util.SourceCodeValidationException;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;
import com.helger.jcodemodel.meta.CodeModelBuildingException;
import java.lang.annotation.Annotation;

import javax.annotation.Generated;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValueClassModelFactory {
    public static JDefinedClass createValueClass(JDefinedClass jVisitorModel, Visitor visitorAnnotation) throws SourceCodeValidationException, CodeModelBuildingException {
        JAnnotationUse annotation = null;
        for (JAnnotationUse anyAnnotation: jVisitorModel.annotations()) {
            if (anyAnnotation.getAnnotationClass().fullName().equals(GenerateValueClassForVisitor.class.getName()))
                annotation = anyAnnotation;
        }
        if (annotation == null)
            throw new IllegalStateException("ValueClassModelFactory can't be run for interface without " + GenerateValueClassForVisitor.class + " annotation");
        ValueVisitorInterfaceModel visitorModel = ValueVisitorInterfaceModel.createInstance(jVisitorModel, visitorAnnotation, annotation);
        ValueClassModelFactory factory = new ValueClassModelFactory(jVisitorModel._package());
        ValueClassModel valueClassModel = factory.createValueClass(visitorModel);
        return valueClassModel.getJDefinedClass();
    }

    private final JPackage jpackage;

    ValueClassModelFactory(JPackage jpackage) {
        this.jpackage = jpackage;
    }

    private JDefinedClass createAcceptingInterface(JDefinedClass valueClass,
                                                   ValueVisitorInterfaceModel visitorInterface,
                                                   Types types) throws JClassAlreadyExistsException {

        JDefinedClass acceptingInterface = valueClass._class(JMod.PUBLIC, valueClass.name() + "Acceptor", EClassType.INTERFACE);

        // Hack to overcome bug in codeModel. We want private interface!!! Not public.
        acceptingInterface.mods().setPrivate();

        for (JTypeVar visitorTypeParameter: visitorInterface.getValueTypeParameters()) {
            Types.generifyWithBoundsFrom(acceptingInterface, visitorTypeParameter.name(), visitorTypeParameter);
        }

        JMethod acceptMethod = acceptingInterface.method(JMod.PUBLIC, types._void, visitorInterface.acceptMethodName());

        JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
        JTypeVar resultType = Types.generifyWithBoundsFrom(acceptMethod, visitorResultType.name(), visitorResultType);
        acceptMethod.type(resultType);

        JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            exceptionType = Types.generifyWithBoundsFrom(acceptMethod, visitorExceptionType.name(), visitorExceptionType);
            acceptMethod._throws(exceptionType);
        }

        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        AbstractJClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");

        return acceptingInterface;
    }

    ValueClassModel createValueClass(ValueVisitorInterfaceModel visitorInterface) throws SourceCodeValidationException, CodeModelBuildingException {
        try {
            Types types = Types.createInstance(jpackage.owner());
            if (visitorInterface.isValueClassSerializable()) {
                for (JMethod interfaceMethod: visitorInterface.methods()) {
                    for (JVar param: interfaceMethod.params()) {
                        AbstractJType type = param.type();
                        if (!type.isError() && !visitorInterface.isSelf(type) && !types.isSerializable(type))
                            throw new SourceCodeValidationException("Value class can't be serializable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not serializable");
                    }
                    JVar param = interfaceMethod.varParam();
                    if (param != null) {
                        AbstractJType type = param.type();
                        if (!type.isError() && !visitorInterface.isSelf(type) && !types.isSerializable(type))
                            throw new SourceCodeValidationException("Value class can't be serializable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not serializable");
                    }
                }
            }

            if (visitorInterface.isValueClassComparable()) {
                for (JMethod interfaceMethod: visitorInterface.methods()) {
                    for (JVar param: interfaceMethod.params()) {
                        AbstractJType type = param.type();
                        if (!type.isError() && !visitorInterface.isSelf(type) && !types.isComparable(type))
                            throw new SourceCodeValidationException("Value class can't be comparable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not comparable");
                    }
                    JVar param = interfaceMethod.varParam();
                    if (param != null) {
                        AbstractJType type = param.type();
                        if (!type.isError() && !visitorInterface.isSelf(type) && !types.isComparable(type))
                            throw new SourceCodeValidationException("Value class can't be comparable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not comparable");
                    }
                }
            }

            int mods = visitorInterface.isValueClassPublic() ? JMod.PUBLIC: JMod.NONE;
            JDefinedClass valueClass = jpackage._class(mods, visitorInterface.valueClassName(), EClassType.CLASS);
            JAnnotationUse generatedAnnotation = valueClass.annotate(Generated.class);
            generatedAnnotation.param("value", GenerateValueClassForVisitorProcessor.class.getName());
            Source.annotateParametersAreNonnullByDefault(valueClass);
            for (JTypeVar visitorTypeParameter: visitorInterface.getValueTypeParameters()) {
                Types.generifyWithBoundsFrom(valueClass, visitorTypeParameter.name(), visitorTypeParameter);
            }
            for (AbstractJClass iface: visitorInterface.implementsInterfaces()) {
                valueClass._implements(iface.typeParams().length == 0 ? iface : iface.narrow(valueClass.typeParams()));
            }
            AbstractJClass extendsClass = visitorInterface.valueClassExtends();
            valueClass._extends(extendsClass.typeParams().length == 0 ? extendsClass : extendsClass.narrow(valueClass.typeParams()));
            if (visitorInterface.isValueClassSerializable()) {
                valueClass._implements(types._Serializable);
                valueClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, types._long, "serialVersionUID", JExpr.lit(visitorInterface.serialVersionUIDForGeneratedCode()));
            }

            if (visitorInterface.isValueClassComparable()) {
                valueClass._implements(types._Comparable.narrow(valueClass.narrow(valueClass.typeParams())));
            }

            JDefinedClass acceptingInterface = createAcceptingInterface(valueClass, visitorInterface, types);
            if (visitorInterface.isValueClassSerializable()) {
                acceptingInterface._extends(types._Serializable);
            }

            ValueClassModel result = new ValueClassModel(valueClass, acceptingInterface, visitorInterface, types);
            ValueClassModel.MethodBuilder methodBuilder = result.createMethodBuilder(visitorInterface.serialization());
            Map<String, JMethod> constructorMethods = methodBuilder.buildConstructorMethods(visitorInterface.serialization());
            methodBuilder.buildPrivateConstructor();
            if (visitorInterface.isValueClassSerializable())
                methodBuilder.buildReadObjectMethod();
            methodBuilder.buildProtectedConstructor(visitorInterface.serialization());
            methodBuilder.buildAcceptMethod();
            Map<String, FieldConfiguration> gettersConfigutation = result.getGettersConfigutation();
            for (FieldConfiguration getter: gettersConfigutation.values()) {
                methodBuilder.generateGetter(getter);
            }
            Map<String, FieldConfiguration> updatersConfiguration = result.getUpdatersConfiguration();
            for (FieldConfiguration updater: updatersConfiguration.values()) {
                methodBuilder.generateUpdater(updater);
            }
            Map<String, PredicateConfigutation> predicates = result.getPredicates();
            for (Map.Entry<String, PredicateConfigutation> predicate: predicates.entrySet()) {
                methodBuilder.generatePredicate(predicate.getKey(), predicate.getValue());
            }
            if (visitorInterface.isValueClassComparable()) {
                methodBuilder.buildCompareTo();
            }
            methodBuilder.buildEqualsMethod();
            methodBuilder.buildHashCodeMethod(visitorInterface.hashCodeBase());
            methodBuilder.buildToStringMethod();
            result.buildFactory(constructorMethods);

            return result;
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeModelBuildingException(ex);
        }
    }


}
