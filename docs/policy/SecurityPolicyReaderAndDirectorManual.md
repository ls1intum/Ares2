# Security Policy Reader and Director, Manual

> **Audience:** IT-Education experts with no security background.
> **Scope:** All classes inside `SecurityPolicyReaderAndDirector.java`, the `reader` and `director` packages.
> **Ares Version:** 2.1.0

**Related documentation:**
- [Security Policy Manual](SecurityPolicyManual.md), how to write a security policy YAML file
- [How to Make a Project an Ares Project](../HowToMakeAProjectAnAresProject.md), project setup (build.gradle / pom.xml)

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Purpose — What Problem Does This Solve?](#2-purpose--what-problem-does-this-solve)
3. [Architecture Overview](#3-architecture-overview)
4. [The User Input, Security Policy YAML File](#4-the-user-input-security-policy-yaml-file)
5. [Reading the Policy — The `reader` Package](#5-reading-the-policy--the-reader-package)
   - [5.1. SecurityPolicyReader (Abstract Class)](#51-securitypolicyreader-abstract-class)
   - [5.2. SecurityPolicyYAMLReader (Concrete Class)](#52-securitypolicyyamlreader-concrete-class)
6. [Directing Test-Case Creation — The `director` Package](#6-directing-test-case-creation--the-director-package)
   - [6.1. SecurityPolicyDirector (Abstract Class)](#61-securitypolicydirector-abstract-class)
   - [6.2. SecurityPolicyJavaDirector (Concrete Class)](#62-securitypolicyjavadirector-concrete-class)
7. [Orchestration — SecurityPolicyReaderAndDirector](#7-orchestration--securitypolicyreaderanddirector)
   - [7.1. Construction](#71-construction)
   - [7.2. Three-Step Workflow](#72-three-step-workflow)
   - [7.3. Null-Safety Strategy](#73-null-safety-strategy)
8. [Processing Pipeline (Overview)](#8-processing-pipeline-overview)
9. [End-to-End Example](#9-end-to-end-example)
10. [Integration with JUnit Extensions](#10-integration-with-junit-extensions)
11. [Troubleshooting](#11-troubleshooting)
12. [Glossary](#12-glossary)

---

## 1. Prerequisites

- **Java 17** or later
- **Gradle** or **Maven 3.8+** for building, with versions compatible with the AspectJ and test plugins used by the project
- **JUnit 5** (Jupiter) for test execution
- **Ares 2**

---

## 2. Purpose — What Problem Does This Solve?

When students submit programming exercises, instructors need to make sure the submitted code does **not** perform dangerous operations (e.g. deleting files, opening network connections, executing shell commands). Ares 2 automates this by:

1. Letting the instructor write a **security policy file** (currently YAML) that declares which operations are allowed and which are forbidden.
2. **Reading** that file into an in-memory data model.
3. **Directing** (orchestrating) the automatic generation, writing, and execution of **security test cases** that enforce the policy.

A **security test case** is either:
- An **Architecture test** that statically analyses student bytecode to check whether it calls forbidden APIs (e.g., `java.io.File`, `Runtime.exec()`). These tests use ArchUnit (rule-based) or WALA (call-graph-based) to detect violations without running the student code.
- An **AOP test** that dynamically intercepts forbidden operations at runtime. These tests configure the Ares agent (ByteBuddy Instrumentation or AspectJ) to block actual I/O calls when the student code executes.

Security test cases are systematically generated from the policy to cover all declared permissions and restrictions.

The classes described in this manual implement steps 2 and 3.

---

## 3. Architecture Overview

The architecture follows multiple well-known software design patterns. The table below summarises them for reference, understanding these patterns is **not required** to use Ares.

<details>
<summary>Click to expand: Design Pattern Reference</summary>

| Pattern | Where it is used | Why |
|---|---|---|
| **Builder Pattern** | `SecurityPolicy`<br/>`SupervisedCode`<br/>`SecurityPolicyReaderAndDirector`<br/>`SecurityPolicyYAMLReader`<br/>`SecurityPolicyJavaDirector`<br/>All `*Permission` records | Ares security policies involve many optional fields (file permissions, network permissions, thread permissions, etc.). The Builder pattern lets instructors configure only the permissions they need, in any order, while guaranteeing that the resulting objects are immutable and fully validated, preventing misconfigured policies from reaching the test-generation stage. |
| **Strategy Pattern** | `SecurityPolicyReader` → `SecurityPolicyYAMLReader`<br/>`SecurityPolicyDirector` → `SecurityPolicyJavaDirector` | Ares must support different policy file formats (currently YAML, potentially JSON or TOML in the future) and different target programming languages (currently Java, potentially Python or other languages). The Strategy pattern allows each combination to be implemented as an independent, swappable subclass without modifying the core orchestration logic, adhering to the Open/Closed Principle. |
| **Abstract Factory + Builder** | `SecurityPolicyDirector.createTestCases()` returns `TestCaseAbstractFactoryAndBuilder` | Ares supports multiple test-generation toolchains, ArchUnit or WALA for architecture tests, and AspectJ or Instrumentation for AOP tests. The Abstract Factory pattern allows the director to produce the correct set of test artefacts for each toolchain combination through a unified interface, so the rest of the system remains agnostic to the specific toolchain in use. |
| **Facade** | `SecurityPolicyReaderAndDirector` | Internally, Ares must select a reader, parse the policy, select a director, configure a factory, generate tests, write them to disk, and execute them. The Facade pattern hides all of this behind a small four-method API (`createTestCases`, `writeTestCases`, `writeTestCasesAndContinue`, `executeTestCases`), so that instructors and integration code can enforce security policies with minimal boilerplate. |
| **Fluent API / Method Chaining** | Methods in `SecurityPolicyReaderAndDirector` and builder classes return `this` | Ares is designed to be embedded into automated grading pipelines. The Fluent API enables the entire security-enforcement workflow to be expressed as a single, readable method chain (e.g., `.createTestCases().writeTestCasesAndContinue(folder).executeTestCases()`), reducing integration effort and making the call sequence self-documenting. |
| **Factory Method** | `selectSecurityPolicyReader(Path)`<br/>`selectSecurityPolicyDirector(SecurityPolicy)` | Ares needs to automatically pick the correct reader and director at runtime, based on the policy file extension and the programming language declared in the policy. These factory methods centralise that selection logic so that callers never need to know which concrete reader or director class to instantiate. |
| **Dependency Injection & Null-Safety** | Constructor injection in `SecurityPolicyDirector` and subclasses<br/>`@Nonnull` and `@Nullable` annotations throughout | The director depends on five collaborators (`Creator`, `Writer`, `Executer`, `EssentialDataReader`, `ProjectScanner`) that can vary across toolchains. Injecting them via the constructor makes each dependency explicit, allows unit tests to substitute mock implementations, and enables compile-time null-safety checks to prevent runtime crashes during security enforcement. |
| **Immutable Value Objects (Java Records)** | All permission types in the `policySubComponents` package are records (`ProgrammingLanguageConfiguration` is an enum)<br/>Key data containers like `ResourceAccesses` | Security policies must not be accidentally modified after parsing, a mutated permission list could silently weaken security enforcement. Java Records guarantee immutability, provide automatic `equals()`, `hashCode()`, and `toString()`, and make the policy data model inherently thread-safe and self-documenting. |

</details>

> See the accompanying draw.io diagrams for visual class and sequence diagrams:
> - [SecurityPolicyReaderAndDirectorClassDiagram.drawio](SecurityPolicyReaderAndDirectorClassDiagram.drawio)
> - [SecurityPolicyReaderAndDirectorSequenceDiagram.drawio](SecurityPolicyReaderAndDirectorSequenceDiagram.drawio)

---

## 4. The User Input, Security Policy YAML File

The educator provides a YAML file such as `SecurityConfiguration.yaml`. Its structure maps **1-to-1** to the Java data model. Example:

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: anonymous
  theMainClassInsideThisPackageIs: "ReproducibilityCli"
  theFollowingClassesAreTestClasses: []
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: []
    regardingNetworkConnections: []
    regardingCommandExecutions: []
    regardingThreadCreations: []
    regardingPackageImports: []
    regardingTimeouts:
      - timeout: 3000
```

The field names are deliberately chosen to read like English sentences (e.g. `regardingTheSupervisedCode.theFollowingResourceAccessesArePermitted.regardingFileSystemInteractions`), making the configuration self-documenting.


---

## 5. Reading the Policy — The `reader` Package

The **reader** is responsible for loading the YAML policy file and turning it into a `SecurityPolicy` Java object. The reader is selected at runtime based on the file extension — for `.yaml` / `.yml` files this is `SecurityPolicyYAMLReader`. If you add a new file format in the future, you only implement `readSecurityPolicyFrom` and add a new branch in the factory method.

### 5.1. `SecurityPolicyReader` (Abstract Class)

| Aspect | Detail |
|---|---|
| **Role** | Defines a strategy interface for reading `SecurityPolicy` objects from any file format. It is designed as an abstract class so that different concrete readers (currently `SecurityPolicyYAMLReader`, potentially `SecurityPolicyJSONReader` in the future) can implement file-format-specific parsing logic. |
| **Pattern** | Strategy Pattern. This abstract class with concrete subclasses allows runtime selection of the appropriate reader based on file extension. |
| **Key dependencies** | None. This abstract class has no injected dependencies of its own. Its single attribute `ObjectMapper objectMapper` is set by concrete subclasses (e.g., `SecurityPolicyYAMLReader` provides a `YAMLMapper`). |
| **Key attribute** | `ObjectMapper objectMapper`, Jackson's polymorphic serialisation engine, used by concrete subclasses to deserialise file content into `SecurityPolicy` Java objects. |
| **Key methods** | `readSecurityPolicyFrom(Path)` is the main abstract method that concrete subclasses implement to read and parse a policy file, returning a fully populated `SecurityPolicy` object. The static factory method `selectSecurityPolicyReader(Path)` inspects the file extension and returns the matching concrete reader instance (currently `.yaml` or `.yml` → `SecurityPolicyYAMLReader`; other extensions throw an exception). This design allows new file formats to be added simply by implementing `readSecurityPolicyFrom` and adding a new case branch in `selectSecurityPolicyReader`. |

This design makes it trivial to add, say, a `SecurityPolicyJSONReader` in the future: implement `readSecurityPolicyFrom`, then add a `case "json"` branch in `selectSecurityPolicyReader`.

### 5.2. `SecurityPolicyYAMLReader` (Concrete Class)

| Aspect | Detail |
|---|---|
| **Extends** | Extends the abstract `SecurityPolicyReader` class, inheriting its strategy pattern interface and `objectMapper` attribute. |
| **Library** | Uses Jackson's YAML library (`YAMLMapper`) to perform polymorphic deserialisation of YAML policy files into strongly typed Java objects. Jackson handles the mapping from untyped YAML text to the `SecurityPolicy` record structure. |
| **Construction** | Uses the Builder pattern via a fluent API (e.g., `yamlBuilder().yamlMapper(…).build()`), allowing flexible configuration of the reader's Jackson serialiser at construction time. |

**What it does step by step:**

1. Receives the file system `Path` to the YAML policy file.
2. Reads the raw file content via `FileTools.readFile(path)`.
3. Deserialises the content into a `SecurityPolicy` Java object via `FileTools.readYamlFile(…, SecurityPolicy.class)`.
4. Returns the fully populated, validated, immutable `SecurityPolicy` instance.

---

## 6. Directing Test-Case Creation — The `director` Package

The **director** receives the parsed `SecurityPolicy` and orchestrates the multi-step process of generating, writing, and executing security tests. Think of the reader as fetching the instructions and the director as carrying them out: the reader says "here is what the policy contains," and the director decides which test toolchain to build and how to configure it.

### 6.1. `SecurityPolicyDirector` (Abstract Class)

| Aspect | Detail |
|---|---|
| **Role** | This abstract class orchestrates (directs) the entire workflow for creating security test cases from a parsed `SecurityPolicy`. It is designed as a Strategy, with subclasses per target programming language, so that each language-specific director can customise the test-generation pipeline (build mode, architecture-checking mode, AOP mode) while the overall orchestration remains language-agnostic. |
| **Pattern** | Strategy Pattern (one concrete subclass per target language) combined with the Director pattern (orchestrating a multi-step workflow). The class acts as a builder-like component in the `SecurityPolicyReaderAndDirector` facade. |
| **Key dependencies** | Five collaborator interfaces are injected via the constructor (`Creator`, `Writer`, `Executer`, `EssentialDataReader`, `ProjectScanner`), each representing a distinct responsibility (see tables below). Additionally, two file-system paths (`essentialPackagesPath`, `essentialClassesPath`) point to the essential-data YAML files. This separation of concerns allows each collaborator to be tested and extended independently. |
| **Key attributes** | `Creator creator`, generates test-case artefacts. `Writer writer`, serialises artefacts to disk. `Executer executer`, configures the runtime agent and runs the generated tests in-process. `EssentialDataReader essentialDataReader`, reads essential packages/classes files (the YAML implementation is `EssentialDataYAMLReader`). `ProjectScanner projectScanner`, discovers student project structure. `Path essentialPackagesPath` and `Path essentialClassesPath`, paths to essential-data YAML files (see defaults below). |
| **Key methods** | `createTestCases(SecurityPolicy, Path)` is the main abstract method that all concrete directors must implement. It receives the parsed policy and the student project path, and returns a `TestCaseAbstractFactoryAndBuilder` that is ready to generate, write, and execute test cases. The static factory method `selectSecurityPolicyDirector(SecurityPolicy)` reads the `ProgrammingLanguageConfiguration` enum from the policy and instantiates the matching director implementation (currently always `SecurityPolicyJavaDirector`, but the pattern allows future `SecurityPolicyPythonDirector` or other language directors to be added). |

**Collaborator Interfaces (injected into `SecurityPolicyDirector`):**

| Interface | Responsibility |
|---|---|
| `Creator` | Generates test-case source-code artefacts (architecture checks and AOP configuration) from the parsed policy. |
| `Writer` | Serialises the generated artefacts to disk in the appropriate test folder. |
| `Executer` | Executes the generated test cases in-process: it writes the advice settings (modes, allowed packages/classes, restricted package, main class) into the agent configuration, then runs the architecture test cases and finally the AOP test cases. No compilation and no build tool invocation takes place. |
| `EssentialDataReader` | Reads files that list packages and classes Ares itself needs at runtime (e.g. JUnit, ByteBuddy). These are always allowed regardless of the policy. The concrete implementation for YAML files is `EssentialDataYAMLReader`. |
| `ProjectScanner` | Scans the student project directory to discover its package structure and locate compiled bytecode. |

**Essential-data file paths (defaults):**

| Constant | Resolves to | Content |
|---|---|---|
| **DEFAULT_ESSENTIAL_PACKAGES_PATH** | `<Ares code-source location>/de/tum/cit/ase/ares/api/configuration/essentialFiles/java/EssentialPackages.yaml` | Packages that Ares and its dependencies need, always permitted. |
| **DEFAULT_ESSENTIAL_CLASSES_PATH** | `<Ares code-source location>/de/tum/cit/ase/ares/api/configuration/essentialFiles/java/EssentialClasses.yaml` | Individual classes that Ares needs, always permitted. |

Both defaults are resolved by `FileTools.resolveFileOnSourceDirectory(…)`, which takes the directory containing the Ares classes (the code-source location, e.g. the classes directory or the extracted JAR location) as the base and appends `de/tum/cit/ase/ares/api` before the relative path shown above. They are therefore not relative to the student project.

These files are read by the `EssentialDataYAMLReader` collaborator during test-case creation so that Ares's own framework classes are never accidentally blocked by the student's security policy.

This design makes it trivial to add, say, a `SecurityPolicyPythonDirector` in the future: extend `SecurityPolicyDirector`, implement `createTestCases`, add a new enum value to `ProgrammingLanguageConfiguration`, then add a `case` branch in `selectSecurityPolicyDirector`.

### 6.2. `SecurityPolicyJavaDirector` (Concrete Class)

| Aspect | Detail |
|---|---|
| **Extends** | Extends the abstract `SecurityPolicyDirector` class, inheriting its abstract `createTestCases` method and static factory `selectSecurityPolicyDirector`, along with all five injected collaborators. |
| **Library** | Depends on the five injected collaborators: `Creator` (`JavaCreator`) generates the architecture and AOP test cases from the policy, `Writer` (`JavaWriter`) uses file I/O utilities to serialise them, `Executer` (`JavaExecuter`) writes the advice settings into the agent configuration and runs the test cases in-process (no build tool, no compilation), `EssentialDataReader` (`EssentialDataYAMLReader`) uses Jackson YAML for parsing, and `ProjectScanner` (`JavaProjectScanner`) scans the project's source files with regular expressions to discover package name, main class, and test classes. The specific toolchain libraries (ArchUnit, WALA, AspectJ, Instrumentation agent) are loaded based on the `ProgrammingLanguageConfiguration` in the policy. |
| **Construction** | Provides a static `javaBuilder()` method that returns a fluent `Builder` with seven mandatory parameters: the five collaborators (`Creator`, `Writer`, `Executer`, `EssentialDataReader`, `ProjectScanner`) plus the two file paths to the essential packages and classes YAML files. This forces callers to provide all dependencies upfront, reducing the risk of misconfiguration. |

**What it does step by step:**

1. Receives the parsed `SecurityPolicy` object (containing all resource access permissions and restrictions) and the project folder `Path` (where the student code lives).
2. Extracts the `ProgrammingLanguageConfiguration` enum from the policy. This enum encodes eight combinations: Java × {Maven, Gradle} × {ArchUnit, WALA} × {AspectJ, Instrumentation}.
3. Maps the configuration to three mode enums that control test generation: `BuildMode` (`MAVEN` or `GRADLE`, determines the build tool), `ArchitectureMode` (`ARCHUNIT` or `WALA`, determines the static analysis framework), and `AOPMode` (`ASPECTJ` or `INSTRUMENTATION`, determines the runtime enforcement mechanism).
4. Calls the internal method `generateFactoryAndBuilder(…)`, which constructs a `JavaTestCaseFactoryAndBuilder` via its builder (`JavaTestCaseFactoryAndBuilder.builder()`), passing the five collaborators (`Creator`, `Writer`, `Executer`, `EssentialDataReader`, `ProjectScanner`), the two essential-data file paths, the three mode selections, the security policy, and the project path. This factory-builder is specialised for the exact Java toolchain combination specified in the configuration.
5. Returns the `TestCaseAbstractFactoryAndBuilder` instance, which is now ready for the caller to invoke **create** (generate test cases according to the selected toolchain), **write** (save them to disk in the appropriate test folder), and **execute** (run the architecture checks and configure the runtime agent).

> **What is a `TestCaseAbstractFactoryAndBuilder`?** It is a combined Abstract Factory and Builder that produces two kinds of test artefacts: (1) `ArchitectureTestCase` objects for static analysis and (2) `AOPTestCase` objects that configure the runtime interception agent. The "execute" step runs the architecture tests immediately and writes the allowed-resource lists into the agent's configuration so that the agent can enforce them when the student code runs.

---

## 7. Orchestration — `SecurityPolicyReaderAndDirector`

This is the **entry-point class** that ties together reading, directing, and test-case management. It is the main public API that instructors and automated grading systems interact with. The class implements two important roles:
- A **Facade**, exposing a small public interface (`createTestCases`, `writeTestCases`, `writeTestCasesAndContinue`, `executeTestCases`) that hides the complexity of reader selection, policy parsing, director selection, and factory configuration.
- A **Client of the Abstract Factory Pattern**, consuming the `TestCaseAbstractFactoryAndBuilder` that the director creates, and delegating each workflow step to the factory.

The class uses the **Builder pattern** for construction and the **Fluent API** for the workflow steps, allowing the entire security-enforcement process to be expressed as a single, method-chained expression.

### 7.1. Construction

To use this class, instructors instantiate it via its builder:

```java
SecurityPolicyReaderAndDirector.builder()
    .securityPolicyFilePath(Path.of("SecurityConfiguration.yaml"))
    .projectFolderPath(Path.of("/path/to/student/project"))
    .build();
```

The builder accepts two optional paths (both are `@Nullable`, and `build()` performs no null checks):
- `securityPolicyFilePath`: The file system path to the YAML security policy file (e.g., `SecurityConfiguration.yaml`) that the instructor has written. If this is `null`, no policy file is read and Ares falls back to the default most-restricted enforcement (see Section 7.3).
- `projectFolderPath`: The file system path to the student's project folder, where the code to be verified lives. If this is `null`, the project-dependent steps operate without a project base path.

The resulting instance is immutable and ready to orchestrate the three-step workflow.

### 7.2. Three-Step Workflow

The public API consists of three methods that orchestrate the security-enforcement pipeline:

| Step | Method | What happens internally |
|---|---|---|
| **1. Create** | `createTestCases()` | Detects the policy file format (currently YAML) and selects the appropriate `SecurityPolicyReader` → reads and parses the policy file into a `SecurityPolicy` object → reads the `ProgrammingLanguageConfiguration` enum and selects the appropriate `SecurityPolicyDirector` (currently `SecurityPolicyJavaDirector`) → the director constructs and returns a `TestCaseAbstractFactoryAndBuilder` configured for the exact toolchain combination specified. This step validates the policy and prepares the factory for test generation. |
| **2. Write** | `writeTestCases(testFolderPath)` or `writeTestCasesAndContinue(testFolderPath)` | Delegates to the factory/builder's `write` method, which invokes the injected `Writer` collaborator to serialise the generated test-case source code artefacts to disk in the specified test folder. `writeTestCases` returns a `List<Path>` of written files and ends the chain. `writeTestCasesAndContinue` returns `this` to allow further chaining. |
| **3. Execute** | `executeTestCases()` | Delegates to the factory/builder's `execute` method, which invokes the injected `Executer` collaborator. No compilation and no build tool invocation takes place: the executer writes the advice settings (modes, allowed packages/classes, restricted package, main class) into the agent configuration, then runs the architecture test cases (ArchUnit or WALA) in-process and finally the AOP test cases (AspectJ or Instrumentation) to enforce the declared permissions. Returns `this` for chaining. |

The chainable methods (`createTestCases`, `writeTestCasesAndContinue`, `executeTestCases`) return `this` (the `SecurityPolicyReaderAndDirector` instance), enabling **method chaining** via the **Fluent API**. Note that `writeTestCases` returns a `List<Path>` instead and ends the chain:

```java
SecurityPolicyReaderAndDirector.builder()
    .securityPolicyFilePath(policyPath)
    .projectFolderPath(projectPath)
    .build()
    .createTestCases()
    .writeTestCasesAndContinue(testFolder)
    .executeTestCases();
```

This chained call reads as a sequence of actions: create → write → execute, making the workflow both self-documenting and resistant to accidental operation reordering.

### 7.3. Null-Safety Strategy

The class uses a plain null-and-empty check (`if (securityPolicyFilePath != null && !securityPolicyFilePath.toString().isEmpty())`) to handle the case where no policy file path is provided. If `securityPolicyFilePath` is `null` (or empty), only the **reader** step is skipped:
- No reader is selected and no policy file is read; the parsed policy remains `null`.
- `SecurityPolicyDirector.selectSecurityPolicyDirector(null)` is still called and returns the default `SecurityPolicyJavaDirector`.
- The director's `createTestCases(null, …)` builds a factory with the default modes `BuildMode.MAVEN`, `ArchitectureMode.ARCHUNIT`, and `AOPMode.ASPECTJ`.
- The factory falls back to `ResourceAccesses.createRestrictive()`: **every** resource access (file system, network, command execution, thread creation, package imports) is denied, and a 10-second execution timeout is enforced.
- The supervised package, main class, and test classes are derived by scanning the project instead of the policy.

In other words, a missing policy path does **not** disable enforcement. It results in the most restrictive default enforcement, so that a forgotten or misconfigured policy path fails closed rather than open.

---

## 8. Processing Pipeline (Overview)

The following diagram shows the end-to-end flow from a YAML policy file to enforced security tests:

```
┌─────────────────────┐
│  SecurityPolicy.yaml│   ← instructor writes this
└─────────┬───────────┘
          │  selectSecurityPolicyReader(path)
          ▼
┌─────────────────────┐
│ SecurityPolicyYAML  │   ← reads & deserialises YAML
│ Reader              │
└─────────┬───────────┘
          │  returns SecurityPolicy record
          ▼
┌─────────────────────┐
│ SecurityPolicyJava  │   ← selects build/arch/AOP modes
│ Director            │
└─────────┬───────────┘
          │  returns TestCaseAbstractFactoryAndBuilder
          ▼
┌─────────────────────┐
│ TestCaseAbstract    │   ← creates ArchitectureTestCase
│ FactoryAndBuilder   │     + AOPTestCase artefacts
└─────────┬───────────┘
          │  .create() → .write(folder) → .execute()
          ▼
┌─────────────────────┐
│ Architecture checks │   ← static: ArchUnit / WALA
│ + Agent config      │   ← dynamic: AspectJ / Instrumentation
└─────────────────────┘
```

---

## 9. End-to-End Example

**1. Instructor writes `SecurityConfiguration.yaml`:**

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: com.student
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

**2. Instructor writes a JUnit test:**

```java
import de.tum.cit.ase.ares.api.Policy;
import org.junit.jupiter.api.Test;

class SecurityTest {
    @Test
    @Policy(value = "SecurityConfiguration.yaml",
            withinPath = "classes/java/main/com/student")
    void studentCodeMustNotAccessFileSystem() {
        // Call the student's code, if it tries to read/write files,
        // Ares will throw a SecurityException.
        com.student.Main.main(new String[]{});
    }
}
```

**3. What happens at runtime:**

1. JUnit runs the test → Ares creates a `SecurityPolicyReaderAndDirector` (enforcement is on by default; the `@Policy` annotation supplies the policy path and bytecode scope).
2. Reader parses the YAML → produces `SecurityPolicy` record.
3. Director selects Gradle + ArchUnit + Instrumentation modes.
4. Factory creates architecture test cases → executed immediately → if `com.student.Main` imports `java.io.File`, the test fails.
5. Factory configures the ByteBuddy agent → when `Main.main()` runs, any `File.read()` call is intercepted and throws `SecurityException`.

---

## 10. Integration with JUnit Extensions

In practice, instructors do not call `SecurityPolicyReaderAndDirector` directly. Instead, they annotate test methods with `@Policy` (see [Security Policy Manual](SecurityPolicyManual.md)), and Ares’s JUnit extension handles the rest:

1. **Before each test method**, `JupiterSecurityExtension` (or `JqwikSecurityExtension`) checks for a `@Policy` annotation on the test method or test class.
2. It **resets** all previous security settings so that tests are isolated from each other.
3. Enforcement is **on by default**: both extensions evaluate the `activated` field and skip enforcement only for `@Policy(activated = false)`. `JupiterSecurityExtension` enforces whenever Ares is activated and a `@Policy` annotation or a test method is present; `JqwikSecurityExtension` enforces for every activated property. With `@Policy` present, the annotation's `value` (policy path) and `withinPath` (bytecode scope) are used; **without** `@Policy`, a `SecurityPolicyReaderAndDirector` is still created with a `null` policy path, which triggers the default most-restricted enforcement (see Section 7.3).
4. It calls `createTestCases().executeTestCases()`, this runs the architecture checks and configures the runtime agent.
5. The test method executes. Any forbidden I/O call is intercepted by the agent and throws a `SecurityException`.
6. **After each test method**, the settings are reset again in a `finally` block.

---

## 11. Troubleshooting

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| `IllegalArgumentException` on test startup mentioning unsupported file format | Policy file does not have `.yaml` or `.yml` extension | Rename the file to use `.yaml` or `.yml` |
| `SecurityException`, YAML parse error (caused by `StreamReadException`) | Malformed YAML syntax (wrong indentation, tabs, missing colons) | Validate the YAML file with a linter; use spaces only |
| `SecurityException`, cannot deserialise policy (caused by `DatabindException`) | YAML field names or types do not match the `SecurityPolicy` record schema | Check field names against the [Security Policy Manual](SecurityPolicyManual.md) |
| `SecurityException`, file not found or unreadable | The path in `@Policy(value = "...")` does not point to an existing file. `JupiterSecurityExtension` rejects a non-existent path already when reading the annotation; a read failure inside the YAML reader (caused by `IOException`) is likewise wrapped in a `SecurityException` | Verify the path is correct and relative to the project root |
| Tests fail with denied resource accesses although no policy was set | `securityPolicyFilePath` is `null`, so the default most-restricted enforcement applies (Section 7.3): all resource accesses are denied and a 10-second timeout is enforced | Set the policy path to a policy that permits the required accesses, or use `@Policy(activated = false)` to deactivate Ares for the test |
| Architecture tests pass but runtime enforcement is missing | Agent JAR not loaded via `-javaagent` | See [How to Make a Project an Ares Project](../HowToMakeAProjectAnAresProject.md) |

---

## 12. Glossary

| Term | Meaning |
|------|----------|
| **AOP (Aspect-Oriented Programming)** | A technique for intercepting method calls at defined points ("pointcuts") without modifying the original code. Ares uses AOP to block forbidden I/O operations at runtime. |
| **ArchUnit** | A Java library for checking architecture rules on compiled bytecode (e.g., "no class in package X may call class Y"). |
| **WALA** | A static analysis framework that builds inter-procedural call graphs to detect forbidden API usage even through chains of method calls. |
| **AspectJ** | A compile-time AOP framework that weaves interception code directly into bytecode. |
| **Instrumentation (Java Agent)** | A runtime AOP approach using the `java.lang.instrument` API and ByteBuddy. A Java agent modifies class bytecode at load time. |
| **ByteBuddy** | A library for creating and modifying Java classes at runtime, used by Ares to implement the instrumentation agent. |
| **Facade** | A design pattern that provides a simplified interface to a complex subsystem. |
| **Strategy Pattern** | A design pattern that defines a family of interchangeable algorithms (here: different readers and directors). |


