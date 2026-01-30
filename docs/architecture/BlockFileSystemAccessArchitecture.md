# File System Security Mechanism (Architecture Analysis)

## Table of Contents

1. [High-Level Overview](#1-high-level-overview)
   - [1.1 Architecture Analysis Approach](#11-architecture-analysis-approach)
   - [1.2 Configuration Settings](#12-configuration-settings)
   - [1.3 Summary: When Is File Access Blocked?](#13-summary-when-is-file-access-blocked)
   - [1.4 What Code Is Trusted vs. Restricted?](#14-what-code-is-trusted-vs-restricted)
2. [Ares Monitors FileSystem Methods](#2-ares-monitors-filesystem-methods)
   - [2.1 FILE SYSTEM - READ Operations](#21-file-system---read-operations)
   - [2.2 FILE SYSTEM - WRITE/OVERWRITE Operations](#22-file-system---writeoverwrite-operations)
   - [2.3 FILE SYSTEM - CREATE Operations](#23-file-system---create-operations)
   - [2.4 FILE SYSTEM - DELETE Operations](#24-file-system---delete-operations)
   - [2.5 FILE SYSTEM - EXECUTE Operations](#25-file-system---execute-operations)
3. [Student Code Triggers Security Check](#3-student-code-triggers-security-check)
4. [Ares Collects Information About the File Access](#4-ares-collects-information-about-the-file-access)
   - [4.1 Loading Java Classes (ArchUnit)](#41-loading-java-classes-archunit)
   - [4.2 Building the Call Graph (WALA)](#42-building-the-call-graph-wala)
5. [Ares Validates the File Access](#5-ares-validates-the-file-access)
   - [5.1 ArchUnit Mode: Static Analysis](#51-archunit-mode-static-analysis)
   - [5.2 WALA Mode: Call Graph Analysis](#52-wala-mode-call-graph-analysis)
   - [5.3 Transitive Access Detection](#53-transitive-access-detection)
   - [5.4 Reachability Analysis (WALA)](#54-reachability-analysis-wala)
   - [5.5 False Positive Filtering (WALA)](#55-false-positive-filtering-wala)
6. [Conclusion](#6-conclusion)

---

# 1. High-Level Overview

This document describes how Ares 2 prevents unauthorized file system access in student code using **static code analysis** techniques via Architecture Testing frameworks.

**Key Difference from AOP Approach:**
- **AOP (Runtime)**: Monitors method calls during program execution and blocks forbidden operations in real-time
- **Architecture (Static)**: Analyzes compiled bytecode before execution to detect potential security violations in the code structure

---

## 1.1 Architecture Analysis Approach

**What is Architecture Testing?**

Architecture testing validates that code follows specific structural rules by analyzing compiled bytecode. Instead of running the code and intercepting method calls (AOP), it examines the program structure to find violations.

**Think of it like:**
- **AOP = Security Guard**: Checks IDs when people actually enter the building
- **Architecture = Building Inspector**: Reviews building plans before construction to ensure doors don't open into forbidden areas

**Two Analysis Frameworks:**

### **ArchUnit (Static Analysis)**
- **Type**: Pure static analysis using Archunit framework
- **Strength**: Fast, no call graph needed
- **Method**: Analyzes class dependencies and method calls in compiled bytecode
- **Use Case**: Detecting direct and transitive method access patterns

### **WALA (Dynamic Call Graph Analysis)**
- **Type**: Static analysis with dynamic modeling using IBM WALA framework
- **Strength**: Precise call path detection, understands complex call chains
- **Method**: Builds a complete call graph representing all possible method invocations
- **Use Case**: Finding reachable forbidden methods through complex call chains

**Validation Flow:**

1. **Load Classes**: Import compiled `.class` files from classpath
2. **Build Analysis Model**: 
   - ArchUnit: Load class metadata and dependencies
   - WALA: Build complete call graph with entry points
3. **Define Rules**: Specify forbidden method patterns (file system operations)
4. **Execute Analysis**: Check if student code violates rules
5. **Report Violations**: Generate detailed error messages with call paths

---

## 1.2 Configuration Settings

Security policies are configured through settings that instructors can adjust:

| Setting | Type | Description | Example |
|---------|------|-------------|---------|
| **architectureMode** | `String` | Analysis framework | `"ARCHUNIT"` or `"WALA"` |
| **allowedPackages** | `PackagePermission[]` | Packages allowed to be imported/used | `[new PackagePermission("java.io")]` |
| **classPath** | `String` | Path to compiled student code | `"target/classes"` |
| **restrictedPackage** | `String` | Package containing student code | `"de.student."` |

**Architecture-Specific Configuration:**
- No path-based permissions (no `pathsAllowedToBeRead`, etc.) - Architecture testing detects ANY file system access attempt
- No AOP mode selection - Uses `architectureMode` instead
- Package-level permissions instead of path-level permissions

---

## 1.3 Summary: When Is File Access Blocked?

Access is **BLOCKED** 🔴 when **ALL** conditions are true:

1. **Architecture Mode Enabled**: `architectureMode == "ARCHUNIT"` or `architectureMode == "WALA"`
2. **Student Code Contains File System Calls**: Analysis detects method calls to file system APIs
3. **Calls Are Reachable**: The forbidden methods can be reached from student code (directly or transitively)
4. **Not in Allowed Packages**: The accessed classes are not in the `allowedPackages` list

**If ANY condition fails → No Violation Detected** 🟢

**Key Differences from AOP:**
- 🔴 Detected at analysis time (before execution), not at runtime
- 🔴 Blocks ALL file system access attempts, not just specific paths
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
- All classes that call file system methods from the forbidden lists

**Security Assumptions:** 
- Student code is compiled and available as `.class` files
- Class files have not been tampered with after compilation
- Call graph accurately represents possible execution paths (WALA mode)

---

# 2. Ares Monitors FileSystem Methods

Both ArchUnit and WALA modes monitor the same set of file system methods, loaded from template files:

**Template Locations:**
- **ArchUnit**: `src/main/resources/templates/architecture/java/archunit/methods/file-system-access-methods.txt`
- **WALA**: `src/main/resources/templates/architecture/java/wala/methods/file-system-access-methods.txt`

**What is Architecture Testing?**

Instead of intercepting method calls at runtime (AOP approach), architecture testing analyzes the compiled bytecode to detect which file system methods the student code accesses. This happens during the test phase, before the code actually runs.

**Two Analysis Approaches:**
- **ArchUnit**: Fast static analysis of class dependencies
- **WALA**: Precise call graph analysis with false positive filtering

---

## 2.1 FILE SYSTEM - READ Operations

**Monitored Methods (loaded from `file-system-access-methods.txt`):**

**java.io Package:**
- `FileInputStream` constructors and read methods
- `FileReader` constructors
- `RandomAccessFile` read methods
- `File.exists()`, `File.canRead()`, `File.length()`, etc.

**java.nio.file Package:**
- `Files.readAllBytes()`, `Files.readAllLines()`, `Files.readString()`
- `Files.newInputStream()`, `Files.newBufferedReader()`
- `Files.list()`, `Files.walk()`, `Files.find()`
- `Files.readAttributes()`, `Files.getAttribute()`

**java.nio.channels Package:**
- `FileChannel.open()`, `FileChannel.read()`
- `AsynchronousFileChannel.open()`, `AsynchronousFileChannel.read()`

**Other Packages:**
- `ImageIO.read()`
- `Scanner` constructors with file parameters
- `DocumentBuilder.parse()`
- `AudioSystem.getAudioInputStream()`

**Method Signature Format:**
```
java.io.FileInputStream.<init>(Ljava/lang/String;)V
java.nio.file.Files.readString(Ljava/nio/file/Path;)Ljava/lang/String;
```

---

## 2.2 FILE SYSTEM - WRITE/OVERWRITE Operations

**Monitored Methods:**
- `FileOutputStream` write methods
- `FileWriter` write methods
- `RandomAccessFile.write()`
- `Files.write()`, `Files.writeString()`
- `Files.newOutputStream()`, `Files.newBufferedWriter()`
- `Files.setAttribute()`, `File.setReadable()`, etc.
- `ImageIO.write()`
- `Transformer.transform()` (XML output to file)

---

## 2.3 FILE SYSTEM - CREATE Operations

**Monitored Methods:**
- `File.createNewFile()`, `File.mkdir()`, `File.mkdirs()`
- `File.createTempFile()`
- `Files.createFile()`, `Files.createDirectory()`, `Files.createDirectories()`
- `Files.createTempFile()`, `Files.createTempDirectory()`
- `Files.createLink()`, `Files.createSymbolicLink()`
- `FileChannel.open()` with CREATE option
- `FileSystemProvider.newFileSystem()`

---

## 2.4 FILE SYSTEM - DELETE Operations

**Monitored Methods:**
- `File.delete()`, `File.deleteOnExit()`
- `Files.delete()`, `Files.deleteIfExists()`
- `Desktop.moveToTrash()`
- `FileSystemProvider.delete()`

---

## 2.5 FILE SYSTEM - EXECUTE Operations

**Monitored Methods:**
- `Files.move()`, `Files.copy()`
- `File.renameTo()`
- `Desktop.open()`, `Desktop.edit()`, `Desktop.print()`
- `FileSystemProvider.move()`, `FileSystemProvider.copy()`

---

# 3. Student Code Triggers Security Check

When student code (any code within the configured restricted package) attempts to use one of these file system methods, the architecture analysis will detect it during the test phase.

**Example:**
```java
// Student Code
package de.student.solution;

import java.nio.file.Files;
import java.nio.file.Path;

public class StudentSolution {
    public String readSecretFile() {
        // This will be detected by architecture analysis
        return Files.readString(Path.of("/etc/passwd"));
    }
}
```

**What happens during analysis:**

**ArchUnit Mode:**
1. `ClassFileImporter` loads `StudentSolution.class`
2. Analyzes that `readSecretFile()` method calls `Files.readString()`
3. Checks if `Files.readString()` is in the forbidden methods list
4. Reports violation: "StudentSolution.readSecretFile transitively accesses Files.readString"

**WALA Mode:**
1. Builds call graph from student code entry points
2. Performs reachability analysis: Can `Files.readString()` be reached from student code?
3. Finds path: `StudentSolution.readSecretFile → Files.readString`
4. Reports violation with source line number

**Key Difference from AOP:**
- This happens during **test time**, not when the code actually runs
- Detects **all potential file accesses** in the code structure, even unreachable code
- No runtime overhead, but cannot check specific file paths

---

# 4. Ares Collects Information About the File Access

During architecture analysis, Ares collects information about the code structure to detect file system access patterns.

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

---

## 4.2 Building the Call Graph (WALA)

**Framework:** IBM WALA (T.J. Watson Libraries for Analysis)

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

**Entry Point Selection:**

Entry points are methods where analysis should start. WALA uses all methods in student code (except `main`) as entry points to ensure comprehensive coverage.

**Why exclude `main`?**
- In test scenarios, tests invoke student methods directly
- `main` is typically not the actual entry point for student code
- Including `main` may introduce unnecessary analysis paths

**Entry Point Representation:**
```java
DefaultEntrypoint(
    methodReference = "Lde/student/StudentCode.readFile()V",
    classHierarchy = <ClassHierarchy>
)
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
  ├─ CGNode: StudentCode.main()
  │   ├─ successor: StudentCode.readFile()
  │   └─ successor: System.out.println()
  ├─ CGNode: StudentCode.readFile()
  │   ├─ successor: Files.readString()
  │   └─ successor: Path.of()
  └─ CGNode: Files.readString()
      └─ (implementation details...)
```

**Analysis Process:**
1. **Create Analysis Scope**: Define which classes to include/exclude
2. **Build Class Hierarchy**: Resolve all type relationships
3. **Construct Call Graph**: Map all possible method call relationships
4. **Find Entry Points**: Identify starting methods in student code
5. **Reachability Analysis**: Use DFS to find paths to forbidden methods
6. **Filter False Positives**: Remove JDK helper paths (e.g., internal thread creation)

**Strengths:**
- ✅ Very precise call path detection
- ✅ Understands complex call patterns (lambdas, method references, reflection)
- ✅ Can filter out JDK internal calls (false positive reduction)
- ✅ Provides exact call paths with line numbers
- ✅ Models runtime behavior more accurately

**Limitations:**
- ⚠️ Slower analysis (call graph construction is expensive)
- ⚠️ Requires more memory
- ⚠️ More complex configuration (exclusions, entry points)
- ⚠️ May still have false positives for very dynamic code

---

# 5. Ares Validates the File Access

---

## 5.1 ArchUnit Mode: Static Analysis

**How it works:**
```java
// Define rule
ArchRule rule = noClasses()
    .should(new TransitivelyAccessesMethodsCondition(
        javaAccess -> forbiddenMethods.contains(javaAccess.getTarget().getFullName())
    ))
    .as("No class should access file system");

// Execute rule
rule.check(javaClasses);  // Throws AssertionError if violated
```

**Step 2: Create Architecture Rule**

```java
// Load forbidden methods from template file
Set<String> forbiddenMethods = FileTools.readMethodsFile(
    FileTools.readFile(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS)
);

// Create custom condition that checks method access
DescribedPredicate<JavaAccess<?>> checkForbiddenAccess = 
    new DescribedPredicate<>("accesses forbidden file system method") {
        @Override
        public boolean test(JavaAccess<?> javaAccess) {
            return forbiddenMethods.stream()
                .anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
        }
    };

// Create rule that no class should access forbidden methods
ArchRule rule = ArchRuleDefinition.noClasses()
    .should(new TransitivelyAccessesMethodsCondition(checkForbiddenAccess))
    .as("No class should access file system");
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
Architecture Violation [Priority: MEDIUM] - Rule 'No class should access file system' was violated (1 times):
Method <de.student.StudentCode.readFile()> transitively accesses method <java.nio.file.Files.readString(Ljava/nio/file/Path;)Ljava/lang/String;> by 
  [de.student.StudentCode.readFile() -> de.student.Helper.loadFile() -> java.nio.file.Files.readString()]
```

**Error is converted to:**
```java
throw new SecurityException(
    "Ares Security Error (Reason: Student-Code; Stage: Execution): " +
    "Illegal file system access: StudentCode.readFile transitively calls Files.readString"
);
```

**Example Violation Detection:**
```java
// Student Code
class StudentCode {
    void readFile() {
        Helper.processFile();  // Indirect call
    }
}

class Helper {
    void processFile() {
        Files.readString(Path.of("file.txt"));  // Forbidden!
    }
}

// ArchUnit detects: StudentCode → Helper.processFile → Files.readString
// Reports: "StudentCode transitively accesses Files.readString by [StudentCode.readFile→Helper.processFile]"
```

**Violation Example:**
```
Architecture Violation [Priority: MEDIUM] - Rule 'No class should access file system' was violated (1 times):
Method <de.student.StudentCode.exploit()> transitively accesses method <java.io.FileInputStream.<init>(Ljava/lang/String;)V> by 
  [de.student.StudentCode.exploit() -> de.student.Helper.readFile() -> java.io.FileInputStream.<init>()]
```

**Best For:**
- Quick validation during development
- Broad detection of file system usage patterns
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
    void processData() {
        executor.submit(() -> readConfig());  // Lambda expression
    }
    
    void readConfig() {
        Properties props = new Properties();
        props.load(new FileInputStream("config.properties"));  // Forbidden!
    }
}

// WALA detects: 
// StudentCode.processData → Lambda$1.run → StudentCode.readConfig → FileInputStream.<init>
// Reports: Method call at StudentCode.java:12 reaches forbidden FileInputStream.<init>
```

**Violation Example:**
```
Forbidden method 'java.io.FileInputStream.<init>(Ljava/lang/String;)V' is reachable from 'de.student.StudentCode.exploit()V' 
in class 'de.student.StudentCode' at line 42
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
StudentCode.main() calls:
  ├─ Helper.processData() ✓ (not forbidden, continue searching)
  │   └─ Files.readString() ✗ (FORBIDDEN! Report path)
  └─ System.out.println() ✓ (not forbidden, ignore)

Result: Path found = [StudentCode.main → Helper.processData → Files.readString]
```

---

## 5.4 Reachability Analysis (WALA)

**Step 4: Find Paths to Forbidden Methods**

```java
// Load forbidden methods
Set<String> forbiddenMethods = FileTools.readMethodsFile(
    FileTools.readFile(FileHandlerConstants.WALA_FILESYSTEM_METHODS)
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
Entry: StudentCode.processData()
  ↓ calls
Helper.loadConfig()
  ↓ calls
FileInputStream.<init>(String)  ← FORBIDDEN METHOD REACHED!

Path: [StudentCode.processData, Helper.loadConfig, FileInputStream.<init>]
```

---

## 5.5 False Positive Filtering (WALA)

**Challenge:** JDK classes legitimately use file system operations internally (e.g., `Files.list()` creates threads internally for directory traversal).

**Solution:** Filter out paths that only go through JDK helper classes.

```java
private static final List<String> JDK_THREAD_HELPERS = List.of(
    "Lsun/nio/fs/", "sun/nio/fs/",
    "Lsun/nio/ch/", "sun/nio/ch/",
    "Ljava/nio/file/Files", "java/nio/file/Files",
    "Ljava/lang/ClassLoader", "java/lang/ClassLoader"
);

private static final List<String> ALLOWED_HELPER_APIS = List.of(
    "java.lang.ClassLoader.getSystemClassLoader",
    "java.io.File.getName",
    "java.io.File.<init>"
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
        return JDK_THREAD_HELPERS.stream().anyMatch(cls::startsWith);
    });
    
    // Ignore if it's a helper API called through helper classes
    return helperApi && helperSeen;
}
```

**Example Filtering:**

**Path 1 (Ignored):**
```
StudentCode.listFiles()
  ↓
Files.list()  (JDK helper)
  ↓
UnixDirectoryStream.<init>()  (internal JDK class)
  ↓
FileInputStream.<init>()  (allowed helper API)

Result: IGNORED (legitimate JDK internal use)
```

**Path 2 (Detected):**
```
StudentCode.readSecret()
  ↓
FileInputStream.<init>()  (forbidden, no helpers in path)

Result: VIOLATION (direct file access by student)
```

**Special WALA Features:**

**JDK Helper Filtering:**
WALA mode includes intelligent filtering of JDK-internal helper classes that legitimately use file/thread operations:
```java
private static final List<String> JDK_THREAD_HELPERS = List.of(
    "sun/nio/fs/", "sun/nio/ch/", "java/nio/file/Files",
    "java/lang/ClassLoader", "jdk/internal/loader/NativeLibraries"
);

// Ignores paths like: StudentCode → Files.list → UnixDirectoryStream (internal helper)
// Detects real violations: StudentCode → Files.readString → (no internal helper)
```

## Summary for Programming Instructors (TL;DR)

**What does Architecture Testing do?**
- ✅ Analyzes **compiled bytecode** to detect forbidden file system operations
- ✅ Works **before code execution** - catches violations during testing phase
- ✅ Provides **two analysis modes**: Fast (ArchUnit) and Precise (WALA)
- ✅ Detects **transitive calls** - finds violations even through helper methods
- ✅ Generates **clear error messages** with complete call chains

**When do you need this?**
- When you want to prevent students from using file system operations entirely
- For pre-submission checks (CI/CD pipelines)
- When runtime monitoring (AOP) is not feasible or desired
- For comprehensive code structure validation

**How does it work (simplified)?**
1. Compile student code to `.class` files
2. Architecture analysis scans bytecode for file system method calls
3. If forbidden patterns detected → Report violation with call chain
4. Unlike AOP, this happens during testing, not when code runs

---

## Comparison: Architecture vs. AOP

| Aspect | Architecture (ArchUnit/WALA) | AOP (Byte Buddy/AspectJ) |
|--------|------------------------------|--------------------------|
| **Analysis Time** | Before execution (static) | During execution (runtime) |
| **Detection** | Analyzes code structure | Intercepts method calls |
| **Granularity** | Binary (allowed/forbidden) | Path-based permissions |
| **Performance Impact** | Analysis overhead only | Runtime overhead on every call |
| **False Positives** | Possible (unreachable code) | None (only executed code checked) |
| **Coverage** | All code paths | Only executed paths |
| **Configuration** | Package-level permissions | Path-level permissions |
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
Architecture Violation [Priority: MEDIUM] - Rule 'No class should access file system' was violated (1 times):
Method <de.student.StudentCode.exploit()> transitively accesses method <java.io.FileInputStream.<init>(Ljava/lang/String;)V> by 
  [de.student.StudentCode.exploit() -> de.student.Helper.readFile() -> java.io.FileInputStream.<init>()]
```

**Best For:**
- Quick validation during development
- Broad detection of file system usage patterns
- When exact call paths are not critical

---

### **WALA Mode (Call Graph Analysis)**

**Implementation:**
- Builds complete call graph using IBM WALA framework
- Performs reachability analysis from entry points to forbidden methods
- Includes false positive filtering for JDK internals

**Violation Example:**
```
Forbidden method 'java.io.FileInputStream.<init>(Ljava/lang/String;)V' is reachable from 'de.student.StudentCode.exploit()V' 
in class 'de.student.StudentCode' at line 42
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
# READ operations
java.io.FileInputStream.<init>(Ljava/lang/String;)V
java.io.FileReader.<init>(Ljava/lang/String;)V
java.nio.file.Files.readString(Ljava/nio/file/Path;)Ljava/lang/String;

# WRITE operations  
java.io.FileOutputStream.<init>(Ljava/lang/String;)V
java.nio.file.Files.writeString(Ljava/nio/file/Path;Ljava/lang/CharSequence;)Ljava/nio/file/Path;

# ... more methods
```

**Template Locations:**
- ArchUnit: `src/main/resources/templates/architecture/java/archunit/methods/file-system-access-methods.txt`
- WALA: `src/main/resources/templates/architecture/java/wala/methods/file-system-access-methods.txt`

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
void testNoFileSystemAccess() {
    // Load student classes
    JavaClasses javaClasses = new ClassFileImporter()
        .importPackages("de.student");
    
    // Create test case
    JavaArchitectureTestCase testCase = JavaArchitectureTestCase.builder()
        .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
        .allowedPackages(Set.of())
        .javaClasses(javaClasses)
        .callGraph(null)  // null for ArchUnit mode
        .build();
    
    // Execute
    testCase.executeArchitectureTestCase("ARCHUNIT", "");
}
```

---

**The architecture testing approach provides comprehensive security validation at compile/test time, complementing the runtime AOP approach for defense-in-depth security.**
