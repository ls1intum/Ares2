#!/usr/bin/env python3
"""Check a pull request body against .github/PULL_REQUEST_TEMPLATE.md.

The required section headings are read from the template itself rather than duplicated
here, so editing the template cannot leave this check behind.

The check validates shape, not substance: that every section exists, that none was left
empty, and that no unfilled stub survived. It deliberately does not require checkboxes to
be ticked, which would only train contributors to tick them.

The body arrives through the PR_BODY environment variable. It is untrusted input on fork
pull requests and must never be interpolated into a shell command.
"""

import os
import re
import sys

TEMPLATE = ".github/PULL_REQUEST_TEMPLATE.md"

HEADING = re.compile(r"^## .+$", re.MULTILINE)
HTML_COMMENT = re.compile(r"<!--.*?-->", re.DOTALL)
FENCED_BLOCK = re.compile(r"^```.*?^```", re.DOTALL | re.MULTILINE)
BARE_LIST_MARKER = re.compile(r"^\s*\d+\.\s*$")
EMPTY_TABLE_ROW = re.compile(r"^\s*\|(\s*\|)+\s*$")

# Escape hatches the template itself documents. A section carrying one of these is
# complete by definition and is exempt from the leftover-stub scan.
ESCAPE_HATCHES = (
    "no production java code changed",
    "not reproducible from an exercise",
    "no mode-specific behaviour changed",
    "no improvement",
)


def sections(text, headings):
    """Split text into {heading: body} for the given headings, in document order."""
    found = [(m.start(), m.group().strip()) for m in HEADING.finditer(text)]
    found = [(pos, head) for pos, head in found if head in headings]
    result = {}
    for index, (pos, head) in enumerate(found):
        end = found[index + 1][0] if index + 1 < len(found) else len(text)
        result[head] = text[pos + len(head):end]
    return result


def visible(text):
    """The prose a reader actually sees: HTML comments removed, whitespace collapsed."""
    return HTML_COMMENT.sub("", text).strip()


def main():
    try:
        with open(TEMPLATE, encoding="utf-8") as handle:
            template = handle.read()
    except OSError as error:
        print(f"::error::cannot read {TEMPLATE}: {error}")
        return 1

    required = [m.group().strip() for m in HEADING.finditer(template)]
    if not required:
        print(f"::error::{TEMPLATE} declares no '## ' headings; nothing to enforce")
        return 1

    body = os.environ.get("PR_BODY") or ""
    problems = []

    if not visible(body):
        problems.append(
            "The pull request body is empty. Start from "
            f"{TEMPLATE} and fill in every section."
        )
        report(problems, required)
        return 1

    missing = [head for head in required if head not in body]
    for head in missing:
        problems.append(f"Missing section heading: '{head}'")

    present = sections(body, set(required) - set(missing))
    for head, content in present.items():
        prose = visible(content)
        if not prose:
            problems.append(
                f"Section '{head}' is empty. The template states what to write when it "
                "does not apply."
            )
            continue

        if any(hatch in prose.lower() for hatch in ESCAPE_HATCHES):
            continue

        scannable = FENCED_BLOCK.sub("", HTML_COMMENT.sub("", content))
        for line in scannable.splitlines():
            if BARE_LIST_MARKER.match(line):
                problems.append(
                    f"Section '{head}' still contains an unfilled list stub "
                    f"({line.strip()!r} with nothing after it)."
                )
                break
        for line in scannable.splitlines():
            if EMPTY_TABLE_ROW.match(line) and not re.match(r"^\s*\|[\s:-]*\|", line):
                problems.append(
                    f"Section '{head}' still contains the empty template table row. "
                    "Fill it in, or use the documented escape hatch."
                )
                break

    report(problems, required)
    return 1 if problems else 0


def report(problems, required):
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if problems:
        lines = [
            "## Pull request template check failed",
            "",
            f"The body does not follow [{TEMPLATE}]({TEMPLATE}). "
            f"It declares {len(required)} required sections.",
            "",
        ]
        lines += [f"- {problem}" for problem in problems]
        lines += [
            "",
            "Copy the template into the body, fill in every section, and the check "
            "re-runs automatically when the description is edited.",
        ]
        for problem in problems:
            print(f"::error::{problem}")
    else:
        lines = [
            "## Pull request template check passed",
            "",
            f"All {len(required)} required sections are present and filled in.",
        ]
        print(f"All {len(required)} required sections are present and filled in.")

    if summary_path:
        with open(summary_path, "a", encoding="utf-8") as handle:
            handle.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    sys.exit(main())
