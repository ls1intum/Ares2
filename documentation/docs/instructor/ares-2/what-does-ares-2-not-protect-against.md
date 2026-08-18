---
title: "What does Ares 2 not protect against"
sidebar_position: 3
description: "Where the trust boundary ends, and the build-side work Ares 2 cannot do for you."
---

:::tip[Simple Story]
Every checklist has a last line, and it is safer to know where yours ends than to assume it
covers the whole room.

Two things are trusted rather than checked: the school's own fixtures, and the test classes the
board named in `theFollowingClassesAreTestClasses`. Everything else is a pupil.

And one job cannot be done from inside the examination at all. A pupil who can slip their own
paperwork in among the school's stationery can impersonate the very thing doing the checking,
and no desk-side check will catch it. Your build has to make that impossible, because Ares
cannot guard the door it is standing behind.
:::

## What Ares 2 trusts

Ares 2 trusts only two things:

1. its own versioned infrastructure boundary, and
2. the test classes named by the active security policy in
   `theFollowingClassesAreTestClasses`.

Everything else is untrusted and subject to the policy.

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
