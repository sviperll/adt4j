adt4j - Algebraic Data Types for Java
=====================================

Changelog
---------

Since 3.2

 * Optimized updaters/withers: avoid unnecessary allocation
 * New @WrapsGeneratedValueClass annotation to support automatic wrapping, see Expression class README
 * Configurable floatEpsilon and doubleEpsilon for generated floating-point comparisons

Since 3.1

 * Always generate code, even in presence of compilation errors
 * Implement support for explicit recursion without `selfReferenceVariableName` hack
 * Split README file into more manageable/readable parts

Since 3.0

 * No more classes from chicory* packages in public API
 * adt4-shaded artifact should truly have no dependencies
 * javax.annotation.Nullable, javax.annotation.Nonnull and javax.annotation.ParametersAreNonnullByDefault
   are used only if these classes are present in classpath during compilation

Since 2.0

 * Better generated code: no FireBugs warnings
 * Add adt4-shaded artifact without any dependencies.
 * Switch to Java 7 as minimal java version.

Since 1.3

 * Add `hashCodeCaching` parameter to `@GenerateValueClassForVisitor` annotation to support
   hash code caching, see `RecordVisitor`, `GroupNameVisitor`, `UserKeyVisitor` and `Expression` examples.

Since 1.2

 * Add support for multiple predicate generation for single method with new `@GeneratePredicates` annotation.

Since 1.1

 * As little information from source code as possible is used during code generation.
   It's now possible to define fully customized single file data-type definitions, see `Either` example.

 * metachicory is not required at runtime, since `@Visitor`-annotation retention is set to `SOURCE` now.
   ADT4J has no run-time dependencies now.

Since 1.0

 * Add default names for generated getters, updaters and predicates. Allow to omit name parameter.
 * API-breaking change: rename value argument to name argument of @Getter, @Updater and @GeneratePredicate annotations
 * API-breaking change: use com.github.sviperll.meta.Visitor annotation from metachicory package.
 * Add dependency to metachicory package which provides some generic metaprogramming support.

Since 0.14

 * Fully customizable access levels for generated API

Since 0.13

 * Lots of performance improvements

Since 0.12

 * Fix recursive types support (see `TreeVisitor` example)
 * Fix varargs support
 * Code cleanup
 * Rename `adt4j-core` artifact to `adt4j`

Since 0.11

 * Nullable and Nonnull annotations on generated methods
 * No warnings from generated code

Since 0.10

 * Use maven as build system

Since 0.9

 * Predicates can be generated to test for specific case
 * Comparable instances can be generated
