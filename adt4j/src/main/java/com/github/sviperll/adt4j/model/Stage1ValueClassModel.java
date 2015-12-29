/*
 * Copyright (c) 2015, vir
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

import com.github.sviperll.adt4j.model.config.FieldConfiguration;
import com.github.sviperll.adt4j.model.config.PredicateConfigutation;
import com.github.sviperll.adt4j.model.config.ValueVisitorInterfaceModel;
import com.github.sviperll.adt4j.model.util.GenerationResult;
import com.github.sviperll.adt4j.model.util.Types;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vir
 */
public class Stage1ValueClassModel {
    private final JDefinedClass valueClass;
    private final ValueVisitorInterfaceModel visitorInterface;
    private final Types types;

    Stage1ValueClassModel(JDefinedClass valueClass, ValueVisitorInterfaceModel visitorInterface, Types types) {
        this.valueClass = valueClass;
        this.visitorInterface = visitorInterface;
        this.types = types;
    }
    
    public GenerationResult<JDefinedClass> createResult() {
        List<String> errors = new ArrayList<>();
        errors.addAll(validateInterfaces());
        
        GenerationResult<Map<String, FieldConfiguration>> gettersConfigutationResult = visitorInterface.getGettersConfigutation(valueClass, types);
        errors.addAll(gettersConfigutationResult.errors());
        Map<String, FieldConfiguration> gettersConfigutation = gettersConfigutationResult.result();

        GenerationResult<Map<String, FieldConfiguration>> updatersConfigurationResult = visitorInterface.getUpdatersConfiguration(valueClass, types);
        errors.addAll(updatersConfigurationResult.errors());
        Map<String, FieldConfiguration> updatersConfiguration = updatersConfigurationResult.result();

        GenerationResult<Map<String, PredicateConfigutation>> predicatesResult = visitorInterface.getPredicates();
        errors.addAll(predicatesResult.errors());
        Map<String, PredicateConfigutation> predicates = predicatesResult.result();

        FinalValueClassModel result;
        if (!errors.isEmpty()) {
            result = FinalValueClassModel.createErrorModel(valueClass, visitorInterface, types);
        } else {
            JDefinedClass acceptingInterface;
            try {
                acceptingInterface = createAcceptingInterface();
            } catch (JClassAlreadyExistsException ex) {
                throw new RuntimeException("Unexpected exception", ex);
            }
            if (visitorInterface.isValueClassSerializable()) {
                acceptingInterface._extends(types._Serializable);
            }

            result = FinalValueClassModel.createModel(valueClass, acceptingInterface, visitorInterface, types);
        }
        result.buildSerialVersionUID();
        FinalValueClassModel.MethodBuilder methodBuilder = result.createMethodBuilder(visitorInterface.serialization());
        Map<String, JMethod> constructorMethods = methodBuilder.buildConstructorMethods(visitorInterface.serialization());
        methodBuilder.buildPrivateConstructor();
        if (visitorInterface.isValueClassSerializable())
            methodBuilder.buildReadObjectMethod();
        methodBuilder.buildProtectedConstructor(visitorInterface.serialization());
        methodBuilder.buildAcceptMethod();
        for (FieldConfiguration getter: gettersConfigutation.values()) {
            methodBuilder.generateGetter(getter);
        }
        for (FieldConfiguration updater: updatersConfiguration.values()) {
            methodBuilder.generateUpdater(updater);
        }
        for (Map.Entry<String, PredicateConfigutation> predicate: predicates.entrySet()) {
            methodBuilder.generatePredicate(predicate.getKey(), predicate.getValue());
        }
        if (visitorInterface.isValueClassComparable()) {
            methodBuilder.buildCompareTo();
        }
        methodBuilder.buildEqualsMethod();
        methodBuilder.buildHashCodeMethod(visitorInterface.hashCodeBase());
        methodBuilder.buildToStringMethod();
        try {
            result.buildFactory(constructorMethods);
        } catch (JClassAlreadyExistsException ex) {
            throw new RuntimeException("Unexpected exception :)", ex);
        }

        return new GenerationResult<>(valueClass, errors);
    }

    private Collection<? extends String> validateInterfaces() {
        List<String> errors = new ArrayList<>();
        if (visitorInterface.isValueClassSerializable()) {
            for (JMethod interfaceMethod: visitorInterface.methods()) {
                for (JVar param: interfaceMethod.params()) {
                    AbstractJType type = param.type();
                    if (!type.isError() && !visitorInterface.isSelf(type) && !types.isSerializable(type))
                        errors.add("Value class can't be serializable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not serializable");
                }
                JVar param = interfaceMethod.varParam();
                if (param != null) {
                    AbstractJType type = param.type();
                    if (!type.isError() && !visitorInterface.isSelf(type) && !types.isSerializable(type))
                        errors.add("Value class can't be serializable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not serializable");
                }
            }
        }
        
        if (visitorInterface.isValueClassComparable()) {
            for (JMethod interfaceMethod: visitorInterface.methods()) {
                for (JVar param: interfaceMethod.params()) {
                    AbstractJType type = param.type();
                    if (!type.isError() && !visitorInterface.isSelf(type) && !types.isComparable(type))
                        errors.add("Value class can't be comparable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not comparable");
                }
                JVar param = interfaceMethod.varParam();
                if (param != null) {
                    AbstractJType type = param.type();
                    if (!type.isError() && !visitorInterface.isSelf(type) && !types.isComparable(type))
                        errors.add("Value class can't be comparable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not comparable");
                }
            }
        }
        return errors;
    }

    void fullySpecifyClassHeader() {
        for (JTypeVar visitorTypeParameter: visitorInterface.getValueTypeParameters()) {
            JTypeVar typeParameter = valueClass.generify(visitorTypeParameter.name());
            typeParameter.boundLike(visitorTypeParameter);
        }
        for (AbstractJClass iface: visitorInterface.implementsInterfaces()) {
            valueClass._implements(iface.typeParams().length == 0 ? iface : iface.narrow(valueClass.typeParams()));
        }
        AbstractJClass extendsClass = visitorInterface.valueClassExtends();
        valueClass._extends(extendsClass.typeParams().length == 0 ? extendsClass : extendsClass.narrow(valueClass.typeParams()));
        if (visitorInterface.isValueClassSerializable()) {
            valueClass._implements(types._Serializable);
        }
        if (visitorInterface.isValueClassComparable()) {
            valueClass._implements(types._Comparable.narrow(visitorInterface.wrapValueClass(valueClass).narrow(valueClass.typeParams())));
        }
    }

    private JDefinedClass createAcceptingInterface() throws JClassAlreadyExistsException {
        JDefinedClass acceptingInterface = valueClass._class(JMod.PUBLIC, valueClass.name() + "Acceptor", EClassType.INTERFACE);

        // Hack to overcome bug in codeModel. We want private interface!!! Not public.
        acceptingInterface.mods().setPrivate();

        for (JTypeVar visitorTypeParameter: visitorInterface.getValueTypeParameters()) {
            JTypeVar typeParameter = acceptingInterface.generify(visitorTypeParameter.name());
            typeParameter.boundLike(visitorTypeParameter);
        }

        JMethod acceptMethod = acceptingInterface.method(JMod.PUBLIC, types._void, visitorInterface.acceptMethodName());

        JTypeVar visitorResultType = visitorInterface.getResultTypeParameter();
        AbstractJClass resultType;
        if (visitorResultType == null)
            resultType = types._Object;
        else {
            JTypeVar resultTypeVar = acceptMethod.generify(visitorResultType.name());
            resultTypeVar.boundLike(visitorResultType);
            resultType = resultTypeVar;
        }
        acceptMethod.type(resultType);

        JTypeVar visitorExceptionType = visitorInterface.getExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            JTypeVar exceptionTypeParameter = acceptMethod.generify(visitorExceptionType.name());
            exceptionTypeParameter.boundLike(visitorExceptionType);
            exceptionType = exceptionTypeParameter;
            acceptMethod._throws(exceptionType);
        }

        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        AbstractJClass usedVisitorType = visitorInterface.narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType, "visitor");

        return acceptingInterface;
    }
    
}
