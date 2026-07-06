# /check-updates

**KMM Agent Skills** — check whether the local skills collection is behind
`origin/main` on [github.com/ronjunevaldoz/kmm-agent-skills](https://github.com/ronjunevaldoz/kmm-agent-skills).

---

## Step 1 — Run the update check

```bash
python3 scripts/check_updates.py
```

**Exit 0** → skills are up to date. Continue with your work.

**Exit 1** → updates are available. Show the output verbatim and present the user with the
choice below.

**Exit 2** → could not reach remote (offline / no git remote). Warn the user that the skills
may be stale and continue.

---

## Step 2 — Present the update choice (exit 1 only)

```
Skills update available:
  Local:  v<local_version>
  Remote: v<remote_version>  (<N> commit(s) ahead)

Changed files shown above.

Options:
  [1] Pull now  — git pull origin main
  [2] Skip      — continue with current local skills
  [3] View diff — show git log origin/main..HEAD and git diff summary
```

Wait for the user to choose. Do not pull automatically.

---

## Step 3a — Pull (option 1)

```bash
git pull origin main
```

After pulling:
1. Re-run the audit: `python3 skills/kotlin-multiplatform-audit/scripts/audit_skills_repo.py .`
2. Re-run tests: `python3 -m pytest tests/ -q`
3. Report: skills updated to v<new_version>, audit clean / failing, tests pass / fail.

---

## Step 3b — Skip (option 2)

Acknowledge and continue. Remind the user that the local skills may be missing recent
fixes or new patterns. Log a session note:

```
⚠️  Running with stale skills — local v<local> is behind remote v<remote>.
   Some patterns or fixes added since v<local> may not apply.
```

---

## Step 3c — View diff (option 3)

```bash
git log HEAD..origin/main --oneline -- skills/ agents/ commands/ scripts/
```

Show the log. Then re-present the Pull / Skip choice.

---

## Notes

- This command is non-destructive: it only reads from remote and, on option 1, fast-forwards
  the local branch via `git pull` (no force-push, no rebase).
- If the working tree has uncommitted changes, `git pull` will fail. Report this and ask the
  user to commit or stash before pulling.
- If `git pull` would create a merge conflict, stop and ask the user to resolve it manually.
