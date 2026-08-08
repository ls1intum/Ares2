---
title: "Programming Language Configuration"
sidebar_position: 1
description: "The pipeline selector and the identity of the supervised code: build system, static analysis tool, instrumentation backend, root package and main class."
---

:::tip[ELI5]
Before Ares can guard anything, it has to know **what it is guarding and how**.

Think of it like hiring a security guard for a building. First you tell the guard which
building it is (the package), where the front door is (the main class), and which set of
tools they should bring (Maven or Gradle, ArchUnit or WALA, AspectJ or instrumentation).
None of that says what is forbidden yet. It just sets up who is on duty and where.
:::

## Position in the example policy file

The section documented on this page is marked in red. Every page in this section shows the
same example file, so reading them in order walks it from top to bottom.

```yaml title="security-policy.yaml"
regardingTheSupervisedCode:
# policy-focus-start
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_WALA_AND_ASPECTJ
  theSupervisedCodeUsesTheFollowingPackage: "org.example"
  theMainClassInsideThisPackageIs: "Main"
# policy-focus-end

  theFollowingClassesAreTestClasses:
    - "org.example.PenguinTest"

  theFollowingResourceAccessesArePermitted:

    regardingFileSystemInteractions:
      - onThisPathAndAllPathsBelow: "something.txt"
        readAllFiles: true
        overwriteAllFiles: true
        createAllFiles: true
        executeAllFiles: false
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
        ofThisClass: "org.example.Worker"

    regardingPackageImports:
      - importTheFollowingPackage: "java.util"

    regardingTimeouts:
      - timeout: 120
```

## Fields

Implemented by `SupervisedCode` in
[`policy/policySubComponents/SupervisedCode.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/SupervisedCode.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `theFollowingProgrammingLanguageConfigurationIsUsed` | enum `ProgrammingLanguageConfiguration` | Selects the whole supervision pipeline: build system, static analysis tool and runtime instrumentation backend. Required. | `JAVA_USING_MAVEN_WALA_AND_ASPECTJ` | `^JAVA_USING_(?:MAVEN\|GRADLE)_(?:ARCHUNIT\|WALA)_AND_(?:ASPECTJ\|INSTRUMENTATION)$` |
| `theSupervisedCodeUsesTheFollowingPackage` | `String` (nullable) | The root package holding the student code to supervise. May be omitted, in which case the project is scanned. | `org.example` | `JAVA_PACKAGE_PATTERN`: a dot-separated Java package name, each segment a Java identifier that is not a reserved word (`\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*`) |
| `theMainClassInsideThisPackageIs` | `String` (nullable) | The entrypoint class used to build the call graph of the student program. | `Main` | `JAVA_CLASS_NAME_PATTERN`: a single Java type name, excluding `var`, `yield`, `record`, `sealed` and `permits` |

## Notes

The eight accepted values are the full cross product of the three dimensions: `MAVEN`/`GRADLE` × `ARCHUNIT`/`WALA` × `ASPECTJ`/`INSTRUMENTATION`. CI exercises all four analysis and weaving combinations on every change.

Both nullable fields are validated only when present; a `null` is accepted and triggers project discovery instead. A blank string is not the same as absent and is rejected.
