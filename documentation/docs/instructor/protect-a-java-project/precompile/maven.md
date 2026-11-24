---
title: "Maven"
sidebar_position: 2
description: "Protecting a Maven project with Ares 2 in Precompile mode, from policy file to enforced build."
---

:::tip[Simple Story]
This page sets up the same examination as the [Gradle page](./gradle.md), with the rules printed
beforehand, for a school whose office keeps its records in Maven.

[Who is who](/instructor/who-is-who)
:::

The generator steps are the same for both build tools, so this page refers to the Gradle page for
them and gives the Maven side in full.

:::warning[Supported configurations]
This page sets up the two `JAVA_USING_MAVEN_*_AND_INSTRUMENTATION` configurations. It gives no
build wiring for the `_ASPECTJ` ones, for the reason the Gradle page gives. An `_ASPECTJ` exercise
built from this page runs no test: before the first one, Ares finds the student classes without
the woven aspects and stops the run.
:::

## The path, in order

1. **Define the policy** as on the [Gradle page](./gradle.md#define-the-policy-file), with
   `theFollowingProgrammingLanguageConfigurationIsUsed` set to
   `JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION` or `JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION`.
2. **Compile the tests** with `mvn test-compile`. The generator looks up every class in
   `theFollowingClassesAreTestClasses` in `target/test-classes` and stops with an error if one is
   missing.
3. **Run the generator** from the [Ares UI](./gradle.md#run-the-generator-from-the-ares-ui) or the
   [command line](./gradle.md#run-the-generator-from-the-command-line). The project root has to
   carry a `pom.xml`, or the run stops with `The selected MAVEN layout has no build descriptor in
   <path>` and writes nothing.
4. **Check what was written**: the same files as on the
   [Gradle page](./gradle.md#what-ends-up-in-the-project), next to `pom.xml` instead of
   `build.gradle`.
5. **Write `pom.xml`**, below.
6. **Reject student classes in reserved packages**, below.
7. **Verify with both controls** as on the [Gradle page](./gradle.md#verify-your-setup), running
   `mvn test`.

## Write `pom.xml`

The exercise needs no Ares dependency. The configuration below assumes the student project uses
the Java package `org.example`; for another package, change `org/example` in the agent includes
and the manifest path accordingly.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>ares-precompile-exercise</artifactId>
    <version>1.0.0</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <junit.version>6.1.3</junit.version>
        <bytebuddy.version>1.18.17</bytebuddy.version>
    </properties>

    <dependencies>
        <!-- What the copied Ares classes compile against. -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-launcher</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>net.bytebuddy</groupId>
            <artifactId>byte-buddy</artifactId>
            <version>${bytebuddy.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
            <artifactId>archunit</artifactId>
            <version>1.5.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-yaml</artifactId>
            <version>2.23.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>33.8.0-jre</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.google.code.findbugs</groupId>
            <artifactId>jsr305</artifactId>
            <version>3.0.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.opencsv</groupId>
            <artifactId>opencsv</artifactId>
            <version>5.12.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apiguardian</groupId>
            <artifactId>apiguardian-api</artifactId>
            <version>1.1.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>1.9.27</version>
            <scope>test</scope>
        </dependency>
        <!-- Needed only for a JAVA_USING_MAVEN_WALA_* configuration: the copied WALA
             classes name these libraries, so the test sources do not compile without them. -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.ibm.wala</groupId>
            <artifactId>com.ibm.wala.core</artifactId>
            <version>1.9.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.27.7</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.hamcrest</groupId>
            <artifactId>hamcrest</artifactId>
            <version>3.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-testkit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.24.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>net.bytebuddy</groupId>
            <artifactId>byte-buddy-agent</artifactId>
            <version>${bytebuddy.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <!-- The generated checks read their method lists from next to their own
             classes, so the .txt files Precompile wrote beside the sources are copied
             into target/test-classes as well. -->
        <testResources>
            <testResource>
                <directory>src/test/resources</directory>
            </testResource>
            <testResource>
                <directory>src/test/java</directory>
                <includes>
                    <include>**/*.txt</include>
                </includes>
            </testResource>
        </testResources>
        <plugins>
            <!-- The agent is the copied Ares API itself, with the manifest Precompile wrote.
                 It is packed after the tests compile and before they run. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.5.0</version>
                <executions>
                    <execution>
                        <id>ares-agent</id>
                        <phase>process-test-classes</phase>
                        <goals>
                            <goal>test-jar</goal>
                        </goals>
                        <configuration>
                            <classifier>ares-agent</classifier>
                            <includes>
                                <include>org/example/ares/api/**</include>
                            </includes>
                            <archive>
                                <manifestFile>src/test/java/org/example/META-INF/MANIFEST.MF</manifestFile>
                            </archive>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <!-- Attaches the agent and opens the JDK internals Ares introspects, as on
                 the Postcompile page. The agent path is quoted so a project directory
                 containing a space still works. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.6.1</version>
                <configuration>
                    <argLine>
                        "-javaagent:${project.build.directory}/${project.build.finalName}-ares-agent.jar"
                        --add-exports java.base/java.lang=ALL-UNNAMED
                        --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
                        --add-opens java.base/java.io=ALL-UNNAMED
                        --add-opens java.base/java.lang=ALL-UNNAMED
                        --add-opens java.base/java.lang.reflect=ALL-UNNAMED
                        --add-opens java.base/java.net=ALL-UNNAMED
                        --add-opens java.base/java.nio=ALL-UNNAMED
                        --add-opens java.base/java.nio.channels=ALL-UNNAMED
                        --add-opens java.base/java.util=ALL-UNNAMED
                        --add-opens java.base/java.util.concurrent=ALL-UNNAMED
                        --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED
                        --add-opens java.base/java.util.concurrent.locks=ALL-UNNAMED
                        --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
                        --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED
                        --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED
                        --add-opens java.base/sun.nio.ch=ALL-UNNAMED
                        --add-opens jdk.unsupported/sun.misc=ALL-UNNAMED
                    </argLine>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Three parts differ from a Postcompile `pom.xml`:

- The `testResources` entry copies the `.txt` method lists from `src/test/java` into
  `target/test-classes`. Without it every generated check fails with `Failed to read content from
  source file.`, and the guard stops the run.
- The agent is packed from the copied classes in `process-test-classes`, after the tests compile
  and before Surefire starts them. Postcompile downloads the Ares agent instead, and that agent
  reads the settings of the Ares JAR, not the ones Precompile generated.
- There is no `aspectj-maven-plugin`, because this page sets up instrumentation only.

## Reject student classes in reserved packages

**This section is required**, exactly as in Postcompile. Copy the shipped
`MavenReservedPackages.xml` into the `<plugins>` of `pom.xml`, as the
[Maven Postcompile page](../postcompile/maven.md#reject-student-classes-in-reserved-packages)
describes. Precompile does not generate it.

The copied Ares classes live in `org.example.ares.api`, inside the student's own package, which
the shipped snippet does not cover. The Ares guard covers it: a compiled student class there stops
the run before the first test.

## Verify your setup

Use the sample exercise and the paired controls from the
[Gradle page](./gradle.md#verify-your-setup) and run `mvn test`. With the files above the run is
green: the 13 generated checks pass, the permitted read succeeds and the forbidden read is
rejected. A failed generated check, a missing agent or a student class in `org.example.ares.api`
stops the run before the first test, and Surefire reports `There was an error in the forked
process` followed by the Ares message.
