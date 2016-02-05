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
import com.github.sviperll.adt4j.model.config.ValueClassConfiguration;
import com.github.sviperll.adt4j.model.config.VisitorDefinition;
import com.github.sviperll.adt4j.model.util.GenerationProcess;
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
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author vir
 */
public class Stage1ValueClassModel {
    private final JDefinedClass valueClass;
    private final ValueClassConfiguration configuration;
    private final Types types;

    Stage1ValueClassModel(JDefinedClass valueClass, ValueClassConfiguration configuration, Types types) {
        this.valueClass = valueClass;
        this.configuration = configuration;
        this.types = types;
    }
    
    public GenerationResult<JDefinedClass> createResult() {
        GenerationProcess generation = new GenerationProcess();
        generation.reportAllErrors(validateInterfaces());
        
        Map<String, FieldConfiguration> gettersConfigutation = generation.processGenerationResult(configuration.getGettersConfigutation(valueClass, types));
        Map<String, FieldConfiguration> updatersConfiguration = generation.processGenerationResult(configuration.getUpdatersConfiguration(valueClass, types));
        Map<String, PredicateConfigutation> predicates = generation.processGenerationResult(configuration.getPredicates());

        FinalValueClassModel result;
        if (generation.hasErrors()) {
            FinalValueClassModelEnvironment environment = new FinalValueClassModelEnvironment(valueClass, null, configuration);
            result = FinalValueClassModel.createErrorModel(environment, types);
        } else {
            JDefinedClass acceptingInterface;
            try {
                acceptingInterface = createAcceptingInterface();
            } catch (JClassAlreadyExistsException ex) {
                throw new RuntimeException("Unexpected exception", ex);
            }
            if (configuration.isValueClassSerializable()) {
                acceptingInterface._extends(types._Serializable);
            }
            FinalValueClassModelEnvironment environment = new FinalValueClassModelEnvironment(valueClass, acceptingInterface, configuration);
            result = FinalValueClassModel.createModel(environment, types);
        }
        result.buildSerialVersionUID();
        FinalValueClassModel.MethodBuilder methodBuilder = result.createMethodBuilder(configuration.serialization());
        Map<String, JMethod> constructorMethods = methodBuilder.buildConstructorMethods(configuration.serialization());
        methodBuilder.buildPrivateConstructor();
        if (configuration.isValueClassSerializable())
            methodBuilder.buildReadObjectMethod();
        methodBuilder.buildProtectedConstructor(configuration.serialization());
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
        if (configuration.isValueClassComparable()) {
            methodBuilder.buildCompareTo();
        }
        methodBuilder.buildEqualsMethod();
        methodBuilder.buildHashCodeMethod(configuration.hashCodeBase());
        methodBuilder.buildToStringMethod();
        try {
            result.buildFactory(constructorMethods);
        } catch (JClassAlreadyExistsException ex) {
            throw new RuntimeException("Unexpected exception :)", ex);
        }

        return generation.createGenerationResult(valueClass);
    }

    private Collection<? extends String> validateInterfaces() {
        GenerationProcess generation = new GenerationProcess();
        if (configuration.isValueClassSerializable()) {
            for (JMethod interfaceMethod: configuration.visitorDefinition().methodDefinitions()) {
                for (JVar param: interfaceMethod.params()) {
                    AbstractJType type = param.type();
                    if (!type.isError() && !configuration.visitorDefinition().isSelfTypeParameter(type) && !types.isSerializable(type))
                        generation.reportError("Value class can't be serializable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not serializable");
                }
                JVar param = interfaceMethod.varParam();
                if (param != null) {
                    AbstractJType type = param.type();
                    if (!type.isError() && !configuration.visitorDefinition().isSelfTypeParameter(type) && !types.isSerializable(type))
                        generation.reportError("Value class can't be serializable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not serializable");
                }
            }
        }
        
        if (configuration.isValueClassComparable()) {
            for (JMethod interfaceMethod: configuration.visitorDefinition().methodDefinitions()) {
                for (JVar param: interfaceMethod.params()) {
                    AbstractJType type = param.type();
                    if (!type.isError() && !configuration.visitorDefinition().isSelfTypeParameter(type) && !types.isComparable(type))
                        generation.reportError("Value class can't be comparable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not comparable");
                }
                JVar param = interfaceMethod.varParam();
                if (param != null) {
                    AbstractJType type = param.type();
                    if (!type.isError() && !configuration.visitorDefinition().isSelfTypeParameter(type) && !types.isComparable(type))
                        generation.reportError("Value class can't be comparable: " + param.name() + " parameter in " + interfaceMethod.name() + " method is not comparable");
                }
            }
        }
        return generation.reportedErrors();
    }

    void fullySpecifyClassHeader() {
        for (JTypeVar visitorTypeParameter: configuration.getValueTypeParameters()) {
            JTypeVar typeParameter = valueClass.generify(visitorTypeParameter.name());
            typeParameter.boundLike(visitorTypeParameter);
        }
        for (AbstractJClass iface: configuration.implementsInterfaces()) {
            valueClass._implements(iface.typeParams().length == 0 ? iface : iface.narrow(valueClass.typeParams()));
        }
        AbstractJClass extendsClass = configuration.valueClassExtends();
        valueClass._extends(extendsClass.typeParams().length == 0 ? extendsClass : extendsClass.narrow(valueClass.typeParams()));
        if (configuration.isValueClassSerializable()) {
            valueClass._implements(types._Serializable);
        }
        if (configuration.isValueClassComparable()) {
            valueClass._implements(types._Comparable.narrow(configuration.wrapValueClass(valueClass).narrow(valueClass.typeParams())));
        }
    }

    private JDefinedClass createAcceptingInterface() throws JClassAlreadyExistsException {
        JDefinedClass acceptingInterface = valueClass._class(JMod.PUBLIC, valueClass.name() + "Acceptor", EClassType.INTERFACE);

        // Hack to overcome bug in codeModel. We want private interface!!! Not public.
        acceptingInterface.mods().setPrivate();

        for (JTypeVar visitorTypeParameter: configuration.getValueTypeParameters()) {
            JTypeVar typeParameter = acceptingInterface.generify(visitorTypeParameter.name());
            typeParameter.boundLike(visitorTypeParameter);
        }

        JMethod acceptMethod = acceptingInterface.method(JMod.PUBLIC, types._void, configuration.acceptMethodName());

        JTypeVar visitorResultType = configuration.visitorDefinition().getResultTypeParameter();
        AbstractJClass resultType;
        if (visitorResultType == null)
            resultType = types._Object;
        else {
            JTypeVar resultTypeVar = acceptMethod.generify(visitorResultType.name());
            resultTypeVar.boundLike(visitorResultType);
            resultType = resultTypeVar;
        }
        acceptMethod.type(resultType);

        JTypeVar visitorExceptionType = configuration.visitorDefinition().getExceptionTypeParameter();
        JTypeVar exceptionType = null;
        if (visitorExceptionType != null) {
            JTypeVar exceptionTypeParameter = acceptMethod.generify(visitorExceptionType.name());
            exceptionTypeParameter.boundLike(visitorExceptionType);
            exceptionType = exceptionTypeParameter;
            acceptMethod._throws(exceptionType);
        }

        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        VisitorDefinition.VisitorUsage usedVisitorType = configuration.visitorDefinition().narrowed(usedValueClassType, resultType, exceptionType);
        acceptMethod.param(usedVisitorType.getVisitorType(), "visitor");

        return acceptingInterface;
    }
    
}
