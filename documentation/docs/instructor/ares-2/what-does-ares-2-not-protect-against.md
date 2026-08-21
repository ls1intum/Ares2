---
title: "What does Ares 2 not protect against"
sidebar_position: 3
description: "Where the trust boundary ends: what Ares 2 does not attempt, where enforcement is thinner than it looks, and the build-side work it cannot do for you."
---

:::tip[Simple Story]
Every checklist has a last line, and it is safer to know where yours ends than to assume it
covers the whole room.

The teacher watches what a pupil does. They cannot tell whether the answer was the pupil's own
idea, and they cannot vouch for equipment the school itself carried in. And one job cannot be
done from a desk at all: nobody sitting there can confirm that the checklist in front of them
is the one the board issued. Your build has to guarantee that, because Ares cannot guard the
door it is standing behind.
:::

## What Ares 2 trusts

Ares 2 trusts only two things:

1. its own versioned infrastructure boundary, and
2. the test classes named by the active security policy in
   `theFollowingClassesAreTestClasses`.

Everything else is untrusted and subject to the policy.

## What Ares 2 does not attempt

These are not gaps to be closed later. They are outside what a sandbox can do at all.

**Plagiarism.** Ares 2 observes what a submission *does*. It has no view of whether the
submission is the student's own work, whether it resembles another student's, or whether it was
copied from a repository or generated. A plagiarised solution that stays inside the policy runs
and passes, exactly as an honest one does. Plagiarism detection compares submissions with each
other and with external sources, which is a different discipline needing different tooling;
Artemis provides it separately. Ares protects the integrity of the *execution*, not the
authorship of the code.

**Whether the solution is any good.** Correctness, style and efficiency are what your own test
methods and review are for. Ares only decides whether the code was allowed to do what it did.

**Your dependencies.** Ares 2 supervises the student's code. It does not audit the libraries
your exercise itself pulls in, and code you list under `theFollowingClassesAreTestClasses` is
exempt from the policy by design. A compromised or careless dependency in the exercise
scaffolding is your supply chain to manage, not something the sandbox inspects.

**The machine around the Java Virtual Machine (JVM).** Everything outside the supervised process, the continuous integration (CI) runner, the
container and the network it sits on, is your infrastructure to secure.

## Where enforcement is thinner than it looks

These are real limits of the current implementation, worth knowing before you rely on them.

**The nine non-grantable categories have no runtime enforcement of their own.** Reflection,
native code, class loading, JVM termination, agent attach, module system bypasses,
serialisation, JNDI and environment access are rejected by the architecture layer if a route to
them exists in the compiled submission, and that is the only check aimed at those categories.
An individual call may still be caught where it overlaps a category that does have runtime
checks, `System.load` being both native code and a file execution, but a route the static
analysis cannot see is not otherwise caught. Package imports are architecture-only as well,
though that one you can configure.

**Granting anything in a category makes it runtime-only.** While a permission list is empty,
Ares adds an architecture check for that category. Grant one path, host, command or thread
class and the check is dropped, because static analysis cannot distinguish your permitted call
from a forbidden one. The category is then only as strong as the runtime mechanism you
configured, which is why the weakness below matters.

**AspectJ mode is strictly weaker than instrumentation mode.** AspectJ weaves the call site, so
it can only intercept a forbidden operation where the calling code was itself woven. A call
that bottoms out inside the Java Development Kit (JDK), in the NIO network stack for instance, is invisible to it.
Instrumentation rewrites the target class and catches it regardless of the caller. The
[honest comparison](/contributor/subsystems/aop/aspectj-vs-instrumentation-weaknesses) lists
what the weaker mode misses.

**A policy timeout does not bound anything today.** `regardingTimeouts` parses and is written
into the generated configuration, but the JUnit extension path does not enforce it. Use
`@StrictTimeout` for an actual deadline. See
[Resource Limits](/instructor/policy-reference/resource-limits).

## The one job Ares 2 cannot do for you

:::danger[Student code must never use a reserved package]
If student code can place a class into a reserved package, it can shadow the classes Ares 2
and the test harness rely on, and the sandbox can be bypassed. This is why the build-side
check described in [the Gradle walkthrough](../protect-a-java-project/postcompile/gradle.md)
is mandatory rather than optional: Ares 2 cannot enforce it from inside the Java Virtual Machine (JVM) it is
protecting.
:::

## Further reading

For the runtime inventory, the build prerequisite and the migration table, see the
[enforcement model](/contributor/subsystems/policy/enforcement-model) in the developer guide.
