#!/usr/bin/env python3
"""
KMM Design System — Palette Generator

Takes N named brand seed colors and outputs:
  1. tokens/AppColors.kt        — dynamic BrandColorFamily map + fixed neutrals
  2. tokens/BrandExtensions.kt  — typed per-project extension properties
  3. previews/ColorPalettePreview.kt — Compose @Preview swatches (light + dark)

Usage:
  python3 generate_palette.py \
    --brand primary=#1E3A5F \
    --brand accent=#E67E22 \
    --group-id com.example.app \
    --output src/commonMain/kotlin/com/example/app/core/designsystem/
"""
from __future__ import annotations

import argparse
import math
import os
from pathlib import Path


# ── Color math (no external dependencies) ─────────────────────────────────────

def hex_to_rgb(hex_color: str) -> tuple[int, int, int]:
    h = hex_color.lstrip("#")
    if len(h) == 3:
        h = "".join(c * 2 for c in h)
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


def rgb_to_hsl(r: int, g: int, b: int) -> tuple[float, float, float]:
    r_, g_, b_ = r / 255, g / 255, b / 255
    max_c, min_c = max(r_, g_, b_), min(r_, g_, b_)
    l = (max_c + min_c) / 2
    if max_c == min_c:
        h = s = 0.0
    else:
        d = max_c - min_c
        s = d / (2 - max_c - min_c) if l > 0.5 else d / (max_c + min_c)
        if max_c == r_:
            h = (g_ - b_) / d + (6 if g_ < b_ else 0)
        elif max_c == g_:
            h = (b_ - r_) / d + 2
        else:
            h = (r_ - g_) / d + 4
        h /= 6
    return h * 360, s * 100, l * 100


def hsl_to_rgb(h: float, s: float, l: float) -> tuple[int, int, int]:
    h_, s_, l_ = h / 360, s / 100, l / 100
    if s_ == 0:
        v = int(l_ * 255)
        return v, v, v

    def hue2rgb(p: float, q: float, t: float) -> float:
        if t < 0: t += 1
        if t > 1: t -= 1
        if t < 1 / 6: return p + (q - p) * 6 * t
        if t < 1 / 2: return q
        if t < 2 / 3: return p + (q - p) * (2 / 3 - t) * 6
        return p

    q = l_ * (1 + s_) if l_ < 0.5 else l_ + s_ - l_ * s_
    p = 2 * l_ - q
    return (
        int(hue2rgb(p, q, h_ + 1 / 3) * 255),
        int(hue2rgb(p, q, h_) * 255),
        int(hue2rgb(p, q, h_ - 1 / 3) * 255),
    )


def _linear(c: int) -> float:
    v = c / 255
    return v / 12.92 if v <= 0.03928 else ((v + 0.055) / 1.055) ** 2.4


def luminance(r: int, g: int, b: int) -> float:
    return 0.2126 * _linear(r) + 0.7152 * _linear(g) + 0.0722 * _linear(b)


def contrast_ratio(lum1: float, lum2: float) -> float:
    hi, lo = max(lum1, lum2), min(lum1, lum2)
    return (hi + 0.05) / (lo + 0.05)


def on_color(r: int, g: int, b: int) -> tuple[int, int, int]:
    """Return near-black or near-white — whichever has higher WCAG contrast."""
    lum = luminance(r, g, b)
    dark  = (9, 9, 11)       # #09090B
    light = (250, 250, 250)  # #FAFAFA
    c_dark  = contrast_ratio(lum, luminance(*dark))
    c_light = contrast_ratio(lum, luminance(*light))
    return dark if c_dark >= c_light else light


def shift_lightness(r: int, g: int, b: int, delta: float) -> tuple[int, int, int]:
    h, s, l = rgb_to_hsl(r, g, b)
    return hsl_to_rgb(h, s, max(0.0, min(100.0, l + delta)))


def shift_saturation(r: int, g: int, b: int, delta: float) -> tuple[int, int, int]:
    h, s, l = rgb_to_hsl(r, g, b)
    return hsl_to_rgb(h, max(0.0, min(100.0, s + delta)), l)


def rgb_to_hex(r: int, g: int, b: int) -> str:
    return f"#{r:02X}{g:02X}{b:02X}"


def to_argb_hex(r: int, g: int, b: int, a: int = 255) -> str:
    return f"0xFF{r:02X}{g:02X}{b:02X}"


# ── Brand family derivation ────────────────────────────────────────────────────

def derive_family_light(seed_hex: str) -> dict[str, str]:
    r, g, b = hex_to_rgb(seed_hex)
    on = on_color(r, g, b)
    container = shift_lightness(r, g, b, +38)
    on_container = on_color(*container)
    hover   = shift_lightness(r, g, b, -8)
    pressed = shift_lightness(r, g, b, -15)
    dis_rgb = shift_saturation(*shift_lightness(r, g, b, +20), -40)
    return {
        "color":       to_argb_hex(r, g, b),
        "onColor":     to_argb_hex(*on),
        "container":   to_argb_hex(*container),
        "onContainer": to_argb_hex(*on_container),
        "hover":       to_argb_hex(*hover),
        "pressed":     to_argb_hex(*pressed),
        "disabled":    to_argb_hex(*dis_rgb),
    }


def derive_family_dark(seed_hex: str) -> dict[str, str]:
    r, g, b = hex_to_rgb(seed_hex)
    # In dark mode: lighten the color slightly so it pops on dark surfaces
    dr, dg, db = shift_lightness(r, g, b, +15)
    on = on_color(dr, dg, db)
    container = shift_lightness(r, g, b, -25)
    on_container = on_color(*container)
    hover   = shift_lightness(dr, dg, db, +8)
    pressed = shift_lightness(dr, dg, db, +15)
    dis_rgb = shift_saturation(*shift_lightness(r, g, b, -10), -40)
    return {
        "color":       to_argb_hex(dr, dg, db),
        "onColor":     to_argb_hex(*on),
        "container":   to_argb_hex(*container),
        "onContainer": to_argb_hex(*on_container),
        "hover":       to_argb_hex(*hover),
        "pressed":     to_argb_hex(*pressed),
        "disabled":    to_argb_hex(*dis_rgb),
    }


# ── Fixed neutral palettes ─────────────────────────────────────────────────────

LIGHT_NEUTRALS = {
    "background":       "0xFFFFFFFF",
    "surface":          "0xFFFFFFFF",
    "surfaceVariant":   "0xFFF4F4F5",
    "surfaceContainer": "0xFFE4E4E7",
    "onBackground":     "0xFF09090B",
    "onSurface":        "0xFF09090B",
    "onSurfaceVariant": "0xFF71717A",
    "outline":          "0xFFE4E4E7",
    "outlineVariant":   "0xFFF4F4F5",
    "scrim":            "0x80000000",
    "inverseSurface":   "0xFF27272A",
    "inverseOnSurface": "0xFFFAFAFA",
    "success":          "0xFF16A34A",
    "onSuccess":        "0xFFFFFFFF",
    "warning":          "0xFFD97706",
    "onWarning":        "0xFFFFFFFF",
    "error":            "0xFFDC2626",
    "onError":          "0xFFFFFFFF",
    "errorContainer":   "0xFFFEE2E2",
    "onErrorContainer": "0xFF7F1D1D",
    "hoverOverlay":     "0x0A000000",
    "pressedOverlay":   "0x1A000000",
}

DARK_NEUTRALS = {
    "background":       "0xFF09090B",
    "surface":          "0xFF09090B",
    "surfaceVariant":   "0xFF18181B",
    "surfaceContainer": "0xFF27272A",
    "onBackground":     "0xFFFAFAFA",
    "onSurface":        "0xFFFAFAFA",
    "onSurfaceVariant": "0xFFA1A1AA",
    "outline":          "0xFF3F3F46",
    "outlineVariant":   "0xFF27272A",
    "scrim":            "0x99000000",
    "inverseSurface":   "0xFFF4F4F5",
    "inverseOnSurface": "0xFF18181B",
    "success":          "0xFF4ADE80",
    "onSuccess":        "0xFF052E16",
    "warning":          "0xFFFBBF24",
    "onWarning":        "0xFF451A03",
    "error":            "0xFFFCA5A5",
    "onError":          "0xFF7F1D1D",
    "errorContainer":   "0xFF7F1D1D",
    "onErrorContainer": "0xFFFEE2E2",
    "hoverOverlay":     "0x1AFFFFFF",
    "pressedOverlay":   "0x2AFFFFFF",
}


# ── Kotlin codegen ─────────────────────────────────────────────────────────────

def _family_block(name: str, fam: dict[str, str], indent: int = 12) -> str:
    pad = " " * indent
    lines = [f'"{name}" to BrandColorFamily(']
    fields = ["color", "onColor", "container", "onContainer", "hover", "pressed", "disabled"]
    for i, field in enumerate(fields):
        comma = "," if i < len(fields) - 1 else ""
        lines.append(f"{pad}    {field} = Color({fam[field]}){comma}")
    lines.append(f"{pad}),")
    return ("\n" + pad).join(lines)


def generate_app_colors_kt(group_id: str, brand_light: dict, brand_dark: dict) -> str:
    pkg = f"{group_id}.core.designsystem.tokens"

    light_families = "\n            ".join(
        _family_block(name, fam, indent=12) for name, fam in brand_light.items()
    )
    dark_families = "\n            ".join(
        _family_block(name, fam, indent=12) for name, fam in brand_dark.items()
    )

    def neutral_lines(n: dict) -> str:
        return "\n    ".join(f"{k} = Color({v})," for k, v in n.items())

    return f"""\
package {pkg}

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class BrandColorFamily(
    val color:       Color,  // brand color — buttons, active states
    val onColor:     Color,  // text ON this color (WCAG-derived)
    val container:   Color,  // tonal bg — cards, chips, badges in brand color
    val onContainer: Color,  // text ON container
    val hover:       Color,
    val pressed:     Color,
    val disabled:    Color,
)

@Immutable
data class AppColors(
    // Dynamic — project defines as many brand families as it needs
    val brand: Map<String, BrandColorFamily>,

    // Fixed neutrals — text always lives here, never in brand
    val background:       Color,
    val surface:          Color,
    val surfaceVariant:   Color,
    val surfaceContainer: Color,
    val onBackground:     Color,
    val onSurface:        Color,          // body text
    val onSurfaceVariant: Color,          // secondary text / hints

    // Utility
    val outline:          Color,
    val outlineVariant:   Color,
    val scrim:            Color,
    val inverseSurface:   Color,
    val inverseOnSurface: Color,

    // Status — semantic, not brand
    val success:          Color,
    val onSuccess:        Color,
    val warning:          Color,
    val onWarning:        Color,
    val error:            Color,
    val onError:          Color,
    val errorContainer:   Color,
    val onErrorContainer: Color,

    val hoverOverlay:   Color,
    val pressedOverlay: Color,

    val isLight: Boolean,
)

val LightColors = AppColors(
    brand = mapOf(
        {light_families}
    ),
    {neutral_lines(LIGHT_NEUTRALS)}
    isLight = true,
)

val DarkColors = AppColors(
    brand = mapOf(
        {dark_families}
    ),
    {neutral_lines(DARK_NEUTRALS)}
    isLight = false,
)
"""


def generate_brand_extensions_kt(group_id: str, brand_names: list[str]) -> str:
    pkg = f"{group_id}.core.designsystem.tokens"
    lines = []
    for name in brand_names:
        lines.append(
            f"val AppColors.{name}: BrandColorFamily "
            f'get() = brand.getValue("{name}")'
        )
    props = "\n".join(lines)
    return f"""\
package {pkg}

// Auto-generated — re-run generate_palette.py to update.
// Add these to the project's :core:designsystem module.

{props}
"""


def generate_palette_preview_kt(group_id: str, brand_names: list[str]) -> str:
    pkg = f"{group_id}.core.designsystem.previews"
    tokens_pkg = f"{group_id}.core.designsystem.tokens"

    swatch_rows = "\n        ".join(
        f'BrandFamilyRow(label = "{name}", family = colors.{name})'
        for name in brand_names
    )

    return f"""\
package {pkg}

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import {tokens_pkg}.AppColors
import {tokens_pkg}.BrandColorFamily
import {tokens_pkg}.DarkColors
import {tokens_pkg}.LightColors
import {tokens_pkg}.*

@Preview(name = "Palette — Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PalettePreviewLight() = PaletteDisplay(LightColors)

@Preview(name = "Palette — Dark", showBackground = true, backgroundColor = 0xFF09090B)
@Composable
fun PalettePreviewDark() = PaletteDisplay(DarkColors)

@Composable
private fun PaletteDisplay(colors: AppColors) {{
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {{
        item {{ SectionLabel("Brand Families", colors.onSurface) }}
        item {{
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {{
                {swatch_rows}
            }}
        }}
        item {{ SectionLabel("Neutrals", colors.onSurface) }}
        item {{ NeutralRow(colors) }}
        item {{ SectionLabel("Status", colors.onSurface) }}
        item {{ StatusRow(colors) }}
    }}
}}

@Composable
private fun BrandFamilyRow(label: String, family: BrandColorFamily) {{
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {{
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF71717A))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {{
            Swatch(family.color,     family.onColor,     "color")
            Swatch(family.onColor,   family.color,       "on")
            Swatch(family.container, family.onContainer, "container")
            Swatch(family.hover,     family.onColor,     "hover")
            Swatch(family.pressed,   family.onColor,     "pressed")
            Swatch(family.disabled,  Color(0xFF71717A),  "disabled")
        }}
    }}
}}

@Composable
private fun NeutralRow(colors: AppColors) {{
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {{
        Swatch(colors.background,     colors.onBackground,     "bg")
        Swatch(colors.surface,        colors.onSurface,        "surface")
        Swatch(colors.surfaceVariant, colors.onSurfaceVariant, "variant")
        Swatch(colors.onSurface,      colors.surface,          "text")
        Swatch(colors.onSurfaceVariant, colors.surface,        "hint")
        Swatch(colors.outline,        colors.onSurface,        "outline")
    }}
}}

@Composable
private fun StatusRow(colors: AppColors) {{
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {{
        Swatch(colors.success,        colors.onSuccess,        "success")
        Swatch(colors.warning,        colors.onWarning,        "warning")
        Swatch(colors.error,          colors.onError,          "error")
        Swatch(colors.errorContainer, colors.onErrorContainer, "errCont")
    }}
}}

@Composable
private fun Swatch(bg: Color, textColor: Color, label: String) {{
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 56.dp)
            .background(bg)
            .border(0.5.dp, Color(0x22000000)),
        contentAlignment = Alignment.BottomStart,
    ) {{
        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            modifier = Modifier.padding(3.dp),
        )
    }}
}}

@Composable
private fun SectionLabel(text: String, color: Color) {{
    Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
}}
"""


# ── Image color extraction ────────────────────────────────────────────────────

def _kmeans(pixels: list[tuple[int, int, int]], k: int, iterations: int = 20) -> list[tuple[int, int, int]]:
    """Pure-Python k-means — no numpy required."""
    import random
    centroids = random.sample(pixels, min(k, len(pixels)))
    for _ in range(iterations):
        clusters: list[list[tuple[int, int, int]]] = [[] for _ in range(k)]
        for px in pixels:
            dists = [
                (px[0] - c[0]) ** 2 + (px[1] - c[1]) ** 2 + (px[2] - c[2]) ** 2
                for c in centroids
            ]
            clusters[dists.index(min(dists))].append(px)
        new_c = []
        for i, cluster in enumerate(clusters):
            if cluster:
                n = len(cluster)
                new_c.append((
                    sum(p[0] for p in cluster) // n,
                    sum(p[1] for p in cluster) // n,
                    sum(p[2] for p in cluster) // n,
                ))
            else:
                new_c.append(centroids[i])
        if new_c == centroids:
            break
        centroids = new_c
    return centroids


def _filter_near_neutral(colors: list[tuple[int, int, int]], threshold: float = 20.0) -> list[tuple[int, int, int]]:
    """Remove near-white, near-black, and near-gray clusters (low saturation)."""
    result = []
    for r, g, b in colors:
        _, s, l = rgb_to_hsl(r, g, b)
        if s >= threshold and 10 <= l <= 90:
            result.append((r, g, b))
    return result or colors  # fallback: return all if everything filtered


def extract_colors_from_image(image_path: str, n: int = 4) -> list[tuple[int, int, int]]:
    """Extract N dominant brand colors from an image using PIL + k-means.

    Filters out near-neutral colors (grays, white, black) so the returned
    palette contains only the most prominent brand hues.

    Requires: pip install Pillow
    """
    try:
        from PIL import Image
    except ImportError:
        raise SystemExit(
            "❌  Pillow is required for image color extraction.\n"
            "    Install with: pip install Pillow\n"
            "    Or provide colors manually: --brand primary=#HEX --brand accent=#HEX"
        )

    img = Image.open(image_path).convert("RGB")
    # Downsample for speed — 120px longest side is plenty for color extraction
    img.thumbnail((120, 120))
    pixels = list(img.getdata())

    # Over-cluster then filter neutrals, take top N
    k_initial = min(n * 3, len(pixels), 24)
    clusters = _kmeans(pixels, k_initial)
    brand_clusters = _filter_near_neutral(clusters)

    # Sort by saturation × size-prominence (just saturation here, stable enough)
    brand_clusters.sort(key=lambda c: rgb_to_hsl(*c)[1], reverse=True)
    return brand_clusters[:n]


# ── CLI ────────────────────────────────────────────────────────────────────────

def parse_brand_args(raw: list[str]) -> dict[str, str]:
    """Parse ['name=#HEX', ...] into {'name': '#HEX'}."""
    result: dict[str, str] = {}
    for entry in raw:
        if "=" not in entry:
            raise ValueError(f"Invalid brand entry '{entry}' — expected name=#HEXCOLOR")
        name, hex_color = entry.split("=", 1)
        name = name.strip().lower().replace("-", "_")
        hex_color = hex_color.strip()
        if not hex_color.startswith("#"):
            hex_color = "#" + hex_color
        result[name] = hex_color
    return result


def print_contrast_report(brand_names: list[str], brand_light: dict, brand_dark: dict) -> None:
    NEAR_BLACK = (9, 9, 11)
    NEAR_WHITE = (250, 250, 250)

    print("\nContrast report (WCAG AA requires ≥ 4.5 for normal text):")
    for name in brand_names:
        for mode, fam in [("light", brand_light[name]), ("dark", brand_dark[name])]:
            cr, g, b = hex_to_rgb(fam["color"].replace("0xFF", "#"))
            lum_bg = luminance(cr, g, b)
            on_rgb = hex_to_rgb(fam["onColor"].replace("0xFF", "#"))
            lum_on = luminance(*on_rgb)
            ratio = contrast_ratio(lum_bg, lum_on)
            status = "✅" if ratio >= 4.5 else "⚠️ "
            print(f"  {status} {name} ({mode}): {ratio:.1f}:1  color→onColor")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Generate AppColors.kt, BrandExtensions.kt, and ColorPalettePreview.kt"
    )
    parser.add_argument(
        "--brand",
        action="append",
        metavar="name=#HEX",
        default=None,
        help="Brand color entry. Repeat for each role. E.g. --brand primary=#1E3A5F",
    )
    parser.add_argument(
        "--image",
        metavar="PATH",
        help="Extract N dominant brand colors from an image (requires Pillow). "
             "Use --count to control how many colors to extract.",
    )
    parser.add_argument(
        "--count",
        type=int,
        default=4,
        help="Number of brand colors to extract from --image (default: 4)",
    )
    parser.add_argument(
        "--group-id",
        default="com.example.app",
        help="Kotlin package group ID (e.g. com.example.app)",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("."),
        help="Output directory (e.g. src/commonMain/kotlin/com/example/app/core/designsystem/)",
    )
    args = parser.parse_args()

    if args.image:
        print(f"Extracting {args.count} dominant colors from {args.image} …")
        extracted = extract_colors_from_image(args.image, args.count)
        brand_seeds: dict[str, str] = {}
        role_names = ["primary", "secondary", "tertiary", "quaternary",
                      "quinary", "senary", "septenary", "octonary"]
        for i, (r, g, b) in enumerate(extracted):
            name = role_names[i] if i < len(role_names) else f"brand{i + 1}"
            hex_val = rgb_to_hex(r, g, b)
            brand_seeds[name] = hex_val
            print(f"  {name}: {hex_val}")
        print()
        if args.brand:
            # Merge: explicit --brand overrides extracted names
            for entry in args.brand:
                n, h = entry.split("=", 1)
                brand_seeds[n.strip().lower()] = h.strip()
    elif args.brand:
        brand_seeds = parse_brand_args(args.brand)
    else:
        parser.error("Provide --brand name=#HEX or --image path/to/image.png")
    brand_names = list(brand_seeds.keys())

    brand_light = {name: derive_family_light(hex_) for name, hex_ in brand_seeds.items()}
    brand_dark  = {name: derive_family_dark(hex_)  for name, hex_ in brand_seeds.items()}

    tokens_dir   = args.output / "tokens"
    previews_dir = args.output / "previews"
    tokens_dir.mkdir(parents=True, exist_ok=True)
    previews_dir.mkdir(parents=True, exist_ok=True)

    files = {
        tokens_dir   / "AppColors.kt":             generate_app_colors_kt(args.group_id, brand_light, brand_dark),
        tokens_dir   / "BrandExtensions.kt":        generate_brand_extensions_kt(args.group_id, brand_names),
        previews_dir / "ColorPalettePreview.kt":    generate_palette_preview_kt(args.group_id, brand_names),
    }

    for path, content in files.items():
        path.write_text(content, encoding="utf-8")
        print(f"✅  {path}")

    print_contrast_report(brand_names, brand_light, brand_dark)

    print(f"""
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Generated {len(files)} files → {args.output}

  Open previews/ColorPalettePreview.kt in Android Studio
  to see your full palette as annotated swatches (light + dark).

  Run /kmm-record-design-baselines to capture goldens.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━""")


if __name__ == "__main__":
    main()
