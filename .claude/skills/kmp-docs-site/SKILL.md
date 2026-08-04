---
name: kmp-docs-site
description: >
  Sets up a public developer-guide website (getting started, guides, code examples, API
  reference) for a published Kotlin Multiplatform library, deployed to GitHub Pages.
  Covers MkDocs Material for hand-written guide content, Dokka HTML for auto-generated
  API reference, a compiler-verified snippet-extraction technique so code examples can't
  drift stale, and the CI deploy workflow. Library-only — does not apply to app projects,
  which have no external consumers to write a developer guide for. Distinct from
  kmp-library-publishing (owns the Maven Central pipeline) and
  kmp-project-docs-maintainer (owns internal, non-public planning docs).
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-11'
  keywords:
    - GitHub Pages
    - developer guide
    - docs site
    - MkDocs
    - MkDocs Material
    - Dokka
    - dokkaGenerate
    - API reference site
    - documentation website
    - gh-deploy
    - library documentation
    - code examples
    - snippet extraction
---

## When to Use This Skill

Use when you need to:
- Publish a getting-started guide, concept pages, and code examples for a Kotlin
  Multiplatform **library** that has external consumers
- Generate a browsable API reference site (not just the Javadoc jar required for Maven
  Central) from KDoc
- Keep code examples in the guide from silently drifting out of sync with the real API

**Do not use this skill for an app project.** An app scaffolded via
`kmp-feature-scaffold` has no external consumers reading an installation
guide or API reference — there is nothing to publish a developer guide *for*. This skill
only applies to a project that is itself a published library.

**Do not scaffold this for a small, single-purpose library either.** A library with a
handful of functions and one obvious usage pattern is well served by a good README. Reach
for a full site once the library has enough surface area that a flat README stops being
navigable — multiple components/modules, several distinct usage patterns, or genuine
external adoption asking for more (this repo's own `design-system-extended`'s 28
components, or a component library the size of shadcn-compose's 70+, are the right shape
for this; a single-function utility library is not).

**Trigger keywords:** GitHub Pages, developer guide, docs site, MkDocs, MkDocs Material,
Dokka HTML, API reference site, documentation website, gh-deploy, library documentation
site, publish developer docs, docs.<library> site.

**Freshness rule:** MkDocs Material's recommended CI workflow and Dokka's plugin
id/task names change between major versions (Dokka 2.x's DGP v2 renamed several tasks
from 1.x) — recheck the
[MkDocs Material publishing guide](https://squidfunk.github.io/mkdocs-material/publishing-your-site/)
and the [Dokka migration guide](https://kotlinlang.org/docs/dokka-migration.html) before
pinning versions or copying setup code from this skill.

---

## Recommendation First

Default to **MkDocs Material for the hand-written guide, Dokka HTML for the API
reference, deployed separately** — not one tool trying to do both, and not published from
the same `docs/` folder `kmp-project-docs-maintainer` uses for internal
planning docs.

Why:
- MkDocs Material's own deploy mechanism (`mkdocs gh-deploy --force`) is simpler and more
  current than routing through a generic Pages-deploy action — verified against MkDocs
  Material's own publishing guide, not assumed
- Dokka generates the API reference *from KDoc that already has to be correct* — hand
  duplicating parameter/return docs into guide prose is a second source of truth that
  will drift
- Reusing `docs/` (the internal project-docs-maintainer folder) as the GitHub Pages
  source would publish task notes, roadmap, and architecture decisions to the public
  site — keep the guide's source in a separate `website/` folder

---

## Prerequisites

- The project is a published (or about-to-publish) Kotlin Multiplatform **library** —
  see the scope note above
- `kmp-library-publishing` already set up (Dokka is partially configured
  there for the Javadoc jar; this skill adds the separate HTML output)
- A concrete reason to need more than a README — see the size/complexity gate above

---

## Structure

```text
website/
  docs/
    index.md              # what/why, install snippet
    getting-started.md    # one full working example, start to finish
    guides/
      theming.md
      components/
        button.md          # one page per component: props table + code example + preview image
        card.md
    migration.md           # version upgrade notes
  mkdocs.yml
.github/workflows/docs.yml  # builds + deploys on release tag
```

`website/` lives in `main` alongside the code — only the *built* site goes to the
`gh-pages` branch, via `mkdocs gh-deploy`, which handles that push for you.

---

## Step 1: MkDocs Material setup

```bash
pip install mkdocs-material
```

```yaml
# website/mkdocs.yml
site_name: <Library Name>
site_url: https://<github-user>.github.io/<repo>/
repo_url: https://github.com/<github-user>/<repo>
theme:
  name: material
  features:
    - navigation.sections
    - content.code.copy
nav:
  - Home: index.md
  - Getting Started: getting-started.md
  - Guides:
      - Theming: guides/theming.md
      - Components:
          - Button: guides/components/button.md
          - Card: guides/components/card.md
  - Migration: migration.md
  - API Reference: api/index.html
markdown_extensions:
  - pymdownx.superfences
  - pymdownx.snippets:
      base_path: [.]
```

---

## Step 2: Code examples that can't drift stale

**Never hand-paste a code example into the guide.** The same problem this repo's own
KDoc convention already solved for `@sample` (`kmp-code-quality`: "points
at an actual, compiled function elsewhere as the usage example... type-checked, can't
silently drift stale") applies here — a markdown code fence is not compiled and will
silently go stale the moment the real API changes.

**If a demo/sample/catalog module exists** (see
`kmp-project-docs-maintainer`'s conditional `docs/demos.md` guidance),
mark the exact range to extract with region comments in the real, compiled source:

```kotlin
// demo/src/commonMain/kotlin/GROUP_ID/demo/ButtonDemo.kt

// START-doc:button-basic
AppButton(onClick = { /* ... */ }) {
    AppText("Click me")
}
// END-doc:button-basic
```

Pull it into the guide with `pymdownx.snippets`' section markers:

```markdown
<!-- website/docs/guides/components/button.md -->
```kotlin
--8<-- "demo/src/commonMain/kotlin/GROUP_ID/demo/ButtonDemo.kt:button-basic"
```
```

This snippet is now a real slice of compiled code — a build failure in the demo module
is a build failure for the docs, not a silent stale example.

**If no demo module exists**, do not invent one just to satisfy this rule. Use a plain
fenced code block, and mark it explicitly so a reader knows it hasn't been compiler
verified:

```markdown
<!-- Not extracted from a compiled source — verify this compiles before publishing. -->
​```kotlin
AppButton(onClick = { }) { AppText("Click me") }
​```
```

---

## Step 3: Dokka HTML API reference

Dokka's HTML output is a **separate task from the Javadoc jar** `library-publishing`
already configures for Maven Central — same plugin family, different output.

```kotlin
// build.gradle.kts — reuses the `dokka` version catalog entry from library-publishing
plugins {
    alias(libs.plugins.dokka)  // id("org.jetbrains.dokka") — HTML output
}
```

```bash
./gradlew dokkaGenerate   # DGP v2 (Dokka 2.1.0+) — writes HTML to build/dokka/
```

Copy the generated HTML into `website/docs/api/` (or configure Dokka's output directory
to write there directly) before running `mkdocs gh-deploy`, so the API reference is
served as a subpath of the same site.

---

## Step 4: CI deploy workflow

```yaml
# .github/workflows/docs.yml
name: docs
on:
  push:
    tags:
      - 'v*'
permissions:
  contents: write
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Configure Git Credentials
        run: |
          git config user.name github-actions[bot]
          git config user.email 41898282+github-actions[bot]@users.noreply.github.com
      - uses: actions/setup-python@v5
        with:
          python-version: '3.x'
      - run: pip install mkdocs-material
      - name: Generate Dokka HTML
        run: ./gradlew dokkaGenerate
      - name: Copy API reference into site
        run: cp -R build/dokka/html/. website/docs/api/
      - name: Deploy
        working-directory: website
        run: mkdocs gh-deploy --force
```

Trigger on a release tag (matching `library-publishing`'s release flow), not every push
to `main` — the guide should track published releases, not in-progress work.

---

## Common Anti-Patterns

- publishing the developer guide from the same `docs/` folder `project-docs-maintainer`
  uses for internal planning — leaks task notes/roadmap to the public site; use a
  separate `website/` folder
- hand-pasting code examples into guide markdown instead of extracting them from a real
  compiled demo module — the example silently drifts stale the moment the API changes
  and nothing catches it
- scaffolding a demo/sample module solely to satisfy the snippet-extraction rule — if one
  doesn't already exist, use a clearly-marked, non-extracted code block instead
- duplicating parameter/return documentation from KDoc into hand-written guide prose —
  Dokka already generates this from the KDoc that has to be correct anyway; one source
  of truth
- scaffolding this skill for a small single-purpose library or an app project — see the
  scope gate in "When to Use This Skill"
- deploying the docs site on every push to `main` instead of on release tags — the public
  guide should track what's actually published, not in-progress work
- using `actions/deploy-pages` for an MkDocs Material site instead of its own
  `mkdocs gh-deploy` — extra workflow complexity for no benefit; MkDocs Material's own
  publishing guide recommends `gh-deploy` directly

---

## Testing

There's no separate unit-test layer for a docs site — validate the build and links
instead:

- `mkdocs build --strict` fails on any broken internal link or missing nav entry — run
  it in CI before `gh-deploy`, not just locally
- Every `--8<--` snippet reference resolves to a real file:region — a renamed/removed
  region comment breaks the build loudly instead of silently going stale
- `./gradlew dokkaGenerate` completes and produces HTML under `build/dokka/`
- Spot-check the deployed site's `/api/` path resolves to the copied Dokka output, not a
  404

---

## Output Style

When asked to set up a developer guide or docs site for a library, respond in this order:
1. Confirm the project is a published library with enough surface area to warrant this
   (see scope gate) — ask if unclear, don't scaffold speculatively
2. `website/` folder structure and `mkdocs.yml`
3. Snippet-extraction wiring if a demo module exists, or the explicit fallback if not
4. Dokka HTML task, separate from the existing Javadoc jar task
5. The CI deploy workflow, triggered on release tags

---

## Related Skills

- `kmp-library-publishing` — owns the Maven Central pipeline and the
  existing Javadoc jar Dokka configuration this skill's HTML output extends
- `kmp-project-docs-maintainer` — owns internal, non-public planning
  docs (`docs/`) and the conditional `docs/demos.md` guidance this skill's
  snippet-extraction step depends on
- `kmp-code-quality` — the `@sample` KDoc convention this skill's
  snippet-extraction technique applies to a public site instead of inline KDoc

---

## Changelog

| Date | Change |
|---|---|
| 2026-07-11 | Initial release — MkDocs Material guide site, Dokka HTML API reference (separate from library-publishing's Javadoc jar task), compiler-verified snippet extraction with an explicit no-demo-module fallback, and a release-tag-triggered CI deploy workflow. Verified against MkDocs Material's own publishing guide (mkdocs gh-deploy, not actions/deploy-pages) and Dokka's migration guide (DGP v2 task names) rather than assumed. Explicitly scoped to published libraries with real surface area — not apps, not small single-purpose libraries. |
