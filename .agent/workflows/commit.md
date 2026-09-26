---
description: Create a git commit for staged changes only, strictly adhering to Conventional Commits and project invariants.
argument-hint: "[optional commit message]"
---

Automate git commits following the repository's Conventional Commits standard and `AGENTS.md` invariants.

## Step 1 — Check Staged Area

Check what is currently staged:

```bash
git diff --cached --name-only
```

- **If the output is empty:**
  Stop immediately. Inform the user in English:
  > "⚠️ **No changes in the staged area.** Stage your intended files first using `git add <files>` before committing."
  Do **NOT** stage any files automatically. Do **NOT** commit. End turn.

## Step 2 — Verify Project Invariants (`AGENTS.md`)

Before committing, check the staged diff:

```bash
git diff --cached -U0
```

Analyze the staged modifications across all touched components, dependencies, and configuration files.

## Step 3 — Determine the Commit Message

### Case A: The user passed a message in `$ARGUMENTS` (e.g. `/commit "feat(server): add jwt"`)
Validate that it strictly follows Conventional Commits:
- **Format:** `<type>(<scope>): <description>` or `<type>: <description>`
- **Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`
- **Rules:** English only, imperative mood, lowercase description, no period at the end, first line <= 72 characters.
- If it deviates from the convention, format it cleanly in English and proceed.

### Case B: No argument was passed (`$ARGUMENTS` is empty)
Infer the best type, scope, and concise English description by inspecting the staged files:
- Only docs/markdown (`docs/*`, `README*`, `SPEC.md`) -> `docs: ...`
- Dependencies / build files (`pom.xml`, `package.json`) -> `build: ...`
- Backend API / Server (`apps/server/*`) -> `feat(server): ...` or `fix(server): ...`
- Client / UI (`apps/client/*`, `src/*`) -> `feat(ui): ...` or `fix(ui): ...`
- Configuration & environment (`.env*`, `application.properties`) -> `chore(config): ...`

### Multi-Change Structure (Bullet Points for Distinct Changes)
When the staged area contains multiple distinct modifications (e.g., configuration changes, new services, updated controllers, bug fixes):
- **Subject line (Header):** Concise conventional commit title summarizing the overarching intent (<= 72 chars).
- **Body:** Leave an empty line after the header, followed by a bulleted list where each distinct change is documented in its own bullet point:
  ```text
  <type>(<scope>): <concise title>

  * <change A: description of component change>
  * <change B: description of component change>
  * <change C: description of component change>
  ```
- **Atomic Commits Option:** If the staged changes represent completely unrelated concerns (e.g. unrelated docs update + feature code + tooling changes), propose or execute separate atomic commits by staging subsets of files with `git add`/`git reset`.

## Step 4 — Execute Commit

Commit **ONLY** the staged files using multiple `-m` flags to preserve the header and bulleted body cleanly:

```bash
git commit -m "<header>" -m "* <change A>
* <change B>
* <change C>"
```

*Note: If the execution environment has `.git` in a read-only sandbox, present the exact multi-line commit command formatted and ready for the user to run in their terminal.*

## Step 5 — Report Output

Report the result strictly in English:
- Commit hash and branch (or ready-to-run terminal command if execution requires local terminal).
- Formatted commit message including the subject and all bullet points.
- Summary of committed files categorized by component.
- Mention any remaining unstaged/untracked files if present.
