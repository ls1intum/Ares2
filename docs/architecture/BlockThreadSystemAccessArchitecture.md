# Thread System Security Mechanism (Architecture Analysis)

## Table of Contents

1. [High-Level Overview](#1-high-level-overview)
   - [1.1 Architecture Analysis Approach](#11-architecture-analysis-approach)
   - [1.2 Configuration Settings](#12-configuration-settings)
   - [1.3 Summary: When Is Thread Manipulation Blocked?](#13-summary-when-is-thread-manipulation-blocked)
   - [1.4 What Code Is Trusted vs. Restricted?](#14-what-code-is-trusted-vs-restricted)
2. [Ares Monitors Thread Methods](#2-ares-monitors-thread-methods)
   - [2.1 ArchUnit Mode (Static Dependency Analysis)](#21-archunit-mode-static-dependency-analysis)
   - [2.2 WALA Mode (Call Graph Analysis)](#22-wala-mode-call-graph-analysis)
3. [Monitored Thread System Methods](#3-monitored-thread-system-methods)
   - [3.1 THREAD SYSTEM - CREATE Operations (With Parameters)](#31-thread-system---create-operations-with-parameters)
   - [3.2 THREAD SYSTEM - CREATE Operations (Without Parameters)](#32-thread-system---create-operations-without-parameters)
4. [ArchUnit Analysis Flow](#4-archunit-analysis-flow)
   - [4.1 Loading Java Classes](#41-loading-java-classes)
   - [4.2 Defining Architecture Rules](#42-defining-architecture-rules)
   - [4.3 Executing Rules](#43-executing-rules)
   - [4.4 Transitive Access Detection](#44-transitive-access-detection)
5. [WALA Analysis Flow](#5-wala-analysis-flow)
   - [5.1 Building the Call Graph](#51-building-the-call-graph)
   - [5.2 Finding Entry Points](#52-finding-entry-points)
   - [5.3 Rule Checking with WalaRule](#53-rule-checking-with-walarule)
   - [5.4 Path Classification and False Positive Filtering](#54-path-classification-and-false-positive-filtering)
6. [Test Case Generation](#6-test-case-generation)
   - [6.1 Writing Architecture Test Cases](#61-writing-architecture-test-cases)
   - [6.2 Executing Architecture Test Cases](#62-executing-architecture-test-cases)
7. [Conclusion](#7-conclusion)

---

# 1. High-Level Overview

This document describes how Ares 2 prevents unauthorised thread creation in student code using **static code analysis** techniques via Architecture Testing frameworks.

Static analysis cannot enforce a runtime class-and-count quota. It supplies a
domain-wide deny rule only when no thread allowance exists. With any thread
allowance, AspectJ or instrumentation is authoritative for matching the permitted
class and quota and rejecting other creation; see `docs/policy/EnforcementModel.md`.

**Key Difference from AOP Approach:**
- **AOP (Runtime)**: Monitors thread creation during program execution and enforces thread quotas in real-time
- **Architecture (Static)**: Analyses compiled bytecode before execution to detect potential thread creation violations in the code structure

---

## 1.1 Architecture Analysis Approach

**What is Architecture Testing?**

Architecture testing validates that code follows specific structural rules by analysing compiled bytecode. Instead of running the code and intercepting thread creation calls (AOP), it examines the program structure to find violations.

**Think of it like:**
- **AOP = Security Guard**: Checks thread quotas when threads are actually spawned
- **Architecture = Building Inspector**: Reviews code structure before execution to ensure no thread creation capabilities exist

**Two Analysis Frameworks:**

### **ArchUnit (Static Analysis)**
- **Type**: Pure static analysis using ArchUnit framework
- **Strength**: Fast, no call graph needed
- **Method**: Analyses class dependencies and method calls in compiled bytecode
- **Use Case**: Detecting direct and transitive method access patterns to thread creation APIs

### **WALA (Call Graph Analysis)**
- **Type**: Static analysis with call graph modelling using IBM WALA framework
- **Strength**: Precise call path detection, understands complex call chains
- **Method**: Builds a call graph representing all possible method invocations
- **Use Case**: Finding reachable thread creation methods through complex call chains, including lambda expressions
- **Special Feature**: Path classification (`WalaPathClassification`) that attributes each violation to the nearest student frame and suppresses transitive-JDK false positives

**Validation Flow:**

1. **Load Classes**: Import compiled `.class` files from classpath
2. **Build Analysis Model**:
   - ArchUnit: Load class metadata and dependencies
   - WALA: Build call graph with entry points
3. **Define Rules**: Specify forbidden method patterns (thread creation operations)
4. **Execute Analysis**: Check if student code violates rules
5. **Report Violations**: Generate detailed error messages with caller, forbidden method, and entry point

---

## 1.2 Configuration Settings

Security policies are configured through settings that instructors can adjust:

| Setting | Type | Description | Example |
|---------|------|-------------|---------|
| **architectureMode** | `ArchitectureMode` enum at policy level (selected via the `ProgrammingLanguageConfiguration`); passed as a `String` to the write/execute methods | Analysis framework | `ArchitectureMode.ARCHUNIT` or `ArchitectureMode.WALA`; `"ARCHUNIT"` / `"WALA"` at the method level |
| **allowedPackages** | `Set<PackagePermission>` | Packages allowed to be imported; only consulted by the PACKAGE_IMPORT rule, **not** by THREAD_CREATION | `Set.of(new PackagePermission("java.lang"))` |
| **allowedClasses** | `Set<ClassPermission>` | Classes exempt from the architecture rules (essential/test infrastructure) | `Set.of(new ClassPermission("de.tum.cit.ase.Helper"))` |
| **classPath** | `String` | Path to compiled student code | `"target/classes"` |
| **theSupervisedCodeUsesTheFollowingPackage** | `String` (policy field) | Package containing the supervised (student) code; steers the AOP instrumentation, **not** the architecture rules | `"de.student"` |

**Architecture-Specific Configuration:**
- No thread quota management (no `threadNumberAllowedToBeCreated`) - Architecture testing detects ANY thread creation attempt
- No thread class allowlists (no `threadClassAllowedToBeCreated`) - All thread creation is flagged
- No AOP mode selection - Uses `architectureMode` instead
- Exemptions are **class-based** (`allowedClasses` / `ClassPermission`), not package-based

---

## 1.3 Summary: When Is Thread Manipulation Blocked?

Access is **BLOCKED** 🔴 when **ALL** conditions are true:

1. **Architecture Mode Enabled**: `architectureMode == "ARCHUNIT"` or `architectureMode == "WALA"`
2. **Student Code Contains Thread Manipulation Calls**: Analysis detects method calls to thread manipulation APIs
3. **Calls Are Reachable**: The forbidden methods can be reached from student code (directly or transitively)
4. **Not Exempt**: The involved classes are not on the `allowedClasses` (`ClassPermission`) allow-list, and (in WALA mode) the path is not classified as an all-infrastructure or transitive-JDK false-positive path

**If ANY condition fails → No Violation Detected** 🟢

**Key Differences from AOP:**
- 🔴 Detected at analysis time (before execution), not at runtime
- 🔴 Blocks ALL thread manipulation attempts, not based on quotas or thread classes
- 🔴 Reports potential violations, even if the code path is never executed
- 🔴 No distinction between different thread types (all treated equally)

**Legend:**
- 🔴 SecurityException thrown → Security violation detected in code structure
- 🟢 No violations found → Code passes architecture analysis

---

## 1.4 What Code Is Trusted vs. Restricted?

**Trusted Code (No Restrictions):**
- Ares internal code (`de.tum.cit.ase.ares.api.*` is excluded from the ArchUnit import and classified as infrastructure by WALA)
- In WALA mode: all packages on the `WalaPathClassification.INFRA_PREFIXES` list (`java.`, `javax.`, `sun.`, `jdk.`, `com.sun.`, `net.bytebuddy.`, `org.aspectj.`, `com.ibm.wala.`, `com.tngtech.archunit.`, plus the reproducibility test-helper packages `anonymous.toolclasses.` and `metatest.`) and all classes loaded by the JDK boot/extension class loaders
- Classes on the `allowedClasses` allow-list

**Restricted Code (Subject to Security Checks):**
- ArchUnit mode: **every** class imported from the analysed class path (there is no package-based trust boundary; only Ares' own `/de/tum/cit/ase/ares/api/` classes are excluded from the import)
- WALA mode: every class whose methods become entry points; the entry-point set is narrowed to the package directory derived from the analysed classpath (see [5.2](#52-finding-entry-points)), not by a policy `restrictedPackage` setting

**Security Assumptions:**
- Student code is compiled and available as `.class` files
- Class files have not been tampered with after compilation
- Call graph accurately represents possible execution paths (WALA mode)

---

# 2. Ares Monitors Thread Methods

Each analysis mode loads its forbidden thread methods from its own template file. The two lists overlap in their core (Thread.start, executors, parallel streams) but differ substantially in size and content: the ArchUnit list contains **378 entries**, the WALA list contains **90 entries** (see [Section 3](#3-monitored-thread-system-methods)).

**Template Locations:**
- **ArchUnit**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/archunit/methods/thread-manipulation-methods.txt`
- **WALA**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/wala/methods/thread-manipulation-methods.txt`

**What is Architecture Testing?**

Instead of intercepting method calls at runtime (AOP approach), architecture testing analyses the compiled bytecode to detect which thread creation methods the student code accesses. This happens during the test phase, before the code actually runs.

**Two Analysis Approaches:**
- **ArchUnit**: Fast static analysis of class dependencies
- **WALA**: Precise call graph analysis with path classification (critical for thread analysis!)

---

## 2.1 ArchUnit Mode (Static Dependency Analysis)

**Framework:** TNGs ArchUnit (https://www.archunit.org/)

**How it works:**
```java
// Define rule (see JavaArchunitTestCaseCollection.createNoClassShouldHaveMethodRule)
ArchRule rule = ArchRuleDefinition.noClasses()
    .that(isNotAllowedClass(allowedClasses))  // exempt allow-listed classes
    .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>(ruleName) {
        @Override
        public boolean test(JavaAccess<?> javaAccess) {
            return forbiddenMethods.stream()
                .anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
        }
    }))
    .as(ruleName);  // localised: "Manipulates threads"

// Execute rule
rule.check(javaClasses);  // Throws AssertionError if violated
```

**Analysis Process:**
1. **Import Classes**: Load `.class` files using `ClassFileImporter`
2. **Analyse Dependencies**: Build class dependency graph
3. **Check Access Patterns**: Detect direct and transitive method calls
4. **Report Violations**: Throw `AssertionError` with call chain details

**Strengths:**
- ✅ Fast analysis (no call graph construction)
- ✅ Detects transitive dependencies (A→B→C where C is forbidden)
- ✅ Simple configuration
- ✅ Good error messages with class names

**Limitations:**
- ⚠️ Less precise than WALA for complex call patterns
- ⚠️ May have false positives for conditional code paths
- ⚠️ Cannot determine if code path is actually reachable at runtime
- ⚠️ May not handle lambda expressions as precisely

**Example Violation Detection:**
```java
// Student Code
class StudentCode {
    void processInParallel() {
        Helper.createWorkerThread();  // Indirect call
    }
}

class Helper {
    void createWorkerThread() {
        new Thread(() -> System.out.println("Worker")).start();  // Forbidden!
    }
}

// ArchUnit detects: StudentCode → Helper.createWorkerThread → Thread.start
// Reports: "StudentCode transitively accesses <java.lang.Thread.start()> by [StudentCode.processInParallel()->Helper.createWorkerThread()]"
```

---

## 2.2 WALA Mode (Call Graph Analysis)

**Framework:** IBM WALA (T.J. Watson Libraries for Analysis)

**How it works:**
```java
// Build call graph
CallGraph cg = new CustomCallgraphBuilder(classPath).buildCallGraph(classPath);

// Check the rule (see WalaRule.check)
WalaRule rule = JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS;
rule.check(cg, allowedClasses);  // Throws AssertionError if violated
```

**Analysis Process (WalaRule.check):**
1. **Create Analysis Scope**: Define which classpath entries and JDK classes to include/exclude
2. **Build Class Hierarchy**: Resolve all type relationships
3. **Construct Call Graph**: Map all possible method call relationships
4. **Collect Forbidden Sinks**: Find every call-graph node whose signature matches a forbidden method
5. **Forward Reachability**: BFS from the entry points; only sinks reachable from student entry points are considered
6. **Per-Sink Reverse Walk**: For each reachable sink, reverse-walk through infrastructure frames to every distinct nearest-student approach and classify the reconstructed path (see [5.4](#54-path-classification-and-false-positive-filtering))

**Strengths:**
- ✅ Very precise call path detection
- ✅ Understands complex call patterns (lambdas, method references, executors)
- ✅ **Path classification** attributes violations to the nearest student frame and suppresses transitive-JDK false positives
- ✅ Provides the caller, forbidden method, declaring class, line number, and entry point in the error message
- ✅ Models runtime behaviour more accurately

**Limitations:**
- ⚠️ Slower analysis (call graph construction is expensive; mitigated by per-JVM and disk caches)
- ⚠️ Requires more memory
- ⚠️ More complex configuration (exclusions, entry points)
- ⚠️ May still have false positives for very dynamic code

**Example Violation Detection:**
```java
// Student Code
class StudentCode {
    void processAsync() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> processData());  // Lambda creates thread - Forbidden!
    }

    void processData() {
        // ... processing logic
    }
}

// WALA detects:
// StudentCode.processAsync is the nearest student frame reaching the forbidden
// sink ExecutorService.submit (itself already on the forbidden list)
// Reports: Method <de.student.StudentCode.processAsync()> calls method
// <java.util.concurrent.ExecutorService.submit(java.lang.Runnable)> ...
```

**Special WALA Feature for Thread Analysis: Path Classification**

Thread creation is extensively used by JDK internals (file I/O, networking, class loading). WALA mode therefore classifies every reconstructed path before reporting it, using `WalaPathClassification`:

```java
// WalaPathClassification.java
public static final List<String> INFRA_PREFIXES = List.of(
    "java.", "javax.", "sun.", "jdk.", "com.sun.",
    "de.tum.cit.ase.ares.api.", "net.bytebuddy.", "org.aspectj.",
    "com.ibm.wala.", "com.tngtech.archunit.",
    "anonymous.toolclasses.", "metatest.");

static final List<String> TRANSITIVE_FALSE_POSITIVE_PREFIXES = List.of(
    "java.", "javax.", "sun.", "jdk.", "com.sun.",
    "de.tum.cit.ase.ares.", "net.bytebuddy.", "org.aspectj.",
    "com.ibm.wala.", "com.tngtech.archunit.");
```

- **`nearestStudentFrame(path)`** scans the path from the forbidden sink back towards the entry point and returns the first frame that is *not* infrastructure (not in `INFRA_PREFIXES` and not loaded by the JDK boot/extension class loader). If the entire path is infrastructure, the path is dropped.
- **`isFalsePositiveTransitivePath(path, studentIdx)`** suppresses the path when every frame strictly between the nearest student frame and the sink is a transitive-false-positive frame (JDK or framework internals). This is the case when student code calls a permitted JDK API (e.g. `BufferedReader`) whose internal implementation transitively reaches a forbidden method such as `Thread.start()`.
- **Direct violations are never suppressed**: if the student frame is immediately adjacent to the forbidden sink, the path is always reported.
- The two project test-helper packages (`anonymous.toolclasses.`, `metatest.`) count as infrastructure for frame attribution but deliberately **not** for false-positive suppression, so a student call routed through such a helper into a forbidden JDK API is still reported.

**Why This Classification Is Critical:**

Without it, WALA would report false positives for innocent operations:
- ❌ `Files.list()` → internal JDK machinery may reach thread-related methods
- ❌ `Class.forName()` → class loader internals
- ❌ `InetAddress.getByName()` → DNS lookup internals

With classification:
- ✅ All-infrastructure paths and transitive-JDK side effects are ignored
- ✅ Genuine student code creating threads is detected and attributed to the correct student frame

---

# 3. Monitored Thread System Methods

Each mode loads its forbidden methods from its own template file:

**Template Locations:**
- **ArchUnit**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/archunit/methods/thread-manipulation-methods.txt` (378 entries)
- **WALA**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/wala/methods/thread-manipulation-methods.txt` (90 entries)

The lists are **not identical**: the WALA list is a focused set of thread creation and manipulation entry points, while the ArchUnit list additionally covers a large number of thread-adjacent JDK APIs (see the grouped overview in [3.2](#32-thread-system---create-operations-without-parameters)). The entries below are annotated where they are present in only one of the two files.

---

## 3.1 THREAD SYSTEM - CREATE Operations (With Parameters)

**Security Component:** Thread creation monitor (methods that receive a task/runnable as parameter)

**Monitored Methods (present in both methods files unless noted):**

**java.lang.Thread:**
- `Thread.startVirtualThread(Runnable)`
- `Thread(...)` constructors (`Thread()`, `Thread(Runnable)`, `Thread(Runnable, String)`, `Thread(String)`, `Thread(ThreadGroup, Runnable)`, `Thread(ThreadGroup, Runnable, String)`, `Thread(ThreadGroup, Runnable, String, long)`, ...)

**java.lang.Thread.Builder (ArchUnit list only):**
- `Thread.Builder.start(Runnable)`
- `Thread.Builder.OfPlatform.start(Runnable)`

**java.lang.ThreadBuilders (WALA list only):**
- `ThreadBuilders$PlatformThreadFactory.newThread(Runnable)`
- `ThreadBuilders$PlatformThreadBuilder.unstarted(Runnable)`

**java.util.concurrent.Executor:**
- `Executor.execute(Runnable)`

**java.util.concurrent.ExecutorService:**
- `ExecutorService.execute(Runnable)`
- `ExecutorService.submit(Runnable)`
- `ExecutorService.submit(Runnable, Object)`
- `ExecutorService.submit(Callable)`
- `ExecutorService.invokeAll(Collection)` (also the timed overload)
- `ExecutorService.invokeAny(Collection)` (also the timed overload)

**java.util.concurrent.AbstractExecutorService:**
- `AbstractExecutorService.submit(Runnable)`
- `AbstractExecutorService.submit(Runnable, Object)`
- `AbstractExecutorService.submit(Callable)`
- `AbstractExecutorService.invokeAll(Collection)` (also the timed overload)
- `AbstractExecutorService.invokeAny(Collection)` (also the timed overload)

**java.util.concurrent.ThreadPoolExecutor:**
- `ThreadPoolExecutor.execute(Runnable)`
- `ThreadPoolExecutor.submit(Runnable)`
- `ThreadPoolExecutor.submit(Runnable, Object)`
- `ThreadPoolExecutor.submit(Callable)`
- WALA additionally lists `ThreadPoolExecutor.shutdown()`, `ThreadPoolExecutor.shutdownNow()` and a class-level `java.util.concurrent.ThreadPoolExecutor` entry

**java.util.concurrent.Executors (factory methods):**
- `Executors.newCachedThreadPool()` / `newCachedThreadPool(ThreadFactory)`
- `Executors.newFixedThreadPool(int)` / `newFixedThreadPool(int, ThreadFactory)`
- `Executors.newSingleThreadExecutor()` / `newSingleThreadExecutor(ThreadFactory)`
- ArchUnit additionally lists `Executors.newVirtualThreadPerTaskExecutor()`

**java.util.concurrent.ScheduledExecutorService:**
- `ScheduledExecutorService.schedule(Runnable, long, TimeUnit)`
- `ScheduledExecutorService.schedule(Callable, long, TimeUnit)`
- `ScheduledExecutorService.scheduleAtFixedRate(Runnable, long, long, TimeUnit)`
- `ScheduledExecutorService.scheduleWithFixedDelay(Runnable, long, long, TimeUnit)`

**java.util.concurrent.ScheduledThreadPoolExecutor:**
- `ScheduledThreadPoolExecutor.schedule(Runnable, long, TimeUnit)`
- `ScheduledThreadPoolExecutor.schedule(Callable, long, TimeUnit)`
- `ScheduledThreadPoolExecutor.scheduleAtFixedRate(Runnable, long, long, TimeUnit)`
- `ScheduledThreadPoolExecutor.scheduleWithFixedDelay(Runnable, long, long, TimeUnit)`

**java.util.concurrent.ForkJoinPool:**
- `ForkJoinPool.execute(Runnable)`
- `ForkJoinPool.submit(Runnable)`
- `ForkJoinPool.submit(Callable)`

**java.util.concurrent.CompletableFuture:**
- `CompletableFuture.runAsync(Runnable)`
- `CompletableFuture.runAsync(Runnable, Executor)`
- `CompletableFuture.supplyAsync(Supplier)`
- `CompletableFuture.supplyAsync(Supplier, Executor)`
- `CompletableFuture.thenApplyAsync(Function)`
- `CompletableFuture.thenApplyAsync(Function, Executor)`
- `CompletableFuture.thenCombineAsync(CompletionStage, BiFunction)` (also the Executor overload)
- `CompletableFuture.thenCombine(CompletionStage, BiFunction)`

**java.util.concurrent.ThreadFactory:**
- `ThreadFactory.newThread(Runnable)`

**java.util.Timer:**
- `Timer.<init>` (all constructors)

---

## 3.2 THREAD SYSTEM - CREATE Operations (Without Parameters)

**Security Component:** Thread creation monitor (methods that start threads without task parameter)

**Monitored Methods (present in both files):**

**java.lang.Thread:**
- `Thread.start()`

**java.util.Collection:**
- `Collection.parallelStream()`

**java.util.stream.Stream:**
- `Stream.parallel()`

**java.util.stream.BaseStream:**
- `BaseStream.parallel()`

**Additional ArchUnit-only coverage (grouped overview of the 378-entry list):**

The ArchUnit list goes far beyond thread *creation* and also flags thread *manipulation* and thread-adjacent APIs:
- **Thread lifecycle and interrogation**: `Thread.sleep(...)`, `Thread.join(...)`, `Thread.yield()`, `Thread.interrupt()` / `interrupted()` / `isInterrupted()`, `Thread.isAlive()`, `Thread.run()`, priority/daemon/name setters and getters, `Thread.getAllStackTraces()`, `Object.wait(long)`
- **ThreadGroup manipulation**: constructors, `interrupt()`, `enumerate(...)`, `setMaxPriority(...)`, `list()`, ...
- **ThreadLocal**: `ThreadLocal.get()` / `set(Object)` / `remove()`, `ThreadLocalRandom.current()`
- **Locks and synchronisers**: `LockSupport.park*` / `unpark(...)`, `AbstractQueuedSynchronizer` / `AbstractQueuedLongSynchronizer` (incl. their `ConditionObject.await*` methods), `StampedLock`, `ReentrantLock.toString()`
- **Coordination primitives**: `SynchronousQueue`, `Exchanger`, `LinkedTransferQueue`, `DelayQueue`, `ForkJoinTask.fork()` / `get()` / `quietlyJoin(...)`, `FutureTask`, `CountedCompleter`
- **Modern concurrency**: `Executors.newVirtualThreadPerTaskExecutor()`, `StructuredTaskScope` (constructors and `fork(Callable)`), `SubmissionPublisher.subscribe(...)`
- **Timers**: `java.util.Timer` constructors, `TimeUnit.sleep(long)` / `timedJoin(...)`
- **AWT / Swing / sound / print internals**: `java.awt.EventQueue.<init>()`, `JTable.print(...)`, `sun.awt.*`, `com.sun.media.sound.*`, ...
- **RMI internals**: `java.rmi.server.UID.<init>()`, `sun.rmi.*`
- **JDK-internal and GraalVM helpers**: `jdk.internal.*`, `org.graalvm.*`, `sun.*`

---

**Method Signature Format:**

The two files use different signature formats. Entries omit return types; blank lines and lines whose first non-whitespace character is `#` are ignored by `FileTools.readMethodsFile`.

ArchUnit (source form):
```
java.lang.Thread.start()
java.lang.Thread.sleep(long, int)
java.util.concurrent.ExecutorService.submit(java.util.concurrent.Callable)
java.util.concurrent.CompletableFuture.runAsync(java.lang.Runnable)
```

WALA (JVM-descriptor parameters):
```
java.lang.Thread.start()
java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)
java.util.concurrent.ThreadPoolExecutor.execute(Ljava/lang/Runnable;)
java.util.concurrent.CompletableFuture.runAsync(Ljava/lang/Runnable;)
```

ArchUnit matches entries as prefixes of `JavaAccess.getTarget().getFullName()` (after converting Java array notation such as `byte[]` to the JNI form `[B`). WALA matches with the return type stripped from the actual signature and the descriptors normalised (see `WalaRule.matchesForbiddenMethod`).

**Why These Methods?**

These are the **comprehensive set of APIs** in Java for creating and managing threads:
- `Thread.start()`: Direct thread creation
- `Executor` family: Modern concurrent programming APIs
- `CompletableFuture`: Asynchronous computation
- `parallelStream()`: Parallel stream processing (implicitly creates threads)

**Coverage:**
- ✅ All variants of thread creation (direct, executor-based, async)
- ✅ Lambda expressions passed to executors
- ✅ Parallel streams (implicit thread creation)
- ✅ Virtual threads (Java 21+)

---

# 4. ArchUnit Analysis Flow

## 4.1 Loading Java Classes

**Step 1: Import Compiled Classes**

At runtime, Ares imports the classes via `ArchitectureMode.getJavaClasses(classPath)`:

```java
JavaClasses javaClasses = new ClassFileImporter()
    .withImportOption(location ->
        !location.toString().replace("\\", "/").contains("/de/tum/cit/ase/ares/api/"))
    .importPath(classPath);
```

**What happens:**
1. `ClassFileImporter` scans the given path for `.class` files
2. Loads class metadata (methods, fields, dependencies)
3. Excludes Ares' own framework classes (`/de/tum/cit/ase/ares/api/`), so the rules never flag Ares' trusted advice code
4. Creates a `JavaClasses` object containing all analysed classes

Note: the **generated-template** variant (see [6.1](#61-writing-architecture-test-cases)) differs slightly. It additionally uses `ImportOption.Predefined.DO_NOT_INCLUDE_TESTS` and imports by package (`importPackages(...)`) instead of by path.

---

## 4.2 Defining Architecture Rules

**Step 2: Create Architecture Rule**

`JavaArchunitTestCaseCollection.createNoClassShouldHaveMethodRule` builds the rule; the forbidden methods are loaded lazily from the template file on the first check:

```java
// Rule name is localised: "Manipulates threads"
// (messages key: security.architecture.manipulate.threads)
ArchRule rule = ArchRuleDefinition.noClasses()
    .that(isNotAllowedClass(allowedClasses))  // exempt allow-listed classes
    .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>(ruleName) {
        private Set<String> forbiddenMethods;

        @Override
        public boolean test(JavaAccess<?> javaAccess) {
            if (forbiddenMethods == null) {
                forbiddenMethods = FileTools.readMethodsFile(
                        FileTools.readFile(FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS))
                    .stream().map(JavaArchunitTestCaseCollection::convertArrayNotation)
                    .collect(Collectors.toSet());
            }
            return forbiddenMethods.stream().filter(method -> !method.isEmpty())
                .anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
        }
    }))
    .as(ruleName);
```

**Rule Components:**
- **DescribedPredicate**: Tests if a method access is forbidden (prefix match on the target's full name)
- **TransitivelyAccessesMethodsCondition**: Finds transitive access paths
- **`.that(isNotAllowedClass(...))`**: Exempts classes on the `allowedClasses` allow-list (delegating to `JavaArchitectureTestCase.isAllowedClass`, which also matches nested classes on the `$` boundary)
- **ArchRule**: Complete rule that can be checked against `JavaClasses`

The pre-built constant `JavaArchunitTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS` is the unfiltered variant (empty allow-list), used by the generated-template path. The runtime execution path instead obtains the rule via `allowAwareRuleFor(THREAD_CREATION, allowedClasses, allowedPackages)`.

---

## 4.3 Executing Rules

**Step 3: Run Architecture Test**

The runtime path is `JavaArchunitTestCase.executeArchitectureTestCase`, which internally runs the rule and converts violations:

```java
// inside JavaArchunitTestCase.runRuleAndCapture
try {
    JavaArchunitTestCaseCollection
        .allowAwareRuleFor(supported, getAllowedClasses(), allowedPackages)
        .check(javaClasses);
} catch (AssertionError ae) {
    JavaArchitectureTestCase.parseErrorMessage(ae);  // throws SecurityException
}
```

**When a violation is found**, ArchUnit produces an `AssertionError` such as:
```
Architecture Violation [Priority: MEDIUM] - Rule 'Manipulates threads' was violated (1 times):
Method <de.student.StudentCode.processAsync()> calls method <java.util.concurrent.ExecutorService.submit(java.util.concurrent.Callable)> in (StudentCode.java:15) accesses <java.util.concurrent.ExecutorService.submit(java.util.concurrent.Callable)>
```

**The error is converted** by `parseErrorMessage` into a `SecurityException` using the format from `messages.properties` (key `security.archunit.violation.error`, line 97):
```
Ares Security Error (Reason: Student-Code; Stage: Execution): %s tried to illegally %s via %s (called by %s) but was blocked by Ares.
```
For the thread rule, the action placeholder is `manipulate threads` (mapped from the rule name "Manipulates threads"), e.g.:
```
Ares Security Error (Reason: Student-Code; Stage: Execution): de.student.StudentCode.processAsync() tried to illegally manipulate threads via java.util.concurrent.ExecutorService.submit(java.util.concurrent.Callable) (called by de.student.StudentCode) but was blocked by Ares.
```

The outcome (pass or the parsed `SecurityException`) is cached in-memory per (rule category, allow-list, `JavaClasses` instance), so repeated executions in the same JVM replay the cached verdict instead of re-scanning.

---

## 4.4 Transitive Access Detection

**How TransitivelyAccessesMethodsCondition Works:**

```java
public class TransitivelyAccessesMethodsCondition extends ArchCondition<JavaClass> {

    @Override
    public void check(JavaClass clazz, ConditionEvents events) {
        // Get all method accesses from this class
        for (JavaAccess<?> accessInsideClass : clazz.getAccessesFromSelf()) {
            // Find transitive path to a violating (forbidden) method
            List<JavaAccess<?>> path = transitiveAccessPath
                .findPathFromViolatingMethodTo(accessInsideClass);

            if (!path.isEmpty()) {
                // Report violation with complete call chain
                events.add(newTransitiveAccessPathFoundEvent(accessInsideClass, path));
            }
        }
    }
}
```

The inner class `TransitiveAccessPath` performs a depth-first search: `findPathFromViolatingMethodTo(JavaAccess)` recursively follows the accesses originating from each target method (avoiding cycles via a visited set) until the predicate matches, then returns the reversed access chain. `newTransitiveAccessPathFoundEvent` renders the chain and registers it via `SimpleConditionEvent.satisfied(access, message)`; because the rule is a `noClasses().should(...)` rule, a *satisfied* condition event constitutes a violation.

**Example Path Detection:**

```
StudentCode.processData() calls:
  ├─ Helper.processInParallel() ✓ (not forbidden, continue searching)
  │   └─ ExecutorService.submit() ✗ (FORBIDDEN! Report path)
  └─ System.out.println() ✓ (not forbidden, ignore)

Result: Path found = [StudentCode.processData → Helper.processInParallel → ExecutorService.submit]
```

---

# 5. WALA Analysis Flow

## 5.1 Building the Call Graph

All steps below are implemented in `CustomCallgraphBuilder`.

**Step 1: Filter the Classpath and Create the Analysis Scope**

Before WALA sees the classpath, `filterClassPath` drops entries matching `CLASSPATH_EXCLUDE_SUBSTRINGS`: compiled test outputs, JUnit/test-platform libraries, Mockito, AssertJ/Hamcrest/jqwik, JaCoCo, Gradle test-runner infrastructure, static-analysis tooling, Ares' own jar, the WALA runtime, and the AspectJ runtime. It also widens the scope with the verified helper subdirectory (`anonymous/toolclasses`) so student superclasses can be resolved without pulling in every other category's classes.

```java
AnalysisScope scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
    filteredClassPath,
    FileTools.readFile(FileTools.resolveFileOnSourceDirectory(
        "templates", "architecture", "java", "exclusions.txt")));

ClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);
```

**Exclusions File (excerpt of `exclusions.txt`):**
```
jdk/.*
java/time/.*
sun/security/.*
java/text/.*
java/util/function.*
...
```
(These JDK areas are excluded from the scope to keep the analysis fast; they are not relevant to the rules.)

The scope and class hierarchy are cached per filtered classpath in a bounded LRU, so subsequent tests in the same JVM reuse them.

**Step 2: Collect Entry Points** (see [5.2](#52-finding-entry-points))

**Step 3: Build the Call Graph**

```java
// Configure analysis options with the scope and the collected entry points
AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);

// Create call graph builder (0-1-CFA algorithm)
CallGraphBuilder<?> builder = Util.makeZeroOneCFABuilder(
    Language.JAVA, options, new AnalysisCacheImpl(), classHierarchy);

// Wrap the default selectors so JDK (primordial) methods stay opaque:
// the call edge from student code to a JDK method is still recorded,
// but WALA does not load or analyse the JDK method's bytecode.
options.setSelector(new JdkOpaqueMethodTargetSelector(
    options.getMethodTargetSelector(), classHierarchy));

// Build call graph
CallGraph callGraph = builder.makeCallGraph(options, null);
```

The `JdkOpaqueMethodTargetSelector` returns an empty `SummarizedMethod` for primordial (JDK) targets, so forbidden JDK sinks such as `Thread.start()` appear as call-graph nodes (which `WalaRule` needs) without WALA descending into JDK bytecode. Built call graphs are cached per classpath pair in a bounded LRU.

**Call Graph Structure:**
```
CallGraph:
  ├─ CGNode: StudentCode.processAsync()
  │   ├─ successor: Executors.newFixedThreadPool()   (opaque JDK node)
  │   └─ successor: ExecutorService.submit()          (opaque JDK node, FORBIDDEN!)
  └─ ...
```

---

## 5.2 Finding Entry Points

**Entry Point Selection:**

`ReachabilityChecker.getEntryPointsFromStudentSubmission(classPath, classHierarchy)` turns **every declared method** of every class loaded by the Application class loader into a `DefaultEntrypoint`. There is **no** special-casing of `main`; it becomes an entry point like any other method.

The narrowing happens afterwards in `CustomCallgraphBuilder.buildCallGraph`: the entry points are filtered to the package directory derived from the analysed classpath (e.g. `build/classes/java/main/anonymous/foo/bar` → prefix `Lanonymous/foo/bar/`), so each architecture rule only flags violations in the classes under test rather than spilling into unrelated categories that share the wider scope:

```java
String narrowPackagePrefix = derivePackagePrefix(classPathToAnalyze);
List<DefaultEntrypoint> customEntryPoints = ReachabilityChecker
    .getEntryPointsFromStudentSubmission(filterClassPath(classPathToAnalyze), classHierarchy)
    .stream()
    .filter(ep -> narrowPackagePrefix == null
        || ep.getMethod().getDeclaringClass().getName().toString().startsWith(narrowPackagePrefix))
    .collect(Collectors.toCollection(ArrayList::new));
```

Entry-point lists are cached per (class hierarchy, classpath) in a bounded LRU.

**Entry Point Representation:**
```java
DefaultEntrypoint(
    methodReference = "Lde/student/StudentCode.processAsync()V",
    classHierarchy = <ClassHierarchy>
)
```

---

## 5.3 Rule Checking with WalaRule

**Step 4: Check the Rule Against the Call Graph**

The rule check is implemented in `WalaRule.check(CallGraph, Set<ClassPermission>)` (WalaRule.java:65-98). It is a **per-sink reverse-reachability** analysis, not a single forward DFS:

1. **Collect forbidden sinks**: every `CGNode` whose method signature matches a forbidden entry (`isForbidden`; matching strips return types and normalises descriptors). The sinks are sorted by signature so the reported violation is deterministic across JVM runs.
2. **Forward reachability**: a BFS over successor edges from the entry points (`forwardReachableFromEntrypoints`). If the call graph has **no entry points at all**, the check **fails closed** with a `SecurityException` (message key `security.architecture.wala.entrypoints.empty`) instead of silently passing, because an empty entry set means the analysis was mis-scoped.
3. **Per-sink evaluation** (`evaluateSink`): for each entry-reachable sink, reverse-walk from the sink through *infrastructure frames only* and evaluate each distinct nearest-student approach `[nearestStudentFrame, ...infra..., sink]` the moment it is found. Each approach gets an entry-to-student prefix via reverse BFS (`reversePathToEntry`; if the nearest student frame is allow-listed, a prefix through a non-allowed student ancestor is preferred), and the full witness path is classified by `evaluatePath`.
4. **Throw on the first genuine violation**: a path that survives classification produces an `AssertionError`. A backstop of 64 approaches per sink (`MAX_APPROACHES_PER_SINK`) bounds pathological graphs; hitting the bound is logged, never silently passed.

**Why not a single forward DFS?** The previous implementation (`ReachabilityChecker.findReachableMethods` delegating to `CustomDFSPathFinder`) marked nodes globally visited and therefore reported each sink on exactly one path; an allow-listed or false-positive caller discovered first could permanently mask a genuine violation by a different caller of the same sink. Both classes still exist in the codebase but are **legacy** for rule checking: `ReachabilityChecker` remains in use only for entry-point collection, and `CustomDFSPathFinder` (which also consumed the `false-positives-file.txt` exclusion list) has been superseded by the per-sink reverse walk in `WalaRule`.

**Violation Message:**

A genuine violation raises an `AssertionError` with the localised format (`messages.properties` key `security.architecture.method.call.message`, line 115):
```
'%s'\n Method <%s> calls method <%s> in (%s.java:%d) accesses <%s>
```
with the arguments (rule name, caller signature, forbidden signature, declaring class of the forbidden method, line number, entry-point signature). WALA's JVM-descriptor signatures are converted to source form (`formatJvmSignature`) so the message matches ArchUnit's output format. Example:
```
'Manipulates threads'
 Method <de.student.StudentCode.processAsync()> calls method <java.util.concurrent.ExecutorService.submit(java.lang.Runnable)> in (ExecutorService.java:15) accesses <de.student.StudentCode.processAsync()>
```

**Example Reachability Detection:**

```
Entry: StudentCode.processAsync()
  ↓ calls
ExecutorService.submit(lambda)   ← FORBIDDEN SINK (on the WALA methods list)

Nearest student frame: StudentCode.processAsync()  (adjacent to the sink → never suppressed)
Result: AssertionError → parsed into a SecurityException
```

---

## 5.4 Path Classification and False Positive Filtering

**Challenge:** JDK classes extensively use threads internally for legitimate operations:
- File I/O: directory traversal, asynchronous channels
- Networking: DNS lookups
- Class Loading: parallel class loading

**Without filtering:** Almost every student program would be flagged!

**Solution:** Every reconstructed path is classified by `WalaPathClassification` before it is reported.

```java
// WalaPathClassification.java:59-79
public static final List<String> INFRA_PREFIXES = List.of(
    "java.", "javax.", "sun.", "jdk.", "com.sun.",
    "de.tum.cit.ase.ares.api.", "net.bytebuddy.", "org.aspectj.",
    "com.ibm.wala.", "com.tngtech.archunit.",
    "anonymous.toolclasses.", "metatest.");

static final List<String> TRANSITIVE_FALSE_POSITIVE_PREFIXES = List.of(
    "java.", "javax.", "sun.", "jdk.", "com.sun.",
    "de.tum.cit.ase.ares.", "net.bytebuddy.", "org.aspectj.",
    "com.ibm.wala.", "com.tngtech.archunit.");
```

**Classification Logic (WalaRule.evaluatePath):**

1. **Find the nearest student frame** (`nearestStudentFrame`): scan the path from the forbidden sink back towards the entry point; the first frame that is not infrastructure (not in `INFRA_PREFIXES`, not loaded by the JDK boot/extension class loader, not malformed) is the student frame. **All-infrastructure paths are dropped.**
2. **Suppress transitive-JDK false positives** (`isFalsePositiveTransitivePath`): if every frame strictly between the student frame and the sink is a transitive-false-positive frame, the student merely called a permitted JDK/framework API whose internals reached the forbidden method → the path is ignored. **Direct violations (student frame adjacent to the sink) are never suppressed.**
3. **Apply the class allow-list**: with a non-empty `allowedClasses` set, a path is exempt ONLY when *every* student frame on it is allow-listed; if a non-allowed student frame reaches the forbidden API through an allow-listed helper, the violation is still reported against that non-allowed frame.
4. **Report**: otherwise the path is a genuine violation and the localised `AssertionError` is raised.

**Example Classification:**

**Path 1 (Ignored - Transitive JDK Side Effect):**
```
StudentCode.listFiles()               (nearest student frame)
  ↓
java.nio.file.Files.list(Path)        (JDK frame)
  ↓
sun.nio.fs.UnixDirectoryStream.<init>()  (JDK frame)
  ↓
java.lang.Thread.start()              (forbidden sink)

Result: IGNORED — every frame between the student frame and the sink is JDK
        (isFalsePositiveTransitivePath == true)
```

**Path 2 (Detected - Direct Violation):**
```
StudentCode.processAsync()            (nearest student frame, adjacent to sink)
  ↓
java.lang.Thread.start()              (forbidden sink)

Result: VIOLATION — direct violations are never suppressed
```

**Path 3 (Detected - Executor Service):**
```
StudentCode.parallelProcess()         (nearest student frame, adjacent to sink)
  ↓
java.util.concurrent.ExecutorService.submit(...)  (forbidden sink)

Result: VIOLATION — the forbidden API is the call the student wrote
```

**Path 4 (Detected - Routed Through a Project Test Helper):**
```
StudentCode.trickyCall()              (nearest non-helper student caller)
  ↓
anonymous.toolclasses.Helper.spawn()  (infra for attribution, but NOT a
  ↓                                    transitive-false-positive frame)
java.lang.Thread.start()              (forbidden sink)

Result: VIOLATION — project test helpers are deliberately excluded from
        TRANSITIVE_FALSE_POSITIVE_PREFIXES, so routing through them does
        not launder a forbidden call
```

**Legacy false-positives file:** the older DFS-based path finder (`CustomDFSPathFinder`) additionally skipped the method signatures listed in `templates/architecture/java/wala/false-positives/false-positives-file.txt` (`FileHandlerConstants.FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS`). That mechanism is superseded by the prefix-based classification above for rule checking; the file's content still feeds into the WALA disk-cache fingerprint so edits invalidate cached verdicts.

**Why This Classification Is Essential:**

Without it, these innocent operations would be flagged:
- ❌ `Files.list("/tmp")` → false positive
- ❌ `Class.forName("MyClass")` → false positive
- ❌ `InetAddress.getByName("localhost")` → false positive

With it:
- ✅ JDK-internal side effects are ignored
- ✅ Genuine student code creating threads is detected
- ✅ Violations are attributed to the correct (nearest, non-exempt) student frame
- ✅ Architecture testing becomes practical for thread analysis

---

# 6. Test Case Generation

## 6.1 Writing Architecture Test Cases

Architecture test cases can be **generated as Java code** for integration into test suites.

**ArchUnit Test Case Generation:**

```java
JavaArchunitTestCase testCase = JavaArchunitTestCase.archunitBuilder()
    .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.THREAD_CREATION)
    .allowedPackages(Set.of(new PackagePermission("java.lang")))
    .javaClasses(javaClasses)
    .build();

String testCode = testCase.writeArchitectureTestCase("ARCHUNIT", "");
```

The generator fills the placeholders (`${javaClasses}`) in the template `templates/architecture/java/archunit/rules/THREAD_CREATION.txt`.

**Generated Code Example:**
```java
@Test
public void threadSystemShouldNotBeAccessed() {
    javaClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .withImportOption(location -> {
            String path = location.toString().replace("\\", "/");
            return !path.contains("/de/tum/cit/ase/ares/api/");
        })
        .importPackages("de.student");
    JavaArchunitTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(javaClasses);
}
```

Note: the thread template does not emit an `allowedPackages` block (that placeholder is only used by the PACKAGE_IMPORT template), and the static `NO_CLASS_MUST_CREATE_THREADS` constant is the unfiltered rule without class exemptions.

**WALA Test Case Generation:**

```java
JavaWalaTestCase testCase = JavaWalaTestCase.walaBuilder()
    .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.THREAD_CREATION)
    .allowedPackages(Set.of(new PackagePermission("java.lang")))
    .javaClasses(javaClasses)
    .callGraph(callGraph)
    .build();

String testCode = testCase.writeArchitectureTestCase("WALA", "");
```

**Generated Code Example** (from `templates/architecture/java/wala/rules/THREAD_CREATION.txt`, `${callGraph}` filled in):
```java
@Test
public void threadSystemShouldNotBeAccessed() {
    callGraph = new de.tum.cit.ase.ares.api.architecture.java.wala.CustomCallgraphBuilder(
            System.getProperty("java.class.path"))
        .buildCallGraph(System.getProperty("java.class.path"));
    JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(callGraph);
}
```

---

## 6.2 Executing Architecture Test Cases

**Direct Execution (Without Code Generation):**

```java
JavaArchitectureTestCase testCase = JavaArchitectureTestCase.builder()
    .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.THREAD_CREATION)
    .allowedPackages(Set.of(new PackagePermission("java.lang")))
    .javaClasses(javaClasses)
    .callGraph(callGraph)  // null for ArchUnit mode
    .build();

try {
    testCase.executeArchitectureTestCase("ARCHUNIT", "");  // or "WALA"
} catch (SecurityException e) {
    System.err.println("Security violation detected: " + e.getMessage());
}
```

**Execution Flow:**

```
executeArchitectureTestCase()
  ↓
Switch on architectureMode
  ├─ ARCHUNIT → JavaArchunitTestCase.executeArchitectureTestCase()
  │   ↓
  │   in-memory outcome cache (per rule, allow-list, JavaClasses instance)
  │   ↓ on cache miss
  │   JavaArchunitTestCaseCollection.allowAwareRuleFor(THREAD_CREATION,
  │       allowedClasses, allowedPackages).check(javaClasses)
  │   ↓
  │   [AssertionError caught internally, parsed into SecurityException]
  │
  └─ WALA → JavaWalaTestCase.executeArchitectureTestCase()
      ↓
      HMAC-protected disk-backed outcome cache (JavaWalaTestCase.java:53-324;
      keyed by classpath, Ares-jar mtime, rule name, and SHA-256 fingerprints
      of class structure, bytecode, allow-lists, and the blocklist files) —
      on a cache hit the call graph is never built
      ↓ on cache miss
      callGraphSupplier.get()  (lazy call-graph construction)
      ↓
      JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(cg, allowedClasses)
      ↓ (with path classification)
      [AssertionError caught internally, parsed into SecurityException]
```

**Error Handling:**

The `AssertionError` never escapes `executeArchitectureTestCase`; it is caught inside `runRuleAndCapture` and converted via `JavaArchitectureTestCase.parseErrorMessage`. Callers therefore only see the `SecurityException`:

```java
try {
    testCase.executeArchitectureTestCase(architectureMode, aopMode);
} catch (SecurityException e) {
    // Already in the Ares error format:
    // "Ares Security Error (Reason: Student-Code; Stage: Execution):
    //  <caller> tried to illegally manipulate threads via <target> (called by <class>)
    //  but was blocked by Ares."
}
```

---

# 7. Conclusion

## Summary for Programming Instructors (TL;DR)

**What does Architecture Testing do?**
- ✅ Analyses **compiled bytecode** to detect forbidden thread creation operations
- ✅ Works **before code execution** - catches violations during testing phase
- ✅ Provides **two analysis modes**: Fast (ArchUnit) and Precise (WALA)
- ✅ Detects **transitive calls** - finds violations even through helper methods
- ✅ **Path classification** (WALA mode) - suppresses transitive-JDK false positives and attributes violations to the nearest student frame
- ✅ Generates **clear error messages** with caller, forbidden method, and entry point

**When do you need this?**
- When you want to prevent students from creating threads entirely
- For pre-submission checks (CI/CD pipelines)
- When runtime monitoring (AOP) is not feasible or desired
- For comprehensive code structure validation
- When you want to avoid false positives from JDK internal thread usage

**How does it work (simplified)?**
1. Compile student code to `.class` files
2. Architecture analysis scans bytecode for thread creation method calls
3. WALA mode classifies each call path and drops JDK-internal side effects
4. If forbidden patterns detected → Report violation with caller and forbidden method
5. Unlike AOP, this happens during testing, not when code runs

---

## Comparison: Architecture vs. AOP

| Aspect | Architecture (ArchUnit/WALA) | AOP (Byte Buddy/AspectJ) |
|--------|------------------------------|--------------------------|
| **Analysis Time** | Before execution (static) | During execution (runtime) |
| **Detection** | Analyses code structure | Intercepts method calls |
| **Granularity** | Binary (allowed/forbidden) | Quota-based permissions |
| **Performance Impact** | Analysis overhead only (mitigated by outcome caches) | Runtime overhead on every call |
| **False Positives** | Possible (WALA classifies paths) | None (only executed code checked) |
| **Coverage** | All code paths | Only executed paths |
| **Configuration** | Class-level exemptions (`allowedClasses`) plus WALA infra-prefix classification | Thread class + quota permissions |
| **Use Case** | Pre-submission validation | Runtime security enforcement |
| **Error Timing** | Test phase | Production execution |
| **JDK Internal Handling** | Classified as infrastructure (WALA mode) | Not needed (runtime checks actual code) |

---

## Technical Details

### **ArchUnit Mode (Static Analysis)**

**Implementation:**
- Uses ArchUnit's `ArchRule` and custom `TransitivelyAccessesMethodsCondition`
- Analyses class dependencies and method access patterns
- No call graph construction required
- No path classification (may report JDK-adjacent usage; the much longer methods list is tuned accordingly)

**Violation Example (raw AssertionError before conversion):**
```
Architecture Violation [Priority: MEDIUM] - Rule 'Manipulates threads' was violated (1 times):
Method <de.student.StudentCode.processAsync()> calls method <java.util.concurrent.ExecutorService.submit(java.util.concurrent.Callable)> in (StudentCode.java:15) accesses <java.util.concurrent.ExecutorService.submit(java.util.concurrent.Callable)>
```

**Best For:**
- Quick validation during development
- Broad detection of thread creation patterns
- When exact call paths are not critical
- When false positives are acceptable

---

### **WALA Mode (Call Graph Analysis with Path Classification)**

**Implementation:**
- Builds a call graph using IBM WALA (0-1-CFA, JDK methods kept opaque)
- Per-sink reverse-reachability analysis in `WalaRule.check`
- **Critical feature: path classification via `WalaPathClassification`**
- Reduces false positives dramatically

**Violation Example (raw AssertionError before conversion; format `security.architecture.method.call.message`):**
```
'Manipulates threads'
 Method <de.student.StudentCode.processAsync()> calls method <java.util.concurrent.ThreadPoolExecutor.execute(java.lang.Runnable)> in (ThreadPoolExecutor.java:15) accesses <de.student.StudentCode.processAsync()>
```

**Best For:**
- Precise violation detection
- Understanding exact call paths
- Production-grade security validation
- **Minimising false positives from JDK internal thread usage**
- Analysing complex concurrent code (lambdas, executors, CompletableFuture)

---

### **Forbidden Method Templates**

Both modes load forbidden methods from text files. Every non-empty, non-comment line is one signature; return types are omitted, and `#` comment lines are ignored.

**ArchUnit File Format (source form):**
```
java.lang.Thread.start()
java.lang.Thread.startVirtualThread(java.lang.Runnable)
java.util.concurrent.ExecutorService.submit(java.lang.Runnable)
java.util.concurrent.ThreadPoolExecutor.execute(java.lang.Runnable)
java.util.Collection.parallelStream()
java.util.stream.Stream.parallel()
```

**WALA File Format (JVM-descriptor parameters):**
```
java.lang.Thread.start()
java.lang.Thread.startVirtualThread(Ljava/lang/Runnable;)
java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)
java.util.concurrent.ThreadPoolExecutor.execute(Ljava/lang/Runnable;)
java.util.concurrent.ScheduledExecutorService.schedule(Ljava/lang/Runnable;JLjava/util/concurrent/TimeUnit;)
java.util.concurrent.CompletableFuture.runAsync(Ljava/lang/Runnable;)
java.util.Collection.parallelStream()
```

**Template Locations:**
- ArchUnit: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/archunit/methods/thread-manipulation-methods.txt`
- WALA: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/wala/methods/thread-manipulation-methods.txt`

---

### **Integration Example**

**Maven Project Setup:**
```xml
<dependencies>
    <dependency>
        <groupId>de.tum.cit.ase</groupId>
        <artifactId>ares</artifactId>
        <version>2.0.1-Beta9</version>
    </dependency>
</dependencies>
```

**Test Class:**
```java
@Test
void testNoThreadCreation() {
    // Load student classes
    JavaClasses javaClasses = new ClassFileImporter()
        .importPackages("de.student");

    // Build call graph for WALA mode
    String classPath = System.getProperty("java.class.path");
    CallGraph callGraph = new CustomCallgraphBuilder(classPath)
        .buildCallGraph(classPath);

    // Create test case
    JavaArchitectureTestCase testCase = JavaArchitectureTestCase.builder()
        .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.THREAD_CREATION)
        .allowedPackages(Set.of())
        .javaClasses(javaClasses)
        .callGraph(callGraph)  // null for ArchUnit mode
        .build();

    // Execute (WALA mode recommended for thread analysis due to its path classification)
    testCase.executeArchitectureTestCase("WALA", "");  // throws SecurityException on violation
}
```

---

**The architecture testing approach provides comprehensive security validation at compile/test time, complementing the runtime AOP approach for defence-in-depth security against thread creation attacks. WALA mode is particularly powerful for thread analysis due to its path classification, which suppresses JDK-internal false positives while attributing genuine violations to the responsible student code.**
