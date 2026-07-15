# Command System Security Mechanism

## Table of Contents

1. [High-Level Overview](#1-high-level-overview)
   - [1.1 Complete Validation Flow Diagram](#11-complete-validation-flow-diagram)
   - [1.2 Configuration Settings](#12-configuration-settings)
   - [1.3 Summary: When Is Command Execution Blocked?](#13-summary-when-is-command-execution-blocked)
   - [1.4 What Code Is Trusted vs. Restricted?](#14-what-code-is-trusted-vs-restricted)
2. [Ares Monitors Command System Methods](#2-ares-monitors-command-system-methods)
   - [2.1 COMMAND SYSTEM - EXECUTE Operations](#21-command-system---execute-operations)
3. [Student Code Triggers Security Check](#3-student-code-triggers-security-check)
4. [Ares Collects Information About the Command Execution](#4-ares-collects-information-about-the-command-execution)
   - [4.1 Which Method Was Called?](#41-which-method-was-called)
   - [4.2 What's the Current State of the Object?](#42-whats-the-current-state-of-the-object)
   - [4.3 What Parameters Were Passed?](#43-what-parameters-were-passed)
   - [4.4 Information Passed to Security Validator](#44-information-passed-to-security-validator)
5. [Ares Validates the Command Execution](#5-ares-validates-the-command-execution)
   - [5.1 Check 1: Is Security Enabled?](#51-check-1-is-security-enabled)
   - [5.2 Check 2: Does the Call Come from Student Code?](#52-check-2-does-the-call-come-from-student-code)
     - [5.2.1 Load Configuration](#521-load-configuration)
     - [5.2.2 Analyze the Call Chain](#522-analyze-the-call-chain)
     - [5.2.3 Find Which Test Called the Student Code](#523-find-which-test-called-the-student-code)
   - [5.3 Check 3: Extract and Validate Commands from Parameters](#53-check-3-extract-and-validate-commands-from-parameters)
     - [5.3.1 Load List of Allowed Commands](#531-load-list-of-allowed-commands)
     - [5.3.2 Apply Special Rules for Specific Methods](#532-apply-special-rules-for-specific-methods)
     - [5.3.3 Check Method Parameters for Commands](#533-check-method-parameters-for-commands)
     - [5.3.4 Check Executable File Paths (pathsAllowedToBeExecuted)](#534-check-executable-file-paths-pathsallowedtobeexecuted)
   - [5.4 Check 4: Extract and Validate Commands from Object State](#54-check-4-extract-and-validate-commands-from-object-state)
   - [5.5 Check 5: Block Access with Detailed Error Message](#55-check-5-block-access-with-detailed-error-message)
6. [Conclusion](#6-conclusion)

---

# 1. High-Level Overview

This document describes how Ares 2 prevents unauthorized command execution in student code.

---

## 1.1 Complete Validation Flow Diagram

**Legend throughout the document:**
- **🟢 Green** = Access allowed (no security violation)
- **🔴 Red** = Access blocked (security violation detected)
- **🌕 Yellow** = Continue to next check

The validation flow is similar to the file system security mechanism but adapted for command execution:

1. Student code calls a command execution method (e.g., `Runtime.exec()`)
2. Ares intercepts the call before execution
3. Security checks determine if the command is allowed
4. If allowed, execution proceeds; if blocked, SecurityException is thrown

---

## 1.2 Configuration Settings

Security policies are configured through settings that instructors can adjust:

| Setting | Type | Description | Example |
|---------|------|-------------|---------|
| **aopMode** | `String` | AOP implementation | `"INSTRUMENTATION"` (Byte Buddy) or `"ASPECTJ"` |
| **restrictedPackage** | `String` | The package where student code is located | `"de.student."` |
| **allowedListedClasses** | `String[]` | Trusted helper classes students can use | `["de.student.util.Helper"]` |
| **commandsAllowedToBeExecuted** | `String[]` | Commands students are allowed to execute | `["ls", "echo"]` |
| **argumentsAllowedToBePassed** | `String[][]` | Arguments allowed for each command | `[["--help"], ["-n", "Hello"]]` |
| **pathsAllowedToBeExecuted** | `String[]` | Optional allow-list of executable file paths the commands may resolve to (see 5.3.4) | `["/usr/bin/ls"]` |

---

## 1.3 Summary: When Is Command Execution Blocked?

Access is **BLOCKED** 🔴 when **ALL** conditions are true:

1. **Security Enabled**: `aopMode == "INSTRUMENTATION"` or `aopMode == "ASPECTJ"`
2. **Student Code Detected**: Call chain contains code in `restrictedPackage` and not in `allowedListedClasses`
3. **Command Not Allowed**: Command doesn't match any entry in `commandsAllowedToBeExecuted` with matching arguments from `argumentsAllowedToBePassed`, **or** (when `pathsAllowedToBeExecuted` is configured) the command's executable file path is not covered by that allow-list (see 5.3.4)

**If ANY condition fails → Access is ALLOWED** 🟢

**Legend:**
- 🔴 Security exception thrown → Analysis and method execution terminated (forbidden command execution detected)
- 🌕 Condition passed → Continue to next validation step
- 🟢 Access allowed → Analysis terminated and method execution continues (no forbidden command execution detected)

---

## 1.4 What Code Is Trusted vs. Restricted?

**Trusted Code (No Restrictions):**
- Code outside the `restrictedPackage`
- Classes listed in `allowedListedClasses` within the student package
- Ares internal code

**Restricted Code (Subject to Security Checks):**
- All code within `restrictedPackage`

**Security Assumptions:** 
- Student code cannot modify Ares security settings (guaranteed by making settings private; reflection is disabled for student code)
- Student code cannot interfere with security monitoring (guaranteed by making settings private; reflection is disabled for student code)
- Student code executes after Ares is initialized (guaranteed by build pipeline)

---

# 2. Ares Monitors Command System Methods

**What is AOP?** AOP (Aspect-Oriented Programming) is a technique that automatically runs security checks before certain methods execute, without modifying the student code. Think of it like a security guard checking IDs before people enter a building - the building code doesn't change, but everyone gets checked automatically when interacting with the building.

**Concrete Example:**

**Without AOP:** You would have to manually write security checks before every command execution (if that is even possible).
```java
public void runCommand(String cmd) {
    if (!isAllowed(cmd)) throw new SecurityException(); // Security check happens manually and has to be stated explicitly!
    Runtime.getRuntime().exec(cmd);  // Actual code
}
```

**With AOP:** Ares automatically inserts this check before EVERY `Runtime.exec()`, so no code changes are required.
```java
public void runCommand(String cmd) {
    Runtime.getRuntime().exec(cmd);  // Actual code, Security check happens automatically in the background!
}
```

Ares automatically monitors command system operations by intercepting specific Java methods using one of two AOP implementations:

- **Byte Buddy (Instrumentation Mode)**: Automatically adds security checks when Java loads classes (called bytecode manipulation).
- **AspectJ (AspectJ Mode)**: Automatically adds security checks in a second compilation step (called weaving).

Both implementations set up "checkpoints" that activate **before** the command operation actually happens, giving Ares a chance to verify whether the operation should be allowed or blocked. The validation logic is aligned. The command pointcuts are kept in sync between AspectJ and instrumentation.

---

## 2.1 COMMAND SYSTEM - EXECUTE Operations

**Security Component:** Execute operation monitor

**Monitored Methods (3 total):**
- **java.lang.Runtime**: `exec()`
- **java.lang.ProcessBuilder**: `<init>()` (constructor), `start()`

---

**Total Monitored Methods: 3**

**Note:** This count is based on the method lists shown above. The actual number may vary slightly as some methods may support multiple overloaded variants. Both implementations monitor the same set of methods.

---

# 3. Student Code Triggers Security Check

When student code (any code within the configured restricted package) calls one of these monitored methods, Ares automatically performs a security check **before** the command operation executes.

**Example:**
```java
// Student Code
package de.student.solution;

public class StudentSolution {
    public void executeCommand() throws Exception {
        // This call triggers JavaInstrumentationExecuteCommandMethodAdvice
        Runtime.getRuntime().exec("rm -rf /");
    }
}
```

When the `Runtime.getRuntime().exec("rm -rf /")` method is called, Ares intercepts the call:
- **Byte Buddy**: Automatically runs a security check before the method executes (technical implementation: `JavaInstrumentationExecuteCommandMethodAdvice.onEnter()`)
- **AspectJ**: Automatically runs a security check before the method executes (technical implementation: `before()` advice in `JavaAspectJCommandSystemAdviceDefinitions.aj`)

Ares then checks whether the student is allowed to execute `"rm -rf /"` **before** the command is actually executed.

---

# 4. Ares Collects Information About the Command Execution

The security monitor collects information about what's happening: Which method is being called, what command is being executed, and where in the student code this is happening.

**Collection Mechanisms:**

- **Byte Buddy**: Uses special Java annotations (`@Advice`) to automatically capture information about the intercepted method (technical implementation: `JavaInstrumentationExecuteCommandMethodAdvice.onEnter()`)
- **AspectJ**: Receives method information automatically through a parameter object called `JoinPoint` (technical implementation: `checkCommandSystemInteraction()` method)

Both approaches collect the same information:

**What is collected:**
- **Method information**: Which method was called
- **Object state**: Internal state of the object
- **Parameters**: Values passed to the method

**Why do we need all three types of information?**

Commands can appear in **different places** depending on how the method is used:

1. **Method information is needed** to identify which operation is attempted and apply special handling rules
   - Example: `ProcessBuilder.start()` needs to check the command stored in the object, not the parameters

2. **Object state is needed** because commands can be stored inside objects
   - Example: `processBuilder.start()` - The command is in `processBuilder.command` field, not passed as parameter

3. **Parameters are needed** because commands are often passed as method arguments
   - Example: `Runtime.getRuntime().exec("ls -la")` - The command `"ls -la"` is a parameter

**What is NOT collected here:** Whether the access is allowed or blocked - that determination happens in the **next step** (Section 5: Ares Validates the Command Execution)

---

## 4.1 Which Method Was Called?

**1. What Information Do We Collect:**

| Information | Type | Description |
|-------------|------|-------------|
| **declaringTypeName** | `String` | **Class name** where the method is defined. Example: `"java.lang.Runtime"`. |
| **methodName** | `String` | **Method name**. Example: `"exec"` or `"<init>"` for constructors. |
| **methodSignature** | `String` | **Method signature** with parameter types in source form. Example: `"(java.lang.String)"` for exec taking a String. **Reading signatures:** `(java.lang.String)` means "takes a String parameter". The return type is not part of the signature. |

> 💡 **Method Signature Explained:** `(java.lang.String)`
> - `(` = Parameter list begins
> - `java.lang.String` = Parameter of type String
> - `)` = Parameter list ends
>
> Byte Buddy's `@Advice.Origin("#s")` yields this source form (no return type). The AspectJ side produces the same shape via `formatSignature()`, so both backends report signatures as `<declaringType>.<methodName>(<paramType1>,<paramType2>,...)` once the class and method name are prepended.
>
> **More Examples:**
> - `(java.lang.String[])` = String array parameter
> - `()` = no parameters

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
public void checkCommandSystemInteraction(
    String action,
    JoinPoint thisJoinPoint
) {
    String declaringTypeName = thisJoinPoint.getSignature().getDeclaringTypeName();
    String methodName = thisJoinPoint.getSignature().getName();
    // formatSignature() strips Java modifiers (e.g. "public transient") from
    // AspectJ's join-point signature and normalises constructors to "<init>",
    // so both backends produce the same signature shape.
    String fullMethodSignature = formatSignature(thisJoinPoint.getSignature());
    // Information is now available for validation
}
```

**3. How All Method Information Is Used:**
- Identify which command operation was attempted
- Look up special handling rules for specific methods
- Distinguish between different versions of the same method (overloading)
- Example: `java.lang.Runtime.exec(java.lang.String)` → Identifies a `Runtime.exec` method taking a String parameter

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
public void checkCommandSystemInteraction(
    String action,
    JoinPoint thisJoinPoint
) {
    Object instance = thisJoinPoint.getTarget();  // Get the object instance
    // Extract attributes using reflection (see below)
}
```

**Both modes then use best-effort attribute extraction (using Java's built-in ability to inspect object contents):**
```java
// For constructors, instance is null (object doesn't exist yet)
// → attributes stays an empty array

// Ares uses Java's inspection capabilities to access private fields
final Field[] fields = instance != null ? instance.getClass().getDeclaredFields() : new Field[0];
final Object[] attributes = new Object[fields.length];

for (int i = 0; i < fields.length; i++) {
    try {
        fields[i].setAccessible(true);  // Make private fields accessible
        attributes[i] = fields[i].get(instance);  // Read the value
    } catch (InaccessibleObjectException | IllegalAccessException | SecurityException
            | IllegalArgumentException | NullPointerException | ExceptionInInitializerError e) {
        // Skip the unreadable field rather than aborting the whole interaction:
        // a JDK-internal field that throws on read must not turn a JDK-side
        // reflection limit into an Ares SecurityException. The security check
        // still runs over the parameters and the readable fields.
        continue;
    }
}
```

**Difference between the modes for unreadable fields:**
- **Byte Buddy**: skips the unreadable field (the corresponding `attributes` slot stays `null`) and continues.
- **AspectJ**: null-fills the unreadable field, but treats the `command` field of `ProcessBuilder` before `start()` as **critical**: if that field cannot be read, Ares cannot inspect what would be executed, so it **fails closed** and later throws a SecurityException reporting the command as `<unknown>` instead of letting an uninspected command through.

**3. How All Object State Information Is Used:**
- Extract commands that might be stored inside the object
- Check object fields for security violations
- Access internal state even if not passed as parameters
- Example: `ProcessBuilder` object with `command` field = `["rm", "-rf", "/"]` → Command extracted from `attributes` array → Checked against allowed commands

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
public void checkCommandSystemInteraction(
    String action,
    JoinPoint thisJoinPoint
) {
    Object[] parameters = thisJoinPoint.getArgs();  // Get method arguments
}
```

**3. How All Parameter Information Is Used:**
- Extract commands from method arguments
- Convert command representations (String, String[], List<String>)
- Check extracted commands against allowed commands list
- Example: `Runtime.getRuntime().exec("ls -la")` → parameters = `["ls -la"]` → Checked against allowed commands

---

## 4.4 Information Passed to Security Validator

After collecting this information, Ares passes it to the security validation component.

> 💡 **Concrete Example:** `Runtime.getRuntime().exec("rm -rf /")`
> ```
> Collected Information:
> ├─ action: "execute"
> ├─ declaringTypeName: "java.lang.Runtime"
> ├─ methodName: "exec"
> ├─ methodSignature: "(java.lang.String)"
> ├─ parameters: ["rm -rf /"]
> ├─ attributes: [...] (Runtime internal fields)
> └─ instance: Runtime instance
> ```

**Where does the action type come from?**

The action type is **automatically determined** based on which method was intercepted. For command system operations, the action is always `"execute"`:

| Intercepted Method Example | Action Type |
|---------------------------|-------------|
| `Runtime.exec()`, `ProcessBuilder.start()`, `new ProcessBuilder()` | `"execute"` |

**For Command System Operations:**
```java
checkCommandSystemInteraction(
    "execute",            // What type of operation? (always "execute" for command system)
    declaringTypeName,    // Which class?
    methodName,           // Which method?
    methodSignature,      // Exact signature?
    attributes,           // Object's internal field values (Object[])
    parameters,           // Values passed to method (Object[])
    instance              // The receiver object of the intercepted call (may be null)
)
```

The AspectJ implementation takes the equivalent information from the `JoinPoint` object instead of separate parameters: `checkCommandSystemInteraction(action, thisJoinPoint)`.

**How is the action determined?**

The action type is **hardcoded** based on which methods are intercepted:

**Byte Buddy (Instrumentation Mode)** - Separate advice classes:
- `JavaInstrumentationExecuteCommandConstructorAdvice` → Uses `"execute"`
- `JavaInstrumentationExecuteCommandMethodAdvice` → Uses `"execute"`

**AspectJ Mode** - `before()` advice in one aspect:
- `before(): commandExecuteMethods()` → Uses `"execute"`

**Possible action values:**
- `"execute"` - Executing external commands or programs

---

# 5. Ares Validates the Command Execution

The security validator performs a **series of checks** to decide whether the command operation should be allowed or blocked.

**The 5 Checks in Order** (stops at first "Allow" or "Block"):

1. **Is Security Enabled?** → If no: 🟢
2. **Does the Call Come from Student Code?** → If no: 🟢  
3. **Are All Commands in Parameters Allowed?** → If no: 🔴
4. **Are All Commands in Object State Allowed?** → If no: 🔴
5. **Block and Throw Error** → If violation found

> 💡 **Re-entrancy guard:** Before any check runs, both backends call `enterAdvice()`. The advice body itself performs file work and stack walks that can lazily load JDK classes, which would re-enter the advice on the same thread and cause `ClassCircularityError` or unbounded recursion. Nested invocations on the same thread (trusted Ares internals) are therefore skipped; only the outermost invocation is enforced, and `exitAdvice()` clears the per-thread flag in a `finally` block.

---

## 5.1 Check 1: Is Security Enabled?

**1. Purpose**

Verify that command system security monitoring is turned on. This check ensures that Ares only performs security validations when explicitly enabled.

Without this check, the security system would either always run (causing unnecessary overhead) or never run (leaving the system unprotected). The configuration-based approach allows instructors to enable or disable security monitoring as needed.

**2. How it works**

Each implementation checks only its own mode value:

**Byte Buddy (Instrumentation) Mode:**
```java
String aopMode = getValueFromSettings("aopMode");
if (aopMode == null || aopMode.isEmpty() || !aopMode.equals("INSTRUMENTATION")) {
    return;  // Security is disabled, allow everything
}
String restrictedPackage = getValueFromSettings("restrictedPackage");
if (restrictedPackage == null || restrictedPackage.isEmpty()) {
    return;  // No restricted package configured, allow everything
}
```

**AspectJ Mode:**
```java
String aopMode = getValueFromSettings("aopMode");
if (aopMode == null || !aopMode.equals("ASPECTJ")) {
    return;  // Security is disabled, allow everything
}
```

- Byte Buddy accepts only `"INSTRUMENTATION"` (and additionally requires a non-empty `aopMode` and returns early - allowing the operation - when `restrictedPackage` is null or empty)
- AspectJ accepts only `"ASPECTJ"` (a null `restrictedPackage` leads to the same allow result in the subsequent call-stack check)

**3. Used variables**

- **`aopMode`** (String): Configuration setting that determines whether security monitoring is active. Must be set to `"INSTRUMENTATION"` (Byte Buddy) or `"ASPECTJ"` (AspectJ) to enable command system security checks. Retrieved from the configuration settings.

**4. Result**

- Security enabled → 🌕 **Continue to Check 2**
- Security disabled → 🟢 **Allow operation** (no restrictions - analysis terminated)

---

## 5.2 Check 2: Does the Call Come from Student Code?

This check determines whether the command operation was triggered by restricted student code or by trusted framework code. It consists of three sub-steps:

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

Walk through the call history to find if restricted student code triggered the command operation. This is like following breadcrumbs backwards to see how we got here.

This is the core security check that distinguishes between legitimate framework operations and potentially malicious student code (e.g., trying to execute system commands).

> 💡 **Analogy:** Like a detective following footprints backwards:
> - **Crime Scene:** `Runtime.getRuntime().exec("rm -rf /")` [we are here now]
> - **Step Back:** `StudentCode.deleteEverything()` [AHA! Student code found! 🔴]
> - **Further Back:** `TestClass.testStudent()` [this is the test]
> - **Origin:** JUnit Framework [trustworthy ✓]
>
> **Result:** Student code attempted to execute a forbidden command!

**Visual Example - Walking the Call History:**
```
[Top]    Runtime.exec("rm -rf /")             ← Current method being called
[...]    StudentCode.deleteEverything()        ← Found student code! ✓
[...]    StudentCode.exploit()                 ← Still in student package
[...]    TestClass.testStudent()               ← Test method (outside student package)
[Bottom] JUnit framework
         
Result: Student code detected at StudentCode.deleteEverything()
```

**2. How it works**

```java
String violatingMethod = checkIfCallstackCriteriaIsViolated(
    restrictedPackage, allowedClasses, declaringTypeName, methodName);
if (violatingMethod == null) {
    return;  // Not from student code
}
```

**Detailed steps:**

1. **Walk the Call History Once:** Instead of `Thread.currentThread().getStackTrace()` (which allocates a `Throwable` and materialises the full stack), Ares uses a cached `StackWalker` and performs a **single combined, lazy walk** (`inspectCallstackOnce`) that stops as soon as it has found both the violation and the caller above the first restricted frame:
   ```java
   String[] inspection = inspectCallstackOnce(restrictedPackage, allowedClasses);
   ```

2. **Skip Ares Internal Code, Class Loading and Reflection Trampolines:** For each walked frame, ignorable frames are skipped by prefix:
   ```java
   boolean ignorable = false;
   for (String ignore : IGNORE_CALLSTACK) {
       if (className.startsWith(ignore)) {
           ignorable = true;
           break;
       }
   }
   if (ignorable) {
       continue;  // Skip this frame, it's part of Ares or Java internals
   }
   ```

3. **Check if This is Student Code:**
   ```java
   boolean inRestricted = className.startsWith(restrictedPackage);
   ```

4. **Check if it's an Allowed Helper Class:** There is no separate helper method; the check is an inline **prefix** loop, so an entry in `allowedClasses` allows the class itself and everything whose fully qualified name starts with that entry:
   ```java
   boolean allowed = false;
   if (allowedClasses != null) {
       for (String allowedClass : allowedClasses) {
           if (className.startsWith(allowedClass)) {
               allowed = true;
               break;
           }
       }
   }
   if (!allowed) {
       violation = className + "." + frame.getMethodName();
   }
   ```

5. **Cache the Result Per Thread:** The same walk also records the first non-ignored caller **above** the first restricted frame. Both results are stored in a per-thread cache (`CALLSTACK_INSPECTION_CACHE`) so the immediately following `findFirstMethodOutsideOfRestrictedPackage` call (5.2.3) can consume it without walking the stack a second time.

6. **If No Student Code Found:**
   ```java
   return null;  // No restricted student code in call chain
   ```

**3. Used variables**

- **`restrictedPackage`** (String): From 5.2.1 - defines student code boundary
- **`allowedClasses`** (String[]): From 5.2.1 - list of trusted helper class name **prefixes** (matched via `className.startsWith(...)`)
- **`violatingMethod`** (String): Returns the fully qualified method name of the student code that triggered the command operation, or `null` if no student code found
- **`STACK_WALKER`** (StackWalker): Cached `StackWalker` instance used for the single lazy walk over the call chain
- **`IGNORE_CALLSTACK`** (List<String>): Identical in both backends: `["java.lang.ClassLoader", "de.tum.cit.ase.ares.api.", "com.intellij.rt.debugger.", "jdk.internal.loader.", "jdk.internal.reflect."]`
- **`className`** (String): The fully qualified class name for each frame in the call stack
- **`CALLSTACK_INSPECTION_CACHE`** (ThreadLocal<String[]>): Per-thread one-shot cache of the walk result, consumed by `findFirstMethodOutsideOfRestrictedPackage`

**4. Result**

- Found student code calling the command operation → Returns method name like `"de.student.StudentCode.exploit"` → 🌕 **Continue to 5.2.3**
- No student code found in call chain → Returns `null` → 🟢 **Allow operation** (called from test framework or trusted code - analysis terminated)

### 5.2.3 Find Which Test Called the Student Code

**1. Purpose**

Identify which test method triggered the student code. This helps instructors know which test case revealed the security violation.

**2. How it works**

```java
String testMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
```

This looks for the first non-ignored method **above** the first restricted frame in the call history - this is the test method that called the student code. In the common case no second stack walk is needed: the result was already computed during the combined walk in 5.2.2 and is consumed from the per-thread cache (`CALLSTACK_INSPECTION_CACHE`); only when the cache is empty does the method perform its own `StackWalker` walk.

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

## 5.3 Check 3: Extract and Validate Commands from Parameters

This check finds all commands in method parameters and validates them against the allowed commands list. It consists of three sub-steps:

### 5.3.1 Load List of Allowed Commands

**1. Purpose**

Load the configuration that specifies which commands are allowed to be executed and with which arguments. This allows instructors to grant fine-grained permissions - for example, students might be allowed to execute `ls` with `--help` but not `rm` with any arguments.

**2. How it works**

```java
String[] commandsAllowedToBeExecuted = getValueFromSettings("commandsAllowedToBeExecuted");
String[][] argumentsAllowedToBePassed = getValueFromSettings("argumentsAllowedToBePassed");
String[] pathsAllowedToBeExecuted = getValueFromSettings("pathsAllowedToBeExecuted");

// Validate configuration consistency (null arrays count as size 0);
// the error message is the localised key "security.advice.command.allowed.size"
int commandsSize = commandsAllowedToBeExecuted == null ? 0 : commandsAllowedToBeExecuted.length;
int argumentsSize = argumentsAllowedToBePassed == null ? 0 : argumentsAllowedToBePassed.length;
if (commandsSize != argumentsSize) {
    throw new SecurityException(localize("security.advice.command.allowed.size",
            argumentsSize, commandsSize));
}
```

**3. Used variables**

- **`commandsAllowedToBeExecuted`** (String[]): Array of commands that students are allowed to execute (e.g., `["ls", "echo"]`)
- **`argumentsAllowedToBePassed`** (String[][]): 2D array where each entry contains the allowed arguments for the corresponding command (e.g., `[["--help"], ["-n", "Hello"]]`)
- **`pathsAllowedToBeExecuted`** (String[]): Optional allow-list of executable **file paths**; used by the additional executable-path validation layer (see 5.3.4)

**4. Result**

Allowed commands list loaded → 🌕 **Continue to 5.3.2**

### 5.3.2 Apply Special Rules for Specific Methods

**1. Purpose**

Apply method-specific rules to determine which parameters should be checked. Some methods have complex signatures where not all parameters represent commands. These special rules prevent false positives while maintaining security.

**2. How it works**

```java
IgnoreValues ignoreRule = COMMAND_SYSTEM_IGNORE_PARAMETERS_EXCEPT.getOrDefault(
    declaringTypeName + "." + methodName,
    IgnoreValues.NONE
);
Object[] filteredVariables = filterVariables(parameters, ignoreRule);
```

**Special Cases:**

| Method | What We Check | Why |
|--------|---------------|-----|
| `Runtime.exec(String[], ...)` | Only parameter at position 0 | First parameter is the command array, rest are environment/directory |

**3. Used variables**

- **`COMMAND_SYSTEM_IGNORE_PARAMETERS_EXCEPT`** (Map): Maps method names to which parameter indices should be checked
- **`ignoreRule`** (IgnoreValues): The filtering rule for the current method
- **`filteredVariables`** (Object[]): Subset of parameters to validate

**4. Result**

Filtered variables ready for command validation → 🌕 **Continue to 5.3.3**

### 5.3.3 Check Method Parameters for Commands

**1. Purpose**

Extract and validate all commands from method parameters. This step systematically extracts commands from all parameter types and validates each against the allowed commands list.

**2. How it works**

```java
// 1. Filter parameters based on special rules
Object[] filteredVariables = filterVariables(parameters, ignoreRule);

// 2. Extract command from each parameter and check if it is forbidden
for (Object observedVariable : filteredVariables) {
    if (analyseViolation(observedVariable, commandsAllowedToBeExecuted, argumentsAllowedToBePassed)) {
        // Command is NOT allowed → Record violation (e.g. "rm -rf /")
        return extractViolationPath(observedVariable, commandsAllowedToBeExecuted, argumentsAllowedToBePassed);
    }
}
```

**Command Conversion Logic:**

The `variableToCommand()` method converts different command representations into a `CommandTarget` record (a pair of command name + argument array):

```java
private static CommandTarget variableToCommand(Object variableValue) {
    String[] parts = null;
    if (variableValue == null) {
        return null;
    } else if (variableValue instanceof String[] && ((String[]) variableValue).length != 0) {
        parts = (String[]) variableValue;  // Already an array (empty arrays are rejected)
    } else if (variableValue instanceof List<?>) {
        // List is an interface; fail closed on non-JDK List subtypes before
        // calling any overridable method on them (see note below)
        requireTrustedRuntimeType(variableValue);
        if (((List<?>) variableValue).stream().allMatch(o -> o instanceof String)) {
            parts = ((List<String>) variableValue).toArray(new String[0]);  // All elements must be Strings
        }
    } else if (variableValue instanceof String) {
        parts = parseCommandString((String) variableValue);  // Quote-aware shell-like parsing, NOT split-by-space
    }
    if (parts == null || parts.length == 0) {
        return null;
    }
    String command = parts[0];
    String[] arguments = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
    return new CommandTarget(command, arguments);
}
```

Key points:
- Strings are parsed with `parseCommandString()`, a **quote-aware** parser that handles single quotes, double quotes and backslash escapes like a shell. A naive split-by-space would let quoted arguments manipulate argument boundaries.
- **`requireTrustedRuntimeType()` fails closed** for `List` values whose runtime class was not loaded by the bootstrap or platform class loader: Ares would otherwise have to invoke overridable methods (`stream()`, `toArray()`) on an attacker-supplied subclass while the re-entrancy guard is active, so untrusted `List` subtypes are blocked with a SecurityException instead of being inspected.
- Lists containing non-String elements and empty `String[]` arrays yield no command (`null`), as does an empty parse result.

**Command Validation Logic:**

```java
private static boolean checkIfCommandIsForbidden(
    CommandTarget actual,
    String[] allowedCommands,
    String[][] allowedArguments
) {
    if (actual == null) {
        return false;
    }
    // Null or empty allow-lists mean nothing is allowed → forbidden
    if (allowedCommands == null || allowedCommands.length == 0 || allowedArguments == null
            || allowedArguments.length == 0) {
        return true;
    }
    // Iterate ALL entries: the same command name may appear multiple times,
    // each with a different allowed argument list, and any matching entry allows
    for (int i = 0; i < allowedCommands.length; i++) {
        if (commandMatches(actual.command(), allowedCommands[i])
                && argumentsMatch(allowedArguments[i], actual.arguments())) {
            return false;  // Explicitly allowed
        }
    }
    return true;  // No allow entry matches
}
```

Argument matching (`argumentsMatch`) requires the same number of arguments and, per argument, either an **exact** match or a **relative-vs-absolute path tolerance**: a non-empty allowed token also matches an actual argument that ends with it on a path-separator boundary (e.g. allowed `src/main/file.sh` matches actual `/home/user/project/src/main/file.sh`). Substring ("contains") matching is deliberately not used, since it would let a student append arbitrary content to an allowed argument.

**3. Used variables**

- **`parameters`** (Object[]): From section 4.3 - all method parameters
- **`ignoreRule`** (IgnoreValues): From 5.3.2 - determines which parameters to check
- **`filteredVariables`** (Object[]): From 5.3.2 - subset of parameters to validate
- **`commandsAllowedToBeExecuted`** (String[]): From 5.3.1 - list of allowed command names
- **`argumentsAllowedToBePassed`** (String[][]): From 5.3.1 - allowed arguments for each command
- **`CommandTarget`** (record): Extracted command as a pair of command name and argument array
- **`variableToCommand()`** (method): Helper that converts various types (String, String[], List<String>) to a `CommandTarget`

**4. Result**

- All commands allowed → 🌕 **Continue to 5.3.4**
- Forbidden command found → Record violation → 🔴 **Block and throw security exception**

### 5.3.4 Check Executable File Paths (pathsAllowedToBeExecuted)

**1. Purpose**

Optionally restrict not only **which command names** may be executed, but also **which executable files on disk** they may resolve to. This closes the gap where an allowed command name (e.g. `git`) could be shadowed by a malicious executable of the same name. Both backends run this layer (`checkIfExecutablePathCriteriaIsViolated`) directly after each command check - once on the parameters and once on the object attributes.

**2. How it works**

```java
String pathViolation = checkIfExecutablePathCriteriaIsViolated(
    filteredVariables, pathsAllowedToBeExecuted, ignoreRule);
if (pathViolation != null) {
    throw new SecurityException(localize("security.advice.illegal.file.execution", ...));
}
```

- **Opt-in narrowing:** If `pathsAllowedToBeExecuted` is null or empty, this layer allows everything - the command-name allow-list alone governs. It never turns into a deny-all.
- **Executable resolution (`resolveExecutable`):** A command token containing a path separator is resolved literally. A **bare command name** (e.g. `git`) is looked up on the `PATH` environment variable, mirroring the operating system's search (on Windows additionally trying the `PATHEXT` extensions) and skipping non-executable matches.
- **Fail-closed for unresolvable commands:** If an execute-path list IS configured but the command resolves to no existing executable file (including a bare name not found on `PATH`), it cannot be proven to be on the list and is **denied** rather than silently passed.
- **Path matching:** The resolved executable path is allowed if it equals an allowed path or lies below an allowed directory (paths are normalised and symlinks resolved for existing files).
- **Precise error message:** Reaching this check means the command-name check already passed, so the violation is reported with the dedicated key `security.advice.illegal.file.execution` and the denial reason "The command is allow-listed, but its executable file path is not in the execute allow-list."

**3. Used variables**

- **`pathsAllowedToBeExecuted`** (String[]): From 5.3.1 - allow-list of executable file paths or directories (setting defined in `JavaAOPTestCaseSettings`)
- **`resolveExecutable()`** (method): Resolves a command token to an existing executable file, searching `PATH`/`PATHEXT` for bare names

**4. Result**

- No execute-path list configured, or all executables allowed → 🌕 **Continue to Check 4**
- Executable file path not in the allow-list (or unresolvable) → 🔴 **Block and throw security exception**

---

## 5.4 Check 4: Extract and Validate Commands from Object State

**1. Purpose**

Extract and validate all commands from the object's internal state. This is critical for methods like `ProcessBuilder.start()` where the command is stored in the object rather than passed as a parameter.

**2. How it works**

**Same process as checking parameters (Section 5.3.3 above)**, but we examine the object's internal field values (from Section 4.2) instead of method parameters. The command extraction and validation logic is identical.

**Special Cases:**

| Method | What We Check | Why |
|--------|---------------|-----|
| `ProcessBuilder.start()` | Only the `command` field | Its index is resolved dynamically via `findFieldIndex(ProcessBuilder.class, "command")` because field indices shift across JDK versions (e.g. JDK 21 added a `LOGGER` field to `ProcessBuilder`) |

After the command check, the executable-path layer from 5.3.4 also runs on the same attributes. In AspectJ mode there is one additional fail-closed rule: if the `command` field of `ProcessBuilder` could not be read during attribute extraction (see 4.2), the `start()` call is denied with the command reported as `<unknown>`, because an unreadable command cannot be validated against the allow-list.

**3. Used variables**

- **`attributes`** (Object[]): From section 4.2 - values of the object's fields
- **`COMMAND_SYSTEM_IGNORE_ATTRIBUTES_EXCEPT`** (Map): Maps method names to which attribute indices should be checked
- **`commandsAllowedToBeExecuted`** (String[]): From 5.3.1 - list of allowed command names
- **`argumentsAllowedToBePassed`** (String[][]): From 5.3.1 - allowed arguments for each command

**4. Result**

- All commands allowed → 🟢 **Allow the command operation** (analysis terminated - no forbidden command detected)
- Forbidden command found → Record violation → 🔴 **Block and throw security exception**

---

## 5.5 Check 5: Block Access with Detailed Error Message

🔴 **Security Exception Thrown - Analysis Terminated**

**1. Purpose**

Block the forbidden command operation and provide a comprehensive error message. When a security violation is detected, it's crucial to give instructors and students clear information about what went wrong, where it happened, and which test triggered it. A generic "access denied" message would be unhelpful for debugging. The detailed message helps instructors identify the exact violation and helps students understand which part of their code caused the security issue.

**2. How it works**

```java
throw new SecurityException(localize(
    "security.advice.illegal.command.execution",
    violatingMethod,           // de.student.StudentCode.exploit
    action,                    // "execute"
    violatingCommand,          // "rm -rf /"
    fullMethodSignature        // "java.lang.Runtime.exec(java.lang.String)"
        + " (called by " + studentCalledMethod + ")"  // " (called by org.junit.TestClass.testStudent)"
        + " | " + buildDenialReason(noAllowRuleConfigured)  // " | Reason: No configured allow rule permits this access."
));
```

The trailing `buildDenialReason(...)` suffix distinguishes between "no allow rule configured at all" and "an allow rule exists but does not permit this access". Executable-path violations (5.3.4) use the separate key `security.advice.illegal.file.execution` with their own denial reason instead.

**3. Used variables**

- **`violatingMethod`** (String): From 5.2.2 - the student method that attempted the command execution (e.g., `"de.student.StudentCode.exploit"`)
- **`action`** (String): From section 4.4 - the type of operation attempted (always `"execute"` for command system)
- **`violatingCommand`** (String): From 5.3.3/5.4 - the forbidden command that was attempted (e.g., `"rm -rf /"`)
- **`fullMethodSignature`** (String): From section 4.1 - complete method signature showing exactly which method was called (e.g., `"java.lang.Runtime.exec(java.lang.String)"`)
- **`studentCalledMethod`** (String): From 5.2.3 - the test method that invoked the student code (e.g., `"org.junit.TestClass.testStudent"`)
- **`noAllowRuleConfigured`** (boolean): Whether the command allow-lists are null or empty; determines which denial reason `buildDenialReason()` appends
- **`localize()`** (method): Translates the error message to the configured language

**4. Result**

**Example Error Message** (format defined by the localised key `security.advice.illegal.command.execution`: `"Ares Security Error (Reason: Student-Code; Stage: Execution): %s tried to illegally %s Command %s via %s but was blocked by Ares."`):
```
Ares Security Error (Reason: Student-Code; Stage: Execution): de.student.StudentCode.exploit tried to illegally execute Command rm -rf / via java.lang.Runtime.exec(java.lang.String) (called by org.junit.TestClass.testStudent) | Reason: No configured allow rule permits this access. but was blocked by Ares.
```

🔴 **SecurityException thrown** - Analysis terminated, command operation blocked

---

# 6. Conclusion

## Summary for Programming Instructors (TL;DR)

**What does Ares do?**
- ✅ Monitors **3 command execution methods** automatically (`Runtime.exec()`, `ProcessBuilder` constructor and `start()`)
- ✅ Blocks **student code** from executing **forbidden commands**
- ✅ **Configurable via YAML** - You determine which commands and arguments are allowed
- ✅ Works **without code changes** to student code (via AOP)
- ✅ Provides **clear error messages** with exact source (which method, which command, which test)

**When do you need this?**
- When students should NOT be able to execute system commands
- When you want to allow only specific commands for exercises (e.g., `ls --help`)
- To prevent students from escaping the sandbox or accessing the underlying system

**How does it work (simplified)?**
1. Student calls `Runtime.getRuntime().exec("rm -rf /")`
2. Ares intercepts the call (AOP) and checks:
   - Does this come from student code? ✓ Yes
   - Is `rm` with arguments `-rf /` in the allowed list? ✗ No
3. Ares blocks and throws a meaningful exception

---

## Technical Details

The command system security mechanism provides **comprehensive protection** through:

1. **API Coverage**: 3 intercepted methods covering all standard command execution paths
2. **Call Stack Analysis**: Distinguishes trusted framework code from untrusted student code
3. **Command-Based Validation**: Strict enforcement of allowed commands with argument matching
4. **Executable-Path Validation**: Optional additional allow-list of executable file paths (`pathsAllowedToBeExecuted`, see 5.3.4) with PATH resolution and fail-closed handling of unresolvable commands
5. **Detailed Error Messages**: Precise violation reporting with full call context
6. **Flexible Configuration**: YAML-based security policies with command and argument whitelists

The system operates **transparently** using AOP techniques, requiring no modifications to student code, and enforces policies **before** dangerous operations execute.

> 💡 **Byte Buddy vs. AspectJ:** Validation flow is aligned, and the ignored callstack prefixes are identical in both modes (see Check 2 for the exact list). Both backends also share the re-entrancy guard (`enterAdvice()`/`exitAdvice()`), the fail-closed `requireTrustedRuntimeType()` blocking of non-JDK `List` subtypes, and the executable-path validation layer (5.3.4); AspectJ additionally fails closed with an `<unknown>` denial when the `ProcessBuilder` command field is unreadable.

**Implementation Differences:**

| Aspect | Byte Buddy (Instrumentation) | AspectJ |
|--------|------------------------------|----------|
| **Weaving Time** | Runtime (when classes are loaded) | Compile-time or load-time |
| **Configuration** | `aopMode = "INSTRUMENTATION"` | `aopMode = "ASPECTJ"` |
| **Advice Structure** | Separate classes for constructor and method advice | Single aspect with `before()` advice |
| **Method Info Access** | `@Advice.Origin` annotations | `JoinPoint.getSignature()` |
| **Instance Access** | `@Advice.This` annotation | `JoinPoint.getTarget()` |
| **Parameters Access** | `@Advice.AllArguments` annotation | `JoinPoint.getArgs()` |
| **Validation Logic** | Delegates to `JavaInstrumentationAdviceCommandSystemToolbox` | Implements in `JavaAspectJCommandSystemAdviceDefinitions` |

**Both implementations provide the same validation logic for command extraction and permission checking, including identical ignore-callstack prefixes.**
