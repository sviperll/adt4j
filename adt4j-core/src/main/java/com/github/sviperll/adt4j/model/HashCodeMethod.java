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

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class HashCodeMethod {
    private final Types types;
    private final int hashCodeBase;
    private final JBlock methodBody;
    private final VariableNameSource methodNameSource;

    HashCodeMethod(Types types, int hashCodeBase, JBlock methodBody, VariableNameSource methodNameSource) {
        this.types = types;
        this.hashCodeBase = hashCodeBase;
        this.methodBody = methodBody;
        this.methodNameSource = methodNameSource;
    }

    Body createBody(int tag) {
        JVar result = methodBody.decl(types._int, methodNameSource.get("result"), JExpr.lit(tag));
        return new Body(result, methodBody, methodNameSource);
    }

    class Body {

        private final JBlock body;
        private final VariableNameSource nameSource;
        private final JVar result;

        private Body(JVar result, JBlock body, VariableNameSource nameSource) {
            this.result = result;
            this.body = body;
            this.nameSource = nameSource;
        }

        void appendNullableValue(AbstractJType type, IJExpression value) {
            if (!type.isReference())
                throw new AssertionError("appendNullableValue called for non-reference type");
            else {
                JConditional _if = body._if(value.eq(JExpr._null()));
                Body thenBody = new Body(result, _if._then(), nameSource);
                thenBody.appendNotNullValue(types._int, JExpr.lit(0));
                Body elseBody = new Body(result, _if._else(), nameSource);
                elseBody.appendNotNullValue(type, value);
            }
        }

        void appendNotNullValue(AbstractJType type, IJExpression value) {
            if (type.isArray()) {
                VariableNameSource localNames = nameSource.forBlock();
                JForLoop _for = body._for();
                JVar i = _for.init(types._int, localNames.get("i"), JExpr.lit(0));
                _for.test(i.lt(value.ref("length")));
                _for.update(i.incr());
                Body forBody = new Body(result, _for.body(), localNames);
                if (type.elementType().isReference())
                    forBody.appendNullableValue(type.elementType(), value.component(i));
                else
                    forBody.appendNotNullValue(type.elementType(), value.component(i));
            } else if (!type.isPrimitive()) {
                appendNotNullValue(types._int, value.invoke("hashCode"));
            } else if (type.name().equals("double")) {
                JInvocation invocation = types._Double.staticInvoke("doubleToLongBits");
                invocation.arg(value);
                appendNotNullValue(types._long, invocation);
            } else if (type.name().equals("float")) {
                JInvocation invocation = types._Float.staticInvoke("floatToIntBits");
                invocation.arg(value);
                appendNotNullValue(types._int, invocation);
            } else if (type.name().equals("boolean")) {
                appendNotNullValue(types._int, JOp.cond(value, JExpr.lit(0), JExpr.lit(1)));
            } else if (type.name().equals("long")) {
                appendNotNullValue(types._int, JExpr.cast(types._int, value.xor(value.shrz(JExpr.lit(32)))));
            } else {
                body.assign(result, result.mul(JExpr.lit(hashCodeBase)).plus(value));
            }
        }

        IJExpression result() {
            return result;
        }
    }

}
