# Thread System Security Mechanism

## Table of Contents

1. [High-Level Overview](#1-high-level-overview)
   - [1.1 Complete Validation Flow Diagram](#11-complete-validation-flow-diagram)
   - [1.2 Configuration Settings](#12-configuration-settings)
   - [1.3 Summary: When Is Thread Creation Blocked?](#13-summary-when-is-thread-creation-blocked)
   - [1.4 What Code Is Trusted vs. Restricted?](#14-what-code-is-trusted-vs-restricted)
2. [Ares Monitors Thread System Methods](#2-ares-monitors-thread-system-methods)
   - [2.1 THREAD SYSTEM - CREATE Operations (With Parameters)](#21-thread-system---create-operations-with-parameters)
   - [2.2 THREAD SYSTEM - CREATE Operations (Without Parameters)](#22-thread-system---create-operations-without-parameters)
3. [Student Code Triggers Security Check](#3-student-code-triggers-security-check)
4. [Ares Collects Information About the Thread Creation](#4-ares-collects-information-about-the-thread-creation)
   - [4.1 Which Method Was Called?](#41-which-method-was-called)
   - [4.2 What's the Current State of the Object?](#42-whats-the-current-state-of-the-object)
   - [4.3 What Parameters Were Passed?](#43-what-parameters-were-passed)
   - [4.4 Information Passed to Security Validator](#44-information-passed-to-security-validator)
5. [Ares Validates the Thread Creation](#5-ares-validates-the-thread-creation)
   - [5.1 Check 1: Is Security Enabled?](#51-check-1-is-security-enabled)
   - [5.2 Check 2: Does the Call Come from Student Code?](#52-check-2-does-the-call-come-from-student-code)
     - [5.2.1 Load Configuration](#521-load-configuration)
     - [5.2.2 Analyze the Call Chain](#522-analyze-the-call-chain)
     - [5.2.3 Find Which Test Called the Student Code](#523-find-which-test-called-the-student-code)
   - [5.3 Check 3: Extract and Validate Thread Classes from Parameters](#53-check-3-extract-and-validate-thread-classes-from-parameters)
     - [5.3.1 Load List of Allowed Thread Classes](#531-load-list-of-allowed-thread-classes)
     - [5.3.2 Convert Variable to Thread Class Name](#532-convert-variable-to-thread-class-name)
     - [5.3.3 Check if Thread Class is Forbidden](#533-check-if-thread-class-is-forbidden)
   - [5.4 Check 4: Extract and Validate Thread Classes from Object State](#54-check-4-extract-and-validate-thread-classes-from-object-state)
   - [5.5 Check 5: Block Access with Detailed Error Message](#55-check-5-block-access-with-detailed-error-message)
6. [Conclusion](#6-conclusion)

---

# 1. High-Level Overview

This document describes how Ares 2 prevents unauthorized thread creation in student code.

---

## 1.1 Complete Validation Flow Diagram

**Legend throughout the document:**
- **🟢 Green** = Access allowed (no security violation)
- **🔴 Red** = Access blocked (security violation detected)
- **🌕 Yellow** = Continue to next check

The validation flow is similar to the file system security mechanism but adapted for thread creation:

1. Student code calls a thread creation method (e.g., `new Thread().start()`)
2. Ares intercepts the call before execution
3. Security checks determine if the thread class is allowed and quota is available
4. If allowed, execution proceeds; if blocked, SecurityException is thrown

---

## 1.2 Configuration Settings

Security policies are configured through settings that instructors can adjust:

| Setting | Type | Description | Example |
|---------|------|-------------|---------|
| **aopMode** | `String` | AOP implementation | `"INSTRUMENTATION"` (Byte Buddy) or `"ASPECTJ"` |
| **restrictedPackage** | `String` | The package where student code is located | `"de.student."` |
| **allowedListedClasses** | `String[]` | Trusted helper classes students can use | `["de.student.util.Helper"]` |
| **threadClassAllowedToBeCreated** | `String[]` | Thread classes students are allowed to create | `["java.lang.Thread", "Lambda-Expression", "*"]` |
| **threadNumberAllowedToBeCreated** | `int[]` | Number of threads allowed per class | `[5, 10, 3]` |

**Special Values for `threadClassAllowedToBeCreated`:**
- `"Lambda-Expression"` - Matches lambda expressions passed to thread executors
- `"*"` - Wildcard that matches any thread class (fallback)

---

## 1.3 Summary: When Is Thread Creation Blocked?

Access is **BLOCKED** 🔴 when **ALL** conditions are true:

1. **Security Enabled**: `aopMode == "INSTRUMENTATION"` or `aopMode == "ASPECTJ"`
2. **Student Code Detected**: Call chain contains code in `restrictedPackage` and not in `allowedListedClasses`
3. **Thread Class Not Allowed**: Thread class doesn't match any entry in `threadClassAllowedToBeCreated`
4. **OR Thread Quota Exhausted**: The allowed count for that thread class has reached zero

**If ANY condition fails → Access is ALLOWED** 🟢

**Legend:**
- 🔴 Security exception thrown → Analysis and method execution terminated (forbidden thread creation detected)
- 🌕 Condition passed → Continue to next validation step
- 🟢 Access allowed → Analysis terminated and method execution continues (thread quota decremented)

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

# 2. Ares Monitors Thread System Methods

**What is AOP?** AOP (Aspect-Oriented Programming) is a technique that automatically runs security checks before certain methods execute, without modifying the student code. Think of it like a security guard checking IDs before people enter a building - the building code doesn't change, but everyone gets checked automatically when interacting with the building.

**Concrete Example:**

**Without AOP:** You would have to manually write security checks before every thread creation (if that is even possible).
```java
public void createThread(Runnable task) {
    if (!isAllowed(task)) throw new SecurityException(); // Security check happens manually and has to be stated explicitly!
    new Thread(task).start();  // Actual code
}
```

**With AOP:** Ares automatically inserts this check before EVERY `Thread.start()`, so no code changes are required.
```java
public void createThread(Runnable task) {
    new Thread(task).start();  // Actual code, Security check happens automatically in the background!
}
```

Ares automatically monitors thread system operations by intercepting specific Java methods using one of two AOP implementations:

- **Byte Buddy (Instrumentation Mode)**: Automatically adds security checks when Java loads classes (called bytecode manipulation).
- **AspectJ (AspectJ Mode)**: Automatically adds security checks in a second compilation step (called weaving).

Both implementations set up "checkpoints" that activate **before** the thread operation actually happens, giving Ares a chance to verify whether the operation should be allowed or blocked. The validation logic is aligned, and the thread pointcuts are kept in sync between AspectJ and instrumentation.

---

## 2.1 THREAD SYSTEM - CREATE Operations (With Parameters)

**Security Component:** Thread creation monitor (methods that receive task/runnable as parameter)

**Monitored Methods (34 total):**
- **java.lang.Thread**: `startVirtualThread()`
- **java.lang.Thread.Builder**: `start()`, `run()`
- **java.lang.Thread.Builder.OfPlatform**: `start()`
- **java.lang.ThreadGroup**: `newThread()`
- **java.util.concurrent.Executor**: `execute()`
- **java.util.concurrent.ExecutorService**: `submit()`, `invokeAll()`, `invokeAny()`, `execute()`
- **java.util.concurrent.AbstractExecutorService**: `submit()`, `invokeAll()`, `invokeAny()`
- **java.util.concurrent.ThreadPoolExecutor**: `execute()`, `submit()`
- **java.util.concurrent.ScheduledExecutorService**: `schedule()`, `scheduleAtFixedRate()`, `scheduleWithFixedDelay()`
- **java.util.concurrent.ScheduledThreadPoolExecutor**: `schedule()`, `scheduleAtFixedRate()`, `scheduleWithFixedDelay()`
- **java.util.concurrent.ForkJoinPool**: `execute()`, `submit()`
- **java.util.concurrent.CompletableFuture**: `runAsync()`, `supplyAsync()`, `thenApplyAsync()`, `thenCombineAsync()`, `thenCombine()`
- **java.util.concurrent.ThreadFactory**: `newThread()`
- **java.util.concurrent.Executors$DelegatedExecutorService**: `submit()`, `invokeAll()`, `invokeAny()`
- **java.util.concurrent.Executors$DefaultThreadFactory**: `newThread()`
- **java.util.concurrent.ExecutorCompletionService**: `submit()`

---

## 2.2 THREAD SYSTEM - CREATE Operations (Without Parameters)

**Security Component:** Thread creation monitor (methods that start threads without task parameter)

**Monitored Methods (4 total):**
- **java.lang.Thread**: `start()`
- **java.util.Collection**: `parallelStream()`
- **java.util.stream.Stream**: `parallel()`
- **java.util.stream.BaseStream**: `parallel()`

---

**Total Monitored Methods: 38**

**Note:** This count is based on the method lists shown above. The actual number may vary slightly as some methods may support multiple overloaded variants. Both implementations monitor the same set of methods.

---

# 3. Student Code Triggers Security Check

When student code (any code within the configured restricted package) calls one of these monitored methods, Ares automatically performs a security check **before** the thread operation executes.

**Example:**
```java
// Student Code
package de.student.solution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentSolution {
    public void createThreads() {
        // This call triggers JavaInstrumentationCreateThreadMethodAdvice
        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> System.out.println("Hello"));  // Lambda-Expression
    }
}
```

When the `executor.submit(() -> ...)` method is called, Ares intercepts the call:
- **Byte Buddy**: Automatically runs a security check before the method executes (technical implementation: `JavaInstrumentationCreateThreadMethodAdvice.onEnter()`)
- **AspectJ**: Automatically runs a security check before the method executes (technical implementation: `before()` advice in `JavaAspectJThreadSystemAdviceDefinitions.aj`)

Ares then checks whether the student is allowed to create a thread with a `Lambda-Expression` **before** the thread is actually created.

---

# 4. Ares Collects Information About the Thread Creation

The security monitor collects information about what's happening: Which method is being called, what task/runnable is being submitted, and where in the student code this is happening.

**Collection Mechanisms:**

- **Byte Buddy**: Uses special Java annotations (`@Advice`) to automatically capture information about the intercepted method (technical implementation: `JavaInstrumentationCreateThreadMethodAdvice.onEnter()`)
- **AspectJ**: Receives method information automatically through a parameter object called `JoinPoint` (technical implementation: `checkThreadSystemInteraction()` method)

Both approaches collect the same information:

**What is collected:**
- **Method information**: Which method was called
- **Object state**: Internal state of the object (including the declaring type for thread class detection)
- **Parameters**: Values passed to the method (Runnable, Callable, etc.)

**Why do we need all three types of information?**

Thread tasks can appear in **different places** depending on how the method is used:

1. **Method information is needed** to identify which operation is attempted and apply special handling rules
   - Example: `Thread.start()` needs to check the thread object itself, not parameters

2. **Object state is needed** because tasks can be stored inside objects
   - Example: `thread.start()` - The Runnable is in `thread.target` field, not passed as parameter

3. **Parameters are needed** because tasks are often passed as method arguments
   - Example: `executor.submit(() -> doWork())` - The lambda is a parameter

**What is NOT collected here:** Whether the access is allowed or blocked - that determination happens in the **next step** (Section 5: Ares Validates the Thread Creation)

---

## 4.1 Which Method Was Called?

**1. What Information Do We Collect:**

| Information | Type | Description |
|-------------|------|-------------|
| **declaringTypeName** | `String` | **Class name** where the method is defined. Example: `"java.util.concurrent.ExecutorService"`. |
| **methodName** | `String` | **Method name**. Example: `"submit"` or `"<init>"` for constructors. |
| **methodSignature** | `String` | **Method signature** with parameter types. Example: `"(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;"` for submit taking a Runnable. |

> 💡 **Method Signature Explained:** `(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;`
> - `(` = Parameter list begins
> - `Ljava/lang/Runnable;` = Parameter of type Runnable
> - `)` = Parameter list ends  
> - `Ljava/util/concurrent/Future;` = Returns a Future object
>
> **More Examples:**
> - `(Ljava/util/concurrent/Callable;)Ljava/util/concurrent/Future;` = Callable parameter, returns Future
> - `()V` = no parameters, void return (e.g., `Thread.start()`)

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
public void checkThreadSystemInteraction(
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
- Identify which thread operation was attempted
- Look up special handling rules for specific methods
- Distinguish between different versions of the same method (overloading)
- Example: `java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;` → Identifies an `ExecutorService.submit` method taking a Runnable parameter

---

## 4.2 What's the Current State of the Object?

**1. What Information Do We Collect:**

| Information | Type | Description |
|-------------|------|-------------|
| **instance** | `Object` | **The object** on which the method is called (the `this` reference). `null` for constructors since the object doesn't exist yet. |
| **attributes** | `Object[]` | **Array of the object's internal field values**. The actual values stored in each field. **Note:** For thread validation, also includes `declaringTypeName` for class-based checks. |

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
public void checkThreadSystemInteraction(
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

**Special Handling for Thread System:**

For thread system validation, the `declaringTypeName` is also included in the attributes to check:
```java
// Create combined array with declaringTypeName and attributes
Object[] attributesToCheck = attributes == null ? new Object[]{declaringTypeName} :
    Stream.concat(
        Stream.of(declaringTypeName),
        Arrays.stream(attributes)
    ).toArray();
```

**3. How All Object State Information Is Used:**
- Extract thread tasks that might be stored inside the object
- Check object fields for security violations
- Access internal state even if not passed as parameters
- Check the declaring type name for class-based thread restrictions
- Example: `Thread` object with `target` field = `MyRunnable` → Task extracted from `attributes` array → Checked against allowed thread classes

---

## 4.3 What Parameters Were Passed?

**1. What Information Do We Collect:**

| Information | Type | Description |
|------------|------|-------------|
| **parameters** | `Object[]` | **Method arguments** - the values passed to the method when it was called (Runnable, Callable, Supplier, etc.). |

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
public void checkThreadSystemInteraction(
    String action,
    JoinPoint thisJoinPoint
) {
    Object[] parameters = thisJoinPoint.getArgs();  // Get method arguments
}
```

**3. How All Parameter Information Is Used:**
- Extract thread task classes from method arguments
- Identify lambda expressions vs. concrete class implementations
- Check extracted classes against allowed thread classes list
- Example: `executor.submit(() -> doWork())` → parameters = `[Lambda-Expression]` → Checked against allowed thread classes

---

## 4.4 Information Passed to Security Validator

After collecting this information, Ares passes it to the security validation component.

> 💡 **Concrete Example:** `executor.submit(() -> System.out.println("Hello"))`
> ```
> Collected Information:
> ├─ action: "create"
> ├─ declaringTypeName: "java.util.concurrent.ExecutorService"
> ├─ methodName: "submit"
> ├─ methodSignature: "(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;"
> ├─ parameters: [Lambda-Expression instance]
> ├─ attributes: [declaringTypeName, ...executor internal fields...]
> └─ instance: ExecutorService instance
> ```

**Where does the action type come from?**

The action type is **automatically determined** based on which method was intercepted. For thread system operations, the action is always `"create"`:

| Intercepted Method Example | Action Type |
|---------------------------|-------------|
| `Thread.start()`, `ExecutorService.submit()`, `CompletableFuture.runAsync()` | `"create"` |

**For Thread System Operations:**
```java
checkThreadSystemInteraction(
    "create",             // What type of operation? (always "create" for thread system)
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
- `JavaInstrumentationCreateThreadConstructorAdvice` → Uses `"create"`
- `JavaInstrumentationCreateThreadMethodAdvice` → Uses `"create"`

**AspectJ Mode** - `before()` advice in one aspect:
- `before(): threadCreateMethodsWithParameters() || threadCreateMethodsWithoutParameters()` → Uses `"create"`

**Possible action values:**
- `"create"` - Creating or starting threads

---

# 5. Ares Validates the Thread Creation

The security validator performs a **series of checks** to decide whether the thread operation should be allowed or blocked.

**The 5 Checks in Order** (stops at first "Allow" or "Block"):

1. **Is Security Enabled?** → If no: 🟢
2. **Does the Call Come from Student Code?** → If no: 🟢  
3. **Are All Thread Classes in Parameters Allowed?** → If no: 🔴
4. **Are All Thread Classes in Object State Allowed?** → If no: 🔴
5. **Block and Throw Error** → If violation found

---

## 5.1 Check 1: Is Security Enabled?

**1. Purpose**

Verify that thread system security monitoring is turned on. This check ensures that Ares only performs security validations when explicitly enabled. Without this check, the security system would either always run (causing unnecessary overhead) or never run (leaving the system unprotected). The configuration-based approach allows instructors to enable or disable security monitoring as needed.

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

- **`aopMode`** (String): Configuration setting that determines whether security monitoring is active. Must be set to `"INSTRUMENTATION"` (Byte Buddy) or `"ASPECTJ"` (AspectJ) to enable thread system security checks. Retrieved from the configuration settings.

**4. Result**

- Security enabled → 🌕 **Continue to Check 2**
- Security disabled → 🟢 **Allow operation** (no restrictions - analysis terminated)

---

## 5.2 Check 2: Does the Call Come from Student Code?

This check determines whether the thread operation was triggered by restricted student code or by trusted framework code. It consists of three sub-steps:

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

Walk through the call history to find if restricted student code triggered the thread operation. This is like following breadcrumbs backwards to see how we got here. This is the core security check that distinguishes between legitimate framework operations and potentially malicious student code (e.g., trying to create excessive threads for DoS).

> 💡 **Analogy:** Like a detective following footprints backwards:
> - **Crime Scene:** `executor.submit(() -> exploit())` [we are here now]
> - **Step Back:** `StudentCode.createManyThreads()` [AHA! Student code found! 🔴]
> - **Further Back:** `TestClass.testStudent()` [this is the test]
> - **Origin:** JUnit Framework [trustworthy ✓]
>
> **Result:** Student code attempted to create a forbidden thread!

**Visual Example - Walking the Call History:**
```
[Top]    ExecutorService.submit(Lambda)        ← Current method being called
[...]    StudentCode.createManyThreads()        ← Found student code! ✓
[...]    StudentCode.exploit()                  ← Still in student package
[...]    TestClass.testStudent()                ← Test method (outside student package)
[Bottom] JUnit framework
         
Result: Student code detected at StudentCode.createManyThreads()
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
- **`violatingMethod`** (String): Returns the fully qualified method name of the student code that triggered the thread operation, or `null` if no student code found
- **`stackTrace`** (StackTraceElement[]): The complete call chain showing all method calls leading to this point
- **`IGNORE_CALLSTACK`** (String[]): Instrumentation uses `["java.lang.ClassLoader", "de.tum.cit.ase.ares.api."]`; AspectJ uses `["java.lang.ClassLoader", "de.tum.cit.ase.ares.api.", "de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension", "de.tum.cit.ase.ares.api.jqwik.JqwikSecurityExtension", "de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions"]`
- **`className`** (String): The fully qualified class name for each method in the call stack
- **`isInAllowedList()`** (method): Helper function that returns `true` if the class is listed in the `allowedClasses` array

**4. Result**

- Found student code calling the thread operation → Returns method name like `"de.student.StudentCode.exploit"` → 🌕 **Continue to 5.2.3**
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

## 5.3 Check 3: Extract and Validate Thread Classes from Parameters

This check finds all thread task classes in method parameters and validates them against the allowed thread classes list. It consists of three sub-steps:

### 5.3.1 Load List of Allowed Thread Classes

**1. Purpose**

Load the configuration that specifies which thread classes are allowed to be created and how many instances of each. This allows instructors to grant fine-grained permissions - for example, students might be allowed to create 5 instances of `java.lang.Thread` but only 2 lambda expressions.

**2. How it works**

```java
String[] threadClassAllowedToBeCreated = getValueFromSettings("threadClassAllowedToBeCreated");
int[] threadNumberAllowedToBeCreated = getValueFromSettings("threadNumberAllowedToBeCreated");

// Validate configuration consistency
if (threadClassAllowedToBeCreated.length != threadNumberAllowedToBeCreated.length) {
    throw new SecurityException("Configuration error: thread classes and numbers arrays must have same length");
}
```

**3. Used variables**

- **`threadClassAllowedToBeCreated`** (String[]): Array of thread classes that students are allowed to create (e.g., `["java.lang.Thread", "Lambda-Expression", "*"]`)
- **`threadNumberAllowedToBeCreated`** (int[]): Array where each entry specifies the maximum number of threads allowed for the corresponding class (e.g., `[5, 10, 3]`)

**4. Result**

Allowed thread classes list loaded → 🌕 **Continue to 5.3.2**

### 5.3.2 Convert Variable to Thread Class Name

**1. Purpose**

Convert parameter values (Runnable, Callable, Supplier, etc.) to their class names for validation. This step handles special cases like lambda expressions and `Thread.FieldHolder` internal structures.

**2. How it works**

```java
private static String variableToClassname(Object variableValue) {
    if (variableValue == null) {
        return null;
    } else if (isThreadFieldHolder(variableValue)) {
        return getTaskFromThreadFieldHolder(variableValue);
    } else if (variableValue instanceof Runnable || 
               variableValue instanceof Callable<?> || 
               variableValue instanceof ForkJoinTask<?> ||
               variableValue instanceof CompletableFuture<?> ||
               variableValue instanceof Supplier<?> ||
               variableValue instanceof Function<?, ?> ||
               variableValue instanceof BiFunction<?, ?, ?> ||
               variableValue instanceof CompletionStage<?>) {
        Class<?> variableClass = variableValue.getClass();
        return isReallyLambda(variableClass) ? "Lambda-Expression" : variableClass.getName();
    } else {
        return null;
    }
}
```

**Lambda Detection Logic:**

```java
private static boolean isReallyLambda(Class<?> variableClass) {
    // Step 1: check if the class is synthetic
    if (!variableClass.isSynthetic()) {
        return false;
    }
    String className = variableClass.getName();

    // Check for common lambda patterns
    return className.contains("$$Lambda") ||
           className.contains("$Lambda$") ||
           className.matches(".*\\$\\$Lambda\\$.*");
}
```

**Thread.FieldHolder Handling:**

For internal Java thread structures, Ares uses `sun.misc.Unsafe` to access the task field:

```java
private static String getTaskFromThreadFieldHolder(Object threadFieldHolder) {
    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
    Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    Object unsafe = unsafeField.get(null);
    
    Method objectFieldOffsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
    Field taskField = threadFieldHolder.getClass().getDeclaredField("task");
    long offset = (Long) objectFieldOffsetMethod.invoke(unsafe, taskField);
    
    Method getObjectMethod = unsafeClass.getMethod("getObject", Object.class, long.class);
    Object taskValue = getObjectMethod.invoke(unsafe, threadFieldHolder, offset);
    
    return isReallyLambda(taskValue.getClass()) ? "Lambda-Expression" : taskValue.getClass().getName();
}
```

**3. Used variables**

- **`variableValue`** (Object): The parameter value to convert (Runnable, Callable, etc.)
- **`isReallyLambda()`** (method): Helper that detects lambda expressions by checking synthetic class patterns
- **`isThreadFieldHolder()`** (method): Helper that detects internal Thread.FieldHolder instances
- **`getTaskFromThreadFieldHolder()`** (method): Uses Unsafe to extract task from Thread.FieldHolder

**4. Result**

Class name extracted (e.g., `"Lambda-Expression"` or `"com.example.MyRunnable"`) → 🌕 **Continue to 5.3.3**

### 5.3.3 Check if Thread Class is Forbidden

**1. Purpose**

Check if the extracted thread class is allowed and if the quota is still available. This step implements the core security logic including wildcard matching and quota management.

**2. How it works**

```java
private static boolean checkIfThreadIsForbidden(
    String actualClassname,
    String[] allowedThreadClasses,
    int[] allowedThreadNumbers
) {
    if (actualClassname == null) return false;
    if (allowedThreadClasses == null || allowedThreadClasses.length == 0) return true;
    
    boolean actualIsLambda = "Lambda-Expression".equals(actualClassname);
    int starIndex = -1;  // Track wildcard position
    
    for (int i = 0; i < allowedThreadClasses.length; i++) {
        String allowedClassName = allowedThreadClasses[i];
        boolean allowedIsLambda = "Lambda-Expression".equals(allowedClassName);
        
        // Track wildcard for fallback
        if ("*".equals(allowedClassName)) {
            starIndex = i;
        }
        
        // Lambda matching
        if (allowedIsLambda && actualIsLambda) {
            return handleFoundClassIsForbidden(allowedThreadNumbers, i);
        }
        if (allowedIsLambda || actualIsLambda) {
            continue;  // Skip if only one is lambda
        }
        
        // Class hierarchy matching
        Class<?> allowedClass = Class.forName(allowedClassName);
        Class<?> actualClass = Class.forName(actualClassname);
        if (allowedClass.isAssignableFrom(actualClass)) {
            return handleFoundClassIsForbidden(allowedThreadNumbers, i);
        }
    }
    
    // Fallback to wildcard if present
    if (starIndex != -1) {
        return handleFoundClassIsForbidden(allowedThreadNumbers, starIndex);
    }
    
    return true;  // Class not in allowed list
}
```

**Quota Management:**

```java
private static boolean handleFoundClassIsForbidden(int[] allowedThreadNumbers, int index) {
    if (allowedThreadNumbers == null) {
        return true;
    }
    boolean threadDisallowed = allowedThreadNumbers[index] <= 0;
    if (!threadDisallowed) {
        decrementSettingsArrayValue("threadNumberAllowedToBeCreated", index);
    }
    return threadDisallowed;
}
```

**Matching Rules:**

| Scenario | Behavior |
|----------|----------|
| Exact class match | Check quota for that class |
| Lambda vs. `"Lambda-Expression"` | Check lambda quota |
| Class hierarchy (subclass) | Check superclass quota |
| `"*"` wildcard | Fallback if no specific match |
| No match found | Return forbidden |

**3. Used variables**

- **`actualClassname`** (String): The class name to check (from 5.3.2)
- **`allowedThreadClasses`** (String[]): From 5.3.1 - list of allowed class names
- **`allowedThreadNumbers`** (int[]): From 5.3.1 - quota for each class
- **`starIndex`** (int): Position of wildcard `"*"` for fallback matching
- **`handleFoundClassIsForbidden()`** (method): Checks and decrements quota
- **`decrementSettingsArrayValue()`** (method): Reduces the quota counter

**4. Result**

- Thread class allowed AND quota available → Quota decremented → 🟢 **Continue to Check 4**
- Thread class forbidden OR quota exhausted → Record violation → 🔴 **Block and throw security exception**

---

## 5.4 Check 4: Extract and Validate Thread Classes from Object State

**1. Purpose**

Extract and validate all thread classes from the object's internal state. This is critical for methods like `Thread.start()` where the task is stored in the Thread object rather than passed as a parameter.

**2. How it works**

**Same process as checking parameters (Section 5.3 above)**, but we examine the object's internal field values (from Section 4.2) instead of method parameters. The class extraction and validation logic is identical.

**Special Handling:**

For thread system validation, the `declaringTypeName` is prepended to the attributes array to also check the class being called on:

```java
Object[] attributesToCheck = attributes == null ? new Object[]{declaringTypeName} :
    Stream.concat(
        Stream.of(declaringTypeName),
        Arrays.stream(attributes)
    ).toArray();
```

This ensures that when checking `Thread.start()`, we also validate the Thread class itself.

**3. Used variables**

- **`attributes`** (Object[]): From section 4.2 - values of the object's fields
- **`declaringTypeName`** (String): The class name where the method is defined
- **`threadClassAllowedToBeCreated`** (String[]): From 5.3.1 - list of allowed class names
- **`threadNumberAllowedToBeCreated`** (int[]): From 5.3.1 - quota for each class

**4. Result**

- All thread classes allowed AND quotas available → 🟢 **Allow the thread operation** (analysis terminated - quotas decremented)
- Forbidden thread class OR quota exhausted → Record violation → 🔴 **Block and throw security exception**

---

## 5.5 Check 5: Block Access with Detailed Error Message

🔴 **Security Exception Thrown - Analysis Terminated**

**1. Purpose**

Block the forbidden thread operation and provide a comprehensive error message. When a security violation is detected, it's crucial to give instructors and students clear information about what went wrong, where it happened, and which test triggered it. A generic "access denied" message would be unhelpful for debugging. The detailed message helps instructors identify the exact violation and helps students understand which part of their code caused the security issue.

**2. How it works**

```java
throw new SecurityException(localize(
    "security.advice.illegal.thread.execution",
    violatingMethod,           // de.student.StudentCode.exploit
    action,                    // "create"
    violatingThreadClass,      // "Lambda-Expression" or "com.example.MaliciousRunnable"
    fullMethodSignature +      // "java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;"
    " (called by " + studentCalledMethod + ")"  // " (called by org.junit.TestClass.testStudent)"
));
```

**3. Used variables**

- **`violatingMethod`** (String): From 5.2.2 - the student method that attempted the thread creation (e.g., `"de.student.StudentCode.exploit"`)
- **`action`** (String): From section 4.4 - the type of operation attempted (always `"create"` for thread system)
- **`violatingThreadClass`** (String): From 5.3.3/5.4 - the forbidden thread class or the class that exceeded its quota (e.g., `"Lambda-Expression"`)
- **`fullMethodSignature`** (String): From section 4.1 - complete method signature showing exactly which method was called
- **`studentCalledMethod`** (String): From 5.2.3 - the test method that invoked the student code (e.g., `"org.junit.TestClass.testStudent"`)
- **`localize()`** (method): Translates the error message to the configured language

**4. Result**

**Example Error Messages:**

**Thread Class Not Allowed:**
```
SecurityException: Unauthorized thread creation detected
Student method: de.student.StudentCode.exploit
Operation: create
Forbidden thread class: com.example.MaliciousRunnable
Attempted via: java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;
Test method: org.junit.TestClass.testStudent
```

**Thread Quota Exhausted:**
```
SecurityException: Thread quota exceeded
Student method: de.student.StudentCode.createManyThreads
Operation: create
Thread class: Lambda-Expression (quota exhausted)
Attempted via: java.util.concurrent.ExecutorService.submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;
Test method: org.junit.TestClass.testStudent
```

🔴 **SecurityException thrown** - Analysis terminated, thread operation blocked

---

# 6. Conclusion

## Summary for Programming Instructors (TL;DR)

**What does Ares do?**
- ✅ Monitors **38 thread creation methods** automatically (Thread, ExecutorService, CompletableFuture, ForkJoinPool, etc.)
- ✅ Blocks **student code** from creating **forbidden thread classes**
- ✅ Enforces **thread quotas** per class (prevent DoS attacks)
- ✅ **Configurable via YAML** - You determine which classes and how many threads are allowed
- ✅ Works **without code changes** to student code (via AOP)
- ✅ Provides **clear error messages** with exact source (which method, which class, which test)

**When do you need this?**
- When students should NOT be able to create unlimited threads (prevent resource exhaustion)
- When you want to allow only specific thread patterns for exercises
- To prevent students from escaping the sandbox through parallel execution
- When teaching concurrency but wanting to limit complexity

**How does it work (simplified)?**
1. Student calls `executor.submit(() -> exploit())`
2. Ares intercepts the call (AOP) and checks:
   - Does this come from student code? ✓ Yes
   - Is `Lambda-Expression` in the allowed list? Depends on config
   - Is quota available for `Lambda-Expression`? Depends on count
3. Ares either allows (decrementing quota) or blocks with a meaningful exception

**Special Features:**
- **Lambda Detection**: Recognizes `() -> ...` patterns as `"Lambda-Expression"`
- **Class Hierarchy**: `MyThread extends Thread` matches `java.lang.Thread` allowance
- **Wildcard**: Use `"*"` to allow any class with a quota limit
- **Quota System**: Each allowed class has a counter that decrements on use

---

## Technical Details

The thread system security mechanism provides **comprehensive protection** through:

1. **Extensive API Coverage**: 38 intercepted methods covering all standard thread creation paths
2. **Call Stack Analysis**: Distinguishes trusted framework code from untrusted student code
3. **Class-Based Validation**: Strict enforcement of allowed thread classes with inheritance support
4. **Quota Management**: Runtime tracking and enforcement of thread creation limits
5. **Lambda Expression Detection**: Special handling for functional programming patterns
6. **Detailed Error Messages**: Precise violation reporting with full call context
7. **Flexible Configuration**: YAML-based security policies with class and quota whitelists

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
| **Validation Logic** | Delegates to `JavaInstrumentationAdviceThreadSystemToolbox` | Implements in `JavaAspectJThreadSystemAdviceDefinitions` |

**Both implementations provide the same validation logic for class extraction, quota management, and permission checking; ignore-callstack prefixes differ by mode.**

**Supported Task Types:**
- `Runnable`
- `Callable<T>`
- `ForkJoinTask<T>`
- `CompletableFuture<T>`
- `Supplier<T>`
- `Function<T, R>`
- `BiFunction<T, U, R>`
- `CompletionStage<T>`
- Lambda expressions (detected via synthetic class patterns)
