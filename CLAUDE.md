# CLAUDE.md

@AGENTS.md

That import is this file's content. `AGENTS.md` holds the conventions this repository is
held to, written for whoever is doing the work, and a rule restated here would be a second
copy free to drift from the first.

What follows are deliberately abbreviated reminders of the two conventions an agent gets
wrong most often. Both are stated in full above, and where the short form and the full one
disagree, the full one is right.

- `gh pr create --body` bypasses `.github/PULL_REQUEST_TEMPLATE.md` silently, so read that
  file before writing a body, and check the body before opening the pull request with
  `PR_BODY="$(cat body.md)" java .github/scripts/CheckPullRequestTemplate.java`.
- A required heading, a character limit or a whole-section phrase changed in the template
  has to change in `.github/scripts/CheckPullRequestTemplate.java` in the same commit.
  Nothing detects the two files drifting apart.
