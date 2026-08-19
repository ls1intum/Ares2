---
title: "What does Ares 2 protect against"
sidebar_position: 2
description: "The resources Ares 2 guards, and the two compile modes through which it enforces a policy."
---

:::tip[Simple Story]
The checklist carries two kinds of line. Some name a thing a pupil may do in one specific way:
this paper, that telephone number, an errand with exactly these instructions. The rest name
things nobody in the room may do at all, because no honest answer to the question needs them.

It also sets out the two moments at which the school can act: the checks printed into the
examination beforehand, or the teacher carrying the checklist in while it runs. Ares does both.
:::

Ares 2 sorts what student code might attempt into categories. For each one it asks the same
question: is there a route to it, and if the code takes that route, is the specific thing it
reaches for permitted?

Two things follow from that, and they are the whole of this page. A handful of categories are
**grantable**: your policy can open them a crack, and Ares checks each individual use against
what you opened. The rest are **refused outright**, with no policy field to open them at all.

## Categories you can grant

These have a field in `security-policy.yaml`. Beyond a small implicit allowance, nothing is
permitted until you write it down, and what you write down is then checked value by value.

The implicit part matters for package imports: Ares always permits its own essential packages,
the supervised package itself and the packages of your test classes, whatever your list says.

| Category | Policy field | What you grant, and how narrowly |
| --- | --- | --- |
| File system | `regardingFileSystemInteractions` | Named paths, and per path any of read, overwrite, create, execute and delete as five separate permissions |
| Network | `regardingNetworkConnections` | A named host and port, and separately whether the code may connect, send and receive |
| Command execution | `regardingCommandExecutions` | Named executables together with the exact arguments they may be given |
| Thread creation | `regardingThreadCreations` | Which class of worker may be started, and how many at most |
| Package imports | `regardingPackageImports` | Which packages the submission may import. Naming a package covers everything beneath it |

:::warning[Granting anything changes how the category is enforced]
For the first four, the two layers do not both apply. While a category's list is **empty**,
Ares adds an architecture check that rejects any route to it before the code runs. As soon as
you grant one thing, that architecture check is dropped and the category becomes
**runtime-only**, because static analysis cannot tell your permitted path from a forbidden one.

So a category you have opened is guarded by the runtime layer alone, and it is worth knowing
which runtime mechanism you actually have. Package imports are settled from the compiled code
alone in either case.
:::

Full field-by-field detail is in the [Policy Reference](/instructor/policy-reference/), and
worked examples for common situations are in the
[Policy Cookbook](/instructor/policy-cookbook/).

## Categories that are refused outright

These have no policy field. There is no syntax for permitting them, narrowly or otherwise.

| Category | What it covers |
| --- | --- |
| Reflection | `java.lang.reflect.*`, `java.lang.invoke.*`, `Method.invoke`, `Field.set`, `Proxy`, `setAccessible` |
| Native code | `System.loadLibrary`, `System.load`, `sun.misc.Unsafe`, and the foreign function entry points `Linker.nativeLinker`, `Linker.downcallHandle` and `SymbolLookup.libraryLookup` |
| Class loading | `Class.forName` and dynamic loading through `ClassLoader` and its subclasses |
| JVM termination | `System.exit`, `Runtime.exit`, `Runtime.halt` |
| Agent attach | `Instrumentation` access, class redefinition, `VirtualMachine.attach` |
| Module system | `implAddOpens`, `implAddExports`, `privateLookupIn` |
| Serialisation | `ObjectInputStream` and `ObjectOutputStream` |
| JNDI | Lookups through `InitialContext` and `InitialDirContext` |
| Environment access | `System.getenv`, `System.getProperty` and `setProperty`, `ProcessHandle` metadata |

### Why these are not grantable

Ares 2 deliberately offers no granular grant model for these.

For most of them the reason is simply that an ordinary exercise has no use for them. A student
implementing a sorting algorithm, a parser or a data structure has no reason to load a native
library, attach an agent or halt the JVM. The capability is not part of the learning goal, so
granting it would buy nothing.

For several it is stronger than that. Reflection reaches any method regardless of what the
policy says about it, a class loader introduces code the analysis never saw, and agent attach
lets the submission rewrite the very classes doing the checking. These are general-purpose
routes around the rest of the checklist, so a narrow grant would not stay narrow.

If an exercise genuinely needs one of these capabilities, change the exercise. Do not reach for
`theFollowingClassesAreTestClasses`: that field exempts a class from the policy completely, and
it exists for instructor-controlled test infrastructure. Listing student code there switches the
sandbox off for that code entirely.

:::warning[Refused outright does not mean caught in every case]
These nine have no dedicated runtime enforcement of their own. The architecture layer reads the
compiled submission and rejects it if a route exists, and that is the only check aimed at the
category as such. An individual call may still be caught by another category's runtime checks
where the two overlap, `System.load` being both native code and a file execution, but do not
rely on that. See
[What does Ares 2 not protect against](what-does-ares-2-not-protect-against.md).
:::

## When Ares 2 steps in

### Precompile

Precompile mode generates security test cases and infrastructure files **before** the student
code is compiled or executed. From your security configuration file, Ares 2 automatically
creates:

- architecture test cases using ArchUnit or T. J. Watson Libraries for Analysis (WALA)
- AOP configuration files for AspectJ or instrumentation
- build system modifications (Maven and Gradle plugins and dependencies)
- test infrastructure for enforcing security policies

This mode runs once during exercise setup and produces test files that are committed to the
repository. When students submit their code, these pre-generated tests run automatically and
enforce the security policy.

**Key advantages:**

- tests are versioned and can be reviewed before deployment
- no runtime configuration parsing overhead
- clear separation between policy definition and enforcement
- exercise creators can validate the generated tests before release

See [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md) for the integration steps.

### Postcompile

Postcompile mode enforces security policies at **runtime**, without writing any generated
files into the repository. When a test carries an Ares test annotation and a `@Policy`,
Ares 2:

- loads the security configuration file
- arms the runtime interception the policy asks for
- intercepts method calls to protected resources (files, network, threads, commands)
- validates each operation against the policy and blocks unauthorised access

This mode activates dynamically when tests run, and gives immediate, fine-grained control over
student code execution without requiring pre-generated test files.

:::danger[`@Policy` alone supervises nothing]
The Ares test annotation is what registers the extensions; `@Policy` only names the policy they
apply. A method carrying a plain JUnit `@Test` and a `@Policy` runs unsupervised and passes,
with nothing to indicate that no domain was enforced. See
[Test Annotations](../protect-a-java-project/test-annotations.md).
:::

:::warning[The mode does not decide the interception mechanism]
The interception mechanism comes from the policy's
`theFollowingProgrammingLanguageConfigurationIsUsed`, not from the mode. An `_ASPECTJ`
configuration needs the aspects woven into the bytecode by the AspectJ compiler during the
build; an `_INSTRUMENTATION` configuration needs the Byte Buddy agent attached to the test Java Virtual Machine (JVM).
Both work in Postcompile, and the two shipped examples run Postcompile with AspectJ. Setting up
only the agent leaves an `_ASPECTJ` policy with nothing to intercept with, and the run stays
green while enforcing nothing. [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md)
sets out the same distinction and lists which combination needs which build step.
:::

**Key advantages:**

- dynamic enforcement at runtime
- no repository modifications needed
- easy policy updates without regenerating tests
- fine-grained control over individual test methods
- detailed security violation messages

See [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md) for the integration steps.

### Both modes together

The two modes work together. Ares 2 provides both steps to achieve strong isolation and
predictable, student-friendly feedback.

Both rely on the same configuration file, which is introduced in
[Policy Configuration](../protect-a-java-project/policy-configuration.md).

:::info[How it works internally]
The developer guide describes the enforcement machinery itself: the
[enforcement model](/contributor/subsystems/policy/enforcement-model), the
[AOP layer](/contributor/subsystems/aop/block-file-system-access) and the
[architecture analysis layer](/contributor/subsystems/architecture/block-file-system-access).
:::
