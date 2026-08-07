---
title: "Precompile Mode"
sidebar_position: 3
description: "Integrating precompile mode: defining a policy, running the precompile phase via Ares2UI or the main class, and configuring Gradle to enforce the result."
---

This page shows how precompile mode is integrated, using a concrete example.

Consider the following student implementation. The method attempts to write to a file
`secret.txt` inside `getName()`:

```java
public String getName() {
    try {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("secret.txt"));
        bufferedWriter.write("First Line");
        bufferedWriter.close();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    return name;
}
```

In a secure testing environment this behaviour must be blocked. Activating the protection for
a given test takes the steps below.

## 1. Define a `security-policy.yaml`

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

## 2. Running precompile mode via Ares2UI

[Ares2UI](https://github.com/ls1intum/Ares2UI) provides an interactive way to run the
precompile phase without invoking the command-line runner.

**Select the project directory.** Choose the root directory of the student project to be
processed. For the example above, that is `example_project/`.

**Select or create a security policy.** If a `security-policy.yaml` already exists it can be
selected and loaded. Alternatively, create a new policy with the built-in editor via the
`Create Policy` button.

**Execute the precompile phase.** Clicking `Create Files` runs the precompile phase and
generates the enforcement artefacts inside the selected project.

## 3. Running precompile mode via the main class

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

## 4. Resulting project structure

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

:::danger Do not edit the generated files
They are produced automatically by the precompile phase and must not be modified by hand.
:::

## 5. Configure Gradle and verify enforcement

After the precompile phase, the generated enforcement artefacts sit in the project's test
scope (for example under `src/test/java/`). To activate the instrumentation during test
execution, the project must build the required agent JARs and run tests with the corresponding
JVM arguments.

### 5.1 Update `build.gradle`

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

### 5.2 Run the tests

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
