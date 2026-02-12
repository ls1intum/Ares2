# Ares-Protected Student Exercise Manual

> **Audience:** IT-Education experts with no security background.
> **Scope:** The `build.gradle` and `pom.xml` files.
> **Ares Version:** 2.0.1-Beta6

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
- **Gradle 7+** (recommended) or **Maven 3.8+**
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

### 3.1 Gradle (recommended)

First, add the AspectJ compiler plugin:

```gradle
plugins {
    id 'io.freefair.aspectj.post-compile-weaving' version '8.12.1'
}
```

This plugin runs the AspectJ compiler (`ajc`) during your build to weave security aspects into bytecode.

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

**Explanation:** This creates a new Gradle configuration named `aresAgent`. A Gradle configuration is a named bucket of dependencies that can be resolved independently; by creating a dedicated one for the agent, the agent JAR is downloaded and resolved separately from the compile and test classpaths. This isolation ensures that the agent's bundled dependencies (e.g., shaded ByteBuddy classes) do not conflict with other project dependencies, and it lets us reference the agent JAR by its exact file path in the `jvmArgs` of the `test` task (see [Section 3.1.4](#314-attach-agent-to-test-execution)).

> **Note:** If your `build.gradle` already contains a `configurations` block, add the `aresAgent` configuration to that existing block instead of creating a new one.

#### 3.1.3 Add Ares dependencies

Add the Ares library to both the agent configuration and test implementation, as well as the AspectJ runtime library:

```gradle
dependencies {
    aresAgent "de.tum.cit.ase:ares:2.0.1-Beta6:agent"
    aresAgent 'org.aspectj:aspectjrt:1.9.24'
    testImplementation "de.tum.cit.ase:ares:2.0.1-Beta6"
    implementation 'org.aspectj:aspectjrt:1.9.24'
}
```

> **Tip (Gradle version catalog):** If your project uses a `gradle/libs.versions.toml`, you can declare the version centrally:
> ```toml
> [versions]
> ares = "2.0.1-Beta6"
> aspectjrt = "1.9.24"
> [libraries]
> ares = { module = "de.tum.cit.ase:ares", version.ref = "ares" }
> aspectjrt = { module = "org.aspectj:aspectjrt", version.ref = "aspectjrt" }
> ```
> Then reference `libs.ares` and `libs.aspectjrt` in `build.gradle`. Note that Gradle version catalogs do not natively support Maven classifiers, so the `aresAgent` dependency with the `:agent` classifier must remain as a direct dependency string in `build.gradle`.

**Explanation:**
- `aresAgent "de.tum.cit.ase:ares:..."`: Downloads the Ares **agent** JAR (with classifier `agent`) into the custom `aresAgent` configuration. This shaded JAR contains the ByteBuddy instrumentation agent with the correct `Premain-Class` manifest entry and all bundled dependencies.
- `aresAgent 'org.aspectj:aspectjrt:...'`: Also adds the AspectJ runtime JAR to the `aresAgent` configuration so that it can be resolved in [Section 3.1.4](#314-attach-agent-to-test-execution) via `filter { it.name.contains('aspectjrt') }` for the `-Xbootclasspath/a:` JVM argument.
- `testImplementation`: Makes Ares classes available on the test classpath so your test code can use `@Policy`, `@Public`, and other Ares annotations. We use `testImplementation` instead of `implementation` because Ares is only needed during testing, not in the production code of the exercise. Using `implementation` would unnecessarily add Ares to the main classpath and the final artefact, which could interfere with student code and violates the principle of minimal dependency scope.
- `implementation 'org.aspectj:aspectjrt:1.9.24'`: The AspectJ runtime library providing classes (e.g., `org.aspectj.lang.JoinPoint`) that woven bytecode references. This must be available both at compile time (for the AspectJ compiler to resolve) and at runtime (on the bootstrap classpath, configured in [Section 3.1.4](#314-attach-agent-to-test-execution)). We use `implementation` instead of `testImplementation` because the AspectJ compiler weaves main classes during the `compileJava` phase — at that point, only the main compile classpath is visible, so `aspectjrt` must be on it for `ajc` to resolve the runtime types it injects.

> **Note:** If your `build.gradle` already contains a `dependencies` block, add these dependencies to that existing block instead of creating a new one.

#### 3.1.4 Attach agent to test execution

Configure the test task to load the agent when running tests:

```gradle
test {
    useJUnitPlatform()
    jvmArgs += [
        "-javaagent:${configurations.aresAgent.filter { it.name.startsWith('ares-') }.singleFile.absolutePath}",
        "-Xbootclasspath/a:${configurations.aresAgent.filter { it.name.contains('aspectjrt') }.singleFile.absolutePath}",
        '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
        '--add-exports', 'java.base/java.lang=ALL-UNNAMED',
        '--add-opens', 'java.base/jdk.internal.misc=ALL-UNNAMED',
        '--add-exports', 'java.base/jdk.internal.misc=ALL-UNNAMED',
        '--add-opens', 'java.base/sun.misc=ALL-UNNAMED',
        '--add-opens', 'java.base/java.lang.reflect=ALL-UNNAMED'
    ]
}
```

**Explanation:**
- `useJUnitPlatform()`: Enables JUnit 5 (Jupiter) test discovery
- `-javaagent:...`: Loads the Ares agent before any user code runs, which is critical for bytecode instrumentation to work
- `-Xbootclasspath/a:...`: Appends the AspectJ **runtime** JAR (`aspectjrt`) to the bootstrap classpath so that woven bytecode can resolve AspectJ runtime types at the bootstrap class-loader level. The `filter { it.name.contains('aspectjrt') }` dynamically resolves it from the `aresAgent` configuration (where it was explicitly added in [Section 3.1.3](#313-add-ares-dependencies)).
- **JVM Module Opening Flags** (all modules need to be opened for Ares to instrument bytecode):
  - `--add-opens java.base/java.lang`: Allows reflective access to private fields and methods in `java.lang` (e.g., `Class.declaredFields`)
  - `--add-exports java.base/java.lang`: Makes the public `java.lang` API accessible to the unnamed module (test classpath)
  - `--add-opens java.base/jdk.internal.misc`: Allows reflective access to private `Unsafe` fields in `jdk.internal.misc` (e.g., `theUnsafe` in `NativeCodeAccess`)
  - `--add-exports java.base/jdk.internal.misc`: Makes `jdk.internal.misc.Unsafe` accessible to the unnamed module for direct API calls
  - `--add-opens java.base/sun.misc`: Allows reflective access to the private `sun.misc.Unsafe` instance (legacy `Unsafe` access path)
  - `--add-opens java.base/java.lang.reflect`: Allows reflective access to private members of the reflection API itself (e.g., `Field.setAccessible`, `Method.setAccessible`)

> **Note:** If your `build.gradle` already contains a `test` block, add `useJUnitPlatform()` and the `jvmArgs +=` to that existing block instead of creating a new one.

#### 3.1.5 How compile-time weaving works

The build automatically runs the AspectJ compiler (`ajc`) in the following way:

1. **After compilation:** The `io.freefair.aspectj.post-compile-weaving` plugin runs `ajc` **after** `javac` finishes compiling your source files to bytecode.
2. **Aspect discovery:** `ajc` scans the entire compile classpath for `.aj` aspect files. Since Ares is declared as a dependency, its JAR is on the classpath, and `ajc` automatically discovers the `.aj` aspects shipped inside it.
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
    <version>2.0.1-Beta6</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>1.9.24</version>
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
            -javaagent:${settings.localRepository}/de/tum/cit/ase/ares/2.0.1-Beta6/ares-2.0.1-Beta6-agent.jar
            -Xbootclasspath/a:${settings.localRepository}/org/aspectj/aspectjrt/1.9.24/aspectjrt-1.9.24.jar
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-exports java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
            --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
            --add-opens java.base/sun.misc=ALL-UNNAMED
            --add-opens java.base/java.lang.reflect=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

**Explanation:**
- `argLine`: JVM arguments passed to every test execution
- `-javaagent:${settings.localRepository}/...`: Path to the **agent** JAR (note the `-agent` suffix) in the local Maven repository (Maven resolves `${settings.localRepository}` to `~/.m2/repository`). When updating the Ares version, update both the `<version>` in the dependency and the version in this path.
- `-Xbootclasspath/a:...`: Appends the AspectJ **runtime** JAR (`aspectjrt`) to the bootstrap classpath so that woven bytecode can resolve AspectJ runtime types at the bootstrap class-loader level. `aspectjrt` is a separate Maven artefact (`org.aspectj:aspectjrt`), so its repository path follows the standard Maven layout. You may need to adjust the version (`1.9.24`) if a newer AspectJ version is required.
- **JVM Module Opening Flags** (same as Gradle):
  - `--add-opens java.base/java.lang`: Allows reflective access to private fields and methods in `java.lang` (e.g., `Class.declaredFields`)
  - `--add-exports java.base/java.lang`: Makes the public `java.lang` API accessible to the unnamed module (test classpath)
  - `--add-opens java.base/jdk.internal.misc`: Allows reflective access to private `Unsafe` fields in `jdk.internal.misc` (e.g., `theUnsafe` in `NativeCodeAccess`)
  - `--add-exports java.base/jdk.internal.misc`: Makes `jdk.internal.misc.Unsafe` accessible to the unnamed module for direct API calls
  - `--add-opens java.base/sun.misc`: Allows reflective access to the private `sun.misc.Unsafe` instance (legacy `Unsafe` access path)
  - `--add-opens java.base/java.lang.reflect`: Allows reflective access to private members of the reflection API itself (e.g., `Field.setAccessible`, `Method.setAccessible`)

> **Tip (avoid hardcoded version):** Define a Maven property so the version appears in one place only:
> ```xml
> <properties>
>     <ares.version>2.0.1-Beta6</ares.version>
> </properties>
> ```
> Then use `${ares.version}` in both the dependency and the `<argLine>`:
> ```
> -javaagent:${settings.localRepository}/de/tum/cit/ase/ares/${ares.version}/ares-${ares.version}-agent.jar
> -Xbootclasspath/a:${settings.localRepository}/org/aspectj/aspectjrt/1.9.24/aspectjrt-1.9.24.jar
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
            <version>1.9.24</version>
        </dependency>
    </dependencies>
    <configuration>
        <forceAjcCompile>true</forceAjcCompile>
        <complianceLevel>${maven.compiler.source}</complianceLevel>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
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
- `aspectjtools` (version `1.9.24`) is the AspectJ compiler itself, declared as a plugin dependency so the plugin uses this specific version of `ajc`.
- `forceAjcCompile`: Forces `ajc` to run even if no `.aj` source files exist in the student project. This is essential because the aspects come from the Ares library on the classpath, not from the project itself.
- `complianceLevel`, `source`, `target`: Inherit the Java version from the project's `maven.compiler.source` and `maven.compiler.target` properties, ensuring the AspectJ compiler uses the same language level as `javac`.
- The `<execution>` block binds `ajc` to the `process-classes` phase, so it runs immediately after `javac` compiles main sources to `.class` files. Only main classes are woven; test classes are left untouched.
- **Aspect discovery:** `ajc` scans the entire compile classpath for `.aj` aspect files. Since Ares is declared as a dependency (see [Section 3.2.2](#322-add-ares-dependency)), its JAR is on the classpath, and `ajc` automatically discovers the `.aj` aspects shipped inside it.
- **What gets woven:** `ajc` reads the compiled `.class` files in `target/classes` and weaves in any code whose execution matches the pointcut expressions defined in the discovered aspects.
- `aspectjrt` (added in [Section 3.2.2](#322-add-ares-dependency)) provides the runtime classes (e.g., `org.aspectj.lang.JoinPoint`) that woven bytecode references at runtime. These must be on the bootstrap classpath (already configured in [Section 3.2.3](#323-attach-agent-via-maven-surefire-plugin)) **and** available at compile time.

---

## 4. Verify your setup

Create a minimal test to confirm Ares is working. This test does **not** require a `SecurityPolicy.yaml` file, it only checks that the agent loads and Ares classes are available:

```java
import de.tum.cit.ase.ares.api.jupiter.Public;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class AresSetupVerificationTest {

    @Public
    @Test
    void aresAgentIsLoaded() {
        // If this test compiles and runs without errors, Ares is correctly configured.
        assertDoesNotThrow(() -> {});
    }
}
```

Run `./gradlew test` (or `mvn test`). If the test passes, Ares is correctly set up.

> **Note:** To verify full policy enforcement, also create a `SecurityPolicy.yaml` and add `@Policy` to your test (see [Next steps](#5-next-steps)).

> **What happens without the agent?** If the `-javaagent` flag is missing, Ares's **static analysis** (ArchUnit/WALA architecture tests) still works, but **runtime enforcement** (ByteBuddy/AspectJ interception) is inactive. Students could then bypass security restrictions at runtime. Always ensure the agent is loaded.

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
| `InaccessibleObjectException` at runtime | Missing `--add-opens` / `--add-exports` flags | Ensure all six JVM module flags are present |
| Policy seems to have no effect | Wrong `withinPath` | Gradle: `classes/java/main/<package/path>`, Maven: `classes/<package/path>` |

---

## 7. Glossary

| Term | Meaning |
|------|----------|
| **Java Agent** | A JVM mechanism (`-javaagent`) that allows code to transform class bytecode at load time. Ares uses a ByteBuddy-based agent to intercept forbidden operations at runtime. |
| **ByteBuddy** | A library for creating and modifying Java classes at runtime, used by Ares to implement the instrumentation agent. |
| **Instrumentation** | The runtime AOP approach where class bytecode is modified at load time via the `java.lang.instrument` API. The recommended enforcement mechanism for Ares. |
| **AspectJ** | A compile-time AOP framework used for runtime enforcement. Requires two things: (1) the AspectJ compiler plugin (`io.freefair.aspectj.post-compile-weaving` for Gradle or `dev.aspectj:aspectj-maven-plugin` for Maven) to weave aspects into bytecode during the build, and (2) the AspectJ runtime JAR (`aspectjrt`) on the bootstrap classpath (`-Xbootclasspath/a:`). The compiler discovers `.aj` aspect files from the Ares JAR on the classpath and weaves matching pointcuts into student bytecode. |
| **`aresAgent` Configuration** | A custom Gradle configuration that isolates the Ares agent JAR from other dependencies, allowing it to be referenced separately in `jvmArgs`. |
| **`-javaagent`** | A JVM command-line flag that loads a Java agent before the application starts. Required for Ares runtime enforcement. |
| **`--add-opens` / `--add-exports`** | JVM flags that grant access to internal Java modules. Required by Ares to instrument bytecode in `java.base`. |
| **`withinPath`** | The path to compiled student bytecode, relative to the build output directory. Differs between Gradle (`classes/java/main/...`) and Maven (`classes/...`). |
| **`ProgrammingLanguageConfiguration`** | An enum encoding the combination of build tool (Maven/Gradle), static analysis framework (ArchUnit/WALA), and runtime enforcement mechanism (AspectJ/Instrumentation). |
| **Classifier (`:agent`)** | A Maven/Gradle coordinate qualifier that selects a specific variant of an artefact. The `:agent` classifier selects the shaded agent JAR with `Premain-Class` manifest entry. |
