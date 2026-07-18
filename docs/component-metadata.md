# Component metadata

`component-metadata.json` is a machine-readable index of every catalog component —
generated from the same source the catalog app and this repo's own docs are built
from, not hand-maintained separately. Meant for tooling that wants to compare another
library's components against this one (properties + a real rendered preview per
state/theme), not for humans browsing — see [`components.md`](components.md) for that.

## Regenerating

```bash
python3 scripts/normalize_preview_images.py    # docs/previews/*.png (run first)
python3 scripts/generate_component_metadata.py # docs/component-metadata.json
```

Run both after any component, doc, or screenshot-golden change. `normalize_preview_images.py`
must run first — the metadata generator's preview URLs point at its output
(`docs/previews/`), not the raw `shadcn/core/src/jvmTest/snapshots/` test goldens.

## Shape

```jsonc
{
  "stylePreset": "Vega",
  "stylePresetNote": "...",
  "components": [
    {
      "id": "button",
      "title": "Button",
      "description": "Triggers an action or event. Supports six variants and five sizes.",
      "category": "core-primitives",
      "family": "action",
      "isNew": false,
      "properties": {
        "ButtonVariant": ["Default", "Outline", "Secondary", "Ghost", "Destructive", "Link"],
        "ButtonSize": ["Xs", "Sm", "Md", "Lg", "Icon"]
      },
      "previews": [
        { "state": "variants", "theme": "light", "url": "https://raw.githubusercontent.com/.../button_variants_light.png" },
        { "state": "variants", "theme": "dark", "url": "..." },
        { "state": "variants_disabled", "theme": "light", "url": "..." }
      ]
    }
  ]
}
```

**Top level**

| Field | Meaning |
|---|---|
| `stylePreset` | The one style preset (of 8 this library ships) every preview image below was captured under. Read `stylePresetNote` before assuming these previews represent "the" look of a component — swapping presets changes shape/spacing/animation. |
| `components` | Array described below. |

Beyond `stylePreset`, `ShadcnTheme` has two more independent theming axes, neither varied here:
every preview also used `baseColor = ShadcnBaseColor.Neutral` (of 7: Neutral, Stone, Zinc, Mauve,
Olive, Mist, Taupe) and `accent = ShadcnAccent.Base` (of 18: Base + 17 named colors, e.g. Blue,
Emerald, Rose, Violet — `accent` only overrides `primary`/`onPrimary` on top of whichever
`baseColor` is active). Unlike `stylePreset`, the screenshot-test harness (`ShadcnScreenshotTest`)
doesn't even expose these as parameters, so there's no per-color variant set to link to yet.

**Per component**

| Field | Meaning |
|---|---|
| `id` | Stable slug, matches the catalog app's URL/route for this component. |
| `title` | Display name. |
| `description` | One-line summary, taken verbatim from the catalog doc page. |
| `category` | This library's own UI-navigation grouping (sidebar section) — `core-primitives`, `forms-and-inputs`, `data-display`, `feedback`, `disclosure-and-navigation`, `overlays-and-navigation`, `data-and-layout`, `ai-elements`, `utils`. |
| `family` | A *different*, curated grouping by interaction pattern, meant specifically for cross-library parity comparison — not the same as `category`. See below. |
| `isNew` | Whether the catalog currently flags this component as newly added. |
| `properties` | Every variant/size sealed-interface or enum this component exposes, with its full set of values. Empty object `{}` is a real answer for components with no such API (e.g. plain layout primitives), not a gap. |
| `previews` | One entry per captured state × theme. `state` is a free-form label (`"variants"`, `"variants_disabled"`, `"focused"`, ...) — read it, don't parse it. `url` is a public GitHub-raw link, no auth needed. |

## Parity families

Grouped by the *interaction pattern* a component needs to get right, not by where it
lives in this repo's own sidebar — the two rarely line up 1:1 (`popover` is `overlays-and-navigation`
by category but `field` by family, since the comparison that matters for it is "does it let you
pick/enter a value correctly," not "is it an overlay"). Useful when checking whether another
library's implementation of, say, a select/combobox/dropdown genuinely matches shadcn's
actual UX for that pattern, regardless of which of those three specific components it used.

| Family | Components |
|---|---|
| `action` | button, button-group |
| `field` | text-field, textarea, select, dropdown-menu, popover, input-group, input-otp, combobox, date-picker, slider, field |
| `overlay` | dialog, tooltip, sheet, sidebar, context-menu, drawer, alert-dialog, hover-card |
| `selection` | tabs, radio-group, switch, command, menubar, navigation-menu, toggle, toggle-group, checkbox, stepper |
| `feedback` | alert, progress, skeleton, spinner, toast |
| `disclosure` | collapsible, accordion, breadcrumb |
| `layout` | card, separator, aspect-ratio, resizable, scroll-area, table, pagination |
| `data` | chart, calendar, carousel |
| `text` | text, badge, chip, kbd, label, avatar, item, empty |
| `ai` | message, bubble, attachment, marker, message-scroller |
| `utils` | shimmer, scroll-fade |

## Example: side-by-side comparison

```python
import json, urllib.request

data = json.load(urllib.request.urlopen(
    "https://raw.githubusercontent.com/ronjunevaldoz/shadcn-compose/main/docs/component-metadata.json"
))

for c in data["components"]:
    if c["family"] != "field":
        continue
    print(c["id"], "--", list(c["properties"].keys()))
    for p in c["previews"]:
        if p["theme"] == "light":
            print("  ", p["state"], p["url"])
```
