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
public @interface DataVisitor {
    String result();
    String exception() default ":none";
    String self() default ":none";
    String className() default ":auto";
    boolean isPublic() default false;
    int hashCodeBase() default 27;
}
