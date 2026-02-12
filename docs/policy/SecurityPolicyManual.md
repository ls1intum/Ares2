# Security Policy Manual for Ares 2

> **Audience:** IT-Education experts with no security background.
> **Scope:** All classes inside `SecurityPolicy.java`, and the `policySubComponents` package.
> **Ares Version:** 2.0.1-Beta6

**Related documentation:**
- [How to Make a Project an Ares Project](../HowToMakeAProjectAnAresProject.md), project setup (build.gradle / pom.xml)
- [Security Policy Reader and Director Manual](SecurityPolicyReaderAndDirectorManual.md), internal processing pipeline

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Purpose: What Problem Does This Solve?](#2-purpose-what-problem-does-this-solve)
3. [Introduction](#3-introduction)
   1. [What is a Security Policy?](#31-what-is-a-security-policy)
4. [Architecture Overview](#4-architecture-overview)
5. [Quick Start](#5-quick-start)
   1. [Step 1: Create a Policy File](#51-step-1-create-a-policy-file)
   2. [Step 2: Apply the Policy to Your Test](#52-step-2-apply-the-policy-to-your-test)
   3. [Step 3: Run the Tests](#53-step-3-run-the-tests)
6. [Understanding Security Policies](#6-understanding-security-policies)
   1. [The Default-Deny Approach](#61-the-default-deny-approach)
   2. [What Can Be Controlled?](#62-what-can-be-controlled)
   3. [What Cannot Be Controlled?](#63-what-cannot-be-controlled)
7. [The Security Policy File](#7-the-security-policy-file)
   1. [File Format](#71-file-format)
   2. [Complete Structure](#72-complete-structure)
   3. [Configuration Options](#73-configuration-options)
   4. [Supervised Code Package](#74-supervised-code-package)
   5. [Main Class](#75-main-class)
   6. [Test Classes](#76-test-classes)
8. [Permission Types Explained](#8-permission-types-explained)
   1. [File System Permissions](#81-file-system-permissions)
   2. [Network Permissions](#82-network-permissions)
   3. [Command Permissions](#83-command-permissions)
   4. [Thread Permissions](#84-thread-permissions)
   5. [Package Permissions](#85-package-permissions)
   6. [Timeout Permissions](#86-timeout-permissions)
   7. [Internal Record: `ClassPermission`](#87-internal-record-classpermission)
9. [Best Practices](#9-best-practices)
   1. [Security Guidelines](#91-security-guidelines)
   2. [Documentation Guidelines](#92-documentation-guidelines)
   3. [Testing Your Policies](#93-testing-your-policies)
10. [Programmatic API (Java Builder)](#10-programmatic-api-java-builder)
11. [Troubleshooting](#11-troubleshooting)
12. [Glossary](#12-glossary)

---

## 1. Prerequisites

- **Java 17** or later
- **Gradle 7+** or **Maven 3.8+** for building
- **JUnit 5** (Jupiter) for test execution
- **Ares 2**

---

## 2. Purpose: What Problem Does This Solve?

When you teach programming exercises, you need to prevent students from performing dangerous operations, such as deleting files, accessing the network, or executing system commands, that could compromise your grading infrastructure or other students' work. Writing manual security tests for each operation is tedious and error-prone.

Ares 2 automates this by letting you write a simple **security policy** (a YAML configuration file) that declares exactly which operations are allowed and which are forbidden. The system then automatically generates and runs security tests that enforce your policy, catching violations before they cause damage.

This manual covers how to write these policies.

---

## 3. Introduction

### 3.1 What is a Security Policy?

A security policy is a simple configuration file that acts as an allowlist. It specifies:

- Which files can be read, written, created, or deleted
- Which network connections can be opened, and whether data can be sent or received
- Which system commands can be executed
- Which threads (by class) can be created and how many of each
- Which Java packages can be imported
- How long the code can run

**Key principle:** Everything is forbidden by default. You only grant the permissions that are absolutely necessary for the exercise.

---

## 4. Architecture Overview

The architecture follows multiple well-known software design patterns. The table below summarises them for reference, understanding these patterns is **not required** to use Ares.

<details>
<summary>Click to expand: Design Pattern Reference</summary>

| Pattern | Where it is used | Why |
|---|---|---|
| **Builder Pattern** | `SecurityPolicy`<br/>`SupervisedCode`<br/>All `*Permission` records | Ares security policies involve many optional fields (file permissions, network permissions, thread permissions, etc.). The Builder pattern lets instructors configure only the permissions they need, in any order, while guaranteeing that the resulting objects are immutable and fully validated, preventing misconfigured policies from reaching the test-generation stage. |
| **Immutable Value Objects (Java Records)** | All classes in `policySubComponents` package<br/>Key data containers like `ResourceAccesses` | Security policies must not be accidentally modified after parsing, a mutated permission list could silently weaken security enforcement. Java Records guarantee immutability, provide automatic `equals()`, `hashCode()`, and `toString()`, and make the policy data model inherently thread-safe and self-documenting. |
| **Strategy Pattern** | `SecurityPolicyReader` → `SecurityPolicyYAMLReader`<br/>`SecurityPolicyDirector` → `SecurityPolicyJavaDirector` | Ares must support different policy file formats (currently YAML, potentially JSON or TOML in the future) and different target programming languages (currently Java, potentially Python or other languages). The Strategy pattern allows each combination to be implemented as an independent, swappable subclass without modifying the core orchestration logic, adhering to the Open/Closed Principle. |
| **Factory Method** | `createRestrictive()` on permission records | Ares needs to provide sensible default instances for permission records. These factory methods return maximally restrictive (all-denied) default instances so that callers can start from a secure baseline. |

</details>

> For detailed information on the internal processing pipeline (readers, directors, test case generation), see the [Security Policy Reader and Director Manual](SecurityPolicyReaderAndDirectorManual.md).

---

## 5. Quick Start

### 5.1 Step 1: Create a Policy File

Create a file named `SecurityPolicy.yaml` in your project root:

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: "de.tum.cit.aet"
  theMainClassInsideThisPackageIs: "Main"
  theFollowingClassesAreTestClasses: []
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: []
    regardingNetworkConnections: []
    regardingCommandExecutions: []
    regardingThreadCreations: []
    regardingPackageImports: []
    regardingTimeouts:
      - timeout: 5000
```

This policy forbids all file, network, and command operations, and limits execution to 5 seconds.

### 5.2 Step 2: Apply the Policy to Your Test

```java
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import org.junit.jupiter.api.Test;

public class ExerciseTest {

    @Public
    @Policy(value = "SecurityPolicy.yaml", withinPath = "classes/java/main/com/student")
    @Test
    void testStudentSolution() {
        // Your test code here
        // Student code will be restricted by the policy
    }
}
```

The `@Policy` annotation has three parameters:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `value` | `String` | `""` | The path to the security policy YAML file, **relative to the project root**. For example, `"SecurityPolicy.yaml"` refers to a file at the top level of your project. You can also place policies in subdirectories, e.g., `"policies/FileIOPolicy.yaml"`. Ares 2 reads this file at test startup and configures all security restrictions accordingly. |
| `withinPath` | `String` | `""` | The path to the **compiled** student bytecode, **relative to the build output directory**. This tells Ares 2 which `.class` files to monitor and restrict. The path must match the package structure of the supervised student code. See the mapping table below. |
| `activated` | `boolean` | `true` | Whether the policy is active. Set to `false` to run the test in **unprotected mode** (all AOP advices are deactivated). Useful for debugging or baseline tests. |

> **Important:** Always place the `@Policy` annotation on the **test method**, not on the test class. Placing it on the class may cause the policy not to activate for individual test methods.

**`withinPath` mapping, project structure to bytecode path:**

| Build Tool | Student package | Source files location | `withinPath` value |
|---|---|---|---|
| Gradle | `com.student` | `src/main/java/com/student/` | `classes/java/main/com/student` |
| Gradle | `de.tum.cit.aet` | `src/main/java/de/tum/cit/aet/` | `classes/java/main/de/tum/cit/aet` |
| Maven | `com.student` | `src/main/java/com/student/` | `classes/com/student` |
| Maven | `de.tum.cit.aet` | `src/main/java/de/tum/cit/aet/` | `classes/de/tum/cit/aet` |

> **Rule of thumb:** Take the package name, replace dots with `/`, and prepend `classes/java/main/` (Gradle) or `classes/` (Maven).

#### Supervision and Policy Interaction

The interaction between test annotations (`@Public`, `@Hidden`, `@Test`, `@PublicTest`, `@HiddenTest`) and the `@Policy` annotation determines how strictly student code is restricted:

| Scenario | Annotations | Resource Access |
|----------|-------------|-----------------|
| **No Supervision** | No `@Test`/`@PublicTest`/`@HiddenTest` & no `@Policy` | No Ares security code is activated at all, student code runs with no restrictions |
| **Supervision Without Policy** | `@Public`/`@Hidden` + `@Test`/`@PublicTest`/`@HiddenTest` but **no** `@Policy` | Student code **cannot access** supervised resources (supervision is active, default-deny applies) |
| **Supervision With Policy** | `@Public`/`@Hidden` + `@Test`/`@PublicTest`/`@HiddenTest` **and** `@Policy` | Student code can access **only allowed** supervised resources (explicit allowlist via policy) |

### 5.3 Step 3: Run the Tests

When you run the tests, Ares 2 will automatically enforce the security policy. If student code tries to do something not explicitly permitted, the test fails with a clear error message.

---

## 6. Understanding Security Policies

### 6.1 The Default-Deny Approach

Ares 2's behaviour depends on whether test supervision is active (via `@Test`, `@PublicTest`, or `@HiddenTest`) and whether a `@Policy` annotation is present:

- **Without supervision** (no test annotation present, no policy annotation present): Student code runs freely with no restrictions.
- **With supervision but no policy** (test annotation present, no policy annotation present): Ares 2 enforces a **default-deny** security model, student code cannot access any supervised resources (file system, network, commands, threads, package imports). This typically causes tests to fail.
- **With supervision and a policy** (test annotation present, policy annotation present): Ares 2 enforces only the permissions explicitly listed in the policy file. Everything else is forbidden.

When you define a security policy file, you start with maximum security (everything forbidden) and selectively allow only what the exercise absolutely requires. Specifying an explicit `@Policy` annotation (even with empty permission lists like `theFollowingResourceAccessesArePermitted: []`) activates Ares 2's security enforcement machinery, resulting in the same default-deny behaviour as supervision without a policy. The key difference is that a policy lets you **selectively grant** permissions, whereas supervision without a policy denies everything uniformly.

### 6.2 What Can Be Controlled?

| Resource Type | What It Controls | Example |
|---------------|------------------|---------|
| File System | Reading, writing, creating, executing, and deleting files | Allow reading `input.txt` |
| Network | Opening connections, sending data, and receiving data | Allow connecting to `api.example.com:443` |
| Commands | Executing system commands with specific arguments | Allow running `python --version` |
| Threads | Which thread classes can be created and how many of each | Allow up to 10 `java.lang.Thread` instances |
| Packages | Importing Java packages | Allow `java.util` (including subpackages via prefix match) |
| Timeouts | Maximum execution time | Limit to 10 seconds |

### 6.3 What Cannot Be Controlled?

The security policy does not cover the following resource types. They are enforced by static analysis only (no runtime enforcement) or not covered at all:

| Resource Type | What It Controls | Example |
|---------------|------------------|---------|
| Class Loading | Dynamic loading of classes via `java.lang.ClassLoader` and subclasses | Block `URLClassLoader.loadClass()` |
| Environment | Reading/writing environment variables (`System.getenv`), system properties (`System.getProperty`/`setProperty`), and `ProcessHandle` metadata | Block `System.getenv("SECRET")` |
| Exhaustion | Denial-of-service attacks through resource exhaustion: memory, CPU, file handles, threads, disk space, infinite loops, stack overflow, fork bombs, log flooding | Block `new byte[Integer.MAX_VALUE]` |
| JNDI | JNDI lookup access via `InitialContext` and `InitialDirContext` (LDAP/RMI/DNS injection paths) | Block `new InitialContext().lookup("ldap://...")` |
| JVM Termination | Terminating the JVM via `System.exit()`, `Runtime.exit()`, `Runtime.halt()`, and JDK tool `main()` methods | Block `System.exit(0)` |
| Module System | JPMS module boundary crossings: internal API access, `setAccessible` bypass, `Module.implAddOpens`/`implAddExports`, `MethodHandles.privateLookupIn` | Block `field.setAccessible(true)` on module-internal fields |
| Native Code | Loading native libraries (`System.loadLibrary`, `System.load`, `Runtime.loadLibrary`) and `sun.misc.Unsafe` operations (memory allocation, CAS, direct byte buffers) | Block `System.loadLibrary("native")` |
| Reflection | 201 methods in `java.lang.reflect.*`, `java.lang.invoke.*`, `Class.forName()`, `Method.invoke()`, `Field.set()`, `Proxy.newProxyInstance()`, `sun.misc.Unsafe`, `java.lang.foreign.*` (FFI/Panama) | Block `Method.invoke(obj, args)` |
| Serialization | Java object serialization via `ObjectInputStream` and `ObjectOutputStream` | Block `new ObjectInputStream(stream).readObject()` |
| Test Utilities | Ares 2 test infrastructure classes that are listed in the policy as `theFollowingClassesAreTestClasses` to exempt them from security restrictions | Exempt `com.instructor.ExerciseTest` |
| Agent | JVM agent attach and instrumentation APIs: `Instrumentation` access, class redefinition/retransformation, `VirtualMachine.attach`, `loadAgent` | Block `VirtualMachine.attach(pid)` |

---

## 7. The Security Policy File

### 7.1 File Format

Security policies are written in YAML format. YAML uses indentation (spaces, not tabs) to show structure.

### 7.2 Complete Structure

```yaml
regardingTheSupervisedCode:
  # REQUIRED: Which configuration to use
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION

  # OPTIONAL (but strongly recommended): The package containing student code
  theSupervisedCodeUsesTheFollowingPackage: "de.tum.cit.aet"

  # OPTIONAL: The main class (if applicable)
  theMainClassInsideThisPackageIs: "Main"

  # REQUIRED: Your test classes (can be empty array if no test classes apply)
  theFollowingClassesAreTestClasses: []

  # REQUIRED: The permissions section (all sub-lists are required but can be empty)
  theFollowingResourceAccessesArePermitted:
    # REQUIRED list: File system permissions (can be empty array)
    regardingFileSystemInteractions:
      - onThisPathAndAllPathsBelow: "data"           # REQUIRED
        readAllFiles: true                            # REQUIRED
        overwriteAllFiles: false                      # REQUIRED
        createAllFiles: false                         # REQUIRED
        executeAllFiles: false                        # REQUIRED
        deleteAllFiles: false                         # REQUIRED
    
    # REQUIRED list: Network connections (can be empty array)
    regardingNetworkConnections:
      - onTheHost: "api.example.com"                  # REQUIRED
        onThePort: 443                                # REQUIRED
        openConnections: true                         # REQUIRED
        sendData: true                                # REQUIRED
        receiveData: true                             # REQUIRED
    
    # REQUIRED list: Command executions (can be empty array)
    regardingCommandExecutions:
      - executeTheCommand: "echo"                     # REQUIRED
        withTheseArguments:                           # REQUIRED (can be empty list)
          - "hello"
    
    # REQUIRED list: Thread creations (can be empty array)
    regardingThreadCreations:
      - createTheFollowingNumberOfThreads: 5         # REQUIRED
        ofThisClass: "java.lang.Thread"              # REQUIRED
    
    # REQUIRED list: Package imports (can be empty array)
    regardingPackageImports:
      - importTheFollowingPackage: "java.util"       # REQUIRED
    
    # REQUIRED list: Timeouts (can be empty array)
    regardingTimeouts:
      - timeout: 10000                               # REQUIRED (milliseconds)
```

### 7.3 Configuration Options

Choose the configuration that matches your project setup:

| Configuration | Build Tool | Architecture Analysis | Runtime Enforcement |
|---------------|------------|----------------------|---------------------|
| `JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ` | Maven | ArchUnit | AspectJ |
| `JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION` | Maven | ArchUnit | Java Agent |
| `JAVA_USING_MAVEN_WALA_AND_ASPECTJ` | Maven | WALA | AspectJ |
| `JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION` | Maven | WALA | Java Agent |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ` | Gradle | ArchUnit | AspectJ |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION` | Gradle | ArchUnit | Java Agent |
| `JAVA_USING_GRADLE_WALA_AND_ASPECTJ` | Gradle | WALA | AspectJ |
| `JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION` | Gradle | WALA | Java Agent |

### 7.4 Supervised Code Package

The `theSupervisedCodeUsesTheFollowingPackage` field specifies the root package name where the student code is located. This field helps Ares 2 identify which classes are part of the student code and should be subject to security restrictions.

**Field Properties:**
- **Type:** String
- **Required:** No (technically optional in the Java data model, but strongly recommended, without it, Ares cannot scope enforcement to the student package)
- **Description:** The fully qualified base package name of the student submission

**Examples:**

Using a shallow package prefix:
```yaml
theSupervisedCodeUsesTheFollowingPackage: "de.tum.cit.aet"
```
This matches all classes under `de.tum.cit.aet` (including `de.tum.cit.aet.solution`, `de.tum.cit.aet.util`, etc.).

Using a deep package path:
```yaml
theSupervisedCodeUsesTheFollowingPackage: "de.tum.cit.aet.solution"
```
This matches only classes under `de.tum.cit.aet.solution` and its subpackages, excluding `de.tum.cit.aet.util`.

**Subpackages:** Matching uses a **prefix match** (`startsWith`). A value like `"de.tum.cit.aet"` matches all classes in that package and all subpackages. Choose the package depth based on your exercise structure.

### 7.5 Main Class

The `theMainClassInsideThisPackageIs` field identifies the main entry point class of the supervised student code. Ares 2 uses this value to configure AOP advices (via `JavaAOPTestCaseSettings.mainClass`), to substitute placeholders in generated ArchUnit rule files and AOP configuration files, and to set runtime configuration parameters for test execution.

**Field Properties:**
- **Type:** String
- **Required:** No (if omitted, Ares 2 auto-detects by scanning all `.java` files for `public static void main(String[] args)`, preferring classes named `Main` or `Application`, and defaulting to `"Main"` if none is found)
- **Description:** The simple class name (without package prefix) of the main class

**Example:**
```yaml
theMainClassInsideThisPackageIs: "Main"
```

**Subpackages:** Not applicable, this is a simple class name (e.g., `"Main"`), not a package path. It is used alongside the supervised package name but not combined into a fully qualified name for matching purposes.

### 7.6 Test Classes

The `theFollowingClassesAreTestClasses` field lists the fully qualified names of test classes that belong to the instructor and should be treated as trusted code. These classes are exempt from security restrictions so they can freely access resources, invoke student code, and verify results. Without this exemption, test setup and assertions would themselves be blocked by the security policy.

**Field Properties:**
- **Type:** Array of strings
- **Required:** Yes (but can be an empty array `[]` if all test code is external to the supervised package; if omitted, Ares 2 auto-detects by scanning for classes annotated with `@Test`, `@PublicTest`, `@HiddenTest`, or extending `TestCase`)
- **Description:** List of fully qualified test class names that are trusted and not subject to security restrictions

**Examples:**

Using a package prefix to cover all test classes:
```yaml
theFollowingClassesAreTestClasses:
  - "com.instructor"
```
This matches all classes under `com.instructor` (including `com.instructor.ExerciseTest`, `com.instructor.utils.HelperTest`, etc.).

Using specific test classes:
```yaml
theFollowingClassesAreTestClasses:
  - "com.instructor.ExerciseTest"
  - "com.instructor.AdvancedTest"
  - "com.instructor.IntegrationTest"
```

**Subpackages:** Matching uses a **prefix match** (`startsWith`). A value like `"com.instructor"` matches all classes under that package. A value like `"com.instructor.ExerciseTest"` also matches inner classes such as `com.instructor.ExerciseTest$Inner`.

---

## 8. Permission Types Explained

### 8.1 File System Permissions

Controls what files student code can access.

```yaml
regardingFileSystemInteractions:
  - onThisPathAndAllPathsBelow: "data/input.txt"
    readAllFiles: true
    overwriteAllFiles: false
    createAllFiles: false
    executeAllFiles: false
    deleteAllFiles: false
```

| Property | Type | Description |
|----------|------|-------------|
| `onThisPathAndAllPathsBelow` | text | The path where permissions apply (can be file or folder; must not be blank) |
| `readAllFiles` | true/false | Allow reading files |
| `overwriteAllFiles` | true/false | Allow modifying existing files |
| `createAllFiles` | true/false | Allow creating new files |
| `executeAllFiles` | true/false | Allow executing files |
| `deleteAllFiles` | true/false | Allow deleting files |

**Example: Allow reading from a data folder**
```yaml
regardingFileSystemInteractions:
  - onThisPathAndAllPathsBelow: "src/main/resources/data"
    readAllFiles: true
    overwriteAllFiles: false
    createAllFiles: false
    executeAllFiles: false
    deleteAllFiles: false
```

**Example: Allow reading and writing to a temp folder**
```yaml
regardingFileSystemInteractions:
  - onThisPathAndAllPathsBelow: "temp"
    readAllFiles: true
    overwriteAllFiles: true
    createAllFiles: true
    executeAllFiles: false
    deleteAllFiles: true
```

### 8.2 Network Permissions

Controls network connections.

```yaml
regardingNetworkConnections:
  - onTheHost: "api.example.com"
    onThePort: 443
    openConnections: true
    sendData: true
    receiveData: true
```

| Property | Type | Description |
|----------|------|-------------|
| `onTheHost` | text | Hostname or IP address (must not be blank) |
| `onThePort` | number (0–65535) | Port number (validated: must be between 0 and 65535) |
| `openConnections` | true/false | Allow opening connections |
| `sendData` | true/false | Allow sending data |
| `receiveData` | true/false | Allow receiving data |

**Example 1: Allow HTTP requests to a specific API**
```yaml
regardingNetworkConnections:
  - onTheHost: "api.openweathermap.org"
    onThePort: 443
    openConnections: true
    sendData: true
    receiveData: true
```
**Example 2: Allow localhost connections for testing**
```yaml
regardingNetworkConnections:
  - onTheHost: "localhost"
    onThePort: 8080
    openConnections: true
    sendData: true
    receiveData: true
```
### 8.3 Command Permissions

Controls execution of system commands.

```yaml
regardingCommandExecutions:
  - executeTheCommand: "echo"
    withTheseArguments:
      - "hello"
```

| Property | Type | Description |
|----------|------|-------------|
| `executeTheCommand` | text | The command name (must not be blank) |
| `withTheseArguments` | list | Allowed arguments (must match exactly; can be empty list) |

> **Note:** In YAML, a command can also be specified as a single string (e.g., `"echo hello"`) instead of the structured format. Ares uses Jackson's `@JsonCreator` to parse both formats.

**Example 1: Allow running a specific Python script**
```yaml
regardingCommandExecutions:
  - executeTheCommand: "python"
    withTheseArguments:
      - "helper.py"
      - "--safe-mode"
```

**Example 2: Allow running a Maven build**
```yaml
regardingCommandExecutions:
  - executeTheCommand: "mvn"
    withTheseArguments:
      - "compile"
      - "-f"
      - "student-project/pom.xml"
```

**Example 3: Allow running a shell script without arguments**
```yaml
regardingCommandExecutions:
  - executeTheCommand: "./run_tests.sh"
    withTheseArguments: []
```

### 8.4 Thread Permissions

Controls thread creation to prevent denial-of-service attacks.

```yaml
regardingThreadCreations:
  - createTheFollowingNumberOfThreads: 5
    ofThisClass: "java.lang.Thread"
```

| Property | Type | Description |
|----------|------|-------------|
| `createTheFollowingNumberOfThreads` | number | Maximum number of threads (validated: must be ≥ 0) |
| `ofThisClass` | text | Fully qualified class name (must not be blank) |

**Example 1: Allow basic threads**
```yaml
regardingThreadCreations:
  - createTheFollowingNumberOfThreads: 10
    ofThisClass: "java.lang.Thread"
```

**Example 2: Allow multiple thread types**
```yaml
regardingThreadCreations:
  - createTheFollowingNumberOfThreads: 10
    ofThisClass: "java.lang.Thread"
  - createTheFollowingNumberOfThreads: 10
    ofThisClass: "java.util.concurrent.ThreadPoolExecutor$Worker"
  - createTheFollowingNumberOfThreads: 10
    ofThisClass: "java.util.concurrent.CompletableFuture$AsyncRun"
```

### 8.5 Package Permissions

Controls which Java packages can be imported.

```yaml
regardingPackageImports:
  - importTheFollowingPackage: "java.util"
  - importTheFollowingPackage: "java.io"
```

| Property | Type | Description |
|----------|------|-------------|
| `importTheFollowingPackage` | text | Fully qualified package name to allow importing |

> **Matching semantics:** Package matching uses a **prefix match** (`startsWith`). Listing `"java.util"` automatically permits all subpackages such as `java.util.concurrent`, `java.util.stream`, `java.util.function`, etc. You do **not** need to list subpackages separately. Be careful with short prefixes, e.g., `"java"` would permit all standard library packages.

**Example 1: Allow common standard library packages**
```yaml
regardingPackageImports:
  - importTheFollowingPackage: "java.util"
  - importTheFollowingPackage: "java.io"
  - importTheFollowingPackage: "java.nio"
```

**Example 2: Allow collections and concurrency packages**
```yaml
regardingPackageImports:
  - importTheFollowingPackage: "java.util"
  - importTheFollowingPackage: "java.util.concurrent"
  - importTheFollowingPackage: "java.util.stream"
```

### 8.6 Timeout Permissions

Sets maximum execution time in milliseconds.

```yaml
regardingTimeouts:
  - timeout: 10000
```

| Property | Type | Description |
|----------|------|-------------|
| `timeout` | number | Maximum execution time in milliseconds |

**Example 1: Short timeout for simple computations**
```yaml
regardingTimeouts:
  - timeout: 5000
```

**Example 2: Extended timeout for file processing**
```yaml
regardingTimeouts:
  - timeout: 60000
```

> **Default behaviour:** If the `regardingTimeouts` list is empty (`[]`), Ares 2 applies a **default timeout of 10,000 ms (10 seconds)**. If multiple timeout entries are listed, Ares uses the **minimum** (tightest) value.

### 8.7 Internal Record: `ClassPermission`

The `ClassPermission` record specifies a class that receives **elevated privileges**, i.e., a class that is exempt from Ares security restrictions. This is used internally by Ares to whitelist framework classes and is not typically written in YAML policy files by instructors.

| Property | Type | Description |
|----------|------|-------------|
| `className` | text | Fully qualified class name (must not be blank) |

This record exists in the `policySubComponents` package alongside the other permission types but is not exposed through the YAML configuration surface.

---

## 9. Best Practices

### 9.1 Security Guidelines

When designing security policies, consider these security design principles that depend on your decisions:

1. **Least Privilege:** Grant only the minimum permissions required for the exercise to function. Do not add permissions "just in case", each permission expands the attack surface.

2. **Defence in Depth:** Use multiple layers of restriction by limiting file paths, network hosts, command arguments, thread counts, and execution time simultaneously. No single control should be your only defence.

3. **Least Common Mechanism:** Isolate each exercise's policy configuration. Do not reuse overly permissive policies across multiple exercises, each exercise should have its own tailored policy.

4. **Psychological Acceptability:** Policies should align with how students naturally write code. If permissions are too restrictive, students struggle with legitimate operations; too permissive, security is ineffective. Balance is key.

5. **Weakest Link:** Security is only as strong as the weakest policy entry. Review all permissions critically, one overly broad network host or file path can undermine the entire policy's intent.

### 9.2 Documentation Guidelines

1. **Comment your policies:** Add comments to explain why each permission is needed.

```yaml
regardingFileSystemInteractions:
  # Students need to read the CSV data file for the exercise
  - onThisPathAndAllPathsBelow: "data/students.csv"
    readAllFiles: true
    overwriteAllFiles: false
    createAllFiles: false
    executeAllFiles: false
    deleteAllFiles: false
```

2. **Use meaningful file names:** Name your policy files descriptively.
   - `SecurityPolicy-FileIOExercise.yaml`
   - `SecurityPolicy-NetworkExercise.yaml`
   - `SecurityPolicy-RestrictiveDefault.yaml`

3. **Version control:** Keep policies in version control alongside your exercises.

### 9.3 Testing Your Policies

Before releasing an exercise:

1. **Test with a correct solution:** Ensure your reference solution works with the policy.

2. **Test common mistakes:** Try variations of incorrect solutions to ensure they are properly restricted.

3. **Test malicious scenarios:** Attempt to read unauthorized files, make network connections, etc. to verify the policy blocks them.

---

## 10. Programmatic API (Java Builder)

Besides YAML, policies can also be constructed programmatically in Java using the Builder pattern. This is useful for dynamic test generation or when policies need to be computed at runtime.

```java
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.*;

SecurityPolicy policy = SecurityPolicy.builder()
    .regardingTheSupervisedCode(
        SupervisedCode.builder()
            .theFollowingProgrammingLanguageConfigurationIsUsed(
                ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION)
            .theSupervisedCodeUsesTheFollowingPackage("de.tum.cit.aet")
            .theMainClassInsideThisPackageIs("Main")
            .theFollowingClassesAreTestClasses(new String[]{})
            .theFollowingResourceAccessesArePermitted(
                ResourceAccesses.builder()
                    .regardingFileSystemInteractions(List.of(
                        FilePermission.builder()
                            .onThisPathAndAllPathsBelow("src/main/resources/data")
                            .readAllFiles(true)
                            .overwriteAllFiles(false)
                            .createAllFiles(false)
                            .executeAllFiles(false)
                            .deleteAllFiles(false)
                            .build()
                    ))
                    .regardingNetworkConnections(List.of())
                    .regardingCommandExecutions(List.of())
                    .regardingThreadCreations(List.of())
                    .regardingPackageImports(List.of())
                    .regardingTimeouts(List.of(
                        ResourceLimitsPermission.builder()
                            .withTimeout(5000)
                            .build()
                    ))
                    .build()
            )
            .build()
    )
    .build();
```

Every record in the `policySubComponents` package provides a `builder()` method and (where applicable) a `createRestrictive()` factory method that returns a maximally restrictive default instance.

---

## 11. Troubleshooting

### Validation Rules and Error Messages

Ares 2 validates all policy fields when the YAML file is parsed. If validation fails, the test will not run and an error message is displayed.

| Field | Validation Rule | Error on Violation |
|-------|----------------|--------------------|
| `onThisPathAndAllPathsBelow` | Must not be `null` or blank | `IllegalArgumentException` with a localized message |
| `onTheHost` | Must not be `null` or blank | `IllegalArgumentException` with a localized message |
| `onThePort` | Must be between 0 and 65535 | `IllegalArgumentException` with a localized message |
| `executeTheCommand` | Must not be `null` or blank | `IllegalArgumentException` with a localized message |
| `createTheFollowingNumberOfThreads` | Must be ≥ 0 | `IllegalArgumentException` with a localized message |
| `ofThisClass` | Must not be `null` or blank | `IllegalArgumentException` with a localized message |
| `importTheFollowingPackage` | Must not be `null` or blank | `IllegalArgumentException` with a localized message |
| `timeout` | Must be ≥ 0 (default: 10000 ms) | `IllegalArgumentException` with a localized message |

When a student's code violates a policy at runtime, Ares throws a `SecurityException` with a descriptive message such as:

```
Security violation: Access to file '/etc/passwd' is not permitted.
Allowed paths: [src/main/resources/data]
```

### Common Mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Using **tabs** instead of spaces in YAML | YAML parse error | Use spaces only (2-space indentation recommended) |
| Wrong `withinPath` for Gradle vs. Maven | Policy has no effect (student code unrestricted) | Gradle: `classes/java/main/...`, Maven: `classes/...` |
| Empty lists `[]` vs. missing field | Varying behaviour | Always include all six `regarding*` lists explicitly, even if empty |
| Agent not loaded (`-javaagent` missing) | Static analysis works but runtime enforcement does not | See [How to Make a Project an Ares Project](../HowToMakeAProjectAnAresProject.md) |
| `@Policy` on class but not on test method | Policy may not activate for individual tests | Place `@Policy` on the test method for reliable activation |

### Runtime Violations

**Example: What a violation looks like in practice**

Given this policy:
```yaml
regardingFileSystemInteractions:
  - onThisPathAndAllPathsBelow: "data"
    readAllFiles: true
    overwriteAllFiles: false
    createAllFiles: false
    executeAllFiles: false
    deleteAllFiles: false
```

If a student's code calls `new FileWriter("data/output.txt")`, the test fails because `overwriteAllFiles` is `false`:
```
java.lang.SecurityException: Security violation, 
File write access to 'data/output.txt' is not permitted.
Policy allows: [read] on path 'data'. Denied operation: [write].
```

If the student's code calls `Files.readString(Path.of("/etc/shadow"))`, the test fails because `/etc/shadow` is not under the allowed path `data`:
```
Security violation: Access to file '/etc/shadow' is not permitted.
Allowed paths: [data]
```

### Diagnosis Guide

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| YAML parse error on startup | Using **tabs** instead of spaces | Use spaces only (2-space indentation recommended) |
| Policy has no effect (student code unrestricted) | Wrong `withinPath` for Gradle vs. Maven | Gradle: `classes/java/main/...`, Maven: `classes/...` |
| Varying behaviour with empty vs. missing fields | Some `regarding*` lists omitted instead of set to `[]` | Always include all six `regarding*` lists explicitly, even if empty |
| Static analysis works but runtime enforcement does not | Agent not loaded (`-javaagent` missing) | See [How to Make a Project an Ares Project](../HowToMakeAProjectAnAresProject.md) |
| Policy may not activate for individual test methods | `@Policy` placed on the class instead of the test method | Place `@Policy` on each test method for reliable activation |
| `IllegalArgumentException` when loading the policy | A required field is `null`, blank, or out of range | Check validation rules above |
| `SecurityException` at runtime in student code | Student code accesses a resource not listed in the policy | Either the policy is working as intended, or add the missing permission |

---

## 12. Glossary

| Term | Meaning |
|------|----------|
| **Security Policy** | A YAML configuration file that declares which operations student code is allowed to perform. Everything not explicitly listed is forbidden (default-deny). |
| **`@Policy` Annotation** | A JUnit annotation (`@Target({TYPE, METHOD, ANNOTATION_TYPE})`) that links a test method to a security policy YAML file and bytecode scope. Has three parameters: `value` (policy file path), `withinPath` (bytecode path), and `activated` (enable/disable; default `true`). |
| **Default-Deny** | The security model where all operations are forbidden unless explicitly permitted by the policy. |
| **`withinPath`** | The path to the compiled student bytecode, relative to the build output directory. Tells Ares which `.class` files to monitor. |
| **`ProgrammingLanguageConfiguration`** | An enum encoding the combination of build tool (Maven/Gradle), static analysis framework (ArchUnit/WALA), and runtime enforcement mechanism (AspectJ/Instrumentation). |
| **`SupervisedCode`** | The top-level record in the policy YAML that groups the language configuration, package name, main class, test classes, and permitted resource accesses. |
| **`ResourceAccesses`** | The record listing all six permission categories: file system, network, commands, threads, package imports, and timeouts. |
| **`createRestrictive()`** | A factory method available on most permission records that returns a maximally restrictive (all-denied) default instance. |
| **Java Agent** | A JVM mechanism (`-javaagent`) that allows Ares to modify class bytecode at load time for runtime enforcement. |
| **ArchUnit** | A Java library for checking architecture rules on compiled bytecode (e.g., "no class in package X may call class Y"). |
| **WALA** | A static analysis framework that builds inter-procedural call graphs to detect forbidden API usage through chains of method calls. |
| **AspectJ** | A compile-time AOP framework that weaves interception code directly into bytecode. |
| **Instrumentation (ByteBuddy)** | A runtime AOP approach using the `java.lang.instrument` API and ByteBuddy to modify class bytecode at load time. |
| **Prefix Match** | The matching strategy used by `PackagePermission`. A permitted package `"java.util"` matches any package whose name starts with that string (e.g., `java.util.concurrent`, `java.util.stream`). |