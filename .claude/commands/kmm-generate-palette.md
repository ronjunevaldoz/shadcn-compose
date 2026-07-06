# /kmm-generate-palette $ARGUMENTS

**KMM Agent Skills** — generate a full dynamic color palette from N brand seed colors.

Outputs:
- `tokens/AppColors.kt` — `BrandColorFamily` map + fixed neutrals, both light and dark
- `tokens/BrandExtensions.kt` — typed per-project extension properties
- `previews/ColorPalettePreview.kt` — Compose `@Preview` swatches for IDE mockup

---

## Step 0 — Image input (if an image path or URL is provided)

If `$ARGUMENTS` contains a file path or the user attaches/pastes an image:

1. **Read the image visually** — identify the N most prominent brand hues (ignore near-white,
   near-black, and near-gray — those are neutrals, not brand colors).
2. Present a proposed mapping:
   ```
   EXTRACTED PALETTE from <filename>:
     primary   → #1E3A5F  (dominant dark blue)
     secondary → #E67E22  (accent orange)
     tertiary  → #2ECC71  (supporting green)
   
   Rename any role or drop colors you don't need.
   Proceed with these? [y] Yes  [e] Edit  [n] Cancel
   ```
3. On confirmation, pass extracted colors as `--brand` args to the script (Step 3).

You can also pass an image file directly to the script via `--image`:
```bash
python3 generate_palette.py --image design_mockup.png --count 3 --group-id com.example.app
```
The script uses PIL (Pillow) for image reading + pure-Python k-means for color extraction.
If Pillow is not installed: `pip install Pillow`.

---

## Step 1 — Parse arguments

`$ARGUMENTS` format:
```
<name>=<#HEX> [<name>=<#HEX> ...] [--output <path>] [--group-id <package>]
```

Examples:
```
/kmm-generate-palette primary=#1E3A5F secondary=#F0F0F0
/kmm-generate-palette primary=#1E3A5F accent=#E67E22 danger=#E74C3C --group-id com.example.app
/kmm-generate-palette --image brand_guide.png --count 4
```

- Brand entries: `name=#HEX` pairs — **any number, any names you choose**
- `--image PATH`: extract dominant colors from an image instead of hex input
- `--count N`: how many colors to extract from the image (default: 4)
- `--output`: destination directory (default: `src/commonMain/kotlin/<group-id-path>/core/designsystem/`)
- `--group-id`: Kotlin package prefix (default: read from `build.gradle.kts` or prompt)

If no arguments are given, ask the user:
```
What brand colors does your project use?
Provide hex values (primary=#1E3A5F) or share an image of your brand palette.
```

---

## Step 2 — Detect output path

If `--output` is not specified:
1. Read `build.gradle.kts` or `settings.gradle.kts` to find the group ID
2. Derive path: `src/commonMain/kotlin/<group/id/as/path>/core/designsystem/`
3. Confirm with user before writing:
   ```
   Output path: <resolved path>
   Group ID:    <group.id>
   Proceed? [y/n]
   ```

---

## Step 3 — Run the generator

```bash
python3 ~/.claude/skills/kotlin-multiplatform-design-system/scripts/generate_palette.py \
  --brand <name>=<#HEX> \
  [--brand <name>=<#HEX> ...] \
  --group-id <group.id> \
  --output <resolved path>
```

If running from inside kmm-agent-skills:
```bash
python3 skills/kotlin-multiplatform-design-system/scripts/generate_palette.py \
  --brand <name>=<#HEX> \
  --group-id <group.id> \
  --output <resolved path>
```

---

## Step 4 — Show contrast report

The script prints a WCAG contrast report. Present it clearly:

```
CONTRAST REPORT (WCAG AA requires ≥ 4.5:1 for normal text):
  ✅ primary  (light): 8.3:1  color → onColor
  ✅ primary  (dark):  6.1:1  color → onColor
  ⚠️  accent  (light): 3.2:1  color → onColor  ← below AA — consider darkening
  ✅ accent   (dark):  5.1:1  color → onColor
```

For any ⚠️ finding, suggest a corrected hex:
- If contrast is below 4.5:1 in light mode → darken the seed color by 10–15% and re-run
- If below 4.5:1 in dark mode → lighten the seed color

---

## Step 5 — Show the generated files

Print a summary of what was written:

```
GENERATED FILES:
  ✅ tokens/AppColors.kt
     BrandColorFamily struct + LightColors + DarkColors
     Brand families: <list of names>

  ✅ tokens/BrandExtensions.kt
     Typed extension properties:
       val AppColors.primary   get() = brand.getValue("primary")
       val AppColors.<name>    get() = brand.getValue("<name>")
       ...

  ✅ previews/ColorPalettePreview.kt
     @Preview  PalettePreviewLight  — swatches on white background
     @Preview  PalettePreviewDark   — swatches on dark background
```

---

## Step 6 — Preview instructions

Tell the user how to see the palette:

```
HOW TO VIEW YOUR PALETTE:
  1. Open previews/ColorPalettePreview.kt in Android Studio or Fleet
  2. Click the preview panel — you'll see:
       • Each brand family: color / onColor / container / onContainer / hover / pressed / disabled
       • Neutrals: background / surface / text / hint / outline
       • Status: success / warning / error
     in both Light and Dark mode side by side.

  3. To capture as golden PNGs:
       /kmm-record-design-baselines

  4. To share with designers (Figma / Notion):
       Screenshot the preview panel or run the Roborazzi capture —
       the PNG shows the full swatch grid with labels.
```

---

## Step 7 — Wire into AppTheme (if not already done)

Check whether `theme/AppTheme.kt` already uses `AppColors`. If not, show the wiring:

```kotlin
// theme/AppTheme.kt — wire LightColors / DarkColors
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    // ... rest of AppTheme
}
```

---

## Notes

- Brand color names are **yours to define** — `primary`, `accent`, `guild`, `premium`, anything
- Each brand entry gets a full `BrandColorFamily`: color, onColor, container, onContainer, hover, pressed, disabled — derived automatically
- Neutrals (background, surface, text, outline) are **always fixed** — they never inherit brand color
- Text color is always `AppTheme.colors.onSurface` or `AppTheme.colors.onSurfaceVariant` — never a brand color directly
- Re-run any time you change brand colors — the script is idempotent
- `BrandExtensions.kt` gives you autocomplete: `AppTheme.colors.primary.color`, `AppTheme.colors.primary.onColor`, etc.
