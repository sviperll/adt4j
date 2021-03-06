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

import com.github.sviperll.adt4j.model.config.FloatCustomization;
import com.github.sviperll.adt4j.model.util.Types;
import com.github.sviperll.adt4j.model.util.VariableNameSource;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JForLoop;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JOp;
import com.helger.jcodemodel.JVar;
import java.text.MessageFormat;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class CompareToMethod {
    private final Types types;
    private final JBlock methodBody;
    private final VariableNameSource methodNameSource;
    private final FloatCustomization floatCustomization;

    CompareToMethod(Types types, JBlock methodBody, VariableNameSource methodNameSource, FloatCustomization floatCustomization) {
        this.types = types;
        this.methodBody = methodBody;
        this.methodNameSource = methodNameSource;
        this.floatCustomization = floatCustomization;
    }

    CompareToMethod.Body createBody() {
        JVar resultVariable = methodBody.decl(types._int, methodNameSource.get("result"));
        return new Body(resultVariable, methodBody, methodNameSource);
    }

    class Body {

        private final JVar resultVariable;
        private final JBlock body;
        private final VariableNameSource nameSource;

        private Body(JVar resultVariable, JBlock body, VariableNameSource nameSource) {
            this.resultVariable = resultVariable;
            this.body = body;
            this.nameSource = nameSource;
        }

        void appendNullableValue(AbstractJType paramType, IJExpression field1, IJExpression field2) {
            JBlock _then = body._if(field1.ne(JExpr._null()).cor(field2.ne(JExpr._null())))._then();
            JConditional _if = _then._if(field1.eq(JExpr._null()));
            _if._then()._return(JExpr.lit(-1));
            JConditional _elseif = _if._elseif(field2.eq(JExpr._null()));
            _elseif._then()._return(JExpr.lit(1));
            Body ifbody = new Body(resultVariable, _elseif._else(), nameSource);
            ifbody.appendNotNullValue(paramType, field1, field2);
        }

        void appendNotNullValue(AbstractJType type, IJExpression value1, IJExpression value2) {
            if (!type.isPrimitive() && !type.isArray()) {
                appendNonnullComparable(value1, value2);
            } else if (type.isPrimitive()) {
                appendPrimitive(type, value1, value2);
            } else if (type.isArray()) {
                appendNonnullArray(value1, value2, type);
            } else {
                throw new IllegalStateException(MessageFormat.format("Unsupported type {0} when generating compareTo method!", type));
            }
        }

        private void appendNonnullArray(IJExpression value1, IJExpression value2, AbstractJType type) {
            JInvocation invocation = types._Math.staticInvoke("min");
            invocation.arg(value1.ref("length"));
            invocation.arg(value2.ref("length"));
            JVar length = body.decl(types._int, nameSource.get("length"), invocation);
            VariableNameSource localNames = nameSource.forBlock();
            JForLoop _for = body._for();
            JVar i = _for.init(types._int, localNames.get("i"), JExpr.lit(0));
            _for.test(i.lt(length));
            _for.update(i.incr());
            Body forBody = new Body(resultVariable, _for.body(), localNames);
            if (type.elementType().isReference())
                forBody.appendNullableValue(type.elementType(), value1.component(i), value2.component(i));
            else
                forBody.appendNotNullValue(type.elementType(), value1.component(i), value2.component(i));
            appendNotNullValue(types._int, value1.ref("length"), value2.ref("length"));
        }

        private void appendPrimitive(AbstractJType type, IJExpression value1, IJExpression value2) {
            IJExpression equalityCondition;
            if (!type.name().equals("float") && !type.name().equals("doable")) {
                equalityCondition = value1.eq(value2);
            } else {
                IJExpression epsilon = type.name().equals("float") ? JExpr.lit(floatCustomization.floatEpsilon()) : JExpr.lit(floatCustomization.doubleEpsilon());
                JInvocation invocation = types._Math.staticInvoke("abs");
                invocation.arg(value1.minus(value2));
                equalityCondition = invocation.lte(epsilon);
            }
            IJExpression condition = JOp.cond(equalityCondition, JExpr.lit(0), JOp.cond(value1.lt(value2), JExpr.lit(-1), JExpr.lit(1)));
            body.assign(resultVariable, condition);
            JConditional _if = body._if(resultVariable.ne(JExpr.lit(0)));
            _if._then()._return(resultVariable);
        }

        private void appendNonnullComparable(IJExpression value1, IJExpression value2) {
            JInvocation invocation = value1.invoke("compareTo");
            invocation.arg(value2);
            body.assign(resultVariable, invocation);
            JConditional _if = body._if(resultVariable.ne(JExpr.lit(0)));
            _if._then()._return(resultVariable);
        }
    }

}
