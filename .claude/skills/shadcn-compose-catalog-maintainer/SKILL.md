---
name: shadcn-compose-catalog-maintainer
description: >
  Keeps this repo's own catalog app (app/shared/.../catalog/docs/*Doc.kt) complete and
  consistent: every component needs a ComponentDoc with a real description, usage code,
  at least one example with a working preview, a screenshot golden, a docs/components.md
  row, and (unless it's genuinely original to this library) a verified referenceUrl to
  the real shadcn/ui docs page it's modeled on. Project-local -- not part of the vendored
  kmp-agent-skills set, since this convention is specific to this repo's own catalog, not
  a generic KMP concern.
license: Apache-2.0
metadata:
  author: shadcn-compose (project-local)
  last-updated: '2026-08-04'
  keywords:
    - catalog
    - component doc
    - ComponentDoc
    - ComponentExample
    - showcase
    - preview
    - reference url
    - shadcn/ui reference
    - component-metadata
    - components.md
    - screenshot golden
    - new component checklist
---

## When to use this skill

Use it whenever you:
- add a new component to `shadcn/core` and need to wire up its catalog page
- aren't sure whether an existing component's catalog page is complete
- want to check the whole catalog for gaps (missing examples, missing reference links,
  stale docs) in one pass

Do NOT use it for:
- the library's own README/GETTING_STARTED/architecture docs — see `kmp-project-docs-maintainer`
- the kmp-agent-skills repo's own skill docs — not relevant to this project

## The convention

Every `CatalogEntry` in `CatalogRegistry.kt` (except the five Getting Started prose
pages: introduction/installation/theming/dark-mode/typography) needs, in
`app/shared/src/commonMain/kotlin/io/github/ronjunevaldoz/shadcncompose/catalog/docs/`:

1. A `<Name>Doc.kt` file declaring `val <name>Doc = ComponentDoc(...)`, with:
   - `id` matching the registry entry's id
   - `referenceUrl` — the real `https://ui.shadcn.com/docs/components/base/<slug>` page
     this component is modeled on. **Verify it live** (fetch the URL, confirm it's not a
     404) before adding it — never guess a slug from the component name. Leave it
     unset (defaults to `null`) only for components genuinely original to this library
     (currently: `chip`, `stepper`, `shimmer`, `scroll-fade` — see
     `NO_REAL_REFERENCE_EQUIVALENT` in the audit script). shadcn/ui's own registry
     changes over time (components get added, renamed, or migrated between registries)
     — re-verify rather than trusting a stale note if it's been a while.
   - a non-empty `description`
   - a non-empty `usageCode` showing the minimal real usage
   - at least one `ComponentExample` with a real (non-empty) `preview` composable
2. That `val` listed in `ComponentDocs.kt`'s `componentDocsById` — an unwired Doc.kt is
   dead code, unreachable from the catalog UI.
3. A Roborazzi screenshot golden in `shadcn/core/src/jvmTest/snapshots/` (light + dark)
   — this is what `docs/component-metadata.json`'s `previews` array links to.
4. A row in `docs/components.md`'s own category table (`ShadcnX` | use case | keywords).

## Step 1 — Run the audit

```bash
python3 scripts/audit_catalog_showcase.py
```

Exit 0 and "clean" means every registry component has a doc, examples, a preview, a
screenshot golden, a `components.md` row, and a reference URL (or a documented
exception). Exit 1 lists each finding with the specific file/id — fix them one at a
time, re-running after each fix.

This is a static/regex check on source text, not a real Kotlin parser — false positives
are possible for unusual formatting. If a finding looks wrong, read the actual `Doc.kt`
file before assuming the checker is right.

## Step 2 — Fix findings

| Finding | Fix |
|---|---|
| `[MISSING DOC]` | Create `<Name>Doc.kt` following an existing one as a template (e.g. `ButtonDoc.kt`) |
| `[UNWIRED DOC]` | Add the `val` to `ComponentDocs.kt`'s `componentDocsById` list |
| `[EMPTY DESCRIPTION]` / `[EMPTY USAGE CODE]` | Fill in the field — one real sentence / one real minimal snippet, not a placeholder |
| `[NO EXAMPLES]` | Add at least one `ComponentExample` with a real `preview = { ... }` body |
| `[EMPTY PREVIEW]` | The `preview` lambda body is blank — render the actual component |
| `[NO SCREENSHOT GOLDEN]` | Add a `*ScreenshotTest.kt` under `shadcn/core/src/jvmTest/`, then `./gradlew :shadcn:core:recordRoborazziJvm --tests "*<Name>ScreenshotTest*"` |
| `[NOT IN components.md]` | Add a row to the right category table in `docs/components.md` |
| `[NO REFERENCE URL]` | Fetch `https://ui.shadcn.com/docs/components/base/<guess>` and confirm it's real (not 404) before adding `referenceUrl`; if genuinely no real equivalent exists, add the id to `NO_REAL_REFERENCE_EQUIVALENT` in `scripts/audit_catalog_showcase.py` with a one-line reason |

## Step 3 — Regenerate derived docs

After any Doc.kt change (new component, new example, new screenshot):

```bash
python3 scripts/normalize_preview_images.py    # must run first
python3 scripts/generate_component_metadata.py
```

This keeps `docs/component-metadata.json` (the machine-readable cross-library
comparison index — see `docs/component-metadata.md`) in sync, including the
`referenceUrl` field this skill maintains.

## Step 4 — Verify

```bash
./gradlew :app:shared:compileKotlinJvm ktlintCheck detekt
python3 scripts/audit_catalog_showcase.py
```

## Notes

- Never guess a `referenceUrl`. shadcn/ui's real docs structure has changed before
  (e.g. the whole registry moved under a `/base/` prefix, and components once
  exclusive to `vercel/ai-elements` — Bubble, Message, Marker, Attachment,
  MessageScroller — were later absorbed into shadcn/ui's own base registry). Fetch and
  confirm, don't pattern-match from memory.
- `docs/components.md`'s row and the catalog's own category (`CatalogCategory`) don't
  have to match a real shadcn/ui category — this repo's categories are its own UI
  grouping, documented in `component-metadata.md`'s `family` field for the
  parity-comparison grouping instead.
