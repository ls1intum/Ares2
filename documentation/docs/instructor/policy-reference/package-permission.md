---
title: "Package Permission"
sidebar_position: 8
description: "Which packages the supervised code may import, matched by prefix."
---

:::tip[Simple Story]
The permitted aids are the things a pupil's work is allowed to lean on. The rule sheet lists
them. Only a few aids are always allowed, whatever it says, such as the pupils' own things and
the two simplest aids every pupil needs to write and to print. No line on the rule sheet lets a
pupil lean on the grading instructions.

Allowing an aid allows every part that belongs to it, unless the line says that one aid only.
One short line can therefore let in far more than it seems to. Some parts with names of their
own still need a line of their own.

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
| `importTheFollowingPackage` | `String` | The package this entry permits, and every package beneath it, but never a reserved namespace that the entry does not name (see the notes below). | `java.util` | Either the wildcard `*`, or `JAVA_PACKAGE_PATTERN`: a dot-separated Java package name, each segment a Java identifier that is not a reserved word (`\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*`). |

## Notes

Matching is by **prefix**. A permitted package `java.util` matches itself and every package below it, so `java.util.concurrent` and `java.util.stream` are covered by the single entry above. A package cannot be permitted without the packages below it.

A reserved namespace is never covered by a package above it. The reserved namespaces are:

- the five of the Java Development Kit (JDK): `java.`, `javax.`, `sun.`, `jdk.` and `com.sun.`;
- Ares's own `de.tum.cit.ase.ares.api.` and Ares 1's `de.tum.in.test.api.`;
- those of the libraries and tools Ares and its tests run with, under `org.`: `org.aspectj.`, `org.junit.`, `org.opentest4j.`, `org.assertj.`, `org.mockito.`, `org.slf4j.`, `org.yaml.`, `org.json.`, `org.jgrapht.`, `org.apiguardian.`, `org.apache.`, `org.eclipse.`, `org.gradle.` and `org.jacoco.`;
- and the others of that kind: `net.bytebuddy.`, `com.ibm.wala.`, `com.tngtech.archunit.`, `junit.`, `ch.qos.logback.`, `com.github.javaparser.`, `com.fasterxml.jackson.`, `com.opencsv.`, `com.intellij.` and `worker.org.gradle.`.

A permission for `de.tum.cit.ase` therefore covers `de.tum.cit.ase.exercise`, but not `de.tum.cit.ase.ares.api.policy`, and `org` does not permit `org.aspectj.lang`. Name the one you need instead: `de.tum.cit.ase.ares.api.policy` permits that package and the ones beneath it. Ares itself needs `org.aspectj` and always permits it, so an import of AspectJ is not refused.

`*` is accepted as a whole value by `matchesPackageImport`, which checks for it before falling through to the package pattern. It is not a general glob: `java.*` is not valid. Unlike a package name, it covers the reserved namespaces, because it states outright that every package is permitted.

**Type annotations count as imports.** Ares checks a type that the code names only through a type annotation, such as `@NonNull` in `@NonNull String`, like any other import. It checks the classes, enums and annotations written inside the values of such an annotation in the same way. The Checker Framework's `@NonNull` therefore needs `importTheFollowingPackage: "org.checkerframework.checker.nullness.qual"`. Ares reads these annotations from the compiled class, so it sees only what the compiler kept there. An annotation with source retention never reaches the compiled class, so Ares neither sees it nor needs permission for it. In AspectJ 1.9.26, the compiler keeps no type annotation on `extends` or on a local variable. Where that version compiles the supervised code, as the `compile` goal of the `aspectj-maven-plugin` does, Ares cannot see those annotations. With Ares on its aspect path, that goal weaves every supervised class. AspectJ 1.9.27 then drops every type annotation inside a method body as well, such as on a cast, an `instanceof` or a `new`. It keeps those on a field, a return type or a parameter. The woven class no longer depends on the package of a dropped annotation, so Ares neither sees it nor needs permission for it.

### What is permitted without an entry

With a policy, Ares permits three things whether or not the list names them:

- `java.lang` and `java.io`, each with the packages beneath it. Every class extends `java.lang.Object`, and `System.out.println` depends on `java.io.PrintStream`. So the supervised code can import `java.lang.reflect`, `java.lang.invoke` and, on newer JDKs, `java.lang.foreign` too, but the method rules, such as the one for reflection, still apply.
- The packages the supervised code's own compiled classes declare, each with the packages beneath it. Ares reads them from the classes it checks when the rule runs.
- The packages Ares itself weaves into the supervised code: `org.aspectj` and `de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut`.

Every other `java.*` package, all of `javax`, `jdk`, `sun` and `com.sun`, and every third-party library outside those packages needs an entry. Below the supervised code's own packages, a class needs no entry, unless it is a named test class. The checks on reflection and the other refused categories run on the compiled code, with no general check while it runs.

### Test classes are never importable

The supervised code must not depend on a class named in `theFollowingClassesAreTestClasses`, nor on a class whose name starts with that name followed by `$`, which covers the classes nested in it. No entry on this list changes that, not even one naming the test class's own package.

### Without a policy

A test without `@Policy` runs with a restrictive default. There the supervised code can import every `java.*` package, `java.lang.reflect` included. `javax`, `jdk`, `sun`, `com.sun` and third-party packages stay forbidden, and the other rules, such as the ones for reflection and file access, still apply.
