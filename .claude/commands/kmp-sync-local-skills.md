# /kmp-sync-local-skills

Sync the latest `kmp-agent-skills` release into the local assistant skill bundles on this Mac:

- `~/.claude/skills`
- `~/.codex/skills`
- `~/.gemini/skills`

Use this when the repo has been released and you want your local assistants to see the same skill version immediately.

## Usage

```bash
bash /Users/ronvaldoz/Documents/kmp-agent-skills/scripts/sync-local-assistant-skills.sh
```

Dry run:

```bash
bash /Users/ronvaldoz/Documents/kmp-agent-skills/scripts/sync-local-assistant-skills.sh --dry-run
```

## Behavior

- Updates skills only
- Does not copy `commands/`
- Creates a backup of any existing local install before replacing it
- Keeps the local assistant bundles aligned with the released repo state

## When to use

- After a new release is pushed
- After pulling a newer tag into the repo clone
- When Claude, Codex, and Gemini need the same skill set on this machine
