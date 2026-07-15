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

Both implementations set up "checkpoints" that activate **before** the thread operation actually happens, giving Ares a chance to verify whether the operation should be allowed or blocked. The validation logic is aligned, but the thread pointcuts are **not** identical between the two engines: AspectJ weaves a superset of the instrumentation pointcuts. It additionally intercepts `AbstractExecutorService.execute()`, `ThreadPoolExecutor.invokeAll()/invokeAny()`, `ScheduledExecutorService.submit()/invokeAll()/invokeAny()/execute()`, `ScheduledThreadPoolExecutor.submit()/execute()/invokeAll()/invokeAny()`, `ForkJoinPool.invokeAll()/invokeAny()`, and `SubmissionPublisher.submit()/offer()`, none of which appear in the instrumentation pointcut map (see Section 2 for the exact lists).

---

## 2.1 THREAD SYSTEM - CREATE Operations (With Parameters)

**Security Component:** Thread creation monitor (methods that receive task/runnable as parameter)

**Monitored Methods in Both Engines (33 total in the instrumentation map):**
- **java.lang.Thread**: `startVirtualThread()`
- **java.lang.Thread$Builder**: `start()` (a former `run()` entry matched no real method, since `Thread.Builder` declares no `run()`, and was dropped as a phantom)
- **java.lang.Thread$Builder$OfPlatform**: `start()`
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

**Additionally Woven by AspectJ Only (15 signatures, bringing AspectJ to 48 with-parameter signatures):**
- **java.util.concurrent.AbstractExecutorService**: `execute()`
- **java.util.concurrent.ThreadPoolExecutor**: `invokeAll()`, `invokeAny()`
- **java.util.concurrent.ScheduledExecutorService**: `submit()`, `invokeAll()`, `invokeAny()`, `execute()`
- **java.util.concurrent.ScheduledThreadPoolExecutor**: `submit()`, `execute()`, `invokeAll()`, `invokeAny()`
- **java.util.concurrent.ForkJoinPool**: `invokeAll()`, `invokeAny()`
- **java.util.concurrent.SubmissionPublisher**: `submit()`, `offer()`

These extra signatures exist only in the AspectJ pointcuts (`JavaAspectJThreadSystemPointcutDefinitions.aj`); the instrumentation pointcut map (`methodsWhichCanCreateThreads` in `JavaInstrumentationPointcutDefinitions.java`) does not contain them.

---

## 2.2 THREAD SYSTEM - CREATE Operations (Without Parameters)

**Security Component:** Thread creation monitor (methods that start threads without task parameter)

**Monitored Methods (4 total, identical in both engines):**
- **java.lang.Thread**: `start()`
- **java.util.Collection**: `parallelStream()`
- **java.util.stream.Stream**: `parallel()`
- **java.util.stream.BaseStream**: `parallel()`

---

**Total Monitored Methods: instrumentation 37 class-method pairs (33 with parameters + 4 without); AspectJ 52 woven signatures (48 with parameters + 4 without)**

**Note:** These counts refer to class-method pairs in the pointcut definitions; each pair may cover multiple overloaded variants at runtime. The two implementations do **not** monitor the same set of methods: AspectJ weaves a superset that additionally includes the executor overloads and `SubmissionPublisher` methods listed above.

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
| **methodSignature** | `String` | **Method signature** with parameter types in source form. Example: `"(java.lang.Runnable)"` for submit taking a Runnable. |

> 💡 **Method Signature Explained:** `(java.lang.Runnable)`
> - `(` = Parameter list begins
> - `java.lang.Runnable` = Parameter of type Runnable (fully qualified source-form name, not a JVM descriptor)
> - `)` = Parameter list ends
> - The return type is **not** part of the signature: Byte Buddy's `@Advice.Origin("#s")` yields only the parameter list
>
> **More Examples:**
> - `(java.util.concurrent.Callable)` = Callable parameter
> - `()` = no parameters (e.g., `Thread.start()`)

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
    // formatSignature() (JavaAspectJAbstractAdviceDefinitions) normalizes the
    // join-point signature to the same shape the instrumentation engine builds:
    // <declaringType>.<methodName>(<paramType1>,<paramType2>,...)
    String fullMethodSignature = formatSignature(thisJoinPoint.getSignature());
    // Information is now available for validation
}
```

**3. How All Method Information Is Used:**
- Identify which thread operation was attempted
- Look up special handling rules for specific methods
- Distinguish between different versions of the same method (overloading)
- Example: `java.util.concurrent.ExecutorService.submit(java.lang.Runnable)` → Identifies an `ExecutorService.submit` method taking a Runnable parameter

---

## 4.2 What's the Current State of the Object?

**1. What Information Do We Collect:**

| Information | Type | Description |
|-------------|------|-------------|
| **instance** | `Object` | **The object** on which the method is called (the `this` reference). `null` for constructors since the object doesn't exist yet. |
| **attributes** | `Object[]` | **Array of the object's internal field values**. The actual values stored in each field. Together with the parameters and the receiver instance, these are later resolved into one de-duplicated set of thread-class candidates (see Section 5.3/5.4). |

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

**Both modes then use the same best-effort attribute extraction (using Java's built-in ability to inspect object contents). A field that cannot be read is skipped rather than turned into a SecurityException, so a JDK-side reflection limit does not abort otherwise legal student code:**
```java
// For constructors, instance is null (object doesn't exist yet), so there are no fields to read
final Field[] fields = instance != null ? instance.getClass().getDeclaredFields() : new Field[0];
final Object[] attributes = new Object[fields.length];

if (instance != null) {
    for (int i = 0; i < fields.length; i++) {
        try {
            fields[i].setAccessible(true);  // Make private fields accessible
            attributes[i] = fields[i].get(instance);  // Read the value
        } catch (InaccessibleObjectException | IllegalAccessException | SecurityException
                | IllegalArgumentException | NullPointerException | ExceptionInInitializerError e) {
            // Skip the unreadable field: the check still runs over the parameters
            // and the readable fields. The instrumentation advice continues to the
            // next field; the AspectJ advice sets attributes[i] = null. Uniform
            // across all four subsystems and both engines.
            continue;
        }
    }
}
```

**How the Collected Values Feed the Thread Check:**

There is no special "prepend the declaring type name" step. Instead, the validator resolves every thread-class candidate from three sources (the method **parameters**, the **receiver instance**, and the extracted **attributes**) into a single de-duplicated `LinkedHashSet` via `collectThreadClassNames(...)`:
```java
Set<String> threadClassNames = new LinkedHashSet<>();
if (parameters != null) {
    for (Object parameter : parameters) {
        collectThreadClassNames(parameter, threadClassNames);
    }
}
collectThreadClassNames(instance, threadClassNames);
if (attributes != null) {
    for (Object attribute : attributes) {
        collectThreadClassNames(attribute, threadClassNames);
    }
}
```

When the receiver is a `Thread`, a dedicated branch in `variableToClassname(...)` resolves the thread's carried task class (or, if the thread has no task because `run()` is overridden, the thread's own class). The de-duplication ensures the thread quota is consumed at most once per distinct class per interception.

**3. How All Object State Information Is Used:**
- Extract thread tasks that might be stored inside the object
- Check object fields for security violations
- Access internal state even if not passed as parameters
- Example: `Thread` object with a stored `MyRunnable` task → Task class resolved from the receiver/attributes → Checked against allowed thread classes

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
> ├─ methodSignature: "(java.lang.Runnable)"
> ├─ parameters: [Lambda-Expression instance]
> ├─ attributes: [...executor internal field values...]
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
    parameters,           // Values passed to method (Object[])
    instance              // The receiver object of the intercepted call (may be null)
)
```

**Note:** The constructor advice (`JavaInstrumentationCreateThreadConstructorAdvice`) passes `null` as the `instance`, since the object does not exist yet during `<init>`.

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

**The Actual Validation Flow** (stops at first "Allow" or "Block"):

1. **Re-entrancy Guard**: `enterAdvice()`/`exitAdvice()`: nested advice invocations on the same thread (triggered by the advice's own class loading and stack walking) return immediately, so only the outermost interception is enforced
2. **Is Security Enabled?** → If no: 🟢 (in instrumentation mode, a missing/empty `restrictedPackage` also allows immediately)
3. **Configuration Consistency**: If `threadClassAllowedToBeCreated` and `threadNumberAllowedToBeCreated` have different lengths → 🔴 SecurityException (this runs **before** the callstack check)
4. **Internal Bypasses**: Thread creations owned by `ProcessBuilder`/`Runtime.exec` process I/O (`isThreadCreationFromCommandExecution()`) or by Ares's own `@StrictTimeout` machinery (`isThreadCreationFromAresTimeout()`) → 🟢
5. **Does the Call Come from Student Code?** → If no: 🟢
6. **Are All Thread Classes Allowed?** → Parameters, receiver instance, and object state are resolved into **one de-duplicated set** and checked in a single pass, so the quota is consumed at most once per distinct class per interception → If a class is forbidden or its quota is exhausted: 🔴
7. **Block and Throw Error** → If violation found

**Additional safeguards inside step 6:**
- **Fail-closed sentinel**: If a thread-task carrier exists but its task class cannot be read, the sentinel `<unresolved-thread-class>` is used; it matches no allow-list entry, so the creation is denied instead of silently allowed
- **Trusted-type check (instrumentation only)**: Before iterating a `List` parameter, `requireTrustedRuntimeType()` verifies it is a JDK type, so the advice never invokes overridable methods on attacker-supplied subclasses while the re-entrancy guard is active
- **Implicit-operation sentinel (AspectJ only)**: `parallelStream()`, `parallel()`, and `SubmissionPublisher.submit()/offer()` carry no thread-task class; when invoked directly by student code they are represented by a per-operation sentinel token (e.g. `<implicit-thread-op:parallelStream>`) which the `"*"` wildcard **deliberately never matches**; only an exact sentinel entry in the allow-list can permit them

The subsections below describe the main checks in detail: 5.1 covers step 2, 5.2 covers step 5, 5.3 and 5.4 cover step 6, and 5.5 covers step 7.

---

## 5.1 Check 1: Is Security Enabled?

**1. Purpose**

Verify that thread system security monitoring is turned on. This check ensures that Ares only performs security validations when explicitly enabled. Without this check, the security system would either always run (causing unnecessary overhead) or never run (leaving the system unprotected). The configuration-based approach allows instructors to enable or disable security monitoring as needed.

**2. How it works**

Each engine checks only its **own** mode value:

**Byte Buddy (Instrumentation) Mode:**
```java
String aopMode = getValueFromSettings("aopMode");
if (aopMode == null || aopMode.isEmpty() || !aopMode.equals("INSTRUMENTATION")) {
    return;  // Security is disabled, allow everything
}
// Instrumentation additionally requires a configured restricted package:
String restrictedPackage = getValueFromSettings("restrictedPackage");
if (restrictedPackage == null || restrictedPackage.isEmpty()) {
    return;  // No student package configured, allow everything
}
```

**AspectJ Mode:**
```java
String aopMode = getValueFromSettings("aopMode");
if (aopMode == null || !aopMode.equals("ASPECTJ")) {
    return;  // Security is disabled, allow everything
}
```

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
String violatingMethod = checkIfCallstackCriteriaIsViolated(
    restrictedPackage, allowedClasses, declaringTypeName, methodName);
if (violatingMethod == null) {
    return;  // Not from student code
}
```

**Detailed steps:**

1. **Walk the Call History Lazily:** A cached `StackWalker` (shared constant `STACK_WALKER`) streams the stack frames instead of materialising a full `StackTraceElement[]` array. A **single walk** (`inspectCallstackOnce`) finds both the violating frame **and** the first non-ignored caller above the first restricted frame, then caches the pair in the thread-local `CALLSTACK_INSPECTION_CACHE` so the immediately following `findFirstMethodOutsideOfRestrictedPackage` call (5.2.3) reads from the cache instead of walking the stack a second time.

2. **Skip Ares Internal Code and Java Internals:**
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

4. **Check if it's an Allowed Helper Class** (an inline prefix match, not a separate helper method):
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

5. **Stop Early:** The walk stops as soon as both the violation and the caller above the first restricted frame are known. If no restricted frame is found, `null` is returned (no restricted student code in call chain).

**3. Used variables**

- **`restrictedPackage`** (String): From 5.2.1 - defines student code boundary
- **`allowedClasses`** (String[]): From 5.2.1 - list of trusted helper classes
- **`violatingMethod`** (String): Returns the fully qualified method name of the student code that triggered the thread operation, or `null` if no student code found
- **`STACK_WALKER`** (StackWalker): Cached walker shared by all call-stack inspectors; streams frames lazily instead of materialising the whole stack
- **`CALLSTACK_INSPECTION_CACHE`** (ThreadLocal<String[]>): Per-thread one-shot cache holding `{violationFqn, callerAboveFqn}` from the single walk, consumed by 5.2.3
- **`IGNORE_CALLSTACK`** (List<String>): The **identical** five-entry list in both engines: `["java.lang.ClassLoader", "de.tum.cit.ase.ares.api.", "com.intellij.rt.debugger.", "jdk.internal.loader.", "jdk.internal.reflect."]`
- **`className`** (String): The fully qualified class name for each frame in the call stack; a class is treated as allowed when it starts with any prefix in `allowedClasses` (inline `startsWith` prefix match)

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

Find the first method **outside** the student package - this is the test method that called the student code. In practice this reads the caller cached by the single stack walk in 5.2.2 (`CALLSTACK_INSPECTION_CACHE`); a second walk only happens if the cache is empty.

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
    try {
        if (variableValue == null) {
            return null;
        } else if (isThreadFieldHolder(variableValue)) {
            return getTaskFromThreadFieldHolder(variableValue);
        } else if (variableValue instanceof Thread thread) {
            // Must precede the Runnable branch because Thread implements Runnable.
            // Resolve the thread's effective class from the receiver itself: when the
            // thread carries a Runnable task, that task's class identifies it;
            // otherwise (an overridden run()) the thread's own class does.
            String taskClassName = threadTaskClassName(thread);
            return taskClassName != null ? taskClassName : thread.getClass().getName();
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
    } catch (SecurityException ignored) {
        // A thread-task carrier whose task class could not be read must not pass
        // silently; return a sentinel that matches no allow-list entry so the
        // creation is denied (fail closed).
        return UNRESOLVED_THREAD_CLASS;
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

For internal Java thread structures, Ares first tries **standard reflection** and only falls back to `sun.misc.Unsafe` when strong encapsulation blocks reflective access. A genuinely `null` task field (a Thread subclass with an overridden `run()` and no Runnable) returns `null` instead of being treated as a read failure:

```java
private static String getTaskFromThreadFieldHolder(Object threadFieldHolder) {
    Class<?> fieldHolderClass = threadFieldHolder.getClass();

    // First, try standard reflection (preferred approach)
    try {
        Field taskField = fieldHolderClass.getDeclaredField("task");
        taskField.setAccessible(true);
        Object taskValue = taskField.get(threadFieldHolder);
        if (taskValue == null) {
            // Genuinely no Runnable task: not a read failure. The caller falls
            // back to the thread's own class instead of denying outright.
            return null;
        }
        Class<?> taskClass = taskValue.getClass();
        return isReallyLambda(taskClass) ? "Lambda-Expression" : taskClass.getName();
    } catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
        // Standard reflection failed; try the Unsafe fallback below.
    }

    // Fallback to sun.misc.Unsafe for runtimes with strong encapsulation
    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
    Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    Object unsafe = unsafeField.get(null);
    Method objectFieldOffsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
    Field taskField = fieldHolderClass.getDeclaredField("task");
    long offset = (Long) objectFieldOffsetMethod.invoke(unsafe, taskField);
    Method getObjectMethod = unsafeClass.getMethod("getObject", Object.class, long.class);
    Object taskValue = getObjectMethod.invoke(unsafe, threadFieldHolder, offset);
    if (taskValue == null) {
        return null;
    }
    return isReallyLambda(taskValue.getClass()) ? "Lambda-Expression" : taskValue.getClass().getName();
}
```

If both reflection and Unsafe fail, a SecurityException escapes to `variableToClassname(...)`, which converts it into the fail-closed sentinel `<unresolved-thread-class>`. Companion helpers exist for `Thread` receivers: `threadTaskClassName(...)`/`readThreadTask(...)` read the task via the modern `holder.task` layout first and the legacy `target` field as a fallback, each via `readField(...)` (reflection first, Unsafe second).

**3. Used variables**

- **`variableValue`** (Object): The parameter value to convert (Runnable, Callable, etc.)
- **`isReallyLambda()`** (method): Helper that detects lambda expressions by checking synthetic class patterns
- **`isThreadFieldHolder()`** (method): Helper that detects internal Thread.FieldHolder instances
- **`getTaskFromThreadFieldHolder()`** (method): Extracts the task from Thread.FieldHolder via standard reflection, with Unsafe only as fallback
- **`threadTaskClassName()`/`readThreadTask()`/`readField()`** (methods): Resolve the task carried by a `Thread` receiver (modern `holder.task` layout first, legacy `target` field as fallback)
- **`UNRESOLVED_THREAD_CLASS`** (String): Fail-closed sentinel `"<unresolved-thread-class>"` used when a task carrier exists but its class cannot be read

**4. Result**

Class name extracted (e.g., `"Lambda-Expression"` or `"com.example.MyRunnable"`) → 🌕 **Continue to 5.3.3**

### 5.3.3 Check if Thread Class is Forbidden

**1. Purpose**

Check if the extracted thread class is allowed and if the quota is still available. This step implements the core security logic including wildcard matching and quota management.

**2. How it works**

```java
private static boolean checkIfThreadIsForbidden(
    ThreadTarget target,               // record wrapping the class name, not a raw String
    String[] allowedThreadClasses,
    int[] allowedThreadNumbers
) {
    if (target == null || target.className() == null) {
        return false;
    }
    String actualClassname = target.className();
    // Forbidden when EITHER list is null or empty
    if (allowedThreadClasses == null || allowedThreadClasses.length == 0
            || allowedThreadNumbers == null || allowedThreadNumbers.length == 0) {
        return true;
    }

    int starIndex = -1;  // Track wildcard position
    for (int i = 0; i < allowedThreadClasses.length; i++) {
        String allowedClassName = allowedThreadClasses[i];
        // The "*" entry only records the fallback position and continues;
        // it is never passed to Class.forName
        if ("*".equals(allowedClassName)) {
            starIndex = i;
            continue;
        }
        if (threadClassMatches(actualClassname, allowedClassName)) {
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

**Class Matching (separate helper `threadClassMatches`):**

```java
private static boolean threadClassMatches(String actualClassname, String allowedClassName) {
    boolean actualIsLambda = "Lambda-Expression".equals(actualClassname);
    boolean allowedIsLambda = "Lambda-Expression".equals(allowedClassName);

    if (allowedIsLambda && actualIsLambda) {
        return true;
    }
    if (allowedIsLambda || actualIsLambda) {
        return false;  // Only one is a lambda: no match
    }

    try {
        // initialize=false: an assignability check must not run static initialisers
        // of student-controlled classes during a security decision
        Class<?> allowedClass = Class.forName(allowedClassName, false, ClassLoader.getSystemClassLoader());
        Class<?> actualClass = Class.forName(actualClassname, false, ClassLoader.getSystemClassLoader());
        return allowedClass.isAssignableFrom(actualClass);
    } catch (ClassNotFoundException e) {
        // e.g. JDK internal classes: fall back to exact string comparison
        return allowedClassName.equals(actualClassname);
    } catch (IllegalStateException | NullPointerException e) {
        // Fail secure: if the class cannot be verified, continue to the next check
        return false;
    }
}
```

**AspectJ addition:** In AspectJ mode, an implicit-operation sentinel (`<implicit-thread-op:...>`, see Section 5) is **excluded** from the `"*"` wildcard: the wildcard position is only recorded for non-sentinel class names, and a sentinel matches solely by exact string equality against an allow-list entry.

**Quota Management:**

```java
private static boolean handleFoundClassIsForbidden(int[] allowedThreadNumbers, int index) {
    if (allowedThreadNumbers == null) {
        return true;
    }
    // Atomic check-and-decrement: check and decrement happen as ONE operation
    // under the settings lock, preventing the check-then-act race where multiple
    // threads could pass the check simultaneously and exceed the configured limit
    boolean successfullyDecremented = checkAndDecrementSettingsArrayValue("threadNumberAllowedToBeCreated", index);
    return !successfullyDecremented;
}
```

`checkAndDecrementSettingsArrayValue(...)` (in `JavaInstrumentationAdviceAbstractToolbox`) synchronises on the canonical settings lock, re-reads the array, returns `false` if the value at the index is not positive, and otherwise writes back a decremented copy and returns `true`.

**Matching Rules:**

| Scenario | Behavior |
|----------|----------|
| Exact class match | Check quota for that class |
| Lambda vs. `"Lambda-Expression"` | Check lambda quota |
| Class hierarchy (subclass) | Check superclass quota |
| `"*"` wildcard | Fallback if no specific match |
| No match found | Return forbidden |

**3. Used variables**

- **`target`** (ThreadTarget): Record wrapping the class name to check (from 5.3.2); `target.className()` yields the actual class name
- **`allowedThreadClasses`** (String[]): From 5.3.1 - list of allowed class names
- **`allowedThreadNumbers`** (int[]): From 5.3.1 - quota for each class
- **`starIndex`** (int): Position of wildcard `"*"` for fallback matching
- **`handleFoundClassIsForbidden()`** (method): Checks and decrements quota
- **`checkAndDecrementSettingsArrayValue()`** (method): Atomically checks and reduces the quota counter under the settings lock

**4. Result**

- Thread class allowed AND quota available → Quota decremented → 🟢 **Continue to Check 4**
- Thread class forbidden OR quota exhausted → Record violation → 🔴 **Block and throw security exception**

---

## 5.4 Check 4: Extract and Validate Thread Classes from Object State

**1. Purpose**

Extract and validate all thread classes from the object's internal state. This is critical for methods like `Thread.start()` where the task is stored in the Thread object rather than passed as a parameter.

**2. How it works**

**Same process as checking parameters (Section 5.3 above)**, but we examine the receiver instance and the object's internal field values (from Section 4.2) instead of method parameters. The class extraction and validation logic is identical.

**Special Handling:**

There is no separate second validation pass and no prepending of `declaringTypeName`. Instead, all candidates (parameters, the receiver instance, and the attributes) are resolved into **one de-duplicated `LinkedHashSet`** via `collectThreadClassNames(...)`, and each distinct class is checked exactly once:

```java
Set<String> threadClassNames = new LinkedHashSet<>();
if (parameters != null) {
    for (Object parameter : parameters) {
        collectThreadClassNames(parameter, threadClassNames);
    }
}
collectThreadClassNames(instance, threadClassNames);
if (attributes != null) {
    for (Object attribute : attributes) {
        collectThreadClassNames(attribute, threadClassNames);
    }
}
```

For `Thread.start()`, the receiver `Thread` branch in `variableToClassname(...)` resolves the thread's carried task class (or the thread's own class when `run()` is overridden). The FieldHolder attribute resolves to the same task class and is de-duplicated, so the quota is consumed only once. `collectThreadClassNames(...)` recurses into arrays and Lists; in instrumentation mode, a `List` is only iterated after `requireTrustedRuntimeType(...)` confirms it is a JDK type.

**3. Used variables**

- **`attributes`** (Object[]): From section 4.2 - values of the object's fields
- **`instance`** (Object): The receiver object of the intercepted call (`null` for constructors)
- **`threadClassNames`** (LinkedHashSet<String>): De-duplicated set of all resolved thread-class candidates
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
    fullMethodSignature        // "java.util.concurrent.ExecutorService.submit(java.lang.Runnable)"
        + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
        + " | " + buildDenialReason(noAllowRuleConfigured)
));
```

The fourth argument is composed of the full method signature, the calling test method (guarded against `null`), and a denial reason appended after `" | "`. `buildDenialReason(...)` distinguishes "no allow rule configured at all" from "an allow rule exists but does not permit this access".

**3. Used variables**

- **`violatingMethod`** (String): From 5.2.2 - the student method that attempted the thread creation (e.g., `"de.student.StudentCode.exploit"`)
- **`action`** (String): From section 4.4 - the type of operation attempted (always `"create"` for thread system)
- **`violatingThreadClass`** (String): From 5.3.3/5.4 - the forbidden thread class or the class that exceeded its quota (e.g., `"Lambda-Expression"`)
- **`fullMethodSignature`** (String): From section 4.1 - complete method signature showing exactly which method was called
- **`studentCalledMethod`** (String): From 5.2.3 - the test method that invoked the student code (e.g., `"org.junit.TestClass.testStudent"`)
- **`noAllowRuleConfigured`** (boolean): `true` when no thread allow rule is configured at all; selects the denial reason text
- **`buildDenialReason()`** (method): Returns the appended reason string ("No allow rule configured for this resource type." or "No configured allow rule permits this access.")
- **`localize()`** (method): Translates the error message to the configured language

**4. Result**

**Example Error Messages:**

The message template is a **single line** (`messages.properties`, key `security.advice.illegal.thread.execution`): `Ares Security Error (Reason: Student-Code; Stage: Execution): %s tried to illegally %s Thread %s via %s but was blocked by Ares.` The same key is used for both denial causes; they are distinguished only by the appended denial reason.

**Thread Class Not Allowed:**
```
Ares Security Error (Reason: Student-Code; Stage: Execution): de.student.StudentCode.exploit tried to illegally create Thread com.example.MaliciousRunnable via java.util.concurrent.ExecutorService.submit(java.lang.Runnable) (called by org.junit.TestClass.testStudent) | Reason: No configured allow rule permits this access. but was blocked by Ares.
```

**Thread Quota Exhausted / No Allow Rule Configured:**
```
Ares Security Error (Reason: Student-Code; Stage: Execution): de.student.StudentCode.createManyThreads tried to illegally create Thread Lambda-Expression via java.util.concurrent.ExecutorService.submit(java.lang.Runnable) (called by org.junit.TestClass.testStudent) | Reason: No allow rule configured for this resource type. but was blocked by Ares.
```

🔴 **SecurityException thrown** - Analysis terminated, thread operation blocked

---

# 6. Conclusion

## Summary for Programming Instructors (TL;DR)

**What does Ares do?**
- ✅ Monitors **thread creation methods** automatically (Thread, ExecutorService, CompletableFuture, ForkJoinPool, etc.): 37 class-method pairs in instrumentation mode, 52 woven signatures in AspectJ mode
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

1. **Extensive API Coverage**: 37 intercepted class-method pairs (instrumentation) and 52 woven signatures (AspectJ) covering all standard thread creation paths
2. **Call Stack Analysis**: Distinguishes trusted framework code from untrusted student code
3. **Class-Based Validation**: Strict enforcement of allowed thread classes with inheritance support
4. **Quota Management**: Runtime tracking and enforcement of thread creation limits
5. **Lambda Expression Detection**: Special handling for functional programming patterns
6. **Detailed Error Messages**: Precise violation reporting with full call context
7. **Flexible Configuration**: YAML-based security policies with class and quota whitelists

The system operates **transparently** using AOP techniques, requiring no modifications to student code, and enforces policies **before** dangerous operations execute.

> 💡 **Byte Buddy vs. AspectJ:** Validation flow is aligned and both modes use the identical ignored-callstack prefix list (see Check 2). What differs is the pointcut coverage (AspectJ weaves a superset, see Section 2) and the AspectJ-only implicit-operation sentinel handling (see Section 5).

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

**Both implementations provide the same validation logic for class extraction, quota management, and permission checking, using the identical ignore-callstack prefix list; they differ in pointcut coverage and the AspectJ-only implicit-operation sentinels.**

**Supported Task Types (the `instanceof` chain in `variableToClassname`):**
- `Runnable`
- `Callable<T>`
- `ForkJoinTask<T>`
- `CompletableFuture<T>`
- `Supplier<T>`
- `Function<T, R>`
- `BiFunction<T, U, R>`
- `CompletionStage<T>`
- Lambda expressions (detected via synthetic class patterns)

Two carrier types are handled by **dedicated earlier branches** before this chain: `Thread` receivers (resolved to their carried task class, or the thread's own class when `run()` is overridden; this branch must precede the `Runnable` check because `Thread` implements `Runnable`) and `Thread$FieldHolder` carriers (resolved via `getTaskFromThreadFieldHolder`).
