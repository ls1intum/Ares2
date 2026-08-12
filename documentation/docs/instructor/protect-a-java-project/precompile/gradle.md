---
title: "Gradle"
sidebar_position: 1
description: "Protecting a Gradle project with Ares 2 in Precompile mode, from policy file to enforced build."
---

:::tip[ELI5]
This is the whole path for a Gradle exercise that has its security tests generated into it
beforehand.

Write the policy, run the generator, then wire the generated files into the build.
:::

Precompile generates the enforcement artefacts before the exercise is built. The generator is run
once, from outside the project, and what it writes is then compiled by the exercise's own build
like any other source.

## The path, in order

1. **Define the policy**, below.
2. **Run the generator**, below, from the Ares UI or from the main class.
3. **Check what was written into the project**, below.
4. **Update `build.gradle`**, below.
5. **Reject student classes in reserved packages**, below. Precompile does **not** generate this
   step; you install it by hand exactly as in Postcompile.
6. **Verify with both controls**, below.

## Define the policy file

A security policy must be provided for the precompile phase. The policy file does not have to
be stored inside the student project, and is typically maintained separately by instructors or
test authors.

For illustration only, the following example assumes that `security-policy.yaml` sits in the
project root:

```text
example_project/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── org/example/
│   │           ├── Main.java
│   │           └── Penguin.java
│   ├── test/
│   │   └── java/
│   │       └── org/example/
│   │           └── PenguinTest.java
├── build.gradle
├── pom.xml
├── secret.txt
├── something.txt
└── security-policy.yaml   ← example location
```

The policy can be authored manually as YAML, or created with
[Ares2UI](https://github.com/ls1intum/Ares2UI), which provides a graphical editor with guided
configuration and validation. Both approaches produce the same `security-policy.yaml`, which
is the input for the precompile phase.

With the following minimal configuration, virtually all actions are denied by default,
providing a strict precompile sandbox for the sample test:

```yaml
thisPolicyFileCompliesToThePolicyVersion: 1
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
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
        executeAllFiles: true
        deleteAllFiles: true
    regardingNetworkConnections: [ ]
    regardingCommandExecutions: [ ]
    regardingThreadCreations: [ ]
    regardingPackageImports: [ ]
    regardingTimeouts: [ ]
```

This permits file system operations **only** on `something.txt`. All other paths, including
`secret.txt`, are not listed and are therefore fully denied by the sandbox. Any attempt by
student code to read, create, overwrite or delete `secret.txt` is intercepted and blocked.

To see this from the other side, modify the sample code to operate on `something.txt` instead;
those operations succeed, whereas accesses to `secret.txt` correctly trigger a security
exception.

Once the policy is defined, the precompile phase can be run in one of two ways, described
below.

## Run the generator from the Ares UI

[Ares2UI](https://github.com/ls1intum/Ares2UI) provides an interactive way to run the
precompile phase without invoking the command-line runner.

**Select the project directory.** Choose the root directory of the student project to be
processed. For the example above, that is `example_project/`.

**Select or create a security policy.** If a `security-policy.yaml` already exists it can be
selected and loaded. Alternatively, create a new policy with the built-in editor via the
`Create Policy` button.

**Execute the precompile phase.** Clicking `Create Files` runs the precompile phase and
generates the enforcement artefacts inside the selected project.

## Run the generator from the main class

Open the main class in the Ares 2 repository,
[`src/main/java/de/tum/cit/ase/ares/api/Main.java`](https://github.com/ls1intum/Ares2/tree/main/src/main/java/de/tum/cit/ase/ares/api/Main.java),
and fill in the three required paths: the path to your `SecurityConfiguration.yaml`, the path
to the project to be precompiled, and the `src/test/java` directory inside that project.

```java
public class Main {
    public static void main(String[] args) {
        SecurityPolicyReaderAndDirector securityPolicyReaderAndDirector =
            new SecurityPolicyReaderAndDirector(
                // Path to your SecurityConfiguration.yaml
                Path.of("<path-to-your-SecurityConfiguration.yaml>"),

                // Path to the project you want to test (the student's project)
                // (e.g., the example_project/ directory shown above)
                Path.of("<path-to-student-project>")
            ).createTestCases();

        // Path to the test/java directory inside the same project
        securityPolicyReaderAndDirector.writeTestCases(
            Path.of("<path-to-student-project>/src/test/java")
        );
    }
}
```

Once the paths are set, run the main class. Ares 2 interprets the configuration, generates the
corresponding enforcement rules and writes them into the target project, completing the
precompile integration.

## What ends up in the project

After the precompile phase has run, whether via Ares2UI or the command-line runner, the
project contains additional generated files needed for enforcement during postcompile
execution:

```text
example_project/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── org/example/
│   │           ├── Main.java
│   │           └── Penguin.java
│   ├── resources/
│   ├── test/
│   │   └── java/
│   │       └── org/example/
│   │           ├── PenguinTest.java
│   │           ├── ares/api/
│   │           └── META-INF/
├── build.gradle
├── pom.xml
├── secret.txt
├── something.txt
└── security-policy.yaml   ← example location
```

The generated directory contains Ares 2 specific helper code, instrumentation logic and
metadata required during test execution.

:::danger[Do not edit the generated files]
They are produced automatically by the precompile phase and must not be modified by hand.
:::

## Update `build.gradle`

The configuration below assumes the student project uses the Java package `org.example`, as in
the example structure above. If a different base package is used, every occurrence of
`org/example` must be adapted accordingly.

```groovy
plugins {
    id 'java'
    id 'application'
}

group = 'de.tum.cit.aet'
version = '1.0-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

ext {
    ver = [
            junit                : '6.0.0',
            junitPlatformLauncher: '6.0.0',
            bytebuddy            : '1.17.7',
            aspectj              : '1.9.24',
            archunit             : '1.4.1',
            wala                 : '1.6.12',
            jacksonYaml          : '2.20.0',
            guava                : '31.1-jre',
            jsr305               : '3.0.2',
            opencsv              : '5.12.0'
    ]
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation "net.bytebuddy:byte-buddy:${ver.bytebuddy}"
    testImplementation "org.junit.jupiter:junit-jupiter:${ver.junit}"
    testImplementation "org.junit.platform:junit-platform-launcher:${ver.junitPlatformLauncher}"
    testImplementation "com.google.code.findbugs:jsr305:${ver.jsr305}"
    testImplementation "com.google.guava:guava:${ver.guava}"
    testImplementation "com.opencsv:opencsv:${ver.opencsv}"
    testImplementation "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${ver.jacksonYaml}"
    testImplementation "com.tngtech.archunit:archunit-junit5:${ver.archunit}"
    testImplementation "com.ibm.wala:com.ibm.wala.core:${ver.wala}"
    testImplementation "org.aspectj:aspectjrt:${ver.aspectj}"
    testImplementation 'io.vavr:vavr:0.10.4'
}

application {
    mainClass = 'org/example.Main'
}

test {
    //maxParallelForks = 1
    useJUnitPlatform()
    dependsOn 'javaagentJar', 'xbootclasspathJar'
    doFirst {
        def agentFile = tasks.named('javaagentJar').get().archiveFile.get().asFile
        def bootFile = tasks.named('xbootclasspathJar').get().archiveFile.get().asFile

        // Collect full Byte Buddy (and agent) jars to put on bootstrap append path
        def byteBuddyJars = configurations.testRuntimeClasspath.files.findAll { it.name.startsWith('byte-buddy') }*.absolutePath
        // Collect opentest4j jars to place on bootstrap append path as requested
        def opentest4jJars = configurations.testRuntimeClasspath.files.findAll { it.name.contains('opentest4j') }*.absolutePath
        // Collect ArchUnit jars to also place on bootstrap append path
        def archUnitJars = configurations.testRuntimeClasspath.files.findAll { it.name.startsWith('archunit') }*.absolutePath
        // Collect JUnit (org.junit.*) jars to place on bootstrap append path
        def junitJars = configurations.testRuntimeClasspath.files.findAll { it.name.startsWith('junit-') || it.name.startsWith('junit-platform-') }*.absolutePath
        // Collect SLF4J (org.slf4j.*) jars to place on bootstrap append path
        def slf4jJars = configurations.testRuntimeClasspath.files.findAll { it.name.contains('slf4j') }*.absolutePath
        // Collect OpenCSV (com.opencsv.*) jars to place on bootstrap append path
        def openCsvJars = configurations.testRuntimeClasspath.files.findAll { it.name.startsWith('opencsv-') || it.name.contains('opencsv') }*.absolutePath
        // Avoid duplicates if already present
        def bootAppendEntries = ([bootFile.absolutePath] + byteBuddyJars + opentest4jJars + archUnitJars + junitJars + slf4jJars + openCsvJars).unique()
        def bootAppend = bootAppendEntries.join(File.pathSeparator)

        // Clear any previous -Xbootclasspath/a we might have added on incremental runs
        def filtered = (jvmArgs ?: []).findAll { !it.startsWith('-Xbootclasspath/a:') && !it.startsWith('-javaagent:') }
        jvmArgs = filtered

        jvmArgs "-javaagent:${agentFile.absolutePath}"
        jvmArgs "-Xbootclasspath/a:${bootAppend}"
        jvmArgs "-Dinstrboot.jar.path=${bootFile.absolutePath}"
        jvmArgs '-Xshare:off'

        // Required opens for deep reflection/instrumentation
        jvmArgs '--add-opens', 'java.base/java.io=ALL-UNNAMED'
        jvmArgs '--add-opens', 'java.base/java.nio.file=ALL-UNNAMED'
        jvmArgs '--add-opens', 'java.base/java.lang=ALL-UNNAMED'
    }
}

tasks.register('copyTestTxtIntoClasses', Copy) {
    from('src/test/java') {
        include '**/*.txt'
    }
    into layout.buildDirectory.dir('classes/java/test')
}

tasks.register('javaagentJar', Jar) {
    archiveClassifier = 'agent'
    dependsOn 'testClasses'
    from(sourceSets.test.output) {
        include 'org/example/ares/api/aop/java/instrumentation/**'
        include '**/*$*.class'
    }
    manifest {
        from(file('src/test/java/org/example/META-INF/MANIFEST.MF'))
    }
}

tasks.register('xbootclasspathJar', Jar) {
    archiveClassifier = 'instrboot'
    dependsOn 'testClasses'
    from(sourceSets.test.output) {
        // Include all instrumentation advice classes
        include 'org/example/ares/api/aop/java/instrumentation/advice/**'
        // Include all instrumentation pointcut classes
        include 'org/example/ares/api/aop/java/instrumentation/pointcut/**'
        // Include the main AOP settings class
        include 'org/example/ares/api/aop/java/JavaAOPTestCaseSettings.class'
        // Include the Messages localization class
        include 'org/example/ares/api/localization/Messages.class'
        // Include utility classes that might be referenced
        include 'org/example/ares/api/util/FileTools.class'
        include 'org/example/ares/api/util/FileTools$*.class'
        include 'org/example/ares/api/util/LruCache.class'
        include 'org/example/ares/api/util/LruCache$*.class'
        include "org/example/ares/api/architecture/java/archunit/**"
        include "org/example/ares/api/policy/policySubComponents/**"
        include "org/example/ares/api/architecture/java/**"
        include "org/example/ares/api/architecture/**"
        // Include synthetic inner classes for any of the above
        include '**/*$*.class'
    }
}

tasks.named('build') { dependsOn 'javaagentJar', 'xbootclasspathJar' }
tasks.named('testClasses') { dependsOn 'copyTestTxtIntoClasses' }
```

## Reject student classes in reserved packages

**This section is required.** Ares trusts a number of runtime identities **by name**, including its own `de.tum.cit.ase.ares.api` package and the platform namespaces. If a student can put a class into one of those packages, that class inherits the trust and every other check can be walked around. The build must therefore refuse to compile student output into a reserved package.

The [Enforcement Model](/contributor/subsystems/policy/enforcement-model) specifies this boundary and calls it a deployment prerequisite, not an optional Ares runtime feature. Ares ships the executable snippets, so copy them rather than writing your own:

They ship inside the Ares JAR under `de/tum/cit/ase/ares/api/configuration/reservedPackages/`,
and live in the repository at
[`src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages/`](https://github.com/ls1intum/Ares2/tree/main/src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages):

- `GradleReservedPackages.gradle`
- `MavenReservedPackages.xml`
- `ReservedPackagePrefixes.txt` (the machine-readable prefix list)

Two versions are pinned. `RESERVED_PACKAGE_PREFIX_VERSION = 1` is the prefix data. `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION = 2` is the build-side contract that enforces it. Your exercise and its CI must pin both.

### Gradle

Apply the shipped `GradleReservedPackages.gradle`, which registers `verifyAresReservedPackagesV2` over `sourceSets.main.output.classesDirs` and then attaches it in **two** places:

```gradle
tasks.named('check') { dependsOn tasks.named('verifyAresReservedPackagesV2') }
tasks.withType(Test).configureEach { dependsOn tasks.named('verifyAresReservedPackagesV2') }
```

Both are required, and the second is the one that is easy to get wrong. Gradle's Java plugin defines `check.dependsOn test`, **not** the reverse. A snippet that hangs the validation off `check` alone is therefore never executed by `gradlew test`, which is the command this manual gives you and the command a grading run invokes. That was the defect in boundary version 1: an exercise carrying it accepted student classes in reserved packages. If your exercise still contains a `verifyAresReservedPackagesV1` task, migrate it.

In a multi-project build, apply the snippet to **every** project that compiles student code: `tasks.withType(Test)` covers only the project it is applied to.

### About the forbidden package list

The list above is the versioned reserved-package boundary, and it is deliberately a superset of
the canonical Ares list. Besides the packages Ares trusts by name, it also stops student code
shadowing the test harness itself (JUnit, jqwik, AssertJ, Logback, Gradle).

Keep it aligned with `WalaPathClassification.RESERVED_PACKAGE_PREFIX_VERSION` (the prefix data)
and `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION` (the build-side contract), and do not disable it.

:::warning[Hook the check onto every `Test` task, not onto `check`]
Gradle defines `check.dependsOn test` and not the reverse, so a `check`-only hook never runs
for `gradlew test`.
:::

The canonical Maven and Gradle contract is documented in the
[enforcement model](/contributor/subsystems/policy/enforcement-model). The executable snippets ship inside
the Ares JAR and live in the repository at
[`src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages/`](https://github.com/ls1intum/Ares2/tree/main/src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages),
and complete working exercises are in
[`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples)
([Maven](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-maven),
[Gradle](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-gradle)).

### What this boundary does not defend against

The build descriptor and the command that invokes it are **trusted instructor configuration**. The failure message says "No bypass flag is supported", and that is a statement about the shipped snippets: they offer no opt-out of their own. It is not a claim that the check survives an adversary who controls the build. Anyone who can edit `build.gradle` or `pom.xml`, or pass `-x verifyAresReservedPackagesV2`, can remove the boundary outright.

The threat this addresses is student **code** that declares a reserved package, not student control over the build. Your exercise template and its CI must own the build files and the invocation, and must fail visibly if either is altered.

## Verify your setup

### The two controls that matter

A setup check is only worth running if it can fail for the right reason. The examples are therefore built around a **pair** of controls in the same domain:

- **Positive control:** supervised code reads `allowed.txt`, which the policy permits. This must succeed. If it fails, enforcement is too strict, or the policy does not say what you think it says.
- **Negative control:** supervised code reads `secret.txt`, which the policy does not permit. The test asserts that Ares rejects it. If it succeeds, enforcement is not active at all.

Two details make this a genuine test rather than a reassuring one:

1. **The forbidden read must happen in supervised code, not in the test.** A test class named in `theFollowingClassesAreTestClasses` is exempt from enforcement, so a read performed by the test itself is *supposed* to succeed. Put the read in the student-facing class and let the test assert the exception.
2. **The policy must permit one file in the domain, not zero.** This is the part that is easy to get wrong. Ares adds a static deny-all rule only while a domain has **no** allowance ([Enforcement Model](/contributor/subsystems/policy/enforcement-model)). Under a fully restrictive file policy, ArchUnit or WALA rejects the operation before any runtime mechanism is consulted, so the negative control passes even with `-javaagent` removed and the weaving switched off, and it proves nothing. Granting exactly one permitted file makes the runtime layer authoritative for that domain, and only then does the negative control actually exercise the agent or the woven aspects.

A correct run is therefore **green**, and contains an asserted rejection. It is not a failed build.

### What a green run does and does not prove

A minimal test with no `@Policy` annotation confirms only that the Ares classes are on the test classpath and that the JVM started with the configured arguments. It does **not** prove that the agent instrumented anything, and it is not necessarily enforcement-free either: see [Further Options](../further-options.md) for what does and does not happen without a policy.

To prove enforcement, use the paired controls from the two controls above, then break the setup deliberately and confirm each break is detected:

- Remove `-javaagent` from an exercise whose configuration ends in `_INSTRUMENTATION`. The negative control must now fail to reject.
- Remove the `aspect` dependency (Gradle) or `<aspectLibraries>` (Maven) from an exercise whose configuration ends in `_ASPECTJ`. Same expectation.
- Add a class in `de.tum.cit.ase.ares.api` to the student sources. The build must fail with the reserved-package diagnostic.

Each break must be made in the mode that depends **only** on the removed component. Removing `-javaagent` from an AspectJ exercise changes nothing, because the aspects were woven at compile time and still enforce the policy.

> **What happens without the agent?** Ares's **static analysis** (ArchUnit/WALA) still works, and **AspectJ enforcement** still works, because those aspects are woven at compile time. Only the **ByteBuddy instrumentation** path is inactive, since it relies on the agent to transform classes at load time. If you use an `INSTRUMENTATION` configuration, students could then bypass runtime restrictions, so always ensure the agent is loaded.

### Run the tests

Run the test task as usual, for example `./gradlew test`. During test execution, any policy
violation is intercepted and results in a `SecurityException`.

For the example above, attempting to write `secret.txt`, the test run fails with an exception
similar to:

```text
!security.advice.illegal.file.execution!
java.lang.SecurityException: !security.advice.illegal.file.execution!
    at org.example.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteractionForAction(JavaInstrumentationAdviceFileSystemToolbox.java:538)
    at org.example.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(JavaInstrumentationAdviceFileSystemToolbox.java:597)
    ...
```

:::note[No runnable Precompile example exists yet]
Both exercises under [`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples) are
Postcompile: they depend on Ares and activate it with `@Policy`. There is nothing to copy for
Precompile, so the steps above are the reference.
:::
