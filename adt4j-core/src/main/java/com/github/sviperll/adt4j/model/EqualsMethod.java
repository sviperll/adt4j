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
import com.helger.jcodemodel.JVar;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class EqualsMethod {
    private final Types types;
    private final JBlock body;
    private final VariableNameSource nameSource;

    EqualsMethod(Types types, JBlock body, VariableNameSource nameSource) {
        this.types = types;
        this.body = body;
        this.nameSource = nameSource;
    }

    void appendNullableValue(AbstractJType type, IJExpression value1, IJExpression value2) {
        if (!type.isReference()) {
            throw new AssertionError("appendNullableValue called for non-reference type");
        } else {
            JConditional _if = body._if(value1.ne(value2));
            IJExpression isNull = value1.eq(JExpr._null()).cor(value2.eq(JExpr._null()));
            JConditional _if1 = _if._then()._if(isNull);
            _if1._then()._return(JExpr.FALSE);
            EqualsMethod innerBody = new EqualsMethod(types, _if._then(), nameSource);
            innerBody.appendNotNullValue(type, value1, value2);
        }
    }

    void appendNotNullValue(AbstractJType type, IJExpression value1, IJExpression value2) {
        if (type.isArray()) {
            appendNotNullValue(types._int, value1.ref("length"), value2.ref("length"));
            VariableNameSource localNames = nameSource.forBlock();
            JForLoop _for = body._for();
            JVar i = _for.init(types._int, localNames.get("i"), JExpr.lit(0));
            _for.test(i.lt(value1.ref("length")));
            _for.update(i.incr());
            EqualsMethod forBody = new EqualsMethod(types, _for.body(), localNames);
            if (type.elementType().isReference())
                forBody.appendNullableValue(type.elementType(), value1.component(i), value2.component(i));
            else
                forBody.appendNotNullValue(type.elementType(), value1.component(i), value2.component(i));
        } else if (type.isPrimitive()) {
            JConditional _if = body._if(value1.ne(value2));
            _if._then()._return(JExpr.FALSE);
        } else {
            JInvocation invocation = value1.invoke("equals");
            invocation.arg(value2);
            JConditional _if = body._if(invocation.not());
            _if._then()._return(JExpr.FALSE);
        }
    }

}
