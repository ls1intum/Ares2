# File System Security Mechanism

## Table of Contents

1. [High-Level Overview](#1-high-level-overview)
   - [1.1 Complete Validation Flow Diagram](#11-complete-validation-flow-diagram)
   - [1.2 Configuration Settings](#12-configuration-settings)
   - [1.3 Summary: When Is File Access Blocked?](#13-summary-when-is-file-access-blocked)
   - [1.4 What Code Is Trusted vs. Restricted?](#14-what-code-is-trusted-vs-restricted)
2. [Ares Monitors File System Methods](#2-ares-monitors-file-system-methods)
   - [2.1 FILE SYSTEM - READ Operations](#21-file-system---read-operations)
   - [2.2 FILE SYSTEM - WRITE/OVERWRITE Operations](#22-file-system---writeoverwrite-operations)
   - [2.3 FILE SYSTEM - CREATE Operations](#23-file-system---create-operations)
   - [2.4 FILE SYSTEM - DELETE Operations](#24-file-system---delete-operations)
   - [2.5 FILE SYSTEM - EXECUTE Operations](#25-file-system---execute-operations)
3. [Student Code Triggers Security Check](#3-student-code-triggers-security-check)
4. [Ares Collects Information About the File Access](#4-ares-collects-information-about-the-file-access)
   - [4.1 Which Method Was Called?](#41-which-method-was-called)
   - [4.2 What's the Current State of the Object?](#42-whats-the-current-state-of-the-object)
   - [4.3 What Parameters Were Passed?](#43-what-parameters-were-passed)
   - [4.4 Information Passed to Security Validator](#44-information-passed-to-security-validator)
5. [Ares Validates the File Access](#5-ares-validates-the-file-access)
   - [5.1 Check 1: Is Security Enabled?](#51-check-1-is-security-enabled)
   - [5.2 Check 2: Does the Call Come from Student Code?](#52-check-2-does-the-call-come-from-student-code)
     - [5.2.1 Load Configuration](#521-load-configuration)
     - [5.2.2 Analyze the Call Chain](#522-analyze-the-call-chain)
     - [5.2.3 Find Which Test Called the Student Code](#523-find-which-test-called-the-student-code)
   - [5.3 Check 3: Determine Which Security Permissions to Check](#53-check-3-determine-which-security-permissions-to-check)
   - [5.4 Check 4: Extract and Validate File Paths](#54-check-4-extract-and-validate-file-paths)
     - [5.4.1 Load List of Allowed Paths](#541-load-list-of-allowed-paths)
     - [5.4.2 Apply Special Rules for Specific Methods](#542-apply-special-rules-for-specific-methods)
     - [5.4.3 Check Method Parameters for File Paths](#543-check-method-parameters-for-file-paths)
     - [5.4.4 Check Object State for File Paths](#544-check-object-state-for-file-paths)
     - [5.4.5 Allow Ares Internal Files](#545-allow-ares-internal-files)
   - [5.5 Check 5: Block Access with Detailed Error Message](#55-check-5-block-access-with-detailed-error-message)
6. [Conclusion](#6-conclusion)

---

# 1. High-Level Overview

This document describes how Ares 2 prevents unauthorized file system access in student code.

---

## 1.1 Complete Validation Flow Diagram

**Legend througout the document:**
- **🟢 Green** = Access allowed (no security violation)
- **🔴 Red** = Access blocked (security violation detected)
- **🌕 Yellow** = Continue to next check

![File System Security Validation Flow](BlockFileSystemAccess.drawio.png)

The UML activity diagram illustrates the complete validation flow from student code triggering a file access through all security checks to the final allow/block decision.

---

## 1.2 Configuration Settings

Security policies are configured through settings that instructors can adjust:

| Setting | Type | Description | Example |
|---------|------|-------------|---------|
| **aopMode** | `String` | AOP implementation | `"INSTRUMENTATION"` (Byte Buddy) or `"ASPECTJ"` |
| **restrictedPackage** | `String` | The package where student code is located | `"de.student."` |
| **allowedListedClasses** | `String[]` | Trusted helper classes students can use | `["de.student.util.Helper"]` |
| **pathsAllowedToBeRead** | `String[]` | Folders students can read from files | `["/tmp", "/home/student/input"]` |
| **pathsAllowedToBeOverwritten** | `String[]` | Folders students can write to files | `["/tmp", "/home/student/input"]` |
| **pathsAllowedToBeCreated** | `String[]` | Folders students create files | `["/tmp", "/home/student/input"]` |
| **pathsAllowedToBeExecuted** | `String[]` | Folders students can execute files | `["/tmp", "/home/student/input"]` |
| **pathsAllowedToBeDeleted** | `String[]` | Folders students can delete files | `["/tmp", "/home/student/input"]` |

---

## 1.3 Summary: When Is File Access Blocked?

Access is **BLOCKED** 🔴 when **ALL** conditions are true:

1. **Security Enabled**: `aopMode == "INSTRUMENTATION"` or `aopMode == "ASPECTJ"`
2. **Student Code Detected**: Call chain contains code in `restrictedPackage` and not in `allowedListedClasses`
3. **Path Not Allowed**: File path doesn't match any entry in the allowed paths list for that operation type
4. **Not Ares Internal**: Path is not an Ares internal configuration file
5. **No Special Rules**: No method-specific rule excludes this parameter/object field

**If ANY condition fails → Access is ALLOWED** 🟢

**Legend:**
- 🔴 Security exception thrown → Analysis and method execution terminated (forbidden file access detected)
- 🌕 Condition passed → Continue to next validation step
- 🟢 Access allowed → Analysis terminated and method execution continues (no forbidden file access detected)

---

## 1.4 What Code Is Trusted vs. Restricted?

**Trusted Code (No Restrictions):**
- Code outside the `restrictedPackage`
- Classes listed in `allowedListedClasses` within the student package
- Ares internal code

**Restricted Code (Subject to Security Checks):**
- All code within `restrictedPackage`

**Security Assumptions:** 
- Student code cannot modify Ares security settings (guaranteed by making settings private and modifying them only by reflection (which is excluded for student code))
- Student code cannot interfere with security monitoring (guaranteed by making settings private and modifying them only by reflection (which is excluded for student code))
- Student code executes after Ares is initialized (guaranteed by build pipeline)

---

# 2. Ares Monitors File System Methods

**What is AOP?** AOP (Aspect-Oriented Programming) is a technique that automatically runs security checks before certain methods execute, without modifying the student code. Think of it like a security guard checking IDs before people enter a building - the building code doesn't change, but everyone gets checked automatically when interacting with the building.

**Concrete Example:**

**Without AOP:** You would have to manually write security checks before every file access (if that is even possible).
```java
public void readFile(String path) {
    if (!isAllowed(path)) throw new SecurityException(); // Security check happens manually and has to be stated explicitly!
    Files.readString(Path.of(path));  // Actual code
}
```

**With AOP:** Ares automatically inserts this check before EVERY `Files.readString()`, so no code changes are required.
```java
public void readFile(String path) {
    Files.readString(Path.of(path));  // Actual code, Security check happens automatically in the background!
}
```

Ares automatically monitors file system operations by intercepting specific Java methods using one of two AOP implementations:

- **Byte Buddy (Instrumentation Mode)**: Automatically adds security checks when Java loads classes (called bytecode manipulation).
- **AspectJ (AspectJ Mode)**: Automatically adds security checks in a second compilation step (called weaving).

Both implementations set up "checkpoints" that activate **before** the file operation actually happens, giving Ares a chance to verify whether the operation should be allowed or blocked. The validation logic is identical in both modes, but interception coverage differs slightly (AspectJ uses explicit pointcuts; instrumentation uses type-hierarchy maps). For the authoritative lists, see `src/main/java/de/tum/cit/ase/ares/api/aop/java/aspectj/adviceandpointcut/JavaAspectJFileSystemPointcutDefinitions.aj` and `src/main/java/de/tum/cit/ase/ares/api/aop/java/instrumentation/pointcut/JavaInstrumentationPointcutDefinitions.java`.

---

## 2.1 FILE SYSTEM - READ Operations

**Security Component:** Read operation monitor

**Monitored APIs (representative; see pointcuts for the full list per mode):**
- **java.io**: stream/reader/data input types (e.g., `InputStream`, `Reader`, `DataInput`, `ObjectInput`, `RandomAccessFile`) and file metadata access via `File`
- **java.nio.file**: `Files` read APIs (e.g., `readAllBytes`, `readString`, `readAttributes`), directory traversal (`list`, `walk`, `find`)
- **java.nio.channels**: `FileChannel`/`AsynchronousFileChannel` read/size/position
- **java.nio.file.spi**: `FileSystemProvider` read/attribute APIs
- **file-backed APIs**: `ImageIO`, `DocumentBuilder`, `AudioSystem`, `Scanner`, `Toolkit` image loading

---

## 2.2 FILE SYSTEM - WRITE/OVERWRITE Operations

**Security Component:** Write operation monitor

**Monitored APIs (representative; see pointcuts for the full list per mode):**
- **java.io**: `OutputStream`/`RandomAccessFile` write APIs and `File` attribute mutation
- **java.nio.file**: `Files` write/mutate APIs (e.g., `write`, `newOutputStream`, `setAttribute`)
- **java.nio.channels**: `FileChannel`/`AsynchronousFileChannel` write/force/truncate
- **java.nio.file.spi**: `FileSystemProvider` write/copy/move APIs
- **file-backed APIs**: `ImageIO`, `AudioSystem`, `Transformer`, `Marshaller`, `DocPrintJob`, `HotSpotDiagnosticMXBean`, `Recording`

---

## 2.3 FILE SYSTEM - CREATE Operations

**Security Component:** Create operation monitor

**Monitored APIs (representative; see pointcuts for the full list per mode):**
- **java.io.File** creation methods (`createNewFile`, `mkdir`, `createTempFile`)
- **java.nio.file.Files** creation methods (`createFile`, `createDirectories`, `createTempFile`, links)
- **java.nio.file.FileSystems** and **java.nio.file.spi.FileSystemProvider** filesystem creation APIs
- **java.nio.channels.FileChannel** `open`

---

## 2.4 FILE SYSTEM - DELETE Operations

**Security Component:** Delete operation monitor

**Monitored APIs (representative; see pointcuts for the full list per mode):**
- **java.awt.Desktop** trash operations
- **java.io.File** delete/rename operations
- **java.nio.file.Files** delete operations
- **java.nio.file.spi.FileSystemProvider** delete operations

---

## 2.5 FILE SYSTEM - EXECUTE Operations

**What does "Execute" mean?** File system actions that trigger execution-like behavior such as filesystem traversal or opening files with their default programs (e.g., `Files.walk(...)` or `Desktop.open(...)`).

**Security Component:** Execute operation monitor

**Monitored APIs (representative; see pointcuts for the full list per mode):**
- **java.io.File** rename operations
- **java.nio.file.Files** move/copy operations
- **java.nio.channels.FileChannel** positioning/locking operations
- **java.awt.Desktop** open/edit/print/browse operations

---

---

# 3. Student Code Triggers Security Check

When student code (any code within the configured restricted package) calls one of these monitored methods, Ares automatically performs a security check **before** the file operation executes.

**Example:**
```java
// Student Code
package de.student.solution;

import java.nio.file.Files;
import java.nio.file.Path;

public class StudentSolution {
    public void readFile() throws Exception {
        // This call triggers JavaInstrumentationReadPathMethodAdvice
        String content = Files.readString(Path.of("/etc/passwd"));
    }
}
```

When the `Files.readString(Path.of("/etc/passwd"))` method is called, Ares intercepts the call:
- **Byte Buddy**: Automatically runs a security check before the method executes (technical implementation: `JavaInstrumentationReadPathMethodAdvice.onEnter()`)
- **AspectJ**: Automatically runs a security check before the method executes (technical implementation: `before()` advice in `JavaAspectJFileSystemAdviceDefinitions.aj`)

Ares then checks whether the student is allowed to access `Path.of("/etc/passwd")` **before** the file is actually read.

---

# 4. Ares Collects Information About the File Access

The security monitor collects information about what's happening: Which method is being called, what file path is being accessed, and where in the student code this is happening.

**Collection Mechanisms:**

- **Byte Buddy**: Uses special Java annotations (`@Advice`) to automatically capture information about the intercepted method (technical implementation: `JavaInstrumentationReadPathMethodAdvice.onEnter()`)
- **AspectJ**: Receives method information automatically through a parameter object called `JoinPoint` (technical implementation: `checkFileSystemInteraction()` method)

Both approaches collect the same information:

**What is collected:**
- **Method information**: Which method was called
- **Object state**: Internal state of the object
- **Parameters**: Values passed to the method

**Why do we need all three types of information?**

File paths can appear in **different places** depending on how the method is used:

1. **Method information is needed** to identify which operation is attempted and apply special handling rules
   - Example: Some methods like `Files.copy()` need both source and destination paths checked

2. **Object state is needed** because paths can be stored inside objects
   - Example: `file.delete()` - The path is in `file.path` field, not passed as parameter

3. **Parameters are needed** because paths are often passed as method arguments
   - Example: `Files.readString(Path.of("/etc/passwd"))` - The path `"/etc/passwd"` is a parameter

**What is NOT collected here:** Whether the access is allowed or blocked - that determination happens in the **next step** (Section 5: Ares Validates the File Access)

---

## 4.1 Which Method Was Called?

**1. What Information Do We Collect:**

| Information | Type | Description |
|-------------|------|-------------|
| **declaringTypeName** | `String` | **Class name** where the method is defined. Example: `"java.io.FileInputStream"`. |
| **methodName** | `String` | **Method name**. Example: `"read"` or `"<init>"` for constructors. |
| **methodSignature** | `String` | **Method signature** with parameter types. Example: `"(Ljava/lang/String;)V"` for a constructor taking a String. **Reading signatures:** `(Ljava/lang/String;)V` means "takes a String parameter, returns void (nothing)" - like a function signature: `constructor(String fileName) → returns nothing`. |

> 💡 **Method Signature Explained:** `(Ljava/lang/String;)V`
> - `(` = Parameter list begins
> - `Ljava/lang/String;` = Parameter of type String
> - `)` = Parameter list ends  
> - `V` = "void" (no return value)
>
> **More Examples:**
> - `()V` = no parameters, void return
> - `(II)I` = two int parameters, returns int
> - `(Ljava/nio/file/Path;)Ljava/lang/String;` = Path parameter, returns String

**2. How Do We Collect This Information:**

**Byte Buddy (Instrumentation) Mode:**
```java
@Advice.OnMethodEnter
public static void onEnter(
    @Advice.Origin("#t") String declaringTypeName,  // Class name
    @Advice.Origin("#m") String methodName,          // Method name
    @Advice.Origin("#s") String methodSignature      // Full signature
) {
    // Information is now available for validation
}
```

**AspectJ Mode:**
```aspectj
public void checkFileSystemInteraction(
    String action,
    JoinPoint thisJoinPoint
) {
    String declaringTypeName = thisJoinPoint.getSignature().getDeclaringTypeName();
    String methodName = thisJoinPoint.getSignature().getName();
    String methodSignature = thisJoinPoint.getSignature().toLongString();
    // Information is now available for validation
}
```

**3. How All Method Information Is Used:**
- Identify which file operation was attempted
- Look up special handling rules for specific methods
- Distinguish between different versions of the same method (overloading)
- Example: `java.io.FileInputStream.<init>(Ljava/lang/String;)V` → Identifies a `FileInputStream` constructor taking a String parameter

---

## 4.2 What's the Current State of the Object?

**1. What Information Do We Collect:**

| Information | Type | Description |
|-------------|------|-------------|
| **instance** | `Object` | **The object** on which the method is called (the `this` reference). `null` for constructors since the object doesn't exist yet. |
| **attributes** | `Object[]` | **Array of the object's internal field values**. The actual values stored in each field. **Note:** Empty for constructors since object doesn't exist yet. |

**2. How Do We Collect This Information:**

**Byte Buddy (Instrumentation) Mode:**
```java
@Advice.OnMethodEnter
public static void onEnter(
    @Advice.This(optional = true) Object instance  // The object on which method is called
) {
    // Extract attributes using reflection (see below)
}
```

**AspectJ Mode:**
```aspectj
public void checkFileSystemInteraction(
    String action,
    JoinPoint thisJoinPoint
) {
    Object instance = thisJoinPoint.getTarget();  // Get the object instance
    // Extract attributes using reflection (see below)
}
```

**Both modes then use identical attribute extraction (using Java's built-in ability to inspect object contents):**
```java
// For constructors, instance is null (object doesn't exist yet)
if (instance == null) {
    return;  // Skip for constructors
}

// Ares uses Java's inspection capabilities to access private fields
final Field[] fields = instance.getClass().getDeclaredFields();
final Object[] attributes = new Object[fields.length];

for (int i = 0; i < fields.length; i++) {
    try {
        fields[i].setAccessible(true);  // Make private fields accessible
        attributes[i] = fields[i].get(instance);  // Read the value
    } catch (InaccessibleObjectException | IllegalAccessException | SecurityException e) {
        continue;  // Skip fields that cannot be accessed
    } catch (IllegalArgumentException e) {
        throw new SecurityException("Invalid field access: " + fields[i].getName(), e);
    } catch (NullPointerException e) {
        throw new SecurityException("Null pointer accessing field: " + fields[i].getName(), e);
    } catch (ExceptionInInitializerError e) {
        throw new SecurityException("Initialization error for field: " + fields[i].getName(), e);
    }
}
```

**3. How All Object State Information Is Used:**
- Extract file paths that might be stored inside the object
- Check object fields for security violations
- Access internal state even if not passed as parameters
- Example: `File` object with `path` field = `"/etc/passwd"` → Path extracted from `attributes` array → Checked against allowed paths

---

## 4.3 What Parameters Were Passed?

**1. What Information Do We Collect:**

| Information | Type | Description |
|------------|------|-------------|
| **parameters** | `Object[]` | **Method arguments** - the values passed to the method when it was called. |

**2. How Do We Collect This Information:**

**Byte Buddy (Instrumentation) Mode:**
```java
@Advice.OnMethodEnter
public static void onEnter(
    @Advice.AllArguments Object[] parameters  // All method arguments
) {
    // Parameters array contains all values passed to the method
}
```

**AspectJ Mode:**
```aspectj
public void checkFileSystemInteraction(
    String action,
    JoinPoint thisJoinPoint
) {
    Object[] parameters = thisJoinPoint.getArgs();  // Get method arguments
}
```

**3. How All Parameter Information Is Used:**
- Extract file paths from method arguments
- Convert paths to standard format (`Path.normalize().toAbsolutePath()`)
- Check extracted paths against allowed paths list
- Example: `Files.readString(Path.of("/etc/passwd"))` → parameters = `[Path.of("/etc/passwd")]` → Checked against allowed paths

---

## 4.4 Information Passed to Security Validator

After collecting this information, Ares passes it to the security validation component.

> 💡 **Concrete Example:** `Files.readString(Path.of("/etc/passwd"))`
> ```
> Collected Information:
> ├─ action: "read"
> ├─ declaringTypeName: "java.nio.file.Files"
> ├─ methodName: "readString"
> ├─ methodSignature: "(Ljava/nio/file/Path;)Ljava/lang/String;"
> ├─ parameters: [Path.of("/etc/passwd")]
> ├─ attributes: [] (not applicable for static method)
> └─ instance: null (static method has no instance)
> ```

**Where does the action type come from?**

The action type (e.g., `"read"`) is **automatically determined** based on which method was intercepted:

| Intercepted Method Example | Action Type |
|---------------------------|-------------|
| `FileInputStream.read()`, `Files.readString()` | `"read"` |
| `FileOutputStream.write()`, `Files.write()` | `"overwrite"` |
| `File.createNewFile()`, `Files.createFile()` | `"create"` |
| `File.delete()`, `Files.delete()` | `"delete"` |
| `Files.copy()`, `Desktop.open()` | `"execute"` |

**For File System Operations:**
```java
checkFileSystemInteraction(
    "read",               // What type of operation? (from table above)
    declaringTypeName,    // Which class?
    methodName,           // Which method?
    methodSignature,      // Exact signature?
    attributes,           // Object's internal field values (Object[])
    parameters,           // Values passed to method (Object[])
    instance              // The object instance (for additional context)
)
```

**How is the action determined?**

The action type is **hardcoded** based on which methods are intercepted:

**Byte Buddy (Instrumentation Mode)** - Separate advice classes:
- `JavaInstrumentationReadPathMethodAdvice` → Uses `"read"`
- `JavaInstrumentationOverwritePathMethodAdvice` → Uses `"overwrite"`
- `JavaInstrumentationCreatePathMethodAdvice` → Uses `"create"`
- `JavaInstrumentationDeletePathMethodAdvice` → Uses `"delete"`
- `JavaInstrumentationExecutePathMethodAdvice` → Uses `"execute"`

**AspectJ Mode** - Multiple `before()` advice in one aspect:
- `before(): fileReadMethods()` → Uses `"read"`
- `before(): fileWriteMethods()` → Uses `"overwrite"`
- `before(): fileCreateMethods()` → Uses `"create"`
- `before(): fileDeleteMethods()` → Uses `"delete"`
- `before(): fileExecuteMethods()` → Uses `"execute"`

**Possible action values:**
- `"read"` - Reading from files
- `"overwrite"` - Writing to or modifying files
- `"create"` - Creating new files or directories
- `"delete"` - Deleting files or directories
- `"execute"` - Executing files or opening them with external programs

---

# 5. Ares Validates the File Access

The security validator performs a **series of checks** to decide whether the file operation should be allowed or blocked.

**The 5 Checks in Order** (stops at first "Allow"):

1. **Is Security Enabled?** → If no: 🟢
2. **Does the Call Come from Student Code?** → If no: 🟢  
3. **Which Permissions Need to Be Checked?** → Determine permission list
4. **Are All Affected Paths Allowed?** → If yes: 🟢, If no: 🔴
5. **Block and Throw Error** → Determine error message

---

## 5.1 Check 1: Is Security Enabled?

**1. Purpose**

Verify that file system security monitoring is turned on. This check ensures that Ares only performs security validations when explicitly enabled. Without this check, the security system would either always run (causing unnecessary overhead) or never run (leaving the system unprotected). The configuration-based approach allows instructors to enable or disable security monitoring as needed.

**2. How it works**

```java
// Read security settings
String aopMode = getValueFromSettings("aopMode");
if (aopMode == null || (!aopMode.equals("INSTRUMENTATION") && !aopMode.equals("ASPECTJ"))) {
    return;  // Security is disabled, allow everything
}
```

Both implementations check the `aopMode` setting but accept different values:
- Byte Buddy checks for `"INSTRUMENTATION"`
- AspectJ checks for `"ASPECTJ"`

**3. Used variables**

- **`aopMode`** (String): Configuration setting that determines whether security monitoring is active. Must be set to `"INSTRUMENTATION"` (Byte Buddy) or `"ASPECTJ"` (AspectJ) to enable file system security checks. Retrieved from the configuration settings.

**4. Result**

- Security enabled → 🌕 **Continue to Check 2**
- Security disabled → 🟢 **Allow operation** (no restrictions - analysis terminated)

---

## 5.2 Check 2: Does the Call Come from Student Code?

This check determines whether the file operation was triggered by restricted student code or by trusted framework code. It consists of three sub-steps:

### 5.2.1 Load Configuration

**1. Purpose**

Load the security configuration that defines which code is considered "student code" and which helper classes are trusted. This configuration is essential because not all code within a student project should be restricted - some utility classes provided by instructors should remain accessible. The configuration allows instructors to customize the security boundaries for each exercise.

**2. How it works**

```java
String restrictedPackage = getValueFromSettings("restrictedPackage");
String[] allowedClasses = getValueFromSettings("allowedListedClasses");
```

**3. Used variables**

- **`restrictedPackage`** (String): The Java package prefix where student code is located (e.g., `"de.student."`). Any code within this package is considered restricted unless explicitly allowed.
- **`allowedClasses`** (String[]): List of trusted helper class names that students can use even though they're in the restricted package (e.g., `["de.student.util.SafeHelper"]`). These classes are pre-approved by instructors.

**4. Result**

Configuration loaded → 🌕 **Continue to 5.2.2**

### 5.2.2 Analyze the Call Chain

**1. Purpose**

Walk through the call history to find if restricted student code triggered the file operation. This is like following breadcrumbs backwards to see how we got here. This is the core security check that distinguishes between legitimate framework operations (e.g., JUnit loading test classes) and potentially malicious student code (e.g., trying to read sensitive files).

> 💡 **Analogy:** Like a detective following footprints backwards:
> - **Crime Scene:** `Files.readString("/etc/passwd")` [we are here now]
> - **Step Back:** `StudentCode.readSecretFile()` [AHA! Student code found! 🔴]
> - **Further Back:** `TestClass.testStudent()` [this is the test]
> - **Origin:** JUnit Framework [trustworthy ✓]
>
> **Result:** Student code attempted to read a forbidden file!

**Visual Example - Walking the Call History:**
```
[Top]    Files.readString("/etc/passwd")     ← Current method being called
[...]    StudentCode.readSecretFile()         ← Found student code! ✓
[...]    StudentCode.exploit()                ← Still in student package
[...]    TestClass.testStudent()              ← Test method (outside student package)
[Bottom] JUnit framework
         
Result: Student code detected at StudentCode.readSecretFile()
```

**2. How it works**

```java
String violatingMethod = checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
if (violatingMethod == null) {
    return;  // Not from student code
}
```

**Detailed steps:**

1. **Capture the Call History:**
   ```java
   StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
   ```

2. **Walk Through Each Method Call:**
   ```java
   for (StackTraceElement element : stackTrace) {
       String className = element.getClassName();
   ```

3. **Skip Ares Internal Code and Java ClassLoader:**
   ```java
   boolean shouldSkip = false;
   for (String ignorePattern : IGNORE_CALLSTACK) {
       if (className.startsWith(ignorePattern)) {
           shouldSkip = true;
           break;
       }
   }
   if (shouldSkip) {
       continue;  // Skip this method, it's part of Ares or Java internals
   }
   ```

4. **Check if This is Student Code:**
   ```java
   if (className.startsWith(restrictedPackage)) {
       // This method is in the student package
   ```

5. **Check if it's an Allowed Helper Class:**
   ```java
       if (!isInAllowedList(allowedClasses, element)) {
           // Not in the allowed list → This is restricted student code
           return className + "." + element.getMethodName();
       }
   }
   ```

6. **If No Student Code Found:**
   ```java
   return null;  // No restricted student code in call chain
   ```

**3. Used variables**

- **`restrictedPackage`** (String): From 5.2.1 - defines student code boundary
- **`allowedClasses`** (String[]): From 5.2.1 - list of trusted helper classes
- **`violatingMethod`** (String): Returns the fully qualified method name of the student code that triggered the file operation, or `null` if no student code found
- **`stackTrace`** (StackTraceElement[]): The complete call chain showing all method calls leading to this point
- **`IGNORE_CALLSTACK`** (String[]): Instrumentation uses `["java.lang.ClassLoader", "de.tum.cit.ase.ares.api."]`; AspectJ uses `["java.lang.ClassLoader", "de.tum.cit.ase.ares.api.", "de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension", "de.tum.cit.ase.ares.api.jqwik.JqwikSecurityExtension", "de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions"]`
- **`className`** (String): The fully qualified class name for each method in the call stack
- **`isInAllowedList()`** (method): Helper function that returns `true` if the class is listed in the `allowedClasses` array

**4. Result**

- Found student code calling the file operation → Returns method name like `"de.student.StudentCode.exploit"` → 🌕 **Continue to 5.2.3**
- No student code found in call chain → Returns `null` → 🟢 **Allow operation** (called from test framework or trusted code - analysis terminated)

### 5.2.3 Find Which Test Called the Student Code

**1. Purpose**

Identify which test method triggered the student code. This helps instructors know which test case revealed the security violation.

**2. How it works**

```java
String testMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
```

Continue walking backwards through the call history (from 5.2.2) to find the first method **outside** the student package - this is the test method that called the student code.

**Example from the visual diagram above:**
```
[...] StudentCode.exploit()          ← Student code (found in 5.2.2)
[...] TestClass.testStudent()        ← FOUND: First method outside student package
```

Result: `"org.junit.TestClass.testStudent"` - this is the test method that invoked the student code

**3. Used variables**

- **`restrictedPackage`** (String): From 5.2.1 - used to identify where student code ends and test code begins
- **`testMethod`** (String): The fully qualified name of the test method that invoked the student code (e.g., `"org.junit.TestClass.testStudent"`)

**4. Result**

Test method identified → Stored for error message → 🌕 **Continue to Check 3**

---

## 5.3 Check 3: Determine Which Security Permissions to Check

**1. Purpose**

Determine which security actions to validate based on method parameters. **Real-world analogy:** Like a door that needs both a key AND a fingerprint scan - some file operations need multiple permissions checked simultaneously.

Most methods need just one permission (e.g., "read"), but some need multiple. For example, `Files.write()` with `CREATE` and `WRITE` options needs both "create" and "overwrite" permissions. This check analyzes the operation mode to determine which permission types need validation.

**2. How it works**

```java
List<Map.Entry<String, Boolean>> actionsToValidate = deriveActionChecks(action, parameters);
```

**How It Works:**

1. **Search for StandardOpenOption in parameters:**
   - If found, map each option to corresponding actions
   - Build list of all required permissions
   
2. **If no StandardOpenOption found:**
   - Use the `action` parameter from the advice class (e.g., "read", "overwrite")
   - This is the default behavior for simple operations

3. **Return list of actions with non-existence flags:**
   - Each entry: `(action, canBeNonExistent)`
   - Example: `[("create", true), ("overwrite", false)]`

**Example with StandardOpenOption:**
```java
Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
```
This triggers validation for **both** "create" and "overwrite" actions.

**Example without StandardOpenOption:**
```java
Files.readString(path);
```
This uses the default action "read" from `JavaInstrumentationReadPathMethodAdvice`.

> 💡 **For Beginners:** In 90% of cases, only one permission is checked (derived from the method name, e.g., `Files.readString()` → "read"). Only for complex operations like `Files.write()` with multiple modes are multiple permissions checked simultaneously.

**Mapping Rules with Everyday Examples:**

| File Opening Mode | Permission Needed | Can Path Be Non-Existent? | Everyday Example |
|--------------------|----------------|--------------------------|-----------------|
| `CREATE`, `CREATE_NEW` | `"create"` | Yes | Create new Word file |
| `WRITE`, `APPEND`, `TRUNCATE_EXISTING` | `"overwrite"` | No | Edit/overwrite existing file |
| `READ` | `"read"` | No | Open file for reading |
| `DELETE_ON_CLOSE` | `"delete"` | No | Temporary file deleted on close |

**Why Can CREATE Paths Be Non-Existent?**
When creating a new file, the file doesn't exist yet. The security check must validate the path before the file is created.

**Multiple Permissions:** If multiple modes are specified, **all** corresponding permissions are checked.

**Default:** If no `StandardOpenOption` found, uses the `action` parameter passed to `checkFileSystemInteraction()` based on which method was intercepted (e.g., `FileInputStream` → `"read"`).

**3. Used variables**

- **`action`** (String): The base action type from section 4.4 (e.g., `"read"`, `"overwrite"`, `"create"`, `"delete"`, `"execute"`)
- **`parameters`** (Object[]): Method parameters that may contain `StandardOpenOption` values
- **`actionsToValidate`** (List<Map.Entry<String, Boolean>>): List of action-permission pairs to check. The Boolean indicates whether the path can be non-existent for this action.

**4. Result**

List of actions to validate (e.g., `[("create", true), ("overwrite", false)]`) → 🌕 **Continue to Check 4**

---

## 5.4 Check 4: Extract and Validate File Paths

This check finds all file paths involved in the operation and validates them against the allowed paths list. It consists of five sub-steps:

**Overview of the 5 Steps:**
1. **5.4.1** Load list of allowed paths (e.g., `["/tmp", "/home/student/output"]`)
2. **5.4.2** Apply special method rules (ignore some parameters)
3. **5.4.3** Extract paths from **parameters** and check against list
4. **5.4.4** Extract paths from **object state** and check against list
5. **5.4.5** Exception for Ares-internal files (so Ares itself can function)

### 5.4.1 Load List of Allowed Paths

**1. Purpose**

Load the configuration that specifies which file paths are allowed for the current operation type. Each action type (read, write, create, delete, execute) has its own allowlist of permitted paths. This separation allows instructors to grant fine-grained permissions - for example, students might be allowed to read from `/input` but only write to `/output`. Without action-specific path lists, the system would need to either allow all paths (insecure) or use one restrictive list for all operations (too limiting).

**2. How it works**

```java
String[] allowedPaths = getValueFromSettings(
    switch (action) {
        case "read" -> "pathsAllowedToBeRead";
        case "overwrite" -> "pathsAllowedToBeOverwritten";
        case "create" -> "pathsAllowedToBeCreated";
        case "execute" -> "pathsAllowedToBeExecuted";
        case "delete" -> "pathsAllowedToBeDeleted";
    }
);
```

**3. Used variables**

- **`action`** (String): The action type from 5.3 (e.g., `"read"`, `"overwrite"`, `"create"`, `"delete"`, `"execute"`)
- **`allowedPaths`** (String[]): Array of file path prefixes that are allowed for this action type. Paths from configuration like `["/tmp", "/home/student/output"]`

**4. Result**

Allowed paths list loaded → 🌕 **Continue to 5.4.2**

### 5.4.2 Apply Special Rules for Specific Methods

**1. Purpose**

Apply method-specific rules to determine which parameters or object fields should be checked. Some methods have complex signatures where not all parameters represent file paths. These special rules prevent false positives while maintaining security.

**2. How it works**

```java
IgnoreValues ignoreRule = FILE_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(
    declaringTypeName + "." + methodName,
    IgnoreValues.NONE
);
Object[] filteredVariables = filterVariables(parameters, ignoreRule);
```

**Current file system special cases:**

| Method | What We Check | Why |
|--------|---------------|-----|
| `File.delete()` | Only the relevant `File` attribute | The path is stored in the `File` object, not passed as a parameter |
| `File.deleteOnExit()` | Only the relevant `File` attribute | The path is stored in the `File` object, not passed as a parameter |
| `File.createNewFile()` | Only the relevant `File` attribute | The path is stored in the `File` object, not passed as a parameter |

**4. Result**

Filtered variables ready for path validation → 🌕 **Continue to 5.4.3**

### 5.4.3 Check Method Parameters for File Paths

**1. Purpose**

Extract and validate all file paths from method parameters. This step systematically extracts paths from all parameter types and validates each against the allowed paths list.

**2. How it works**

```java
// 1. Filter parameters based on special rules
Object[] filteredVariables = filterVariables(parameters, ignoreRule);

// 2. Extract paths from each parameter
for (Object variable : filteredVariables) {
    Path candidate = variableToPath(variable);
    if (candidate == null) {
        continue;
    }

    // 3. Normalize and validate path
    Path normalizedCandidate = candidate.normalize().toAbsolutePath();
    
    for (String allowedPathString : allowedPaths) {
        Path allowedPath = Path.of(allowedPathString).normalize().toAbsolutePath();
        if (normalizedCandidate.startsWith(allowedPath)) {
            continue;  // Path is allowed
        }
    }
    // Path is NOT allowed → Record violation
}
```

**3. Used variables**

- **`parameters`** (Object[]): From section 4.3 - all method parameters
- **`ignoreRule`** (IgnoreValues): From 5.4.2 - determines which parameters to check
- **`filteredVariables`** (Object[]): From 5.4.2 - subset of parameters to validate
- **`allowedPaths`** (String[]): From 5.4.1 - list of allowed path prefixes
- **`candidate`** (Path): Converted path candidate (String/Path/File only; other types are ignored)
- **`normalizedCandidate`** (Path): Normalized absolute path of the file being accessed
- **`allowedPath`** (Path): Normalized, absolute, real path of an allowed path prefix
- **`variableToPath()`** (method): Helper that converts only `String`, `Path`, or `File` values to `Path`

**4. Result**

- All paths allowed → 🌕 **Continue to 5.4.4**
- Forbidden path found → Record violation → 🌕 **Continue to 5.4.5** (check if Ares internal)

### 5.4.4 Check Object State for File Paths

**1. Purpose**

Extract and validate all file paths from the object's internal state. This step systematically extracts paths from all object's internal state types and validates each against the allowed paths list.

**2. How it works**

**Same process as checking parameters (Section 5.4.3 above)**, but we examine the object's internal field values (from Section 4.2) instead of method parameters. The path normalization and validation logic is identical.

**3. Used variables**

- **`attributes`** (Field[]): From section 4.2 - the object's internal fields
- **`attributeValues`** (Object[]): From section 4.2 - values of the object's fields
- **`ignoreRule`** (IgnoreValues): From 5.4.2 - determines which object fields to check
- **`allowedPaths`** (String[]): From 5.4.1 - list of allowed path prefixes

**4. Result**

- All paths allowed → 🌕 **Continue to 5.4.5**
- Forbidden path found → Record violation → 🌕 **Continue to 5.4.5** (check if Ares internal)

### 5.4.5 Allow Ares Internal Files

**1. Purpose**

Allow Ares to access its own configuration and resource files. Ares needs to read its own files (localization messages, configuration, internal classes) to function properly. Without this exception, Ares would block itself from accessing necessary resources. This allowlist ensures that only genuine Ares internal files are exempted, not files that students might name similarly to bypass security.

**2. How it works**

```java
boolean isInternalAllowed = INTERNAL_PATH_SUFFIXES.stream()
    .anyMatch(pathViolation::endsWith);

if (!isInternalAllowed) {
    throw SecurityException;
}
```

**Ares Internal Files:**
- `"ares/api/localization/Messages.class"`
- `"ares/api/localization/messages.class"`
- `"ares/api/localization/messages.properties"`
- `"ares/api/util/LruCache.class"`

**3. Used variables**

- **`pathViolation`** (String): The file path that was flagged as forbidden in 5.4.3 or 5.4.4
- **`INTERNAL_PATH_SUFFIXES`** (List<String>): Predefined list of Ares internal file path suffixes that should always be allowed
- **`isInternalAllowed`** (boolean): `true` if the path ends with an Ares internal file suffix, `false` otherwise

**4. Result**

- Forbidden path found (not an Ares internal file) → 🔴 **Block and throw security exception** (analysis terminated - forbidden file access detected)
- All paths allowed → 🟢 **Allow the file operation** (analysis terminated - no forbidden access detected)

---

## 5.5 Check 5: Block Access with Detailed Error Message

🔴 **Security Exception Thrown - Analysis Terminated**

**1. Purpose**

Block the forbidden file operation and provide a comprehensive error message. When a security violation is detected, it's crucial to give instructors and students clear information about what went wrong, where it happened, and which test triggered it. A generic "access denied" message would be unhelpful for debugging. The detailed message helps instructors identify the exact violation and helps students understand which part of their code caused the security issue.

**2. How it works**

```java
throw new SecurityException(localize(
    "security.advice.illegal.file.execution",
    violatingMethod,           // de.student.StudentCode.exploit
    action,                    // "read"
    violatingPath,             // "/etc/passwd"
    fullMethodSignature +      // "java.io.FileInputStream.<init>(Ljava/lang/String;)V"
    " (called by " + studentCalledMethod + ")"  // " (called by org.junit.TestClass.testStudent)"
));
```

**3. Used variables**

- **`violatingMethod`** (String): From 5.2.2 - the student method that attempted the file access (e.g., `"de.student.StudentCode.exploit"`)
- **`action`** (String): From section 4.4 - the type of operation attempted (e.g., `"read"`, `"write"`, `"delete"`)
- **`violatingPath`** (String): From 5.4.3/5.4.4 - the forbidden file path that was accessed (e.g., `"/etc/passwd"`)
- **`fullMethodSignature`** (String): From section 4.1 - complete method signature showing exactly which method was called (e.g., `"java.io.FileInputStream.<init>(Ljava/lang/String;)V"`)
- **`studentCalledMethod`** (String): From 5.2.3 - the test method that invoked the student code (e.g., `"org.junit.TestClass.testStudent"`)
- **`localize()`** (method): Translates the error message to the configured language

**4. Result**

**Example Error Message:**
```
SecurityException: Unauthorized file access detected
Student method: de.student.StudentCode.exploit
Operation: read
Forbidden path: /etc/passwd
Attempted via: java.nio.file.Files.readString(Ljava/nio/file/Path;)Ljava/lang/String;
Test method: org.junit.TestClass.testStudent
```

🔴 **SecurityException thrown** - Analysis terminated, file operation blocked

---

# 6. Conclusion

## Summary for Programming Instructors (TL;DR)

**What does Ares do?**
- ✅ Monitors a **broad set of file system APIs** automatically (Read, Write, Create, Delete, Execute)
- ✅ Blocks **student code** from accessing **forbidden paths**
- ✅ **Configurable via YAML** - You determine which paths are allowed
- ✅ Works **without code changes** to student code (via AOP)
- ✅ Provides **clear error messages** with exact source (which method, which path, which test)

**When do you need this?**
- When students should practice file operations (e.g., reading/writing files)
- But you want to prevent them from reading sensitive files or deleting system files
- Example: Allow `/tmp` for exercises, block `/etc` and `/home`

**How does it work (simplified)?**
1. Student calls `Files.readString("/etc/passwd")`
2. Ares intercepts the call (AOP) and checks:
   - Does this come from student code? ✓ Yes
   - Is `/etc/passwd` in the allowed list? ✗ No
3. Ares blocks and throws a meaningful exception

---

## Technical Details

The file system security mechanism provides **comprehensive protection** through:

1. **Extensive API Coverage**: Broad interception across file system operations
2. **Call Stack Analysis**: Distinguishes trusted framework code from untrusted student code
3. **Path-Based Validation**: Strict enforcement of allowed file paths
4. **Detailed Error Messages**: Precise violation reporting with full call context
5. **Flexible Configuration**: YAML-based security policies

The system operates **transparently** using AOP techniques, requiring no modifications to student code, and enforces policies **before** dangerous operations execute.

> 💡 **Byte Buddy vs. AspectJ:** For most use cases the validation flow is the same, but interception coverage differs slightly because AspectJ uses explicit pointcuts while instrumentation uses type-hierarchy maps.

**Implementation Differences:**

| Aspect | Byte Buddy (Instrumentation) | AspectJ |
|--------|------------------------------|----------|
| **Weaving Time** | Runtime (when classes are loaded) | Compile-time or load-time |
| **Configuration** | `aopMode = "INSTRUMENTATION"` | `aopMode = "ASPECTJ"` |
| **Advice Structure** | Separate class per operation type | Single aspect with multiple `before()` advice |
| **Method Info Access** | `@Advice.Origin` annotations | `JoinPoint.getSignature()` |
| **Instance Access** | `@Advice.This` annotation | `JoinPoint.getTarget()` |
| **Parameters Access** | `@Advice.AllArguments` annotation | `JoinPoint.getArgs()` |
| **Validation Logic** | Delegates to `JavaInstrumentationAdviceFileSystemToolbox` | Implements in `JavaAspectJFileSystemAdviceDefinitions` |

**Both implementations provide the same validation flow and permission checks; intercepted APIs differ slightly by mode.**
