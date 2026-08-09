---
title: "How to transform an Ares 1 protected project into an Ares 2 protected project"
sidebar_position: 3
description: "Migrating an existing Ares 1 exercise to Ares 2, including the annotation-to-policy mapping table."
---

:::tip[ELI5]
If you have an exercise built on the old Ares, this page moves it to the new one.

The biggest change is that permissions used to be scattered across labels in the test code and
now live together in one policy file. Most of this page is the table telling you which old
label becomes which new setting.
:::

> **Audience:** IT-Education experts maintaining an existing Ares 1 exercise.
> **Scope:** The whole exercise: build files, test sources, security configuration.
> **From:** Ares 1 (`de.tum.in.ase:artemis-java-test-sandbox:1.15.0`)
> **To:** Ares 2 (`de.tum.cit.ase:ares:2.1.1`)

> **Note:** This guide is **self-contained**. Everything needed to complete a migration is here, including the full build configuration, so you do not need a second document open. Where it duplicates the [Postcompile walkthroughs](protect-a-java-project/precompile-or-postcompile.md), those pages are the canonical source: if the two ever disagree, follow it and report the discrepancy.

> **Version snapshot:** the configuration below is correct for Ares 2.1.1. Later releases may change it; check the setup manual before copying this into a new exercise.

**Related documentation:**
- [Precompile or Postcompile](protect-a-java-project/precompile-or-postcompile.md), the canonical setup guide
- [Security Policy Manual](/contributor/subsystems/policy/security-policy-manual), the reference for the policy file
- [Enforcement Model](/contributor/subsystems/policy/enforcement-model), which defines what static analysis and the runtime layer are each responsible for

---

## 1. Why migrate

Ares 1 enforces its restrictions with a `SecurityManager`. `ArtemisSecurityManager` installs itself by calling `System.setSecurityManager(...)` at runtime, and every permission decision is made by inspecting the call stack for non-whitelisted frames.

That mechanism has been withdrawn from the platform. JEP 411 deprecated the Security Manager for removal in Java 17. From Java 18, installing one at runtime is disallowed by default, so `System.setSecurityManager` throws unless the JVM was started with `-Djava.security.manager=allow`. JEP 486, in Java 24, disabled it permanently: the call now always throws, and no flag re-enables it. An Ares 1 exercise therefore cannot be run on a current JDK, and the workarounds available on Java 18 to 23 expire.

Ares 2 does not use a `SecurityManager` at all. It combines two layers:

- **Static analysis** of the compiled student bytecode, using either ArchUnit (rule-based) or WALA (call-graph based), which rejects forbidden operations before anything runs.
- **A runtime layer**, using either AspectJ aspects woven into the bytecode at compile time, or a ByteBuddy `-javaagent` that transforms classes at load time, which intercepts operations as they happen.

Neither depends on a platform feature that is going away. The cost is that the build has more moving parts, which is what [Section 4](#4-step-1-replace-the-dependency-and-wire-up-the-build) is about.

---

## 2. What changes, at a glance

| | Ares 1 | Ares 2 |
|---|---|---|
| Coordinate | `de.tum.in.ase:artemis-java-test-sandbox` | `de.tum.cit.ase:ares` |
| Root package | `de.tum.in.test.api` | `de.tum.cit.ase.ares.api` |
| Enforcement | `SecurityManager`, stack-frame inspection | Static analysis (ArchUnit or WALA) plus a runtime layer (AspectJ or a ByteBuddy agent) |
| Configuration | Per-test annotations (`@WhitelistPath`, `@AllowThreads`, …) | A `SecurityPolicy.yaml` file referenced by `@Policy` |
| Combining configuration | The whitelist and blacklist annotations are repeatable and additive across class and method; others, such as `@AllowLocalPort` and `@AllowThreads`, resolve nearest-first | The **nearest** `@Policy` wins; policies are never merged |
| Default without configuration | Denies file paths; most package access permitted | Denies file, network, command and thread access; package imports restricted to an implicit allowlist |
| Build requirements | A dependency | A dependency, AspectJ weaving, an agent attachment, JVM module-access flags |
| Class-shadowing guard | `maven-enforcer-plugin`, or a Gradle `doFirst` assertion, with the Ares 1 prefix list | The shipped reserved-package boundary, version 2, with the Ares 2 prefix list |
| Test-type annotations | `@Public`, `@Hidden`, `@Deadline`, `@StrictTimeout`, … | The same, with new imports |

The essential shape of the migration: **the test-type and lifecycle annotations survive a rename; the security annotations do not survive at all and must be re-expressed as a policy file.**

---

## 3. Prerequisites

- **Java 17** or later
- **Gradle** in a version compatible with the chosen freefair AspectJ plugin (the freefair 9.x line used here requires Gradle 9; older Gradle versions need an older freefair line), or **Maven 3.8+**
- **JUnit 5** (Jupiter)

---

## 4. Step 1: replace the dependency and wire up the build

Remove the Ares 1 dependency:

```xml
<!-- DELETE THIS -->
<dependency>
    <groupId>de.tum.in.ase</groupId>
    <artifactId>artemis-java-test-sandbox</artifactId>
    <version>1.15.0</version>
    <scope>test</scope>
</dependency>
```

```gradle
// DELETE THIS
testImplementation 'de.tum.in.ase:artemis-java-test-sandbox:1.15.0'
```

> **On the libraries Ares 1 bundled.** Ares 1 shipped JUnit 5, AssertJ and Hamcrest transitively, so exercises often relied on them without declaring them. Ares 2 still exposes JUnit and AssertJ; Hamcrest is the one that disappears. Regardless of what remains transitive, declare the test libraries your tests actually import. Relying on another library's transitive graph is what makes an upgrade break compilation for reasons unrelated to the upgrade.

Then apply one of the two sections below. Ares 2 needs four things where Ares 1 needed one: the dependency, AspectJ weaving of the student bytecode, the agent attached to the test JVM, and a set of JVM module-access flags.

### 4.1 Gradle

Add the AspectJ compiler plugin:

```gradle
plugins {
    id 'java'
    id 'io.freefair.aspectj.post-compile-weaving' version '9.2.0'
}
```

Declare the versions once, so an upgrade is a single edit:

```gradle
ext {
    aresVersion = '2.1.1'
    aspectjVersion = '1.9.25.1'
}

repositories {
    mavenCentral()
}
```

Create two dedicated, **non-transitive** configurations. Non-transitive is what makes them safe to use by file path: each resolves to exactly one JAR, so no file-name matching is involved and the wrong JAR cannot be picked up.

```gradle
configurations {
    aresAgent {
        canBeConsumed = false
        canBeResolved = true
        transitive = false
    }
    aresAspectjRuntime {
        canBeConsumed = false
        canBeResolved = true
        transitive = false
    }
}
```

Declare the dependencies:

```gradle
dependencies {
    aresAgent "de.tum.cit.ase:ares:${aresVersion}:agent"
    aresAspectjRuntime "org.aspectj:aspectjrt:${aspectjVersion}"
    testImplementation "de.tum.cit.ase:ares:${aresVersion}"
    aspect "de.tum.cit.ase:ares:${aresVersion}"
    implementation "org.aspectj:aspectjrt:${aspectjVersion}"
}
```

The `aspect` line is the one with no Ares 1 counterpart and the one most often forgotten. The AspectJ compiler only weaves binary aspects from JARs on its **aspect path**; a JAR on the ordinary compile classpath contributes nothing. Without that line the build succeeds and weaves nothing, silently.

Attach the agent through an argument provider rather than `jvmArgs`, so that dependency resolution happens when the test task runs rather than while Gradle configures the build:

```gradle
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider

abstract class AresJvmArguments implements CommandLineArgumentProvider {
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    abstract ConfigurableFileCollection getAgentJar()

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    abstract ConfigurableFileCollection getAspectjRuntimeJar()

    @Override
    Iterable<String> asArguments() {
        [
                "-javaagent:${agentJar.singleFile.absolutePath}".toString(),
                "-Xbootclasspath/a:${aspectjRuntimeJar.singleFile.absolutePath}".toString(),
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
}

def aresJvmArguments = objects.newInstance(AresJvmArguments)
aresJvmArguments.agentJar.from(configurations.aresAgent)
aresJvmArguments.aspectjRuntimeJar.from(configurations.aresAspectjRuntime)

tasks.withType(Test).configureEach {
    useJUnitPlatform()
    jvmArgumentProviders.add(aresJvmArguments)
}
```

The seventeen module-access flags are not optional and not a subset you can trim. They let the Ares advice introspect intercepted JDK objects; a partial list fails only once a policy exercises the corresponding advice, which means it fails later and more confusingly than it should.

> **Note:** the freefair plugin manages an `aspectjrt` version of its own. On an AspectJ version conflict, either drop the explicit `implementation "org.aspectj:aspectjrt:..."` line and let the plugin supply it, or align the plugin through `aspectj { version = aspectjVersion }`.

### 4.2 Maven

There is no repository step: Maven's super-POM already defines Central.

Declare the versions and an empty `argLine` property:

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <ares.version>2.1.1</ares.version>
    <aspectj.version>1.9.25.1</aspectj.version>
    <!-- Keeps @{argLine} resolvable when JaCoCo is not part of the run. -->
    <argLine></argLine>
</properties>
```

Add the dependencies. **Note the scope.**

```xml
<dependency>
    <groupId>de.tum.cit.ase</groupId>
    <artifactId>ares</artifactId>
    <version>${ares.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>${aspectj.version}</version>
</dependency>
```

Your Ares 1 dependency was `<scope>test</scope>`, and carrying that scope across is the single most common way to produce a Maven build that compiles but enforces nothing. `aspectj-maven-plugin` resolves its `<aspectLibraries>` against the **compile-visible** dependencies and weaves the *main* classes during `process-classes`, where a test-scoped artefact does not exist. It fails with `The artifact de.tum.cit.ase:ares referenced in aspectj plugin as an aspect library, is not found the project dependencies`. `provided` is visible at compile time and at test time while staying out of the packaged artefact, which is what an exercise wants.

Weave the aspects:

```xml
<plugin>
    <groupId>dev.aspectj</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.14.1</version>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjtools</artifactId>
            <version>${aspectj.version}</version>
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

`forceAjcCompile` is required because your project contains no `.aj` sources: the aspects come from the Ares JAR, and without this flag `ajc` would decline to run.

Put the agent and the AspectJ runtime at a path the build knows:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.9.0</version>
    <executions>
        <execution>
            <id>copy-ares-runtime-jars</id>
            <phase>process-test-classes</phase>
            <goals>
                <goal>copy</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>de.tum.cit.ase</groupId>
                        <artifactId>ares</artifactId>
                        <version>${ares.version}</version>
                        <classifier>agent</classifier>
                        <overWrite>true</overWrite>
                        <outputDirectory>${project.build.directory}/ares</outputDirectory>
                        <destFileName>ares-agent.jar</destFileName>
                    </artifactItem>
                    <artifactItem>
                        <groupId>org.aspectj</groupId>
                        <artifactId>aspectjrt</artifactId>
                        <version>${aspectj.version}</version>
                        <overWrite>true</overWrite>
                        <outputDirectory>${project.build.directory}/ares</outputDirectory>
                        <destFileName>aspectjrt.jar</destFileName>
                    </artifactItem>
                </artifactItems>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Do not point `-javaagent` at `${settings.localRepository}` instead. That hard-codes the repository layout, assumes the artefact has already been downloaded, and repeats the version inside a path where an upgrade is easy to miss.

Attach the agent:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.4</version>
    <configuration>
        <argLine>
            @{argLine}
            "-javaagent:${project.build.directory}/ares/ares-agent.jar"
            "-Xbootclasspath/a:${project.build.directory}/ares/aspectjrt.jar"
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

Three details that bite:

- **Pin the version.** Without `<version>`, Surefire floats with whatever the super-POM binds, and a Maven upgrade silently changes how tests are launched.
- **`@{argLine}` first.** JaCoCo's `prepare-agent` works by *setting* the `argLine` property. A plain `<argLine>` overwrites it and coverage silently reports nothing. This is also why the properties block declares an empty `<argLine></argLine>`: without it, a run without JaCoCo fails on an unresolved `@{argLine}`.
- **Quote the two paths.** `${project.build.directory}` contains a space whenever the project sits under a directory such as `My Projects`. Surefire splits on whitespace but honours double quotes.

If another agent is present, put the Ares agent **after** a coverage agent, so coverage instrumentation is applied to the classes Ares then transforms rather than the reverse.

---

## 5. Step 2: rewrite the imports

The test-type and lifecycle annotations survive the migration. Rewrite the package and nothing else:

| Ares 1 | Ares 2 |
|---|---|
| `de.tum.in.test.api.jupiter.Public` | `de.tum.cit.ase.ares.api.jupiter.Public` |
| `de.tum.in.test.api.jupiter.Hidden` | `de.tum.cit.ase.ares.api.jupiter.Hidden` |
| `de.tum.in.test.api.jupiter.PublicTest` | `de.tum.cit.ase.ares.api.jupiter.PublicTest` |
| `de.tum.in.test.api.jupiter.HiddenTest` | `de.tum.cit.ase.ares.api.jupiter.HiddenTest` |
| `de.tum.in.test.api.jqwik.Public` / `.Hidden` | `de.tum.cit.ase.ares.api.jqwik.Public` / `.Hidden` |
| `de.tum.in.test.api.Deadline` | `de.tum.cit.ase.ares.api.Deadline` |
| `de.tum.in.test.api.ExtendedDeadline` | `de.tum.cit.ase.ares.api.ExtendedDeadline` |
| `de.tum.in.test.api.ActivateHiddenBefore` | `de.tum.cit.ase.ares.api.ActivateHiddenBefore` |
| `de.tum.in.test.api.StrictTimeout` | `de.tum.cit.ase.ares.api.StrictTimeout` |
| `de.tum.in.test.api.MirrorOutput` | `de.tum.cit.ase.ares.api.MirrorOutput` |
| `de.tum.in.test.api.WithIOManager` | `de.tum.cit.ase.ares.api.WithIOManager` |
| `de.tum.in.test.api.PrivilegedExceptionsOnly` | `de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly` |
| `de.tum.in.test.api.TestUtils` | `de.tum.cit.ase.ares.api.TestUtils` |
| `de.tum.in.test.api.AresConfiguration` | `de.tum.cit.ase.ares.api.AresConfiguration` |
| `de.tum.in.test.api.localization.UseLocale` | `de.tum.cit.ase.ares.api.localization.UseLocale` |
| `de.tum.in.test.api.io.IOTester` | `de.tum.cit.ase.ares.api.io.IOTester` |

A blanket search and replace of `de.tum.in.test.api` with `de.tum.cit.ase.ares.api` handles all of these. It will also produce unresolved imports for every **security** annotation, which is the correct outcome: those have no Ares 2 counterpart and are the subject of [Section 6](#6-step-3-translate-the-security-annotations-into-a-policy-file). Delete them as you translate them, rather than before, so you do not lose the configuration they encoded.

> **Keep `@StrictTimeout`.** It is the effective timeout mechanism in Ares 2, exactly as in Ares 1. Do **not** rewrite it as a policy entry; see [Section 6.2](#62-the-mapping-table).

---

## 6. Step 3: translate the security annotations into a policy file

This is the substantive part of the migration. Ares 1 encoded its security configuration in annotations spread over test classes and methods. Ares 2 encodes it in one YAML file per scope.

### 6.1 The policy file structure

Create `src/test/resources/SecurityPolicy.yaml`:

```yaml
thisPolicyFileCompliesToThePolicyVersion: 1
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
  theSupervisedCodeUsesTheFollowingPackage: "org.example"
  theMainClassInsideThisPackageIs: "Main"
  theFollowingClassesAreTestClasses:
    - "org.example.PenguinTest"
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: [ ]
    regardingNetworkConnections: [ ]
    regardingCommandExecutions: [ ]
    regardingThreadCreations: [ ]
    regardingPackageImports: [ ]
    regardingTimeouts: [ ]
```

Three structural rules that cause immediate rejection when broken:

1. `thisPolicyFileCompliesToThePolicyVersion` is **required** and must be exactly `1`.
2. **All six** lists under `theFollowingResourceAccessesArePermitted` must be present, even when empty. `regardingTimeouts: [ ]` is required too, for the reason in [Section 6.2](#62-the-mapping-table).
3. `theFollowingClassesAreTestClasses` must be present, though it may be empty.

Pick `theFollowingProgrammingLanguageConfigurationIsUsed` from these eight values:

| Value | Build tool | Static analysis | Runtime enforcement |
|---|---|---|---|
| `JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ` | Maven | ArchUnit | AspectJ |
| `JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION` | Maven | ArchUnit | ByteBuddy agent |
| `JAVA_USING_MAVEN_WALA_AND_ASPECTJ` | Maven | WALA | AspectJ |
| `JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION` | Maven | WALA | ByteBuddy agent |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ` | Gradle | ArchUnit | AspectJ |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION` | Gradle | ArchUnit | ByteBuddy agent |
| `JAVA_USING_GRADLE_WALA_AND_ASPECTJ` | Gradle | WALA | AspectJ |
| `JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION` | Gradle | WALA | ByteBuddy agent |

ArchUnit is simpler and faster; WALA detects transitive violations through call chains. Configure both runtime mechanisms in the build regardless of which you name here, so that switching later is a policy edit rather than a build change.

The permission entries have these shapes:

```yaml
regardingFileSystemInteractions:
  - onThisPathAndAllPathsBelow: "data"
    readAllFiles: true
    overwriteAllFiles: false
    createAllFiles: false
    executeAllFiles: false
    deleteAllFiles: false

regardingNetworkConnections:
  - onTheHost: "localhost"
    onThePort: 8080
    openConnections: true
    sendData: true
    receiveData: true

regardingCommandExecutions:
  - executeTheCommand: "echo"
    withTheseArguments:
      - "hello"

regardingThreadCreations:
  - createTheFollowingNumberOfThreads: 5
    ofThisClass: "java.lang.Thread"

regardingPackageImports:
  - importTheFollowingPackage: "java.util"

regardingTimeouts:
  - timeout: 10000
```

### 6.2 The mapping table

Ares 1 and Ares 2 do not express the same things, so this is not a substitution table. The **Fidelity** column tells you how much thought each row needs.

| Ares 1 | Fidelity | Ares 2 |
|---|---|---|
| `@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest` | Faithful | Same annotations, new import |
| `@Deadline`, `@ExtendedDeadline`, `@ActivateHiddenBefore` | Faithful | Same annotations, new import |
| `@MirrorOutput`, `@WithIOManager`, `@PrivilegedExceptionsOnly`, `@UseLocale` | Faithful | Same annotations, new import |
| `@StrictTimeout` | Faithful | Same annotation, new import. **Do not** convert to `regardingTimeouts` |
| `@WhitelistPath` | Approximate | A `regardingFileSystemInteractions` entry with `onThisPathAndAllPathsBelow` |
| `PathActionLevel` | Approximate | The five independent booleans; see the conversion below |
| `@BlacklistPath` | **None** | No deny rule exists; narrow the allowance instead |
| `@AllowLocalPort` | Approximate | A `regardingNetworkConnections` entry on `localhost` |
| `@AllowThreads` | Approximate | A `regardingThreadCreations` entry |
| `@WhitelistPackage` | Approximate | A `regardingPackageImports` entry |
| `@BlacklistPackage` | **None** | No negative package rule exists |
| `@WhitelistClass` | Conditional | `theFollowingClassesAreTestClasses`, but only under the condition below |
| `@AddTrustedPackage` | **None** | Nothing. See the warning below |
| `@TrustedThreads`, `@DisableThreadGroupCheckFor` | **None** | Nothing |
| `ArtemisSecurityManager.requestThreadWhitelisting(Thread)` | **None** | Nothing |
| `ares.security.trustedpackages`, `ares.maven.ignore`, `ares.gradle.ignore`, `ares.maven.pom`, `ares.gradle.build` | **None** | Nothing; delete these system properties |

Notes on the rows that need them:

**`@StrictTimeout` and `regardingTimeouts`.** Keep the annotation. `regardingTimeouts` is parsed and validated into the policy model, but timeouts belong to the **Phobos** test-case family, which Ares 2.1.1 generates without yet dispatching it from the in-process execution path. That stage of the pipeline has not been migrated across, so a timeout expressed in the policy does not bound a test today. The list must still be present in the file, because the schema requires all six; `regardingTimeouts: [ ]` is the clearest form unless you want to record an intended value for a later release. Use `@StrictTimeout` wherever a test needs a deadline.

**`@WhitelistPath` and path types.** Only the `STARTS_WITH` path type maps naturally onto `onThisPathAndAllPathsBelow`, which is prefix-shaped by construction. `PathType.GLOB` and regular-expression variants have no counterpart. A glob such as `@WhitelistPath(value = "../course1920xyz**", type = PathType.GLOB)` must be re-expressed as one or more concrete path prefixes, and the result is usually narrower than the original, which is the safe direction.

**`PathActionLevel`.** Ares 1 levels are ordinal and cumulative, `READ < READLINK < WRITE < DELETE < EXECUTE`. For a whitelist rule, an operation is permitted when the requested level is at or below the configured level. Ares 2 instead has five independent booleans. Convert as follows, then verify against the exercise's actual behaviour:

| Ares 1 level | Ares 2 booleans to set `true` |
|---|---|
| `READ` | `readAllFiles` |
| `READLINK` | No exact equivalent; treat as `readAllFiles` and check symbolic-link behaviour explicitly |
| `WRITE` | `readAllFiles`, `overwriteAllFiles`, `createAllFiles` |
| `DELETE` | the `WRITE` set plus `deleteAllFiles` |
| `EXECUTE` | all five |

**`@BlacklistPath`.** Ares 2 is allowlist-only. A blacklist that carved an exception out of a broader whitelist has no direct form. Sometimes you can reproduce the intent by granting several narrower paths instead of the parent, but "allow this directory except one file inside it" is not representable. The common Ares 1 idiom `@WhitelistPath("target")` with `@BlacklistPath("target/test-classes")` becomes: grant only the specific subdirectories the exercise legitimately needs.

**`@AllowLocalPort`.** A fixed port becomes one `regardingNetworkConnections` entry on `localhost`. Ares 1's range-with-exclusions form has no counterpart; enumerate the ports you actually need. One special case is worth knowing: in Ares 2, `onThePort: 0` is a **wildcard** matching every port, not port zero. That makes an unrestricted Ares 1 `allowPortsAbove = 0` representable, and it makes writing `0` for anything else dangerously broad.

**`@AllowThreads`.** Ares 1 capped the number of *concurrently active* threads. Ares 2 counts threads per thread class through `createTheFollowingNumberOfThreads` and `ofThisClass`. The accounting differs, so a translated limit is an approximation, not a rename. Re-derive the number the exercise needs rather than copying `maxActiveCount`.

**`@BlacklistPackage`.** No negative package rule exists. Note additionally that the `java` prefix is always permitted as an essential package, so an Ares 1 blacklist that forbade a specific `java.*` package cannot be reproduced at all.

**`@WhitelistClass`.** Map it to `theFollowingClassesAreTestClasses` **only** when the class is instructor-owned test infrastructure that students cannot modify. An entry in that list is exempt from both the static and the runtime checks, so it is considerably stronger than an Ares 1 whitelist entry. The Ares 1 warning applies with more force here: never list a class that students can edit.

> **Do not map `@AddTrustedPackage`.** It is tempting to put its package name into `theFollowingClassesAreTestClasses`, and that is wrong in both directions at once. As [Section 6.4](#64-naming-your-test-classes-correctly) explains, the field matches exact class names, so a package name grants **no** class exemption. But it is not inert either: Ares derives a package permission from every entry by stripping the last dotted component, so an entry `"com.thirdparty.tool"` silently permits imports from `com.thirdparty`, and a two-part entry such as `"org.example"` permits the whole `org` prefix. You therefore get no exemption, plus a package allowance you did not intend. If a third-party library genuinely needs to perform a restricted operation on behalf of student code, express that as the specific resource permission it needs.

### 6.3 Consolidating additive annotations

Ares 1's whitelist annotations were `@Repeatable` and **additive**: annotations on the test class and on the test method combined, and multiple annotations of the same kind accumulated. Ares 2 does not merge. Resolution runs from the method outwards through the enclosing classes, and the **nearest** `@Policy` wins outright.

So this Ares 1 arrangement:

```java
@WhitelistPath("target")
@WhitelistPath(value = "data", level = PathActionLevel.DELETE)
public class ExerciseTest {

    @WhitelistPath("config")
    @Public
    @Test
    void oneParticularTest() { }
}
```

does not become three policy fragments. Work out the *effective* permission set for each distinct scope, then write one complete policy file per scope. In practice most exercises need a single file for the whole test class. Where one test genuinely needs more than the others, write a second complete file, containing the class-level permissions **and** the extra one, and point the method-level `@Policy` at it.

### 6.4 Naming your test classes correctly

`theFollowingClassesAreTestClasses` takes **exact fully qualified class names**. Nested classes are recognised, but only on the `$` boundary, so listing `org.example.ExerciseTest` also covers `org.example.ExerciseTest$Inner`.

Package names and package prefixes do not exempt anything. `"org.example"` does not trust the classes in `org.example`; it matches a class literally named `org.example`, which does not exist. List every test class explicitly:

```yaml
theFollowingClassesAreTestClasses:
  - "org.example.PenguinTest"
  - "org.example.AdvancedPenguinTest"
  - "org.example.util.PenguinTestHelper"
```

This is the same rule at both enforcement layers, so a mistake here fails consistently rather than intermittently: your test class is treated as supervised code, and your own assertions start tripping the policy.

---

## 7. Step 4: apply `@Policy` to the tests

Ares 1 needed no annotation to activate security; the test-type annotation was enough, and the security annotations configured it. Ares 2 keeps the test-type annotation and adds `@Policy`:

```java
package org.example;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

@Policy(value = "src/test/resources/SecurityPolicy.yaml", withinPath = "classes/org/example")
public class PenguinTest {

    @PublicTest
    void name() {
        // ...
    }
}
```

The three parameters:

| Parameter | Meaning |
|---|---|
| `value` | Path to the policy file, relative to the working directory of the test run. Blank means the policy-free configuration of [Section 9](#9-step-6-the-alternative-no-policy-file-at-all) |
| `withinPath` | Path to the compiled student bytecode, relative to the build output directory. It must start with `classes` or `test-classes` |
| `activated` | Defaults to `true`. `@Policy(activated = false)` is the explicit opt-out |

`withinPath` differs by build tool, and getting it wrong is the classic "the policy seems to have no effect" symptom:

- **Gradle:** `classes/java/main/<package/path>`, for example `classes/java/main/org/example`
- **Maven:** `classes/<package/path>`, for example `classes/org/example`

> **The activation rule that has no Ares 1 counterpart.** `@Policy` is not itself a JUnit extension and registers nothing. What activates Ares is the test-type annotation (`@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest`). A test annotated with a plain JUnit `@Test` and a `@Policy` runs completely unsupervised, silently. If you migrate a test class and drop its `@Public` in the process, you lose all enforcement without any error.

---

## 8. Step 5: replace the class-shadowing guard

Your Ares 1 exercise almost certainly contains a build-side guard preventing student classes from landing in trusted packages, as the Ares 1 documentation required. It must be replaced, not kept: the prefix list it uses names Ares 1's packages and third-party libraries that are no longer the relevant ones.

Ares 2 trusts a different set of identities by name, including its own `de.tum.cit.ase.ares.api` package, Byte Buddy, AspectJ, WALA and ArchUnit. Ares ships the executable snippets, so copy them rather than editing your old list:

- `GradleReservedPackages.gradle`
- `MavenReservedPackages.xml`
- `ReservedPackagePrefixes.txt` (the machine-readable list)

They ship inside the Ares JAR under `de/tum/cit/ase/ares/api/configuration/reservedPackages/`, and live in the Ares repository at `src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages/`. Both are reproduced in full below, so you can complete the migration without extracting them.

Two versions are pinned, and your exercise and its CI must pin both. `RESERVED_PACKAGE_PREFIX_VERSION = 1` is the prefix data. `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION = 2` is the build-side contract that enforces it.

### 8.1 Gradle

Delete the Ares 1 `forbiddenPackageFolders` list and its `test { doFirst { ... } }` assertion. Create `gradle/AresReservedPackages.gradle` with the shipped content, reproduced here in full:

```gradle
// Ares reserved-package build boundary, version 2. Apply from the exercise build.
//
// Boundary version 2 supersedes version 1, which attached the validation to
// `check` alone. Gradle's Java plugin defines `check.dependsOn test`, not the
// reverse, so `gradlew test` never ran it and a student class under a reserved
// package survived a grading run that only invoked `test`. Every Test task is
// gated as well now. An exercise still carrying a version 1 snippet is
// bypassable and must be migrated.
//
// The build descriptor and the command used to invoke it are trusted instructor
// configuration. This validates student *code*; it is not a defence against
// someone who can edit the build or choose arbitrary Gradle arguments.
//
// In a multi-project build, apply this to every project that compiles student
// code: `tasks.withType(Test)` covers only the project it is applied to.
import org.gradle.api.tasks.testing.Test

def aresReservedPackageBoundaryVersion = '2'
def aresReservedPackagePatterns = [
        'java/**', 'javax/**', 'sun/**', 'jdk/**', 'com/sun/**',
        'de/tum/cit/ase/ares/api/**', 'net/bytebuddy/**', 'org/aspectj/**',
        'com/ibm/wala/**', 'com/tngtech/archunit/**', 'anonymous/toolclasses/**', 'metatest/**'
]

tasks.register('verifyAresReservedPackagesV2') {
    dependsOn tasks.named('classes')
    // Resolved at configuration time and declared as an input, so the task body
    // touches no Project API and the build stays configuration-cache compatible.
    def studentClassesDirs = sourceSets.main.output.classesDirs
    def reservedPrefixes = aresReservedPackagePatterns.collect { it.substring(0, it.length() - 2) }
    def boundaryVersion = aresReservedPackageBoundaryVersion
    inputs.files(studentClassesDirs).withPropertyName('studentClasses')
    doLast {
        def forbidden = []
        studentClassesDirs.files.each { root ->
            if (!root.isDirectory()) {
                return
            }
            def rootPath = root.toPath()
            root.eachFileRecurse { candidate ->
                if (!candidate.isFile()) {
                    return
                }
                // Compared as a '/'-separated relative path, so the same prefixes
                // apply on Windows as on Linux and macOS.
                def relative = rootPath.relativize(candidate.toPath()).toString().replace(File.separator, '/')
                if (reservedPrefixes.any { relative.startsWith(it) }) {
                    forbidden << relative
                }
            }
        }
        if (!forbidden.isEmpty()) {
            throw new GradleException("Ares reserved-package validation ${boundaryVersion} " +
                    "rejected student output: ${forbidden.sort()}. No bypass flag is supported.")
        }
    }
}

// Both hooks are required. `check` covers `gradlew check` and `gradlew build`;
// the Test hook covers `gradlew test` and any custom Test task, which is what a
// grading run actually invokes.
tasks.named('check') { dependsOn tasks.named('verifyAresReservedPackagesV2') }
tasks.withType(Test).configureEach { dependsOn tasks.named('verifyAresReservedPackagesV2') }
```

Then apply it from your `build.gradle`:

```gradle
// Ares reserved-package build boundary, version 2.
apply from: 'gradle/AresReservedPackages.gradle'
```

It registers `verifyAresReservedPackagesV2` over `sourceSets.main.output.classesDirs` and attaches it in **two** places, on the last two lines above:

```gradle
tasks.named('check') { dependsOn tasks.named('verifyAresReservedPackagesV2') }
tasks.withType(Test).configureEach { dependsOn tasks.named('verifyAresReservedPackagesV2') }
```

Both are required. Gradle's Java plugin defines `check.dependsOn test`, **not** the reverse, so a validation hung off `check` alone is never executed by `gradlew test`, which is what a grading run invokes. That was the defect in boundary version 1.

Your Ares 1 guard, being a `doFirst` on the `test` task, did run under `gradlew test`. So this is not a regression you are introducing by migrating; the problem is only that its prefix list is now the wrong one.

In a multi-project build, apply the snippet to **every** project that compiles student code: `tasks.withType(Test)` covers only the project it is applied to.

### 8.2 Maven

Delete the `maven-enforcer-plugin` execution with the `requireFilesDontExist` rule. Add this to `<build><plugins>`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-antrun-plugin</artifactId>
  <version>3.2.0</version>
  <executions>
    <execution>
      <id>verify-ares-reserved-packages-v2</id>
      <phase>process-classes</phase>
      <goals><goal>run</goal></goals>
      <configuration>
        <target>
          <resourcecount property="ares.reserved.package.count">
            <fileset dir="${project.build.outputDirectory}">
              <include name="java/**"/><include name="javax/**"/><include name="sun/**"/>
              <include name="jdk/**"/><include name="com/sun/**"/>
              <include name="de/tum/cit/ase/ares/api/**"/><include name="net/bytebuddy/**"/>
              <include name="org/aspectj/**"/><include name="com/ibm/wala/**"/>
              <include name="com/tngtech/archunit/**"/><include name="anonymous/toolclasses/**"/>
              <include name="metatest/**"/>
            </fileset>
          </resourcecount>
          <fail message="Ares reserved-package validation 2 rejected student output. No bypass flag is supported.">
            <condition><not><equals arg1="${ares.reserved.package.count}" arg2="0"/></not></condition>
          </fail>
        </target>
      </configuration>
    </execution>
  </executions>
</plugin>
```

`process-classes` precedes `test`, so `mvn test` runs it. The Maven binding was already correct at boundary version 1; the version moves to 2 only so both build tools name the same contract.

> **What this boundary does not defend against.** The build descriptor and the command that invokes it are **trusted instructor configuration**. "No bypass flag is supported" describes the shipped snippets: they offer no opt-out of their own. It is not a claim that the check survives an adversary who controls the build. Anyone who can edit `build.gradle` or `pom.xml`, or pass `-x verifyAresReservedPackagesV2`, can remove it. The threat addressed is student **code** that declares a reserved package. Your exercise template and its CI must own the build files and the invocation.

---

## 9. Step 6: the alternative, no policy file at all

If your exercise's supervised code needs no file, network, command or thread access, you can skip Step 3 and Step 4's `value` parameter entirely. With the test-type annotation present and no `@Policy` (or a `@Policy` whose `value` is blank), Ares applies a policy-free configuration that grants none of those things.

What it restricts:

- **File system, network, command execution and thread creation: denied.** No allowance exists.
- **Package imports: restricted, not eliminated.** Ares always permits an implicit allowlist made of the essential packages it ships, the supervised package itself, and the packages of the recognised test classes. The essential list includes the `java` prefix, so all of `java.*` stays importable.
- **No default execution timeout yet.** A 10,000 ms limit is constructed, but timeouts belong to the Phobos test-case family, which Ares 2.1.1 generates without yet dispatching in-process. Add `@StrictTimeout` where a test needs a deadline.

It also fixes the modes: always ArchUnit for static analysis and AspectJ for the runtime layer, with the build tool discovered from the project. So the AspectJ weaving of [Section 4](#4-step-1-replace-the-dependency-and-wire-up-the-build) is what enforces at runtime here, and a project that is not woven falls back to the static checks alone.

That discovery step has to succeed before any of this applies. Without a policy there is no explicitly selected build tool, so a project containing both a `pom.xml` and a `build.gradle` is rejected as ambiguous, and one containing neither is rejected as unsupported. Both fail the build outright rather than falling back to the restrictive configuration.

> **This is not the Ares 1 default.** Do not reach for it because "Ares 1 denied everything until I whitelisted something". Ares 1 denied *file paths* in the absence of `@WhitelistPath`, but the other domains had their own defaults, and package access was mostly permitted: `@BlacklistPackage`'s own documentation states that by default all packages can be used apart from `java.lang.reflect` and Ares internals. Check every domain your exercise touches before concluding that the policy-free configuration matches your old behaviour.

The second reason for caution is that without a policy, Ares derives from the project what a policy would have pinned:

- **The supervised package** is chosen as the most frequent non-reserved package among the production sources, so a submission with an unexpected file distribution can shift the enforcement scope.
- **The exempt test classes** are found by scanning the discovered test source roots for annotated test classes. If students can add files beneath a test source root, they can obtain that exemption. Note that a nested test class is covered only when its enclosing class is also recognised: the scanner reports nested types in source notation (`Outer.Inner`), whereas the exemption check matches binary notation (`Outer$Inner`), so an independently detected nested class is not exempt on its own.

For a graded exercise, prefer a policy with six empty lists over no policy at all. It is equally strict and additionally pins the scope, the exempt set and the mode.

---

## 10. Verify the migration

A migration is verified when enforcement can be shown to fail for the right reason. Build a **pair** of controls in one domain:

- **Positive control:** supervised code performs an operation the policy permits. It must succeed. If it fails, enforcement is stricter than the policy says, or the policy does not say what you think.
- **Negative control:** supervised code performs an operation the policy does not permit. The test asserts the rejection. If it succeeds, enforcement is not active.

Two details decide whether this proves anything:

1. **The forbidden operation must happen in supervised code, not in the test.** A class named in `theFollowingClassesAreTestClasses` is exempt, so an operation performed by the test itself is *supposed* to succeed. Put it in the student-facing class and let the test assert the exception.
2. **The domain must permit exactly one thing, not nothing.** Ares adds a static deny-all rule only while a domain has **no** allowance. Under a fully restrictive file policy, the static analysis rejects the operation before the runtime layer is consulted, so the negative control passes even with the agent detached and the weaving switched off, and it proves nothing. One allowance makes the runtime layer authoritative for that domain.

A correct run is therefore **green** and contains an asserted rejection. Then break the setup deliberately and confirm each break is detected:

- Remove the `aspect` dependency (Gradle) or `<aspectLibraries>` (Maven) from an `_ASPECTJ` exercise. The negative control must stop rejecting.
- Remove `-javaagent` from an `_INSTRUMENTATION` exercise. Same expectation. Removing it from an `_ASPECTJ` exercise correctly changes nothing, because the aspects were woven at compile time.
- Add a class declaring `package de.tum.cit.ase.ares.api;` to the student sources. The build must fail with the reserved-package diagnostic.

Two complete, runnable exercises are available in [`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples), one per build tool. If an example passes and your migrated project does not, the difference between the two is your defect.

---

## 11. Behaviour differences you will notice

**Enforcement is no longer stack-frame based.** Ares 1's rule was that a permission required *every* frame on the stack to be whitelisted, which is why whitelisting one class could unblock an entire call chain, and why `@WhitelistClass` was so powerful. Ares 2 decides from the policy and the supervised scope. Reasoning of the form "this works because I trusted the caller" does not carry over.

**The class loader no longer needs paths whitelisted.** In Ares 1, loading one student class from another could itself be denied, producing `BAD PATH ACCESS` warnings and the standing advice that "all test setups should have some whitelisting". That whole class of problem disappears: Ares 2 does not mediate class loading through the path policy.

**Violation messages are localised.** Ares 2 renders violations in the JVM's locale, so "blocked by Ares" appears as "von Ares blockiert" on a German JVM. Assert on locale-stable content, such as the file name or the offending method, never on the prose:

```java
SecurityException violation = assertThrows(SecurityException.class,
        () -> new Penguin("Julian").readForbiddenFile());
assertTrue(violation.getMessage().contains("secret.txt"),
        "expected an Ares file-system violation naming secret.txt, was: " + violation.getMessage());
```

**Some restrictions are not policy-controlled.** Ares 2 installs fixed restrictions covering reflection, native access, JVM termination, class loading, JNDI and related domains. A policy governs the five resource domains; it cannot grant these.

**`@Deadline` and the hidden-test annotations remain available** with updated imports, so the public/hidden workflow you have carries across unchanged in shape.

---

## 12. Troubleshooting

| Problem | Cause | Solution |
|---|---|---|
| `The artifact de.tum.cit.ase:ares referenced in aspectj plugin as an aspect library, is not found the project dependencies` | Maven only: you carried `<scope>test</scope>` across from Ares 1 | Change the scope to `provided` ([Section 4.2](#42-maven)) |
| `package de.tum.in.test.api does not exist` | An Ares 1 import survived the rewrite | Search for `de.tum.in.test` across the test sources; any remaining hit is either a rename ([Section 5](#5-step-2-rewrite-the-imports)) or a security annotation to translate ([Section 6](#6-step-3-translate-the-security-annotations-into-a-policy-file)) |
| `cannot find symbol: class WhitelistPath` (and similar) | These annotations do not exist in Ares 2 | Translate them into the policy file, then delete them |
| The build succeeds but nothing is enforced | The Ares JAR is on the compile classpath but not on the **aspect path** | Add `aspect "de.tum.cit.ase:ares:..."` (Gradle) or the `<aspectLibraries>` entry (Maven). `ajc` ignores binary aspects that are not on the aspect path, so this fails silently |
| Tests pass, and nothing is restricted, and no error appears | The test carries a plain `@Test` and `@Policy` but no Ares test annotation, so the extension was never registered | Add `@Public`, `@Hidden`, `@PublicTest` or `@HiddenTest` ([Section 7](#7-step-4-apply-policy-to-the-tests)) |
| Your own test code is blocked by the policy | `theFollowingClassesAreTestClasses` names a package instead of exact class names | List every test class by its fully qualified name ([Section 6.4](#64-naming-your-test-classes-correctly)) |
| A policy is rejected on load | `thisPolicyFileCompliesToThePolicyVersion` is missing or is not `1`, or one of the six lists is absent | All six lists must be present even when empty ([Section 6.1](#61-the-policy-file-structure)) |
| A policy loads but the run fails with a supervised-code error | `theSupervisedCodeUsesTheFollowingPackage` is missing or blank. The schema tolerates that, but a **present** policy must name the package, and Ares fails closed rather than guessing it | Set the supervised package ([Section 6.1](#61-the-policy-file-structure)) |
| A `timeout` value of `0` is rejected | `timeout` must be strictly positive | Use a positive value, or leave the list empty |
| Policy seems to have no effect | Wrong `withinPath` | Gradle: `classes/java/main/<package/path>`; Maven: `classes/<package/path>` |
| A `@StrictTimeout` was replaced by `regardingTimeouts` and no longer bounds anything | Timeouts are Phobos cases, and the Phobos stage is generated but not yet dispatched in-process in Ares 2.1.1 | Restore `@StrictTimeout` ([Section 6.2](#62-the-mapping-table)) |
| `InaccessibleObjectException` at runtime | An incomplete list of module-access flags | Use the complete list from [Section 4.1](#41-gradle) or [Section 4.2](#42-maven) |
| Coverage reports nothing after the migration | A plain `<argLine>` overwrote the property JaCoCo sets | Prefix Surefire's `<argLine>` with `@{argLine}` and declare an empty `<argLine>` property |
| The reserved-package check never runs under `gradlew test` | The snippet hooks `check` alone | Use boundary version 2, which also gates every `Test` task ([Section 8.1](#81-gradle)) |
| `Ambiguous project: both Maven and Gradle descriptors are active` | The project has both a `pom.xml` and a `build.gradle`, and no policy names the build tool | Remove the descriptor you do not use, or supply a policy that names the configuration explicitly |

---

## 13. Glossary

| Term | Meaning |
|---|---|
| **Ares 1** | `de.tum.in.ase:artemis-java-test-sandbox`, the Artemis Java Test Sandbox. Enforces via a `SecurityManager` and per-test annotations. |
| **Ares 2** | `de.tum.cit.ase:ares`. Enforces via static analysis plus a bytecode-level runtime layer, configured by a policy file. |
| **Security policy** | The `SecurityPolicy.yaml` file naming the configuration, the supervised scope, the trusted test classes and the permitted resource accesses. |
| **Supervised code** | The student code subject to the policy, identified by `theSupervisedCodeUsesTheFollowingPackage`. |
| **AspectJ** | The compile-time AOP framework used for one of the two runtime enforcement mechanisms. Requires the compiler plugin during the build and `aspectjrt` on the bootstrap classpath. |
| **Aspect path** | The set of JARs `ajc` reads binary aspects from. Distinct from the compile classpath: a JAR on the classpath alone contributes no aspects. |
| **Instrumentation** | The other runtime mechanism: class bytecode modified at load time by a ByteBuddy `-javaagent`. |
| **Reserved package** | A package prefix student code may not declare, because Ares trusts that identity by name. Enforced by the build, see [Section 8](#8-step-5-replace-the-class-shadowing-guard). |
| **`withinPath`** | The path to compiled student bytecode, relative to the build output directory. Differs between Gradle and Maven. |
| **Phobos** | A test-case family covering the file-system, network and timeout domains. Ares 2.1.1 generates Phobos cases but does not yet dispatch them from the in-process execution path, so a policy timeout does not bound a test today. Use `@StrictTimeout` for a deadline. |
| **`@StrictTimeout`** | The annotation that actually bounds test execution. Applied to a test class or method, and unchanged from Ares 1 apart from its package. |
| **Positive / negative control** | The paired checks of [Section 10](#10-verify-the-migration): one permitted operation that must succeed, one forbidden operation that must be rejected. Neither alone demonstrates that enforcement works. |

---

## 14. Appendix A: complete `build.gradle`

A working version of this file, with the sources and policy it refers to, is [`examples/ares-exercise-gradle`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-gradle).

```gradle
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider

plugins {
    id 'java'
    id 'io.freefair.aspectj.post-compile-weaving' version '9.2.0'
}

ext {
    aresVersion = '2.1.1'
    aspectjVersion = '1.9.25.1'
}

repositories {
    mavenCentral()
}

configurations {
    aresAgent {
        canBeConsumed = false
        canBeResolved = true
        transitive = false
    }
    aresAspectjRuntime {
        canBeConsumed = false
        canBeResolved = true
        transitive = false
    }
}

dependencies {
    aresAgent "de.tum.cit.ase:ares:${aresVersion}:agent"
    aresAspectjRuntime "org.aspectj:aspectjrt:${aspectjVersion}"
    testImplementation "de.tum.cit.ase:ares:${aresVersion}"
    aspect "de.tum.cit.ase:ares:${aresVersion}"
    implementation "org.aspectj:aspectjrt:${aspectjVersion}"
}

abstract class AresJvmArguments implements CommandLineArgumentProvider {
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    abstract ConfigurableFileCollection getAgentJar()

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    abstract ConfigurableFileCollection getAspectjRuntimeJar()

    @Override
    Iterable<String> asArguments() {
        [
                "-javaagent:${agentJar.singleFile.absolutePath}".toString(),
                "-Xbootclasspath/a:${aspectjRuntimeJar.singleFile.absolutePath}".toString(),
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
}

def aresJvmArguments = objects.newInstance(AresJvmArguments)
aresJvmArguments.agentJar.from(configurations.aresAgent)
aresJvmArguments.aspectjRuntimeJar.from(configurations.aresAspectjRuntime)

tasks.withType(Test).configureEach {
    useJUnitPlatform()
    jvmArgumentProviders.add(aresJvmArguments)
}

// Ares reserved-package build boundary, version 2. See Section 8.1.
apply from: 'gradle/AresReservedPackages.gradle'
```

## 15. Appendix B: complete `pom.xml`

A working version of this file is [`examples/ares-exercise-maven`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-maven).

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>ares-exercise</artifactId>
    <version>1.0.0</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <ares.version>2.1.1</ares.version>
        <aspectj.version>1.9.25.1</aspectj.version>
        <!-- Keeps @{argLine} resolvable when JaCoCo is not part of the run. -->
        <argLine></argLine>
    </properties>

    <dependencies>
        <!-- provided, not test: see Section 4.2. -->
        <dependency>
            <groupId>de.tum.cit.ase</groupId>
            <artifactId>ares</artifactId>
            <version>${ares.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>${aspectj.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- 1. Weave the Ares aspects into the student classes. -->
            <plugin>
                <groupId>dev.aspectj</groupId>
                <artifactId>aspectj-maven-plugin</artifactId>
                <version>1.14.1</version>
                <dependencies>
                    <dependency>
                        <groupId>org.aspectj</groupId>
                        <artifactId>aspectjtools</artifactId>
                        <version>${aspectj.version}</version>
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

            <!-- 2. Reserved-package build boundary, version 2. Replaces the Ares 1
                    maven-enforcer-plugin rule. See Section 8.2. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-antrun-plugin</artifactId>
                <version>3.2.0</version>
                <executions>
                    <execution>
                        <id>verify-ares-reserved-packages-v2</id>
                        <phase>process-classes</phase>
                        <goals><goal>run</goal></goals>
                        <configuration>
                            <target>
                                <resourcecount property="ares.reserved.package.count">
                                    <fileset dir="${project.build.outputDirectory}">
                                        <include name="java/**"/><include name="javax/**"/><include name="sun/**"/>
                                        <include name="jdk/**"/><include name="com/sun/**"/>
                                        <include name="de/tum/cit/ase/ares/api/**"/><include name="net/bytebuddy/**"/>
                                        <include name="org/aspectj/**"/><include name="com/ibm/wala/**"/>
                                        <include name="com/tngtech/archunit/**"/><include name="anonymous/toolclasses/**"/>
                                        <include name="metatest/**"/>
                                    </fileset>
                                </resourcecount>
                                <fail message="Ares reserved-package validation 2 rejected student output. No bypass flag is supported.">
                                    <condition><not><equals arg1="${ares.reserved.package.count}" arg2="0"/></not></condition>
                                </fail>
                            </target>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- 3. Put the agent and the AspectJ runtime at a known path. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>3.9.0</version>
                <executions>
                    <execution>
                        <id>copy-ares-runtime-jars</id>
                        <phase>process-test-classes</phase>
                        <goals>
                            <goal>copy</goal>
                        </goals>
                        <configuration>
                            <artifactItems>
                                <artifactItem>
                                    <groupId>de.tum.cit.ase</groupId>
                                    <artifactId>ares</artifactId>
                                    <version>${ares.version}</version>
                                    <classifier>agent</classifier>
                                    <overWrite>true</overWrite>
                                    <outputDirectory>${project.build.directory}/ares</outputDirectory>
                                    <destFileName>ares-agent.jar</destFileName>
                                </artifactItem>
                                <artifactItem>
                                    <groupId>org.aspectj</groupId>
                                    <artifactId>aspectjrt</artifactId>
                                    <version>${aspectj.version}</version>
                                    <overWrite>true</overWrite>
                                    <outputDirectory>${project.build.directory}/ares</outputDirectory>
                                    <destFileName>aspectjrt.jar</destFileName>
                                </artifactItem>
                            </artifactItems>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- 4. Attach the agent and open the JDK internals Ares introspects. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.4</version>
                <configuration>
                    <argLine>
                        @{argLine}
                        "-javaagent:${project.build.directory}/ares/ares-agent.jar"
                        "-Xbootclasspath/a:${project.build.directory}/ares/aspectjrt.jar"
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
