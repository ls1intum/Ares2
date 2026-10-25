---
title: "Package Permission"
sidebar_position: 8
description: "Which packages the supervised code may import, matched by prefix."
---

:::tip[Simple Story]
Pupils bring equipment into the room by importing libraries.

Some of it is harmless and some of it is a way straight past the checklist. This list names
what may be brought in. Naming a package covers everything inside it, so permitting
`java.util` permits `java.util.concurrent` as well.
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

**Type annotations count as imports.** Ares checks a type that the code names only through a type annotation, such as `@NonNull` in `@NonNull String`, like any other import. It checks the classes, enums and annotations written inside the values of such an annotation in the same way. The Checker Framework's `@NonNull` therefore needs `importTheFollowingPackage: "org.checkerframework.checker.nullness.qual"`. Ares reads these annotations from the compiled class, so it sees only what the compiler kept there. An annotation with source retention never reaches the compiled class, so Ares neither sees it nor needs permission for it. The AspectJ compiler keeps no type annotation on `extends`, on a local variable or in a lambda. Where it compiles the supervised code, as the `compile` goal of the `aspectj-maven-plugin` does, Ares cannot see those annotations.
