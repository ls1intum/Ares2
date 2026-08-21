---
title: "Precompile or Postcompile"
sidebar_position: 1
description: "The two ways Ares 2 secures an exercise, how they differ, and which one to choose."
---

:::tip[ELI5]
Ares can either write the guard into your exercise before it is built, or attach a guard while
the tests are already running.

Everything else on this page follows from that one choice, so make it first.
:::

## The two modes

**Postcompile.** Ares is a dependency of the project under test and is activated by the test
cases themselves, through `JupiterSecurityExtension` or `JqwikSecurityExtension`. Nothing is
generated: when a test runs, Ares installs the policy into the already-running Java Virtual Machine (JVM) and checks
each action as it is attempted, so it can report exactly which file or which address was asked
for. Every test may carry its own `@Policy`. This assumes tests run sequentially, because the
enforcement settings are static fields in the bootstrap class loader.

**Precompile.** Ares runs once as an external tool, reads the policy, and copies a self-contained
set of security test cases, aspects, configuration and the Ares classes it needs into the
exercise. The target project then needs no Ares dependency of its own. One generated set applies
to every test in the project, and changing the policy means regenerating and rebuilding.

:::note[Precompile and Postcompile are not the same thing as AspectJ and instrumentation]
The two axes are independent in both directions. `examples/ares-exercise-gradle` runs
`JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ` in Postcompile, and the Precompile generator can emit
instrumentation just as well as aspects. The weaving mechanism comes from the policy's
`ProgrammingLanguageConfiguration`, not from the mode.
:::

## How to choose

| If you want | Choose |
| --- | --- |
| A different policy per test method | Postcompile |
| The exercise to build without an Ares dependency | Precompile |
| Enforcement you can change without rebuilding the exercise | Postcompile |
| Generated artefacts you can review and commit like any other code | Precompile |
| The path with runnable reference exercises today | Postcompile |

Postcompile is the better default. Both runnable examples under
[`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples) are Postcompile exercises, and
there is no runnable Precompile example yet.

## What both modes require

Whichever mode you pick, the build has to do five things:

1. Make the Ares library available, as a dependency in Postcompile or through the generated
   artefacts in Precompile.
2. Weave the Ares security aspects into the compiled bytecode with the AspectJ compiler, if the
   configuration ends in `_ASPECTJ`.
3. Download and attach the Ares agent JAR at test startup, if it ends in `_INSTRUMENTATION`.
4. Grant the agent access to Java internals through the required JVM flags.
5. **Reject student classes declared in reserved packages**, so that student code cannot
   impersonate code Ares trusts by name.

Step 5 is not optional and not mode-specific. Ares does **not** install it for you in either
mode: the shipped snippets under
[`configuration/reservedPackages/`](https://github.com/ls1intum/Ares2/tree/main/src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages)
are copied into your build by hand. Without it, a student can declare a class in a package Ares
trusts and be trusted along with it.

## Choosing a configuration

1. **Create a security policy and annotate tests:** follow the [Security Policy Manual](/contributor/subsystems/policy/security-policy-manual), which explains how to write `SecurityPolicy.yaml` files and apply `@Policy` to your tests. `@Policy` selects the policy but activates nothing on its own, so each supervised test also needs an [Ares test annotation](./test-annotations.md) (`@Public`, `@Hidden`, `@PublicTest` or `@HiddenTest`). If your exercise needs no resource access at all, [Further Options](./further-options.md) describes the alternative.
2. **Choose the right configuration:** select one of the eight `ProgrammingLanguageConfiguration` values matching your build tool, architecture analysis and runtime enforcement:

| Value | Build Tool | Static Analysis | Runtime Enforcement |
|---|---|---|---|
| `JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ` | Maven | ArchUnit (rule-based) | AspectJ (compile-time weaving) |
| `JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION` | Maven | ArchUnit (rule-based) | ByteBuddy agent (runtime) |
| `JAVA_USING_MAVEN_WALA_AND_ASPECTJ` | Maven | T. J. Watson Libraries for Analysis (WALA) (call-graph) | AspectJ (compile-time weaving) |
| `JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION` | Maven | WALA (call-graph) | ByteBuddy agent (runtime) |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ` | Gradle | ArchUnit (rule-based) | AspectJ (compile-time weaving) |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION` | Gradle | ArchUnit (rule-based) | ByteBuddy agent (runtime) |
| `JAVA_USING_GRADLE_WALA_AND_ASPECTJ` | Gradle | WALA (call-graph) | AspectJ (compile-time weaving) |
| `JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION` | Gradle | WALA (call-graph) | ByteBuddy agent (runtime) |

**How to choose:**
- **Build tool:** match your project (`MAVEN` or `GRADLE`).
- **Static analysis:** `ARCHUNIT` is simpler and faster; `WALA` detects transitive violations through call chains.
- **Runtime enforcement:** `INSTRUMENTATION` (ByteBuddy agent) or `ASPECTJ` (compile-time weaving). Configure both mechanisms regardless of which you choose, so that switching is a policy edit rather than a build change.

## Glossary

| Term | Meaning |
|------|----------|
| **Java Agent** | A JVM mechanism (`-javaagent`) that allows code to transform class bytecode at load time. Ares uses a ByteBuddy-based agent to intercept forbidden operations at runtime. |
| **ByteBuddy** | A library for creating and modifying Java classes at runtime, used by Ares to implement the instrumentation agent. |
| **Instrumentation** | The runtime aspect-oriented programming (AOP) approach where class bytecode is modified at load time via the `java.lang.instrument` application programming interface (API). One of the two runtime enforcement mechanisms in Ares, alongside AspectJ. |
| **AspectJ** | A compile-time AOP framework used for runtime enforcement. Requires the AspectJ compiler plugin to weave aspects during the build, and the AspectJ runtime JAR on the bootstrap classpath. The compiler weaves the aspects from the Ares JAR only if that JAR is on the aspect path (Gradle: the `aspect` configuration; Maven: an `<aspectLibraries>` entry). |
| **Aspect path** | The set of JARs `ajc` reads binary aspects from. Distinct from the compile classpath: a JAR on the classpath alone contributes no aspects. |
| **`CommandLineArgumentProvider`** | The Gradle interface used here to compute test JVM arguments when the task runs rather than when the build is configured, which keeps dependency resolution out of the configuration phase and the build configuration-cache compatible. |
| **`--add-opens` / `--add-exports`** | JVM flags that grant access to internal Java modules. Required by Ares to introspect intercepted Java Development Kit (JDK) objects. |
| **`withinPath`** | The path to compiled student bytecode, relative to the build output directory. Differs between Gradle (`classes/java/main/...`) and Maven (`classes/...`). |
| **`ProgrammingLanguageConfiguration`** | An enum encoding the combination of build tool, static analysis framework and runtime enforcement mechanism. |
| **Classifier (`:agent`)** | A Maven/Gradle coordinate qualifier selecting a variant of an artefact. The `:agent` classifier selects the agent JAR, which carries the `Premain-Class` manifest entry and needs no repackaging. |
| **Reserved package** | A package prefix that student code may not declare, because Ares trusts that identity by name. Enforced by the build, see the reserved-package step. |
| **Phobos** | A test-case family covering the file-system, network and timeout domains. Ares 2.1.2 generates Phobos cases but does not yet dispatch them from the in-process execution path, so a policy timeout does not bound a test today. Use `@StrictTimeout` for a deadline. |
| **`@StrictTimeout`** | The annotation that actually bounds test execution. Applied to a test class or method, and unchanged from Ares 1 apart from its package. |
| **Positive / negative control** | The paired checks of the two controls above: one permitted operation that must succeed, one forbidden operation that must be rejected. Neither alone demonstrates that enforcement works. |

## Related documentation

- [How to transform an Ares 1 protected project into an Ares 2 protected project](../transform-ares-1-into-ares-2/index.md),
  if you are converting an existing `de.tum.in.ase:artemis-java-test-sandbox` exercise. Start there
  rather than here; it is self-contained and adds the annotation-to-policy translation.
- [Security Policy Manual](/contributor/subsystems/policy/security-policy-manual), which explains how
  to write a security policy YAML file
- [Security Policy Reader and Director Manual](/contributor/subsystems/policy/reader-and-director),
  which describes the internal processing pipeline
- [Enforcement Model](/contributor/subsystems/policy/enforcement-model), which defines what static
  analysis and the runtime layer are each responsible for, and specifies the reserved-package build
  boundary
