# Security Policy Manual for Ares 2

A practical guide for instructors to protect programming exercises from malicious student code.

## Table of Contents

1. [Introduction](#1-introduction)
2. [Quick Start](#2-quick-start)
3. [Understanding Security Policies](#3-understanding-security-policies)
4. [The Security Policy File](#4-the-security-policy-file)
5. [Permission Types Explained](#5-permission-types-explained)
6. [Common Scenarios and Templates](#6-common-scenarios-and-templates)
7. [Applying Policies to Tests](#7-applying-policies-to-tests)
8. [Troubleshooting](#8-troubleshooting)
9. [Best Practices](#9-best-practices)

---

## 1. Introduction

### What is Ares 2?

Ares 2 is a security framework for automated assessment of programming exercises. When students submit code for grading, there is a risk that the code could:

- Read or modify files it should not access
- Connect to external servers to cheat or cause harm
- Execute system commands
- Create excessive threads to crash the system
- Run forever (infinite loops)

Ares 2 prevents these problems by enforcing a **security policy** that defines exactly what the student code is allowed to do.

### What is a Security Policy?

A security policy is a simple configuration file that acts as a whitelist. It specifies:

- Which files can be read, written, or deleted
- Which network connections are allowed
- Which system commands can be executed
- How many threads can be created
- How long the code can run

**Key principle:** Everything is forbidden by default. You only grant the permissions that are absolutely necessary for the exercise.

---

## 2. Quick Start

### Step 1: Create a Policy File

Create a file named `SecurityPolicy.yaml` in your project root:

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: "com.student"
  theMainClassInsideThisPackageIs: "Main"
  theFollowingClassesAreTestClasses:
    - "com.instructor.ExerciseTest"
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

### Step 2: Apply the Policy to Your Test

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

### Step 3: Run the Tests

When you run the tests, Ares 2 will automatically enforce the security policy. If student code tries to do something not explicitly permitted, the test fails with a clear error message.

---

## 3. Understanding Security Policies

### The Default-Deny Approach

Ares 2 follows a **default-deny** security model:

| Without Policy | With Empty Policy |
|----------------|-------------------|
| Student code can do anything | Student code can do nothing |

This means you start with maximum security and selectively allow only what the exercise requires.

### What Can Be Controlled?

| Resource Type | What It Controls | Example |
|---------------|------------------|---------|
| File System | Reading, writing, creating, deleting files | Allow reading `input.txt` |
| Network | Opening connections, sending/receiving data | Allow connecting to `api.example.com:443` |
| Commands | Executing system commands | Allow running `python --version` |
| Threads | Creating new threads | Allow up to 5 threads |
| Packages | Importing Java packages | Allow `java.util.*` |
| Timeouts | Maximum execution time | Limit to 10 seconds |

---

## 4. The Security Policy File

### File Format

Security policies are written in YAML format. YAML uses indentation (spaces, not tabs) to show structure.

### Complete Structure

```yaml
regardingTheSupervisedCode:
  # Required: Which configuration to use
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION

  # Required: The package containing student code
  theSupervisedCodeUsesTheFollowingPackage: "com.student"

  # Optional: The main class (if applicable)
  theMainClassInsideThisPackageIs: "Main"

  # Required: Your test classes (these are trusted)
  theFollowingClassesAreTestClasses:
    - "com.instructor.ExerciseTest"
    - "com.instructor.AdvancedTest"

  # Required: The permissions section
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: []
    regardingNetworkConnections: []
    regardingCommandExecutions: []
    regardingThreadCreations: []
    regardingPackageImports: []
    regardingTimeouts: []
```

### Configuration Options

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

**Recommendation for beginners:** Use `JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION` for Gradle projects or `JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION` for Maven projects.

---

## 5. Permission Types Explained

### 5.1 File System Permissions

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
| `onThisPathAndAllPathsBelow` | text | The path where permissions apply (can be file or folder) |
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

### 5.2 Network Permissions

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
| `onTheHost` | text | Hostname or IP address |
| `onThePort` | number (0-65535) | Port number |
| `openConnections` | true/false | Allow opening connections |
| `sendData` | true/false | Allow sending data |
| `receiveData` | true/false | Allow receiving data |

**Example: Allow HTTP requests to a specific API**
```yaml
regardingNetworkConnections:
  - onTheHost: "api.openweathermap.org"
    onThePort: 443
    openConnections: true
    sendData: true
    receiveData: true
```

### 5.3 Command Permissions

Controls execution of system commands.

```yaml
regardingCommandExecutions:
  - executeTheCommand: "echo"
    withTheseArguments:
      - "hello"
```

| Property | Type | Description |
|----------|------|-------------|
| `executeTheCommand` | text | The command name |
| `withTheseArguments` | list | Allowed arguments (must match exactly) |

**Warning:** Command execution is inherently risky. Only allow specific commands with specific arguments.

**Example: Allow running a specific Python script**
```yaml
regardingCommandExecutions:
  - executeTheCommand: "python"
    withTheseArguments:
      - "helper.py"
      - "--safe-mode"
```

### 5.4 Thread Permissions

Controls thread creation to prevent denial-of-service attacks.

```yaml
regardingThreadCreations:
  - createTheFollowingNumberOfThreads: 5
    ofThisClass: "java.lang.Thread"
```

| Property | Type | Description |
|----------|------|-------------|
| `createTheFollowingNumberOfThreads` | number | Maximum number of threads |
| `ofThisClass` | text | Fully qualified class name |

**Common thread classes to allow:**
```yaml
regardingThreadCreations:
  # Basic threads
  - createTheFollowingNumberOfThreads: 10
    ofThisClass: "java.lang.Thread"

  # Thread pools
  - createTheFollowingNumberOfThreads: 10
    ofThisClass: "java.util.concurrent.ThreadPoolExecutor$Worker"

  # CompletableFuture
  - createTheFollowingNumberOfThreads: 10
    ofThisClass: "java.util.concurrent.CompletableFuture$AsyncRun"
```

### 5.5 Package Permissions

Controls which Java packages can be imported (optional, for advanced use).

```yaml
regardingPackageImports:
  - importTheFollowingPackage: "java.util"
  - importTheFollowingPackage: "java.io"
```

### 5.6 Timeout Permissions

Sets maximum execution time in milliseconds.

```yaml
regardingTimeouts:
  - timeout: 10000  # 10 seconds
```

**Recommendation:** Always set a timeout to prevent infinite loops. Common values:
- Simple exercises: 5000 (5 seconds)
- Complex algorithms: 30000 (30 seconds)
- File processing: 60000 (60 seconds)

---

## 6. Common Scenarios and Templates

### Scenario 1: Pure Computation (No I/O)

For exercises that only involve computation without any file or network access.

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: "com.student"
  theMainClassInsideThisPackageIs: "Main"
  theFollowingClassesAreTestClasses:
    - "com.instructor.AlgorithmTest"
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: []
    regardingNetworkConnections: []
    regardingCommandExecutions: []
    regardingThreadCreations: []
    regardingPackageImports: []
    regardingTimeouts:
      - timeout: 5000
```

### Scenario 2: File Reading Exercise

For exercises where students need to read from a specific input file.

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: "com.student"
  theMainClassInsideThisPackageIs: "FileProcessor"
  theFollowingClassesAreTestClasses:
    - "com.instructor.FileReadingTest"
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions:
      - onThisPathAndAllPathsBelow: "src/main/resources/input.txt"
        readAllFiles: true
        overwriteAllFiles: false
        createAllFiles: false
        executeAllFiles: false
        deleteAllFiles: false
    regardingNetworkConnections: []
    regardingCommandExecutions: []
    regardingThreadCreations: []
    regardingPackageImports: []
    regardingTimeouts:
      - timeout: 10000
```

### Scenario 3: File Writing Exercise

For exercises where students need to create output files.

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: "com.student"
  theMainClassInsideThisPackageIs: "ReportGenerator"
  theFollowingClassesAreTestClasses:
    - "com.instructor.FileWritingTest"
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions:
      - onThisPathAndAllPathsBelow: "output"
        readAllFiles: true
        overwriteAllFiles: true
        createAllFiles: true
        executeAllFiles: false
        deleteAllFiles: false
    regardingNetworkConnections: []
    regardingCommandExecutions: []
    regardingThreadCreations: []
    regardingPackageImports: []
    regardingTimeouts:
      - timeout: 15000
```

### Scenario 4: Multi-threaded Exercise

For exercises involving concurrency.

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: "com.student"
  theMainClassInsideThisPackageIs: "ConcurrentProcessor"
  theFollowingClassesAreTestClasses:
    - "com.instructor.ConcurrencyTest"
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: []
    regardingNetworkConnections: []
    regardingCommandExecutions: []
    regardingThreadCreations:
      - createTheFollowingNumberOfThreads: 10
        ofThisClass: "java.lang.Thread"
      - createTheFollowingNumberOfThreads: 10
        ofThisClass: "java.util.concurrent.ThreadPoolExecutor$Worker"
      - createTheFollowingNumberOfThreads: 10
        ofThisClass: "java.util.concurrent.ForkJoinWorkerThread"
    regardingPackageImports: []
    regardingTimeouts:
      - timeout: 30000
```

### Scenario 5: API Integration Exercise

For exercises that require calling an external API.

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: "com.student"
  theMainClassInsideThisPackageIs: "ApiClient"
  theFollowingClassesAreTestClasses:
    - "com.instructor.ApiIntegrationTest"
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: []
    regardingNetworkConnections:
      - onTheHost: "jsonplaceholder.typicode.com"
        onThePort: 443
        openConnections: true
        sendData: true
        receiveData: true
    regardingCommandExecutions: []
    regardingThreadCreations:
      - createTheFollowingNumberOfThreads: 5
        ofThisClass: "java.lang.Thread"
    regardingPackageImports: []
    regardingTimeouts:
      - timeout: 30000
```

---

## 7. Applying Policies to Tests

### The @Policy Annotation

Use the `@Policy` annotation on your test methods to apply security restrictions:

```java
@Policy(value = "SecurityPolicy.yaml", withinPath = "classes/java/main/com/student")
```

| Parameter | Description |
|-----------|-------------|
| `value` | Path to the security policy file (relative to project root) |
| `withinPath` | Path to the compiled student code |

### Path Configuration

The `withinPath` depends on your build tool:

| Build Tool | Typical withinPath |
|------------|-------------------|
| Gradle | `classes/java/main/com/student` |
| Maven | `classes/com/student` |

### Complete Test Example

```java
package com.instructor;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.Hidden;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExerciseTest {

    // Public test - students can see this test
    @Public
    @Policy(value = "SecurityPolicy.yaml", withinPath = "classes/java/main/com/student")
    @Test
    void testBasicFunctionality() {
        StudentSolution solution = new StudentSolution();
        assertEquals(42, solution.calculate(6, 7));
    }

    // Hidden test - students cannot see this test
    @Hidden
    @Policy(value = "SecurityPolicy.yaml", withinPath = "classes/java/main/com/student")
    @Test
    void testEdgeCases() {
        StudentSolution solution = new StudentSolution();
        assertEquals(0, solution.calculate(0, 0));
        assertThrows(IllegalArgumentException.class, () -> solution.calculate(-1, 5));
    }
}
```

---

## 8. Troubleshooting

### Common Error Messages

#### "Security violation: Unauthorized file access"

**Cause:** Student code tried to read or write a file not permitted by the policy.

**Solution:** Check if the file path needs to be added to `regardingFileSystemInteractions`.

#### "Security violation: Unauthorized network connection"

**Cause:** Student code tried to connect to a host/port not permitted.

**Solution:** Add the required host and port to `regardingNetworkConnections`.

#### "Security violation: Thread creation limit exceeded"

**Cause:** Student code created more threads than allowed.

**Solution:** Increase the `createTheFollowingNumberOfThreads` value or add additional thread class permissions.

#### "Test timed out"

**Cause:** Student code ran longer than the specified timeout.

**Solution:** Increase the `timeout` value in `regardingTimeouts`, or the student code has an infinite loop.

### YAML Syntax Errors

#### Indentation Problems

YAML requires consistent indentation with spaces (not tabs).

**Wrong:**
```yaml
regardingTheSupervisedCode:
	theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
```

**Correct:**
```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
```

#### Missing Colons

Every key must be followed by a colon and space.

**Wrong:**
```yaml
onThisPathAndAllPathsBelow "data/input.txt"
```

**Correct:**
```yaml
onThisPathAndAllPathsBelow: "data/input.txt"
```

#### Empty Lists

Use `[]` for empty lists, not just leaving them blank.

**Wrong:**
```yaml
regardingFileSystemInteractions:
regardingNetworkConnections:
```

**Correct:**
```yaml
regardingFileSystemInteractions: []
regardingNetworkConnections: []
```

---

## 9. Best Practices

### Security Guidelines

1. **Start restrictive:** Begin with all permissions empty and only add what is necessary.

2. **Be specific with paths:** Instead of allowing access to a whole directory, allow access to specific files when possible.

3. **Always set timeouts:** Prevent infinite loops by always including a reasonable timeout.

4. **Limit thread counts:** Even when threads are needed, set reasonable limits (10-50 is usually sufficient).

5. **Avoid command execution:** Only allow command execution if absolutely necessary for the exercise.

6. **Review regularly:** Update policies when exercise requirements change.

### Documentation Guidelines

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

### Testing Your Policies

Before releasing an exercise:

1. **Test with a correct solution:** Ensure your reference solution works with the policy.

2. **Test common mistakes:** Try variations of incorrect solutions to ensure they are properly restricted.

3. **Test malicious scenarios:** Attempt to read unauthorized files, make network connections, etc. to verify the policy blocks them.

---

## Summary

Creating a security policy for Ares 2 involves:

1. Creating a YAML file with the correct structure
2. Specifying your project configuration
3. Listing your test classes
4. Adding only the permissions necessary for the exercise
5. Applying the policy to test methods with the `@Policy` annotation

Remember: **Everything is forbidden by default.** Only grant the minimum permissions required for the exercise to work correctly.

For additional help, consult the [HowToMakeAProjectAnAresProject.md](HowToMakeAProjectAnAresProject.md) guide or the example policies in the test resources.
