# Project-Specific Commands/Agents/Skills — Source of Truth

Part of `kmp-expert`. Load this file when working on: project-specific commands/agents/skills — source of truth.

---

When a user asks for a custom command, agent, skill, or hook for **their own project**
(not one of this collection's own), or an agent decides one is needed — author it at a
project-owned source location first, then deploy a copy into `.claude/` for Claude Code
to actually discover it. Never author directly into `.claude/agents/*.md`,
`.claude/commands/*.md`, or `.claude/skills/*/` as the only copy.

**The model to mirror is this very repo**: `kmp-agent-skills` itself keeps project-owned
agent assets at the repo root, with runtime copies generated separately. A consumer
project should do the same for its *own* custom artifacts so the source stays versioned
next to the app code, reviewable in a normal PR diff, and portable if the project ever
needs to regenerate or move its `.claude/` setup.

Layout — flat, `<name>` is the artifact's own name, never the app/project name:
```
<project root>/
├── agents/<agent-name>.md               ← source
├── rules/<rule-name>.md                 ← source
├── commands/<command-name>.md           ← source
├── skills/<skill-name>/SKILL.md         ← source — project-owned CUSTOM skills only,
│                                           never bundled kmp-agent-skills content
├── hooks/<hook-name>.sh                 ← source
├── docs/reference/ai-collaboration.md   ← canonical cross-agent policy
├── docs/reference/agent-catalog.md      ← canonical model-tier mapping
├── AGENTS.md                            ← optional thin bootstrap
├── CLAUDE.md                            ← optional thin bootstrap
├── GEMINI.md                            ← optional thin bootstrap
├── .agents/
│   └── skills/<skill-name>/             ← DEPLOYED — bundled kmp-agent-skills + mirrored
│                                           custom skills; the cross-client target, read
│                                           by any agentskills.io-compliant client
└── .claude/
    ├── AGENTS.md                        ← deployed routing/context
    ├── commands/<command-name>.md       ← deployed copy
    ├── skills/<skill-name>/             ← deployed copy, mirrors .agents/skills/
    └── settings.json                    ← permissions + hook wiring
```

Thin entrypoints (`AGENTS.md`, `CLAUDE.md`, `GEMINI.md`) should point to the canonical
docs, keep only startup-critical guardrails, and avoid becoming the only copy of
project policy. `docs/reference/agent-catalog.md` owns provider-neutral model tiers and
provider-specific mappings. Do not hardcode stale provider model names across every
agent file when one canonical catalog can carry that mapping.

`rules/` exists for optional project-specific rule snippets or assistant overlays that
should stay project-owned even if only one assistant consumes them today. Do **not**
copy the same policy text from `docs/reference/ai-collaboration.md` into `rules/`.
Keep the explanation canonical in `docs/reference/ai-collaboration.md`; use `rules/`
only when the project genuinely needs short assistant-facing overlays in addition to
that canonical doc.

Use this split consistently:

- `docs/*` answers "how is this project designed?"
- `skills/*` answers "how should an agent work in this repo?"

If a repo-local skill starts retelling architecture docs, stop and move the stable
design guidance back into `docs/*`.

If a project has no custom artifacts yet, still scaffold these folders with placeholder
README files. Empty-but-present source locations make future additions land in the
right place instead of drifting straight into `.claude/`.

**Never nest a project artifact under an app/project-name folder** (e.g.
`skills/<app-name>/<skill-name>/`). Verified against the real, official skill
anatomy (`anthropic-skills:skill-creator`'s own documented convention): a skill's
folder is named after what the skill *does*, flat under `skills/` — this is also how
`.claude/skills/` is actually scanned. If a project-owned skill's name might collide
with one of this collection's 64, resolve it by giving the project-owned skill a more
specific name (e.g. `awaken-ecs-conventions`, not `ecs`) — not by nesting it under an
app-name folder, which isn't a real convention Claude Code (or this collection)
recognizes.

Deploy the copy after every edit to the source — a stale `.claude/` copy that's drifted
from its project-owned source is worse than no source at all, since it looks authoritative
but silently isn't. Simple `cp`/`rsync` is enough; no need for a dedicated script unless
the project has many artifacts to keep in sync. If the project uses
`update-consumer-skills.sh`, that sync path should copy project-owned custom skills from
`skills/<name>/` into `.claude/skills/<name>/` as part of the normal refresh.

**Real gap this closes**: a review of a real KMP game-engine project found two custom
agent definitions (`ecs-dev`, `game-framework-dev`) authored directly into
`.claude/agents/` with no project-owned source anywhere — meaning the only copy of that
authoring work lived in a directory this rule now treats as deploy-only.

**Audited automatically**: `kmp-audit`'s `_detect_project_skill_standards`
checks every `skills/<name>/` folder it finds against the real skill anatomy — SKILL.md
present, opening YAML frontmatter with `name`/`description`, body under ~500 lines unless
a `references/` subdirectory exists. It also checks that the deployed `.claude/skills/`
copy exists and is not stale. Run it any time a project skill is added or edited, not
just once at creation.

