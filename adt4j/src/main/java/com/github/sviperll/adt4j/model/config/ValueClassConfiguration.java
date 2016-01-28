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
package com.github.sviperll.adt4j.model.config;

import com.github.sviperll.adt4j.Caching;
import com.github.sviperll.adt4j.MemberAccess;
import com.github.sviperll.adt4j.Visitor;
import com.github.sviperll.adt4j.WrapsGeneratedValueClass;
import com.github.sviperll.adt4j.model.util.GenerationProcess;
import com.github.sviperll.adt4j.model.util.GenerationResult;
import com.github.sviperll.adt4j.model.util.Source;
import com.github.sviperll.adt4j.model.util.Types;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ValueClassConfiguration {
    private static final String VISITOR_SUFFIX = "Visitor";
    private static final String VALUE_SUFFIX = "Value";

    public static GenerationResult<ValueClassConfiguration> createInstance(VisitorModel visitorModel, JAnnotationUse annotation) {
        return createInstance(visitorModel, annotation, null);
    }

    public static GenerationResult<ValueClassConfiguration> createInstance(VisitorModel visitorModel, JAnnotationUse annotation, JDefinedClass valueClass) {
        GenerationProcess generation = new GenerationProcess();
        String acceptMethodName = annotation.getParam("acceptMethodName", String.class);
        MemberAccess acceptMethodAccess = annotation.getParam("acceptMethodAccess", MemberAccess.class);
        boolean isPublic = annotation.getParam("isPublic", Boolean.class);
        Caching hashCodeCaching = annotation.getParam("hashCodeCaching", Caching.class);
        int hashCodeBase = annotation.getParam("hashCodeBase", Integer.class);
        boolean isComparable = annotation.getParam("isComparable", Boolean.class);
        float floatEpsilon = annotation.getParam("floatEpsilon", Float.class);
        double doubleEpsilon = annotation.getParam("doubleEpsilon", Double.class);
        FloatCustomization floatCustomization = new FloatCustomization(floatEpsilon, doubleEpsilon);
        Serialization serialization = serialization(annotation);
        ClassCustomization classCustomization = generation.processGenerationResult(classCustomization(annotation, visitorModel, valueClass));

        AbstractJClass[] interfaces = annotation.getParam("implementsInterfaces", AbstractJClass[].class);

        AcceptMethodCustomization acceptMethodCustomization = new AcceptMethodCustomization(acceptMethodName, acceptMethodAccess);
        InterfacesCustomization interfaceCustomization = new InterfacesCustomization(isComparable, serialization, interfaces);
        APICustomization apiCustomization = new APICustomization(isPublic, acceptMethodCustomization, interfaceCustomization);
        ImplementationCustomization implementationCustomization = new ImplementationCustomization(hashCodeCaching, hashCodeBase, floatCustomization);
        Customization customiztion = new Customization(classCustomization, apiCustomization, implementationCustomization);
        return generation.createGenerationResult(new ValueClassConfiguration(visitorModel, customiztion));
    }

    private static GenerationResult<ClassCustomization> classCustomization(JAnnotationUse annotation, VisitorModel visitorModel, JDefinedClass valueClass) throws ClassCastException, NullPointerException {
        GenerationProcess generation = new GenerationProcess();
        AbstractJClass extendsClass = annotation.getParam("extendsClass", AbstractJClass.class);
        AbstractJClass wrapperClass = annotation.getParam("wrapperClass", AbstractJClass.class);
        if (wrapperClass == null)
            throw new NullPointerException("wrapperClass annotation argument should never be null");
        String wrapperClassFullName = wrapperClass.fullName();
        if (wrapperClassFullName == null)
            throw new NullPointerException("wrapperClass.fullName() is null");
        if (wrapperClassFullName.equals("java.lang.Object"))
            wrapperClass = null;
        String className = annotation.getParam("className", String.class);
        if (className == null)
            throw new NullPointerException("className annotation argument should never be null");
        if (wrapperClass == null) {
            if (className.equals(":auto")) {
                className = autoClassName(visitorModel.visitorName());
            }
        } else {
            AbstractJClass wrapperClassErasure = wrapperClass.erasure();
            if (wrapperClassErasure instanceof JDefinedClass) {
                JDefinedClass definition = (JDefinedClass)wrapperClassErasure;
                JAnnotationUse wrapsGeneratedAnnotation = null;
                for (JAnnotationUse wrapperAnnotaion: definition.annotations()) {
                    if (wrapperAnnotaion.getAnnotationClass().erasure().fullName().equals(WrapsGeneratedValueClass.class.getName())) {
                        wrapsGeneratedAnnotation = wrapperAnnotaion;
                    }
                }
                if (wrapsGeneratedAnnotation == null)
                    generation.reportError(MessageFormat.format("Wrapper class should be annotated with @{0} annotation.", com.github.sviperll.adt4j.WrapsGeneratedValueClass.class.getName()));
                else {
                    AbstractJClass visitor = wrapsGeneratedAnnotation.getParam("visitor", AbstractJClass.class);
                    if (visitor == null || !visitor.fullName().equals(visitorModel.qualifiedName()))
                        generation.reportError("@" + WrapsGeneratedValueClass.class.getName() + " annotation should have " + visitorModel.qualifiedName() + " as visitor argument");
                }
            }
            if (!className.equals(":auto")) {
                generation.reportError("You shouldn't define className when wrapperClass is used. Generated class name is derived from wrapper class' extends clause.");
            } else {
                AbstractJClass extendedClass = wrapperClass._extends();
                boolean extendedClassError = false;
                if (extendedClass != null) {
                    if (extendedClass.isError()) {
                        className = extendedClass.name();
                    } else {
                        if (valueClass == null) {
                            extendedClassError = true;
                        } else {
                            String valueClassFullName = valueClass.fullName();
                            if (valueClassFullName == null || !valueClassFullName.equals(extendedClass.erasure().fullName()))
                                extendedClassError = true;
                            else
                                className = valueClass.name();
                        }
                    }
                }
                if (extendedClass == null || extendedClassError) {
                    generation.reportError("Wrapper class should explicitly extend non-existing class, that class is to be generated");
                    className = autoClassName(visitorModel.visitorName());
                } else {
                    boolean typeParamsError = false;
                    List<? extends AbstractJClass> typeArguments = extendedClass.getTypeParameters();
                    List<JTypeVar> generatedTypeParameters = visitorModel.nonspecialTypeParameters();
                    JTypeVar[] wrapperTypeParameters = wrapperClass.typeParams();
                    if (wrapperTypeParameters.length != typeArguments.size() || wrapperTypeParameters.length != generatedTypeParameters.size())
                        typeParamsError = true;
                    else {
                        for (int i = 0; i < wrapperTypeParameters.length; i++) {
                            JTypeVar wrapperTypeParameter = wrapperTypeParameters[i];
                            if (typeArguments.get(i) != wrapperTypeParameter) {
                                typeParamsError = true;
                                break;
                            }
                        }
                    }
                    if (typeParamsError) {
                        generation.reportError("Wrapper class should declare same type-parameters as generated class and should extend generated class with all type-arguments applied");
                    }
                }
            }
        }
        ClassCustomization classCustomization = new ClassCustomization(className, wrapperClass, extendsClass);
        return generation.createGenerationResult(classCustomization);
    }

    private static Serialization serialization(JAnnotationUse annotation) {
        if (!annotation.getParam("isSerializable", Boolean.class))
            return Serialization.notSerializable();
        else
            return Serialization.serializable(annotation.getParam("serialVersionUID", Long.class));
    }

    private static String autoClassName(String visitorName) {
        if (visitorName.endsWith(VISITOR_SUFFIX))
            return visitorName.substring(0, visitorName.length() - VISITOR_SUFFIX.length());
        else
            return visitorName + VALUE_SUFFIX;
    }

    private final VisitorModel visitorModel;
    private final Customization customization;

    private ValueClassConfiguration(VisitorModel visitorModel, Customization customiztion) {
        this.visitorModel = visitorModel;
        this.customization = customiztion;
    }

    public VisitorModel visitor() {
        return visitorModel;
    }

    public List<? extends JTypeVar> getValueTypeParameters() {
        return visitorModel.nonspecialTypeParameters();
    }

    public String acceptMethodName() {
        return customization.acceptMethodName();
    }

    public boolean isValueClassPublic() {
        return customization.isValueClassPublic();
    }

    public MemberAccess factoryMethodAccessLevel() {
        return customization.isValueClassPublic() ? MemberAccess.PUBLIC : MemberAccess.PACKAGE;
    }

    public MemberAccess acceptMethodAccessLevel() {
        return customization.acceptMethodAccessLevel();
    }

    public Caching hashCodeCaching() {
        return customization.hashCodeCaching();
    }

    public boolean isValueClassSerializable() {
        return customization.isValueClassSerializable();
    }

    public boolean isValueClassComparable() {
        return customization.isValueClassComparable();
    }

    public String valueClassName() {
        return customization.className();
    }

    public AbstractJClass[] implementsInterfaces() {
        return customization.implementsInterfaces();
    }

    public AbstractJClass valueClassExtends() {
        return customization.valueClassExtends();
    }

    public int hashCodeBase() {
        return customization.hashCodeBase();
    }

    public Serialization serialization() {
        return customization.serialization();
    }

    public long serialVersionUIDForGeneratedCode() {
        return customization.serialVersionUIDForGeneratedCode();
    }

    public GenerationResult<Map<String, FieldConfiguration>> getGettersConfigutation(JDefinedClass valueClass, Types types) {
        GenerationProcess generation = new GenerationProcess();
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> gettersMap = new TreeMap<>();
        FieldReader reader = new FieldReader(gettersMap);
        VisitorModel.NarrowedVisitor narrowed = visitorModel.narrowed(usedValueClassType, visitorModel.getResultTypeParameter(), types._RuntimeException);
        for (JMethod interfaceMethod: visitorModel.methods()) {
            for (JVar param: interfaceMethod.params()) {
                AbstractJType paramType = Source.toDeclarable(narrowed.getNarrowedType(param.type()));
                generation.processGenerationResult(reader.readGetter(interfaceMethod, param, paramType, false));
            }
            JVar param = interfaceMethod.varParam();
            if (param != null) {
                AbstractJType paramType = Source.toDeclarable(narrowed.getNarrowedType(param.type()));
                generation.processGenerationResult(reader.readGetter(interfaceMethod, param, paramType, true));
            }
        }
        return generation.createGenerationResult(gettersMap);
    }

    public GenerationResult<Map<String, FieldConfiguration>> getUpdatersConfiguration(JDefinedClass valueClass, Types types) {
        GenerationProcess generation = new GenerationProcess();
        AbstractJClass usedValueClassType = valueClass.narrow(valueClass.typeParams());
        Map<String, FieldConfiguration> updatersMap = new TreeMap<>();
        FieldReader reader = new FieldReader(updatersMap);
        VisitorModel.NarrowedVisitor narrowed = visitorModel.narrowed(usedValueClassType, visitorModel.getResultTypeParameter(), types._RuntimeException);
        for (JMethod interfaceMethod: visitorModel.methods()) {
            for (JVar param: interfaceMethod.params()) {
                AbstractJType paramType = Source.toDeclarable(narrowed.getNarrowedType(param.type()));
                generation.processGenerationResult(reader.readUpdater(interfaceMethod, param, paramType, false));
            }
            JVar param = interfaceMethod.varParam();
            if (param != null) {
                AbstractJType paramType = Source.toDeclarable(narrowed.getNarrowedType(param.type()));
                generation.processGenerationResult(reader.readUpdater(interfaceMethod, param, paramType, true));
            }
        }
        return generation.createGenerationResult(updatersMap);
    }

    public GenerationResult<Map<String, PredicateConfigutation>> getPredicates() {
        GenerationProcess generation = new GenerationProcess();
        Map<String, PredicateConfigutation> predicates = new TreeMap<>();
        PredicatesReader predicatesReader = new PredicatesReader(predicates);
        for (JMethod interfaceMethod: visitorModel.methods()) {
            for (JAnnotationUse annotationUsage: interfaceMethod.annotations()) {
                generation.processGenerationResult(predicatesReader.read(interfaceMethod, annotationUsage));
            }
        }
        return generation.createGenerationResult(predicates);
    }

    public AbstractJClass wrapValueClass(AbstractJClass valueClass) {
        AbstractJClass wrapperClass = customization.wrapperClass();
        return wrapperClass != null ? wrapperClass : valueClass;
    }

    public IJExpression wrapValue(AbstractJType usedWrappedClassType, IJExpression valueExpression) {
        AbstractJClass wrapperClass = customization.wrapperClass();
        if (wrapperClass == null)
            return valueExpression;
        else {
            JInvocation invocation1 = JExpr._new(usedWrappedClassType);
            invocation1.arg(valueExpression);
            return invocation1;
        }
    }

    public boolean wrappingEnabled() {
        AbstractJClass wrapperClass = customization.wrapperClass();
        return wrapperClass != null;
    }

    public FloatCustomization floatCustomization() {
        return customization.floatCustomization();
    }


}
