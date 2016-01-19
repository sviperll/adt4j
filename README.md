adt4j - Algebraic Data Types for Java
=====================================

This library implements [Algebraic Data Types](http://en.wikipedia.org/wiki/Algebraic_data_type) for Java.
ADT4J provides annotation processor for @GenerateValueClassForVisitor annotation.
ADT4J generates new class for each @GenerateValueClassForVisitor annotation.

It allows you to easily define custom data types. Like this:

```java
    // Define Expression data type
    @GenerateValueClassForVisitor
    @Visitor(resultVariableName="R", selfReferenceVariableName="E")
    interface ExpressionVisitor<E, R> {
        @GeneratePredicate(name = "isLiteral");
        R lit(int i);

        R sum(@Getter(name = "leftOperand") E e1, @Getter(name = "rightOperand") E e2);
        R mul(@Getter(name = "leftOperand") E e1, @Getter(name = "rightOperand") E e2);
    }
```

And that's it. `Expression` class will be generated and you'll be able to define expressions like this:

```java
    import static ...Expression.*;

    /* ... */

    Expression e = mul(sum(lit(5), lit(1)), lit(2));
```

You can process expressions with "pattern-matching" a. k. a. visitor-pattern:

```java
    int value = e.accept(new ExpressionVisitor<Expression, Integer>() {
        @Override
        public Integer int(int i) {
            return i;
        }
        @Override
        public Integer sum(Expression e1, Expression e2) {
            return e1.accept(this) + e2.accept(this);
        }
        @Override
        public Integer mul(Expression e1, Expression e2) {
            return e1.accept(this) * e2.accept(this);
        }
    });
```

Features
--------

 * Support recursive data types
 * Generate hashCode, equals and toString implementations with value semantics
 * Generate predicates, getters and "updaters" with additional annotations
 * Fully customizable API: custom names and access levels for generated methods
 * Optionally generate Comparable implementation with precise compile-time type-check if it is possible
 * Optionally generate serializable classes with precise compile-time type-check if it is possible
 * Sensible error messages
 * Support generated class extention through standard Java's inheritance.
 * Reasonably fast

Known Issues
------------

 * maven-compiler-plugin version 3.2 and later doesn't work nicely with
   annotation processors, see [MCOMPILER-235](https://issues.apache.org/jira/browse/MCOMPILER-235).
   Only clean builds work. Repeated compilation causes duplicate class errors.

 * It is possible to support explicit recursive data-types definitions
   without `selfReferenceVariableName` hack, but
   [javac bug](http://mail.openjdk.java.net/pipermail/compiler-dev/2015-November/009864.html)
   prevents it from working. It works when no type-parameters are used,
   see (IntListVisitor.java example)[https://github.com/sviperll/adt4j/blob/master/adt4j-examples/src/main/java/com/github/sviperll/adt4j/examples/IntListVisitor.java].

License
-------

ADT4J is under BSD 3-clause license.

Flattr
------

[![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=sviperll&url=https%3A%2F%2Fgithub.com%2Fsviperll%2Fadt4j&title=adt4j&language=Java&tags=github&category=software)

Installation
------------

Use maven dependency to use ADT4J:

```xml
    <dependency>
        <groupId>com.github.sviperll</groupId>
        <artifactId>adt4j</artifactId>
        <version>3.1</version>
    </dependency>
```

You can use `adt4j-shaded` artifact to simplify deployment and to avoid dependencies' conflicts.
`adt4j-shaded` has no dependencies and does not pollute classpath.
All java-packages provided by `adt4j-shaded` are rooted at `com.github.sviperll.adt4j` package.

```xml
    <dependency>
        <groupId>com.github.sviperll</groupId>
        <artifactId>adt4j-shaded</artifactId>
        <version>3.1</version>
    </dependency>
```


Changelog
---------

See [NEWS file](https://github.com/sviperll/adt4j/blob/master/NEWS.md).

Usage
-----

See [Tutorial](https://github.com/sviperll/adt4j/wiki/Tutorial)

Build
-----

    $ git clone git@github.com:sviperll/adt4j.git
    $ cd adt4j
    $ mvn test

Check for errors and warnings.

ADT4J is built to be compatible with Java 7.
See [universal-maven-parent](https://github.com/sviperll/universal-maven-parent) project's documentation
for instructions about building projects compatible with JDK7.
