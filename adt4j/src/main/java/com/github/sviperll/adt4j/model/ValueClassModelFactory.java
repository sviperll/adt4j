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

import com.github.sviperll.adt4j.AccessLevel;
import com.github.sviperll.adt4j.model.util.SourceException;
import com.github.sviperll.adt4j.GenerateValueClassForVisitor;
import com.github.sviperll.adt4j.GenerateValueClassForVisitorProcessor;
import com.github.sviperll.adt4j.model.util.CodeGenerationException;
import com.github.sviperll.adt4j.model.util.ErrorTypeFound;
import com.github.sviperll.adt4j.model.util.Serialization;
import com.github.sviperll.adt4j.model.util.Types;
import com.github.sviperll.adt4j.model.util.ValueVisitorInterfaceModel;
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
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.ParametersAreNonnullByDefault;

public class ValueClassModelFactory {
    private static final String VISITOR_SUFFIX = "Visitor";
    private static final String VALUE_SUFFIX = "Value";

    public static JDefinedClass createValueClass(JDefinedClass jVisitorModel, GenerateValueClassForVisitor annotation) throws SourceException, CodeGenerationException, ErrorTypeFound {
        ValueVisitorInterfaceModel visitorModel = ValueVisitorInterfaceModel.createInstance(jVisitorModel, annotation);
        Serialization serialization = serialization(annotation);
        String valueClassName = valueClassName(jVisitorModel, annotation);
        ValueClassModelFactory factory = new ValueClassModelFactory(jVisitorModel._package(), valueClassName, serialization, annotation);
        ValueClassModel valueClassModel = factory.createValueClass(visitorModel);
        return valueClassModel.getJDefinedClass();
    }

    private static String valueClassName(JDefinedClass jVisitorModel, GenerateValueClassForVisitor annotation) {
        if (!annotation.valueClassName().equals(":auto")) {
            return annotation.valueClassName();
        } else {
            String visitorName = jVisitorModel.name();
            if (visitorName.endsWith(VISITOR_SUFFIX))
                return visitorName.substring(0, visitorName.length() - VISITOR_SUFFIX.length());
            else
                return visitorName + VALUE_SUFFIX;
        }
    }

    private static Serialization serialization(GenerateValueClassForVisitor annotation) {
        if (!annotation.valueClassIsSerializable())
            return Serialization.notSerializable();
        else
            return Serialization.serializable(annotation.valueClassSerialVersionUID());
    }

    private final Serialization serialization;
    private final GenerateValueClassForVisitor annotation;
    private final JPackage jpackage;
    private final String className;

    ValueClassModelFactory(JPackage jpackage, String className, Serialization serialization, GenerateValueClassForVisitor annotation) {
        this.serialization = serialization;
        this.annotation = annotation;
        this.jpackage = jpackage;
        this.className = className;
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

    ValueClassModel createValueClass(ValueVisitorInterfaceModel visitorInterface) throws SourceException, CodeGenerationException {
        try {
            Types types = Types.createInstance(jpackage.owner());
            if (annotation.valueClassIsSerializable()) {
                for (JMethod interfaceMethod: visitorInterface.methods()) {
                    for (JVar param: interfaceMethod.params()) {
                        AbstractJType type = param.type();
                        if (!visitorInterface.isSelf(type) && !types.isSerializable(type))
                            throw new SourceException("Value class can't be serializable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not serializable");
                    }
                    JVar param = interfaceMethod.listVarParam();
                    if (param != null) {
                        AbstractJType type = param.type();
                        if (!visitorInterface.isSelf(type) && !types.isSerializable(type))
                            throw new SourceException("Value class can't be serializable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not serializable");
                    }
                }
            }

            if (annotation.valueClassIsComparable()) {
                for (JMethod interfaceMethod: visitorInterface.methods()) {
                    for (JVar param: interfaceMethod.params()) {
                        AbstractJType type = param.type();
                        if (!visitorInterface.isSelf(type) && !types.isComparable(type))
                            throw new SourceException("Value class can't be comparable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not comparable");
                    }
                    JVar param = interfaceMethod.listVarParam();
                    if (param != null) {
                        AbstractJType type = param.type();
                        if (!visitorInterface.isSelf(type) && !types.isComparable(type))
                            throw new SourceException("Value class can't be comparable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not comparable");
                    }
                }
            }

            int mods = annotation.valueClassIsPublic() ? JMod.PUBLIC: JMod.NONE;
            JDefinedClass valueClass = jpackage._class(mods, className, EClassType.CLASS);
            JAnnotationUse generatedAnnotation = valueClass.annotate(Generated.class);
            generatedAnnotation.param("value", GenerateValueClassForVisitorProcessor.class.getName());
            valueClass.annotate(ParametersAreNonnullByDefault.class);
            for (JTypeVar visitorTypeParameter: visitorInterface.getValueTypeParameters()) {
                Types.generifyWithBoundsFrom(valueClass, visitorTypeParameter.name(), visitorTypeParameter);
            }
            if (annotation.valueClassIsSerializable()) {
                valueClass._implements(types._Serializable);
                valueClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, types._long, "serialVersionUID", JExpr.lit(annotation.valueClassSerialVersionUID()));
            }

            if (annotation.valueClassIsComparable()) {
                valueClass._implements(types._Comparable.narrow(valueClass.narrow(valueClass.typeParams())));
            }

            JDefinedClass acceptingInterface = createAcceptingInterface(valueClass, visitorInterface, types);
            if (annotation.valueClassIsSerializable()) {
                acceptingInterface._extends(types._Serializable);
            }

            ValueClassModel result = new ValueClassModel(valueClass, acceptingInterface, visitorInterface, types);
            ValueClassModel.MethodBuilder methodBuilder = result.createMethodBuilder(serialization);
            Map<String, JMethod> constructorMethods = methodBuilder.buildConstructorMethods(serialization);
            methodBuilder.buildPrivateConstructor();
            methodBuilder.buildProtectedConstructor(serialization);
            methodBuilder.buildAcceptMethod();
            Map<String, FieldConfiguration> gettersConfigutation = result.getGettersConfigutation();
            for (FieldConfiguration getter: gettersConfigutation.values()) {
                methodBuilder.generateGetter(getter);
            }
            Map<String, FieldConfiguration> updatersConfiguration = result.getUpdatersConfiguration();
            for (FieldConfiguration updater: updatersConfiguration.values()) {
                methodBuilder.generateUpdater(updater);
            }
            Map<String, AccessLevel> predicates = result.getPredicates();
            for (Map.Entry<String, AccessLevel> predicate: predicates.entrySet()) {
                methodBuilder.generatePredicate(predicate.getKey(), predicate.getValue());
            }
            if (annotation.valueClassIsComparable()) {
                methodBuilder.buildCompareTo();
            }
            methodBuilder.buildEqualsMethod();
            methodBuilder.buildHashCodeMethod(annotation.valueClassHashCodeBase());
            methodBuilder.buildToStringMethod();
            result.buildFactory(constructorMethods);

            return result;
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
    }


}
