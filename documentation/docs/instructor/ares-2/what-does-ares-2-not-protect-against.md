---
title: "What does Ares 2 not protect against"
sidebar_position: 3
description: "Where the trust boundary ends, and the build-side work Ares 2 cannot do for you."
---

Ares 2 trusts only two things:

1. its own versioned infrastructure boundary, and
2. the test classes named by the active security policy in
   `theFollowingClassesAreTestClasses`.

Everything else is untrusted and subject to the policy.

:::danger Student code must never use a reserved package
If student code can place a class into a reserved package, it can shadow the classes Ares 2
and the test harness rely on, and the sandbox can be bypassed. This is why the build-side
check described in [Postcompile Mode](../protect-a-java-project/postcompile.md#3-what-you-need-to-do-outside-ares-2)
is mandatory rather than optional: Ares 2 cannot enforce it from inside the JVM it is
protecting.
:::

For the runtime inventory, the build prerequisite and the migration table, see the
[enforcement model](/maintainer/subsystems/policy/enforcement-model) in the developer guide.
