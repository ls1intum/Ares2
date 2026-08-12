---
title: "Further Important Options"
sidebar_position: 6
description: "Path access, testing before release, deadline extensions, threads, console interaction, networking and locale."
---

:::tip[ELI5]
The basics get an exercise working. This page is everything else you will eventually want.

How to test the exercise before students see it, how to give someone extra time, how to test a
program that talks to the console, and a few smaller knobs.
:::

The basics are covered by [Setup](setup.md) and [Test Annotations](test-annotations.md),
but there is more you need to know about testing with Ares 2. The earlier example used a single
class and very little testing. Without the knowledge below, you may not get Ares 2 to work and
will get rather annoyed, so please read on.

## Path Access and Class Loading

File access is default-deny, and is granted only through `regardingFileSystemInteractions` in
the YAML policy referenced by `@Policy`. Generated files are confined to the explicit canonical
project root. See the [Security Policy Manual](/contributor/subsystems/policy/security-policy-manual).

## Testing the Exercise before Release

Hidden tests are executed by Ares 2 only after the deadline. That raises the question of how
exercise creators are meant to work on the tasks, tests and sample solution. One option would
be to change the deadline temporarily, but then it is quite likely someone forgets to change it
back, and the protection of the hidden tests fails.

Use `@ActivateHiddenBefore` just like `@Deadline` to state the `LocalDateTime` before which
hidden tests should be executed. This date should of course lie before the release of the
exercise on Artemis.

## Extending a Deadline and Disability Compensation

Use `@ExtendedDeadline` together with a duration such as `1d` or `2d 12h 30m` to extend the
deadline by that amount. `@ExtendedDeadline("1d")`, for example, extends the deadline by one
day.

If you use the annotation at several levels (for example class and method) without stating a
new deadline (for example a deadline only at class level), the extensions add up.

## Threads and Concurrency

Thread creation is configured only through `regardingThreadCreations` in the active policy.
Specify the permitted class and the maximum count; omission means denial.

## Testing Console Interaction

One example showing some of the possibilities:

```java
void testSquareCorrect(IOTester tester) { // (1)
    tester.provideInputLines("5"); // (2)

    InputOutputPenguin.calculateSquare(); // (3)

    tester.err().assertThat().isEmpty(); // (4)
    tester.out().assertThat().isEqualTo("""
                Enter Number:
                Answer:
                25"""); // (5)
}
```

1. Declare `IOTester` as a parameter.
2. Provide input lines before calling the student code. This content is used for reading lines
   from `System.in`.
3. Call the student code to process the input and produce output.
4. Assert that nothing was printed to `System.err`.
5. Assert that the standard output (here excluding the final line break) equals the given text.
   If you use text blocks, be aware of their newline handling.

Ares 2 normalises line breaks to `\n`, and
[`OutputTester`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/io/OutputTester.java)
offers many different ways of checking output (single string, list of strings, and more).

If students read more lines than were provided, they get the following feedback:

```text
java.lang.IllegalStateException: no further console input request after the last(number 1: "5") expected.
```

See also `IOTester` and, for more examples, the
[`InputOutputUser`](https://github.com/ls1intum/Ares2/blob/main/src/test/java/de/tum/cit/ase/ares/integration/testuser/InputOutputUser.java)
test.

:::tip[Custom IO managers]
If the default `IOTester` does not meet your requirements, provide a custom implementation by
applying `@WithIOManager(MyCustomOne.class)` to, for example, the test class or individual
methods. This also lets you register a custom parameter to control IO testing conveniently
inside the test method. Have a look at the test class linked above, or read the documentation
of
[`IOManager`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/io/IOManager.java).
:::

## Networking

Network access is default-deny. Configure each host, explicit port and operation boolean under
`regardingNetworkConnections`; port `0` is the sole any-port wildcard. Narrow allowances remain
narrow at runtime, even though argument-insensitive static analysis cannot represent them.

## Locale

You can set a locale for Ares 2, and for the rest of Java, by adding the `@UseLocale` JUnit
extension to classes or methods. It sets the Java default locale via
`Locale.setDefault(Locale)`, which Ares 2 also uses. The locale is changed only for the scope
where the annotation is applied.

Ares 2 is currently localised in German (`de_DE`) and English (`en_US`), where `en_US` is the
fallback for any other locale.

See also the
[`LocaleUser`](https://github.com/ls1intum/Ares2/blob/main/src/test/java/de/tum/cit/ase/ares/integration/testuser/LocaleUser.java)
test for more examples.

## Exercises without a `@Policy` annotation

A security policy is not always necessary. If your supervised code is meant to touch no files, open no connections, run no commands and start no threads, you can omit the policy file entirely and let Ares apply its policy-free configuration, which grants none of those things. This section explains exactly what that configuration does, because it is easy to over-estimate in both directions.

Enforcement depends on **two** independent things: whether the Ares JUnit extension is registered, and whether a policy is present. Only the first is a precondition.

| Your test declares | Ares enforcement |
|---|---|
| A plain JUnit `@Test`, with or without `@Policy` | **None.** No Ares security code runs at all |
| An Ares test annotation (`@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest`), no `@Policy`, or a `@Policy` whose `value` is blank | The policy-free configuration described below |
| An Ares test annotation and a `@Policy` naming a policy file | That policy governs the five resource domains |
| An Ares test annotation and `@Policy(activated = false)` | **None.** This is the explicit opt-out |

The first row is the trap. `@Policy` is not itself a JUnit extension: it carries no `@ExtendWith`, and it registers nothing. What registers `JupiterSecurityExtension` is the `@JupiterAresTest` meta-annotation carried by `@Public`, `@Hidden`, `@PublicTest` and `@HiddenTest`. A test annotated only with `@Test` and `@Policy` therefore runs entirely unsupervised, and it does so silently, with no warning that the policy was never read.

> **Rule of thumb:** the Ares test annotation is what turns Ares on. The policy only decides how strict it then is.

### What the policy-free configuration actually restricts

With the extension registered and no policy present, Ares builds a restrictive configuration in which all five permission lists are empty:

- **File system, network, command execution and thread creation: denied.** No allowance exists in any of those domains, so nothing is permitted.
- **Package imports: denied *outside an implicit allowlist*.** This is the part that is commonly overstated. Ares always unions three sources into the permitted set: the essential packages it ships, the supervised package itself, and the packages of the recognised test classes. The shipped essential list includes the `java` prefix, so all of `java.*` remains importable. Package imports are restricted, not eliminated.
- **No default execution timeout applies yet.** The policy-free configuration does construct a 10,000 ms limit, but timeouts belong to the **Phobos** test-case family, which Ares 2.1.2 generates without yet dispatching it from the in-process execution path. That part of the pipeline has not been migrated across, so the limit does not bound a test today. Add [`@StrictTimeout`](./precompile-or-postcompile.md#glossary) wherever a test needs a deadline.

Two further points apply whether or not a policy is present:

- Ares installs fixed restrictions that no policy can grant, covering reflection, native access, JVM termination, class loading, JNDI and related domains. A policy governs the five resource domains, not everything.
- The reserved-package boundary of the reserved-package step is still required. It is a build-side check and does not depend on the Ares extension activating at all.

The policy-free path also **fixes the analysis and enforcement modes**: it always uses ArchUnit for static analysis and AspectJ for the runtime layer, and it discovers the build tool from the project itself. Two consequences follow:

1. The ByteBuddy agent is not the enforcing mechanism here. The AspectJ weaving configured in [the Gradle walkthrough](./postcompile/gradle.md) or [the Maven walkthrough](./postcompile/maven.md) is what enforces at runtime. A project that is not woven gets the static ArchUnit checks only.
2. Discovery has to succeed first, and it can fail. With no policy there is no explicitly selected build tool, so a project containing both a `pom.xml` and a `build.gradle` is rejected as ambiguous, and one containing neither is rejected as unsupported. Either failure happens **before** any enforcement is configured, so the restrictive configuration described above never takes effect in those cases; the build fails instead. See the [troubleshooting table](../troubleshooting.md).

A minimal test that runs under this configuration:

```java
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PenguinTest {

    @PublicTest
    void name() {
        assertEquals("Julian", new Penguin("Julian").getName());
    }
}
```

Note the absence of `@Test`: `@PublicTest` is itself a test annotation. If you prefer `@Public` you must add `@Test` alongside it, because `@Public` marks the test type without causing execution.

### What it derives from the project

With a policy, the enforcement scope and the trusted test classes are **pinned by the instructor**. Without one, Ares derives both by scanning the project, and the project includes the student's submission:

- **The supervised package** is chosen as the most frequent non-reserved package among the production sources. A submission whose file distribution differs from what you expect can therefore shift the scope away from the code you meant to supervise.
- **The exempt test classes** are collected by scanning the discovered test source roots for annotated test classes. If students can add files beneath a test source root, they can obtain that exemption. A nested test class is covered only when its enclosing class is also recognised, because the scanner reports nested types in source notation (`Outer.Inner`) while the exemption check matches binary notation (`Outer$Inner`).

Neither is a defect in the fallback; it is what a fallback with no instructor input can do. But both mean the policy-free path is only as trustworthy as your control over the source roots. With a policy present, `theFollowingClassesAreTestClasses` pins the exempt set and Ares never scans for it.

### When to use it

**Reasonable:**

- Exercises whose supervised code needs no file, network, command or thread access, and no package imports beyond the implicit allowlist.
- Smoke-testing a fresh setup, to confirm the wiring before writing a policy.

**Not reasonable:**

- Graded exercises, in general. The criterion is not "graded" as such but ownership: if students can influence which package dominates the production sources, or can add files beneath a discovered test root, then the scope and the exempt set are partly theirs to choose.
- Anything where you need to grant a specific allowance. As soon as one permission is required, write the policy; a policy with five empty lists is equally strict and additionally pins the scope, the exempt set and the mode, so it is the better default even when it grants nothing.
