---
title: "Class Permission"
sidebar_position: 2
description: "The classes that are trusted rather than sandboxed, derived from the declared test classes and the essential classes."
---

:::tip[ELI5]
Some code has to be allowed to do the things everyone else is forbidden from doing.

The test class itself, for example, may need to read a file in order to check that the
student's program wrote it correctly. If Ares sandboxed the test too, the test could never
check anything. So the classes listed here get a staff pass: the rules still exist, but
these classes are on the other side of the counter.
:::

## Position in the example policy file

The section documented on this page is marked in red. Every page in this section shows the
same example file, so reading them in order walks it from top to bottom.

```yaml title="security-policy.yaml"
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_WALA_AND_ASPECTJ
  theSupervisedCodeUsesTheFollowingPackage: "org.example"
  theMainClassInsideThisPackageIs: "Main"

# policy-focus-start
  theFollowingClassesAreTestClasses:
    - "org.example.PenguinTest"
# policy-focus-end

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

Implemented by `ClassPermission` in
[`policy/policySubComponents/ClassPermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/ClassPermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `className` | `String` | A fully qualified class name that is trusted and therefore not sandboxed. Never written directly in the policy file; see the note below. | `org.example.PenguinTest` | Must not be `null` and must not be blank. No pattern is applied, because the values are produced internally rather than authored. |

## Notes

**This is the one entry in this section with no field of its own in the policy file.** `ClassPermission` is derived, not authored. `JavaCreator.prepareAllowedClasses` builds the set by concatenating two streams:

1. the **essential classes**, which are Ares' own infrastructure, and
2. the **test classes** declared in `theFollowingClassesAreTestClasses`, the field marked above.

Entries that are `null` or blank are filtered out *before* a `ClassPermission` is constructed, because its constructor throws on both, and one malformed entry from a scanned project would otherwise abort the creation of every test case.

The resulting set reaches both enforcement layers as `allowedClasses`, on `JavaAOPTestCase` and on `JavaArchitectureTestCase`.

Each entry in `theFollowingClassesAreTestClasses` is itself validated against `JAVA_CLASS_PATH_PATTERN`, a fully qualified Java class name.
