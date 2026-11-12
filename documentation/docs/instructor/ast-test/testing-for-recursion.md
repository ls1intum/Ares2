---
title: "Testing for Recursion"
sidebar_position: 6
description: "Requiring or ruling out recursive calls with UnwantedRecursionAssert."
---

:::tip[Simple Story]
The paper says "solve it recursively". The board of education wants the pupil to answer the
question by asking themself a smaller version of it.

No single sentence on the answer sheet shows that. So the teacher draws a map of who asks whom,
one arrow for every call, and looks for a path of arrows that leads back to where it started. The
mark scheme says whether such a path has to be there or must not be.
:::

## Position in the example test class

Red marks the test method this page documents. Every page in this section shows the same example
class, so reading them in order walks it from top to bottom.

```java title="src/test/java/org/example/PenguinStructureTest.java (Maven)"
package org.example;

import com.github.javaparser.ParserConfiguration.LanguageLevel;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.ast.asserting.UnwantedNodesAssert;
import de.tum.cit.ase.ares.api.ast.asserting.UnwantedRecursionAssert;
import de.tum.cit.ase.ares.api.ast.type.ClassType;
import de.tum.cit.ase.ares.api.ast.type.ConditionalType;
import de.tum.cit.ase.ares.api.ast.type.ExceptionHandlingType;
import de.tum.cit.ase.ares.api.ast.type.LoopType;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/** One check per family. A real exercise keeps only the checks its task needs. */
@Policy(value = "src/test/resources/SecurityPolicy.yaml", withinPath = "classes/org/example")
public class PenguinStructureTest {

    /** Checks that the source contains no if, no ?: and no switch. */
    @PublicTest
    void usesNoConditionals() {
        UnwantedNodesAssert.assertThatProjectSources()
                .withinPackage("org.example")
                .withLanguageLevel(LanguageLevel.JAVA_17)
                .excludeMainMethod()
                .hasNo(ConditionalType.ANY);
    }

    /** Checks that the source contains no for, no for-each, no while and no do-while. */
    @PublicTest
    void usesNoLoops() {
        UnwantedNodesAssert.assertThatProjectSources()
                .withinPackage("org.example")
                .withLanguageLevel(LanguageLevel.JAVA_17)
                .excludeMainMethod()
                .hasNo(LoopType.ANY);
    }

    /** Checks that the source contains no assert, no throw and no catch. */
    @PublicTest
    void usesNoExceptionHandling() {
        UnwantedNodesAssert.assertThatProjectSources()
                .withinPackage("org.example")
                .withLanguageLevel(LanguageLevel.JAVA_17)
                .excludeMainMethod()
                .hasNo(ExceptionHandlingType.ANY);
    }

    /** Checks that the source declares no class and no record inside a body of code. */
    @PublicTest
    void declaresNoLocalClassesOrRecords() {
        UnwantedNodesAssert.assertThatProjectSources()
                .withinPackage("org.example")
                .withLanguageLevel(LanguageLevel.JAVA_17)
                .excludeMainMethod()
                .hasNo(ClassType.ANY);
    }

// policy-focus-start
    /** Checks that the source contains at least one recursive chain of calls. */
    @PublicTest
    void solvesRecursively() {
        UnwantedRecursionAssert.assertThatProjectSources()
                .withLanguageLevel(LanguageLevel.JAVA_17)
                .hasRecursion();
    }
// policy-focus-end
}
```

The example uses the Maven layout. For Gradle, write
`withinPath = "classes/java/main/org/example"`, and set `value` to wherever your policy file
lives. `PenguinStructureTest` has to appear in `theFollowingClassesAreTestClasses` of that
policy, as [the overview](./what-is-an-ast-check.md#before-you-write-one) explains.

## Methods

Implemented by `UnwantedRecursionAssert` in
[`ast/asserting/UnwantedRecursionAssert.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/ast/asserting/UnwantedRecursionAssert.java).
The abstract syntax tree (AST) check builds a call graph from the source: one node per method, one
edge per call inside it.

| Method | What it does | Example |
| --- | --- | --- |
| `hasRecursion()` | Fails with "Wanted recursion not found" when no chain of calls leads back to the method it started from | `.hasRecursion()` |
| `hasNoRecursion()` | Fails when such a chain exists, whether a method calls itself or several methods call one another, across classes too | `.hasNoRecursion()` |
| `startingWithMethod(String)` | Counts only the chains that the named method reaches | `.startingWithMethod("org.example.Penguin.countLetters(java.lang.String)")` |
| `excludeMethods(String...)` | Leaves the calls inside the named methods out of the graph, so a chain through them no longer counts | `.excludeMethods("org.example.Penguin.debugPrint(java.lang.String)")` |

A method name for `startingWithMethod` and `excludeMethods` takes the form of the package, the
class, the method and its parameter types in brackets, every type written out in full. A name in
any other form fails the test with "does not match the regular expression".

## Notes

- `hasRecursion()` without `startingWithMethod` accepts any recursive method in the folder, a helper
  the task does not concern included. Name the method the task asks about when that matters.
- A call inside a lambda in the method body counts. A method reference such as `this::count` does
  not, and neither does a constructor that creates a new instance of its own class.
- `startingWithMethod` and `excludeMethods` reject a name with a parameter of a primitive type such
  as `int`, and a name with more than one parameter. Neither of them can therefore name
  `fib(int)` today. Use them for methods without parameters or with one parameter of a reference
  type.
- `withinPackage(...)` moves the folder in which the parser looks up the other classes. A call from
  one class of the package into another can then end the test with an error. Leave it out of a
  recursion check and let `assertThatProjectSources()` read the whole source folder, as the example
  does.
- A file the parser cannot read behaves differently here than in the other families. Depending on
  how far the parser gets, the test ends with an error, or the file drops out of the call graph
  without a word, and `hasNoRecursion()` then passes without having seen it.
- The failure message names each method in the chain with its position. The file name it shows is
  the source folder followed by the class name, without the package folders in between.
