# Ares2: Package Overview

This document provides a high-level conceptual overview of each package in `src/main/java/de/tum/cit/ase/ares/api/`.

---

## `aop`: Aspect-Oriented Programming Security Enforcement

This package implements **runtime security enforcement** via aspect-oriented programming. It intercepts method calls that access the file system, network, command line, or thread management and blocks unauthorised operations according to a security policy.

Two AOP modes are supported (defined in the `AOPMode` enum):

- **INSTRUMENTATION**: Byte Buddy-based load-time weaving via a Java agent (`JavaInstrumentationAgent`)
- **ASPECTJ**: compile-time weaving via AspectJ `.aj` files

The abstract `AOPTestCase` class aggregates four dedicated extractors, one for each guarded subsystem, which derive the set of permitted operations from the active security policy:

| Sub-Package | Responsibility |
|---|---|
| `fileSystem/` | Extracts permitted file paths and action levels |
| `networkSystem/` | Extracts permitted hosts, ports, and connection operations |
| `commandSystem/` | Extracts permitted commands and arguments |
| `threadSystem/` | Extracts permitted thread creation limits |
| `resourceLimits/` | Extracts CPU, memory, and heap constraints |
| `java/aspectj/` | AspectJ pointcut and advice definitions (`.aj` files) |
| `java/instrumentation/` | Byte Buddy agent, advice toolboxes, and pointcut definitions |
| `java/javaAOPModeData/` | CSV-based configuration data loaders |
| `java/javaAOPTestCaseToolbox/` | Helper classes for test-case generation |

`JavaAOPTestCaseSettings` stores active restrictions in static fields and is synchronised across the application and bootstrap class loaders via reflection.

**Key design patterns:** Strategy (extractors), Template Method (`AOPTestCase`), Abstract Factory (AOP mode variants).

---

## `architecture`: Static Architecture Analysis

This package performs **static code analysis** to verify that student code does not use forbidden APIs or packages. Two analysis back-ends are available (selected via the `ArchitectureMode` enum):

- **ArchUnit** (`java/archunit/`): rule-based package and class checks, including a custom `TransitivelyAccessesMethodsCondition`
- **WALA** (`java/wala/`): whole-programme call-graph construction (`CustomCallgraphBuilder`) with reachability analysis (`ReachabilityChecker`, `CustomDFSPathFinder`)

The abstract `ArchitectureTestCase` holds `JavaClasses`, a `CallGraph`, and permitted `PackagePermission` sets.

| Sub-Package | Responsibility |
|---|---|
| `java/archunit/` | ArchUnit-based architecture rule checks and custom conditions |
| `java/wala/` | WALA-based call-graph construction and reachability analysis |

The supported check categories are: `FILESYSTEM_INTERACTION`, `NETWORK_CONNECTION`, `COMMAND_EXECUTION`, `THREAD_CREATION`, `PACKAGE_IMPORT`, `TERMINATE_JVM`, `REFLECTION`, `SERIALIZATION`, `CLASS_LOADING`.

**Key design patterns:** Abstract Factory (ArchUnit/WALA as concrete products), Strategy (analysis back-ends).

---

## `ast`: AST-Based Code Analysis

This package uses **JavaParser** for abstract-syntax-tree analysis of student source files. It detects undesired syntactic constructs such as specific loop types, conditionals, exception-handling patterns, or forbidden recursion.

| Sub-Package | Responsibility |
|---|---|
| `asserting/` | Fluent AssertJ-style assertions (`UnwantedNodesAssert`, `UnwantedRecursionAssert`) |
| `model/` | AST node models (`JavaFile`, `RecursionCheck`, `MethodCallGraph`) |
| `type/` | Category enums for AST node types (`LoopType`, `ConditionalType`, `ExceptionHandlingType`, `ClassType`) |

`UnwantedNodesAssert` and `UnwantedRecursionAssert` provide a fluent API for specifying which syntactic constructs should be absent from student submissions.

**Key design patterns:** Strategy (node-type categories), Fluent API (AssertJ style).

---

## `buildtoolconfiguration`: Build Tool Configuration

This package abstracts **Maven and Gradle build configurations**. The `BuildMode` enum provides build-directory paths, template references (e.g. `pom.xml` header/footer vs. `build.gradle`), and output paths depending on the active build tool.

| Class | Purpose |
|---|---|
| `BuildMode` | Enum providing build directories, template paths, and output file names per build tool |
| `BuildToolConfiguration` | Marker interface serving as an extension point |

`BuildMode` centralises the differences between Maven and Gradle so that the rest of the framework can operate in a build-tool-agnostic manner.

**Key design patterns:** Strategy (build mode variants).

---

## `context`: Test Context Management

This package provides an **adapter layer** over different JUnit 5 test-context implementations. The `TestContext` interface exposes the current test method, test class, test instance, and display name in a framework-agnostic way.

| Class | Purpose |
|---|---|
| `AresContext` | Abstract base class wrapping a `TestContext` |
| `TestContext` | Adapter interface for JUnit 5 test context (method, class, instance, display name) |
| `TestContextUtils` | Static utilities for finding annotations in the class hierarchy (repeatable-annotation-aware) |
| `TestType` | Enum distinguishing `PUBLIC` and `HIDDEN` tests for Artemis visibility |

`TestContextUtils` traverses the class hierarchy and supports repeatable annotations, ensuring that inherited annotations are correctly resolved.

**Key design patterns:** Adapter (framework-agnostic test context).

---

## `dynamic`: Dynamic Reflection API

This package offers a **type-safe reflection API** for testing. It allows dynamic loading, inspection, and invocation of classes, methods, constructors, and fields with localised error messages. In contrast to raw `java.lang.reflect`, the API provides lazy loading, existence checks (`exists()`), and fluent chaining, which is useful for testing student code without compile-time dependencies.

| Class | Purpose |
|---|---|
| `DynamicClass<T>` | Lazy class loading with existence checks |
| `DynamicMethod<T>` | Type-safe method invocation via `invokeOn()` |
| `DynamicConstructor<T>` | Constructor invocation via `newInstance()` |
| `DynamicField<T>` | Field access via typed getters/setters |
| `Check` | Enum for modifier validation (STATIC, FINAL, PUBLIC, etc.) |
| `Checkable` | Interface with `exists()` and `check()` |

`DynamicClass` serves as the entry point: once a class is resolved, methods, constructors, and fields can be looked up from it using a fluent API.

**Key design patterns:** Wrapper/Decorator (around Java Reflection), Fluent API.

---

## `internal`: Internal Infrastructure Utilities

This package contains **internal infrastructure classes** not intended for public consumption. It provides privileged invocation, configuration generation, test guarding, timeout enforcement, I/O extension logic, and reporting utilities.

| Class | Purpose |
|---|---|
| `BlacklistedInvoker` | Executes privileged lambdas/callables whose stack frames appear on the blacklist |
| `ConfigurationUtils` | Generates `AresSecurityConfiguration` instances from test-context annotations |
| `TestGuardUtils` | Pre- and post-test guard logic |
| `TimeoutUtils` | Timeout computation and strict-timeout enforcement |
| `IOExtensionUtils` | I/O extension helper logic |
| `ReportingUtils` | Test reporting helper logic |
| `PrivilegedException` | Wrapper for exceptions thrown during privileged execution |

The `sanitization/` sub-package (18 classes) cleanses `Throwable` instances to prevent security-relevant information from leaking through stack traces and error messages. A chain of type-specific sanitisers (`SpecificThrowableSanitizer` implementations) handles `AssertionFailedError`, `MultipleFailuresError`, `ExceptionInInitializerError`, and others.

**Key design patterns:** Chain of Responsibility (sanitiser chain), Strategy (type-specific sanitisers).

---

## `io`: Console I/O Testing

This package enables testing of **console input and output** (`System.in` / `System.out` / `System.err`). The central `IOTester` class replaces the standard system streams with test streams, so that expected inputs can be fed to the programme and produced outputs can be asserted against.

| Class | Purpose |
|---|---|
| `IOTester` | Installs/uninstalls test streams; thread-safe |
| `IOManager<T>` | Strategy interface for I/O test lifecycle |
| `IOTesterManager` | Default `IOManager` implementation backed by `IOTester` |
| `InputTester` / `OutputTester` | Management of expected inputs and output assertions |
| `TestInStream` / `TestOutStream` | Replacement streams for `System.in` and `System.out`/`err` |
| `Line` / `StaticLine` / `DynamicLine` | Models for input/output lines (static text or callback-based) |

`IOTester` is thread-safe: all static methods (`isInstalled()`, `installNew()`, `uninstallCurrent()`) are synchronised, and a volatile flag tracks the installation state.

**Key design patterns:** Strategy (`IOManager` interface), Decorator (stream wrapping), Observer/Callback (`LineAcceptor`, `LineProvider`).

---

## `jqwik`: Jqwik Property-Based Testing Integration

This package integrates the Ares testing framework with the **jqwik** property-based testing engine. It mirrors the structure of the `jupiter` package but targets jqwik's lifecycle hooks instead.

| Class | Purpose |
|---|---|
| `JqwikAresTest` | Meta-annotation that registers all jqwik lifecycle hooks |
| `@Hidden` / `@Public` | Annotations for test visibility in Artemis |
| `JqwikSecurityExtension` | Security enforcement hook |
| `JqwikIOExtension` | I/O testing hook |
| `JqwikStrictTimeoutExtension` | Timeout enforcement |
| `JqwikTestGuard` | Pre- and post-condition guard |
| `JqwikContext` | Adapter from jqwik's lifecycle context to the Ares `TestContext` |

`JqwikContext` adapts jqwik's native lifecycle context to the framework-agnostic `TestContext` interface, enabling code reuse between the `jupiter` and `jqwik` integrations.

**Key design patterns:** Adapter (`JqwikContext`), Composite (meta-annotation), Decorator (lifecycle hooks).

---

## `jupiter`: JUnit Jupiter Integration

This package integrates Ares with **JUnit Jupiter**. It is the primary test-framework binding and defines the most commonly used annotations.

| Class | Purpose |
|---|---|
| `@PublicTest` / `@HiddenTest` | Convenience meta-annotations combining `@Test` with `@Public`/`@Hidden` |
| `@Hidden` / `@Public` | Type-level visibility markers for Artemis |
| `JupiterAresTest` | Internal meta-annotation registering all four extensions |
| `JupiterIOExtension` | Redirects `System.in`/`out`/`err` |
| `JupiterTestGuard` | Applies pre- and post-test guards |
| `JupiterSecurityExtension` | Reads the `@Policy` annotation, configures and activates the security policy, resets settings before and after each test |
| `JupiterStrictTimeoutExtension` | Enforces strict timeouts |
| `UnifiedInvocationInterceptor` | Collapses all `InvocationInterceptor` callbacks into a single generic method |
| `JupiterContext` | Adapts JUnit's `ExtensionContext` to the Ares `TestContext` |
| `BenchmarkExtension` | Performance benchmarking extension |

`UnifiedInvocationInterceptor` collapses all JUnit `InvocationInterceptor` callback methods into a single generic `interceptGenericInvocation()` method, reducing boilerplate in extension implementations.

**Key design patterns:** Adapter (`JupiterContext`), Template Method (`UnifiedInvocationInterceptor`), Composite (meta-annotation), Decorator (extensions).

---

## `localization`: Internationalisation

This package manages **internationalisation** for error messages and test output. Two resource bundles are shipped: English (`messages.properties`) and German (`messages_de.properties`).

| Class | Purpose |
|---|---|
| `Messages` | Central localisation class with `localized(key, args)` and `localizedFailure(key, args)` factory methods; uses an LRU cache for `ResourceBundle` lookups |
| `@UseLocale` | Annotation to pin the locale for a test class (e.g. `@UseLocale("de")`) |
| `Localisation` | Utility for copying localisation files into target projects |

`Messages` provides factory methods that return localised `AssertionFailedError` instances, ensuring that all error messages shown to students are presented in the configured language.

**Key design patterns:** Singleton (ResourceBundle cache), Utility.

---

## `phobos`: Container and Sandbox-Based Security

Phobos provides an alternative security mechanism based on **external container sandboxing** (e.g. Docker/bwrap). Unlike the in-process AOP approach, Phobos generates configuration files (allowed lists, shell scripts) that are consumed by an external sandbox runtime.

| Class | Purpose |
|---|---|
| `Phobos` | Utility class for file copy/edit configuration (CSV-based) |
| `PhobosTestCase` | Abstract base class with extractors for file-system, network, and resource-limit permissions |
| `JavaPhobosTestCase` | Java implementation producing sandbox configuration from the security policy |
| `PhobosTestCaseSupported` | Interface for supported Phobos test cases |
| `JavaPhobosTestCaseSupported` | Enum: `FILESYSTEM_INTERACTION`, `NETWORK_CONNECTION`, `RESOURCE_LIMIT` |

`JavaPhobosTestCase` transforms the abstract permission model into concrete shell-script and configuration-file content that an external sandbox runtime can enforce.

**Key design patterns:** Builder (`JavaPhobosTestCase.Builder`), Strategy (extractors), Template Method.

---

## `policy`: Security Policy Model

This package defines the **central security-policy data model** as immutable Java records and orchestrates the end-to-end policy workflow.

The data model is structured as follows:

```
SecurityPolicy
└── SupervisedCode
    ├── ProgrammingLanguageConfiguration  (enum, 8 combinations)
    ├── package name, main class, test classes
    └── ResourceAccesses
        ├── FilePermission[]
        ├── NetworkPermission[]
        ├── CommandPermission[]
        ├── ThreadPermission[]
        ├── PackagePermission[]
        ├── ClassPermission[]
        └── ResourceLimitsPermission[]
```

| Sub-Package | Responsibility |
|---|---|
| `reader/yaml/` | YAML policy parsing via Jackson (`SecurityPolicyYAMLReader`) |
| `director/java/` | Translation of parsed policy into a `TestCaseAbstractFactoryAndBuilder` (`SecurityPolicyJavaDirector`) |
| `policySubComponents/` | Permission records: `FilePermission`, `NetworkPermission`, `CommandPermission`, `ThreadPermission`, `PackagePermission`, `ClassPermission`, `ResourceLimitsPermission` |

`SecurityPolicyReaderAndDirector` orchestrates the full workflow: read the YAML policy, create test cases, write them to disc, and execute them.

**Key design patterns:** Builder (`SecurityPolicy`, `SupervisedCode`), Abstract Factory (reader/director), Strategy (YAML vs. other formats), Immutable Record Pattern.

---

## `security`: Runtime Security Configuration

This package manages the **annotation-driven runtime security configuration**, which co-exists alongside the newer policy-based system.

| Class | Purpose |
|---|---|
| `AresSecurityConfiguration` | Immutable configuration object holding whitelisted/blacklisted classes, paths, packages, ports, and thread counts |
| `AresSecurityConfigurationBuilder` | Builds the configuration from `TestContext` annotations with automatic Maven/Gradle detection |
| `SecurityConstants` | Static whitelist/blacklist sets for stack-frame analysis (Java standard library, JUnit, Ares internals, etc.) |
| `AresSystemProperties` | System properties consumed by Ares |
| `ConfigurationException` | Thrown on configuration errors |

`AresSecurityConfigurationBuilder` reads annotations such as `@WhitelistPath`, `@BlacklistPath`, `@AllowThreads`, and `@AllowLocalPort` from the test context and assembles an immutable `AresSecurityConfiguration`.

**Key design patterns:** Builder, Immutable Object.

---

## `securitytest`: Test Case Factory and Lifecycle

This package implements the **central factory** for creating, configuring, serialising, and executing security test cases. It combines the Abstract Factory and Builder patterns into a unified lifecycle: **Create**, then **Write**, then **Execute**.

| Class | Purpose |
|---|---|
| `TestCaseAbstractFactoryAndBuilder` | Abstract base orchestrating the full lifecycle |
| `JavaTestCaseFactoryAndBuilder` | Java-specific implementation |
| `Creator` / `JavaCreator` | Creates architecture and AOP test cases from `ResourceAccesses` |
| `Writer` / `JavaWriter` | Serialises generated test code to files |
| `Executer` / `JavaExecuter` | Executes test cases with proper build, architecture, and AOP mode settings |
| `ProjectScanner` / `JavaProjectScanner` | Detects build mode, package name, and test classes |
| `EssentialDataReader` / `EssentialDataYAMLReader` | Reads essential packages/classes from YAML |
| `StudentCompiledClassesPath` | Annotation for the path to compiled student classes |
| `PathLocationProvider` | ArchUnit `LocationProvider` based on the annotation |

`TestCaseAbstractFactoryAndBuilder` unifies creation, serialisation, and execution into a single orchestration point, delegating each step to language-specific implementations.

**Key design patterns:** Abstract Factory + Builder, Strategy (Creator, Writer, Executer, ProjectScanner), Template Method.

---

## `structural`: Structural Conformance Testing

This package verifies that student code **structurally matches** a predefined oracle (`test.json`). Tests are generated as JUnit 5 `DynamicTest` instances.

| Provider | Checks |
|---|---|
| `ClassTestProvider` | Superclass, interfaces, annotations, modifiers (`abstract`, `enum`, etc.) |
| `MethodTestProvider` | Method signatures, return types, parameters, modifiers |
| `ConstructorTestProvider` | Constructor signatures, parameters, modifiers, annotations |
| `AttributeTestProvider` | Field declarations, types, modifiers, enum values |

The abstract `StructuralTestProvider` base class loads the `test.json` structure oracle and provides shared reflection-based helper methods. The `testutils/` sub-package contains `ClassNameScanner` and `ScanResult` for classpath scanning.

**Key design patterns:** Template Method (`StructuralTestProvider`), Factory Method (dynamic test generation).

---

## `util`: General Utility Classes

This package provides a **cross-cutting collection of utility classes** used throughout the framework.

| Class | Purpose |
|---|---|
| `FileTools` | File read/write/copy, YAML parsing, JAR extraction, path resolution |
| `PathRule` | Glob-based file-path matching rule with `PathType` and `PathActionLevel` |
| `PackageRule` | Regex-based package matching rule with `RuleType` (WHITELIST/BLACKLIST) |
| `ProjectSourcesFinder` | Detects Maven/Gradle projects and extracts source directories from build files |
| `DependencyManager` | Dependency resolution utilities |
| `LruCache` | Simple LRU cache (extends `LinkedHashMap`) |
| `ClassMemberAccessor` | Reflective member discovery across class hierarchies |
| `ReflectionTestUtils` | Assertion helpers for structural tests |
| `RuleType` | Enum: `WHITELIST`, `BLACKLIST` |
| `DelayedFilter` | Predicate-based delayed filtering |
| `IgnorantUnmodifiableList` | List wrapper that silently ignores mutating operations |
| `UnexpectedExceptionError` | Specialised `Error` for wrapping unexpected exceptions |

`FileTools` is the largest utility class, centralising all file-system interactions (reading, writing, copying, formatting, YAML parsing, and JAR extraction) behind a consistent API.

**Key design patterns:** Utility (static methods), Strategy (rule matching), LRU Cache.
