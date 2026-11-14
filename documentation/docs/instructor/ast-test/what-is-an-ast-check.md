---
title: "What is an AST Check and how is it structured"
sidebar_position: 1
description: "What an abstract syntax tree check reads, what it needs before it runs, and the chain of calls every check follows."
---

:::tip[Simple Story]
Next to a question, the head teacher can write instructions forbidding certain words and ways
of answering, such as "solve it without a loop". The teacher keeps to them while marking by the
grading instructions.

The finished answer cannot settle this, because two pupils can reach the same answer in
different ways. So the teacher reads what the pupil wrote and looks for the forbidden words.

These instructions guard nothing. A pupil can reach the same result with words the instructions
do not name, so the head teacher still needs questions that check the answer itself.

[Who is who](/instructor/who-is-who)
:::

## What an AST check is

An abstract syntax tree (AST) check reads the source files of the student code. JavaParser turns
each file into a tree of language constructs, and the check fails the test when it finds a
construct the exercise rules out. It answers questions such as "did the student use a loop?" or
"is the solution recursive?", which no check at runtime can answer.

Every check names what has to be absent. `hasNo(LoopType.ANY)` reads as the requirement itself:
the solution has no loop. Recursion is the one exception, because an exercise often asks for it,
so `hasRecursion()` exists beside `hasNoRecursion()`.

:::warning[Not a security boundary]
An AST check reads source code and nothing else. It never sees what the code does when it runs,
and a student can reach the same behaviour through a construct the check does not name, for
example `list.forEach(...)` in place of a `for` loop. Pair every AST check with a functional test.
Leave files, network, commands and threads to the [security policy](/instructor/policy-reference/).
:::

## Before you write one

| Requirement | Why |
| --- | --- |
| Ares is a test dependency of the exercise | The checks ship in the Ares library, which every Postcompile exercise declares as a dependency. The generated Precompile setup does not include these assertions, so a Precompile exercise only gets them if you add the Ares dependency yourself. |
| Under a `@Policy`, the test class appears in `theFollowingClassesAreTestClasses` | The check reads the source files from disk. Ares treats a test class missing from that list as student code and blocks the read with a `SecurityException`. |
| Without a `@Policy`, nothing further | The policy-free configuration finds the test classes itself, so the check runs. |

## The chain of calls

Every check is one chain of calls, read from left to right.

| Step | Method | Required | What it does |
| --- | --- | --- | --- |
| 1 | `assertThatProjectSources()` or `assertThatSourcesIn(Path)` | One of the two | Chooses the source folder. The first finds it through the build file, the second takes a path relative to the working directory of the test run. |
| 2 | `withinPackage("org.example")` | No | Narrows the folder to one package and every package below it. |
| 3 | `withLanguageLevel(LanguageLevel.JAVA_17)` | Yes | Sets the Java version the parser accepts. Without it, every check fails with "The 'level' is not set." |
| 4 | `excludeMainMethod()` | No, and only on `UnwantedNodesAssert` | Leaves the `main` method out of the search. |
| 5 | `hasNo(...)`, `hasRecursion()` or `hasNoRecursion()` | Yes | Runs the check. `hasNo` returns the chain, so several of them can follow one another. |

`LanguageLevel` is the enumeration `com.github.javaparser.ParserConfiguration.LanguageLevel`.
Choose the Java version the exercise targets.

```java
UnwantedNodesAssert.assertThatProjectSources()
        .withinPackage("org.example")
        .withLanguageLevel(LanguageLevel.JAVA_17)
        .excludeMainMethod()
        .hasNo(LoopType.ANY)
        .hasNo(ConditionalType.ANY_SWITCH);
```

## Where the check looks for the sources

`assertThatProjectSources()` reads the build file in the working directory of the test run.

| Build | What the check reads |
| --- | --- |
| Maven, `pom.xml` | The first `<sourceDirectory>` inside `<build>`. Without one, `src/main/java`. |
| Gradle, `build.gradle` | Only a literal assignment such as `def assignmentSrcDir = 'assignment/src'`, which the Artemis template contains. Source sets do not count, and there is no fallback. |
| Gradle, only `build.gradle.kts` | Call `AresConfiguration.setBuildGradlePath("build.gradle.kts")` first. Without that call, the check tries `build.gradle` and fails. |
| Both `pom.xml` and a Gradle file | Maven wins. |

When the check finds no folder, it fails with "Could not find project sources folder." For that
case, and for any layout the table does not cover, name the folder yourself:

```java
import java.nio.file.Path;

UnwantedNodesAssert.assertThatSourcesIn(Path.of("src/main/java"))
        .withLanguageLevel(LanguageLevel.JAVA_17)
        .hasNo(LoopType.ANY);
```

## What the student sees

A failing check lists every file, every construct and every position it found. Ares translates the
message into the language of the test run; in English it reads like this:

```text
Unwanted statement found:
 - In assignment/src/org/example/Penguin.java:
  - For-Statement was found:
   - Between line 12 (column 9) and line 14 (column 9)
```

## The pages of this section

The five pages after this one take one family each. Every page shows the same example test class,
`PenguinStructureTest`, with its own test method marked in red, then a table of what the family can
name, then notes on what it does not see. Read in sidebar order, the pages walk the class from top
to bottom.

| Page | What it covers |
| --- | --- |
| [Testing for Conditionals](./testing-for-conditionals.md) | `if`, the conditional operator `?:` and `switch` |
| [Testing for Loops](./testing-for-loops.md) | `for`, the enhanced `for`, `while` and `do`-`while` |
| [Testing for Exception Handling](./testing-for-exception-handling.md) | `assert`, `throw` and `catch` |
| [Testing Classes and Records](./testing-classes-and-records.md) | Classes and records declared inside a body of code |
| [Testing for Recursion](./testing-for-recursion.md) | Recursive calls, required or ruled out |

## What is not here

How the parser, the visitor and the call graph work inside Ares is part of the contributor guide,
under [AST](/contributor/subsystems/ast).

## Notes

- A check searches the whole file, not only the method bodies. A conditional operator in a field
  initialiser and a loop in a `static` block both count.
- A folder that exists but holds no `.java` files passes every `hasNo`. "Nothing found" and
  "nothing checked" look the same, so point the check at the folder that holds the student code.
- The first `hasNo` that finds something ends the test. The student sees that family only, and the
  next run reports the next one.
- `excludeMainMethod()` removes a `public static void main` only when its parameter reads
  `String[]`, as in `String[] args` or `String args[]`. It keeps `main(String... args)` and
  `main(java.lang.String[] args)`, so the constructs inside them still count.
- A file the parser cannot read ends the test with an error instead of a failure message. Syntax
  newer than the language level does that, and so does a local `enum`, which JavaParser 3.28.2
  cannot parse at any language level. The recursion check differs here, as
  [Testing for Recursion](./testing-for-recursion.md#notes) explains.
