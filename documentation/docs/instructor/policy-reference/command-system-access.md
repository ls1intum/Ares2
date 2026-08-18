---
title: "Command System Access"
sidebar_position: 6
description: "Which external commands the supervised code may execute, and with which arguments."
---

:::tip[Simple Story]
Sometimes a pupil legitimately has to send somebody out of the room on an errand.

Left open that is the largest hole there is, because an errand can fetch anything at all. So
the checklist names exactly which errands may be run, and exactly which instructions they may
be given. Nothing else leaves the room.
:::

## Position in the example policy file

The section documented on this page is marked in red. Every page in this section shows the
same example file, so reading them in order walks it from top to bottom.

```yaml title="security-policy.yaml"
thisPolicyFileCompliesToThePolicyVersion: 1
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
      - timeout: 120000
```

## Fields

Implemented by `CommandPermission` in
[`policy/policySubComponents/CommandPermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/CommandPermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `executeTheCommand` | `String` | The command this entry permits. | `ls` | Must not be `null` and must not be blank. No pattern is applied: a command name is an operating-system concept, not a Java one. |
| `withTheseArguments` | `List<String>` | The arguments the command may be executed with. Stored as an unmodifiable defensive copy. | `["-l"]` | Must not be `null`. An empty list means the command may be run without arguments; `allowWithoutArguments` is the shorthand for that. |

## Notes

`CommandPermission` also accepts a plain string through a delegating `@JsonCreator`, so a policy may write a bare command instead of the mapping form. It is not a shorthand for a command line: `fromString` delegates to `allowWithoutArguments`, which takes the scalar as the executable in full and leaves the argument list empty. A bare `"ls -l"` therefore permits an executable literally named `ls -l`, and denies `ls` invoked with `-l`. Use the mapping form whenever arguments are involved.

The argument list is copied defensively and wrapped unmodifiable, so the record is genuinely immutable and a caller cannot widen a permission after construction.
