# Conceptual weaknesses of the AspectJ engine compared to Instrumentation

Ares enforces a security policy on untrusted student code through two
interchangeable engines:

- **Instrumentation** (`api.aop.java.instrumentation`): a ByteBuddy Java agent
  that rewrites the bytecode of the JDK target classes themselves and injects
  advice into the intercepted method/constructor.
- **AspectJ** (`api.aop.java.aspectj`): aspects woven into the compiled
  student and project classes, with `before()` advice attached to `call(...)`
  pointcuts.

Both engines are intended to mirror the same policy decisions, but they do so
through separate AspectJ and Byte Buddy implementations that read the same
`JavaAOPTestCaseSettings` fields. For operations covered by both interception
lists, their verdicts agree only while that duplicated logic is kept in sync.
The differences below are not bugs that can be fixed
in code; they are structural consequences of how each engine attaches to the
program. They exist so that policy authors and reviewers understand that
**AspectJ mode is strictly weaker than Instrumentation mode**, and so that
ASPECTJ-mode policies are written with these limits in mind.

## 1. AspectJ uses caller-side weaving (`call()`), not target rewriting

All Ares AspectJ pointcuts are `call(...)` pointcuts. A `call()` join point is
woven into the **call site**, i.e. into the code that makes the call, not into
the method being called. AspectJ can therefore only intercept a forbidden
operation if the call to it is made from code that AspectJ actually wove
(student code and project code).

Instrumentation rewrites the **target** JDK class, so it intercepts the
operation no matter who calls it.

Consequence: a forbidden operation invoked from any code AspectJ did not weave
is not seen by AspectJ but is still caught by Instrumentation.

## 2. JDK-internal call sites cannot be woven

Ares does not weave the JDK. Calls that originate **inside** the JDK are
therefore invisible to AspectJ. The most important example is the NIO network
stack: a high-level connect can bottom out in `sun.nio.ch.Net.connect*` or in
the various `sun.nio.ch.*ChannelImpl` classes, and the actual OS-level call is
made from inside those JDK classes.

Instrumentation explicitly hooks `sun.nio.ch.Net`, `SocketChannelImpl`,
`DatagramChannelImpl`, the asynchronous channel implementations and
`NetMulticastSocket` precisely as a backstop for this dispatch. AspectJ has no
equivalent and cannot acquire one, because it cannot weave call sites that live
inside the JDK.

Consequence: **raw NIO and multicast network operations are not reliably
enforced under ASPECTJ.** ASPECTJ-mode network policies should be treated as
covering the high-level, directly-called socket APIs only.

## 3. Indirect, reflective and library-mediated calls escape

Because only woven call sites are intercepted, an operation reached through a
layer AspectJ did not weave escapes enforcement:

- A third-party library on the classpath that performs the forbidden operation
  is not woven, so its internal call to the JDK is not seen.
- A reflective invocation (`Method.invoke`) made from inside the JDK reflection
  machinery is a JDK-internal call site and is not woven.
- Any helper that the build does not run through the AspectJ weaver is a blind
  spot.

Instrumentation is unaffected because it rewrites the target, so the caller
being unwoven, reflective or third-party does not matter.

## 4. Coverage is an enumerated list of call sites, not of targets

Both engines enumerate the classes and methods they care about, but the meaning
differs. For Instrumentation the enumeration selects **target types** to
rewrite (including subtypes and overrides via `hasSuperType` /
`isOverriddenFrom`), so every caller of those targets is covered. For AspectJ
the enumeration selects **call signatures** to weave at the call site, and the
`+` subtype form only helps where the call site itself is woven.

Consequence: keeping the two engines in parity requires deliberate, ongoing
effort. New JDK APIs, renamed internal classes, or new dispatch paths can widen
the AspectJ gap silently. A cross-engine parity test (run one policy through
both engines and assert identical verdicts) is the recommended guard.

## 5. Net effect on the threat model

Under the assumption that the student controls call sites, timing and object
shapes:

- **Instrumentation** is the fully-trusted enforcement mode: it sees every
  caller of a rewritten target.
- **AspectJ** is best-effort for anything that can be reached without a woven
  call site, and structurally cannot cover JDK-internal dispatch (most
  importantly raw NIO and multicast networking).

Recommendation: prefer Instrumentation where the platform allows it. Where only
AspectJ is available (e.g. environments where attaching a Java agent is not
possible),
document that raw NIO and reflective/library-mediated forbidden operations are
not guaranteed to be blocked, and constrain the policy accordingly.

## 6. The one inversion: inherited `final` `Object` methods on `Thread`

The general ordering above (Instrumentation strictly stronger) inverts for the
thread-monitor methods `Object.notify()`, `Object.notifyAll()` and the
`Object.wait(...)` overloads when the receiver is a `Thread`. The architecture
engines list `java.lang.Thread.notify()` (WALA and ArchUnit) and
`java.lang.Object.wait(long)` (ArchUnit) as forbidden thread-manipulation
methods, so a policy that forbids thread manipulation expects these to be blocked.

- **Instrumentation cannot enforce them.** These methods are `final` and declared
  by `java.lang.Object`; `Thread` neither declares nor can override them. Byte
  Buddy rewrites the *declaring* type, and the thread matcher is
  `named/hasSuperType(java.lang.Thread) and isDeclaredBy(that hierarchy)`, which
  never includes `Object`. There is therefore no `Thread.notify()` method to
  rewrite; the only reachable target is `Object.notify()` itself, which would
  instrument every object in the JVM and is unacceptable.
- **AspectJ can enforce them, caller-side.** `call(* java.lang.Object+.notify())
  && target(java.lang.Thread+)` weaves the call site and the `target(Thread+)`
  runtime guard confines advice execution to `Thread` receivers. (The narrower
  `call(* java.lang.Thread+.notify())` does not weave: AspectJ resolves the
  declaring type to `java.lang.Object`.) The advice treats a manipulation as a
  *membership-only* check against the thread-creation allow-list: a thread whose
  class may be created may also be notified, without consuming the creation quota.

Consequence and current scope: the AspectJ engine enforces `notify/notifyAll/wait`
on `Thread` receivers; the Instrumentation engine does not, so for this specific
method class **AspectJ is the stronger engine**. This is *partial* parity with the
architecture engines: enforcement is scoped to runtime `Thread` monitor targets and
deliberately does not mirror ArchUnit's broad `java.lang.Object.wait(long)` rule,
so that ordinary `Object`-monitor synchronisation is not blocked. Instrumentation-mode
policies that must forbid thread manipulation should note this residual gap.
