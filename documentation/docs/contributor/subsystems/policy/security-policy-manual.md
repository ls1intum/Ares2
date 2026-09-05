---
title: "Security Policy Manual"
sidebar_position: 5
description: "Full reference for the Ares 2 security policy file: structure, every supported option and how each is enforced."
---

:::tip[Simple Story]
This is the full reference for the checklist, the one the board keeps open while filling one in.

Every option, what it means, and what happens when you set it.
:::

> **Audience:** IT-Education experts with no security background.
> **Scope:** All classes inside `SecurityPolicy.java`, and the `policySubComponents` package.
> **Ares Version:** 2.1.4

**Related documentation:**
- [Precompile or Postcompile](/instructor/protect-a-java-project/precompile-or-postcompile), and from there the walkthrough for your build tool
- [Security Policy Reader and Director Manual](reader-and-director.md), internal processing pipeline

---

## 1. Prerequisites

- **Java 17** or later
- **Gradle** or **Maven 3.8+** for building, with versions compatible with the AspectJ and test plugins used by the project
- **JUnit 5** (Jupiter) for test execution
- **Ares 2**

---

## 2. Purpose: What Problem Does This Solve?

When you teach programming exercises, you need to prevent students from performing dangerous operations, such as deleting files, accessing the network, or executing system commands, that could compromise your grading infrastructure or other students' work. Writing manual security tests for each operation is tedious and error-prone.

Ares 2 automates this by letting you write a simple **security policy** (a YAML configuration file) that declares exactly which operations are allowed and which are forbidden. The system then automatically generates and runs security tests that enforce your policy, catching violations before they cause damage.

This manual covers how to write these policies.

> **Static/runtime boundary:** Static ArchUnit or T. J. Watson Libraries for Analysis (WALA) rules deny an entire
> operation domain only when that domain has no allowance. Once a policy grants
> any file, network, command or thread permission, argument-sensitive enforcement
> belongs to AspectJ or instrumentation. A narrow allowance therefore removes the
> domain-wide static deny rule, while every non-matching runtime operation remains
> forbidden. The reviewed interception inventory is maintained in
> [EnforcementModel.md](enforcement-model.md).

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
| **Strategy Pattern** | `SecurityPolicyReader` → `SecurityPolicyYAMLReader`<br/>`SecurityPolicyDirector` → `SecurityPolicyJavaDirector` | Ares must support different policy file formats, currently YAML and potentially JavaScript Object Notation (JSON) or TOML in the future, and different target programming languages (currently Java, potentially Python or other languages). The Strategy pattern allows each combination to be implemented as an independent, swappable subclass without modifying the core orchestration logic, adhering to the Open/Closed Principle. |
| **Factory Method** | `createRestrictive()` on permission records | Ares needs to provide sensible default instances for permission records. These factory methods return maximally restrictive (all-denied) default instances so that callers can start from a secure baseline. |

</details>

> For detailed information on the internal processing pipeline (readers, directors, test case generation), see the [Security Policy Reader and Director Manual](reader-and-director.md).

---

## 5. Quick Start

### 5.1 Step 1: Create a Policy File

Create a file named `SecurityPolicy.yaml` in your project root:

```yaml
thisPolicyFileCompliesToThePolicyVersion: 1
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
    regardingTimeouts: []
```

This policy forbids all file, network, command and thread operations.

> **`thisPolicyFileCompliesToThePolicyVersion` is required** and must be exactly `1`. A policy file that omits it, or that declares any other value, is rejected on load.

> **On timeouts:** `regardingTimeouts` is parsed and validated into the policy model, but timeouts belong to the Phobos test-case family, whose in-process execution has not been migrated across yet, so a value there does not bound test execution in Ares 2.1.4. Use [`@StrictTimeout`](#12-glossary) where a test needs a deadline. The list must still be present, because all six lists are structurally required. See [Section 8.6](#86-timeout-permissions).

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
| `value` | `String` | `""` | The path to the security policy YAML file, **relative to the project root**. For example, `"SecurityPolicy.yaml"` refers to a file at the top level of your project. You can place policies in subdirectories, e.g., `"policies/FileIOPolicy.yaml"`. Ares 2 reads this file at test startup and configures all security restrictions accordingly. |
| `withinPath` | `String` | `""` | The path to the **compiled** student bytecode, **relative to the build output directory**. This tells Ares 2 which `.class` files to monitor and restrict. The path must match the package structure of the supervised student code. See the mapping table below. |
| `activated` | `boolean` | `true` | Whether the policy is active. Set to `false` to run in **unprotected mode** (AOP settings are reset and policy enforcement is skipped). |

> **Note:** Both `JupiterSecurityExtension` and `JqwikSecurityExtension` evaluate `activated`. Setting `@Policy(activated = false)` is the only way to disable enforcement for a supervised test.

> **Important:** The `@Policy` annotation can be placed on the **test method** or on the **test class**. A class-level annotation applies to all test methods in that class and its nested test classes. Resolution proceeds from the method through the innermost test class to its enclosing classes, so the nearest annotation takes precedence (policies are not merged).

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
| **No Supervision** | No Ares test annotation (a plain JUnit `@Test` alone counts as none), with or without `@Policy` | No Ares security code is activated at all, student code runs with no restrictions |
| **Supervision Without Policy** | `@PublicTest`/`@HiddenTest`, or `@Public`/`@Hidden` alongside a JUnit `@Test`, but **no** `@Policy` | Ares enforces a **default most-restricted policy**: file, network, command and thread access is denied, and package imports are restricted to an implicit allowlist (the essential packages, the supervised package, and the test-class packages). No execution timeout applies yet, see [Section 8.6](#86-timeout-permissions) |
| **Supervision With Policy** | `@PublicTest`/`@HiddenTest`, or `@Public`/`@Hidden` alongside a JUnit `@Test`, **and** `@Policy` | Student code can access **only allowed** supervised resources (explicit allowlist via policy) |

> **Opting out:** Once an Ares test annotation has registered the extension, the only way to disable enforcement is an explicit `@Policy(activated = false)`.

> **Note:** `@PublicTest` and `@HiddenTest` are themselves executable test annotations and need no separate `@Test`. `@Public` and `@Hidden` only mark the test type, so they must accompany a JUnit test annotation.

### 5.3 Step 3: Run the Tests

When you run the tests, Ares 2 will automatically enforce the security policy. If student code tries to do something not explicitly permitted, the test fails with a clear error message.

> **What is next:** The Quick Start above covers the minimum to get up and running. The sections below explain the security model in detail and describe every permission type you can configure.

---

## 6. Understanding Security Policies

### 6.1 The Default-Deny Approach

Ares 2's behaviour depends on whether test supervision is active and whether a `@Policy` annotation is present. Supervision is activated by an **Ares** test annotation (`@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest`), which carries the `@JupiterAresTest` meta-annotation that registers the extension. A plain JUnit `@Test` does **not** activate Ares, and neither does `@Policy`, which carries no `@ExtendWith` and registers nothing:

- **Without supervision** (no Ares test annotation present): Student code runs freely with no restrictions, whether or not a `@Policy` is present.
- **With supervision but no policy** (test annotation present, no policy annotation present): Ares 2 enforces a **default most-restricted configuration**. It detects Maven or Gradle from the project root, uses ArchUnit and AspectJ as the analysis and enforcement modes, derives the supervised scope by scanning the project, and applies `ResourceAccesses.createRestrictive()`, which denies file, network, command and thread access outright. Package imports are **restricted rather than eliminated**: Ares always permits an implicit allowlist made of the essential packages it ships (which include the `java` prefix), the supervised package itself, and the packages of the recognised test classes. `createRestrictive()` constructs a 10,000 ms limit, but that becomes a Phobos test case, and the Phobos stage is not yet dispatched in-process, so no execution timeout applies today; use `@StrictTimeout` where a deadline is needed. Static ArchUnit rules are executed immediately; runtime interception in this mode relies on AspectJ weaving, so code that is not AspectJ-woven is covered by the static checks only. The only opt-out is an explicit `@Policy(activated = false)`.
- **With supervision and a policy** (test annotation present, policy annotation present): Ares 2 enforces only the permissions explicitly listed in the policy file. Everything else is forbidden.

When you define a security policy file, you start with maximum security (everything forbidden) and selectively allow only what the exercise absolutely requires. Specifying an explicit `@Policy` annotation with a restrictive policy object (for example, `theFollowingResourceAccessesArePermitted` containing six empty lists) enforces default-deny for policy-controlled resources. This is equivalent in strictness to supervision without a policy; the difference is that an explicit `@Policy` lets you choose the configuration (build tool, analysis framework, enforcement mechanism) and selectively grant permissions.

### 6.2 What Can Be Controlled?

| Resource Type | What It Controls | Example |
|---------------|------------------|---------|
| File System | Reading, writing, creating, executing, and deleting files | Allow reading `input.txt` |
| Network | Opening connections, sending data, and receiving data | Allow connecting to `api.example.com:443` |
| Commands | Executing system commands with specific arguments | Allow running `python --version` |
| Threads | Which thread classes can be created and how many of each | Allow up to 10 `java.lang.Thread` instances |
| Packages | Importing Java packages | Allow `java.util` (including subpackages via prefix match) |
| Timeouts | Declared maximum execution time. **Not enforced by the JUnit extension path in Ares 2.1.4**; use `@StrictTimeout` for an actual deadline | Record an intended limit of 10 seconds |

### 6.3 What Cannot Be Controlled?

The security policy does not cover the following resource types. They are enforced by static analysis only (no runtime enforcement) or not covered at all:

| Resource Type | What It Controls | Example |
|---------------|------------------|---------|
| Class Loading | Dynamic loading of classes via `java.lang.ClassLoader` and subclasses | Block `URLClassLoader.loadClass()` |
| Environment | Reading/writing environment variables (`System.getenv`), system properties (`System.getProperty`/`setProperty`), and `ProcessHandle` metadata | Block `System.getenv("SECRET")` |
| Exhaustion | Denial-of-service attacks through resource exhaustion: memory, CPU, file handles, threads, disk space, infinite loops, stack overflow, fork bombs, log flooding | Block `new byte[Integer.MAX_VALUE]` |
| JNDI | JNDI lookup access via `InitialContext` and `InitialDirContext`, which are the LDAP, RMI and Domain Name System (DNS) injection paths | Block `new InitialContext().lookup("ldap://...")` |
| Java Virtual Machine (JVM) Termination | Terminating the JVM via `System.exit()`, `Runtime.exit()`, `Runtime.halt()`, and Java Development Kit (JDK) tool `main()` methods | Block `System.exit(0)` |
| Module System | JPMS module boundary crossings: internal application programming interface (API) access, `setAccessible` bypass, `Module.implAddOpens`/`implAddExports`, `MethodHandles.privateLookupIn` | Block `field.setAccessible(true)` on module-internal fields |
| Native Code | Loading native libraries (`System.loadLibrary`, `System.load`, `Runtime.loadLibrary`, `Runtime.load`) and `sun.misc.Unsafe` operations (memory allocation, CAS, direct byte buffers) | Block `System.loadLibrary("native")` |
| Reflection | Roughly 190 methods in `java.lang.reflect.*`, `java.lang.invoke.*`, `Class.forName()`, `Method.invoke()`, `Field.set()`, `Proxy.newProxyInstance()`, `sun.misc.Unsafe`, `java.lang.foreign.*` (FFI/Panama) | Block `Method.invoke(obj, args)` |
| Serialisation | Java object serialisation via `ObjectInputStream` and `ObjectOutputStream` | Block `new ObjectInputStream(stream).readObject()` |
| Test Utilities | Ares 2 test infrastructure classes that are listed in the policy as `theFollowingClassesAreTestClasses` to exempt them from security restrictions | Exempt `com.instructor.ExerciseTest` |
| Agent | JVM agent attach and instrumentation APIs: `Instrumentation` access, class redefinition/retransformation, `VirtualMachine.attach`, `loadAgent` | Block `VirtualMachine.attach(pid)` |

---

## 7. The Security Policy File

### 7.1 File Format

Security policies are written in YAML format. YAML uses indentation (spaces, not tabs) to show structure.

### 7.2 Complete Structure

```yaml
# REQUIRED: The policy format version. Must be exactly 1; any other value,
# or omitting the key, causes the policy to be rejected on load.
thisPolicyFileCompliesToThePolicyVersion: 1

regardingTheSupervisedCode:
  # REQUIRED: Which configuration to use
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION

  # REQUIRED in practice: the package containing student code. The schema tolerates
  # its absence, but a policy that is present must name it, or the run fails closed.
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

  # OPTIONAL: Behavioural test-lifecycle defaults, see 7.7
  theFollowingTestBehaviorIsConfigured:
    regardingPrivilegedExceptions:
      onlyPrivilegedExceptionsAreReported: true      # REQUIRED within this block
      theFailureMessageIs: "Test failed."            # OPTIONAL, defaults as shown
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
- **Required:** In practice yes. The YAML schema and the Java record both tolerate its absence, but once a policy is present the factory rejects a missing or blank package with a `SecurityException` rather than guessing the scope, because an undetermined scope would silently disable enforcement for the dynamic domains. Omit it only on the no-policy path, where the package is derived by scanning instead.
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

The `theMainClassInsideThisPackageIs` field identifies the main entry point class of the supervised student code. Ares 2 uses this value to configure aspect-oriented programming (AOP) advices (via `JavaAOPTestCaseSettings.mainClass`), to substitute placeholders in generated ArchUnit rule files and AOP configuration files, and to set runtime configuration parameters for test execution.

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

The `theFollowingClassesAreTestClasses` field lists the fully qualified names of test classes that belong to the instructor and should be treated as test code. These classes are exempt from security restrictions so they can freely access resources, invoke student code, and verify results. Without this exemption, test setup and assertions would themselves be blocked by the security policy.

**Field Properties:**
- **Type:** Array of strings
- **Required:** Yes (the field must be present, `SupervisedCode` rejects a `null` array, but it can be an empty array `[]` if all test code is external to the supervised package. With a policy present, the exempt test classes come **only** from this list, Ares is fail-closed here and never derives the exemption set by scanning the student-controlled project. Auto-detection by scanning happens only on the no-policy fallback path.)
- **Description:** List of fully qualified test class names that are trusted and not subject to security restrictions

**Examples:**

List every test class by its fully qualified name:
```yaml
theFollowingClassesAreTestClasses:
  - "com.instructor.ExerciseTest"
  - "com.instructor.AdvancedTest"
  - "com.instructor.utils.HelperTest"
```

**Matching:** an entry matches a class name **exactly**, or matches a nested class of it on the `$` boundary. So `"com.instructor.ExerciseTest"` covers `com.instructor.ExerciseTest$Inner`, while never matching the unrelated `com.instructor.ExerciseTestOther`. The same comparison is used by the static architecture rules and by the runtime advice, so the behaviour is identical in both layers.

> **Package names and package prefixes do not work here, and are not harmless.** An entry such as `"com.instructor"` does **not** trust the classes beneath that package; it matches a class literally named `com.instructor`, which does not exist, so it exempts nothing. The likely symptom is that your own test classes are treated as supervised code and your assertions start tripping the policy, if they fall within the supervised scope. Worse, the entry is not inert: Ares derives a permitted package from every entry by stripping the last dotted component, so `"com.instructor"` permits imports from the whole `com` prefix. List each test class by its exact fully qualified name.

---

### 7.7 Test Behaviour Configuration

The `theFollowingTestBehaviorIsConfigured` field sets policy-wide defaults for how Ares reports a test's own failure to a student, as opposed to which resources code can access. It currently has one category, `regardingPrivilegedExceptions`, mirroring what the `@PrivilegedExceptionsOnly` annotation already controls per test. A hidden test that fails for an unrelated reason shows the student its real assertion or exception message, or a generic one instead.

**Field Properties:**
- **Type:** Object (optional wrapper), containing the optional `regardingPrivilegedExceptions` object
- **Required:** No. A policy omitting this field entirely, or omitting `regardingPrivilegedExceptions` inside it, behaves exactly as if the feature were never mentioned; no student-visible behaviour changes for a policy written before this field existed.
- **Description:** Policy-wide defaults for test-lifecycle reporting behaviour, currently limited to privileged-exceptions-only reporting

**`regardingPrivilegedExceptions` sub-fields:**
- `onlyPrivilegedExceptionsAreReported` (boolean, **required** once `regardingPrivilegedExceptions` is present): the policy-wide default. `true` hides a non-privileged failure's real detail from the student; `false` (or omitting the whole category) reports it in full.
- `theFailureMessageIs` (string, optional): the message shown instead of the real failure detail when the policy enables the default. Defaults to `"Test failed."` if omitted or blank, the same default `@PrivilegedExceptionsOnly` itself uses.

**Example:**

```yaml
theFollowingTestBehaviorIsConfigured:
  regardingPrivilegedExceptions:
    onlyPrivilegedExceptionsAreReported: true
    theFailureMessageIs: "Something went wrong."
```

**Precedence:** a `@PrivilegedExceptionsOnly` annotation directly on a test method or class always wins over this policy default, whether the policy enables or disables the default, matching the nearest-annotation-wins precedent used elsewhere in this codebase. This field only takes effect for a test that carries no such annotation of its own.

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

> **Note:** In YAML, a command can be specified as a single string (e.g., `"echo hello"`) instead of the structured format. Ares uses Jackson's `@JsonCreator` to parse both formats.

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

Declares an intended maximum execution time in milliseconds. **In Ares 2.1.4 this declaration does not yet take effect**; see the note below.

```yaml
regardingTimeouts:
  - timeout: 10000
```

| Property | Type | Description |
|----------|------|-------------|
| `timeout` | number | Intended maximum execution time in milliseconds. Must be **strictly positive**; `0` is rejected. Does not yet take effect at run time, see the note below |

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

> **Not in effect yet.** `regardingTimeouts` is parsed and validated into the policy model, but the resulting limit becomes a **Phobos** test case. Phobos is the test-case family covering the file-system, network and timeout domains, and in Ares 2.1.4 it is a generation-only stage: Ares writes those cases out, but the in-process execution path used by the JUnit extension does not dispatch them yet. That migration is still in progress. A timeout expressed here therefore does not bound test execution today, whether the list is populated or empty. Use `@StrictTimeout` on the test class or method wherever a deadline is required. The list must still be present in the file, because all six resource-access lists are structurally required; `regardingTimeouts: []` is the clearest form unless you want to record an intended value for a later release.

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

3. **Test malicious scenarios:** Attempt to read unauthorised files, make network connections, etc. to verify the policy blocks them.

---

## 10. Programmatic API (Java Builder)

Besides YAML, policies can be constructed programmatically in Java using the Builder pattern. This is useful for dynamic test generation or when policies need to be computed at runtime.

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
            .theFollowingClassesAreTestClasses(List.of())
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
| `onThisPathAndAllPathsBelow` | Must not be `null` or blank | `IllegalArgumentException` with a localised message |
| `onTheHost` | Must not be `null` or blank | `IllegalArgumentException` with a localised message |
| `onThePort` | Must be between 0 and 65535 | `IllegalArgumentException` with a localised message |
| `executeTheCommand` | Must not be `null` or blank | `IllegalArgumentException` with a localised message |
| `createTheFollowingNumberOfThreads` | Must be ≥ 0 | `IllegalArgumentException` with a localised message |
| `ofThisClass` | Must not be `null` or blank | `IllegalArgumentException` with a localised message |
| `importTheFollowingPackage` | Must not be `null` or blank | `IllegalArgumentException` with a localised message |
| `timeout` | Must be **> 0** (the restrictive default is 10000 ms). Validated on load; does not yet take effect at run time, see [Section 8.6](#86-timeout-permissions) | `IllegalArgumentException` with a localised message |

When a student's code violates a policy at runtime, Ares throws a `SecurityException` with a descriptive single-line message such as:

```
Ares Security Error (Reason: Student-Code; Stage: Execution): com.student.Main.readSecrets tried to illegally read File /etc/passwd via java.nio.file.Files.readString(java.nio.file.Path) | Reason: No allow rule configured for this resource type. but was blocked by Ares.
```

### Common Mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Using **tabs** instead of spaces in YAML | YAML parse error | Use spaces only (2-space indentation recommended) |
| Wrong `withinPath` for Gradle vs. Maven | Ares analyses or instruments the wrong bytecode path, which can produce missing-class/import errors, fail-closed WALA entry-point errors, or missing runtime coverage for the intended classes | Gradle: `classes/java/main/...`, Maven: `classes/...` |
| Empty lists `[]` vs. missing field | Varying behaviour | Always include all six `regarding*` lists explicitly, even if empty |
| Agent not loaded (`-javaagent` missing) | Static analysis works but runtime enforcement does not | See the [Maven](/instructor/protect-a-java-project/postcompile/maven) or [Gradle](/instructor/protect-a-java-project/postcompile/gradle) walkthrough |
| Expecting method-level and class-level `@Policy` to combine | Only the method-level policy applies | A class-level `@Policy` applies to all test methods in the class; a method-level `@Policy` takes precedence over (does not merge with) the class-level one |

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

If a student's code calls `new FileWriter("data/output.txt")`, the test fails because `overwriteAllFiles` is `false` (the message is a single line):
```
java.lang.SecurityException: Ares Security Error (Reason: Student-Code; Stage: Execution): com.student.Main.writeOutput tried to illegally overwrite File data/output.txt via java.io.FileWriter.<init>(java.lang.String) | Reason: No allow rule configured for this resource type. but was blocked by Ares.
```

If the student's code calls `Files.readString(Path.of("/etc/shadow"))`, the test fails because `/etc/shadow` is not under the allowed path `data` (the message is a single line):
```
java.lang.SecurityException: Ares Security Error (Reason: Student-Code; Stage: Execution): com.student.Main.readSecrets tried to illegally read File /etc/shadow via java.nio.file.Files.readString(java.nio.file.Path) | Reason: No configured allow rule permits this access. but was blocked by Ares.
```

### Diagnosis Guide

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| YAML parse error on startup | Using **tabs** instead of spaces | Use spaces only (2-space indentation recommended) |
| Ares analyses or instruments the wrong bytecode path | Wrong `withinPath` for Gradle vs. Maven | Gradle: `classes/java/main/...`, Maven: `classes/...` |
| Varying behaviour with empty vs. missing fields | Some `regarding*` lists omitted instead of set to `[]` | Always include all six `regarding*` lists explicitly, even if empty |
| Static analysis works but runtime enforcement does not | Agent not loaded (`-javaagent` missing) | See the [Maven](/instructor/protect-a-java-project/postcompile/maven) or [Gradle](/instructor/protect-a-java-project/postcompile/gradle) walkthrough |
| A different policy applies than expected | Both a method-level and a class-level `@Policy` are present; the method-level one takes precedence (no merging) | Check both annotation levels; class-level `@Policy` applies to all test methods unless a method-level `@Policy` overrides it |
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

## Further reading

This page describes the policy file as an instructor writes it. For how Ares 2 reads that
file and turns it into the tests that enforce it, see the developer guide:

- [Policy Reader and Director](reader-and-director.md) — parsing the policy
  and directing test-case creation
- [Enforcement Model](enforcement-model.md) — the split between static and
  runtime responsibility, and what the boundary does not defend against
- [Test Case Factory and Builder](../securitytest/test-case-factory-and-builder.md) —
  how a policy becomes generated security tests
