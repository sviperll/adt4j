/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.adt4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface ValueVisitor {
    String resultVariableName();
    String exceptionVariableName() default ":none";
    String selfReferenceVariableName() default ":none";
    String valueClassName() default ":auto";
    boolean valueClassIsPublic() default false;
    int valueClassHashCodeBase() default 27;
}
