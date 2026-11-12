---
title: "Testing for Loops"
sidebar_position: 3
description: "Ruling out for, the enhanced for, while and do-while with LoopType."
---

:::tip[Simple Story]
The paper says "add up the weights of all penguins, without a loop". The board of education wants
to see recursion or a formula, not the pupil walking the list one penguin at a time.

The mark scheme names the four ways of walking: `for`, the enhanced `for` over a collection,
`while` and `do`-`while`. The teacher looks for each of them on the answer sheet.
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

// policy-focus-start
    /** Checks that the source contains no for, no for-each, no while and no do-while. */
    @PublicTest
    void usesNoLoops() {
        UnwantedNodesAssert.assertThatProjectSources()
                .withinPackage("org.example")
                .withLanguageLevel(LanguageLevel.JAVA_17)
                .excludeMainMethod()
                .hasNo(LoopType.ANY);
    }
// policy-focus-end

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

    /** Checks that the source contains at least one recursive chain of calls. */
    @PublicTest
    void solvesRecursively() {
        UnwantedRecursionAssert.assertThatProjectSources()
                .withLanguageLevel(LanguageLevel.JAVA_17)
                .hasRecursion();
    }
}
```

The example uses the Maven layout. For Gradle, write
`withinPath = "classes/java/main/org/example"`, and set `value` to wherever your policy file
lives. `PenguinStructureTest` has to appear in `theFollowingClassesAreTestClasses` of that
policy, as [the overview](./what-is-an-ast-check.md#before-you-write-one) explains.

## Constants

Implemented by `LoopType` in
[`ast/type/LoopType.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/ast/type/LoopType.java).
The abstract syntax tree (AST) check reports each find under the name in the second column.

| Constant | Reported as | What it finds | Example |
| --- | --- | --- | --- |
| `ANY` | All four names below | Every construct of the rows below | Any example below |
| `ANY_FOR` | `For-Statement`, `For-Each-Statement` | Both forms of `for` | The examples of the next two rows |
| `FORSTMT` | `For-Statement` | The counting `for` | `for (int i = 0; i < n; i++) { }` |
| `FOR_EACHSTMT` | `For-Each-Statement` | The enhanced `for` over an array or a collection | `for (Penguin p : penguins) { }` |
| `ANY_WHILE` | `While-Statement`, `Do-While-Statement` | Both forms of `while` | The examples of the next two rows |
| `WHILESTMT` | `While-Statement` | `while` | `while (n > 0) { n--; }` |
| `DO_WHILESTMT` | `Do-While-Statement` | `do`-`while` | `do { n--; } while (n > 0);` |

## Notes

- Repetition without one of these constructs passes. The check does not report
  `penguins.forEach(...)`, a stream such as `IntStream.range(0, n).sum()` or a recursive call.
  Combine this check with [Testing for Recursion](./testing-for-recursion.md) when the exercise asks
  for recursion in place of the loop.
- A loop with a label, such as `outer: for (...)`, counts like any other loop.
