---
title: "Gradle"
sidebar_position: 1
description: "Protecting a Gradle project with Ares 2 in Precompile mode, from policy file to enforced build."
---

:::tip[Simple Story]
This page sets up, step by step, an examination for which the head teacher prints the rules onto
the grading instructions beforehand. The printing leaves out one step. The head teacher has to
make the school's office check that the rule sheet on the desk is theirs.

[Who is who](/instructor/who-is-who)
:::

Precompile generates the enforcement artefacts before the exercise is built. The generator is run
once, from outside the project, and what it writes is then compiled by the exercise's own build
like any other source.

## The path, in order

1. **Define the policy**, below.
2. **Run the generator**, below, from the Ares UI or from the command line, once the exercise's
   tests are compiled.
3. **Check what was written into the project**, below.
4. **Update `build.gradle`**, below.
5. **Reject student classes in reserved packages**, below. Precompile does **not** generate this
   step; you install it by hand exactly as in Postcompile.
6. **Verify with both controls**, below.

:::warning[Supported configurations]
This page sets up the two `JAVA_USING_GRADLE_*_AND_INSTRUMENTATION` configurations. It gives no
build wiring for the `_ASPECTJ` ones: the aspects Precompile writes into `src/test/java` have to
be compiled with the AspectJ compiler and woven into the student classes, and nothing below does
that. An `_ASPECTJ` exercise built from this page does not run unprotected. Before the first test
starts, Ares checks every compiled student class for the woven aspects and, finding none, runs no
test at all.
:::

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
    regardingPackageImports:
      - importTheFollowingPackage: "java.nio.file"
      - importTheFollowingPackage: "java.util"
    regardingTimeouts: [ ]
```

This permits file system operations **only** on `something.txt`. All other paths, including
`secret.txt`, are not listed and are therefore fully denied by the sandbox. Any attempt by
student code to read, create, overwrite or delete `secret.txt` is intercepted and blocked.

Modify the sample code to operate on `something.txt` instead to see this from the other side;
those operations succeed, whereas accesses to `secret.txt` correctly trigger a security
exception.

`regardingPackageImports` lists every package the student code imports beyond `java.lang` and
`java.io`, which are always allowed. The sample reads files through `java.nio.file` and checks its
arguments with `java.util.Objects`, so both are listed. Leave one out and the generated import
check fails, which stops the whole test run, as the last section describes.

Once the policy is defined, the precompile phase can be run in one of two ways, described
below.

## Compile the tests before generating

The generator looks up every class in `theFollowingClassesAreTestClasses` in the exercise's
compiled test output, `build/classes/java/test` for Gradle, and stops with an error if one is
missing. So compile the exercise's own tests, without running them, before either way of
running the generator:

```bash
gradle testClasses
```

Regenerate after you change the tests, so the generated files list what the tests now
declare.

If the tests themselves use generated classes, they cannot compile before the first run. In
that case bootstrap once:

1. Generate with a temporary copy of the policy whose `theFollowingClassesAreTestClasses` is
   empty (`[ ]`).
2. Compile the tests.
3. Generate again with the real policy. This run has to succeed.
4. Compile the tests once more, so the final generated files are the ones built.

The empty list is only a scaffold for the first compilation. Do not hand out an exercise
generated from it.

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

## Run the generator from the command line

[`de.tum.cit.ase.ares.api.Main`](https://github.com/ls1intum/Ares2/tree/main/src/main/java/de/tum/cit/ase/ares/api/Main.java)
is the command-line entry point. It takes exactly two arguments, the policy file and the root
of the project to protect, and derives the `src/test/java` directory inside that project
itself. Nothing in the Ares 2 source is edited, and a run with any other number of arguments
stops with its usage message.

```bash
git clone https://github.com/ls1intum/Ares2.git
cd Ares2
mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=classpath.txt -DincludeScope=runtime
java -cp "target/classes:$(cat classpath.txt)" de.tum.cit.ase.ares.api.Main <path-to-your-security-policy.yaml> <path-to-the-project-root>
```

On Windows the classpath separator is `;` rather than `:`.

The project root has to carry the descriptor of the build tool the policy names, so a
`build.gradle` for any `JAVA_USING_GRADLE_*` configuration. Without it the run stops with
`The selected GRADLE layout has no build descriptor in <path>` and writes nothing.

Ares 2 then interprets the configuration, generates the corresponding enforcement rules and
writes them into `<path-to-the-project-root>/src/test/java`, completing the precompile
integration.

## What ends up in the project

After the precompile phase has run, whether via Ares2UI or the command-line runner, the
project contains the files the build compiles and runs for enforcement:

```text
example_project/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── org/example/
│   │           ├── Main.java
│   │           └── Penguin.java
│   └── test/
│       ├── java/
│       │   └── org/example/
│       │       ├── PenguinTest.java
│       │       ├── ares/api/        ← copied Ares classes, generated checks and method lists
│       │       └── META-INF/        ← the agent manifest, for _INSTRUMENTATION only
│       └── resources/
│           ├── META-INF/services/   ← registers the Ares guard with JUnit
│           ├── archunit.properties  ← the ArchUnit settings the generated checks rely on
│           └── org/example/ares/api/localization/   ← the Ares messages
├── build.gradle
├── PhobosCopyTool.sh                ← hands SpecificExercise.cfg to Phobos
├── SpecificExercise.cfg             ← the policy, for Phobos
├── secret.txt
├── something.txt
└── security-policy.yaml   ← example location
```

`src/test/java/org/example/ares/api/` holds the Ares classes the exercise needs, rewritten into
the exercise package, the generated architecture checks (`JavaArchunitTestCase` or
`JavaWalaTestCase`), the generated settings the runtime checks read, and the `.txt` method lists
those checks load. The file below `src/test/resources/META-INF/services/` makes JUnit call the
copied `PrecompileGuard` before the first test; an existing file of the same name keeps its lines.
`src/test/resources/archunit.properties` holds the ArchUnit settings the generated checks rely on.
The build puts it at the root of the test class path, where ArchUnit looks for it. An existing file
of that name keeps its lines and its values, and gains only the settings it does not name.

:::danger[Do not edit the generated files]
They are produced automatically by the precompile phase and must not be modified by hand.
:::

## Update `build.gradle`

The configuration below assumes the student project uses the Java package `org.example`, as in
the example structure above. If a different base package is used, every occurrence of
`org/example` must be adapted accordingly. It needs no Ares dependency: the agent is built from
the copied classes, and the Java Virtual Machine (JVM) arguments are the ones the
[Postcompile page](../postcompile/gradle.md) uses. With a T. J. Watson Libraries for Analysis
(WALA) configuration the same file
works; the WALA lines in `dependencies` are then required rather than harmless.

```groovy
plugins {
    id 'java'
    id 'application'
}

group = 'de.tum.cit.aet'
version = '1.0-SNAPSHOT'

ext {
    ver = [
            junit      : '6.1.3',
            bytebuddy  : '1.18.17',
            aspectj    : '1.9.27',
            archunit   : '1.5.1',
            wala       : '1.9.0',
            jackson    : '2.23.0',
            guava      : '33.8.0-jre',
            jsr305     : '3.0.2',
            opencsv    : '5.12.0',
            apiguardian: '1.1.2'
    ]
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // What the copied Ares classes compile against.
    testImplementation "org.junit.jupiter:junit-jupiter:${ver.junit}"
    testImplementation "org.junit.platform:junit-platform-launcher:${ver.junit}"
    testImplementation "net.bytebuddy:byte-buddy:${ver.bytebuddy}"
    testImplementation "com.tngtech.archunit:archunit:${ver.archunit}"
    testImplementation "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${ver.jackson}"
    testImplementation "com.google.guava:guava:${ver.guava}"
    testImplementation "com.google.code.findbugs:jsr305:${ver.jsr305}"
    testImplementation "com.opencsv:opencsv:${ver.opencsv}"
    testImplementation "org.apiguardian:apiguardian-api:${ver.apiguardian}"
    testImplementation "org.aspectj:aspectjrt:${ver.aspectj}"
    // Needed only for a JAVA_USING_GRADLE_WALA_* configuration: the copied WALA
    // classes name these libraries, so the test sources do not compile without them.
    testImplementation "com.ibm.wala:com.ibm.wala.core:${ver.wala}"
    testImplementation "org.assertj:assertj-core:3.27.7"
    testImplementation "org.hamcrest:hamcrest:3.0"
    testImplementation "junit:junit:4.13.2"
    testImplementation "org.junit.jupiter:junit-jupiter-engine:${ver.junit}"
    testImplementation "org.junit.platform:junit-platform-testkit:${ver.junit}"
    testImplementation "org.mockito:mockito-core:5.24.0"
    testImplementation "net.bytebuddy:byte-buddy-agent:${ver.bytebuddy}"
}

application {
    mainClass = 'org.example.Main'
}

// The generated checks read their method lists from next to their own classes,
// so the .txt files Precompile wrote beside the sources are copied there.
tasks.register('copyAresTemplates', Copy) {
    dependsOn 'compileTestJava'
    from('src/test/java') {
        include '**/*.txt'
    }
    into sourceSets.test.java.destinationDirectory
}
tasks.named('testClasses') { dependsOn 'copyAresTemplates' }

// The agent is the copied Ares API itself, with the manifest Precompile wrote.
tasks.register('aresAgentJar', Jar) {
    archiveClassifier = 'ares-agent'
    dependsOn 'copyAresTemplates'
    from(sourceSets.test.output) {
        include 'org/example/ares/api/**'
    }
    manifest {
        from(file('src/test/java/org/example/META-INF/MANIFEST.MF'))
    }
}

tasks.named('test') {
    useJUnitPlatform()
    def agentJar = tasks.named('aresAgentJar').flatMap { it.archiveFile }
    inputs.file(agentJar)
    jvmArgumentProviders.add({
        [
                "-javaagent:${agentJar.get().asFile.absolutePath}".toString(),
                '--add-exports', 'java.base/java.lang=ALL-UNNAMED',
                '--add-exports', 'java.base/jdk.internal.misc=ALL-UNNAMED',
                '--add-opens', 'java.base/java.io=ALL-UNNAMED',
                '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
                '--add-opens', 'java.base/java.lang.reflect=ALL-UNNAMED',
                '--add-opens', 'java.base/java.net=ALL-UNNAMED',
                '--add-opens', 'java.base/java.nio=ALL-UNNAMED',
                '--add-opens', 'java.base/java.nio.channels=ALL-UNNAMED',
                '--add-opens', 'java.base/java.util=ALL-UNNAMED',
                '--add-opens', 'java.base/java.util.concurrent=ALL-UNNAMED',
                '--add-opens', 'java.base/java.util.concurrent.atomic=ALL-UNNAMED',
                '--add-opens', 'java.base/java.util.concurrent.locks=ALL-UNNAMED',
                '--add-opens', 'java.base/jdk.internal.misc=ALL-UNNAMED',
                '--add-opens', 'java.base/sun.net.www.protocol.http=ALL-UNNAMED',
                '--add-opens', 'java.base/sun.net.www.protocol.https=ALL-UNNAMED',
                '--add-opens', 'java.base/sun.nio.ch=ALL-UNNAMED',
                '--add-opens', 'jdk.unsupported/sun.misc=ALL-UNNAMED'
        ]
    } as CommandLineArgumentProvider)
}
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

Two versions are pinned. `RESERVED_PACKAGE_PREFIX_VERSION = 3` is the prefix data. `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION = 3` is the build-side contract that enforces it. Your exercise and its continuous integration (CI) must pin both.

### Gradle

Apply the shipped `GradleReservedPackages.gradle`, which registers `verifyAresReservedPackagesV3` over the whole `sourceSets.main.output`, resources included, and then attaches it in **two** places:

```gradle
tasks.named('check') { dependsOn tasks.named('verifyAresReservedPackagesV3') }
tasks.withType(Test).configureEach { dependsOn tasks.named('verifyAresReservedPackagesV3') }
```

Both are required, and the second is the one that is easy to get wrong. Gradle's Java plugin defines `check.dependsOn test`, **not** the reverse. A snippet that hangs the validation off `check` alone is therefore never executed by `gradlew test`, which is the command this manual gives you and the command a grading run invokes. That was the defect in boundary version 1: an exercise carrying it accepted student classes in reserved packages. If your exercise still contains a `verifyAresReservedPackagesV1` task, migrate it.

In a multi-project build, apply the snippet to **every** project that compiles student code: `tasks.withType(Test)` covers only the project it is applied to.

### About the forbidden package list

The shipped snippets are the versioned reserved-package boundary. They reject exactly the
package prefixes listed in `ReservedPackagePrefixes.txt`, which student code must not declare.
Boundary version 3 further rejects every file under `META-INF/services/` and
`mockito-extensions/`, and the JUnit, ArchUnit, Logback and JaCoCo configuration files at the
root of the student's output, such as `junit-platform.properties`, `archunit.properties` and
`logback.xml`. It rejects a `module-info.class` in any folder too, because its `provides`
clauses make Java run a student class. It compares names ignoring case. Those frameworks read such a
file from the test class path before Ares is active, so it could run a student class
unsupervised or hide ArchUnit findings. Keep your own files of this kind in `src/test/resources`, which the check does not scan.

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

In Precompile the copied Ares classes live in `org.example.ares.api`, inside the student's own
package, so the shipped snippets do not cover that name. The guard covers it instead: a compiled
student class there stops the run before the first test, with a message naming the class.

### What this boundary does not defend against

The build descriptor and the command that invokes it are **trusted instructor configuration**. The failure message says "No bypass flag is supported", and that is a statement about the shipped snippets: they offer no opt-out of their own. It is not a claim that the check survives an adversary who controls the build. Anyone who can edit `build.gradle` or `pom.xml`, or pass `-x verifyAresReservedPackagesV3`, can remove the boundary outright.

The threat this addresses is student **code** that declares a reserved package, not student control over the build. Your exercise template and its CI must own the build files and the invocation, and must fail visibly if either is altered.

## Verify your setup

### The two controls that matter

A setup check is only worth running if it can fail for the right reason. The examples are therefore built around a **pair** of controls in the same domain:

- **Positive control:** supervised code reads `something.txt`, which the policy permits. This must succeed. If it fails, enforcement is too strict, or the policy does not say what you think it says.
- **Negative control:** supervised code reads `secret.txt`, which the policy does not permit. The test asserts that Ares rejects it. If it succeeds, enforcement is not active at all.

Two details make this a genuine test rather than a reassuring one:

1. **The forbidden read must happen in supervised code, not in the test.** A test class named in `theFollowingClassesAreTestClasses` is exempt from enforcement, so a read performed by the test itself is *supposed* to succeed. Put the read in the student-facing class and let the test assert the exception.
2. **The policy must permit one file in the domain, not zero.** This is the part that is easy to get wrong. Ares adds a static deny-all rule only while a domain has **no** allowance ([Enforcement Model](/contributor/subsystems/policy/enforcement-model)). Under a fully restrictive file policy, ArchUnit or WALA rejects the operation before any runtime mechanism is consulted, so the negative control passes even with `-javaagent` removed and the weaving switched off, and it proves nothing. Granting exactly one permitted file makes the runtime layer authoritative for that domain, and only then does the negative control exercise the agent or the woven aspects.

A correct run is therefore **green**, and contains an asserted rejection. It is not a failed build.

The sample exercise on this page holds both controls. `Penguin.java` is the supervised class:

```java
package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class Penguin {
    private final String name;

    public Penguin(String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public String getName() {
        return name;
    }

    public String readPermittedFile() throws Exception {
        return Files.readString(Path.of("something.txt")).trim();
    }

    public String readForbiddenFile() throws Exception {
        return Files.readString(Path.of("secret.txt")).trim();
    }
}
```

`PenguinTest.java` asserts both outcomes:

```java
package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PenguinTest {

    @Test
    void name() {
        assertEquals("Julian", new Penguin("Julian").getName());
    }

    @Test
    void readsThePermittedFile() throws Exception {
        assertEquals("something", new Penguin("Julian").readPermittedFile());
    }

    @Test
    void rejectsTheForbiddenFile() {
        SecurityException violation = assertThrows(SecurityException.class,
                () -> new Penguin("Julian").readForbiddenFile());
        assertTrue(violation.getMessage().contains("secret.txt"), violation.getMessage());
    }
}
```

`something.txt` holds the single word `something`.

### What a green run does and does not prove

A minimal test with no `@Policy` annotation confirms only that the Ares classes are on the test classpath and that the JVM started with the configured arguments. It does **not** prove that the agent instrumented anything, and it is not necessarily enforcement-free either: see [Further Options](../further-options.md) for what does and does not happen without a policy.

Prove enforcement with the paired controls from the two controls above, then break the setup deliberately and confirm each break is detected:

- Remove the `-javaagent` line from `build.gradle`. No test runs now: the run stops before the first test with `The policy asks for instrumentation, but the Ares instrumentation agent is not attached to this JVM.`
- Add a class to `src/main/java/org/example/ares/api/`, the package of the copied Ares classes. No test runs: the run stops with a message naming that class and its reserved package.
- Add a class in `de.tum.cit.ase.ares.api` to the student sources. The build must fail with the reserved-package diagnostic.
- Call `Class.forName` from `Penguin`. No test runs: the generated reflection or class-loading check fails before the first test.

### Run the tests

Run the test task as usual, for example `./gradlew test`. Before the first test starts, the
copied `PrecompileGuard` checks that the agent is attached and runs every generated architecture
check once. If any of that fails, no test runs at all and the build stops with a message such as:

```text
Ares Security Error (Reason: Student-Code; Stage: Execution): Ares runs no test of this exercise,
because the generated check reflectionShouldNotBeAccessed failed before the first test started: …
```

This is how Precompile matches Postcompile, where a failed architecture check fails every test
before student code runs. Without it, a submission that breaks a static rule would fail one
generated test and still pass all the others.

During test execution, any runtime policy violation is intercepted and results in a
`SecurityException`. For the negative control above, the forbidden read of `secret.txt` in the
supervised class raises that exception, the test asserts it, and the run therefore stays
**green**. The exception the assertion captures looks similar to this:

```text
java.lang.SecurityException: Ares Security Error (Reason: Student-Code; Stage: Execution): org.example.Penguin.readForbiddenFile tried to illegally read the file /…/example_project/secret.txt via java.nio.file.Files.readString(java.nio.file.Path) (called by …), but was blocked by Ares, because no rule in the policy allows this file.
    at org.example.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteractionForAction(JavaInstrumentationAdviceFileSystemToolbox.java:…)
    at org.example.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(JavaInstrumentationAdviceFileSystemToolbox.java:…)
    at java.base/java.nio.file.Files.readString(Files.java:…)
    at org.example.Penguin.readForbiddenFile(Penguin.java:…)
    ...
```

A red run here means the exception reached the test runner unasserted, which is a test that
does not yet assert its negative control rather than a setup that does not enforce. A green
run with no exception at all is the outcome to worry about: it says the read was permitted.

:::note[No runnable Precompile example exists yet]
Both exercises under [`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples) are
Postcompile: they depend on Ares, and each test carries an Ares test annotation with a
`@Policy` beside it. There is nothing to copy for
Precompile, so the steps above are the reference.
:::
