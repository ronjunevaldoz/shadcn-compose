#!/usr/bin/env python3
"""Derives docs/previews/*.png from the Roborazzi test goldens in
shadcn/core/src/jvmTest/snapshots/ -- same filenames, flattened onto a solid opaque
background and padded with a consistent margin, so images meant for a side-by-side
cross-library comparison actually look consistent next to each other. The raw test
goldens themselves are untouched; this is a derived, comparison-only asset.

Two real things this fixes, confirmed by inspecting the source PNGs directly (not
assumed):
  1. Every golden's corner pixel is fully opaque (alpha=255) *except* captures that
     open a Popup (Dialog/Sheet/Drawer/Popover/DropdownMenu/...), which report semi-
     transparent regions (alpha=128) around the real UI -- a known cosmetic artifact of
     the test harness reporting the platform window's full bounds, documented in
     ShadcnScreenshotTest.captureCompositeRoot()'s own comment. Flattening onto the
     correct theme background (sampled from a real capture, not guessed: (255,255,255)
     light / (10,10,10) dark -- matches colors.background = TwColors.white / zinc950)
     turns that transparency into a plain, correct-looking background instead.
  2. Image sizes range from ~70px to 1024px tall/wide with zero consistent margin --
     fine for CI pixel-diffing, bad for eyeballing two libraries' Button next to each
     other. A fixed margin around every image's *existing* content (no cropping,
     no resizing) at least makes the framing consistent, without the much bigger
     (and riskier -- see the module's own note below) job of forcing every component
     onto one identical canvas size regardless of whether it's a single badge or a
     full-screen Drawer.

Deliberately NOT doing: auto-detecting each image's real content bounding box and
cropping the popup-family images down to it. That would need distinguishing real UI
pixels from dead window margin under partial alpha -- exactly the kind of pixel
heuristic that can silently look "fine" in a spot check and be subtly wrong across
250+ images in ways a script author won't necessarily notice. Left as a known,
documented gap (see generate_component_metadata.py's OVERSIZED_CANVAS_IDS) rather than
risking a fragile auto-crop shipping wrong.
"""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
SNAPSHOTS_DIR = ROOT / "shadcn/core/src/jvmTest/snapshots"
OUTPUT_DIR = ROOT / "docs/previews"

MARGIN_PX = 24
LIGHT_BG = (255, 255, 255)
DARK_BG = (10, 10, 10)


def normalize_one(src_path: Path, dest_path: Path) -> None:
    theme_bg = DARK_BG if src_path.stem.endswith("_dark") else LIGHT_BG
    img = Image.open(src_path).convert("RGBA")

    # Flatten onto the real theme background -- correct even for images that are
    # already fully opaque (flattening white-on-white / near-black-on-near-black is a
    # no-op), and fixes the popup-family images' semi-transparent margin.
    flattened = Image.new("RGB", img.size, theme_bg)
    flattened.paste(img, mask=img.split()[3])

    padded = Image.new("RGB", (img.width + MARGIN_PX * 2, img.height + MARGIN_PX * 2), theme_bg)
    padded.paste(flattened, (MARGIN_PX, MARGIN_PX))
    padded.save(dest_path)


def main():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    sources = sorted(SNAPSHOTS_DIR.glob("*.png"))
    for src_path in sources:
        normalize_one(src_path, OUTPUT_DIR / src_path.name)
    print(f"Normalized {len(sources)} images into {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
