# Ares Security Policy

## Supported Versions

Currently, the only supported Ares version is whatever the current release is (as shown in the [Releases](https://github.com/ls1intum/Ares2/releases)). Pre-release lines (alpha, beta, and release-candidate builds) are not covered by this policy.

## Deliberately dangerous code

Ares is an enforcement framework, so parts of it are written to do exactly what it exists to prevent. The integration test tree under `src/test/java/de/tum/cit/ase/ares/integration/testuser/` exists to be blocked: its `subject/` package holds classes that attempt file input and output, network connections, command execution, thread creation, reflective access, privileged operations and access to reserved packages, and the test classes beside them drive those attempts against a policy and assert the outcome.

The shipped product is security-sensitive machinery of a different kind. The reserved-package prefixes, the AspectJ instrumentation and the Java agent all fall into it. The instrumentation rewrites bytecode, and the agent runs inside the supervised JVM, because that is how the enforcement works.

Neither is a vulnerability. In the test tree it is the thing under test, and in the product it is the mechanism.

## Scope

A report is in scope when Ares fails at what it claims to do, or when it causes harm nobody asked for:

- supervised code performs an operation the active security policy blocks, so containment is bypassed,
- supervised code disables or detaches the enforcement itself, for instance by tampering with the policy, the agent or the class loader,
- the enforcement reports an operation as blocked when it in fact ran, or as permitted when it in fact did not. An incorrect result feeds directly into a grading decision,
- a credential, token, or personal datum is written into a log, a report or a test artefact,
- a build-time download is fetched without the checksum verification the surrounding code claims to perform.

A report is out of scope when:

- the attack fixtures under `testuser/` behave as documented,
- the operation is one Ares does not claim to cover. The enforcement boundary is documented, and a gap on the far side of it is a known limitation rather than a defect,
- it presumes an adversary who controls the build. `docs/HowToConvertAnAres1ProjectIntoAnAres2Project.md` states this for the reserved-package boundary: the build descriptor and the command that invokes it are trusted instructor configuration, so whoever can edit `pom.xml` or `build.gradle` can remove that boundary. The threat addressed is student code, not a hostile build,
- it is a vulnerability in the JVM, in Maven, in Gradle, or in a third-party library that Ares exercises rather than introduces. Those belong upstream.

If you are unsure which side of the line a finding falls on, report it and say so.

## Reporting a bug

If the problem relates to a bug that is associated with unexpected behaviour or inconvenience or something non-critical is broken, simply report it as a bug and use the [issues](https://github.com/ls1intum/Ares2/issues) for that.

## Reporting a Vulnerability

If the problem relates to a vulnerability that could be used maliciously or is in another way a security issue, please do not make the issue public. Instead, collect the following information first:
- as with a bug report, describe how the vulnerability can be reproduced
- state the system and the versions in use, especially your Ares version
- provide any additional information and context, if possible

Then choose one of these reporting channels:
- **Preferred:** use GitHub's Private Vulnerability Reporting. Open the [Security tab](https://github.com/ls1intum/Ares2/security/advisories) of the repository and click "Report a vulnerability". This keeps the report private within GitHub while it is assessed and remediated.
- **Alternative:** send the information by email to paulsenm@in.tum.de.

I will acknowledge receipt within 7 working days and provide an initial assessment within 14 working days. If you do not hear back within these timeframes, please send a follow-up in case the message was missed.