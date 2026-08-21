---
title: "Enforcement Model"
sidebar_position: 1
description: "How static and runtime responsibilities are divided, project discovery, and the reserved package build boundary."
---

:::tip[Simple Story]
Two layers do the checking, and this page settles who is responsible for what.

It states the one rule the build itself has to keep, and it is honest about what the
boundary does **not** cover, which is the part most worth reading.
:::

## Static and runtime responsibility

ArchUnit and T. J. Watson Libraries for Analysis (WALA) can reject a complete operation domain, but they cannot decide
whether a runtime argument is one particular path, host, command or thread class.
Consequently, Ares adds a static deny-all rule only while a domain has no
allowance. The moment a policy grants one file, network, command or thread
permission, the runtime AspectJ or instrumentation layer is authoritative for
distinguishing that narrow allowance from every non-allowed operation. A narrow
allowance must never be interpreted as narrow static analysis.

The reviewed runtime inventory is:

| Domain | AspectJ pointcuts/advice | Instrumentation bindings/toolbox |
| --- | --- | --- |
| Files | `JavaAspectJFileSystemPointcutDefinitions` and `JavaAspectJFileSystemAdviceDefinitions` | `METHODS_WHICH_CAN_{READ,OVERWRITE,CREATE,EXECUTE,DELETE}_FILES` and `JavaInstrumentationAdviceFileSystemToolbox` |
| Network | `JavaAspectJNetworkSystemPointcutDefinitions` and `JavaAspectJNetworkSystemAdviceDefinitions` | connect/send/receive maps and `JavaInstrumentationAdviceNetworkSystemToolbox` |
| Commands | `JavaAspectJCommandSystemPointcutDefinitions` and `JavaAspectJCommandSystemAdviceDefinitions` | command maps and `JavaInstrumentationAdviceCommandSystemToolbox` |
| Threads | `JavaAspectJThreadSystemPointcutDefinitions` and `JavaAspectJThreadSystemAdviceDefinitions` | thread maps, monitor call-site substitution and `JavaInstrumentationAdviceThreadSystemToolbox` |

Port `0` is the only any-port wildcard. Valid policy ports are `0..65535`.

## Project discovery and `withinPath`

No-policy execution recognises `pom.xml`, `build.gradle` and
`build.gradle.kts`, then selects ArchUnit plus AspectJ. Maven and Gradle together
are ambiguous unless the caller explicitly selects a build mode; a project with
no supported descriptor is rejected. Source roots are resolved against the
explicit project root and cannot escape it.

`Policy.withinPath` remains trusted instructor configuration. Its supported
prefixes are `classes/...`, `classes/java/main/...`, `test-classes/...` and
`test-classes/java/test/...`. Ares logs the final resolved analysis/import path.
The remainder is interpreted by the host filesystem; traversal, symlinks and
non-contained instructor values are outside the student threat model and must be
rejected during exercise-author review. Students must not control policy files,
annotations, environment substitution or this value.

## Reserved package build boundary

Ares trusts runtime identities by name. The exercise build must therefore reject
student classes beneath every prefix in
`WalaPathClassification.RESERVED_PACKAGE_PREFIXES`. Build validation is a
deployment prerequisite, not an optional Ares runtime feature.

Two versions are pinned, because the data and the contract that enforces it
change for different reasons. `RESERVED_PACKAGE_PREFIX_VERSION = 1` is the
prefix list. `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION = 2` is the build-side
contract. Templates and continuous integration (CI) must pin both.

Canonical Maven configuration uses a `maven-antrun-plugin` task bound to
`process-classes` that scans `${project.build.outputDirectory}` and fails for
`java/**`, `javax/**`, `sun/**`, `jdk/**`, `com/sun/**`,
`de/tum/cit/ase/ares/api/**`, `net/bytebuddy/**`, `org/aspectj/**`,
`com/ibm/wala/**`, `com/tngtech/archunit/**`, `anonymous/toolclasses/**` and
`metatest/**`. `process-classes` precedes `test`, so `mvn test` runs it.

Canonical Gradle configuration registers a `verifyAresReservedPackagesV2` task
over `sourceSets.main.output.classesDirs` with the same paths, and **both**
makes `check` depend on it and gates every `Test` task with
`tasks.withType(Test).configureEach`. Both hooks are required. Boundary
version 1 hung the validation off `check` alone, and Gradle's Java plugin
defines `check.dependsOn test` rather than the reverse, so `gradlew test`, which
is what a grading run invokes, never ran it: student classes under a reserved
package survived. An exercise still carrying a boundary version 1 snippet is
bypassable and must be migrated. In a multi-project build, apply the snippet to
every project that compiles student code, because `tasks.withType(Test)` covers
only the project it is applied to.

The executable, versioned snippets are shipped inside the Ares JAR under
`de/tum/cit/ase/ares/api/configuration/reservedPackages/`, and live in the
repository at
[`src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages/`](https://github.com/ls1intum/Ares2/tree/main/src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages):
`MavenReservedPackages.xml` and `GradleReservedPackages.gradle`; their common
machine-readable list is `ReservedPackagePrefixes.txt`. They deliberately define
no Maven property, Gradle property, system property or profile which can skip
the check. Removing the plugin/script or detaching its task is equivalent to
disabling the security boundary and must be reported visibly by template CI.

Any system property, Gradle flag or Maven profile that skips the exercise's
reserved-package validation must print a prominent diagnostic. Such a run does
not provide the class-shadowing security boundary.

### What this boundary does not defend against

The build descriptor and the command used to invoke it are **trusted instructor
configuration**, on the same footing as `Policy.withinPath` above. "No bypass
flag is supported" means the shipped snippets offer no opt-out of their own; it
does not mean the check survives an adversary who controls the build. Whoever
can edit `build.gradle` or `pom.xml`, or pass `-x verifyAresReservedPackagesV2`
or `-Dmaven.antrun.skip`, can remove the boundary outright. The threat this
boundary addresses is student *code* that declares a reserved package, not
student control over the build. Exercise templates and their CI must therefore
own the build descriptor and the invocation, and must fail visibly if either is
altered.

## Legacy annotation migration

The detached annotation configuration was removed because no active enforcement
pipeline consumed it. `@Policy` and its YAML document are now the sole authority.

The policy model is an **allowlist only**. It has no deny rule, so an annotation
whose purpose was to carve an exception out of a broader permission has no
counterpart, and the intent must be re-expressed by granting less.

| Removed annotation | Fidelity | Policy replacement |
| --- | --- | --- |
| `WhitelistPath` | approximate | a permitted path and its booleans under `regardingFileSystemInteractions`. Only prefix-shaped paths map naturally; glob and regex path types do not |
| `BlacklistPath` | **none** | there is no deny rule. Grant narrower paths instead of the parent. "Allow a directory except one file inside it" is not representable |
| `WhitelistPackage` | approximate | `regardingPackageImports`, after recomputing the effective permission set |
| `BlacklistPackage` | **none** | there is no negative package rule. Note that the `java` prefix is always permitted as an essential package, so a blacklist of a `java.*` package cannot be reproduced |
| `WhitelistClass` | conditional | `theFollowingClassesAreTestClasses`, **only** for instructor-owned, student-unmodifiable test infrastructure. An entry there is exempt from both the static and the runtime checks |
| `AddTrustedPackage` | **none** | do not place a package name in `theFollowingClassesAreTestClasses`: entries match an exact fully qualified class name, or a nested class on the `$` boundary, so a package name grants no exemption. It is not inert either, because a permitted package is derived from every entry by stripping the last dotted component, so such an entry silently widens the package allowlist. Infrastructure packages stay in the versioned essential configuration |
| `AllowLocalPort` | approximate | `regardingNetworkConnections` with explicit host, port and operation booleans. Range-with-exclusion forms do not map. Note that port `0` is a **wildcard** matching every port, which makes an unrestricted threshold representable and makes a literal `0` dangerously broad |
| `AllowThreads` | approximate | `regardingThreadCreations` with an explicit class and count. The original capped concurrently active threads, so the accounting differs and the limit must be re-derived |
| `TrustedThreads`, `DisableThreadGroupCheckFor` | **none** | these controlled the trusted execution context, not permission to create a thread |

Method-specific behaviour uses a method-level `@Policy`; shared behaviour uses a
class-level `@Policy`. The method annotation takes precedence. The removed whitelist
and blacklist annotations were repeatable and additive across class and method
level (others, such as `AllowLocalPort` and `AllowThreads`, already resolved
nearest-first), whereas `@Policy` resolution is always nearest-wins and policies
are never merged, so an additive configuration must be consolidated into one
complete policy per scope.

A step-by-step migration, including the complete build configuration, is in
[the Ares 1 migration guide](/instructor/transform-ares-1-into-ares-2/).

## Further reading

- [Security Policy Manual](security-policy-manual.md) — the policy file from the
  instructor's point of view
- [Policy Reader and Director](reader-and-director.md) — how the policy is parsed and
  dispatched
