---
title: "Package Permission"
sidebar_position: 8
description: "Which packages the supervised code may import, matched by prefix."
---

:::tip[Simple Story]
Pupils bring equipment into the room by importing libraries.

Some of it is harmless and some of it is a way straight past the checklist. This list names
what may be brought in. Naming a package covers everything inside it, so permitting
`java.util` permits `java.util.concurrent` as well. One exception is Ares itself and the
libraries it is built on: a package above them does not cover them, only naming them does.

A little is on every desk without being listed: `java.lang` and `java.io` themselves, and the
pupils' own packages. The teacher's keys never are: no entry on the list lets a pupil pick up
a test class. Without a checklist at all, Ares lets every `java.*` package in.
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
| `importTheFollowingPackage` | `String` | The package this entry permits, and every package beneath it unless `exactMatchOnly` is `true`, but never a reserved namespace that the entry does not name (see the notes below). | `java.util` | Either the wildcard `*`, or `JAVA_PACKAGE_PATTERN`: a dot-separated Java package name, each segment a Java identifier that is not a reserved word (`\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*`). |
| `exactMatchOnly` | `Boolean`, optional | Whether the entry permits only this package and none beneath it. Leaving it out means `false`. It changes nothing for `*`. | `true` | `true` or `false`; the reader refuses quoted values and `null`. |

## Notes

Matching is by **prefix**. A permitted package `java.util` matches itself and every package below it, so `java.util.concurrent` and `java.util.stream` are covered by the single entry above. With `exactMatchOnly: true`, the same entry permits `java.util` alone, and `java.util.concurrent` needs an entry of its own.

A reserved namespace is never covered by a package above it. The namespaces are `java.`, `javax.`, `sun.`, `jdk.`, `com.sun.`, `de.tum.cit.ase.ares.api.`, `net.bytebuddy.`, `org.aspectj.`, `com.ibm.wala.` and `com.tngtech.archunit.`. A permission for `de.tum.cit.ase` therefore covers `de.tum.cit.ase.exercise`, but not `de.tum.cit.ase.ares.api.policy`, and `org` does not permit `org.aspectj.lang`. Name the one you need instead: `de.tum.cit.ase.ares.api.policy` permits that package and the ones beneath it. Ares itself needs `org.aspectj` and always permits it, so an import of AspectJ is not refused.

`*` is accepted as a whole value by `matchesPackageImport`, which checks for it before falling through to the package pattern. It is not a general glob: `java.*` is not valid. Unlike a package name, it covers the reserved namespaces, because it states outright that every package is permitted.

### What is permitted without an entry

With a policy, Ares permits three things whether or not the list names them:

- `java.lang` and `java.io`, each as that package only. Every class extends `java.lang.Object`, and `System.out.println` depends on `java.io.PrintStream`. Their subpackages, such as `java.lang.reflect`, need an entry.
- The packages the supervised code's own compiled classes declare, each as that package only. Ares reads them from the classes it checks when the rule runs.
- The packages Ares itself weaves into the supervised code: `org.aspectj` and `de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut`.

Every other `java.*` package, all of `javax`, `jdk`, `sun` and `com.sun`, and every third-party library needs an entry.

### Test classes are never importable

The supervised code must not depend on a class named in `theFollowingClassesAreTestClasses`, nor on a class whose name starts with that name followed by `$`, which covers the classes nested in it. No entry on this list changes that, not even one naming the test class's own package.

### Without a policy

A test without `@Policy` runs with a restrictive default. There the supervised code can import every `java.*` package, `java.lang.reflect` included. `javax`, `jdk`, `sun`, `com.sun` and third-party packages stay forbidden, and the other rules, such as the ones for reflection and file access, still apply.
