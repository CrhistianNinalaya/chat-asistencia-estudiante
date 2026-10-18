# Project Invariants & Global Guidelines

Strict quality and clean code standards for `chat-asistencia-estudiante`.

## 1. Clean Code & Language Standards
- **Strict English:** 100% of all code, identifiers, types, exception messages, validation messages, logs, tests, and git commits.
- **Zero Inline Comments (Production):** Absolutely NO inline comments (`//`, `/* */`) in function/method bodies. Code must be self-documenting.
- **Testing Structure (AAA):** Delineate tests strictly with simple section markers: `// Arrange`, `// Act`, and `// Assert` (or `// Act & Assert`).
- **No-Op Block Comment Exception (Sonar S108):** Intentional empty blocks or branches require an explanatory English comment (e.g., `// Intentionally empty: ...`).
- **Imports:** Declare explicit top-level imports only. Never use inline fully qualified class names (FQCN).

## 2. Review & Git Workflow Invariants
- **Unstaged Review Fixes:** When applying fixes resulting from code reviews (`docs/review-*.md`), NEVER automatically stage files (`git add`). Leave them in the working tree for isolated inspection via `git diff`.
