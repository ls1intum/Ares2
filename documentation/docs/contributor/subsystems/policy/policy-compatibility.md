---
title: "Policy Compatibility"
sidebar_position: 4
description: "Which Ares release accepts which policy-format version and which test-behaviour keys."
---

:::tip[Simple Story]
The checklist changes from one release to the next. Before the board of education hands a checklist to a school, it checks that the school's edition of the rules knows every line on it.

[Who is who](/instructor/who-is-who)
:::

## What the table answers

An Ares release refuses a policy that uses a key it does not know, so a policy written for a newer release fails every test on an older one. This table lists, per release, the policy-format versions (`thisPolicyFileCompliesToThePolicyVersion`) and the test-behaviour keys it accepts. Ares publishes the same data as [`policy-compatibility.json`](pathname:///policy-compatibility.json) for tools such as Artemis to read.

The key names of 2.3.0 follow the proposal in issue #386, section C, and are not final until the advisor accepts them.

## Accepted keys by release

<!-- Generated from documentation/static/policy-compatibility.json; PolicyCompatibilityManifestTest fails when the two differ. -->
| Release | Policy format | Test-behaviour keys |
|---|---|---|
| 2.2.1 | 1 to 1 | `theFollowingTestBehaviorIsConfigured` |
| 2.3.0 | 1 to 1 | `theFollowingTestBehaviorIsConfigured`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheStrictTimeoutForTheTestCases`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheOutputOfTheTestCases`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheFailureFeedbackForTheTestCases`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheVisibilityAndScheduleOfTheTestCases`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheStrictTimeoutForTheTestCases.theTimeoutIs`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheStrictTimeoutForTheTestCases.theTerminationGraceIs`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheOutputOfTheTestCases.theOutputIsMirrored`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheOutputOfTheTestCases.theMaximumNumberOfOutputCharactersIs`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheOutputOfTheTestCases.theInputAndOutputAreManagedBy`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheOutputOfTheTestCases.theCustomInputAndOutputManagerIs`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheFailureFeedbackForTheTestCases.onlyPrivilegedExceptionsAreReported`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheFailureFeedbackForTheTestCases.theFailureMessageForOtherExceptionsIs`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheFailureFeedbackForTheTestCases.theSecurityViolationFeedbackLevelIs`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheVisibilityAndScheduleOfTheTestCases.theDeadlineIs`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheVisibilityAndScheduleOfTheTestCases.theDeadlineIsExtendedBy`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheVisibilityAndScheduleOfTheTestCases.theHiddenTestsAreRunBefore`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheVisibilityAndScheduleOfTheTestCases.testsNotListedHereAreHidden`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheVisibilityAndScheduleOfTheTestCases.theFollowingTestsAreHidden`<br/>`theFollowingTestBehaviorIsConfigured.regardingTheVisibilityAndScheduleOfTheTestCases.theFollowingTestsArePublic` |
