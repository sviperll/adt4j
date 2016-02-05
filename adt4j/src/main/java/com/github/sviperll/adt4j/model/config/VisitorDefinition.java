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

import com.github.sviperll.adt4j.Visitor;
import com.github.sviperll.adt4j.model.util.GenerationProcess;
import com.github.sviperll.adt4j.model.util.GenerationResult;
import com.github.sviperll.adt4j.model.util.Source;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMods;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JTypeWildcard;
import com.helger.jcodemodel.JVar;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class VisitorDefinition {
    public static GenerationResult<VisitorDefinition> createInstance(JDefinedClass jVisitorModel, Visitor annotation) {
        GenerationProcess generation = new GenerationProcess();
        JTypeVar resultType = null;
        JTypeVar exceptionType = null;
        JTypeVar selfType = null;
        List<JTypeVar> valueClassTypeParameters = new ArrayList<>();
        for (JTypeVar typeVariable: jVisitorModel.typeParams()) {
            if (typeVariable.name().equals(annotation.resultVariableName()))
                resultType = typeVariable;
            else if (typeVariable.name().equals(annotation.selfReferenceVariableName()))
                selfType = typeVariable;
            else if (typeVariable.name().equals(annotation.exceptionVariableName()))
                exceptionType = typeVariable;
            else
                valueClassTypeParameters.add(typeVariable);
        }
        if (resultType == null) {
            generation.reportError(MessageFormat.format("Result type-variable is not found for visitor, expecting: {0}",
                    annotation.resultVariableName()));
            resultType = jVisitorModel.typeParams().length == 0 ? null : jVisitorModel.typeParams()[0];
        }
        if (exceptionType == null && !annotation.exceptionVariableName().equals(":none")) {
            generation.reportError(MessageFormat.format("Exception type-variable is not found for visitor, expecting: {0}",
                    annotation.exceptionVariableName()));
        }
        if (selfType == null && !annotation.selfReferenceVariableName().equals(":none")) {
            generation.reportError(MessageFormat.format("Self reference type-variable is not found for visitor, expecting: {0}",
                    annotation.selfReferenceVariableName()));
        }
        SpecialTypeVariables specialTypeVariables = new SpecialTypeVariables(resultType, exceptionType, selfType);
        Map<String, JMethod> methods = generation.processGenerationResult(createMethodMap(jVisitorModel, specialTypeVariables));
        return generation.createGenerationResult(new VisitorDefinition(jVisitorModel, methods, specialTypeVariables, valueClassTypeParameters));
    }

    private static GenerationResult<Map<String, JMethod>> createMethodMap(JDefinedClass jVisitorModel,
                                                        SpecialTypeVariables specialTypeVariables) {
        GenerationProcess generation = new GenerationProcess();
        Map<String, JMethod> methods = new TreeMap<>();
        for (JMethod method: jVisitorModel.methods()) {
            AbstractJType methodType = method.type();
            if (methodType == null)
                generation.reportError(MessageFormat.format("Visitor method result type is missing: {0}", method.name()));
            else if (methodType.isError()) {
                generation.reportError(MessageFormat.format("Visitor method result type is erroneous: {0}", method.name()));
            } else if (!specialTypeVariables.isResult(method.type())) {
                generation.reportError(MessageFormat.format("Visitor method is only allowed to return type declared as a result type of visitor: {0}: expecting {1}, found: {2}",
                        method.name(), specialTypeVariables.resultTypeParameter().name(), methodType.fullName()));
            }
            for (JTypeVar typeVariable: method.typeParamList()) {
                for (AbstractJClass bound: typeVariable.bounds()) {
                    if (bound.containsTypeVar(specialTypeVariables.resultTypeParameter())) {
                        generation.reportError(MessageFormat.format("Visitor method type-parameters shouldn''t depend on result type: {0}: {1} type-variable",
                                method.name(), typeVariable.name()));
                    }
                }
            }
            for (JVar parameter: method.listParams()) {
                if (parameter.type().containsTypeVar(specialTypeVariables.resultTypeParameter())) {
                    generation.reportError(MessageFormat.format("Visitor method shouldn''t have result type as a parameter: {0}: result type-parameter: {1}",
                            method.name(), specialTypeVariables.resultTypeParameter().name()));
                }
            }

            Collection<AbstractJClass> exceptions = method.getThrows();
            if (exceptions.size() > 1)
                generation.reportError(MessageFormat.format("Visitor method is allowed to throw no exceptions or throw single exception, declared as type-variable: {0}",
                        method.name()));
            else if (exceptions.size() == 1) {
                AbstractJClass exception = exceptions.iterator().next();
                if (exception.isError())
                    generation.reportError(MessageFormat.format("Visitor method exception type is erroneous: {0}", method.name()));
                else if (!specialTypeVariables.isException(exception))
                    generation.reportError(MessageFormat.format("Visitor method throws exception, not declared as type-variable: {0}: {1}",
                        method.name(), exception.fullName()));
            }

            JMethod exitingValue = methods.put(method.name(), method);
            if (exitingValue != null) {
                generation.reportError(MessageFormat.format("Method overloading is not supported for visitor interfaces: two methods with the same name: {0}",
                                                    method.name()));
            }
            for (JVar param: method.params()) {
                generation.processGenerationResult(Source.getNullability(param));
            }
        }
        return generation.createGenerationResult(methods);
    }

    private final JDefinedClass jVisitor;
    private final Map<String, JMethod> methods;
    private final SpecialTypeVariables specialTypeVariables;
    private final List<JTypeVar> nonspecialTypeParameters;

    private VisitorDefinition(JDefinedClass jVisitor, Map<String, JMethod> methods, SpecialTypeVariables specialTypeVariables, List<JTypeVar> nonspecialTypeParameters) {
        this.jVisitor = jVisitor;
        this.methods = methods;
        this.specialTypeVariables = specialTypeVariables;
        this.nonspecialTypeParameters = nonspecialTypeParameters;
    }

    public VisitorUsage narrowed(AbstractJClass selfType, AbstractJClass resultType, @Nullable AbstractJClass exceptionType) {
        return new VisitorUsage(selfType, resultType, exceptionType);
    }

    List<JTypeVar> nonspecialTypeParameters() {
        return nonspecialTypeParameters;
    }

    public JTypeVar getResultTypeParameter() {
        return specialTypeVariables.resultTypeParameter();
    }

    public JTypeVar getExceptionTypeParameter() {
        return specialTypeVariables.exceptionTypeParameter();
    }

    public JTypeVar getSelfTypeParameter() {
        return specialTypeVariables.selfTypeParameter();
    }

    String visitorName() {
        String result = jVisitor.name();
        if (result == null)
            throw new IllegalStateException("Visitor without a name: " + jVisitor);
        return result;
    }

    String qualifiedName() {
        String result = jVisitor.fullName();
        if (result == null)
            throw new IllegalStateException("Visitor without a qualified name: " + jVisitor);
        return result;
    }

    public boolean isSelfTypeParameter(AbstractJType type) {
        return specialTypeVariables.isSelf(type);
    }

    public Collection<JMethod> methodDefinitions() {
        return methods.values();
    }

    public class VisitorUsage {
        private final AbstractJClass selfType;
        private final AbstractJClass resultType;
        private final @Nullable AbstractJClass exceptionType;
        private VisitorUsage(AbstractJClass selfType, AbstractJClass resultType, @Nullable AbstractJClass exceptionType) {
            this.selfType = selfType;
            this.resultType = resultType;
            this.exceptionType = exceptionType;
        }
        public AbstractJClass getVisitorType() {
            AbstractJClass result = jVisitor;
            for (JTypeVar typeVariable: jVisitor.typeParams()) {
                result = result.narrow(specialTypeVariables.substituteSpecialType(typeVariable, selfType, resultType, exceptionType));
            }
            return result;
        }

        public Collection<MethodUsage> methods() {
            List<MethodUsage> result = new ArrayList<>();
            for (JMethod method: methods.values()) {
                result.add(new MethodUsage(this, method, new JTypeVar[] {}));
            }
            return result;
        }

        @Nullable
        public MethodUsage findMethod(String name) {
            for (JMethod method: methods.values()) {
                if (method.name().equals(name))
                    return new MethodUsage(this, method, new JTypeVar[] {});
            }
            return null;
        }

        public AbstractJType getNarrowedType(AbstractJType type) {
            AbstractJType result = specialTypeVariables.substituteSpecialType(type, selfType, resultType, exceptionType);
            List<? extends AbstractJClass> dataTypeArguments = selfType.getTypeParameters();
            int dataTypeIndex = 0;
            for (JTypeVar typeParameter : jVisitor.typeParams()) {
                if (!specialTypeVariables.isSpecial(typeParameter)) {
                    result = Source.substitute(result, typeParameter, dataTypeArguments.get(dataTypeIndex));
                    dataTypeIndex++;
                }
            }
            return result;
        }
    }

    public static class MethodUsage {

        private final JMethod interfaceMethod;
        private final AbstractJType[] methodTypeArguments;
        private final VisitorUsage visitor;

        private MethodUsage(VisitorUsage visitor, JMethod interfaceMethod, AbstractJType[] methodTypeArguments) {
            this.visitor = visitor;
            this.interfaceMethod = interfaceMethod;
            this.methodTypeArguments = methodTypeArguments;
        }

        public MethodUsage narrow(AbstractJType... typeArguments) {
            AbstractJType[] newArguments = Arrays.copyOf(methodTypeArguments, methodTypeArguments.length + typeArguments.length);
            System.arraycopy(typeArguments, 0, newArguments, methodTypeArguments.length, typeArguments.length);
            return new MethodUsage(visitor, interfaceMethod, newArguments);
        }

        public Collection<? extends VariableDeclaration> params() {
            List<VariableDeclaration> result = new ArrayList<>(interfaceMethod.params().size());
            for (JVar param: interfaceMethod.params()) {
                result.add(new VariableDeclaration(substitute(param.type()), param.name(), param.mods(), param.annotations()));
            }
            return result;
        }

        @Nullable
        public VariableDeclaration varParam() {
            JVar varParam = interfaceMethod.varParam();
            return varParam == null ? null : new VariableDeclaration(substitute(varParam.type()), varParam.name(), varParam.mods(), varParam.annotations());
        }

        private AbstractJType substitute(AbstractJType type) {
            type = visitor.getNarrowedType(type);
            for (int i = 0; i < methodTypeArguments.length; i++) {
                type = Source.substitute(type, interfaceMethod.typeParams()[i], methodTypeArguments[i]);
            }
            return type;
        }

        public String name() {
            return interfaceMethod.name();
        }

        public JMods mods() {
            return interfaceMethod.mods();
        }

        public JTypeVar[] typeParams() {
            return interfaceMethod.typeParams();
        }

        public boolean hasVarArgs() {
            return interfaceMethod.hasVarArgs();
        }
    }

    private static class SpecialTypeVariables {
        private final JTypeVar resultTypeParameter;
        private final JTypeVar exceptionTypeParameter;
        private final JTypeVar selfTypeParameter;
        private SpecialTypeVariables(JTypeVar resultTypeParameter, JTypeVar exceptionTypeParameter, JTypeVar selfTypeParameter) {
            this.resultTypeParameter = resultTypeParameter;
            this.exceptionTypeParameter = exceptionTypeParameter;
            this.selfTypeParameter = selfTypeParameter;
        }

        /**
         * Substitutes special type-variables with provided types.
         *
         * Substitution is deep and is performed through out type's type-variables.
         * <p>
         * For example {@code Tree<R>} is replaced with {@code Tree<String>}
         * when R is special result-type-variable and String is provided as
         * result-type.
         *
         * @param type type to substitute
         * @param selfType special self-type to replace self-type-variables with
         * @param resultType special result-type to replace result-type-variables with
         * @param exceptionType special exception-type to replace exception-type-variables with
         * @return resulting substitution
         */
        private AbstractJType substituteSpecialType(AbstractJType type,
                                            AbstractJClass selfType,
                                            AbstractJClass resultType,
                                            AbstractJClass exceptionType) {
            if (type instanceof AbstractJClass)
                return substituteSpecialType((AbstractJClass)type, selfType, resultType, exceptionType);
            else
                return type;
        }

        /**
         * Substitutes special type-variables with provided types.
         *
         * Substitution is deep and is performed through out type's type-variables.
         * <p>
         * For example {@code Tree<R>} is replaced with {@code Tree<String>}
         * when R is special result-type-variable and String is provided as
         * result-type.
         *
         * @param type type to substitute
         * @param selfType special self-type to replace self-type-variables with
         * @param resultType special result-type to replace result-type-variables with
         * @param exceptionType special exception-type to replace exception-type-variables with
         * @return resulting substitution
         */
        private AbstractJType substituteSpecialType(AbstractJClass type,
                                             AbstractJClass selfType,
                                             AbstractJClass resultType,
                                             AbstractJClass exceptionType) {
            AbstractJType result = type;
            result = Source.substitute(result, selfTypeParameter, selfType);
            result = Source.substitute(result, resultTypeParameter, resultType);
            result = Source.substitute(result, exceptionTypeParameter, exceptionType);
            return result;
        }

        private boolean isSpecial(AbstractJType type) {
            return type != null
                   && (isSelf(type)
                       || isResult(type)
                       || isException(type));
        }

        private boolean isSelf(AbstractJType type) {
            return type == selfTypeParameter;
        }

        private boolean isResult(AbstractJType type) {
            return type == resultTypeParameter;
        }

        private boolean isException(AbstractJType type) {
            return type == exceptionTypeParameter;
        }

        private JTypeVar resultTypeParameter() {
            return resultTypeParameter;
        }

        private JTypeVar exceptionTypeParameter() {
            return exceptionTypeParameter;
        }

        private JTypeVar selfTypeParameter() {
            return selfTypeParameter;
        }
    }
}
