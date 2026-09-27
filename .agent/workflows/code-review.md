---
description: Perform an exhaustive code review strictly on staged changes, generating a dedicated docs/review-#.md file for each discovered issue.
argument-hint: "[optional specific staged path, e.g. src/engine]"
---

Perform an in-depth code review focused strictly on the staged changes (`git diff --cached`). Every distinct issue discovered must be documented in its own dedicated `docs/review-#.md` file.

## Step 1 — Check the Staged Area

Check which files are currently staged, excluding review reports and agent workflow configurations:

```bash
git diff --cached --name-only -- . ':(exclude)docs/review-*.md' ':(exclude).agent/*'
```

- **If the output is empty:**
  Stop immediately. Inform the user in English:
  > "⚠️ **No changes in the staged area.** Stage your intended files first using `git add <files>` before running `/code-review`."
  Do **NOT** stage any files automatically. Do **NOT** generate any review files. End turn.

- **Categorize Staged Files:**
  Separate staged files into two distinct categories:
  1. **Specification & Context Layer:** `AGENTS.md`, `SPEC.md`, `README*.md`, and architectural markdown docs.
  2. **Implementation Layer:** Source code (`src/`), scripts (`tools/`), data (`public/data/`), and configuration files.

## Step 2 — Ingest Context from Staged Specifications

If the staged area contains changes to documentation or specification files (`AGENTS.md`, `SPEC.md`, `README*.md`):

1. Inspect their staged diff first:
   ```bash
   git diff --cached -U5 -- AGENTS.md SPEC.md README*.md
   ```
2. **Extract the Architectural Intent & New Conventions:**
   - What feature, refactor, or convention change is introduced?
   - Are rules being added, modified, or clarified (e.g., test file colocation, `hooks/` vs `utils/` folder architecture)?
3. **Establish Acceptance Criteria:**
   Use these updated specifications as the primary lens for the review. Understand that code changes (such as moved test files, folder reorganization, or deleted directories) may be the intentional consequence of these updated conventions rather than accidental regressions.

## Step 3 — Extract and Inspect the Staged Implementation Diff

Extract the unified diff for all staged implementation files:

```bash
git diff --cached -U5 -- . ':(exclude)docs/review-*.md' ':(exclude).agent/*' $ARGUMENTS
```

- Inspect **only** the lines modified or added in the staged diff, along with their immediate surrounding context.
- For non-trivial modifications, use `view_file` on the staged files to verify imports, component structure, domain separation, and external contracts.
- Do **NOT** flag pre-existing issues in untouched code. Focus strictly on the staged incoming changes.

## Step 4 — Review Against Project Invariants & Soundness

Evaluate every staged file against the project's authoritative source of truth:

### 1. Repository Invariants Compliance ([`AGENTS.md`](../../AGENTS.md))
Audit the staged changes against all rules and constraints defined in [`AGENTS.md`](../../AGENTS.md), which serves as the single source of truth for:
- Clean code and language standards (zero inline comments, 100% English).
- Date and time handling (`Instant` UTC boundaries).
- Database and JPA performance (N+1 query prevention, transaction boundaries, cursor pagination).
- Modern Java and SonarQube rules (pattern switch, no empty blocks, unused variables).
- Architecture, security, and service-level authorization boundaries.

Any violation of an invariant defined in [`AGENTS.md`](../../AGENTS.md) must be documented in a review file.

### 2. End-to-End Flow Traceability & Contract Integrity
Verify that changes maintain seamless flow across all application layers:
- **Controller & DTO Flow:** Validate that request parameters (e.g. `before`, `limit`), validation constraints (`@Valid`, `@Size`, `@NotBlank`), and response DTOs properly connect into the service methods.
- **WebSocket & Messaging Flow:** Ensure payload serialization and destination topics (e.g. `/topic/chat/{ticketId}`) align between publishers and subscribers.
- **Documentation Sync:** Ensure changes to endpoints, query parameters, or payloads are accurately reflected in `README.md`.

### 3. Duplication & Abstraction Scout
Detect boilerplate and repetition introduced in staged code:
- **Repetitive Query & Exception Handling:** Look for duplicated `repository.findById(id).orElseThrow(...)` or repeated access checks across multiple service methods that should be extracted into shared private helpers.
- **Validation Consistency:** Ensure validation logic (e.g. active ticket check, sender role check) is unified rather than copy-pasted across methods.

### 4. Verification & Build
Verify that the project compiles cleanly and passes existing checks without errors.

## Step 5 — Determine Numbering for `review-#.md`

Inspect existing `docs/review-*.md` files to find the current highest review number:

```bash
ls docs/review-*.md 2>/dev/null
```

- Parse the highest existing index $N$ (for example, if `docs/review-1.md` exists, $N = 1$; if none exist, $N = 0$).
- Each distinct issue discovered will be assigned its own sequential file starting from $N + 1$:
  - First issue: `docs/review-<N+1>.md`
  - Second issue: `docs/review-<N+2>.md`
  - And so on.

## Step 6 — Write an Individual `review-#.md` for Each Issue

For each distinct issue, create `docs/review-<#>.md` adhering to the following structure:

```markdown
# Review <#> — <Concise Summary of Issue>

**Date:** <YYYY-MM-DD> · **Scope:** `<file_path>:<line_number>` · **Severity:** **<P0 | P1 | P2>**

Severity definition:
- **P0**: Critical — build failure, runtime crash, data corruption/loss, broken routing/graph invariant.
- **P1**: Major — logic bug, unhandled edge case, strict AGENTS.md invariant violation (e.g. non-null assertion, domain purity leak).
- **P2**: Minor — code hygiene, token hardcoding, formatting, style guideline deviation.

---

## <P0 | P1 | P2>-1 — <Title of Issue>

`<file_path>:<start_line>-<end_line>`

\`\`\`ts
// Offending code snippet from staged diff
\`\`\`

### Failure Scenario / Rationale
<Detailed explanation of why this is defective, how it breaks in production, or which rule in AGENTS.md / SPEC.md is violated.>

### Fix
<Actionable instruction on how to resolve the issue.>

\`\`\`ts
// Drop-in replacement fix
\`\`\`
```

## Step 7 — Summary Report

Conclude with a clear markdown summary in the chat response:

1. **Context Summary:**
   Briefly note if staged specification files were ingested (e.g., new folder or test conventions).
2. **Issues Table:**
   | Review File | Severity | Location | Summary |
   |---|---|---|---|
   | `docs/review-2.md` | P0 | `src/...` | Description of issue |

3. **Overall Status:**
   - If issues were found: list the created `docs/review-#.md` files and summarize recommended next steps.
   - If zero issues were found: announce that the staged changes cleanly satisfy all invariants, tests, and guidelines. No review files needed.
