---
title: "Command System Access"
sidebar_position: 5
description: "Which external commands the supervised code may execute, and with which arguments."
---

:::tip ELI5
Sometimes a program needs to ask the operating system to run another program.

That is an enormous hole if left open, because it can be used to run anything at all. So
Ares keeps a short list of exactly which commands may be run, and exactly which arguments
they may be run with. Nothing else is allowed to start.
:::

## Position in the example policy file

The section documented on this page is marked in red. Every page in this section shows the
same example file, so reading them in order walks it from top to bottom.

```yaml title="security-policy.yaml"
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_WALA_AND_ASPECTJ
  theSupervisedCodeUsesTheFollowingPackage: "org.example"
  theMainClassInsideThisPackageIs: "Main"

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

# policy-focus-start
    regardingCommandExecutions:
      - executeTheCommand: "ls"
        withTheseArguments:
          - "-l"
# policy-focus-end

    regardingThreadCreations:
      - createTheFollowingNumberOfThreads: 10
        ofThisClass: "org.example.Worker"

    regardingPackageImports:
      - importTheFollowingPackage: "java.util"

    regardingTimeouts:
      - timeout: 120
```

## Fields

Implemented by `CommandPermission` in
[`policy/policySubComponents/CommandPermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/CommandPermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `executeTheCommand` | `String` | The command this entry permits. | `ls` | Must not be `null` and must not be blank. No pattern is applied: a command name is an operating-system concept, not a Java one. |
| `withTheseArguments` | `List<String>` | The arguments the command may be executed with. Stored as an unmodifiable defensive copy. | `["-l"]` | Must not be `null`. An empty list means the command may be run without arguments; `allowWithoutArguments` is the shorthand for that. |

## Notes

`CommandPermission` also accepts a plain string through a delegating `@JsonCreator`, so a policy may write a bare command instead of the mapping form. `fromString` splits it into the command and its arguments.

The argument list is copied defensively and wrapped unmodifiable, so the record is genuinely immutable and a caller cannot widen a permission after construction.
