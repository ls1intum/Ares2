---
title: "Package Permission"
sidebar_position: 8
description: "Which packages the supervised code may import, matched by prefix."
---

:::tip[ELI5]
Java code borrows tools from libraries by importing them.

Some of those tools are harmless, and some are a way straight around the fence. This list
names the libraries the student's program may borrow from. Naming a package also covers
everything inside it, so allowing `java.util` allows `java.util.concurrent` too.
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

# policy-focus-start
    regardingPackageImports:
      - importTheFollowingPackage: "java.util"
# policy-focus-end

    regardingTimeouts:
      - timeout: 120000
```

## Fields

Implemented by `PackagePermission` in
[`policy/policySubComponents/PackagePermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/PackagePermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `importTheFollowingPackage` | `String` | The package this entry permits, and every package beneath it. | `java.util` | Either the wildcard `*`, or `JAVA_PACKAGE_PATTERN`: a dot-separated Java package name, each segment a Java identifier that is not a reserved word (`\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*`). |

## Notes

Matching is by **prefix**. A permitted package `java.util` matches any package whose name starts with it, so `java.util.concurrent` and `java.util.stream` are covered by the single entry above.

`*` is accepted as a whole value by `matchesPackageImport`, which checks for it before falling through to the package pattern. It is not a general glob: `java.*` is not valid.
