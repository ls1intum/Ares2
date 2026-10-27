---
title: "Thread System Access"
sidebar_position: 7
description: "How many threads of which class the supervised code may create."
---

:::tip[Simple Story]
A thread is a pupil doing two things at once.

Left unbounded, one pupil can call in thousands of helpers and bring the whole room to a halt,
by accident as easily as on purpose. So the checklist says which kind of helper may be called
in, and how many at most.
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

# policy-focus-start
    regardingThreadCreations:
      - createTheFollowingNumberOfThreads: 10
        ofThisClass: "org.example.Worker"
# policy-focus-end

    regardingPackageImports:
      - importTheFollowingPackage: "java.util"

    regardingTimeouts:
      - timeout: 120000
```

## Fields

Implemented by `ThreadPermission` in
[`policy/policySubComponents/ThreadPermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/ThreadPermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `createTheFollowingNumberOfThreads` | `int` | The maximum number of threads of this class that may be created. | `10` | Range `0` to `Integer.MAX_VALUE`. Negative values are rejected; `0` permits the class but no instances. |
| `ofThisClass` | `String` | The thread class this entry governs. | `org.example.Worker` | `THREAD_CLASS_PATTERN`: a fully qualified Java class name, `*`, `Lambda-Expression`, or one of the implicit tokens `<implicit-thread-op:parallelStream>`, `<implicit-thread-op:parallel>`, `<implicit-thread-op:Thread.sleep>`, `<implicit-thread-op:SubmissionPublisher.submit>` and `<implicit-thread-op:SubmissionPublisher.offer>`. |

## Notes

The implicit tokens exist because not every thread is created by name. A parallel stream, a `Thread.sleep` or a `SubmissionPublisher` submission all reach the threading machinery without the student ever writing `new Thread(...)`, so each needs a token a policy can refer to.

`Lambda-Expression` covers a thread body supplied as a lambda, which has no nameable class.
