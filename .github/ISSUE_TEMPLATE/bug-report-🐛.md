---
name: "Bug Report \U0001F41B"
about: This template should help reporting bugs in Ares2
title: ''
labels: bug
assignees: ''

---

name: 🐛 Bug Report
description: File a bug report to help us improve
title: "[Bug]: "
labels: [bug]
body:
  - type: markdown
    attributes:
      value: |
        Thanks for taking the time to fill out this bug report!

  - type: input
    id: bug_title
    attributes:
      label: Title
      description: A short summary of the bug
    validations:
      required: true

  - type: textarea
    id: description
    attributes:
      label: Describe the bug
      description: What happened? Also tell us, what did you expect to happen?
      placeholder: A clear and concise description of the issue.
    validations:
      required: true

  - type: textarea
    id: steps
    attributes:
      label: To Reproduce
      description: Steps to reproduce the behavior.
      placeholder: |
        1. Go to '...'
        2. Click on '...'
        3. Scroll down to '...'
        4. See error
    validations:
      required: true

  - type: textarea
    id: expected
    attributes:
      label: Expected behavior
      description: What did you expect to happen instead?
    validations:
      required: true

  - type: textarea
    id: screenshots
    attributes:
      label: Screenshots
      description: If applicable, add screenshots to help explain your problem.

  - type: input
    id: version
    attributes:
      label: Which version of Artemis are you seeing the problem on?
      placeholder: e.g., 2025.06.01
    validations:
      required: true

  - type: dropdown
    id: browser
    attributes:
      label: What browsers are you seeing the problem on?
      multiple: true
      options:
        - Chrome
        - Firefox
        - Safari
        - Edge
        - Other
    validations:
      required: false

  - type: textarea
    id: context
    attributes:
      label: Additional context
      description: Add any other context about the problem here.

  - type: textarea
    id: logs
    attributes:
      label: Relevant log output
      description: Copy and paste any relevant log output (e.g. from your browser's Web Console).
