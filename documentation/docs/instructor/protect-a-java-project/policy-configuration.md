---
title: "Policy Configuration"
sidebar_position: 5
description: "The security-policy.yaml file and the eight supported programming language configurations."
---

:::tip[ELI5]
The policy file is where you write down what the student's program is allowed to do.

It is a plain list of permissions. Anything you do not write down is refused, so an empty file
is the strictest file.
:::

Ares 2 security enforcement is driven by a central configuration file. It defines how the
supervised program is built, analysed and instrumented, as well as which resources and
operations are permitted at runtime.

Both [Precompile and Postcompile](precompile-or-postcompile.md) mode rely on this
configuration.

You can create the file manually, or with [Ares2UI](https://github.com/ls1intum/Ares2UI).

:::tip[Full reference]
This page introduces the format. For every supported option and how each is enforced, see the
[Security Policy Manual](/contributor/subsystems/policy/security-policy-manual).
:::

## Example configuration

The file has two main parts: metadata describing the supervised code, and resource access
rules that define the sandbox policy.

```yaml
thisPolicyFileCompliesToThePolicyVersion: 1
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_WALA_AND_ASPECTJ
  theSupervisedCodeUsesTheFollowingPackage: "de.tum.cit.ase.aresUI"
  theMainClassInsideThisPackageIs: "Main"

  theFollowingClassesAreTestClasses:
    - "de.tum.cit.ase.example.ExampleTest"

  theFollowingResourceAccessesArePermitted:

    regardingFileSystemInteractions:
      - onThisPathAndAllPathsBelow: "pom.xml"
        readAllFiles: true
        overwriteAllFiles: true
        createAllFiles: true
        executeAllFiles: true
        deleteAllFiles: false

    regardingNetworkConnections:
      - onTheHost: "www.example.com"
        onThePort: 80
        openConnections: true
        sendData: true
        receiveData: true

    regardingCommandExecutions:
      - executeTheCommand: "ls"
        withTheseArguments:
          - "-l"

    regardingThreadCreations:
      - createTheFollowingNumberOfThreads: 10
        ofThisClass: "instrumentation.lang.Thread"

    regardingPackageImports:
      - importTheFollowingPackage: "instrumentation.util"

    regardingTimeouts:
      - timeout: 120000
```

## Field reference

| Field | Meaning |
| --- | --- |
| `thisPolicyFileCompliesToThePolicyVersion` | The policy format the file is written against. A root field rather than a member of `regardingTheSupervisedCode`. Must be exactly `1`; a file that omits it is rejected on load. |
| `theFollowingProgrammingLanguageConfigurationIsUsed` | Selects the processing pipeline (build system, static analysis tool and instrumentation backend). See the table below. |
| `theSupervisedCodeUsesTheFollowingPackage` | The root package containing all student code to be supervised. |
| `theMainClassInsideThisPackageIs` | The entrypoint class used to construct the call graph of the student program. |
| `theFollowingClassesAreTestClasses` | Test classes that execute student code. These are trusted and not sandboxed. |
| `regardingFileSystemInteractions` | Which paths the student code may interact with, and which operations (read, create, overwrite, execute, delete) are allowed. |
| `regardingNetworkConnections` | Hosts and ports the student program may connect to. |
| `regardingCommandExecutions` | System commands the supervised program may execute. |
| `regardingThreadCreations` | Thread creation limits and allowed thread classes. |
| `regardingPackageImports` | External packages the supervised code may import. |
| `regardingTimeouts` | The execution budget for supervised code, in milliseconds. Parsed and validated, but not dispatched from the in-process path today; see [Resource Limits](../policy-reference/resource-limits.md). |

:::warning[Everything is default-deny]
Any resource not listed is denied. An empty list such as `regardingNetworkConnections: [ ]`
forbids all network access.
:::

## Supported programming language configurations

Ares 2 currently supports eight Java-based configurations, formed by combining three
orthogonal dimensions:

1. **Build system**
   - Maven
   - Gradle

2. **Static analysis and structural validation**
   - **ArchUnit**: validates the architectural structure of the supervised codebase and
     ensures that forbidden packages, imports and dependencies cannot be referenced.
   - **T. J. Watson Libraries for Analysis (WALA)**: performs static call-graph and data-flow analysis to detect disallowed code
     paths before execution.

3. **Runtime instrumentation backend**
   - **AspectJ**: uses aspect-oriented programming to intercept method calls at runtime and
     block access to specified operations.
   - **Instrumentation application programming interface (API)**: uses a Java agent (Byte Buddy) to instrument bytecode at runtime
     and block access to specified operations.

The resulting matrix contains eight modes:

- `JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ`
- `JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION`
- `JAVA_USING_MAVEN_WALA_AND_ASPECTJ`
- `JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION`
- `JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ`
- `JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION`
- `JAVA_USING_GRADLE_WALA_AND_ASPECTJ`
- `JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION`

Each mode determines how the submission is statically analysed (ArchUnit or WALA), how the
project is built (Maven or Gradle), and how the bytecode is instrumented at runtime (AspectJ
or Java agent).

This field must be set correctly. It controls the entire supervision pipeline and is required
for both precompile and postcompile mode.
