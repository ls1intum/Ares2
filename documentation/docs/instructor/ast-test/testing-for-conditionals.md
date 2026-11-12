---
title: "Testing for Conditionals"
sidebar_position: 2
description: "Ruling out if, the conditional operator and switch with ConditionalType."
---

:::tip[Simple Story]
The paper says "decide without asking". The pupil has to sort the penguins by a rule that needs
no "if this, then that", perhaps through a table or a formula.

The mark scheme lists the forms of asking that cost the marks: an `if`, the short question
`?:` and the long list of cases called `switch`. While marking, the teacher looks for each of
them on the answer sheet.
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

// policy-focus-start
    /** Checks that the source contains no if, no ?: and no switch. */
    @PublicTest
    void usesNoConditionals() {
        UnwantedNodesAssert.assertThatProjectSources()
                .withinPackage("org.example")
                .withLanguageLevel(LanguageLevel.JAVA_17)
                .excludeMainMethod()
                .hasNo(ConditionalType.ANY);
    }
// policy-focus-end

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

Implemented by `ConditionalType` in
[`ast/type/ConditionalType.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/ast/type/ConditionalType.java).
The abstract syntax tree (AST) check reports each find under the name in the second column.

| Constant | Reported as | What it finds | Example |
| --- | --- | --- | --- |
| `ANY` | All four names below | Every construct of the rows below | Any example below |
| `ANY_IF` | `If-Statement`, `If-Expression` | `if` and the conditional operator | The examples of the next two rows |
| `IFSTMT` | `If-Statement` | `if`, with or without `else` | `if (a > b) { return a; }` |
| `CONDITIONALEXPR` | `If-Expression` | The conditional operator | `return a > b ? a : b;` |
| `ANY_SWITCH` | `Switch-Statement`, `Switch-Expression` | Both forms of `switch` | The examples of the next two rows |
| `SWITCHSTMT` | `Switch-Statement` | A `switch` statement, with `case 1:` or with `case 1 ->` | `switch (day) { case 1 -> rest(); default -> work(); }` |
| `SWITCHEXPR` | `Switch-Expression` | A `switch` that yields a value | `int hours = switch (day) { case 1 -> 0; default -> 8; };` |

## Notes

- `IFSTMT` does not cover the conditional operator. Use `ANY_IF` when the exercise forbids both.
- A decision without one of these constructs passes. The check does not report `&&` and `||`,
  `Optional.ifPresentOrElse(...)`, a lookup in a `Map` or a call that picks its method through
  inheritance.
- `assert` belongs to [exception handling](./testing-for-exception-handling.md), not to this family.
