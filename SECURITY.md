# Ares Security Policy

## Supported Versions

Currently, the only supported Ares version is whatever the current release is (as shown in the [Releases](https://github.com/ls1intum/Ares2/releases)). Pre-release lines (alpha, beta, and release-candidate builds) are not covered by this policy.

## Deliberately dangerous code

Ares is an enforcement framework, so parts of it are written to do exactly what it exists to prevent. Most of that code is test fixture. It is meant to be found, and none of it is a vulnerability. It lives in these places:

- `src/test/java/de/tum/cit/ase/ares/integration/aop/` holds the largest set by far. Under `forbidden/subject/`, classes attempt file reads, writes, creations, deletions and executions, socket and HTTP connections, command execution through `Runtime` and `ProcessBuilder`, and thread creation through many different APIs, each so that a policy can block it. Under `allowed/subject/`, classes attempt the same operations under a policy that permits them, so that an operation wrongly blocked is caught as well as one wrongly permitted. The tests that drive them live in `integration/aop/` and `integration/architecture/`.
- `src/test/java/de/tum/cit/ase/ares/integration/testuser/` drives an older set. Its `subject/` package attempts file input and output, network connections, command execution, thread creation, reflective access into Ares's own packages, privileged operations and JVM termination. One of its tests opens a connection to a real external host rather than to a reserved test address.
- `src/test/java/org/apache/xyz/` and `src/test/java/p/` impersonate. The first squats a namespace that reads like a third-party library, to check that Ares does not trust a package because of its name, and holds a class called `Circumvention` for that purpose. The second holds a class whose fully qualified name merely shares a prefix with an allow-listed one.
- `src/test/java/example/student/` reaches into Ares itself, invoking package-private advice methods reflectively so that a fixture outside Ares's own packages can exercise them directly.

`examples/` is not a fixture tree, but it behaves like one: both example exercises ship a supervised class that performs a forbidden file read, so that running the exercise shows the enforcement working.

Some of this trips automated scanners. The fixed AES key in `CipherInputStreamReadMain`, under the file-system read fixtures, is one such case. It makes a test deterministic and protects nothing.

The shipped product is security-sensitive machinery of a different kind. The reserved-package prefixes, which are the namespaces supervised code may not itself declare, the AspectJ instrumentation and the Java agent all fall into it. The instrumentation rewrites bytecode, and the agent runs inside the supervised JVM, because that is how the enforcement works.

Neither is a vulnerability. In the test tree it is the thing under test, and in the product it is the mechanism.

## Scope

A report is in scope when Ares fails at what it claims to do, or when it causes harm nobody asked for:

- supervised code performs an operation the active security policy blocks, so containment is bypassed,
- supervised code disables or detaches the enforcement itself, for instance by tampering with the policy, the agent or the class loader,
- the enforcement reports an operation as blocked when it in fact ran, or as permitted when it in fact did not. An incorrect result feeds directly into a grading decision,
- a credential, token, or personal datum is written into a log, a report or a test artefact,
- a build-time download is fetched without the checksum verification the surrounding code claims to perform.

A report is out of scope when:

- the deliberately dangerous code described above behaves as documented,
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