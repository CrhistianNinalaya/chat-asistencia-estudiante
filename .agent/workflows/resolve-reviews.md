---
description: Inspect and resolve active review files in docs/review-*.md, verifying fixes and deleting resolved review reports.
argument-hint: "[optional specific review number, e.g. 1]"
---

Inspect active code review issues documented in `docs/review-*.md`, verify whether each issue is already resolved in the codebase, apply fixes if requested, and clean up resolved review files.

## Step 1 — Check Existing Review Files

List all active review files in `docs/`:

```bash
ls docs/review-*.md 2>/dev/null
```

- **If no review files are found:**
  Inform the user in English:
  > "✅ **No active review files found.** There are no pending reviews in `docs/review-*.md`."
  End turn.

- **If `$ARGUMENTS` specifies a review number (e.g. `1` or `review-1`):**
  Filter the review list to target only `docs/review-<#>.md`.

## Step 2 — Audit Each Review File Against Current Code

For each target `docs/review-<#>.md`:

1. View the review file to extract:
   - Target file path and offending line range.
   - Severity and description of the defect.
   - Expected fix or condition to satisfy.

2. Inspect the target file in the repository using `view_file` or `grep_search`.

3. Determine the status using explicit evaluation criteria:

   #### Mark as **PENDING** if ANY of the following apply:
   - **Flaw Still Present:** The exact offending code snippet, method call, SQL statement, or flawed logic identified in the review is still present in the file.
   - **Partial / Incomplete Implementation:** The fix was applied in one place but omitted in connected layers (for example, updated the Java entity annotation but forgot the SQL seed script, or added a service check but did not update repository queries).
   - **Ineffective Resolution:** The code was modified, but the core failure scenario described in the review can still trigger (e.g., race conditions, precision truncation, N+1 query cascades, or unauthorized access).
   - **Invariant Regressions ([`AGENTS.md`](../../AGENTS.md)):** An attempted fix introduces new invariant violations (e.g. inline comments in method bodies, non-English names, `LocalDateTime` instead of `Instant`, missing `@EntityGraph`, or unhandled empty blocks).
   - **Relocated Flaw:** The code was moved to a different method or class during a refactor, but the underlying defective behavior persists.

   #### Mark as **OUT OF SCOPE** if ANY of the following apply:
   - **Pre-existing / Untouched Code:** The flagged code belongs to an existing module that was not introduced or touched by the current task or staged changes.
   - **Architectural / Feature Scope Mismatch:** The proposed fix requires broader database schema migrations, new infrastructure, or a major refactoring that belongs to a separate, deferred task.
   - **Intentional Design Choice:** The current implementation was explicitly chosen or approved by the user as an intentional trade-off or design decision.

   #### Mark as **RESOLVED** ONLY when ALL of the following are satisfied:
   - **Offending Code Eliminated:** The problematic pattern identified in the review is completely removed.
   - **Sound Alternative Implemented:** The recommended fix (or an equivalent/superior pattern) is fully integrated across all affected layers.
   - **Zero Invariant Violations:** The resolution strictly adheres to all project standards defined in [`AGENTS.md`](../../AGENTS.md).

## Step 3 — Clean Up Resolved and Out-of-Scope Reviews

1. **For reviews marked as RESOLVED:**
   Delete the corresponding review file:
   ```bash
   rm docs/review-<#>.md
   ```
   Log that `docs/review-<#>.md` was confirmed resolved and removed.

2. **For reviews marked as OUT OF SCOPE:**
   When the user has confirmed or the change is verifiably out of the current task's scope:
   ```bash
   rm docs/review-<#>.md
   ```
   Log that `docs/review-<#>.md` was marked as out of scope and cleaned up.

## Step 4 — Applying Fixes & Handling Pending Reviews

When applying fixes for pending reviews:
1. **Strictly Unstaged (Working Area Only):** Never run `git add` or stage files automatically when applying review fixes.
   - All changes must remain unstaged in the working tree so the user can easily run `git diff` to inspect only the review fixes in isolation before deciding to stage them.
2. **Apply the Verified Fixes:** Edit the files in the working directory following the recommendations in the review and respecting all invariants in [`AGENTS.md`](../../AGENTS.md).
3. **Clean Up Confirmed Fixes:** Once the fix is applied in the working tree, delete the corresponding `docs/review-<#>.md` file.
4. **If fixes require clarification or design decisions:** Present each pending review with its file location, severity (`P0`, `P1`, `P2`), and proposed resolution, prompting the user for approval before editing.

## Step 5 — Summary Report

Conclude with a clear summary:

| Review File | Severity | Location | Status | Action Taken |
|---|---|---|---|---|
| `docs/review-1.md` | P1 | `apps/server/...` | Resolved | Deleted |
| `docs/review-2.md` | P2 | `apps/server/...` | Out of Scope | Deleted (Deferred) |
| `docs/review-3.md` | P1 | `apps/server/...` | Pending | Action required |

If all reviews were resolved or dismissed:
> "🎉 **All review issues have been addressed.** The review files in `docs/` have been cleaned up."
