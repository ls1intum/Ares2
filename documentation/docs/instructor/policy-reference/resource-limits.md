---
title: "Resource Limits"
sidebar_position: 9
description: "The execution budget a policy can express, the unit it is expressed in, and what bounds a test today."
---

:::tip[Simple Story]
A pupil who never stops writing never finishes and never fails. They simply keep going, and
the examination never ends.

So the checklist has a line for the clock. Mind this one, though: the time is written onto the
checklist, but nothing yet winds the clock up. What stops a runaway pupil today is the
`@StrictTimeout` annotation.
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
      - timeout: 120000
# policy-focus-end
```

## Fields

Implemented by `ResourceLimitsPermission` in
[`policy/policySubComponents/ResourceLimitsPermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/ResourceLimitsPermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `timeout` | `long` | The execution budget, in **milliseconds**, for the supervised code. | `120000` | Range `1` to `Long.MAX_VALUE`. Zero and negative values are rejected, so a policy cannot express an instantly expiring or unbounded budget through this field. |

## Notes

`regardingTimeouts` is the one list in `ResourceAccesses` whose restrictive default is not empty: `ResourceAccesses.createRestrictive()` seeds it with `ResourceLimitsPermission.createRestrictive()`. Every other domain defaults to an empty list, which means deny; a timeout has to have *some* value, so the restrictive default is a value rather than an absence.

**The unit is milliseconds, not seconds.** `ResourceLimitsPermission` defines the value in milliseconds and `JavaResourceLimitsExtractor.getTightestTimeout()` returns it in milliseconds; the restrictive default of `10000` is ten seconds. The Phobos configuration expresses its own timeout in seconds, and the conversion happens once, where that configuration is written. A value chosen as though the policy field were seconds is a budget a thousand times shorter than intended, which is why the example reads `120000` rather than `120`.

**The value does not bound a test today.** Timeouts belong to the Phobos test-case family, which Ares generates without yet dispatching it from the in-process execution path. The field is parsed, validated and written into the generated configuration, and nothing reads it back, so a policy timeout is a recorded intention rather than a deadline. Use [`@StrictTimeout`](../protect-a-java-project/precompile-or-postcompile.md#glossary) wherever a test needs one.

The field is a `long` rather than an `int`, so the budget is not capped at the roughly 25 days an `int` of milliseconds allows. That is not a practical concern; it simply avoids a needless narrowing.
