---
title: "Programming Language Configuration"
sidebar_position: 2
description: "The pipeline selector and the identity of the supervised code: build system, static analysis tool, dynamic analysis backend, root package and main class."
---

:::tip[Simple Story]
Before the checklist means anything, it has to say **which examination it belongs to and who
is working it**.

Which room (the package), where the pupils come in (the main class), and which equipment the
teacher brings: Maven or Gradle, ArchUnit or WALA, AspectJ or instrumentation. None of that
forbids anything yet. It settles who is on duty, and where.
:::

## Position in the example policy file

The section documented on this page is marked in red. Every page in this section shows the
same example file, so reading them in order walks it from top to bottom.

```yaml title="security-policy.yaml"
# policy-focus-start
thisPolicyFileCompliesToThePolicyVersion: 1
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_WALA_AND_ASPECTJ
  theSupervisedCodeUsesTheFollowingPackage: "org.example"
  theMainClassInsideThisPackageIs: "Main"
# policy-focus-end

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

    regardingTimeouts:
      - timeout: 120000
```

## Fields

The root version field is implemented by `SecurityPolicy`, everything else by `SupervisedCode` in
[`policy/policySubComponents/SupervisedCode.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/SupervisedCode.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `thisPolicyFileCompliesToThePolicyVersion` | `int` | The policy format the file is written against. This is a root field, a sibling of `regardingTheSupervisedCode` rather than one of its members, which is why it sits above it in the example. Required. | `1` | Exactly `1`. Any other value, and any file that omits the field, is rejected on load. |
| `theFollowingProgrammingLanguageConfigurationIsUsed` | enum `ProgrammingLanguageConfiguration` | Selects the whole supervision pipeline: build system, static analysis tool and dynamic analysis backend. Required. | `JAVA_USING_MAVEN_WALA_AND_ASPECTJ` | `^JAVA_USING_(?:MAVEN\|GRADLE)_(?:ARCHUNIT\|WALA)_AND_(?:ASPECTJ\|INSTRUMENTATION)$` |
| `theSupervisedCodeUsesTheFollowingPackage` | `String` | The root package holding the student code to supervise. **Required whenever a policy file is used.** The schema tolerates its absence, but the run then refuses to start rather than scanning for it. | `org.example` | `JAVA_PACKAGE_PATTERN`: a dot-separated Java package name, each segment a Java identifier that is not a reserved word (`\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*`) |
| `theMainClassInsideThisPackageIs` | `String` (nullable) | The entrypoint class used to build the call graph of the student program. Omitting it is safe: the project is scanned for it. | `Main` | `JAVA_CLASS_NAME_PATTERN`: a single Java type name, excluding `var`, `yield`, `record`, `sealed` and `permits` |

## Notes

The eight accepted values are the full cross product of the three dimensions: `MAVEN`/`GRADLE` × `ARCHUNIT`/`WALA` × `ASPECTJ`/`INSTRUMENTATION`. continuous integration (CI) exercises all four analysis and weaving combinations on every change.

**Omitting the package does not fall back to scanning.** The schema accepts a `null`, so the file loads, and `TestCaseAbstractFactoryAndBuilder` then refuses to set up the run at all, with `security.policy.supervised.package.required`. That is deliberate: a policy is the authoritative statement of what is supervised, and deriving the scope from the project instead would let the supervised code influence the boundary drawn around it. Project discovery is what the [policy-free configuration](../protect-a-java-project/further-options.md) does, and it is the only place it happens.

The main class is different, and it is the one nullable field of the two. It is used to build the call graph, never to decide what is supervised, so scanning for it cannot widen or narrow the boundary. Omitting it is ordinary.

Both fields are validated only when present. A blank string is not the same as absent and is rejected either way.

The version field is rejected before anything else is looked at, so a policy that omits it never reaches the pipeline it selects. That is worth knowing when a file that looks complete is refused: the diagnostic names the missing root field, not the section you were editing.
