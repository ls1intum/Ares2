---
title: "File System Access"
sidebar_position: 4
description: "Which paths the supervised code may read, overwrite, create, execute or delete."
---

:::tip[Simple Story]
A worksheet is a sheet of paper a pupil can be allowed to use. For each worksheet, the rule
sheet says separately whether the pupil can read it, change it, make it, use it or throw it
away.

All five have to be on the rule sheet, even when the answer is no. The teacher does not
accept a rule sheet that leaves one out. A worksheet the rule sheet does not mention is not
allowed.

[Who is who](/instructor/who-is-who)
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
| `onThisPathAndAllPathsBelow` | `String` | The path this entry governs, and everything beneath it. | `something.txt` | `FILE_PATH_PATTERN`: either `*`, or a non-blank path containing no `*`, no control character and no line or paragraph separator, which must not traverse upwards with `..`. The placeholders `${PROJECT_ROOT}`, `${java.home}`, `${user.home}` and `${java.io.tmpdir}` are recognised; any other `${...}` is rejected. Precompile refuses three more kinds of path: one containing `#` or a carriage return, one with spaces at either end, and a whole bracketed section name such as `[write]`. |
| `readAllFiles` | `boolean` | Permits reading below the path. | `true` | `true` or `false`. Required: an entry that omits it is rejected on load. |
| `overwriteAllFiles` | `boolean` | Permits replacing the contents of existing files below the path. | `true` | `true` or `false`. Required: an entry that omits it is rejected on load. |
| `createAllFiles` | `boolean` | Permits creating new files below the path. | `true` | `true` or `false`. Required: an entry that omits it is rejected on load. |
| `executeAllFiles` | `boolean` | Permits executing files below the path. | `false` | `true` or `false`. Required: an entry that omits it is rejected on load. |
| `deleteAllFiles` | `boolean` | Permits deleting files below the path. | `false` | `true` or `false`. Required: an entry that omits it is rejected on load. |

### What works without an entry

Some file accesses are part of what ordinary Java code does and carry no risk, so Ares allows them even when this list is empty.

| What the student code does | Why it needs no entry |
| --- | --- |
| Draws random numbers with `SecureRandom`, or calls `UUID.randomUUID()` | The Java Development Kit (JDK) reads its random-seed device itself. Opening `/dev/urandom` directly still needs an entry. |
| Uses the system timezone, for example `ZoneId.systemDefault()` or `ZonedDateTime.now()` | The JDK reads the timezone without Java's file classes, so no check sees it. |
| Loads the JDK's trusted certificates, as an encrypted connection does | The certificate file lies inside the Java installation, which is readable anyway. The connection itself still needs a network entry. |
| Formats numbers, dates or text for a locale, or uses a charset | Their data lies inside the Java installation too. |
| Creates a temporary file without naming a directory: `File.createTempFile(prefix, suffix)` or `Files.createTempFile(prefix, suffix)` | The file always lands in the default temp directory, which Ares fixes when the tests start. |
| Reads or writes through a stream or reader that is not a file, for example `new BufferedReader(new InputStreamReader(System.in))`, a `ByteArrayInputStream`, or `Properties.load` from a `StringReader` | Only opening a file needs an entry. Ares still refuses a wrapper over a file where the code opens the file. |

A temporary file in a directory the code names still needs an entry for that directory, unless the list has an entry and the directory is exactly the default temp directory. A folder inside the temp directory does not count, and `Files.createTempDirectory` needs an entry too.

Ares takes the temp directory from the values the Java Virtual Machine (JVM) started with, so code that changes `java.io.tmpdir` later cannot move it. With instrumentation the Ares agent records them at start-up. With AspectJ no agent is needed, but the test JVM needs the arguments the documented build files pass: `--add-exports java.base/jdk.internal.misc=ALL-UNNAMED`, and, for `File.createTempFile`, `--add-opens java.base/java.io=ALL-UNNAMED`. Without them Ares refuses the temporary file, and its message names the missing argument.

`java.util.prefs.Preferences` counts as file access, but its backing store has no path to name, so only an entry `"*"` for the action concerned permits it.

Ares's own files, such as its messages, need no entry either. Ares reads them itself, not on behalf of the student code, so no rule about their names is needed. A file that merely has the same name as one of Ares's files gets the same check as any other.

[The contributor documentation](/contributor/subsystems/aop/baseline-verification) describes how both enforcement backends verify each of these.

## Notes

**Writing a file needs one flag, chosen by whether the file exists.** Opening a file for
writing needs `createAllFiles` when the file does not exist yet and `overwriteAllFiles` when it
does. That holds for every way of writing, from `Files.writeString` and `FileWriter` to
`FileChannel.open` with `CREATE` and `RandomAccessFile` in mode `"rw"`. `createAllFiles` alone therefore lets the code write a new
file with its content, and `overwriteAllFiles` alone lets it replace an existing file but not
make one. `CREATE_NEW` always needs `createAllFiles`, and writing without `CREATE` always needs
`overwriteAllFiles`. A later write through a file that is already open, such as
`FileChannel.write`, needs `overwriteAllFiles`. `createAllFiles` is enough only where the code
created the file through an opening that Ares checked, and only through what that opening
returned. A channel the test class opened still needs `overwriteAllFiles`. With instrumentation,
Ares checks a write or truncate through an open channel only where the code opens the channel and in
transfers, not at each call. Ares refuses a write through a link that points to a missing file,
while a copy or move with `REPLACE_EXISTING` replaces such a link under `overwriteAllFiles`.

**A class-path resource is read from where the build put it.** Ares checks
`Class.getResourceAsStream` and `ClassLoader.getResourceAsStream` as a read of the file that
holds the resource. That file is the copy in the build output, `target/classes` or
`target/test-classes` with Maven, not the one in `src/main/resources`, so the entry names the
copy. Ares checks a resource inside a JAR as a read of that JAR.

`createRestrictive(path)` builds an entry that names a path and grants nothing, which is how a deny-all default is expressed for a path that must still be mentioned.

**All six fields are required.** `SecurityPolicySchemaValidator` passes the file field set as both the accepted and the required set, so an entry that leaves a boolean out is rejected when the policy is loaded rather than read as a denial. Write `false` explicitly for every operation the entry does not permit. Omitting the whole entry is what expresses "nothing is permitted here".

The `..` rejection is a security property rather than a convenience: without it a policy entry could name a path inside the project and resolve to one outside it.
