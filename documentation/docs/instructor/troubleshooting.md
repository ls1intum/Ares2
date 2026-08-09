---
title: "Troubleshooting"
sidebar_position: 6
description: "The failures instructors actually hit when protecting or migrating an exercise, and what each one means."
---

:::tip[ELI5]
When something goes wrong, the error message usually names the mechanism rather than the cause.

This page translates the messages back into what you actually have to change.
:::

:::note[This page is a stub]
The entries below are the intended structure. The material exists today inside the setup manual
and the Ares 1 migration guide, and is being consolidated here so there is one place to look
rather than two. Until that lands, check those pages as well.
:::

## The build succeeds but nothing is enforced

Missing agent attachment, a `@Policy` that never applied, or a reserved-package check that was
never hooked onto the test task.

## A permitted operation is rejected

Path normalisation, an allow-list entry that names a directory where a file was meant, or a call
that reaches the forbidden operation through a library rather than directly.

## A forbidden operation is not rejected

The most serious case. Which of the four analysis and weaving combinations was actually active,
and whether the supervised package was shadowed.

## The agent does not attach

`--add-opens` requirements, `useSystemClassLoader`, and the Surefire `argLine` trap: a plain
`<argLine>` replaces what other plugins contributed, silently dropping the JaCoCo agent.

## Gradle runs the tests but not the boundary check

`check.dependsOn test` and not the reverse, so a hook on `check` never runs for `gradlew test`.

## The policy file is rejected

Version gate, unknown fields, and values the enforcement layers cannot honour.

## Timeouts do not fire

Policy resource limits are generated but not dispatched in Postcompile. `@StrictTimeout` is the
mechanism that applies there.

## Notes

- Before filing a bug, record which of the four combinations you ran. A failure that appears in
  only one of them is a different problem from one that appears in all four.
