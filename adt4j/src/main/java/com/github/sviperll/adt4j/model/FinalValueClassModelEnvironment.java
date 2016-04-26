/*
 * Copyright (c) 2016, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
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

import com.github.sviperll.adt4j.Caching;
import com.github.sviperll.adt4j.MemberAccess;
import com.github.sviperll.adt4j.model.config.FloatCustomization;
import com.github.sviperll.adt4j.model.config.ValueClassConfiguration;
import com.github.sviperll.adt4j.model.config.VisitorDefinition;
import com.github.sviperll.adt4j.model.config.VisitorDefinition.VisitorUsage;
import com.github.sviperll.adt4j.model.util.Source;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JTypeVar;
import java.util.Collection;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.Nullable;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class FinalValueClassModelEnvironment {
    private final JDefinedClass valueClass;
    private final JDefinedClass acceptingInterface;
    private final ValueClassConfiguration configuration;
    public FinalValueClassModelEnvironment(JDefinedClass valueClass, @Nullable JDefinedClass acceptingInterface, ValueClassConfiguration configuration) {
        this.valueClass = valueClass;
        this.acceptingInterface = acceptingInterface;
        this.configuration = configuration;
    }

    String valueClassName() {
        return valueClass.name();
    }

    Caching hashCodeCaching() {
        return configuration.hashCodeCaching();
    }

    boolean isValueClassSerializable() {
        return configuration.isValueClassSerializable();
    }

    long serialVersionUIDForGeneratedCode() {
        return configuration.serialVersionUIDForGeneratedCode();
    }

    JFieldVar buildValueClassField(int mods, AbstractJType type, String name, IJExpression init) {
        return valueClass.field(mods, type, name, init);
    }

    JFieldVar buildValueClassField(int mods, AbstractJType type, String name) {
        return valueClass.field(mods, type, name);
    }

    AbstractJClass acceptingInterfaceTypeInsideValueClass() {
        return Source.narrowType(acceptingInterface, valueClass.typeParams());
    }

    MemberAccess factoryMethodAccessLevel() {
        return configuration.factoryMethodAccessLevel();
    }

    JMethod buildValueClassMethod(int mods, String name) {
        return valueClass.method(mods, valueClass.owner().VOID, name);
    }

    Collection<? extends JTypeVar> getValueTypeParameters() {
        return configuration.getValueTypeParameters();
    }

    AbstractJClass wrappedValueClassType(AbstractJClass[] typeParams) {
        return Source.narrowType(configuration.wrapValueClass(valueClass), typeParams);
    }

    VisitorUsage visitor(AbstractJClass selfType, AbstractJClass resultType, @Nullable AbstractJClass exceptionType) {
        return configuration.visitorDefinition().narrowed(selfType, resultType, exceptionType);
    }

    JDefinedClass buildValueClassInnerClass(int mods, String name, EClassType eClassType) throws JClassAlreadyExistsException {
        return valueClass._class(mods, name, eClassType);
    }

    JInvocation invokeValueClassStaticMethod(JMethod constructorMethod, AbstractJClass[] typeArguments) {
        JInvocation result = valueClass.staticInvoke(constructorMethod);
        for (AbstractJClass typeArgument: typeArguments) {
            result.narrow(typeArgument);
        }
        return result;
    }

    String acceptingInterfaceName() {
        return acceptingInterface.name();
    }

    AbstractJClass acceptingInterfaceType(AbstractJClass[] typeParams) {
        return Source.narrowType(acceptingInterface, typeParams);
    }

    String acceptMethodName() {
        return configuration.acceptMethodName();
    }

    // JTypeVar getVisitorResultTypeParameter() {
    //     return configuration.visitorDefinition().getResultTypeParameter();
    // }

    // JTypeVar getVisitorExceptionTypeParameter() {
    //    return configuration.visitorDefinition().getExceptionTypeParameter();
    // }

    JMethod buildValueClassConstructor(int mods) {
        return valueClass.constructor(mods);
    }

    AbstractJClass unwrappedValueClassTypeInsideValueClass() {
        return Source.narrowType(valueClass, valueClass.typeParams());
    }

    MemberAccess acceptMethodAccessLevel() {
        return configuration.acceptMethodAccessLevel();
    }

    AbstractJClass wrappedValueClassTypeInsideValueClass() {
        return Source.narrowType(configuration.wrapValueClass(valueClass), valueClass.typeParams());
    }

    AbstractJClass unwrappedValueClassType(AbstractJClass[] typeParams) {
        return Source.narrowType(valueClass, typeParams);
    }

    String valueClassQualifiedName() {
        return valueClass.fullName();
    }

    IJExpression wrappedValue(AbstractJClass usedWrappedValueClassType, IJExpression value) {
        return configuration.wrapValue(usedWrappedValueClassType, value);
    }

    JMethod buildAcceptingInterfaceMethod(int mods, String name) {
        return acceptingInterface.method(mods, valueClass.owner().VOID, name);
    }

    boolean wrappingEnabled() {
        return configuration.wrappingEnabled();
    }

    FloatCustomization floatCustomization() {
        return configuration.floatCustomization();
    }

    VisitorDefinition visitorDefinition() {
        return configuration.visitorDefinition();
    }
}
