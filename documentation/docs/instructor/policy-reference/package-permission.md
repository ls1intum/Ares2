---
title: "Package Permission"
sidebar_position: 8
description: "Which packages the supervised code may import, matched by prefix."
---

:::tip[Simple Story]
Pupils bring equipment into the room by importing libraries.

Some of it is harmless and some of it is a way straight past the checklist. This list names
what may be brought in. Naming a package covers everything inside it, so permitting
`java.util` permits `java.util.concurrent` as well. The one exception is Ares itself and the
libraries it is built on: a package above them does not cover them, only naming them does.
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
| `importTheFollowingPackage` | `String` | The package this entry permits, and every package beneath it, except a reserved namespace that the entry does not name (see the notes below). | `java.util` | Either the wildcard `*`, or `JAVA_PACKAGE_PATTERN`: a dot-separated Java package name, each segment a Java identifier that is not a reserved word (`\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*`). |

## Notes

Matching is by **prefix**. A permitted package `java.util` matches any package whose name starts with it, so `java.util.concurrent` and `java.util.stream` are covered by the single entry above.

A reserved namespace is never covered by a package above it. The namespaces are `java.`, `javax.`, `sun.`, `jdk.`, `com.sun.`, `de.tum.cit.ase.ares.api.`, `net.bytebuddy.`, `org.aspectj.`, `com.ibm.wala.` and `com.tngtech.archunit.`. An exercise in the package `de.tum.cit.ase` therefore permits `de.tum.cit.ase.exercise`, but not `de.tum.cit.ase.ares.api.policy`, and `org` does not permit `org.aspectj.lang`. Name the one you need instead: `de.tum.cit.ase.ares.api.policy` permits that package and the ones beneath it. Ares itself needs `org.aspectj` and always permits it, so an import of AspectJ is not refused.

`*` is accepted as a whole value by `matchesPackageImport`, which checks for it before falling through to the package pattern. It is not a general glob: `java.*` is not valid. Unlike a package name, it covers the reserved namespaces, because it states outright that every package is permitted.
