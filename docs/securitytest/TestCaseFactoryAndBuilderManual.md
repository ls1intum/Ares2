# Test Case Factory and Builder — Manual

> **Audience:** IT-Education experts with no security background.
> **Scope:** All classes inside `de.tum.cit.ase.ares.api.securitytest` — the abstract factory/builder, the Java-specific factory, and the `creator`, `essentialModel`, `executer`, `writer`, `projectScanner`, and `specific` sub-packages.
> **Ares Version:** 2.0.1-Beta6

**Related documentation:**
- [Security Policy Manual](../policy/SecurityPolicyManual.md) — how to write a security policy YAML file
- [Security Policy Reader and Director Manual](../policy/SecurityPolicyReaderAndDirectorManual.md) — how the policy is read, directed, and handed to this factory
- [How to Make a Project an Ares Project](../HowToMakeAProjectAnAresProject.md) — project setup (build.gradle / pom.xml)

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Purpose — What Problem Does This Solve?](#2-purpose--what-problem-does-this-solve)
3. [Architecture Overview](#3-architecture-overview)
4. [The Abstract Factory and Builder — `TestCaseAbstractFactoryAndBuilder`](#4-the-abstract-factory-and-builder--testcaseabstractfactoryandbuilder)
   - [4.1 Attributes](#41-attributes)
   - [4.2 Constructor — The Template Method](#42-constructor--the-template-method)
   - [4.3 Policy vs. Scanner Fallback](#43-policy-vs-scanner-fallback)
   - [4.4 Abstract Methods](#44-abstract-methods)
5. [The Java Factory — `JavaTestCaseFactoryAndBuilder`](#5-the-java-factory--javatestcasefactoryandbuilder)
   - [5.1 Write and Execute](#51-write-and-execute)
   - [5.2 The Builder API](#52-the-builder-api)
6. [Creating Test Cases — The `creator` Package](#6-creating-test-cases--the-creator-package)
   - [6.1 `Creator` Interface](#61-creator-interface)
   - [6.2 `JavaCreator` — The Core Test-Case Generator](#62-javacreator--the-core-test-case-generator)
7. [Essential Data — The `essentialModel` Package](#7-essential-data--the-essentialmodel-package)
   - [7.1 `EssentialClasses` and `EssentialPackages` Records](#71-essentialclasses-and-essentialpackages-records)
   - [7.2 `EssentialDataReader` Interface](#72-essentialdatareader-interface)
   - [7.3 `EssentialDataYAMLReader`](#73-essentialdatayamlreader)
8. [Writing Test Cases — The `writer` Package](#8-writing-test-cases--the-writer-package)
   - [8.1 `Writer` Interface](#81-writer-interface)
   - [8.2 `JavaWriter` — File Generation](#82-javawriter--file-generation)
9. [Executing Test Cases — The `executer` Package](#9-executing-test-cases--the-executer-package)
   - [9.1 `Executer` Interface](#91-executer-interface)
   - [9.2 `JavaExecuter` — Runtime Configuration and Execution](#92-javaexecuter--runtime-configuration-and-execution)
10. [Scanning the Student Project — The `projectScanner` Package](#10-scanning-the-student-project--the-projectscanner-package)
    - [10.1 `ProjectScanner` Interface](#101-projectscanner-interface)
    - [10.2 `JavaProjectScanner`](#102-javaprojectscanner)
    - [10.3 `JavaProgrammingExerciseProjectScanner`](#103-javaprogrammingexerciseprojectscanner)
11. [ArchUnit Integration — The `specific` Package](#11-archunit-integration--the-specific-package)
12. [The `StudentCompiledClassesPath` Annotation](#12-the-studentcompiledclassespath-annotation)
13. [Processing Pipeline (Overview)](#13-processing-pipeline-overview)
14. [End-to-End Example](#14-end-to-end-example)
15. [Troubleshooting](#15-troubleshooting)
16. [Glossary](#16-glossary)

---

## 1 Prerequisites

- **Java 17** or later
- **Gradle 7+** or **Maven 3.8+** for building
- **JUnit 5** (Jupiter) for test execution
- **Ares 2**

---

## 2 Purpose — What Problem Does This Solve?

The [Security Policy Reader and Director](../policy/SecurityPolicyReaderAndDirectorManual.md) reads a YAML policy file and selects the correct toolchain. But the actual **generation, serialisation, and execution** of the security test cases happens in this package.

Given a parsed `SecurityPolicy`, this package must:

1. **Discover** which packages, classes, and build tools the student project uses — either from the policy or by scanning the project directory.
2. **Load essential data** — the list of packages and classes that Ares itself needs at runtime and that must never be blocked by the security policy.
3. **Create** both architecture test cases (static bytecode checks via ArchUnit or WALA) and AOP test cases (dynamic runtime interception via AspectJ or ByteBuddy Instrumentation).
4. **Write** the generated test-case source files to disk.
5. **Execute** the test cases — first the static architecture checks, then the dynamic AOP enforcement.

Without this package, Ares would know *what* to enforce but could not turn that knowledge into runnable tests.

---

## 3 Architecture Overview

The package is structured around five sub-packages, each handling one step of the pipeline. The table below summarises the design patterns used — understanding these patterns is **not required** to use Ares.

<details>
<summary>Click to expand: Design Pattern Reference</summary>

| Pattern | Where it is used | Why |
|---|---|---|
| **Abstract Factory + Builder** | `TestCaseAbstractFactoryAndBuilder` → `JavaTestCaseFactoryAndBuilder` | The factory must produce different kinds of test artefacts (architecture tests, AOP tests, Phobos tests) for different toolchain combinations (Maven/Gradle × ArchUnit/WALA × AspectJ/Instrumentation). The Abstract Factory hides the concrete toolchain behind a uniform interface, while the Builder allows step-by-step configuration of the many required parameters (modes, paths, collaborators, policy). |
| **Template Method** | Constructor of `TestCaseAbstractFactoryAndBuilder` | The test-case creation lifecycle always follows the same steps: inject tools → resolve modes → load essential data → extract policy configuration → create test cases. The abstract base class fixes this sequence while letting the Java-specific subclass provide concrete implementations of `writeTestCases()` and `executeTestCases()`. |
| **Strategy** | `Creator`, `Writer`, `Executer`, `ProjectScanner`, `EssentialDataReader` (all interfaces) | Each step of the pipeline (creating, writing, executing, scanning, reading) must be independently swappable for different programming languages or frameworks. Defining each step as an interface with a Java-specific implementation allows future language support (e.g., Python) without modifying the abstract factory. |
| **Builder** | `JavaTestCaseFactoryAndBuilder.Builder`, `EssentialClasses.Builder`, `EssentialPackages.Builder` | The factory requires 12 parameters (5 collaborators, 2 essential-data paths, 3 modes, 1 policy, 1 project path). A builder with named setter methods prevents parameter-ordering mistakes, enforces mandatory fields via `Objects.requireNonNull` in `build()`, and makes the construction code self-documenting. |
| **Caching (Memoisation)** | `JavaCreator.cacheResult(classPath, supplier)` | Building call graphs (WALA) and importing Java classes (ArchUnit) from compiled bytecode is computationally expensive. The `ConcurrentHashMap`-based cache ensures that these operations are performed at most once per classpath, even when the factory processes multiple test cases. |
| **Immutable Value Objects (Java Records)** | `EssentialClasses`, `EssentialPackages` | The lists of essential packages and classes must not be accidentally modified after parsing — a mutated list could silently weaken security enforcement. Java Records guarantee immutability and provide automatic `equals()`, `hashCode()`, and `toString()`. |
| **Annotation-Driven Configuration** | `@StudentCompiledClassesPath` + `PathLocationProvider` | The location of compiled student classes varies between Learning Management Systems. Rather than hard-coding paths, an annotation lets instructors declare the path on their test class, and ArchUnit's `LocationProvider` SPI reads it at runtime — fully decoupling the test framework from the deployment environment. |

</details>

**Package structure:**

```
securitytest/
├── TestCaseAbstractFactoryAndBuilder.java    ← abstract base (Template Method)
└── java/
    ├── JavaTestCaseFactoryAndBuilder.java     ← concrete factory (Builder)
    ├── StudentCompiledClassesPath.java        ← annotation
    ├── creator/       ← generates test-case objects from the policy
    ├── essentialModel/ ← reads essential (always-allowed) packages/classes
    ├── executer/      ← runs architecture + AOP tests at runtime
    ├── projectScanner/ ← auto-detects build tool, packages, main class
    ├── specific/      ← ArchUnit LocationProvider integration
    └── writer/        ← serialises generated test sources to disk
```

---

## 4 The Abstract Factory and Builder — `TestCaseAbstractFactoryAndBuilder`

This is the **central orchestration class** of the entire package. Its constructor acts as a Template Method: it fixes the exact sequence of steps required to set up security test cases, while its abstract methods (`writeTestCases`, `executeTestCases`) let subclasses provide language-specific logic.

### 4.1 Attributes

The class holds five groups of attributes:

**Tool Collaborators** (injected, all `@Nonnull`):

| Attribute | Type | Responsibility |
|---|---|---|
| `creator` | `Creator` | Generates test-case objects from the policy |
| `writer` | `Writer` | Serialises generated test sources to disk |
| `executer` | `Executer` | Runs architecture and AOP tests |
| `essentialDataReader` | `EssentialDataReader` | Reads essential packages/classes from YAML |
| `projectScanner` | `ProjectScanner` | Auto-detects build tool, packages, main class |

**Modes and Paths** (resolved at construction time):

| Attribute | Type | Source |
|---|---|---|
| `buildMode` | `BuildMode` | From policy, or auto-detected by `projectScanner.scanForBuildMode()` |
| `architectureMode` | `ArchitectureMode` | From policy, or default `WALA` |
| `aopMode` | `AOPMode` | From policy, or default `INSTRUMENTATION` |
| `projectPath` | `Path` (nullable) | Injected — where the student project lives |

**Essential Data** (loaded from YAML at construction time):

| Attribute | Type | Content |
|---|---|---|
| `essentialPackagesPath` | `Path` | File system path to `EssentialPackages.yaml` |
| `essentialClassesPath` | `Path` | File system path to `EssentialClasses.yaml` |
| `essentialPackages` | `List<String>` | Aggregated list of all essential packages |
| `essentialClasses` | `List<String>` | Aggregated list of all essential classes |

**Policy-Derived Configuration** (extracted from `SecurityPolicy` or scanner fallback):

| Attribute | Type | Meaning |
|---|---|---|
| `testClasses` | `List<String>` | FQCNs of the instructor's test classes — never restricted |
| `packageName` | `String` | The student's main package — subject to the security policy |
| `mainClassInPackageName` | `String` | The student's main class (e.g., `Main`) |
| `resourceAccesses` | `ResourceAccesses` | Which I/O operations the student is allowed to perform |

**Generated Test-Case Lists** (populated by the `creator` during construction):

| Attribute | Type | Contains |
|---|---|---|
| `architectureTestCases` | `List<ArchitectureTestCase>` | Static bytecode analysis rules (ArchUnit / WALA) |
| `aopTestCases` | `List<AOPTestCase>` | Runtime interception configurations (AspectJ / Instrumentation) |
| `phobosTestCases` | `List<PhobosTestCase>` | Phobos framework test cases |

### 4.2 Constructor — The Template Method

The constructor executes a **fixed sequence** of five steps. This sequence is the same regardless of the programming language or toolchain:

```
Step 1 — Inject tools
  ├── Preconditions.checkNotNull for all 5 collaborators

Step 2 — Resolve modes
  ├── buildMode         ← explicit value || projectScanner.scanForBuildMode()
  ├── architectureMode  ← explicit value || default WALA
  └── aopMode           ← explicit value || default INSTRUMENTATION

Step 3 — Load essential data
  ├── essentialPackages ← essentialDataReader.readEssentialPackagesFrom(path)
  │                        .getEssentialPackages()
  └── essentialClasses  ← essentialDataReader.readEssentialClassesFrom(path)
                           .getEssentialClasses()

Step 4 — Extract configuration from SecurityPolicy (with scanner fallback)
  ├── testClasses           ← policy.testClasses        || projectScanner.scanForTestClasses()
  ├── packageName           ← policy.packageName        || projectScanner.scanForPackageName()
  ├── mainClassInPackageName ← policy.mainClass         || projectScanner.scanForMainClassInPackage()
  └── resourceAccesses      ← policy.resourceAccesses   || ResourceAccesses.createRestrictive()

Step 5 — Create test cases
  └── creator.createTestCases(...)  ← fills architectureTestCases,
                                       aopTestCases, phobosTestCases
```

### 4.3 Policy vs. Scanner Fallback

Every configuration value follows the same pattern:

```java
Optional.ofNullable(securityPolicy)
    .map(SecurityPolicy::regardingTheSupervisedCode)
    .map(SupervisedCode::theFollowingClassesAreTestClasses)
    .orElseGet(projectScanner::scanForTestClasses);
```

| If the policy provides… | Then | Otherwise |
|---|---|---|
| `theFollowingClassesAreTestClasses` | Uses those class names | Scans `*.java` files for `@Test` / `@Property` annotations |
| `theSupervisedCodeUsesTheFollowingPackage` | Uses that package name | Counts package declarations and picks the most frequent one |
| `theMainClassInsideThisPackageIs` | Uses that class name | Searches for `public static void main(String[])` |
| `theFollowingResourceAccessesArePermitted` | Uses those permissions | Creates a fully restrictive policy via `ResourceAccesses.createRestrictive()` |

This dual-source design allows Ares to work **without** a policy file — the scanner auto-detects everything. When a policy is present, it always takes precedence.

### 4.4 Abstract Methods

| Method | Signature | Responsibility |
|---|---|---|
| `writeTestCases` | `List<Path> writeTestCases(Path testFolderPath)` | Serialises the generated test cases to files in the given directory. Returns the list of created file paths. |
| `executeTestCases` | `void executeTestCases()` | Runs the generated architecture tests (static analysis) and AOP tests (agent configuration). |

---

## 5 The Java Factory — `JavaTestCaseFactoryAndBuilder`

This is the **concrete implementation** of `TestCaseAbstractFactoryAndBuilder` for the Java programming language. It delegates all work to the injected collaborators after casting the generic test-case lists to Java-specific types.

### 5.1 Write and Execute

**`writeTestCases(Path testFolderPath)`:** Casts the generic lists and delegates:

```java
writer.writeTestCases(buildMode, architectureMode, aopMode,
    essentialPackages, essentialClasses, testClasses,
    packageName, mainClassInPackageName,
    architectureTestCases.stream()
        .map(tc -> (JavaArchitectureTestCase) tc).toList(),
    aopTestCases.stream()
        .map(tc -> (JavaAOPTestCase) tc).toList(),
    phobosTestCases.stream()
        .map(tc -> (JavaPhobosTestCase) tc).toList(),
    testFolderPath);
```

**`executeTestCases()`:** Same casting approach, delegates to `executer.executeTestCases(...)`.

### 5.2 The Builder API

The factory is constructed via a fluent builder with 12 parameters:

```java
JavaTestCaseFactoryAndBuilder.builder()
    .creator(new JavaCreator())
    .writer(new JavaWriter())
    .executer(new JavaExecuter())
    .essentialDataReader(new EssentialDataYAMLReader())
    .projectScanner(new JavaProjectScanner())
    .essentialPackagesPath(Path.of("EssentialPackages.yaml"))
    .essentialClassesPath(Path.of("EssentialClasses.yaml"))
    .buildMode(BuildMode.GRADLE)
    .architectureMode(ArchitectureMode.WALA)
    .aopMode(AOPMode.INSTRUMENTATION)
    .securityPolicy(policy)
    .projectPath(Path.of("/path/to/project"))
    .build();
```

The first five parameters (`creator`, `writer`, `executer`, `essentialDataReader`, `projectScanner`) plus the two essential-data paths (`essentialPackagesPath`, `essentialClassesPath`) are **mandatory** — `build()` throws `NullPointerException` if any is missing. The remaining five parameters (`buildMode`, `architectureMode`, `aopMode`, `securityPolicy`, `projectPath`) are **optional** — they fall back to scanner defaults or safe defaults.

> **Note:** In practice, instructors do not call this builder directly. The `SecurityPolicyJavaDirector` (see [Reader and Director Manual](../policy/SecurityPolicyReaderAndDirectorManual.md)) constructs the factory automatically based on the policy file.

---

## 6 Creating Test Cases — The `creator` Package

### 6.1 `Creator` Interface

| Aspect | Detail |
|---|---|
| **Role** | Defines the strategy interface for generating test-case objects from a parsed security policy. Implementations populate three lists: `architectureTestCases`, `aopTestCases`, and `phobosTestCases`. |
| **Pattern** | Strategy Pattern. The interface allows different implementations per programming language (currently `JavaCreator`, potentially `PythonCreator` in the future). |
| **Key method** | `createTestCases(BuildMode, ArchitectureMode, AOPMode, List<String> essentialPackages, List<String> essentialClasses, List<String> testClasses, String packageName, String mainClassInPackageName, List<ArchitectureTestCase>, List<AOPTestCase>, List<PhobosTestCase>, ResourceAccesses, Path projectPath)` — populates the provided test-case lists in place. |

### 6.2 `JavaCreator` — The Core Test-Case Generator

| Aspect | Detail |
|---|---|
| **Implements** | `Creator` |
| **Role** | The heart of this package. Extracts bytecode metadata, computes allowed packages/classes, and generates both fixed (always-on) and variable (policy-dependent) test cases for Java projects. |
| **Pattern** | Strategy (implements `Creator`), plus internal use of Caching (Memoisation) to avoid recomputing expensive call graphs. |

**What it does step by step:**

1. **Extraction (cached):**
   - Computes the `classPath` from the `BuildMode` (Maven: `target/classes`, Gradle: `build/classes/java/main`).
   - Imports `JavaClasses` via the `ArchitectureMode` (ArchUnit's `ClassFileImporter`).
   - Builds a `CallGraph` via the `ArchitectureMode` (null for ArchUnit, WALA's `CustomCallgraphBuilder` for WALA mode).
   - All three results are cached in a `static ConcurrentHashMap` keyed by `projectPath_packageName_artifact`, so they are computed at most once per JVM run.

2. **Preparation — computing allowed packages:**
   Four sources are merged into a single `Set<PackagePermission>`:

   | Source | Rationale |
   |---|---|
   | Essential packages (from YAML) | Framework packages like `java`, `org.aspectj` — Ares infrastructure must never be blocked |
   | Policy-permitted packages | Packages the instructor explicitly allows via `regardingPackageImports` |
   | Student's own package | The student must be able to use their own code |
   | Test-class packages | Infrastructure classes like the test runner must remain accessible |

3. **Preparation — computing allowed classes:**
   Two sources are merged: `essentialClasses` ∪ `testClasses`, each wrapped in a `ClassPermission`. These classes are exempt from all restrictions.

4. **Variable test cases** (policy-dependent):
   - For each `JavaAOPTestCaseSupported` value (`FILESYSTEM_INTERACTION`, `NETWORK_CONNECTION`, `COMMAND_EXECUTION`, `THREAD_CREATION`), creates a `JavaAOPTestCase`.
   - The matching `ResourceAccesses` method is selected by enum ordinal (e.g., ordinal 0 → `regardingFileSystemInteractions()`).
   - **Automatic escalation:** If the policy declares **no** permissions for a category (e.g., no file-system permissions at all), the creator adds a `JavaArchitectureTestCase` for the same category **in addition** to the AOP test case. This provides double protection: static analysis catches forbidden API imports at the bytecode level, and the AOP agent intercepts any calls that slip through. When permissions **do** exist, only the AOP test is created (static analysis would be too coarse to distinguish allowed from forbidden calls).
   - Phobos test cases are created similarly for `FILESYSTEM_INTERACTION`, `NETWORK_CONNECTION`, and `TIMEOUT`.

5. **Fixed test cases** (always-on):
   - Certain architecture test cases are always generated regardless of the policy (e.g., `TERMINATE_JVM`). These are retrieved via `JavaArchitectureTestCaseSupported.TERMINATE_JVM.getStatic()`.

---

## 7 Essential Data — The `essentialModel` Package

This package manages the lists of packages and classes that Ares itself needs at runtime. These are **always permitted**, regardless of the security policy — without them, the test framework would block itself.

### 7.1 `EssentialClasses` and `EssentialPackages` Records

Both are isomorphic Java Records with seven sub-lists:

| Sub-list | Example content (`EssentialClasses.yaml`) | Example content (`EssentialPackages.yaml`) |
|---|---|---|
| `essentialJava*` | *(empty)* | `java` |
| `essentialArchunit*` | `de.tum.cit.ase.ares.api.architecture.java.archunit` | *(empty)* |
| `essentialWala*` | `de.tum.cit.ase.ares.api.architecture.java.wala` | *(empty)* |
| `essentialAspectJ*` | `de.tum.cit.ase.ares.api.aop.java.aspectj` | `org.aspectj`, `org.java.aspectj` |
| `essentialInstrumentation*` | `de.tum.cit.ase.ares.api.aop.java.instrumentation` | `de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut` |
| `essentialAres*` | 19 Ares classes (Creator, Writer, Executer, PolicyReader, etc.) | *(empty)* |
| `essentialJUnit*` | `de.tum.cit.ase.ares.api.jupiter` | *(empty)* |

Each record provides a `getEssentialClasses()` / `getEssentialPackages()` method that concatenates all seven sub-lists into a single flat `List<String>` using `Streams.concat()`.

Both records include a **Builder** with explicit `Objects.requireNonNull` checks on all seven fields.

### 7.2 `EssentialDataReader` Interface

| Aspect | Detail |
|---|---|
| **Role** | Defines the strategy for reading essential-data YAML files into `EssentialClasses` and `EssentialPackages` records. |
| **Pattern** | Strategy Pattern. Different implementations can read different file formats. |
| **Key methods** | `readEssentialClassesFrom(Path)` → `EssentialClasses` and `readEssentialPackagesFrom(Path)` → `EssentialPackages`. |
| **Error handling** | Provides a `default` method `throwReaderErrorMessage(identifier, parameter, exception)` that wraps the error in a `SecurityException` with a localised message via `Messages.localized(...)`. |

### 7.3 `EssentialDataYAMLReader`

| Aspect | Detail |
|---|---|
| **Implements** | `EssentialDataReader` |
| **Library** | Uses Jackson YAML via `FileTools.readYamlFile(FileTools.readFile(path), yamlClass)`. |
| **Error handling** | Four specific catch blocks, each producing a localised error message: |

| Exception | Meaning |
|---|---|
| `StreamReadException` | The YAML file has syntax errors |
| `DatabindException` | The YAML structure does not match the expected record schema |
| `UnsupportedOperationException` | The file format is not supported |
| `IOException` | The file was not found or is not readable |

---

## 8 Writing Test Cases — The `writer` Package

### 8.1 `Writer` Interface

| Aspect | Detail |
|---|---|
| **Role** | Defines the strategy for serialising generated test-case artefacts to disk. |
| **Pattern** | Strategy Pattern — one implementation per target language. |
| **Key method** | `writeTestCases(BuildMode, ArchitectureMode, AOPMode, ..., List<JavaArchitectureTestCase>, List<JavaAOPTestCase>, List<JavaPhobosTestCase>, Path testFolderPath)` → `List<Path>` |

### 8.2 `JavaWriter` — File Generation

| Aspect | Detail |
|---|---|
| **Implements** | `Writer` |
| **Role** | Generates four categories of files and writes them into the student project's test folder. |

**File categories:**

| Category | Method | What is generated |
|---|---|---|
| **Architecture files** | `createJavaArchitectureFiles()` | ArchUnit/WALA test source files. Uses the `ArchitectureMode` to determine which template files to copy and how to format them. Includes both file-system-based (FS) and non-file-system-based (non-FS) files. |
| **AOP files** | `createJavaAOPFiles()` | AspectJ aspects or ByteBuddy Instrumentation configuration files. Uses the `AOPMode` to select the appropriate templates. |
| **Localisation files** | `createLocalisationFiles()` | Copies `messages*.properties` files into the project's `src/test/resources` directory, enabling localised error messages during test execution. |
| **Phobos files** | `createPhobosFiles()` | Phobos framework configuration files for file-system, network, and timeout test cases. |

**Template mechanism (Three-Parted Pattern):**

All generated files follow the same structure:

```
Header template  → formatted with (packageName)
Body             → dynamically built from the test-case list
Footer template  → static
→ Concatenated → Written to target path
```

The `FileTools.createThreePartedFormatStringFile(...)` utility handles template expansion and file writing. This uniform approach ensures that all generated files are consistent in structure.

---

## 9 Executing Test Cases — The `executer` Package

### 9.1 `Executer` Interface

| Aspect | Detail |
|---|---|
| **Role** | Defines the strategy for executing the generated security tests against student code. |
| **Pattern** | Strategy Pattern — one implementation per target language. |
| **Key method** | `executeTestCases(BuildMode, ArchitectureMode, AOPMode, ..., List<JavaArchitectureTestCase>, List<JavaAOPTestCase>)` |

### 9.2 `JavaExecuter` — Runtime Configuration and Execution

| Aspect | Detail |
|---|---|
| **Implements** | `Executer` |
| **Role** | Configures the runtime agent and then executes all generated test cases. |

**What it does step by step:**

1. **Configure the AOP agent settings.** Seven key-value pairs are written into the agent's configuration via `JavaAOPTestCase.setJavaAdviceSettingValue(...)`:

   | Key | Value | Purpose |
   |---|---|---|
   | `buildMode` | `"MAVEN"` or `"GRADLE"` | Tells the agent which build directory to use |
   | `architectureMode` | `"ARCHUNIT"` or `"WALA"` | Tells the agent which analysis framework is active |
   | `aopMode` | `"ASPECTJ"` or `"INSTRUMENTATION"` | Tells the agent which interception mechanism is active |
   | `allowedListedPackages` | `String[]` of essential packages | Packages the agent must never intercept |
   | `allowedListedClasses` | `String[]` of essential + test classes | Classes the agent must never intercept |
   | `restrictedPackage` | Student's package name | The package whose code the agent should monitor |
   | `mainClass` | Student's main class name | The entry point the agent should focus on |

2. **Execute architecture tests.** Each `JavaArchitectureTestCase` is executed in order, performing static bytecode analysis. If the student code imports or calls a forbidden API, the test fails immediately.

3. **Execute AOP tests.** Each `JavaAOPTestCase` is executed in order, configuring the runtime interception rules. After this step, any forbidden I/O call made by the student code will be intercepted and will throw a `SecurityException`.

> **Important:** Architecture tests run **before** AOP tests. This means static violations are detected first (fast, no code execution needed), and runtime enforcement is configured second (covers dynamic behaviour).

---

## 10 Scanning the Student Project — The `projectScanner` Package

### 10.1 `ProjectScanner` Interface

Defines five scanning methods that auto-detect project metadata:

| Method | Returns | What it discovers |
|---|---|---|
| `scanForBuildMode()` | `BuildMode` | Whether the project uses Maven (`pom.xml`) or Gradle (`build.gradle`) |
| `scanForTestClasses()` | `String[]` | Fully qualified names of all classes containing `@Test` or `@Property` annotations |
| `scanForPackageName()` | `String` | The most frequently used package declaration across all `.java` files |
| `scanForMainClassInPackage()` | `String` | The class containing `public static void main(String[])` |
| `scanForTestPath()` | `Path` | The file system path to the test source directory |

### 10.2 `JavaProjectScanner`

| Aspect | Detail |
|---|---|
| **Implements** | `ProjectScanner` |
| **Technique** | Regex-based source code analysis. Walks all `.java` files under the project root and applies four compiled regex patterns. |

**Regex patterns:**

| Pattern | Matches | Used by |
|---|---|---|
| `CLASS_PATTERN` | `public [final\|abstract\|strictfp] class ClassName` | `extractClassName()` |
| `PACKAGE_PATTERN` | `package com.example.foo;` | `extractPackageName()` |
| `MAIN_METHOD_PATTERN` | `public static void main(String[] args)` (including varargs) | `extractMainClass()` |
| `TEST_ANNOTATION_PATTERN` | `@Test` or `@Property` | `extractTestClass()` |

**Scanning pipeline:**

```
ProjectSourcesFinder.findProjectSourcesPath()
  → Files.find(sourcePath, MAX_VALUE, isJavaFile)
    → Files.readString(file)
      → extractor.apply(content)
```

**`scanForPackageName()` algorithm:** Counts the frequency of every `package` declaration across all files → returns the most common one. This heuristic works because in a typical student project, the main source package appears in the majority of files.

**`scanForMainClassInPackage()` algorithm:** Collects all classes with a `main` method → prefers a class named `Main` or `Application` → otherwise returns the first match → defaults to `"Main"`.

**`scanForTestPath()` algorithm:** Checks for Gradle's custom `srcDir 'test'` → falls back to `src/test/java`.

### 10.3 `JavaProgrammingExerciseProjectScanner`

| Aspect | Detail |
|---|---|
| **Extends** | `JavaProjectScanner` |
| **Purpose** | Overrides defaults for TUM Artemis programming exercises. |

| Override | Default in `JavaProjectScanner` | Override in `JavaProgrammingExerciseProjectScanner` |
|---|---|---|
| Default package | `""` (empty string) | `"de.tum.cit.ase"` |
| Default main class | `"Main"` | `"Main"` (unchanged) |

When the base scanner finds no package or main class, these TUM-specific defaults ensure reasonable behaviour for Artemis-hosted exercises.

---

## 11 ArchUnit Integration — The `specific` Package

This package contains a single class: `PathLocationProvider`.

| Aspect | Detail |
|---|---|
| **Implements** | `com.tngtech.archunit.junit.LocationProvider` |
| **Purpose** | Tells ArchUnit where to find the compiled student `.class` files. Normally ArchUnit analyses its own classpath, but student submissions may be compiled to a custom directory (e.g., by a Learning Management System). |
| **Mechanism** | Reads the `@StudentCompiledClassesPath` annotation from the test class → converts its `value()` to a `Path` → wraps it in an ArchUnit `Location`. |
| **Error handling** | Throws a `SecurityException` if the test class is not annotated with `@StudentCompiledClassesPath`. |

---

## 12 The `StudentCompiledClassesPath` Annotation

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface StudentCompiledClassesPath {
    @Nonnull String value();  // file system path to compiled classes
}
```

| Aspect | Detail |
|---|---|
| **Target** | Test classes that use `PathLocationProvider` |
| **Retention** | `RUNTIME` — the annotation is available to ArchUnit's reflection-based `LocationProvider` at test time |
| **Usage** | `@StudentCompiledClassesPath("build/classes/java/main")` on the instructor's test class |

---

## 13 Processing Pipeline (Overview)

The following diagram shows the end-to-end flow from a `SecurityPolicy` object to enforced security tests:

```
                    SecurityPolicy (parsed record)
                           │
                           ▼
           ┌───────────────────────────────┐
           │ TestCaseAbstractFactory       │  ← Constructor (Template Method)
           │ AndBuilder                    │
           ├───────────────────────────────┤
           │ Step 1: Inject tools          │  Creator, Writer, Executer,
           │                               │  EssentialDataReader, ProjectScanner
           │ Step 2: Resolve modes         │  BuildMode, ArchitectureMode, AOPMode
           │ Step 3: Load essential data   │  ← EssentialDataYAMLReader reads YAML
           │ Step 4: Extract config        │  ← from Policy or Scanner fallback
           │ Step 5: Create test cases     │  ← JavaCreator generates tests
           └───────────┬───────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   Architecture    AOP Test      Phobos Test
   TestCases       Cases         Cases
        │              │              │
        └──────────────┼──────────────┘
                       │
          ┌────────────┴────────────┐
          ▼                         ▼
   writeTestCases()          executeTestCases()
          │                         │
          ▼                         ▼
   JavaWriter                 JavaExecuter
   ├─ Architecture files      ├─ Configure agent (7 settings)
   ├─ AOP files               ├─ Run architecture tests (static)
   ├─ Localisation files      └─ Run AOP tests (dynamic)
   └─ Phobos files
          │                         │
          ▼                         ▼
   List<Path>               SecurityException if
   (generated files)         student code violates policy
```

---

## 14 End-to-End Example

**1. Instructor writes `SecurityConfiguration.yaml`:**

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: com.student
  theMainClassInsideThisPackageIs: "Main"
  theFollowingClassesAreTestClasses:
    - com.student.test.SecurityTest
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: []
    regardingNetworkConnections: []
    regardingCommandExecutions: []
    regardingThreadCreations: []
    regardingPackageImports: []
    regardingTimeouts:
      - timeout: 5000
```

**2. Instructor writes a JUnit test:**

```java
import de.tum.cit.ase.ares.api.Policy;
import org.junit.jupiter.api.Test;

class SecurityTest {
    @Test
    @Policy(value = "SecurityConfiguration.yaml",
            withinPath = "classes/java/main/com/student")
    void studentCodeMustNotAccessFileSystem() {
        com.student.Main.main(new String[]{});
    }
}
```

**3. What happens inside this package at runtime:**

1. The `SecurityPolicyJavaDirector` calls `JavaTestCaseFactoryAndBuilder.builder()` with all 12 parameters.
2. The `TestCaseAbstractFactoryAndBuilder` constructor begins:
   - **Mode Resolution:** `BuildMode.GRADLE`, `ArchitectureMode.WALA`, `AOPMode.INSTRUMENTATION`.
   - **Essential Data:** `EssentialDataYAMLReader` reads `EssentialPackages.yaml` (→ `java`, `org.aspectj`, etc.) and `EssentialClasses.yaml` (→ 19 Ares classes).
   - **Policy Extraction:** `packageName = "com.student"`, `mainClass = "Main"`, `testClasses = ["com.student.test.SecurityTest"]`, `resourceAccesses` = all empty (fully restrictive).
3. `JavaCreator.createTestCases()` runs:
   - Computes the classpath: `build/classes/java/main`.
   - Imports `JavaClasses` and builds a WALA `CallGraph` (cached).
   - Computes allowed packages: `{java, org.aspectj, com.student, com.student.test}`.
   - **Variable test cases:** Since all resource-access lists are empty, for each category (FS, Network, Command, Thread) both an `ArchitectureTestCase` **and** an `AOPTestCase` are created (automatic escalation).
   - **Fixed test cases:** `TERMINATE_JVM` architecture test (always-on).
4. `JavaWriter.writeTestCases()` serialises ArchUnit test files, Instrumentation configuration, localisation properties, and Phobos configs into `src/test/java/com/student/`.
5. `JavaExecuter.executeTestCases()`:
   - Configures the ByteBuddy agent with `restrictedPackage = "com.student"` and `allowedListedClasses`.
   - Runs the WALA-based architecture tests → if `com.student.Main` calls `java.io.File` anywhere in the call graph, the test fails.
   - Configures the AOP interception rules → when `Main.main()` runs, any `File.read()` call is intercepted and throws `SecurityException`.

---

## 15 Troubleshooting

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| `NullPointerException` in `JavaTestCaseFactoryAndBuilder.build()` | A mandatory builder parameter (creator, writer, executer, essentialDataReader, projectScanner, or an essential-data path) was not set | Ensure all 7 mandatory parameters are set before calling `build()` |
| `SecurityException` mentioning "essential classes path must not be null" | The path to `EssentialClasses.yaml` was not provided | Verify that the director or builder sets `essentialClassesPath` and `essentialPackagesPath` |
| `SecurityException` from `EssentialDataYAMLReader` — "read failed" or "data bind failed" | The `EssentialClasses.yaml` or `EssentialPackages.yaml` file is malformed or missing | Check that the YAML files exist at the expected classpath location and have the correct schema (7 list fields each) |
| Architecture tests pass but runtime enforcement is missing | The Java agent JAR is not loaded via `-javaagent` | See [How to Make a Project an Ares Project](../HowToMakeAProjectAnAresProject.md) for agent setup |
| Scanner detects the wrong package name | The most-frequent-package heuristic picks a utility package instead of the student's main package | Specify `theSupervisedCodeUsesTheFollowingPackage` explicitly in the security policy YAML |
| Scanner finds no test classes | Java source files do not contain `@Test` or `@Property` annotations, or files are not under the project sources path | Specify `theFollowingClassesAreTestClasses` explicitly in the security policy YAML |
| `SecurityException` from `PathLocationProvider` — "can only be used on classes annotated with…" | The test class using `PathLocationProvider` is missing the `@StudentCompiledClassesPath` annotation | Add `@StudentCompiledClassesPath("build/classes/java/main")` to the test class |
| Call-graph analysis is slow | WALA call-graph construction is computationally expensive for large projects | Switch to `ArchitectureMode.ARCHUNIT` (rule-based, faster but less precise), or ensure the cache is not invalidated between runs |

---

## 16 Glossary

| Term | Meaning |
|------|---------|
| **Abstract Factory** | A design pattern that provides an interface for creating families of related objects (here: architecture tests and AOP tests) without specifying their concrete classes. |
| **Template Method** | A design pattern where an abstract class defines the skeleton of an algorithm in a method, deferring some steps to subclasses. Here, the constructor of `TestCaseAbstractFactoryAndBuilder` fixes the 5-step initialisation sequence. |
| **Strategy Pattern** | A design pattern that defines a family of interchangeable algorithms. Here: `Creator`, `Writer`, `Executer`, `ProjectScanner`, and `EssentialDataReader` are all strategy interfaces with swappable implementations. |
| **AOP (Aspect-Oriented Programming)** | A technique for intercepting method calls at defined points ("pointcuts") without modifying the original code. Ares uses AOP to block forbidden I/O operations at runtime. |
| **ArchUnit** | A Java library for checking architecture rules on compiled bytecode (e.g., "no class in package X may call class Y"). |
| **WALA** | A static analysis framework that builds inter-procedural call graphs to detect forbidden API usage even through chains of method calls. |
| **AspectJ** | A compile-time AOP framework that weaves interception code directly into bytecode. |
| **Instrumentation (Java Agent)** | A runtime AOP approach using the `java.lang.instrument` API and ByteBuddy. A Java agent modifies class bytecode at load time. |
| **ByteBuddy** | A library for creating and modifying Java classes at runtime, used by Ares to implement the instrumentation agent. |
| **Phobos** | An additional test framework within Ares that generates test cases for file-system interactions, network connections, and timeout enforcement. |
| **Essential Packages / Classes** | Packages and classes that Ares itself needs at runtime (e.g., JUnit, ArchUnit, ByteBuddy, Ares internals). These are always permitted, regardless of the security policy, to prevent the test framework from blocking itself. |
| **Automatic Escalation** | When the security policy declares no permissions for a resource category (e.g., no file-system permissions), the `JavaCreator` generates both an architecture test (static) and an AOP test (dynamic) for that category, providing double protection. |
| **Three-Parted File** | A generated file consisting of a Header template, a dynamically built Body, and a Footer template. Used by the `Writer` to produce all test-case source files. |
| **`ResourceAccesses`** | A record from the `policy` package that holds six lists of permissions: file-system, network, command, thread, package-import, and timeout. The factory uses this record to decide which test cases to generate. |
