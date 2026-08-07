---
title: "File System Access"
sidebar_position: 3
description: "Which paths the supervised code may read, overwrite, create, execute or delete."
---

:::tip ELI5
This is a list of doors the student's program is allowed to open, and what it may do once
inside each one.

Every door not on the list is locked. And being allowed through a door does not mean you
may do anything in the room: reading a file, replacing it, creating it, running it and
deleting it are five separate permissions, each switched on or off by itself.
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

# policy-focus-start
    regardingFileSystemInteractions:
      - onThisPathAndAllPathsBelow: "something.txt"
        readAllFiles: true
        overwriteAllFiles: true
        createAllFiles: true
        executeAllFiles: false
        deleteAllFiles: false
# policy-focus-end

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

Implemented by `FilePermission` in
[`policy/policySubComponents/FilePermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/FilePermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `onThisPathAndAllPathsBelow` | `String` | The path this entry governs, and everything beneath it. | `something.txt` | `FILE_PATH_PATTERN`: either `*`, or a non-blank path containing no `*` and no NUL byte, which must not traverse upwards with `..`. The placeholders `${PROJECT_ROOT}`, `${java.home}`, `${user.home}` and `${java.io.tmpdir}` are recognised; any other `${...}` is rejected. |
| `readAllFiles` | `boolean` | Permits reading below the path. | `true` | `true` or `false`. Absent means `false`. |
| `overwriteAllFiles` | `boolean` | Permits replacing the contents of existing files below the path. | `true` | `true` or `false`. Absent means `false`. |
| `createAllFiles` | `boolean` | Permits creating new files below the path. | `true` | `true` or `false`. Absent means `false`. |
| `executeAllFiles` | `boolean` | Permits executing files below the path. | `false` | `true` or `false`. Absent means `false`. |
| `deleteAllFiles` | `boolean` | Permits deleting files below the path. | `false` | `true` or `false`. Absent means `false`. |

## Notes

`createRestrictive(path)` builds an entry that names a path and grants nothing, which is how a deny-all default is expressed for a path that must still be mentioned.

The `..` rejection is a security property rather than a convenience: without it a policy entry could name a path inside the project and resolve to one outside it.
