/*
 * Copyright (c) 2015, Victor Nazarov <asviraspossible@gmail.com>
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
package com.github.sviperll.adt4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark interfaces used as <em>visitors</em>.
 * <p>
 * Visitor definition is used in context of <em>Visitor-pattern</em>.
 * <p>
 * Visitor interfaces are to be generic and declare at least one type-variable.
 * Mandatory type-variable is the result of "visit".
 * <p>
 * Suppose that you have declared interface <tt>MyVisitorInterface</tt>
 * 
 * <blockquote><pre><code>
 *     interface MyVisitorInterface&lt;R&gt; {
 *         R variant1(...);
 *         R variant2(...);
 *     }
 * </code></pre></blockquote>
 * <p>
 * When you actually use an instance of visitor-interface to visit some data-type
 * the result of "visit" is always the type bounded by result-type-variable.
 * <p>
 * <blockquote><pre><code>
 *     MyDataType type = ...;
 *     MyVisitorInterface&lt;Integer&gt; integerVisitor = ...;
 *     Integer visitResult = type.accept(integerVisitor);
 * </code></pre></blockquote>
 * <p>
 * Result of visit is Integer in an example above, but can be any other type.
 * String is used in an example below:
 * <p>
 * <blockquote><pre><code>
 *     MyVisitorInterface&lt;String&gt; stringVisitor = ...;
 *     String visitResult = type.accept(stringVisitor);
 * </code></pre></blockquote>
 * <p>
 * Annotation arguments are used to declare roles of type-variables.
 * <p>
 * <tt>MyVisitorInterface</tt> from an example above should be annotated like this:
 * <p>
 * <blockquote><pre><code>
 *     &#64;Visitor(resultVariableName = "R")
 *     interface MyVisitorInterface&lt;R&gt; {
 *         R variant1(...);
 *         R variant2(...);
 *     }
 * </code></pre></blockquote>
 * <p>
 * All interface methods are to have result-type-variable as a return-type.
 * <p>
 * Visitor interface can have an exception-type variable
 * <p>
 * <blockquote><pre><code>
 *     &#64;Visitor(resultVariableName = "R", exceptionVariableName = "E")
 *     interface MyVisitorInterface2&lt;R, E&gt; {
 *         R variant1(...) throws E;
 *         R variant2(...) throws E;
 *     }
 * </code></pre></blockquote>
 * <p>
 * In this case every-visit can potentially throw an exception of type bounded by exception-type-variable
 * <p>
 * <blockquote><pre><code>
 *     MyDataType2 type = ...;
 *     MyVisitorInterface2&lt;Integer, IOException&gt; integerIOVisitor = ...;
 *     Integer visitResult1;
 *     try {
 *         visitResult1 = type.accept(integerIOVisitor);
 *     } catch (IOException ex) {
 *         ...
 *     }
 *
 *     MyVisitorInterface2&lt;String, SQLException&gt; stringSQLVisitor = ...;
 *     String visitResult2;
 *     try {
 *         visitResult2 = type.accept(stringSQLVisitor);
 *     } catch (SQLException ex) {
 *         ...
 *     }
 * </code></pre></blockquote>
 * <p>
 * When exception type-variable is used all methods should declare this type variable in the list of thrown exceptions.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Visitor_pattern">Wikipedia article</a>
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface Visitor {
    /**
     * Name of type-variable that denotes "visit" result type.
     *
     * @see com.github.sviperll.adt4j.Visitor
     */
    String resultVariableName();

    /**
     * Name of type-variable that denotes an "visit" exception type.
     * <p>
     * No exception type is used if this annotation argument is omitted.
     *
     * @see com.github.sviperll.adt4j.Visitor
     */
    String exceptionVariableName() default ":none";

    /**
     * Name of type-variable that denotes a data-type self-reference.
     * <p>
     * No self-reference type is used if this annotation argument is omitted.
     *
     * @see com.github.sviperll.adt4j.Visitor
     */
    String selfReferenceVariableName() default ":none";
}
