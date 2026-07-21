# Ares 2 enforcement model

## Static and runtime responsibility

ArchUnit and WALA can reject a complete operation domain, but they cannot decide
whether a runtime argument is one particular path, host, command or thread class.
Consequently, Ares adds a static deny-all rule only while a domain has no
allowance. As soon as a policy grants one file, network, command or thread
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
`WalaPathClassification.RESERVED_PACKAGE_PREFIXES`. The list carries version
`RESERVED_PACKAGE_PREFIX_VERSION = 1`; templates and CI must pin the same
version. Build validation is a deployment prerequisite, not an optional Ares
runtime feature.

Canonical Maven configuration uses a verify-phase rule (for example a
`maven-antrun-plugin` task) that scans `${project.build.outputDirectory}` and
fails for `java/**`, `javax/**`, `sun/**`, `jdk/**`, `com/sun/**`,
`de/tum/cit/ase/ares/api/**`, `net/bytebuddy/**`, `org/aspectj/**`,
`com/ibm/wala/**`, `com/tngtech/archunit/**`, `anonymous/toolclasses/**` and
`metatest/**`. Canonical Gradle configuration registers a `verifyReservedPackages`
task over `sourceSets.main.output.classesDirs` with the same paths and makes
`check` depend on it. Exercise templates must keep that validation enabled.

The executable, versioned snippets are shipped with Ares at
`configuration/reservedPackages/MavenReservedPackages.xml` and
`configuration/reservedPackages/GradleReservedPackages.gradle`; their common
machine-readable list is `ReservedPackagePrefixes.txt`. Version 1 deliberately
defines no Maven property, Gradle property, system property or profile which can
skip the check. Removing the plugin/script or detaching its task is equivalent to
disabling the security boundary and must be reported visibly by template CI.

Any system property, Gradle flag or Maven profile that skips the exercise's
reserved-package validation must print a prominent diagnostic. Such a run does
not provide the class-shadowing security boundary.

## Legacy annotation migration

The detached annotation configuration was removed because no active enforcement
pipeline consumed it. `@Policy` and its YAML document are now the sole authority.

| Removed annotation | Policy replacement |
| --- | --- |
| `WhitelistPath` / `BlacklistPath` | add the permitted path and booleans under `regardingFileSystemInteractions`; omit an entry to deny it |
| package/class whitelist or blacklist | `regardingPackageImports`, supervised package and explicit test-class list; omission means denial |
| `AddTrustedPackage` | declare only the minimum trusted test classes; infrastructure packages stay in the versioned essential configuration |
| `AllowLocalPort` | `regardingNetworkConnections` with explicit host, port and operation booleans |
| `AllowThreads` / `TrustedThreads` | `regardingThreadCreations` with an explicit class and count |

Method-specific behaviour uses a method-level `@Policy`; shared behaviour uses a
class-level `@Policy`. The method annotation takes precedence.
