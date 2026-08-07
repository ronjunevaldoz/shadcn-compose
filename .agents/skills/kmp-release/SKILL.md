---
name: kmp-release
description: >
  End-to-end KMP library and app release pipeline. Covers versioning strategy
  (gradle.properties as source of truth), Maven Central publishing via the
  vanniktech plugin, GPG signing, Sonatype Central Portal, changelog generation
  with git-cliff and Conventional Commits, GitHub Release creation, secrets
  management patterns, and — for app targets — deriving platform-native version
  fields (Android versionCode, iOS CFBundleVersion) from a single semver source.
  Distinct from the CI skill (which owns workflow YAML) and the xcframework-spm
  skill (which owns SPM binary distribution).
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-04'
  keywords:
    - Maven Central
    - publish
    - release
    - versioning
    - semantic versioning
    - gradle.properties
    - vanniktech
    - Sonatype
    - GPG signing
    - git-cliff
    - changelog
    - Conventional Commits
    - GitHub Release
    - Doppler
    - secrets
    - version bump
    - versionCode
    - versionName
    - CFBundleVersion
    - CFBundleShortVersionString
    - Play Store release
    - Play Console upload rejected
    - App Store build number
---

## When to Use This Skill

Use when you need to:
- Publish a KMP library to Maven Central for the first time
- Set up a repeatable versioning + changelog + release workflow
- Wire Maven credentials into CI without exposing them as plain text
- Generate a structured CHANGELOG.md from git history
- Create a GitHub Release with auto-generated release notes
- Decide between secrets management approaches (GitHub Secrets / Doppler / env vars)
- Cut a project release end to end, including version bump, tag, changelog, and GitHub Release

**Trigger keywords:** publish to Maven Central, Maven publish, release library, release project,
cut release, ship version, versioning, semantic versioning, bump version, gradle.properties
version, vanniktech, Sonatype, Central Portal, GPG signing, git-cliff, changelog,
conventional commits, GitHub Release, release pipeline, publish KMP library, release workflow,
secrets management publish, alpha release, beta release, release candidate, pre-release,
snapshot, development version, GitHub Packages, promote to stable, version suffix.

**Freshness rule:** Sonatype Central Portal API and the vanniktech plugin change frequently —
recheck the [vanniktech plugin releases](https://github.com/vanniktech/gradle-maven-publish-plugin/releases)
and [Central Portal docs](https://central.sonatype.com/publishing) before scaffolding a new publish setup.

**Does not own:**
- GitHub Actions workflow YAML → `kmp-ci-github-actions`
- XCFramework build + SPM binary target → `kmp-xcframework-spm`
- App Store / Play Store submission → out of scope for this skill

---

## Recommendation First

Default to: **`gradle.properties` VERSION → vanniktech plugin → `ORG_GRADLE_PROJECT_*` env vars → git-cliff changelog → `gh release create`**.

Why:
- `gradle.properties` keeps version as a single editable line — no Gradle task, no plugin needed to bump it
- `ORG_GRADLE_PROJECT_*` env vars are mapped automatically by Gradle — no `-P` flags in scripts, no credential leaks
- vanniktech's `com.vanniktech.maven.publish` is the de-facto standard for KMP Central publishing
- git-cliff + Conventional Commits turns commit history into a structured CHANGELOG automatically
- `gh release create` wires the GitHub Release to the git tag in one command

Use a secrets manager (Doppler, 1Password, AWS Secrets Manager) only if your team already has one. GitHub Secrets is sufficient for most projects.

---

## 1. Versioning

Full content: `references/1-versioning.md`.

## 2. Maven Central Publishing

Full content: `references/2-maven-central-publishing.md`.

## 3. Changelog with git-cliff

### `cliff.toml` (project root)

```toml
[changelog]
header = "# Changelog\n"
body = """
{% if version %}\
## [{{ version | trim_start_matches(pat="v") }}] — {{ timestamp | date(format="%Y-%m-%d") }}
{% else %}\
## [Unreleased]
{% endif %}\
{% for group, commits in commits | group_by(attribute="group") %}
### {{ group }}
{% for commit in commits %}
- {% if commit.scope %}**{{ commit.scope }}:** {% endif %}{{ commit.message | upper_first }}\
  {% if commit.breaking %} ⚠ BREAKING{% endif %}
{%- endfor %}
{% endfor %}\n
"""
trim = true

[git]
conventional_commits = true
filter_unconventional = true
commit_parsers = [
  { message = "^feat",     group = "Features" },
  { message = "^fix",      group = "Bug Fixes" },
  { message = "^refactor", group = "Refactoring" },
  { message = "^perf",     group = "Performance" },
  { message = "^docs",     group = "Documentation" },
  { message = "^build",    group = "Build" },
  { message = "^ci",       group = "CI" },
  { message = "^chore",    skip = true },
  { message = "^test",     skip = true },
]
filter_commits = true
tag_pattern = "v[0-9].*"
sort_commits = "oldest"
```

### Commands

```bash
# Update CHANGELOG.md for a new tag
git-cliff --tag v1.2.0 --output CHANGELOG.md

# Generate release notes only (for GitHub Release body)
git-cliff --tag v1.2.0 --unreleased --strip all

# Full history regeneration
git-cliff --output CHANGELOG.md
```

Install: `brew install git-cliff`

Requires Conventional Commits (`feat:`, `fix:`, `refactor:`, etc.) — enforce with the `code-quality` skill's commit-lint setup.

---

## 4. GitHub Release

```bash
# Generate release notes from git-cliff
NOTES=$(git-cliff --tag v1.2.0 --unreleased --strip all)

# Create the GitHub Release
gh release create v1.2.0 \
  --title "v1.2.0" \
  --notes "$NOTES" \
  --verify-tag
```

`--verify-tag` ensures the tag exists before creating the release. Push the tag before running this:

```bash
git tag v1.2.0
git push origin v1.2.0
gh release create v1.2.0 ...
```

For libraries that also ship an XCFramework: attach the `.zip` artifact to the release and update `Package.swift`. See `kmp-xcframework-spm` for the full SPM binary target workflow.

---

## 5. Local publish script

A reproducible local publish script eliminates "works on my machine" publish failures.

```bash
#!/usr/bin/env bash
# scripts/publish.sh — publish to Maven Central
# Usage: ./scripts/publish.sh [patch|minor|major]
set -euo pipefail

BUMP="${1:-patch}"
PROPS="gradle.properties"

# Read current version
CURRENT=$(grep '^VERSION=' "$PROPS" | cut -d= -f2)
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT"

case "$BUMP" in
  patch) NEW_VERSION="$MAJOR.$MINOR.$((PATCH + 1))" ;;
  minor) NEW_VERSION="$MAJOR.$((MINOR + 1)).0" ;;
  major) NEW_VERSION="$((MAJOR + 1)).0.0" ;;
  *) echo "Usage: $0 [patch|minor|major]"; exit 1 ;;
esac

echo "Bumping $CURRENT → $NEW_VERSION"

# Bump version
# cross-platform: BSD (macOS) and GNU (Linux) sed differ on -i syntax
if sed --version 2>/dev/null | grep -q GNU; then
    sed -i "s/^VERSION=.*/VERSION=$NEW_VERSION/" "$PROPS"
else
    sed -i '' "s/^VERSION=.*/VERSION=$NEW_VERSION/" "$PROPS"
fi

# Run publish (credentials injected via env or secrets manager)
./gradlew publishAllPublicationsToMavenCentralRepository --no-configuration-cache

# Commit, tag, push
git add "$PROPS"
git commit -m "chore(release): bump version to $NEW_VERSION"
git tag "v$NEW_VERSION"
git push origin main "v$NEW_VERSION"

# GitHub Release
NOTES=$(git-cliff --tag "v$NEW_VERSION" --unreleased --strip all)
gh release create "v$NEW_VERSION" --title "v$NEW_VERSION" --notes "$NOTES" --verify-tag

echo "Released v$NEW_VERSION"
```

---

## 6. Release checklist

Before tagging:
- [ ] All tests pass locally (`./gradlew check`)
- [ ] `CHANGELOG.md` is up to date (`git-cliff --tag vX.Y.Z --output CHANGELOG.md`)
- [ ] `VERSION` in `gradle.properties` matches the intended tag
- [ ] No `-SNAPSHOT` suffix in `VERSION`
- [ ] Maven credentials are available in the environment
- [ ] GPG key is loaded (`gpg --list-secret-keys`)

After tagging:
- [ ] GitHub Release created with release notes
- [ ] Artifact visible on [central.sonatype.com](https://central.sonatype.com) (allow ~10 min propagation)
- [ ] XCFramework zip attached to the release and `Package.swift` checksum updated (if shipping SPM)

**App target with an Android/iOS/store target** (in addition to the checks above):
- [ ] `versionCode` (Android) is derived from the semver source, not a literal integer — grep for `versionCode\s*=\s*\d`
- [ ] `versionCode` for this build is strictly higher than the last **accepted** Play Console upload
- [ ] `CFBundleVersion` (iOS) is monotonic for this `CFBundleShortVersionString` — not reused from a prior submission

---

## Common Anti-Patterns

| Anti-pattern | Rule |
|---|---|
| Fallback version string in `build.gradle.kts` (`?: "0.1.0"`) | Use `error()` — a missing VERSION should fail loudly |
| Publishing `-SNAPSHOT` or any pre-release suffix to Maven Central | Central rejects snapshots and pre-release suffixes; use GitHub Packages for alpha/beta/rc distribution |
| Credentials as `-P` flags (`-PmavenCentralUsername=...`) | Use `ORG_GRADLE_PROJECT_*` env vars — Gradle maps them automatically, no flags needed |
| Version bump in CI | Version is a publisher decision; bump in `gradle.properties` before the CI publish job |
| Committing credentials to `gradle.properties` or `.env` | Always gitignore `.env`; store credentials in GitHub Secrets or a secrets manager |
| Skipping `--no-configuration-cache` | vanniktech plugin is incompatible with configuration cache; omitting this flag causes silent failures |
| Hardcoding Android `versionCode` (or any platform-native version field) as a static literal | Derive it from the single semver source (`major * 1_000_000 + minor * 1_000 + patch`) — a static value builds and runs fine locally but causes a hard Play Console rejection on the *second* upload |
| Deriving iOS `CFBundleVersion` purely from semver | Use a monotonic CI build number instead — `CFBundleVersion` must increase per build submitted for the same `CFBundleShortVersionString`, which pure semver derivation breaks on a re-submitted patch |

---

## Testing the Release Pipeline

The release pipeline itself has no unit-testable Kotlin code — it is a build script and CI workflow. Test it by validating the inputs and outputs at each stage:

| What to verify | How |
|---|---|
| `VERSION` is read correctly | `./gradlew printVersion` — add a task: `tasks.register("printVersion") { doLast { println(project.findProperty("VERSION")) } }` |
| Publish runs without credentials | `./gradlew publishAllPublicationsToMavenCentralRepository --dry-run` — confirms task graph without uploading |
| git-cliff produces valid output | `git-cliff --tag vX.Y.Z --unreleased --strip all` — inspect the output before creating a release |
| `ORG_GRADLE_PROJECT_*` mapping works | Set vars locally and run `./gradlew publishToMavenLocal` — verifies credential injection without hitting Central |
| Changelog is up to date | `git-cliff --output CHANGELOG.md && git diff CHANGELOG.md` — should show only the new section |

**Freshness rule:** run `./gradlew publishToMavenLocal` before every real publish to catch configuration drift early.

---

## References

Full implementation content lives in `references/*.md`: `1-versioning`,
`2-maven-central-publishing`. Load the specific file named in the pointer under its
matching heading above, not all of them.

---

## Related Skills

- `kmp-ci-github-actions` — GitHub Actions workflow YAML that triggers the publish job on tag push
- `kmp-xcframework-spm` — XCFramework build + SPM binary target; attach the zip to the GitHub Release created here
- `kmp-code-quality` — commit-lint enforcement for Conventional Commits (required for git-cliff)
- `kmp-flavor-environment` — dev/staging/prod config; keep release credentials separate from app config

---

## Output Style

When setting up a release pipeline:

1. **Versioning** — confirm `gradle.properties` VERSION is the source of truth
2. **Plugin** — add vanniktech to version catalog + convention plugin
3. **Credentials** — identify the secrets approach (GitHub Secrets / Doppler / other) and show the `ORG_GRADLE_PROJECT_*` mapping
4. **Changelog** — scaffold `cliff.toml`; confirm Conventional Commits are in use
5. **Release script** — generate `scripts/publish.sh` for local use
6. **CI integration** — point to `kmp-ci-github-actions` for the workflow YAML
7. **Checklist** — present the pre/post-release checklist

Never generate credentials or keys. If GPG setup is needed, give the commands the user runs themselves.

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Split "1. Versioning" and "2. Maven Central Publishing" out of SKILL.md into `references/*.md`, leaving pointer stubs plus a new References section. SKILL.md drops from 590 to 327 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Part of the same backlog cleanup as the other 15 skills fixed alongside it (KI-008). |
| 2026-08-01 | Changed the library-version example from `1.0.0` to `0.1.0` — contradicted `library-publishing`'s own pre-1.0 policy (a fresh library hasn't earned a `1.0.0` stability promise yet). Apps are unaffected — no public-API stability promise applies there. |
| 2026-07-04 | Added "App targets: deriving platform-native version fields from one semver source" — Android versionCode, iOS CFBundleVersion, desktop package versions, all derived (never hardcoded) from the single semver source. New checklist items and anti-patterns for the hardcoded-versionCode silent trap (filed as GitHub issue #2). |
| 2026-06-27 | Replaced id("...") with alias(libs.plugins.*) in both the convention plugin and consuming modules. Added convention plugin alias to libs.versions.toml. |
| 2026-06-26 | Bumped vanniktech maven-publish plugin base version to 0.37.0. |
| 2026-06-24 | Added explicit `release project` / `cut release` / `ship version` trigger keywords so project release requests route here instead of the consumer changelog agent. |
| 2026-06-23 | Initial release — versioning, Maven Central, git-cliff, GitHub Release, local publish script, anti-patterns. |
