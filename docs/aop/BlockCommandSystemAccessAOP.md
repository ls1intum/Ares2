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

---

## 1.3 Summary: When Is Command Execution Blocked?

Access is **BLOCKED** 🔴 when **ALL** conditions are true:

1. **Security Enabled**: `aopMode == "INSTRUMENTATION"` or `aopMode == "ASPECTJ"`
2. **Student Code Detected**: Call chain contains code in `restrictedPackage` and not in `allowedListedClasses`
3. **Command Not Allowed**: Command doesn't match any entry in `commandsAllowedToBeExecuted` with matching arguments from `argumentsAllowedToBePassed`

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

Both implementations set up "checkpoints" that activate **before** the command operation actually happens, giving Ares a chance to verify whether the operation should be allowed or blocked. The validation logic is aligned, and the command pointcuts are kept in sync between AspectJ and instrumentation.

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
| **methodSignature** | `String` | **Method signature** with parameter types. Example: `"(Ljava/lang/String;)Ljava/lang/Process;"` for exec taking a String. **Reading signatures:** `(Ljava/lang/String;)Ljava/lang/Process;` means "takes a String parameter, returns a Process". |

> 💡 **Method Signature Explained:** `(Ljava/lang/String;)Ljava/lang/Process;`
> - `(` = Parameter list begins
> - `Ljava/lang/String;` = Parameter of type String
> - `)` = Parameter list ends  
> - `Ljava/lang/Process;` = Returns a Process object
>
> **More Examples:**
> - `([Ljava/lang/String;)Ljava/lang/Process;` = String array parameter, returns Process
> - `()V` = no parameters, void return

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
    String methodSignature = thisJoinPoint.getSignature().toLongString();
    // Information is now available for validation
}
```

**3. How All Method Information Is Used:**
- Identify which command operation was attempted
- Look up special handling rules for specific methods
- Distinguish between different versions of the same method (overloading)
- Example: `java.lang.Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;` → Identifies a `Runtime.exec` method taking a String parameter

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
    } catch (InaccessibleObjectException e) {
        throw new SecurityException("Cannot access field: " + fields[i].getName(), e);
    } catch (IllegalAccessException e) {
        throw new SecurityException("Illegal access to field: " + fields[i].getName(), e);
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
> ├─ methodSignature: "(Ljava/lang/String;)Ljava/lang/Process;"
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
    parameters            // Values passed to method (Object[])
)
```

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

---

## 5.1 Check 1: Is Security Enabled?

**1. Purpose**

Verify that command system security monitoring is turned on. This check ensures that Ares only performs security validations when explicitly enabled. Without this check, the security system would either always run (causing unnecessary overhead) or never run (leaving the system unprotected). The configuration-based approach allows instructors to enable or disable security monitoring as needed.

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

Walk through the call history to find if restricted student code triggered the command operation. This is like following breadcrumbs backwards to see how we got here. This is the core security check that distinguishes between legitimate framework operations and potentially malicious student code (e.g., trying to execute system commands).

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
- **`violatingMethod`** (String): Returns the fully qualified method name of the student code that triggered the command operation, or `null` if no student code found
- **`stackTrace`** (StackTraceElement[]): The complete call chain showing all method calls leading to this point
- **`IGNORE_CALLSTACK`** (String[]): Instrumentation uses `["java.lang.ClassLoader", "de.tum.cit.ase.ares.api."]`; AspectJ uses `["java.lang.ClassLoader", "de.tum.cit.ase.ares.api.", "de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension", "de.tum.cit.ase.ares.api.jqwik.JqwikSecurityExtension", "de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions"]`
- **`className`** (String): The fully qualified class name for each method in the call stack
- **`isInAllowedList()`** (method): Helper function that returns `true` if the class is listed in the `allowedClasses` array

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

## 5.3 Check 3: Extract and Validate Commands from Parameters

This check finds all commands in method parameters and validates them against the allowed commands list. It consists of three sub-steps:

### 5.3.1 Load List of Allowed Commands

**1. Purpose**

Load the configuration that specifies which commands are allowed to be executed and with which arguments. This allows instructors to grant fine-grained permissions - for example, students might be allowed to execute `ls` with `--help` but not `rm` with any arguments.

**2. How it works**

```java
String[] commandsAllowedToBeExecuted = getValueFromSettings("commandsAllowedToBeExecuted");
String[][] argumentsAllowedToBePassed = getValueFromSettings("argumentsAllowedToBePassed");

// Validate configuration consistency
if (commandsAllowedToBeExecuted.length != argumentsAllowedToBePassed.length) {
    throw new SecurityException("Configuration error: commands and arguments arrays must have same length");
}
```

**3. Used variables**

- **`commandsAllowedToBeExecuted`** (String[]): Array of commands that students are allowed to execute (e.g., `["ls", "echo"]`)
- **`argumentsAllowedToBePassed`** (String[][]): 2D array where each entry contains the allowed arguments for the corresponding command (e.g., `[["--help"], ["-n", "Hello"]]`)

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

// 2. Extract command from each parameter
for (Object variable : filteredVariables) {
    String[] command = variableToCommand(variable);
    
    // 3. Check if command is forbidden
    if (checkIfCommandIsForbidden(command, commandsAllowedToBeExecuted, argumentsAllowedToBePassed)) {
        // Command is NOT allowed → Record violation
        return String.join(" ", command);
    }
}
```

**Command Conversion Logic:**

The `variableToCommand()` method handles different command formats:

```java
private static String[] variableToCommand(Object variableValue) {
    if (variableValue == null) {
        return null;
    } else if (variableValue instanceof String[]) {
        return (String[]) variableValue;  // Already an array
    } else if (variableValue instanceof List<?>) {
        return ((List<String>) variableValue).toArray(new String[0]);  // Convert List to array
    } else if (variableValue instanceof String) {
        return ((String) variableValue).split(" ");  // Split string by spaces
    } else {
        return null;
    }
}
```

**Command Validation Logic:**

```java
private static boolean checkIfCommandIsForbidden(
    String[] actualFullCommand,
    String[] allowedCommands,
    String[][] allowedArguments
) {
    if (actualFullCommand == null) return false;
    if (allowedCommands == null || allowedCommands.length == 0) return true;
    
    String actualCommand = actualFullCommand[0];  // Command name
    String[] actualArguments = Arrays.copyOfRange(actualFullCommand, 1, actualFullCommand.length);  // Arguments
    
    for (int i = 0; i < allowedCommands.length; i++) {
        if (allowedCommands[i].equals(actualCommand)) {
            // Command found, check if arguments match
            return !Arrays.deepEquals(allowedArguments[i], actualArguments);
        }
    }
    return true;  // Command not in allowed list
}
```

**3. Used variables**

- **`parameters`** (Object[]): From section 4.3 - all method parameters
- **`ignoreRule`** (IgnoreValues): From 5.3.2 - determines which parameters to check
- **`filteredVariables`** (Object[]): From 5.3.2 - subset of parameters to validate
- **`commandsAllowedToBeExecuted`** (String[]): From 5.3.1 - list of allowed command names
- **`argumentsAllowedToBePassed`** (String[][]): From 5.3.1 - allowed arguments for each command
- **`command`** (String[]): Extracted command as array (command name + arguments)
- **`variableToCommand()`** (method): Helper that converts various types (String, String[], List<String>) to command arrays

**4. Result**

- All commands allowed → 🌕 **Continue to Check 4**
- Forbidden command found → Record violation → 🔴 **Block and throw security exception**

---

## 5.4 Check 4: Extract and Validate Commands from Object State

**1. Purpose**

Extract and validate all commands from the object's internal state. This is critical for methods like `ProcessBuilder.start()` where the command is stored in the object rather than passed as a parameter.

**2. How it works**

**Same process as checking parameters (Section 5.3.3 above)**, but we examine the object's internal field values (from Section 4.2) instead of method parameters. The command extraction and validation logic is identical.

**Special Cases:**

| Method | What We Check | Why |
|--------|---------------|-----|
| `ProcessBuilder.start()` | Only attribute at position 1 | The `command` field is at index 1 in the object's fields |

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
    fullMethodSignature +      // "java.lang.Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;"
    " (called by " + studentCalledMethod + ")"  // " (called by org.junit.TestClass.testStudent)"
));
```

**3. Used variables**

- **`violatingMethod`** (String): From 5.2.2 - the student method that attempted the command execution (e.g., `"de.student.StudentCode.exploit"`)
- **`action`** (String): From section 4.4 - the type of operation attempted (always `"execute"` for command system)
- **`violatingCommand`** (String): From 5.3.3/5.4 - the forbidden command that was attempted (e.g., `"rm -rf /"`)
- **`fullMethodSignature`** (String): From section 4.1 - complete method signature showing exactly which method was called (e.g., `"java.lang.Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;"`)
- **`studentCalledMethod`** (String): From 5.2.3 - the test method that invoked the student code (e.g., `"org.junit.TestClass.testStudent"`)
- **`localize()`** (method): Translates the error message to the configured language

**4. Result**

**Example Error Message:**
```
SecurityException: Unauthorized command execution detected
Student method: de.student.StudentCode.exploit
Operation: execute
Forbidden command: rm -rf /
Attempted via: java.lang.Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;
Test method: org.junit.TestClass.testStudent
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
4. **Detailed Error Messages**: Precise violation reporting with full call context
5. **Flexible Configuration**: YAML-based security policies with command and argument whitelists

The system operates **transparently** using AOP techniques, requiring no modifications to student code, and enforces policies **before** dangerous operations execute.

> 💡 **Byte Buddy vs. AspectJ:** Validation flow is aligned, but the ignored callstack prefixes differ between modes (see Check 2 for the exact lists).

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

**Both implementations provide the same validation logic for command extraction and permission checking; ignore-callstack prefixes differ by mode.**
