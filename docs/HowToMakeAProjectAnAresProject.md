# Ares-Protected Student Exercise Manual

> **Audience:** IT-Education experts with no security background.
> **Scope:** The `build.gradle` and `pom.xml` files.
> **Ares Version:** 2.0.1-Beta9

> **Note:** This guide is a **setup guide**. It covers adding the Ares dependency, attaching the agent, and configuring the build tool so that Ares can run. For writing security policies that control what student code can do, see the [Security Policy Manual](policy/SecurityPolicyManual.md).

**Related documentation:**
- [Security Policy Manual](policy/SecurityPolicyManual.md), which explains how to write a security policy YAML file
- [Security Policy Reader and Director Manual](policy/SecurityPolicyReaderAndDirectorManual.md), which describes the internal processing pipeline

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Purpose: What Problem Does This Solve?](#2-purpose--what-problem-does-this-solve)
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
5. [Next steps](#5-next-steps)
6. [Troubleshooting](#6-troubleshooting)
7. [Glossary](#7-glossary)

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

**Explanation:** This creates a new Gradle configuration named `aresAgent`. A Gradle configuration is a named bucket of dependencies that can be resolved independently; by creating a dedicated one for the agent, the agent JAR is downloaded and resolved separately from the compile and test classpaths. This keeps the agent's bundled dependencies (e.g., the bundled ByteBuddy classes) out of Gradle's compile and test dependency resolution, and it lets us reference the agent JAR by its exact file path in the `jvmArgs` of the `test` task (see [Section 3.1.4](#314-attach-agent-to-test-execution)). Note that this isolation only applies to build-time classpath resolution: at runtime, the `-javaagent` mechanism appends the agent JAR to the system classpath, so its bundled classes are still visible to the test JVM.

> **Note:** If your `build.gradle` already contains a `configurations` block, add the `aresAgent` configuration to that existing block instead of creating a new one.

#### 3.1.3 Add Ares dependencies

Add the Ares library to both the agent configuration and test implementation, as well as the AspectJ runtime library:

```gradle
dependencies {
    aresAgent "de.tum.cit.ase:ares:2.0.1-Beta9:agent"
    aresAgent "org.aspectj:aspectjrt:1.9.25.1"
    testImplementation "de.tum.cit.ase:ares:2.0.1-Beta9"
    aspect "de.tum.cit.ase:ares:2.0.1-Beta9"
    implementation "org.aspectj:aspectjrt:1.9.25.1"
}
```

> **Tip (Gradle version catalog):** If your project uses a Gradle version catalog file (for example `gradle/<versions-catalog>.toml`), you can declare the version centrally:
> ```toml
> [versions]
> ares = "2.0.1-Beta9"
> aspectjrt = "1.9.25.1"
> [libraries]
> ares = { module = "de.tum.cit.ase:ares", version.ref = "ares" }
> aspectjrt = { module = "org.aspectj:aspectjrt", version.ref = "aspectjrt" }
> ```
> Then reference `libs.ares` and `libs.aspectjrt` in `build.gradle`. Note that Gradle version catalogs do not natively support Maven classifiers, so the `aresAgent` dependency with the `:agent` classifier must remain as a direct dependency string in `build.gradle`.

**Explanation:**
- `aresAgent "de.tum.cit.ase:ares:..."`: Downloads the Ares **agent** JAR (with classifier `agent`) into the custom `aresAgent` configuration. This JAR contains the ByteBuddy instrumentation agent with the correct `Premain-Class` manifest entry and all bundled dependencies (bundled under their original package names, without relocation).
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
    <version>2.0.1-Beta9</version>
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
            -javaagent:${settings.localRepository}/de/tum/cit/ase/ares/2.0.1-Beta9/ares-2.0.1-Beta9-agent.jar
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
>     <ares.version>2.0.1-Beta9</ares.version>
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

Create a minimal test to confirm Ares is working. This test does **not** require a `SecurityPolicy.yaml` file. A passing run confirms that the Ares classes are available on the test classpath and that the test JVM starts with the configured arguments; it does **not** prove that the agent performed any instrumentation. Note that the test is not enforcement-free either: without a `@Policy` annotation, Ares's JUnit extension still applies its default, most restrictive security policy to the test:

```java
import de.tum.cit.ase.ares.api.jupiter.Public;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AresSetupVerificationTest {

    @Public
    @Test
    void aresSetupIsAvailable() {
        // If this test compiles and runs without errors, the Ares classes are on the
        // test classpath and the default security policy could be applied.
        assertDoesNotThrow(() -> {});
    }
}
```

Run `./gradlew test` (or `mvn test`). If the test passes, the Ares dependency and JVM arguments are correctly set up. To verify that the agent and the weaving actually enforce restrictions, write a test whose code performs a forbidden operation (e.g., writing a file outside the allowed paths) and check that it fails with an Ares security error.

> **Note:** To verify full policy enforcement, also create a `SecurityPolicy.yaml` and add `@Policy` to your test (see [Next steps](#5-next-steps)).

> **What happens without the agent?** If the `-javaagent` flag is missing, Ares's **static analysis** (ArchUnit/WALA architecture tests) still works, and **AspectJ enforcement** also still works, because the aspects are woven into the bytecode at compile time. Only the **ByteBuddy instrumentation** enforcement path is inactive, since it relies on the agent to transform classes at load time. If you use an `INSTRUMENTATION` configuration, students could then bypass security restrictions at runtime, so always ensure the agent is loaded.

---

## 5. Next steps

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

## 6. Troubleshooting

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| `ClassNotFoundException: de.tum.cit.ase.ares.api.Policy` | Ares not on the test classpath | Verify `testImplementation` dependency is present |
| `Failed to find premain agent` or agent-related errors | Agent JAR not found or wrong classifier | Ensure the `aresAgent` dependency uses the `:agent` classifier (Gradle) or the `-agent.jar` suffix (Maven) |
| Tests pass but student code is not restricted | `-javaagent` JVM argument missing | Check that `jvmArgs` / `<argLine>` includes the `-javaagent:...` path |
| `InaccessibleObjectException` at runtime | Missing `--add-opens` / `--add-exports` flags | Ensure the complete list of JVM module access flags from [Section 3.1.4](#314-attach-agent-to-test-execution) / [Section 3.2.3](#323-attach-agent-via-maven-surefire-plugin) is present |
| Policy seems to have no effect | Wrong `withinPath` | Gradle: `classes/java/main/<package/path>`, Maven: `classes/<package/path>` |

---

## 7. Glossary

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
| **Classifier (`:agent`)** | A Maven/Gradle coordinate qualifier that selects a specific variant of an artefact. The `:agent` classifier selects the agent JAR (with bundled dependencies) carrying the `Premain-Class` manifest entry. |
