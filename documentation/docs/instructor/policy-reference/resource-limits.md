---
title: "Resource Limits"
sidebar_position: 9
description: "The wall-clock budget the supervised code is given."
---

:::tip[ELI5]
An endless loop never fails on its own. It just runs, and runs, and the build never
finishes.

So Ares hands the student's program an egg timer. When the time is up, the program is
stopped and the test reports a timeout, instead of a build that hangs until someone
notices.
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

    regardingCommandExecutions:
      - executeTheCommand: "ls"
        withTheseArguments:
          - "-l"

    regardingThreadCreations:
      - createTheFollowingNumberOfThreads: 10
        ofThisClass: "org.example.Worker"

    regardingPackageImports:
      - importTheFollowingPackage: "java.util"

# policy-focus-start
    regardingTimeouts:
      - timeout: 120
# policy-focus-end
```

## Fields

Implemented by `ResourceLimitsPermission` in
[`policy/policySubComponents/ResourceLimitsPermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/ResourceLimitsPermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `timeout` | `long` | The wall-clock budget, in seconds, for the supervised code. | `120` | Range `1` to `Long.MAX_VALUE`. Zero and negative values are rejected, so a policy cannot express an instantly expiring or unbounded budget through this field. |

## Notes

`regardingTimeouts` is the one list in `ResourceAccesses` whose restrictive default is not empty: `ResourceAccesses.createRestrictive()` seeds it with `ResourceLimitsPermission.createRestrictive()`. Every other domain defaults to an empty list, which means deny; a timeout has to have *some* value, so the restrictive default is a value rather than an absence.

The field is a `long` rather than an `int`, so the budget is not capped at roughly 68 years of seconds. That is not a practical concern; it simply avoids a needless narrowing.
