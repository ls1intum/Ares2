<!--
  Release notes template for Ares 2.

  GitHub does not prefill release notes the way it prefills a pull request, so copy the
  body of this file into the release description and fill it in. Keep the headings and
  their order, so that consecutive releases stay comparable.

  Scope: everything merged since the previous release tag. List them with
  `git log --oneline <previous-tag>..main` and read the bodies of the pull requests it
  names, since each one already answers sections 3 to 5 for its own change.

  Sections 3, 4 and 5 carry the same meaning as in PULL_REQUEST_TEMPLATE.md, aggregated
  over the whole release rather than a single change. Each section states what to write
  when it does not apply.

  Write in British English. State figures you have verified, and leave out those you
  have not: a release note is read as a record.
-->

<!--
  At most three lines: what this release changes, and why it matters. No implementation
  detail, and no list of pull requests.
  Say plainly whether the release is breaking, in a clause, and leave the detail and the
  upgrade steps to "Breaking changes and migration" below. A reader who sees "breaking"
  in the opening paragraph knows to read on; one who only finds out at the bottom has
  usually decided already.
  This section is always required and has no heading, so that it renders as the opening
  paragraph of the release.
-->

## Linked issues

<!--
  The issues this release closes or relates to, for example "Closes #123" or
  "Relates to #456", each with a few words on what it was.
  If no issue is involved, write "None".
-->

## Problems

<!--
  What was wrong before this release, in enough depth that a reader can judge whether it
  affected them. One block per problem, most significant first.

  For each, useful to cover:
  - What behaviour was observed, and under which configuration (Java version, build
    tool, AOP mode: AspectJ or instrumentation, architecture mode: ArchUnit or WALA,
    operating system)?
  - Where the root cause sits. Ares is itself the security boundary, so say whether it
    was in the policy layer, the generated security test, the enforcement (AOP or
    architecture) layer, or the build integration.
  - Which way it failed. A false negative let forbidden student code through, a false
    positive failed a correct submission. Say which, because the two carry very
    different weight for anyone deciding whether to upgrade.

  Group the routine maintenance (dependency bumps, CI configuration, documentation) into
  one short block rather than one block each.

  If a release fixes no defect, describe the gaps, limitations or maintenance burden it
  addresses instead. Never write "None" here: a release with nothing to say under
  Problems does not need notes.
-->

## Improvements from the user's perspective

<!--
  Users are everyone who consumes Ares: students whose submissions run under a security
  policy, and instructors who author policies and ship Ares inside an exercise test
  repository.
  Describe the concrete benefit, for example clearer failure messages, fewer false
  positives, a policy option that was previously impossible to express, a faster test
  run, or a newly supported language or build tool.

  State any limitation that bounds what the improvement is worth, in particular where a
  hardening is partial. A reader who upgrades expecting a guarantee that does not hold
  is worse off than one who was told the boundary.

  If this side does not benefit from this release, write "No Improvement".
-->

## Improvements from the maintainer's perspective

<!--
  Maintainers are those who develop Ares itself.
  Describe the benefit for them, for example reduced duplication, a clearer abstraction,
  a flaky test removed, better diagnostics, less manual release work, or a dependency or
  CI simplification.
  Close with the dependency and tooling updates in one line.

  If this side does not benefit from this release, write "No Improvement".
-->

## Breaking changes and migration

<!--
  Ares is consumed as a released Maven artefact, so state explicitly whether this release
  changes any of:
  - the public API under de.tum.cit.ase.ares.api
  - the security policy file format or its schema
  - the generated security test code that exercise repositories rely on
  - the minimum JDK, Maven or Gradle version

  For each one that changed, say what an instructor has to do to upgrade an existing
  exercise, and show the before and the after where a code or policy snippet makes it
  concrete. An instructor reads this section to size the work, so an unquantified
  "policies must be updated" is worth little.

  A change that fails closed belongs here even when it is technically a fix: a policy
  that used to be accepted and is now rejected breaks a working exercise, whatever the
  reason. Removals belong here in full, listed by name, since a missing symbol is found
  at compile time by the person least able to explain it.

  If the release is fully backwards compatible, write "None".
-->

None.

## Coordinates

<!--
  The dependency snippets for this version, so nobody has to construct them. Replace the
  version in both, and keep the agent line: the Java agent ships under the `agent`
  classifier, and an exercise that misses it repackages the agent by hand for nothing.

  Publish these notes only once the artefacts are actually live on Maven Central, which
  is a separate step from creating the GitHub release and can lag it. Confirm with
  https://repo1.maven.org/maven2/de/tum/cit/ase/ares/maven-metadata.xml, which must list
  this version under <release>. Notes that name coordinates nobody can resolve yet cost
  more goodwill than they buy.

  Close with the full changelog link, comparing the previous tag to this one.
-->

Maven:

```xml
<dependency>
    <groupId>de.tum.cit.ase</groupId>
    <artifactId>ares</artifactId>
    <version>REPLACE_WITH_THIS_VERSION</version>
</dependency>
```

Gradle:

```groovy
testImplementation "de.tum.cit.ase:ares:REPLACE_WITH_THIS_VERSION"
aresAgent "de.tum.cit.ase:ares:REPLACE_WITH_THIS_VERSION:agent"
```

The Java agent is published under the `agent` classifier, so no manual repackaging is needed.

**Full changelog:** https://github.com/ls1intum/Ares2/compare/REPLACE_WITH_PREVIOUS_TAG...REPLACE_WITH_THIS_TAG
