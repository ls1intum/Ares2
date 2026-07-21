# Ares-Protected Student Exercise Manual

> **Audience:** IT-Education experts with no security background.
> **Scope:** The `build.gradle` and `pom.xml` files.
> **Ares Version:** 2.1.0

> **Note:** This guide is a **setup guide**. It covers adding the Ares dependency, attaching the agent, and configuring the build tool so that Ares can run. For writing security policies that control what student code can do, see the [Security Policy Manual](policy/SecurityPolicyManual.md).

**Related documentation:**
- [Security Policy Manual](policy/SecurityPolicyManual.md), which explains how to write a security policy YAML file
- [Security Policy Reader and Director Manual](policy/SecurityPolicyReaderAndDirectorManual.md), which describes the internal processing pipeline

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Purpose: What Problem Does This Solve?](#2-purpose-what-problem-does-this-solve)
3. [Add Ares dependencies and agent setup](#3-add-ares-dependencies-and-agent-setup)
   - [3.1 Gradle (recommended)](#31-gradle-recommended)
     - [3.1.1 Configure repository lookup](#311-configure-repository-lookup)
     - [3.1.2 Configure Ares agent configuration](#312-configure-ares-agent-configuration)
     - [3.1.3 Add Ares dependencies](#313-add-ares-dependencies)
     - [3.1.4 Attach agent to test execution](#314-attach-agent-to-test-execution)
     - [3.1.5 How compile-time weaving works](#315-how-compile-time-weaving-works)
   - [3.2 Maven (alternative)](#32-maven-alternative)
     - [3.2.1 Configure repository lookup](#321-configure-repository-lookup)
     - [3.2.2 Add Ares dependency](#322-add-ares-dependency)
     - [3.2.3 Attach agent via maven-surefire-plugin](#323-attach-agent-via-maven-surefire-plugin)
     - [3.2.4 Configure AspectJ compile-time weaving](#324-configure-aspectj-compile-time-weaving)
4. [Verify your setup](#4-verify-your-setup)
   - [4.1 Step 1: Confirm Ares is on the classpath](#41-step-1-confirm-ares-is-on-the-classpath)
   - [4.2 Step 2: Prove that enforcement actually happens](#42-step-2-prove-that-enforcement-actually-happens)
5. [Upgrading from Ares 1 to Ares 2.1.0](#5-upgrading-from-ares-1-to-ares-210)
   - [5.1 Replace the dependency](#51-replace-the-dependency)
   - [5.2 Rename the package](#52-rename-the-package)
   - [5.3 Map the annotations](#53-map-the-annotations)
   - [5.4 Add `@Policy` to every test class](#54-add-policy-to-every-test-class)
6. [Next steps](#6-next-steps)
7. [Troubleshooting](#7-troubleshooting)
8. [Glossary](#8-glossary)

---

## 1. Prerequisites

- **Java 17** or later
- **Gradle** in a version compatible with the chosen freefair AspectJ plugin (the freefair 9.x line used in this guide requires Gradle 9; older Gradle versions need an older freefair line) or **Maven 3.8+**
- **JUnit 5** (Jupiter) for test execution

---

## 2. Purpose: What Problem Does This Solve?

Your Java project needs to run security tests that verify student submissions don't perform dangerous operations. To enable these tests, your `build.gradle` or `pom.xml` must:

1. Include the Ares library as a test dependency
2. Download and attach the Ares agent JAR to the JVM at test startup (critical for runtime enforcement)
3. Grant the agent access to Java internals via specific JVM flags (required for bytecode instrumentation)

This manual covers the configuration steps for both Gradle and Maven.

---

## 3. Add Ares dependencies and agent setup

Three steps are **required** regardless of build tool: (1) configure the repository so Gradle or Maven can download Ares; (2) add the Ares library as a dependency; and (3) attach the Ares agent to the test JVM. Blockquoted tips (marked `>`) throughout this section describe optional configuration that can be skipped on a first setup.

### 3.1 Gradle (recommended)

First, add the AspectJ compiler plugin:

```gradle
plugins {
    id 'io.freefair.aspectj.post-compile-weaving' version '9.2.0'
}
```

This plugin runs the AspectJ compiler (`ajc`) during your build to weave security aspects into bytecode. The plugin also provides an `aspect` dependency configuration; the Ares JAR must be added to it (see [Section 3.1.3](#313-add-ares-dependencies)) so that `ajc` treats the aspects shipped inside the Ares JAR as an aspect library and weaves them into your bytecode.

#### 3.1.1 Configure repository lookup

Configure Maven repository for downloading dependencies:

```gradle
repositories {
    mavenCentral()
}
```

**Explanation:**
- `mavenCentral()`: Retrieves Ares and other dependencies from Maven Central Repository for publicly available releases

> **Note:** If your `build.gradle` already contains a `repositories` block, add `mavenCentral()` to that existing block instead of creating a new one.

#### 3.1.2 Configure Ares agent configuration

Create a separate Gradle configuration for the Ares agent dependency:

```gradle
configurations {
    aresAgent
}
```

**Explanation:** This creates a new Gradle configuration named `aresAgent`. A Gradle configuration is a named bucket of dependencies that can be resolved independently; by creating a dedicated one for the agent, the agent JAR is downloaded and resolved separately from the compile and test classpaths, and it lets us reference the agent JAR by its exact file path in the `jvmArgs` of the `test` task (see [Section 3.1.4](#314-attach-agent-to-test-execution)). The agent JAR itself contains only Ares's own classes plus the `Premain-Class` manifest entry — it does not bundle Byte Buddy or any other dependency. Byte Buddy still reaches the instrumented JVM because `-javaagent` attaches to the *same* JVM that runs your tests, and that JVM's classpath already carries Byte Buddy transitively via the `testImplementation "de.tum.cit.ase:ares"` dependency declared in [Section 3.1.3](#313-add-ares-dependencies).

> **Note:** If your `build.gradle` already contains a `configurations` block, add the `aresAgent` configuration to that existing block instead of creating a new one.

#### 3.1.3 Add Ares dependencies

Add the Ares library to both the agent configuration and test implementation, as well as the AspectJ runtime library:

```gradle
dependencies {
    aresAgent "de.tum.cit.ase:ares:2.1.0:agent"
    aresAgent "org.aspectj:aspectjrt:1.9.25.1"
    testImplementation "de.tum.cit.ase:ares:2.1.0"
    aspect "de.tum.cit.ase:ares:2.1.0"
    implementation "org.aspectj:aspectjrt:1.9.25.1"
}
```

> **Tip (Gradle version catalog):** If your project uses a Gradle version catalog file (for example `gradle/<versions-catalog>.toml`), you can declare the version centrally:
>
> ```toml
> [versions]
> ares = "2.1.0"
> aspectjrt = "1.9.25.1"
> [libraries]
> ares = { module = "de.tum.cit.ase:ares", version.ref = "ares" }
> aspectjrt = { module = "org.aspectj:aspectjrt", version.ref = "aspectjrt" }
> ```
>
> Then reference `libs.ares` and `libs.aspectjrt` in `build.gradle`. Note that Gradle version catalogs do not natively support Maven classifiers, so the `aresAgent` dependency with the `:agent` classifier must remain as a direct dependency string in `build.gradle`.

**Explanation:**
- `aresAgent "de.tum.cit.ase:ares:..."`: Downloads the Ares **agent** JAR (with classifier `agent`) into the custom `aresAgent` configuration. This JAR contains only Ares's own instrumentation classes plus the correct `Premain-Class` manifest entry — it does **not** bundle Byte Buddy or any other dependency (Maven's shade-plugin `<artifactSet>` for this build only includes `de.tum.cit.ase:ares`). Byte Buddy is available to the instrumented JVM anyway, via the `testImplementation` dependency below, since `-javaagent` attaches to that same JVM rather than a separate one.
- `aresAgent 'org.aspectj:aspectjrt:...'`: Also adds the AspectJ runtime JAR to the `aresAgent` configuration so that it can be resolved in [Section 3.1.4](#314-attach-agent-to-test-execution) via `filter { it.name.contains('aspectjrt') }` for the `-Xbootclasspath/a:` JVM argument.
- `testImplementation`: Makes Ares classes available on the test classpath so your test code can use `@Policy`, `@Public`, and other Ares annotations. We use `testImplementation` instead of `implementation` because Ares is only needed during testing, not in the production code of the exercise. Using `implementation` would unnecessarily add Ares to the main classpath and the final artefact, which could interfere with student code and violates the principle of minimal dependency scope.
- `aspect "de.tum.cit.ase:ares:..."`: Registers the Ares JAR as an **aspect library** for the freefair AspectJ plugin. This is what makes `ajc` weave the binary aspects shipped inside the Ares JAR into your bytecode; a plain `testImplementation` dependency alone is not enough, because `ajc` only weaves aspects that are on the aspect path (see [Section 3.1.5](#315-how-compile-time-weaving-works)).
- `implementation 'org.aspectj:aspectjrt:1.9.25.1'`: The AspectJ runtime library providing classes (e.g., `org.aspectj.lang.JoinPoint`) that woven bytecode references. This must be available both at compile time (for the AspectJ compiler to resolve) and at runtime (on the bootstrap classpath, configured in [Section 3.1.4](#314-attach-agent-to-test-execution)). We use `implementation` instead of `testImplementation` because the AspectJ compiler weaves main classes during the `compileJava` phase — at that point, only the main compile classpath is visible, so `aspectjrt` must be on it for `ajc` to resolve the runtime types it injects.

> **Note:** If your `build.gradle` already contains a `dependencies` block, add these dependencies to that existing block instead of creating a new one.

#### 3.1.4 Attach agent to test execution

Configure the test task to load the agent when running tests:

```gradle
test {
    useJUnitPlatform()
    jvmArgs += [
        "-javaagent:${configurations.aresAgent.filter { it.name.startsWith('ares-') }.singleFile.absolutePath}",
        "-Xbootclasspath/a:${configurations.aresAgent.filter { it.name.contains('aspectjrt') }.singleFile.absolutePath}",
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
}
```

**Explanation:**
- `useJUnitPlatform()`: Enables JUnit 5 (Jupiter) test discovery
- `-javaagent:...`: Loads the Ares agent before any user code runs, which is critical for bytecode instrumentation to work
- `-Xbootclasspath/a:...`: Appends the AspectJ **runtime** JAR (`aspectjrt`) to the bootstrap classpath so that woven bytecode can resolve AspectJ runtime types at the bootstrap class-loader level. The `filter { it.name.contains('aspectjrt') }` dynamically resolves it from the `aresAgent` configuration (where it was explicitly added in [Section 3.1.3](#313-add-ares-dependencies)).
- **JVM Module Access Flags** (all listed packages need to be opened for Ares to introspect intercepted JDK objects and instrument bytecode; the list mirrors the `jvm.module.access.args` property in the Ares `pom.xml`):
  - `--add-exports java.base/java.lang`: Makes the public `java.lang` API accessible to the unnamed module (test classpath)
  - `--add-exports java.base/jdk.internal.misc`: Makes `jdk.internal.misc.Unsafe` accessible to the unnamed module for direct API calls
  - `--add-opens java.base/java.lang`: Allows reflective access to private fields and methods in `java.lang` (e.g., `Class.declaredFields`)
  - `--add-opens java.base/java.lang.reflect`: Allows reflective access to private members of the reflection API itself (e.g., `Field.setAccessible`, `Method.setAccessible`)
  - `--add-opens java.base/jdk.internal.misc`: Allows reflective access to private `Unsafe` fields in `jdk.internal.misc` (e.g., `theUnsafe`, read by the thread-system advice: `JavaInstrumentationAdviceThreadSystemToolbox` / `JavaAspectJThreadSystemAdviceDefinitions`)
  - `--add-opens jdk.unsupported/sun.misc`: Allows reflective access to the private `sun.misc.Unsafe` instance (legacy `Unsafe` access path; `sun.misc` lives in the `jdk.unsupported` module, not in `java.base`)
  - `--add-opens java.base/java.io`, `java.net`, `java.nio`, `java.nio.channels`, `sun.net.www.protocol.http`, `sun.net.www.protocol.https`, `sun.nio.ch`: Allow the Ares advice to reflectively read fields of intercepted file, network, and channel objects (e.g., `Socket.delegate`)
  - `--add-opens java.base/java.util`, `java.util.concurrent`, `java.util.concurrent.atomic`, `java.util.concurrent.locks`: Allow the Ares advice to reflectively read fields of intercepted collection and concurrency objects (e.g., `ThreadPoolExecutor.ctl`)

> **Note:** If your `build.gradle` already contains a `test` block, add `useJUnitPlatform()` and the `jvmArgs +=` to that existing block instead of creating a new one.

#### 3.1.5 How compile-time weaving works

The build automatically runs the AspectJ compiler (`ajc`) in the following way:

1. **After compilation:** The `io.freefair.aspectj.post-compile-weaving` plugin runs `ajc` **after** `javac` finishes compiling your source files to bytecode.
2. **Aspect discovery:** `ajc` only weaves binary aspects from JARs that are placed on its **aspect path**; it does not pick them up from the ordinary compile classpath. The `aspect "de.tum.cit.ase:ares:..."` dependency (declared in [Section 3.1.3](#313-add-ares-dependencies)) puts the Ares JAR on the plugin's aspect path, so `ajc` uses the compiled aspects shipped inside it.
3. **Weaving:** `ajc` reads the compiled `.class` files in the project's output directory and weaves in any code whose execution matches the pointcut expressions defined in the discovered aspects.
4. **Runtime references:** The woven bytecode references AspectJ runtime classes (e.g., `org.aspectj.lang.JoinPoint`), which are supplied by the `aspectjrt` library on the bootstrap classpath (configured in [Section 3.1.4](#314-attach-agent-to-test-execution)).

**Without the plugin, no weaving occurs** and the `-Xbootclasspath/a:` flag has no effect.

---

### 3.2 Maven (alternative)

#### 3.2.1 Configure repository lookup

Configure Maven repository for downloading dependencies:

```xml
<repositories>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>
```

**Explanation:**
- `central`: Retrieves Ares and other dependencies from Maven Central Repository for publicly available releases

> **Note:** If your `pom.xml` already contains a `<repositories>` section, add the `<repository>` to that existing section instead of creating a new one.

#### 3.2.2 Add Ares dependency

Include Ares in your test dependencies, as well as the AspectJ runtime library:

```xml
<dependency>
    <groupId>de.tum.cit.ase</groupId>
    <artifactId>ares</artifactId>
    <version>2.1.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>1.9.25.1</version>
</dependency>
```

**Explanation:**
- Adds Ares to the test classpath for compilation and testing
- The `<scope>test</scope>` ensures Ares is only available during testing, not in production
- `aspectjrt` is the AspectJ runtime library providing classes (e.g., `org.aspectj.lang.JoinPoint`) that woven bytecode references. This must be available both at compile time and at runtime (on the bootstrap classpath, configured in [Section 3.2.3](#323-attach-agent-via-maven-surefire-plugin)). It deliberately has no `<scope>test</scope>` because the AspectJ compiler weaves main classes during the `process-classes` phase — at that point, only the compile-scope classpath is visible, so `aspectjrt` must be on it for `ajc` to resolve the runtime types it injects.
- Maven downloads the JARs automatically (no manual JAR building required)

> **Note:** If your `pom.xml` already contains a `<dependencies>` section, add these dependencies to that existing section instead of creating a new one.

#### 3.2.3 Attach agent via maven-surefire-plugin

Configure the Surefire test plugin to load the agent during test execution:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            @{argLine}
            -javaagent:${settings.localRepository}/de/tum/cit/ase/ares/2.1.0/ares-2.1.0-agent.jar
            -Xbootclasspath/a:${settings.localRepository}/org/aspectj/aspectjrt/1.9.25.1/aspectjrt-1.9.25.1.jar
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
```

**Explanation:**
- `argLine`: JVM arguments passed to every test execution
- `@{argLine}`: Preserves JVM arguments that other plugins contribute late in the build, most importantly the JaCoCo agent injected by `jacoco:prepare-agent`. Surefire's `argLine` is a single value, so a `<argLine>` that omits `@{argLine}` **replaces** those contributions instead of adding to them, and the other agent is dropped silently, with no warning and a still-green build. Verified with Surefire 3.5.2 and JaCoCo 0.8.12: without `@{argLine}` the JaCoCo agent was absent from the test JVM; with it, both agents were present. Ares's own `pom.xml` uses `@{argLine}` for this reason. Keep it as the first entry even if you do not use JaCoCo today, so that adding a coverage or profiling plugin later does not silently disable it
- `-javaagent:${settings.localRepository}/...`: Path to the **agent** JAR (note the `-agent` suffix) in the local Maven repository (Maven resolves `${settings.localRepository}` to `~/.m2/repository`). When updating the Ares version, update both the `<version>` in the dependency and the version in this path.
- `-Xbootclasspath/a:...`: Appends the AspectJ **runtime** JAR (`aspectjrt`) to the bootstrap classpath so that woven bytecode can resolve AspectJ runtime types at the bootstrap class-loader level. `aspectjrt` is a separate Maven artefact (`org.aspectj:aspectjrt`), so its repository path follows the standard Maven layout. You may need to adjust the version (`1.9.25.1`) if a newer AspectJ version is required.
- **JVM Module Access Flags** (same as Gradle, see [Section 3.1.4](#314-attach-agent-to-test-execution) for the full per-flag explanation):
  - `--add-exports java.base/java.lang` and `--add-exports java.base/jdk.internal.misc`: Make the `java.lang` API and `jdk.internal.misc.Unsafe` accessible to the unnamed module (test classpath)
  - `--add-opens java.base/java.lang`, `java.lang.reflect`, `jdk.internal.misc`: Allow reflective access to private members of `java.lang`, the reflection API itself, and `jdk.internal.misc.Unsafe` (e.g., `theUnsafe`, read by the thread-system advice: `JavaInstrumentationAdviceThreadSystemToolbox` / `JavaAspectJThreadSystemAdviceDefinitions`)
  - `--add-opens jdk.unsupported/sun.misc`: Allows reflective access to the private `sun.misc.Unsafe` instance (legacy `Unsafe` access path; `sun.misc` lives in the `jdk.unsupported` module, not in `java.base`)
  - `--add-opens java.base/java.io`, `java.net`, `java.nio`, `java.nio.channels`, `sun.net.www.protocol.http`, `sun.net.www.protocol.https`, `sun.nio.ch`: Allow the Ares advice to reflectively read fields of intercepted file, network, and channel objects (e.g., `Socket.delegate`)
  - `--add-opens java.base/java.util`, `java.util.concurrent`, `java.util.concurrent.atomic`, `java.util.concurrent.locks`: Allow the Ares advice to reflectively read fields of intercepted collection and concurrency objects (e.g., `ThreadPoolExecutor.ctl`)

> **Tip (avoid hardcoded version):** Define a Maven property so the version appears in one place only:
> ```xml
> <properties>
>     <ares.version>2.1.0</ares.version>
> </properties>
> ```
> Then use `${ares.version}` in both the dependency and the `<argLine>`:
> ```
> -javaagent:${settings.localRepository}/de/tum/cit/ase/ares/${ares.version}/ares-${ares.version}-agent.jar
> -Xbootclasspath/a:${settings.localRepository}/org/aspectj/aspectjrt/1.9.25.1/aspectjrt-1.9.25.1.jar
> ```
> **Note:** If your `pom.xml` already contains a `<plugins>` section in the `<build>` section, add this `<plugin>` to that existing section instead of creating a new one. If an `maven-surefire-plugin` plugin already exists, merge the `<configuration>` sections (append the `<argLine>` to any existing one).

#### 3.2.4 Configure AspectJ compile-time weaving

Your build must run the AspectJ compiler (`ajc`) to weave the Ares security aspects into student bytecode. Without this step, the `-Xbootclasspath/a:` flag only provides runtime classes, but no weaving ever occurs, and the aspects have no effect.

**Add the `aspectj-maven-plugin` to your `<build><plugins>` section:**

```xml
<plugin>
    <groupId>dev.aspectj</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.14.1</version>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjtools</artifactId>
            <version>1.9.25.1</version>
        </dependency>
    </dependencies>
    <configuration>
        <forceAjcCompile>true</forceAjcCompile>
        <complianceLevel>${maven.compiler.source}</complianceLevel>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
        <aspectLibraries>
            <aspectLibrary>
                <groupId>de.tum.cit.ase</groupId>
                <artifactId>ares</artifactId>
            </aspectLibrary>
        </aspectLibraries>
    </configuration>
    <executions>
        <execution>
            <id>weave-main-classes</id>
            <phase>process-classes</phase>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Explanation:**
- `aspectj-maven-plugin` from `dev.aspectj` runs the AspectJ compiler (`ajc`) during the Maven build lifecycle.
- `aspectjtools` (version `1.9.25.1`) is the AspectJ compiler itself, declared as a plugin dependency so the plugin uses this specific version of `ajc`.
- `forceAjcCompile`: Forces `ajc` to run even if no `.aj` source files exist in the student project. This is essential because the aspects come from the Ares library, not from the project itself.
- `<aspectLibraries>`: Puts the Ares JAR on the **aspect path** of `ajc`. This is what makes `ajc` weave the compiled aspects shipped inside the Ares JAR; without it, the plugin would run but weave nothing, because `ajc` does not pick up binary aspects from the ordinary compile classpath. The entry references the Ares dependency from [Section 3.2.2](#322-add-ares-dependency), so no version needs to be repeated here.
- `complianceLevel`, `source`, `target`: Inherit the Java version from the project's `maven.compiler.source` and `maven.compiler.target` properties, ensuring the AspectJ compiler uses the same language level as `javac`.
- The `<execution>` block binds `ajc` to the `process-classes` phase, so it runs immediately after `javac` compiles main sources to `.class` files. Only main classes are woven; test classes are left untouched.
- **Aspect discovery:** `ajc` only weaves binary aspects from JARs listed on its aspect path. The `<aspectLibraries>` entry above places the Ares JAR (declared as a dependency in [Section 3.2.2](#322-add-ares-dependency)) on that aspect path, so `ajc` uses the compiled aspects shipped inside it.
- **What gets woven:** `ajc` reads the compiled `.class` files in `target/classes` and weaves in any code whose execution matches the pointcut expressions defined in the discovered aspects.
- `aspectjrt` (added in [Section 3.2.2](#322-add-ares-dependency)) provides the runtime classes (e.g., `org.aspectj.lang.JoinPoint`) that woven bytecode references at runtime. These must be on the bootstrap classpath (already configured in [Section 3.2.3](#323-attach-agent-via-maven-surefire-plugin)) **and** available at compile time.

---

## 4. Verify your setup

Verification has two steps, and **only the second one proves anything about security**.

### 4.1 Step 1: Confirm Ares is on the classpath

This test does **not** require a `SecurityPolicy.yaml` file:

```java
import de.tum.cit.ase.ares.api.jupiter.Public;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AresSetupVerificationTest {

    @Public
    @Test
    void aresSetupIsAvailable() {
        // If this test compiles and runs, the Ares classes are on the test classpath
        // and the test JVM started with the configured arguments.
        assertDoesNotThrow(() -> {});
    }
}
```

Run `./gradlew test` (or `mvn test`). A pass confirms the dependency and the JVM arguments resolve. It confirms **nothing else**.

> **Warning: a test without `@Policy` is not protected.**
> A test class that has no `@Policy` annotation (directly, or inherited from a meta-annotation) runs with **no security enforcement at all**. Ares does not fall back to a restrictive default for such tests. Verified against Ares 2.1.0: in a test class shaped exactly like `AresSetupVerificationTest` above, student code called `Files.readString(Path.of("build.gradle"))` and read the file's full 4233 bytes without being blocked.
>
> Enforcement is opt-in per test class. See [Section 4.2](#42-step-2-prove-that-enforcement-actually-happens) and [Section 6](#6-next-steps).

### 4.2 Step 2: Prove that enforcement actually happens

This is the step that verifies security. Add a class in the supervised package that performs a forbidden operation, and a test **carrying `@Policy`** that calls it:

```java
// In the supervised (student) package, e.g. src/main/java/com/example/AresProbe.java
package com.example;

import java.nio.file.Files;
import java.nio.file.Path;

public class AresProbe {
    public static String readSecret() throws Exception {
        return Files.readString(Path.of("build.gradle"));
    }
}
```

```java
// In the test sources
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

import static org.junit.jupiter.api.Assertions.fail;

@Policy(value = "src/test/resources/SecurityPolicy.yaml", withinPath = "classes/java/main/com/example")
class AresEnforcementTest {

    @PublicTest
    void forbiddenFileReadMustBeBlocked() {
        try {
            AresProbe.readSecret();
            fail("Ares did not block the forbidden file read: enforcement is NOT active.");
        } catch (SecurityException expected) {
            // Ares blocked the call, the setup enforces the policy.
        } catch (Exception e) {
            fail("Unexpected non-security exception: " + e);
        }
    }
}
```

With a policy whose `regardingFileSystemInteractions` list is empty, this must terminate with a `java.lang.SecurityException` mentioning the offending call. If the test instead reports that nothing was blocked, enforcement is not active: re-check `@Policy`, `withinPath`, and the agent arguments before trusting the setup.

Remove the probe class once the setup is confirmed.

> **Note:** Creating the `SecurityPolicy.yaml` used above is covered in the [Security Policy Manual](policy/SecurityPolicyManual.md); see also [Next steps](#6-next-steps).

> **What happens without the agent?** If the `-javaagent` flag is missing, Ares's **static analysis** (ArchUnit/WALA architecture tests) still works, and **AspectJ enforcement** also still works, because the aspects are woven into the bytecode at compile time. Only the **ByteBuddy instrumentation** enforcement path is inactive, since it relies on the agent to transform classes at load time. If you use an `INSTRUMENTATION` configuration, students could then bypass security restrictions at runtime, so always ensure the agent is loaded.

---

## 5. Upgrading from Ares 1 to Ares 2.1.0

Skip this section for a new project. It applies when your project currently uses Ares 1 (`de.tum.in.ase:artemis-java-test-sandbox`) and you are moving it onto Ares 2.1.0.

The build changes in [Section 3](#3-add-ares-dependencies-and-agent-setup) are **not sufficient on their own**: the test sources must be migrated too, because Ares 1 expressed its security rules as annotations, whereas Ares 2 expresses them in a `SecurityPolicy.yaml` file.

### 5.1 Replace the dependency

Remove the Ares 1 dependency and add the Ares 2 dependencies from [Section 3](#3-add-ares-dependencies-and-agent-setup):

```diff
- testImplementation 'de.tum.in.ase:artemis-java-test-sandbox:1.15.0'
+ testImplementation "de.tum.cit.ase:ares:2.1.0"
```

### 5.2 Rename the package

Every Ares 1 import moves from `de.tum.in.test.api` to `de.tum.cit.ase.ares.api`:

```diff
- import de.tum.in.test.api.jupiter.Public;
+ import de.tum.cit.ase.ares.api.jupiter.Public;
```

### 5.3 Map the annotations

| Ares 1 (current) | Ares 2.1.0 (target) | Notes |
|---|---|---|
| `@Public`, `@PublicTest`, `@Hidden`, `@HiddenTest` | unchanged, new package | `de.tum.cit.ase.ares.api.jupiter` |
| `@StrictTimeout`, `@MirrorOutput`, `@Deadline`, `@ExtendedDeadline` | unchanged, new package | `de.tum.cit.ase.ares.api` |
| `structural.*TestProvider`, `util.ReflectionTestUtils`, `io.IOTester` | unchanged, new package | API-compatible |
| `@WhitelistPath` | **removed** | Express as `regardingFileSystemInteractions` entries in the policy file |
| `@BlacklistPath` | **removed** | Ares 2 is default-deny, so anything not permitted is already denied |
| `@WhitelistClass` | **removed** | Express as `theFollowingClassesAreTestClasses` in the policy file |
| `PathType` (`GLOB`, `STARTS_WITH`) | **removed** | Policy paths use `onThisPathAndAllPathsBelow` |

Delete the removed annotations and their imports, then write the equivalent `SecurityPolicy.yaml` as described in the [Security Policy Manual](policy/SecurityPolicyManual.md).

### 5.4 Add `@Policy` to every test class

This step is easy to miss and fails silently. In Ares 1, a test was protected by virtue of the sandbox being installed. In Ares 2, **a test class with no `@Policy` runs unprotected** (see the warning in [Section 4.1](#41-step-1-confirm-ares-is-on-the-classpath)).

Annotate every test class, and remember that `@Policy` is inherited through **meta-annotations**: if your exercise defines a marker annotation that its test classes carry, putting `@Policy` on that marker covers all of them.

```java
@Policy(value = "test/de/tum/cit/aet/SecurityPolicy.yaml", withinPath = "classes/java/main/de/tum/cit/aet")
@Public
@StrictTimeout(3)
@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
public @interface E01 {
}
```

After migrating, audit for stragglers, since these are the classes that will run without enforcement:

```bash
grep -rL "@Policy" src/test/java --include="*Test.java"
```

---

## 6. Next steps

1. **Create a security policy and annotate tests:** Follow the [Security Policy Manual](policy/SecurityPolicyManual.md), which explains how to write `SecurityPolicy.yaml` files and apply the `@Policy` annotation to your tests.
2. **Choose the right configuration:** Select one of the 8 `ProgrammingLanguageConfiguration` values matching your build tool, architecture analysis, and runtime enforcement:

| Value | Build Tool | Static Analysis | Runtime Enforcement |
|---|---|---|---|
| `JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ` | Maven | ArchUnit (rule-based) | AspectJ (compile-time weaving) |
| `JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION` | Maven | ArchUnit (rule-based) | ByteBuddy agent (runtime) |
| `JAVA_USING_MAVEN_WALA_AND_ASPECTJ` | Maven | WALA (call-graph) | AspectJ (compile-time weaving) |
| `JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION` | Maven | WALA (call-graph) | ByteBuddy agent (runtime) |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ` | Gradle | ArchUnit (rule-based) | AspectJ (compile-time weaving) |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION` | Gradle | ArchUnit (rule-based) | ByteBuddy agent (runtime) |
| `JAVA_USING_GRADLE_WALA_AND_ASPECTJ` | Gradle | WALA (call-graph) | AspectJ (compile-time weaving) |
| `JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION` | Gradle | WALA (call-graph) | ByteBuddy agent (runtime) |

**How to choose:**
- **Build tool:** Match your project (`MAVEN` or `GRADLE`).
- **Static analysis:** `ARCHUNIT` is simpler and faster; `WALA` detects transitive violations through call chains.
- **Runtime enforcement:** `INSTRUMENTATION` (ByteBuddy agent) or `ASPECTJ` (compile-time weaving). The AspectJ compiler plugin and bootstrap classpath are always configured (see [Section 3.1.5](#315-how-compile-time-weaving-works) for Gradle or [Section 3.2.4](#324-configure-aspectj-compile-time-weaving) for Maven), regardless of which value you choose.

---

## 7. Troubleshooting

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| `ClassNotFoundException: de.tum.cit.ase.ares.api.Policy` | Ares not on the test classpath | Verify `testImplementation` dependency is present |
| `Failed to find premain agent` or agent-related errors | Agent JAR not found or wrong classifier | Ensure the `aresAgent` dependency uses the `:agent` classifier (Gradle) or the `-agent.jar` suffix (Maven) |
| Tests pass but student code is not restricted | `-javaagent` JVM argument missing | Check that `jvmArgs` / `<argLine>` includes the `-javaagent:...` path |
| Tests pass but student code is not restricted | Test class has no `@Policy` | The most common cause. A test class without `@Policy` (direct or meta-annotated) runs with **no** enforcement; Ares does not apply a restrictive default. Find them with `grep -rL "@Policy" src/test/java --include="*Test.java"`. See [Section 4.1](#41-step-1-confirm-ares-is-on-the-classpath) |
| Enforcement silently inactive for some tests only | `withinPath` points at a directory that does not exist | A `withinPath` that does not match the compiled student package resolves to an empty directory, so nothing is analysed and no error is raised. Confirm the path exists under `build/classes/java/main` (Gradle) or `target/classes` (Maven) after a build |
| Coverage report empty after adding Ares (Maven) | `<argLine>` overwrote the JaCoCo agent | Put `@{argLine}` first inside `<argLine>`, see [Section 3.2.3](#323-attach-agent-via-maven-surefire-plugin) |
| `InaccessibleObjectException` at runtime | Missing `--add-opens` / `--add-exports` flags | Ensure the complete list of JVM module access flags from [Section 3.1.4](#314-attach-agent-to-test-execution) / [Section 3.2.3](#323-attach-agent-via-maven-surefire-plugin) is present |
| Policy seems to have no effect | Wrong `withinPath` | Gradle: `classes/java/main/<package/path>`, Maven: `classes/<package/path>` |

---

## 8. Glossary

| Term | Meaning |
|------|----------|
| **Java Agent** | A JVM mechanism (`-javaagent`) that allows code to transform class bytecode at load time. Ares uses a ByteBuddy-based agent to intercept forbidden operations at runtime. |
| **ByteBuddy** | A library for creating and modifying Java classes at runtime, used by Ares to implement the instrumentation agent. |
| **Instrumentation** | The runtime AOP approach where class bytecode is modified at load time via the `java.lang.instrument` API. One of the two runtime enforcement mechanisms in Ares (selected via `ProgrammingLanguageConfiguration`), alongside AspectJ. |
| **AspectJ** | A compile-time AOP framework used for runtime enforcement. Requires two things: (1) the AspectJ compiler plugin (`io.freefair.aspectj.post-compile-weaving` for Gradle or `dev.aspectj:aspectj-maven-plugin` for Maven) to weave aspects into bytecode during the build, and (2) the AspectJ runtime JAR (`aspectjrt`) on the bootstrap classpath (`-Xbootclasspath/a:`). The compiler weaves the binary aspects from the Ares JAR only if that JAR is placed on the aspect path (Gradle: the `aspect` configuration; Maven: an `<aspectLibraries>` entry). |
| **`aresAgent` Configuration** | A custom Gradle configuration that isolates the Ares agent JAR from other dependencies, allowing it to be referenced separately in `jvmArgs`. |
| **`-javaagent`** | A JVM command-line flag that loads a Java agent before the application starts. Required for Ares runtime enforcement. |
| **`--add-opens` / `--add-exports`** | JVM flags that grant access to internal Java modules. Required by Ares to instrument bytecode in `java.base`. |
| **`withinPath`** | The path to compiled student bytecode, relative to the build output directory. Differs between Gradle (`classes/java/main/...`) and Maven (`classes/...`). |
| **`ProgrammingLanguageConfiguration`** | An enum encoding the combination of build tool (Maven/Gradle), static analysis framework (ArchUnit/WALA), and runtime enforcement mechanism (AspectJ/Instrumentation). |
| **Classifier (`:agent`)** | A Maven/Gradle coordinate qualifier that selects a specific variant of an artefact. The `:agent` classifier selects the agent JAR, which carries the `Premain-Class` manifest entry but does not bundle Byte Buddy or any other dependency — see [Section 3.1.2](#312-configure-ares-agent-configuration)'s explanation of why that's still sufficient. |
