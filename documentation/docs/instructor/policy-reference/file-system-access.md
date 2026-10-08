---
title: "File System Access"
sidebar_position: 4
description: "Which paths the supervised code may read, overwrite, create, execute or delete."
---

:::tip[Simple Story]
This is the list of papers a pupil may pick up, and what they may do with each one.

Anything not on the list stays out of reach. And being allowed to pick something up is not
permission to do as you like with it: reading, replacing, creating, running and destroying are
five separate permissions, each granted on its own.
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
      - timeout: 120000
```

## Fields

Implemented by `FilePermission` in
[`policy/policySubComponents/FilePermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/FilePermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `onThisPathAndAllPathsBelow` | `String` | The path this entry governs, and everything beneath it. | `something.txt` | `FILE_PATH_PATTERN`: either `*`, or a non-blank path containing no `*` and no NUL byte, which must not traverse upwards with `..`. The placeholders `${PROJECT_ROOT}`, `${java.home}`, `${user.home}` and `${java.io.tmpdir}` are recognised; any other `${...}` is rejected. |
| `readAllFiles` | `boolean` | Permits reading below the path. | `true` | `true` or `false`. Required: an entry that omits it is rejected on load. |
| `overwriteAllFiles` | `boolean` | Permits replacing the contents of existing files below the path. | `true` | `true` or `false`. Required: an entry that omits it is rejected on load. |
| `createAllFiles` | `boolean` | Permits creating new files below the path. | `true` | `true` or `false`. Required: an entry that omits it is rejected on load. |
| `executeAllFiles` | `boolean` | Permits executing files below the path. | `false` | `true` or `false`. Required: an entry that omits it is rejected on load. |
| `deleteAllFiles` | `boolean` | Permits deleting files below the path. | `false` | `true` or `false`. Required: an entry that omits it is rejected on load. |

## What works without an entry

Some file accesses are part of what ordinary Java code does and carry no risk, so Ares allows them even when this list is empty.

| What the student code does | Why it needs no entry |
| --- | --- |
| Draws random numbers with `SecureRandom`, or calls `UUID.randomUUID()` | The Java Development Kit (JDK) reads its random-seed device itself. Opening `/dev/urandom` directly still needs an entry. |
| Uses the system timezone, for example `ZoneId.systemDefault()` or `ZonedDateTime.now()` | The JDK reads the timezone without Java's file classes, so no check sees it. |
| Loads the JDK's trusted certificates, as an encrypted connection does | The certificate file lies inside the Java installation, which is readable anyway. The connection itself still needs a network entry. |
| Formats numbers, dates or text for a locale, or uses a charset | Their data lies inside the Java installation too. |
| Creates a temporary file without naming a directory: `File.createTempFile(prefix, suffix)` or `Files.createTempFile(prefix, suffix)` | The file always lands in the default temp directory, which Ares fixes when the tests start. |

A temporary file in a directory the code names still needs an entry for that directory, even for a folder inside the temp directory, and so does `Files.createTempDirectory`. Ares fixes the temp directory when the Ares agent starts with the Java Virtual Machine (JVM). Without the agent Ares refuses a temporary file without a named directory, with a message saying Ares did not start. That happens, for example, in an exercise prepared ahead of time that runs with AspectJ. Name a directory there and list it here with `createAllFiles: true`.

## Notes

`createRestrictive(path)` builds an entry that names a path and grants nothing, which is how a deny-all default is expressed for a path that must still be mentioned.

**All six fields are required.** `SecurityPolicySchemaValidator` passes the file field set as both the accepted and the required set, so an entry that leaves a boolean out is rejected when the policy is loaded rather than read as a denial. Write `false` explicitly for every operation the entry does not permit. Omitting the whole entry is what expresses "nothing is permitted here".

The `..` rejection is a security property rather than a convenience: without it a policy entry could name a path inside the project and resolve to one outside it.
