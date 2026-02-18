# Thread System Security Mechanism (Architecture Analysis)

## Table of Contents

1. [High-Level Overview](#1-high-level-overview)
   - [1.1 Architecture Analysis Approach](#11-architecture-analysis-approach)
   - [1.2 Configuration Settings](#12-configuration-settings)
   - [1.3 Summary: When Is Thread Creation Blocked?](#13-summary-when-is-thread-creation-blocked)
   - [1.4 What Code Is Trusted vs. Restricted?](#14-what-code-is-trusted-vs-restricted)
2. [Ares Monitors Thread Methods](#2-ares-monitors-thread-methods)
   - [2.1 THREAD SYSTEM - CREATE Operations (With Parameters)](#21-thread-system---create-operations-with-parameters)
   - [2.2 THREAD SYSTEM - CREATE Operations (Without Parameters)](#22-thread-system---create-operations-without-parameters)
3. [Student Code Triggers Security Check](#3-student-code-triggers-security-check)
4. [Ares Collects Information About the Thread Creation](#4-ares-collects-information-about-the-thread-creation)
   - [4.1 Loading Java Classes (ArchUnit)](#41-loading-java-classes-archunit)
   - [4.2 Building the Call Graph (WALA)](#42-building-the-call-graph-wala)
5. [Ares Validates the Thread Creation](#5-ares-validates-the-thread-creation)
   - [5.1 ArchUnit Mode: Static Analysis](#51-archunit-mode-static-analysis)
   - [5.2 WALA Mode: Call Graph Analysis](#52-wala-mode-call-graph-analysis)
   - [5.3 Transitive Access Detection](#53-transitive-access-detection)
   - [5.4 Reachability Analysis (WALA)](#54-reachability-analysis-wala)
   - [5.5 False Positive Filtering (WALA)](#55-false-positive-filtering-wala)
6. [Conclusion](#6-conclusion)

---

# 1. High-Level Overview

This document describes how Ares 2 prevents unauthorized thread creation in student code using **static code analysis** techniques via Architecture Testing frameworks.

**Key Difference from AOP Approach:**
- **AOP (Runtime)**: Monitors thread creation during program execution and enforces thread quotas in real-time
- **Architecture (Static)**: Analyzes compiled bytecode before execution to detect potential thread creation violations in the code structure

---

## 1.1 Architecture Analysis Approach

**What is Architecture Testing?**

Architecture testing validates that code follows specific structural rules by analyzing compiled bytecode. Instead of running the code and intercepting thread creation calls (AOP), it examines the program structure to find violations.

**Think of it like:**
- **AOP = Security Guard**: Checks thread quotas when threads are actually spawned
- **Architecture = Building Inspector**: Reviews code structure before execution to ensure no thread creation capabilities exist

**Two Analysis Frameworks:**

### **ArchUnit (Static Analysis)**
- **Type**: Pure static analysis using Archunit framework
- **Strength**: Fast, no call graph needed
- **Method**: Analyzes class dependencies and method calls in compiled bytecode
- **Use Case**: Detecting direct and transitive method access patterns to thread creation APIs

### **WALA (Dynamic Call Graph Analysis)**
- **Type**: Static analysis with dynamic modeling using IBM WALA framework
- **Strength**: Precise call path detection, understands complex call chains
- **Method**: Builds a complete call graph representing all possible method invocations
- **Use Case**: Finding reachable thread creation methods through complex call chains, including lambda expressions
- **Special Feature**: Advanced false positive filtering for JDK-internal thread usage

**Validation Flow:**

1. **Load Classes**: Import compiled `.class` files from classpath
2. **Build Analysis Model**: 
   - ArchUnit: Load class metadata and dependencies
   - WALA: Build complete call graph with entry points
3. **Define Rules**: Specify forbidden method patterns (thread creation operations)
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
- No thread quota management (no `threadNumberAllowedToBeCreated`) - Architecture testing detects ANY thread creation attempt
- No thread class allowlists (no `threadClassAllowedToBeCreated`) - All thread creation is flagged
- No AOP mode selection - Uses `architectureMode` instead
- Package-level permissions instead of thread-level permissions

---

## 1.3 Summary: When Is Thread Creation Blocked?

Access is **BLOCKED** 🔴 when **ALL** conditions are true:

1. **Architecture Mode Enabled**: `architectureMode == "ARCHUNIT"` or `architectureMode == "WALA"`
2. **Student Code Contains Thread Creation Calls**: Analysis detects method calls to thread creation APIs
3. **Calls Are Reachable**: The forbidden methods can be reached from student code (directly or transitively)
4. **Not in Allowed Packages**: The accessed classes are not in the `allowedPackages` list

**If ANY condition fails → No Violation Detected** 🟢

**Key Differences from AOP:**
- 🔴 Detected at analysis time (before execution), not at runtime
- 🔴 Blocks ALL thread creation attempts, not based on quotas or thread classes
- 🔴 Reports potential violations, even if the code path is never executed
- 🔴 No distinction between different thread types (all treated equally)

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
- All classes that call thread creation methods from the forbidden lists

**Security Assumptions:** 
- Student code is compiled and available as `.class` files
- Class files have not been tampered with after compilation
- Call graph accurately represents possible execution paths (WALA mode)

---

# 2. Ares Monitors Thread Methods

Both ArchUnit and WALA modes monitor the same set of thread creation methods, loaded from template files:

**Template Locations:**
- **ArchUnit**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/archunit/methods/thread-manipulation-methods.txt`
- **WALA**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/wala/methods/thread-manipulation-methods.txt`

**What is Architecture Testing?**

Instead of intercepting method calls at runtime (AOP approach), architecture testing analyzes the compiled bytecode to detect which thread creation methods the student code accesses. This happens during the test phase, before the code actually runs.

**Two Analysis Approaches:**
- **ArchUnit**: Fast static analysis of class dependencies
- **WALA**: Precise call graph analysis with false positive filtering (critical for thread analysis!)

---

## 2.1 THREAD SYSTEM - CREATE Operations (With Parameters)

**Framework:** TNGs ArchUnit (https://www.archunit.org/)

**How it works:**
```java
// Define rule
ArchRule rule = noClasses()
    .should(new TransitivelyAccessesMethodsCondition(
        javaAccess -> forbiddenMethods.contains(javaAccess.getTarget().getFullName())
    ))
    .as("No class should create threads");

// Execute rule
rule.check(javaClasses);  // Throws AssertionError if violated
```

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
// Reports: "StudentCode transitively accesses Thread.start by [StudentCode.processInParallel→Helper.createWorkerThread]"
```

---

## 2.2 WALA Mode (Dynamic Call Graph Analysis)

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
6. **Filter False Positives**: Remove JDK helper paths (critical for thread analysis!)

**Strengths:**
- ✅ Very precise call path detection
- ✅ Understands complex call patterns (lambdas, method references, executors)
- ✅ **Advanced JDK helper filtering** (reduces false positives significantly)
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
    void processAsync() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> processData());  // Lambda creates thread - Forbidden!
    }
    
    void processData() {
        // ... processing logic
    }
}

// WALA detects: 
// StudentCode.processAsync → ExecutorService.submit → ThreadPoolExecutor.execute → Thread.start
// Reports: Method call at StudentCode.java:4 reaches forbidden ThreadPoolExecutor.execute
```

**Special WALA Features for Thread Analysis:**

**JDK Helper Filtering (Critical for Thread Detection):**

Thread creation is extensively used by JDK internals (file I/O, networking, class loading). WALA mode includes sophisticated filtering:

```java
private static final List<String> JDK_THREAD_HELPERS = List.of(
    /* File I/O helpers that internally use threads */
    "Lsun/nio/fs/", "sun/nio/fs/",
    "Lsun/nio/ch/", "sun/nio/ch/",
    "Ljava/nio/file/Files", "java/nio/file/Files",
    
    /* Class loading helpers that use threads */
    "Ljava/lang/ClassLoader", "java/lang/ClassLoader",
    "Ljava/lang/Class", "java/lang/Class",
    "Ljdk/internal/loader/NativeLibraries", "jdk/internal/loader/NativeLibraries",
    
    /* Network helpers that use threads */
    "Ljava/net/InetAddress", "java/net/InetAddress",
    
    /* Thread class itself (for internal operations) */
    "Ljava/lang/Thread", "java/lang/Thread",
    
    /* Reflection helpers */
    "Ljava/lang/reflect/Method", "java/lang/reflect/Method"
);

private static final List<String> ALLOWED_HELPER_APIS = List.of(
    "java.lang.Thread.<init>",              // Creating thread objects (not starting)
    "java.lang.Thread.interrupt",           // Interrupting threads
    "java.lang.Thread.getContextClassLoader",
    "java.lang.Thread.getStackTrace",
    "java.lang.ClassLoader.getSystemClassLoader",
    "java.lang.ClassLoader.loadLibrary",
    "java.lang.Runtime.load",
    "java.lang.Runtime.loadLibrary",
    "java.io.File.getName",
    "java.lang.Class.forName",
    "java.net.InetAddress.getAllByName",
    "java.lang.Class.getDeclaredField",
    "java.lang.reflect.Method.invoke",
    "java.lang.Class.checkMemberAccess",
    "java.io.File.<init>",
    "java.lang.Class.getClassLoader"
);

// Sophisticated filtering logic
boolean isLegitimateJDKPath(List<CGNode> path) {
    CGNode forbidden = path.get(path.size() - 1);
    
    // Check if the API is an allowed helper
    boolean isHelperApi = ALLOWED_HELPER_APIS.stream()
        .anyMatch(sig -> forbidden.getMethod().getSignature().startsWith(sig));
    
    // Check if path goes through JDK helper classes
    boolean hasHelperInPath = path.stream().anyMatch(node -> {
        String cls = node.getMethod().getDeclaringClass().getName().toString();
        return JDK_THREAD_HELPERS.stream().anyMatch(cls::startsWith);
    });
    
    // Ignore if it's a helper API called through helper classes
    return isHelperApi && hasHelperInPath;
}
```

**Why This Filtering Is Critical:**

Without this filtering, WALA would report false positives for innocent operations:
- ❌ `Files.list()` → internally uses `DirectoryStream` → creates threads
- ❌ `Class.forName()` → class loader uses threads internally
- ❌ `InetAddress.getByName()` → DNS lookup uses threads

With filtering:
- ✅ Ignores JDK internal thread usage
- ✅ Detects genuine student code creating threads

---

# 3. Monitored Thread System Methods

Both ArchUnit and WALA modes monitor the same set of thread creation methods, loaded from template files:

**Template Locations:**
- **ArchUnit**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/archunit/methods/thread-manipulation-methods.txt`
- **WALA**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/wala/methods/thread-manipulation-methods.txt`

---

## 3.1 THREAD SYSTEM - CREATE Operations (With Parameters)

**Security Component:** Thread creation monitor (methods that receive task/runnable as parameter)

**Monitored Methods (loaded from `thread-manipulation-methods.txt`):**

**java.lang.Thread:**
- `Thread.startVirtualThread(Runnable)`

**java.lang.Thread.Builder:**
- `Thread.Builder.start(Runnable)`
- `Thread.Builder.run(Runnable)`

**java.lang.Thread.Builder.OfPlatform:**
- `Thread.Builder.OfPlatform.start(Runnable)`

**java.lang.ThreadGroup:**
- `ThreadGroup.newThread(Runnable)`

**java.util.concurrent.Executor:**
- `Executor.execute(Runnable)`

**java.util.concurrent.ExecutorService:**
- `ExecutorService.submit(Runnable)`
- `ExecutorService.submit(Callable)`
- `ExecutorService.invokeAll(Collection)`
- `ExecutorService.invokeAny(Collection)`

**java.util.concurrent.AbstractExecutorService:**
- `AbstractExecutorService.submit(Runnable)`
- `AbstractExecutorService.submit(Callable)`
- `AbstractExecutorService.invokeAll(Collection)`
- `AbstractExecutorService.invokeAny(Collection)`

**java.util.concurrent.ThreadPoolExecutor:**
- `ThreadPoolExecutor.execute(Runnable)`
- `ThreadPoolExecutor.submit(Runnable)`
- `ThreadPoolExecutor.submit(Callable)`

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
- `ForkJoinPool.execute(ForkJoinTask)`
- `ForkJoinPool.submit(Runnable)`
- `ForkJoinPool.submit(Callable)`
- `ForkJoinPool.submit(ForkJoinTask)`

**java.util.concurrent.CompletableFuture:**
- `CompletableFuture.runAsync(Runnable)`
- `CompletableFuture.runAsync(Runnable, Executor)`
- `CompletableFuture.supplyAsync(Supplier)`
- `CompletableFuture.supplyAsync(Supplier, Executor)`
- `CompletableFuture.thenApplyAsync(Function)`
- `CompletableFuture.thenApplyAsync(Function, Executor)`
- `CompletableFuture.thenCombineAsync(CompletionStage, BiFunction)`
- `CompletableFuture.thenCombine(CompletionStage, BiFunction)`

**java.util.concurrent.ThreadFactory:**
- `ThreadFactory.newThread(Runnable)`

**java.util.concurrent.Executors$DelegatedExecutorService:**
- `Executors$DelegatedExecutorService.submit(Runnable)`
- `Executors$DelegatedExecutorService.submit(Callable)`
- `Executors$DelegatedExecutorService.invokeAll(Collection)`
- `Executors$DelegatedExecutorService.invokeAny(Collection)`

**java.util.concurrent.Executors$DefaultThreadFactory:**
- `Executors$DefaultThreadFactory.newThread(Runnable)`

**java.util.concurrent.ExecutorCompletionService:**
- `ExecutorCompletionService.submit(Runnable)`
- `ExecutorCompletionService.submit(Callable)`

---

## 3.2 THREAD SYSTEM - CREATE Operations (Without Parameters)

**Security Component:** Thread creation monitor (methods that start threads without task parameter)

**Monitored Methods:**

**java.lang.Thread:**
- `Thread.start()`

**java.util.Collection:**
- `Collection.parallelStream()`

**java.util.stream.Stream:**
- `Stream.parallel()`

**java.util.stream.BaseStream:**
- `BaseStream.parallel()`

---

**Method Signature Format:**
```
java.lang.Thread.start()V
java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;
java.util.concurrent.ThreadPoolExecutor.execute(Ljava/lang/Runnable;)V
java.util.concurrent.CompletableFuture.runAsync(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;
```

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

## 4.2 Defining Architecture Rules

**Step 2: Create Architecture Rule**

```java
// Load forbidden methods from template file
Set<String> forbiddenMethods = FileTools.readMethodsFile(
    FileTools.readFile(FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS)
);

// Create custom condition that checks method access
DescribedPredicate<JavaAccess<?>> checkForbiddenAccess = 
    new DescribedPredicate<>("accesses forbidden thread creation method") {
        @Override
        public boolean test(JavaAccess<?> javaAccess) {
            return forbiddenMethods.stream()
                .anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
        }
    };

// Create rule that no class should access forbidden methods
ArchRule rule = ArchRuleDefinition.noClasses()
    .should(new TransitivelyAccessesMethodsCondition(checkForbiddenAccess))
    .as("No class should create threads");
```

**Rule Components:**
- **DescribedPredicate**: Tests if a method access is forbidden
- **TransitivelyAccessesMethodsCondition**: Finds transitive access paths
- **ArchRule**: Complete rule that can be checked against JavaClasses

---

## 4.3 Executing Rules

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
Architecture Violation [Priority: MEDIUM] - Rule 'No class should create threads' was violated (1 times):
Method <de.student.StudentCode.processAsync()> transitively accesses method <java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;> by 
  [de.student.StudentCode.processAsync() -> java.util.concurrent.Executors.newFixedThreadPool() -> java.util.concurrent.ExecutorService.submit()]
```

**Error is converted to:**
```java
throw new SecurityException(
    "Ares Security Error (Reason: Student-Code; Stage: Execution): " +
    "Illegal thread creation: StudentCode.processAsync transitively calls ExecutorService.submit"
);
```

---

## 4.4 Transitive Access Detection

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
StudentCode.processData() calls:
  ├─ Helper.processInParallel() ✓ (not forbidden, continue searching)
  │   └─ ExecutorService.submit() ✗ (FORBIDDEN! Report path)
  └─ System.out.println() ✓ (not forbidden, ignore)

Result: Path found = [StudentCode.processData → Helper.processInParallel → ExecutorService.submit]
```

---

# 5. WALA Analysis Flow

## 5.1 Building the Call Graph

**Step 1: Create Analysis Scope**

```java
// Read exclusions file (to ignore JDK internals)
File exclusionsFile = FileTools.readFile(
    FileTools.resolveFileOnSourceDirectory("templates", "architecture", "java", "exclusions.txt")
);

// Create analysis scope
AnalysisScope scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
    classPath + File.pathSeparator + System.getProperty("java.class.path"),
    exclusionsFile
);

// Build class hierarchy
ClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);
```

**Exclusions File Example:**
```
# Exclude JDK internals to improve performance
sun/.*
com/sun/.*
jdk/internal/.*
```

**Step 2: Define Entry Points**

```java
// Find all methods in student code as potential entry points
List<DefaultEntrypoint> entryPoints = new ArrayList<>();

for (IClass iClass : classHierarchy) {
    if (iClass.getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
        for (IMethod method : iClass.getDeclaredMethods()) {
            if (!method.getName().toString().equals("main")) {
                entryPoints.add(new DefaultEntrypoint(method.getReference(), classHierarchy));
            }
        }
    }
}
```

**Step 3: Build Call Graph**

```java
// Configure analysis options
AnalysisOptions options = new AnalysisOptions();
options.setEntrypoints(entryPoints);

// Create call graph builder (0-CFA algorithm)
CallGraphBuilder<?> builder = Util.makeZeroCFABuilder(
    Language.JAVA,
    options,
    new AnalysisCacheImpl(),
    classHierarchy
);

// Build call graph
CallGraph callGraph = builder.makeCallGraph(options, null);
```

**Call Graph Structure:**
```
CallGraph:
  ├─ CGNode: StudentCode.processAsync()
  │   ├─ successor: Executors.newFixedThreadPool()
  │   └─ successor: ExecutorService.submit()
  ├─ CGNode: ExecutorService.submit()
  │   └─ successor: ThreadPoolExecutor.execute()
  └─ CGNode: ThreadPoolExecutor.execute()
      └─ successor: Thread.start() (FORBIDDEN!)
```

---

## 5.2 Finding Entry Points

**Entry Point Selection:**

Entry points are methods where analysis should start. WALA uses all methods in student code (except `main`) as entry points to ensure comprehensive coverage.

**Why exclude `main`?**
- In test scenarios, tests invoke student methods directly
- `main` is typically not the actual entry point for student code
- Including `main` may introduce unnecessary analysis paths

**Entry Point Representation:**
```java
DefaultEntrypoint(
    methodReference = "Lde/student/StudentCode.processAsync()V",
    classHierarchy = <ClassHierarchy>
)
```

---

## 5.3 Reachability Analysis

**Step 4: Find Paths to Forbidden Methods**

```java
// Load forbidden methods
Set<String> forbiddenMethods = FileTools.readMethodsFile(
    FileTools.readFile(FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS)
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
Entry: StudentCode.processAsync()
  ↓ calls
Executors.newFixedThreadPool(10)
  ↓ returns
ExecutorService (ThreadPoolExecutor instance)
  ↓ calls
ExecutorService.submit(lambda)
  ↓ calls
ThreadPoolExecutor.execute()  ← FORBIDDEN METHOD REACHED!

Path: [StudentCode.processAsync, Executors.newFixedThreadPool, ExecutorService.submit, ThreadPoolExecutor.execute]
```

---

## 5.4 False Positive Filtering for JDK Helpers

**Challenge:** JDK classes extensively use threads internally for legitimate operations:
- File I/O: `Files.list()` creates threads for directory traversal
- Networking: `InetAddress.getByName()` uses threads for DNS lookups
- Class Loading: `ClassLoader` uses threads for parallel loading

**Without filtering:** Almost every student program would be flagged!

**Solution:** Sophisticated filtering of JDK helper paths.

```java
private static final List<String> JDK_THREAD_HELPERS = List.of(
    /* File system helpers */
    "Lsun/nio/fs/", "sun/nio/fs/",
    "Lsun/nio/ch/", "sun/nio/ch/",
    "Ljava/nio/file/Files", "java/nio/file/Files",
    
    /* Class loading helpers */
    "Ljava/lang/ClassLoader", "java/lang/ClassLoader",
    "Ljava/lang/Class", "java/lang/Class",
    "Ljdk/internal/loader/NativeLibraries", "jdk/internal/loader/NativeLibraries",
    
    /* Network helpers */
    "Ljava/net/InetAddress", "java/net/InetAddress",
    
    /* Thread helpers */
    "Ljava/lang/Thread", "java/lang/Thread",
    
    /* Reflection helpers */
    "Ljava/lang/reflect/Method", "java/lang/reflect/Method"
);

private static final List<String> ALLOWED_HELPER_APIS = List.of(
    "java.lang.Thread.<init>",              // Creating thread object (not starting)
    "java.lang.Thread.interrupt",           // Interrupting threads
    "java.lang.Thread.getContextClassLoader",
    "java.lang.Thread.getStackTrace",
    "java.lang.ClassLoader.getSystemClassLoader",
    "java.lang.ClassLoader.loadLibrary",
    "java.lang.Runtime.load",
    "java.lang.Runtime.loadLibrary",
    "java.io.File.getName",
    "java.io.File.<init>",
    "java.lang.Class.forName",
    "java.lang.Class.getDeclaredField",
    "java.lang.Class.getClassLoader",
    "java.lang.Class.checkMemberAccess",
    "java.net.InetAddress.getAllByName",
    "java.lang.reflect.Method.invoke"
);

// Sophisticated filtering wrapper
private static WalaRule wrapIgnoringJdkHelpers(WalaRule base) {
    return new WalaRule(base.ruleName, base.forbiddenMethods) {
        @Override
        public void check(CallGraph cg) {
            List<CGNode> path = ReachabilityChecker.findReachableMethods(
                cg,
                cg.getEntrypointNodes().iterator(),
                n -> base.forbiddenMethods.stream()
                    .anyMatch(sig -> n.getMethod().getSignature().startsWith(sig))
            );

            if (path == null || path.isEmpty()) {
                return;  // No forbidden method reached
            }

            CGNode forbidden = path.get(path.size() - 1);

            /* Is the API one helpers legitimately call? */
            boolean helperApi = ALLOWED_HELPER_APIS.stream()
                .anyMatch(sig -> forbidden.getMethod().getSignature().startsWith(sig));

            /* Does the call chain contain at least one helper frame? */
            boolean helperSeen = path.stream().anyMatch(n -> {
                String cls = n.getMethod().getDeclaringClass().getName().toString();
                return JDK_THREAD_HELPERS.stream().anyMatch(cls::startsWith);
            });

            if (helperApi && helperSeen) {
                return;  // Housekeeping path – ignore
            }

            /* Otherwise: genuine violation */
            base.check(cg);
        }
    };
}
```

**Filtering Logic:**

1. **Find path to forbidden method** (e.g., Thread.start())
2. **Check if it's a helper API** (e.g., Thread.<init> is allowed)
3. **Check if path contains JDK helpers** (e.g., Files, ClassLoader)
4. **If both true → IGNORE** (legitimate JDK internal use)
5. **Otherwise → REPORT** (student code creating threads)

**Example Filtering:**

**Path 1 (Ignored - Legitimate JDK Use):**
```
StudentCode.listFiles()
  ↓
Files.list(Path)  (JDK helper)
  ↓
UnixDirectoryStream.<init>()  (internal JDK class)
  ↓
ThreadPoolExecutor.execute()  (internal thread pool)
  ↓
Thread.<init>()  (allowed helper API)

Result: IGNORED (Files.list() legitimately uses threads internally)
Analysis: helperApi=true (Thread.<init> is allowed), helperSeen=true (Files is in JDK_THREAD_HELPERS)
```

**Path 2 (Ignored - Class Loading):**
```
StudentCode.loadClass()
  ↓
Class.forName("com.example.MyClass")  (JDK helper)
  ↓
ClassLoader.loadClass()  (JDK helper)
  ↓
Thread.start()  (internal class loading thread)

Result: IGNORED (Class loading legitimately uses threads)
Analysis: helperApi=false, but path goes through ClassLoader (JDK helper)
```

**Path 3 (Detected - Real Violation):**
```
StudentCode.processAsync()
  ↓
new Thread(() -> process()).start()  (direct call, no helpers)

Result: VIOLATION (direct thread creation by student)
Analysis: helperApi=false, helperSeen=false (no JDK helpers in path)
```

**Path 4 (Detected - Executor Service):**
```
StudentCode.parallelProcess()
  ↓
Executors.newFixedThreadPool(10)
  ↓
ExecutorService.submit(() -> task())
  ↓
ThreadPoolExecutor.execute()  (FORBIDDEN!)

Result: VIOLATION (student explicitly creates thread pool)
Analysis: helperApi=false, helperSeen=false (student code, not JDK internal)
```

**Why This Filtering Is Essential:**

Without filtering, these innocent operations would be flagged:
- ❌ `Files.list("/tmp")` → false positive
- ❌ `Class.forName("MyClass")` → false positive
- ❌ `InetAddress.getByName("localhost")` → false positive
- ❌ `System.out.println()` → may trigger class loading → false positive

With filtering:
- ✅ Ignores JDK internal thread usage
- ✅ Detects genuine student code creating threads
- ✅ Dramatically reduces false positives
- ✅ Makes architecture testing practical for thread analysis

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

**Generated Code Example:**
```java
@Test
void testNoThreadCreation() {
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
    
    JavaArchunitTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS
        .check(javaClasses);
}
```

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

**Generated Code Example:**
```java
@Test
void testNoThreadCreation() {
    String classPath = System.getProperty("java.class.path");
    CallGraph callGraph = new CustomCallgraphBuilder(classPath)
        .buildCallGraph(classPath);
    
    Set<PackagePermission> allowedPackages = Set.of(
        new PackagePermission("java.lang")
    );
    
    JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS
        .check(callGraph);
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
  │   JavaArchunitTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(javaClasses)
  │   ↓
  │   [AssertionError thrown if violation found]
  │
  └─ WALA → JavaWalaTestCase.executeArchitectureTestCase()
      ↓
      JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(callGraph)
      ↓ (with JDK helper filtering)
      [AssertionError thrown if violation found]
```

**Error Parsing:**

```java
try {
    testCase.executeArchitectureTestCase(architectureMode, aopMode);
} catch (AssertionError e) {
    JavaArchitectureTestCase.parseErrorMessage(e);
}

// parseErrorMessage converts:
// "Architecture Violation [Priority: MEDIUM] - Rule 'No class should create threads' was violated"
// → SecurityException: "Ares Security Error: Illegal thread creation detected: ..."
```

---

# 7. Conclusion

## Summary for Programming Instructors (TL;DR)

**What does Architecture Testing do?**
- ✅ Analyzes **compiled bytecode** to detect forbidden thread creation operations
- ✅ Works **before code execution** - catches violations during testing phase
- ✅ Provides **two analysis modes**: Fast (ArchUnit) and Precise (WALA)
- ✅ Detects **transitive calls** - finds violations even through helper methods
- ✅ **Advanced false positive filtering** - ignores JDK internal thread usage (WALA mode)
- ✅ Generates **clear error messages** with complete call chains

**When do you need this?**
- When you want to prevent students from creating threads entirely
- For pre-submission checks (CI/CD pipelines)
- When runtime monitoring (AOP) is not feasible or desired
- For comprehensive code structure validation
- When you want to avoid false positives from JDK internal thread usage

**How does it work (simplified)?**
1. Compile student code to `.class` files
2. Architecture analysis scans bytecode for thread creation method calls
3. WALA mode filters out JDK internal thread usage (Files.list, ClassLoader, etc.)
4. If forbidden patterns detected → Report violation with call chain
5. Unlike AOP, this happens during testing, not when code runs

---

## Comparison: Architecture vs. AOP

| Aspect | Architecture (ArchUnit/WALA) | AOP (Byte Buddy/AspectJ) |
|--------|------------------------------|--------------------------|
| **Analysis Time** | Before execution (static) | During execution (runtime) |
| **Detection** | Analyzes code structure | Intercepts method calls |
| **Granularity** | Binary (allowed/forbidden) | Quota-based permissions |
| **Performance Impact** | Analysis overhead only | Runtime overhead on every call |
| **False Positives** | Possible (WALA filters JDK) | None (only executed code checked) |
| **Coverage** | All code paths | Only executed paths |
| **Configuration** | Package-level permissions | Thread class + quota permissions |
| **Use Case** | Pre-submission validation | Runtime security enforcement |
| **Error Timing** | Test phase | Production execution |
| **JDK Internal Handling** | Filtered (WALA mode) | Not needed (runtime checks actual code) |

---

## Technical Details

### **ArchUnit Mode (Static Analysis)**

**Implementation:**
- Uses ArchUnit's `ArchRule` and custom `TransitivelyAccessesMethodsCondition`
- Analyzes class dependencies and method access patterns
- No call graph construction required
- No false positive filtering (may report JDK internal usage)

**Violation Example:**
```
Architecture Violation [Priority: MEDIUM] - Rule 'No class should create threads' was violated (1 times):
Method <de.student.StudentCode.processAsync()> transitively accesses method <java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;> by 
  [de.student.StudentCode.processAsync() -> java.util.concurrent.Executors.newFixedThreadPool() -> java.util.concurrent.ExecutorService.submit()]
```

**Best For:**
- Quick validation during development
- Broad detection of thread creation patterns
- When exact call paths are not critical
- When false positives are acceptable

---

### **WALA Mode (Call Graph Analysis with False Positive Filtering)**

**Implementation:**
- Builds complete call graph using IBM WALA framework
- Performs reachability analysis from entry points to forbidden methods
- **Critical feature: Sophisticated JDK helper filtering**
- Reduces false positives dramatically

**Violation Example:**
```
Forbidden method 'java.util.concurrent.ThreadPoolExecutor.execute(Ljava/lang/Runnable;)V' is reachable from 'de.student.StudentCode.processAsync()V' 
in class 'de.student.StudentCode' at line 15
Called by test method: 'org.example.TestClass.testAsync()V'
```

**Best For:**
- Precise violation detection
- Understanding exact call paths
- Production-grade security validation
- **Minimizing false positives from JDK internal thread usage**
- Analyzing complex concurrent code (lambdas, executors, CompletableFuture)

---

### **Forbidden Method Templates**

Both modes load forbidden methods from text files:

**File Format:**
```
# Direct thread creation
java.lang.Thread.start()V
java.lang.Thread.startVirtualThread(Ljava/lang/Runnable;)Ljava/lang/Thread;

# Executor services
java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;
java.util.concurrent.ThreadPoolExecutor.execute(Ljava/lang/Runnable;)V
java.util.concurrent.ScheduledExecutorService.schedule(Ljava/lang/Runnable;JLjava/util/concurrent/TimeUnit;)Ljava/util/concurrent/ScheduledFuture;

# CompletableFuture async operations
java.util.concurrent.CompletableFuture.runAsync(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;
java.util.concurrent.CompletableFuture.supplyAsync(Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;

# Parallel streams
java.util.Collection.parallelStream()Ljava/util/stream/Stream;
java.util.stream.Stream.parallel()Ljava/util/stream/Stream;

# ... more methods
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
        <version>2.0.1-Beta6</version>
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
    
    // Execute (WALA mode recommended for thread analysis due to false positive filtering)
    testCase.executeArchitectureTestCase("WALA", "");
}
```

---

**The architecture testing approach provides comprehensive security validation at compile/test time, complementing the runtime AOP approach for defense-in-depth security against thread creation attacks. WALA mode is particularly powerful for thread analysis due to its sophisticated false positive filtering of JDK-internal thread usage.**
