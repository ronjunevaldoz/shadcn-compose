#!/usr/bin/env python3
"""Audits the catalog app's showcase convention: every non-"Getting Started"
CatalogRegistry entry must have a ComponentDoc with a real description, usage code,
at least one ComponentExample with a preview, at least one screenshot golden
(so docs/component-metadata.json can link a real image), and a referenceUrl pointing
at the real shadcn/ui docs page it's modeled on (unless it's genuinely original to
this library -- see NO_REAL_REFERENCE_EQUIVALENT). Also flags Doc.kt files that exist
but aren't wired into componentDocsById, and components missing from docs/components.md's
own catalog table.

Exit code 0 on a clean catalog, 1 if any finding is reported.

Usage: python3 scripts/audit_catalog_showcase.py
"""
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from generate_component_metadata import find_screenshots  # noqa: E402 -- reuse the one verified prefix-override table, don't duplicate it

ROOT = Path(__file__).resolve().parent.parent
CATALOG_REGISTRY = ROOT / "app/shared/src/commonMain/kotlin/io/github/ronjunevaldoz/shadcncompose/catalog/CatalogRegistry.kt"
DOCS_DIR = ROOT / "app/shared/src/commonMain/kotlin/io/github/ronjunevaldoz/shadcncompose/catalog/docs"
COMPONENT_DOCS_FILE = DOCS_DIR / "ComponentDocs.kt"
SNAPSHOTS_DIR = ROOT / "shadcn/core/src/jvmTest/snapshots"
COMPONENTS_MD = ROOT / "docs/components.md"

GETTING_STARTED_IDS = {"introduction", "installation", "theming", "dark-mode", "typography"}

# Verified live against ui.shadcn.com (2026-08-04): these ids have no real shadcn/ui
# equivalent page (confirmed 404, not just "didn't check") -- this library's own
# additions (Chip, Stepper) or utility modifiers with no dedicated component page.
# Update this set only after checking the real site directly, never by assumption.
NO_REAL_REFERENCE_EQUIVALENT = {"chip", "stepper", "shimmer", "scroll-fade"}


def parse_registry_ids():
    """`\\s*` after `CatalogEntry\\(` tolerates both the single-line form and the
    multi-line form ktlint wraps a call into once its args exceed the max line length
    (e.g. once `isNew = true` pushes it over) -- see the matching note in
    generate_component_metadata.py's parse_catalog_entries()."""
    text = CATALOG_REGISTRY.read_text()
    ids = set()
    for m in re.finditer(r'CatalogEntry\(\s*id\s*=\s*"([^"]+)"', text):
        if m.group(1) not in GETTING_STARTED_IDS:
            ids.add(m.group(1))
    return ids


def parse_doc_val_names():
    """Every `val xDoc = ComponentDoc(` binding name, across all *Doc.kt files."""
    names = set()
    for doc_file in DOCS_DIR.glob("*Doc.kt"):
        if doc_file.name == "ComponentDoc.kt":
            continue
        text = doc_file.read_text()
        for m in re.finditer(r"val (\w+)\s*=\s*\n?\s*ComponentDoc\(", text):
            names.add(m.group(1))
    return names


def parse_wired_doc_names():
    text = COMPONENT_DOCS_FILE.read_text()
    return set(re.findall(r"^\s*(\w+),\s*$", text, re.MULTILINE))


def parse_doc_entries():
    """id -> {title, referenceUrl, description, usageCode, example_count, empty_previews, file}"""
    entries = {}
    for doc_file in DOCS_DIR.glob("*Doc.kt"):
        if doc_file.name == "ComponentDoc.kt":
            continue
        text = doc_file.read_text()
        id_match = re.search(r'id\s*=\s*"([^"]+)"', text)
        if not id_match:
            continue
        component_id = id_match.group(1)
        title_match = re.search(r'title\s*=\s*"([^"]+)"', text)
        reference_match = re.search(r'referenceUrl\s*=\s*"([^"]+)"', text)
        desc_match = re.search(r'description\s*=\s*"([^"]*)"', text)
        usage_match = re.search(r'usageCode\s*=\s*\n?\s*"""(.*?)"""', text, re.DOTALL)
        example_count = len(re.findall(r"ComponentExample\(", text))
        preview_bodies = re.findall(r"preview\s*=\s*\{(.*?)\n\s*\},\s*\)", text, re.DOTALL)
        entries[component_id] = {
            "title": title_match.group(1) if title_match else doc_file.name.removesuffix("Doc.kt"),
            "reference_url": reference_match.group(1) if reference_match else None,
            "description": (desc_match.group(1) if desc_match else "").strip(),
            "usage_code": (usage_match.group(1) if usage_match else "").strip(),
            "example_count": example_count,
            "empty_previews": [i for i, body in enumerate(preview_bodies) if not body.strip()],
            "file": doc_file.name,
        }
    return entries


def _normalize(s):
    return re.sub(r"[^a-z0-9]", "", s.lower())


def component_referenced_in_components_md(component_id, title):
    text_norm = _normalize(COMPONENTS_MD.read_text())
    return _normalize(title) in text_norm or _normalize(component_id) in text_norm


def main():
    findings = []

    registry_ids = parse_registry_ids()
    doc_val_names = parse_doc_val_names()
    wired_names = parse_wired_doc_names()
    doc_entries = parse_doc_entries()

    # 1. Every registry id (excluding Getting Started) must have a ComponentDoc.
    doc_ids = set(doc_entries.keys())
    for missing_id in sorted(registry_ids - doc_ids):
        findings.append(f"[MISSING DOC] CatalogRegistry has '{missing_id}' but no ComponentDoc declares that id")

    # 2. Every Doc.kt val must be wired into componentDocsById (dead/unreachable doc otherwise).
    for unwired in sorted(doc_val_names - wired_names):
        findings.append(f"[UNWIRED DOC] '{unwired}' is declared but not listed in componentDocsById")

    # 3. Per-doc completeness.
    for component_id, entry in sorted(doc_entries.items()):
        if component_id not in registry_ids:
            continue  # stale doc for a removed registry entry -- caught by a different check if it matters
        if not entry["description"]:
            findings.append(f"[EMPTY DESCRIPTION] {entry['file']} ('{component_id}')")
        if not entry["usage_code"]:
            findings.append(f"[EMPTY USAGE CODE] {entry['file']} ('{component_id}')")
        if entry["example_count"] == 0:
            findings.append(f"[NO EXAMPLES] {entry['file']} ('{component_id}') has zero ComponentExample entries")
        if entry["empty_previews"]:
            findings.append(
                f"[EMPTY PREVIEW] {entry['file']} ('{component_id}') has "
                f"{len(entry['empty_previews'])} ComponentExample(s) with an empty preview body"
            )
        if not find_screenshots(component_id):
            findings.append(f"[NO SCREENSHOT GOLDEN] '{component_id}' has no matching file in {SNAPSHOTS_DIR.relative_to(ROOT)}")
        if not entry["reference_url"] and component_id not in NO_REAL_REFERENCE_EQUIVALENT:
            findings.append(
                f"[NO REFERENCE URL] {entry['file']} ('{component_id}') has no referenceUrl -- either add the "
                f"real ui.shadcn.com/docs/components/base/<slug> link (verify it live, don't guess) or add "
                f"'{component_id}' to NO_REAL_REFERENCE_EQUIVALENT in this script if it genuinely has none"
            )

    # 4. Cross-check docs/components.md mentions each registry component (best-effort substring match).
    for component_id in sorted(registry_ids):
        entry = doc_entries.get(component_id)
        if entry and not component_referenced_in_components_md(component_id, entry["title"]):
            findings.append(f"[NOT IN components.md] '{component_id}' has a ComponentDoc but no matching row in docs/components.md")

    if not findings:
        print("Catalog showcase: clean -- every component has a doc, examples, a preview, a screenshot golden, and a components.md row.")
        return 0

    print(f"Catalog showcase: {len(findings)} finding(s)\n")
    for f in findings:
        print(f"  {f}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
