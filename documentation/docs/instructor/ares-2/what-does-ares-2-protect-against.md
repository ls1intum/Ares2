---
title: "What does Ares 2 protect against"
sidebar_position: 2
description: "The resources Ares 2 guards, and the two compile modes through which it enforces a policy."
---

:::tip[Simple Story]
The rule sheet has two kinds of line. Some say what a pupil can be allowed to do, and exactly
how: this worksheet, that telephone number, these words to the friend outside the room. The
rest list the things nobody is allowed to do, such as pulling the fire alarm.

The teacher looks for any way to those forbidden things when they clear the desks. A way they
cannot see gets past them. Once the rule sheet allows a single worksheet, telephone number,
friend or side job, the desk clearing stops covering that kind, and only the watching remains.

The rules reach the room in one of two ways. Either the head teacher prints them onto the
grading instructions beforehand, or the teacher picks up the rule sheet for each question and
puts it away afterwards.

[Who is who](/instructor/who-is-who)
:::

Ares 2 sorts what student code might attempt into categories. For each one it asks the same
question: is there a route to it, and if the code takes that route, is the specific thing it
reaches for permitted?

Two things follow from that, and they are the whole of this page. A handful of categories are
**grantable**: your policy can open them a crack, and Ares checks each individual use against
what you opened. The rest are **refused outright**, with no policy field to open them at all.

## Categories you can grant

These have a field in `security-policy.yaml`. Beyond an implicit allowance, nothing is
permitted until you write it down, and what you write down is then checked value by value.

The implicit part matters for package imports. Whatever your list says, Ares always permits:

- the Java standard library (every package under `java`), AspectJ (`org.aspectj`) and one
  internal Ares package
- the packages your compiled student code is in
- the packages your test classes sit in, as named in `theFollowingClassesAreTestClasses`

Your list can add to these but cannot remove any of them.

The last point does not hand your tests to the student. Usually the test classes share the
package of the student code, so it adds nothing new. Where they do not, the import check
permits an import of a test class, and the build stops it instead. A standard Maven or Gradle
build compiles the student code without the test classes, so such an import does not compile.

| Category | Policy field | What you grant, and how narrowly |
| --- | --- | --- |
| File system | `regardingFileSystemInteractions` | Named paths, and per path any of read, overwrite, create, execute and delete as five separate permissions |
| Network | `regardingNetworkConnections` | A named host and port, and separately whether the code may connect, send and receive |
| Command execution | `regardingCommandExecutions` | Named executables together with the exact arguments they may be given |
| Thread creation | `regardingThreadCreations` | Which class of worker may be started, and how many at most |
| Package imports | `regardingPackageImports` | Which packages the submission may import. Naming a package covers everything beneath it |

:::warning[Granting anything changes how the category is enforced]
For the first four, the two layers do not both apply. While a category's list is **empty**,
Ares adds an architecture check that rejects any route to it before the code runs.
Granting one thing drops that architecture check, and the category becomes **runtime-only**, because static analysis cannot tell your permitted path from a forbidden one.

So a category you have opened is guarded by the runtime layer alone, and it is worth knowing
which runtime mechanism you have. Package imports are settled from the compiled code
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
| Java Virtual Machine (JVM) termination | `System.exit`, `Runtime.exit`, `Runtime.halt` |
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
routes around the rest of the policy, so a narrow grant would not stay narrow.

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
- aspect-oriented programming (AOP) configuration files for AspectJ or instrumentation
- copies of the Ares classes these files need
- test infrastructure for enforcing security policies

This mode runs once during exercise setup and produces test files that are committed to the
repository. When students submit their code, these pre-generated tests run automatically and
enforce the security policy.

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

See [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md) for the integration steps.

### Both modes together

The two modes are alternatives, so an exercise uses one of them.
[How to choose](../protect-a-java-project/precompile-or-postcompile.md#how-to-choose) helps you
pick.

Both rely on the same configuration file, which is introduced in
[Policy Configuration](../protect-a-java-project/policy-configuration.md).

The table sets the two modes side by side. Each of them needs the build set-up that
[What both modes require](../protect-a-java-project/precompile-or-postcompile.md#what-both-modes-require)
describes.

| Point | Precompile | Postcompile |
| --- | --- | --- |
| When Ares reads the policy | Once, when it generates the security test cases | Each time a test runs |
| What Ares generates into the repository | The security test cases, aspects, configuration and the Ares classes they need, which you can review and commit like any other code | Nothing |
| Ares as a dependency of the exercise | Not needed, though the libraries the copied classes use are | Needed |
| Which tests a policy covers | One generated set covers every test in the project | Each test method can carry its own `@Policy` |
| Changing the policy | Regenerate and rebuild | Edit the policy file. The build stays as it is, as long as it already sets up the interception the policy names |
| Runtime interception | AspectJ or instrumentation, as `theFollowingProgrammingLanguageConfigurationIsUsed` says | The same as in Precompile |
| Runnable example | None yet | `examples/ares-exercise-gradle` and `examples/ares-exercise-maven` |

:::info[How it works internally]
The developer guide describes the enforcement machinery itself: the
[enforcement model](/contributor/subsystems/policy/enforcement-model), the
[AOP layer](/contributor/subsystems/aop/block-file-system-access) and the
[architecture analysis layer](/contributor/subsystems/architecture/block-file-system-access).
:::
