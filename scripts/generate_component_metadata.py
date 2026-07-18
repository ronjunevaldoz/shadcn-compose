#!/usr/bin/env python3
"""Generates docs/component-metadata.json: one entry per catalog component with its
category, a hand-curated "parity family" (for cross-library quality comparison), its
variant/size properties (extracted from sealed interfaces / enums in source), and
public GitHub-raw URLs to every normalized preview image for that component (see
normalize_preview_images.py -- run that first, this script only decides *which*
screenshots exist per component by reading the raw test goldens' filenames, but points
the published URLs at their docs/previews/ counterpart, which is flattened onto a solid
background and consistently margined for side-by-side comparison).

Run after any component/doc change (and after normalize_preview_images.py):
python3 scripts/generate_component_metadata.py
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CATALOG_REGISTRY = ROOT / "app/shared/src/commonMain/kotlin/io/github/ronjunevaldoz/shadcncompose/catalog/CatalogRegistry.kt"
DOCS_DIR = ROOT / "app/shared/src/commonMain/kotlin/io/github/ronjunevaldoz/shadcncompose/catalog/docs"
STYLES_DIR = ROOT / "shadcn/core/src/commonMain/kotlin/io/github/ronjunevaldoz/shadcncompose/styles"
COMPONENTS_DIR = ROOT / "shadcn/core/src/commonMain/kotlin/io/github/ronjunevaldoz/shadcncompose/components"
SNAPSHOTS_DIR = ROOT / "shadcn/core/src/jvmTest/snapshots"
OUTPUT = ROOT / "docs/component-metadata.json"

RAW_BASE = "https://raw.githubusercontent.com/ronjunevaldoz/shadcn-compose/main/docs/previews"

# Excluded: Getting Started pages have no ComponentDoc/screenshot -- they're prose, not components.
EXCLUDED_IDS = {"introduction", "installation", "theming", "dark-mode", "typography"}

CATEGORY_SLUGS = {
    "GETTING_STARTED": "getting-started",
    "CORE_PRIMITIVES": "core-primitives",
    "FORMS_AND_INPUTS": "forms-and-inputs",
    "DATA_DISPLAY": "data-display",
    "FEEDBACK": "feedback",
    "DISCLOSURE": "disclosure-and-navigation",
    "OVERLAYS": "overlays-and-navigation",
    "DATA_AND_LAYOUT": "data-and-layout",
    "AI_ELEMENTS": "ai-elements",
    "UTILS": "utils",
}

# Curated for cross-library shadcn-parity comparison, not the catalog's own UI grouping
# (CatalogCategory above) -- groups components by the *interaction pattern* they need to
# get right, since that's what a reviewer comparing two libraries actually checks
# side-by-side. User-specified anchors (field/overlay/selection); the rest follow the same
# logic, extended to cover every remaining component.
FAMILY_BY_ID = {
    # action-family -- triggers a one-shot action
    "button": "action", "button-group": "action",
    # field-family -- lets the user pick or enter a value
    "text-field": "field", "textarea": "field", "select": "field", "dropdown-menu": "field",
    "popover": "field", "input-group": "field", "input-otp": "field", "combobox": "field",
    "date-picker": "field", "slider": "field", "field": "field",
    # overlay-family -- blocking/anchored surface layered over the page
    "dialog": "overlay", "tooltip": "overlay", "sheet": "overlay", "sidebar": "overlay",
    "context-menu": "overlay", "drawer": "overlay", "alert-dialog": "overlay", "hover-card": "overlay",
    # selection-family -- manages a selected/active state among options
    "tabs": "selection", "radio-group": "selection", "switch": "selection", "command": "selection",
    "menubar": "selection", "navigation-menu": "selection", "toggle": "selection",
    "toggle-group": "selection", "checkbox": "selection", "stepper": "selection",
    # feedback-family -- communicates status/progress, not user-driven
    "alert": "feedback", "progress": "feedback", "skeleton": "feedback", "spinner": "feedback",
    "toast": "feedback",
    # disclosure-family -- expands/collapses content in place
    "collapsible": "disclosure", "accordion": "disclosure", "breadcrumb": "disclosure",
    # layout-family -- structural containers, no interaction state of their own
    "card": "layout", "separator": "layout", "aspect-ratio": "layout", "resizable": "layout",
    "scroll-area": "layout", "table": "layout", "pagination": "layout",
    # data-family -- renders a dataset/media set
    "chart": "data", "calendar": "data", "carousel": "data",
    # text-family -- typographic/inline display primitives
    "text": "text", "badge": "text", "chip": "text", "kbd": "text", "label": "text",
    "avatar": "text", "item": "text", "empty": "text",
    # ai-family -- chat/AI Elements family
    "message": "ai", "bubble": "ai", "attachment": "ai", "marker": "ai", "message-scroller": "ai",
    # utils-family -- modifiers, not standalone components
    "shimmer": "utils", "scroll-fade": "utils",
}

# Maps a component id to the Kotlin type name(s) whose enum/sealed-interface options
# describe its variant/size properties. Not every component has one (plain layout
# primitives like Table/Avatar-without-size-enum genuinely don't) -- absence here means
# an empty "properties" object, which is correct, not a gap.
PROPERTY_TYPES_BY_ID = {
    "alert": ["AlertVariant"],
    "badge": ["BadgeVariant"],
    "button": ["ButtonVariant", "ButtonSize"],
    "card": ["CardVariant", "CardSize"],
    "chip": ["ChipVariant"],
    "select": ["SelectVariant"],
    "text-field": ["TextFieldVariant"],
    "toggle": ["ToggleVariant"],
    "accordion": ["ShadcnAccordionType"],
    "attachment": ["ShadcnAttachmentState", "ShadcnAttachmentSize", "ShadcnAttachmentOrientation"],
    "button-group": ["ButtonGroupOrientation"],
    "avatar": ["ShadcnAvatarSize"],
    "bubble": ["ShadcnBubbleVariant"],
    "drawer": ["ShadcnDrawerDirection"],
    "field": ["ShadcnFieldOrientation"],
    "item": ["ShadcnItemVariant", "ShadcnItemMediaVariant"],
    "marker": ["ShadcnMarkerVariant"],
    "message": ["ShadcnMessageAlign"],
    "message-scroller": ["ShadcnMessageScrollerDirection"],
    "scroll-area": ["ShadcnScrollAreaOrientation"],
    "sheet": ["ShadcnSheetSide"],
    "separator": ["ShadcnSeparatorOrientation"],
    "stepper": ["ShadcnStepperOrientation"],
    "toast": ["ShadcnToastVariant"],
    "text": ["ShadcnTextStyle"],
}


def parse_catalog_entries():
    text = CATALOG_REGISTRY.read_text()
    entries = []
    for m in re.finditer(
        r'CatalogEntry\(id = "([^"]+)", title = "([^"]+)", category = CatalogCategory\.(\w+)(?:, isNew = (true|false))?\)',
        text,
    ):
        entry_id, title, category, is_new = m.groups()
        if entry_id in EXCLUDED_IDS:
            continue
        entries.append({
            "id": entry_id,
            "title": title,
            "category": category,
            "isNew": is_new == "true",
        })
    return entries


def find_description(component_id):
    """Greps every *Doc.kt for `id = "<component_id>"` then the following `description = ...`
    value -- handles both a single string literal and Kotlin's `"..." + "..."` line-continued
    concatenation (a few components, e.g. Field/Shimmer, build theirs that way), and un-escapes
    `\\"`/`\\\\` since some descriptions quote real shadcn/ui class names like `"generating
    response"`. Not a real Kotlin string-literal parser -- multi-line templates or `${'$'}`
    interpolation would still break this, but no current description uses either."""
    for doc_file in DOCS_DIR.glob("*Doc.kt"):
        text = doc_file.read_text()
        if f'id = "{component_id}"' not in text:
            continue
        m = re.search(r'description\s*=\s*((?:"(?:[^"\\]|\\.)*"\s*\+?\s*)+)', text)
        if not m:
            return ""
        segments = re.findall(r'"((?:[^"\\]|\\.)*)"', m.group(1))
        return "".join(segments).replace('\\"', '"').replace("\\\\", "\\")
    return ""


def extract_enum_values(type_name):
    """Finds `enum class Name { A, B, C }` / `enum class Name(...) { A(...), B(...) }` /
    `sealed interface Name { data object A : Name ... }` wherever it's declared, across
    both styles/ and components/ (properties live in either depending on the component)."""
    for directory in (STYLES_DIR, COMPONENTS_DIR):
        for kt_file in directory.glob("*.kt"):
            text = kt_file.read_text()
            enum_match = re.search(rf"enum class {type_name}\b[^{{]*\{{([^}}]*)\}}", text)
            if enum_match:
                body = enum_match.group(1)
                values = [v.strip().split("(")[0] for v in body.split(",") if v.strip()]
                return [v for v in values if v]
            iface_match = re.search(
                rf"sealed interface {type_name}\s*\{{(.*?)\n\}}", text, re.DOTALL,
            )
            if iface_match:
                body = iface_match.group(1)
                values = re.findall(rf"data object (\w+)\s*:\s*{type_name}", body)
                if values:
                    return values
    return []


# Screenshot filename prefixes that don't match `id.replace("-", "_")` -- the test class
# name doesn't always match the catalog id 1:1 (RadioButtonScreenshotTest for
# "radio-group", "textfield" with no separator for "text-field"). Verified against each
# test file's actual `snapshot("...")` call, not guessed.
PREFIX_OVERRIDES = {
    "text-field": "textfield",
    "radio-group": "radio",
}


def find_screenshots(component_id):
    """Roborazzi filenames use the same id with hyphens as underscores
    (`toggle-group` -> `toggle_group_*.png`) -- verified against the actual naming
    convention every ShadcnScreenshotTest subclass already follows, with
    [PREFIX_OVERRIDES] for the handful that don't."""
    prefix_stem = PREFIX_OVERRIDES.get(component_id, component_id.replace("-", "_"))
    matches = sorted(SNAPSHOTS_DIR.glob(f"{prefix_stem}_*.png"))
    previews = []
    for path in matches:
        stem = path.stem  # e.g. button_variants_disabled_dark
        theme = "dark" if stem.endswith("_dark") else "light"
        state = stem[len(prefix_stem) + 1 : -len("_" + theme)]
        previews.append({
            "state": state,
            "theme": theme,
            "url": f"{RAW_BASE}/{path.name}",
        })
    return previews


def build_metadata():
    entries = parse_catalog_entries()
    components = []
    missing_previews = []
    for entry in entries:
        component_id = entry["id"]
        properties = {}
        for type_name in PROPERTY_TYPES_BY_ID.get(component_id, []):
            values = extract_enum_values(type_name)
            if values:
                properties[type_name] = values
        previews = find_screenshots(component_id)
        if not previews:
            missing_previews.append(component_id)
        components.append({
            "id": component_id,
            "title": entry["title"],
            "description": find_description(component_id),
            "category": CATEGORY_SLUGS[entry["category"]],
            "family": FAMILY_BY_ID.get(component_id, "uncategorized"),
            "isNew": entry["isNew"],
            "properties": properties,
            "previews": previews,
        })
    return components, missing_previews



# Every ShadcnScreenshotTest.snapshot() call across the whole suite uses the default
# `stylePreset` param (ShadcnStylePreset.Vega) -- confirmed by grepping every test file
# for an explicit override; zero exist outside the separate style_matrix_* comparison
# test, which isn't part of this per-component preview set. So every preview image here
# is Vega specifically, not a "the theme" in the abstract -- shadcn-compose ships 7 more
# (Nova, Maia, Lyra, Mira, Luma, Sera, Rhea), each changing shape/spacing/animation, so a
# consumer comparing against these previews needs to know which one they're looking at.
ALL_STYLE_PRESETS = ["Vega", "Nova", "Maia", "Lyra", "Mira", "Luma", "Sera", "Rhea"]
PREVIEW_STYLE_PRESET = "Vega"


def main():
    components, missing_previews = build_metadata()
    output = {
        "stylePreset": PREVIEW_STYLE_PRESET,
        "stylePresetNote": (
            "Every preview image was captured under the 'Vega' style preset (this library's "
            f"default), not a neutral/preset-agnostic render. shadcn-compose ships {len(ALL_STYLE_PRESETS)} "
            f"presets total ({', '.join(ALL_STYLE_PRESETS)}), each changing shape/spacing/animation -- "
            "swapping presets would change these components' appearance without changing this file."
        ),
        "components": components,
    }
    OUTPUT.write_text(json.dumps(output, indent=2) + "\n")
    print(f"Wrote {len(components)} components to {OUTPUT}")
    if missing_previews:
        print(f"WARNING: {len(missing_previews)} components have zero screenshot previews: {missing_previews}")
    uncategorized = [c["id"] for c in components if c["family"] == "uncategorized"]
    if uncategorized:
        print(f"WARNING: {len(uncategorized)} components have no family assigned: {uncategorized}")


if __name__ == "__main__":
    main()
