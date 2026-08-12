---
title: "Test class exemptions"
sidebar_position: 3
description: "The field that exempts your test classes from enforcement, and why there is no class-permission field to write."
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
thisPolicyFileCompliesToThePolicyVersion: 1
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
      - timeout: 120000
```

## Fields

Implemented by `ClassPermission` in
[`policy/policySubComponents/ClassPermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/ClassPermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `className` | `String` | A fully qualified class name that is trusted and therefore not sandboxed. Never written directly in the policy file; see the note below. | `org.example.PenguinTest` | Must not be `null` and must not be blank. No pattern is applied, because the values are produced internally rather than authored. |

## Notes

**This is the one entry in this section with no field of its own in the policy file.** The field you write is `theFollowingClassesAreTestClasses`, marked above. The `ClassPermission` record Ares builds from it is derived, not authored: it is the union of two sets,

1. the **essential classes**, which are Ares' own infrastructure, and
2. the **test classes** declared in `theFollowingClassesAreTestClasses`, the field marked above.

How that set is derived and how it reaches the two enforcement layers is described in the contributor guide under [Class permission](/contributor/policy/class-permission).

Each entry in `theFollowingClassesAreTestClasses` is itself validated against `JAVA_CLASS_PATH_PATTERN`, a fully qualified Java class name.
