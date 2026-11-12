---
title: "Testing Classes and Records"
sidebar_position: 5
description: "Ruling out classes and records declared inside a body of code with ClassType."
---

:::tip[Simple Story]
The paper says "use the classes on the paper". The board of education wants the pupil to work
with the existing penguin types, not to invent a small helper type halfway through an answer.

The mark scheme names the two kinds of helper type a pupil can write in the middle of an answer:
a local class and a local record. The teacher looks for both on the answer sheet.
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

// policy-focus-start
    /** Checks that the source declares no class and no record inside a body of code. */
    @PublicTest
    void declaresNoLocalClassesOrRecords() {
        UnwantedNodesAssert.assertThatProjectSources()
                .withinPackage("org.example")
                .withLanguageLevel(LanguageLevel.JAVA_17)
                .excludeMainMethod()
                .hasNo(ClassType.ANY);
    }
// policy-focus-end

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

Implemented by `ClassType` in
[`ast/type/ClassType.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/ast/type/ClassType.java).
The abstract syntax tree (AST) check reports each find under the name in the second column.

| Constant | Reported as | What it finds | Example |
| --- | --- | --- | --- |
| `ANY` | Both names below | Every construct of the rows below | Any example below |
| `CLASS` | `Local-Class-Statement` | A class or an interface declared inside a body of code | `void sort() { class Counter { int seen; } }` |
| `RECORD` | `Local-Record-Statement` | A record declared inside a body of code | `void sort() { record Pair(Penguin a, Penguin b) { } }` |

## Notes

- "Inside a body of code" means wherever Java allows a local declaration: a method, a constructor,
  an initialiser block or the block of a lambda.
- The check does not report a top-level class, a class or record nested directly in another class,
  an anonymous class or a lambda. Code inside their bodies stays searchable by the other families.
- A local interface counts as `Local-Class-Statement`.
- A local `enum` makes JavaParser 3.28.2 fail on the file, so the test ends with an error instead of
  a failure message, at every language level.
