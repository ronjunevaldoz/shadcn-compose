# shadcn-compose vs. real shadcn/ui — parity matrix

This document tracks how closely `shadcn-compose`'s tokens and components match the
real [shadcn/ui](https://ui.shadcn.com) they're modeled after. It exists so token and
component drift is a deliberate, tracked decision rather than something that quietly
accumulates.

**Sources of truth used for this pass:**
- Token values: [ui.shadcn.com/docs/theming](https://ui.shadcn.com/docs/theming) (the
  officially published default theme — light/dark oklch values and `--radius`).
- Component markup/classes: `shadcn-ui/ui` repo, `apps/v4/registry/new-york-v4/ui/*.tsx`,
  commit [`d8ace42`](https://github.com/shadcn-ui/ui/commit/d8ace420baa5c8a1abccd75e52570f2a232f193d)
  (2026-07-06).
- oklch → sRGB hex conversion done via the standard OKLab matrices (D65, no gamut
  mapping beyond clamping to `[0, 1]`); shown so the comparison is against an actual
  color, not an abstract oklch triple.

Real shadcn's default base color family is **neutral**; shadcn-compose's tokens were
originally picked against **zinc**. The two are close in lightness but not hue-identical
— treat "match" below as "same visual weight," not "identical hex."

---

## 1. Color tokens

### Light theme

| Token | shadcn oklch (official) | shadcn hex (computed) | Ours | Match |
|---|---|---|---|---|
| `background` | `oklch(1 0 0)` | `#FFFFFF` | `#FFFFFF` | ✅ |
| `foreground` → `onSurface` | `oklch(0.145 0 0)` | `#0A0A0A` | `#09090B` | ✅ (~1 step) |
| `primary` | `oklch(0.205 0 0)` | `#171717` | `#171717` | ✅ **fixed** (was `#09090B`) |
| `primary-foreground` → `onPrimary` | `oklch(0.985 0 0)` | `#FAFAFA` | `#FAFAFA` | ✅ |
| `secondary` / `muted` / `accent` | `oklch(0.97 0 0)` | `#F5F5F5` | `#F4F4F5` | ✅ |
| `secondary-foreground` → `onSecondary` | `oklch(0.205 0 0)` | `#171717` | `#171717` | ✅ **fixed** (same root cause as `primary`) |
| `muted-foreground` → `onSurfaceVariant`/`onMuted` | `oklch(0.556 0 0)` | `#737373` | `#71717A` | ✅ (neutral vs. zinc hue) |
| `destructive` | `oklch(0.577 0.245 27.325)` | `#E7000B` | `#E7000B` | ✅ **fixed** (was `#DC2626`) |
| `border` / `input` | `oklch(0.922 0 0)` | `#E5E5E5` | `#E4E4E7` | ✅ |
| `ring` → `borderFocus` | `oklch(0.708 0 0)` | `#A1A1A1` | `#A1A1AA` | ✅ |

### Dark theme

| Token | shadcn oklch (official) | shadcn hex (computed) | Ours | Match |
|---|---|---|---|---|
| `background` | `oklch(0.145 0 0)` | `#0A0A0A` | `#09090B` | ✅ |
| `foreground` → `onSurface` | `oklch(0.985 0 0)` | `#FAFAFA` | `#FAFAFA` | ✅ |
| `primary` | `oklch(0.922 0 0)` | `#E5E5E5` | `#E5E5E5` | ✅ **fixed** (was near-white `#FAFAFA`) |
| `primary-foreground` → `onPrimary` | `oklch(0.205 0 0)` | `#171717` | `#171717` | ✅ **fixed** |
| `secondary` / `muted` / `accent` | `oklch(0.269 0 0)` | `#262626` | `#27272A` | ✅ |
| `muted-foreground` → `onSurfaceVariant`/`onMuted` | `oklch(0.708 0 0)` | `#A1A1A1` | `#A1A1AA` | ✅ |
| `destructive` | `oklch(0.704 0.191 22.216)` | `#FF6467` | `#FF6467` | ✅ **fixed** (was a dark maroon `#7F1D1D` — the opposite direction from real) |
| `border` (10%) / `input` (15%) | `oklch(1 0 0 / 10–15%)` translucent white over `background` | `#222222` / `#2F2F2F` effective (composited over `#0A0A0A`) | `#27272A` | ✅ close enough (ours is an opaque token vs. real's translucent overlay — different mechanism, similar result) |
| `ring` → `borderFocus` | `oklch(0.556 0 0)` | `#737373` | `#71717A` | ✅ |

**Update (2026-07-07):** all the `primary`/`onPrimary`/`onSecondary`/`destructive`
mismatches flagged in the original pass have since been fixed in `ShadcnColors.kt` —
verified directly against the hex values above. Everything in this table is now within
rounding/hue-family tolerance.

---

## 2. Radius tokens

Real shadcn derives its radius scale from a single `--radius: 0.625rem` (10px):
`sm = radius*0.6`, `md = radius*0.8`, `lg = radius` itself, `xl = radius*1.4`, etc.

| Scale step | shadcn (computed) | Ours (`ShadcnShapes`) | Match |
|---|---|---|---|
| `sm` | 6px | `sm` = 4dp | ❌ off by 2 (unchanged — named scale itself wasn't remapped, only component *usage* was) |
| `md` | 8px | `md` = 6dp | ❌ off by 2 |
| `lg` (= `--radius`) | 10px | `lg` = 8dp | ❌ off by 2 |
| `xl` | 14px | `xl` = 12dp | ❌ off by 2 |
| `2xl` | 18px | `xxl` = 16dp | ❌ off by 2 |
| `full` | 9999px | `full` = 9999dp | ✅ |

Our whole named scale is still shifted down by roughly one step versus shadcn's
computed scale (the step values themselves haven't been remapped) — but every
component below was moved onto the step that produces the visually-closest actual
radius, which is what "match" means in the table below:

| Component | shadcn radius class | shadcn px | We use | Ours (dp) | Match |
|---|---|---|---|---|---|
| Button / Input / Toggle / ToggleGroup / ButtonGroup | `rounded-md` | 8px | `shapes.lg` | 8dp | ✅ **fixed** (was `shapes.md`/6dp) |
| Checkbox | `rounded-[4px]` (hardcoded, not on the scale) | 4px | `shapes.sm` | 4dp | ✅ **fixed** (was `shapes.xs`/2dp) |
| Badge / Chip / Switch track / Radio / Slider thumb | `rounded-full` | 9999px | `shapes.full` | 9999dp | ✅ |

---

## 3. Spacing tokens

Real shadcn uses Tailwind's default spacing scale unmodified (`0.25rem` = 4px per step).

| Ours (`ShadcnSpacing`) | Value | Tailwind equivalent | Match |
|---|---|---|---|
| `xxs` | 2dp | `0.5` (2px) | ✅ |
| `xs` | 4dp | `1` (4px) | ✅ |
| `sm` | 8dp | `2` (8px) | ✅ |
| `md` | 12dp | `3` (12px) | ✅ |
| `lg` | 16dp | `4` (16px) | ✅ |
| `xl` | 20dp | `5` (20px) | ✅ |
| `xxl` | 24dp | `6` (24px) | ✅ |
| `xxxl` | 32dp | `8` (32px) | ✅ |

Full match — no action needed here.

---

## 4. Component support matrix

"Validated" means checked directly against the real `.tsx` source this pass, not
approximated from memory.

| Component | Real shadcn surface | Ours | States implemented | Sizing vs. real | Notes |
|---|---|---|---|---|---|
| **Button** | `default/destructive/outline/secondary/ghost/link` × `default/xs/sm/lg/icon/icon-xs/icon-sm/icon-lg` | `Default/Destructive/Outline/Secondary/Ghost/Link` × `Xs/Sm/Md/Lg/Icon` | hover, focus (crisp ring as of this session), disabled | ❌ `Sm`=32dp matches real `sm`(32px); `Md`=40dp doesn't match real `default`(36px) — it coincides with real `lg`(40px) instead; `Lg`=48dp has no real counterpart; our `Xs`=28dp has no real counterpart; no `icon-xs`/`icon-sm`/`icon-lg` | radius should be `shapes.lg`, not `shapes.md` (see §2) |
| **Badge** | `default/secondary/destructive/outline/ghost/link`, always reserves a 1px `border-transparent`, has `focus-visible:ring` (for `asChild` link usage) | `Default/Secondary/Destructive/Outline/Ghost` | none — fully static | radius (`full`) ✅ | ⚠️ non-`Outline` variants don't reserve a border like real shadcn does; no focus ring support at all (real Badge supports it for the link use case) |
| **Chip** | *(not a real shadcn/ui component — shadcn only ships static Badge)* | `Default/Selected/Outline` | hover, pressed, focus (crisp ring), disabled | n/a | documented as our own addition, not a shadcn port |
| **TextField / Input** | single style, no variant prop, `h-9` (36px) | `Default/Filled/Ghost` | focus (crisp ring), disabled | no fixed height reserved (real is 36px) | radius should be `shapes.lg`, not `shapes.md` |
| **Checkbox** | single style, `size-4` (16px) | single style | checked, indeterminate, focus (crisp ring), disabled | ❌ 20dp vs. real 16px | radius should be `shapes.sm` (4dp), not `shapes.xs` (2dp) |
| **RadioButton** | single style, `size-4` (16px) | single style | selected, focus (crisp ring), disabled | ❌ 20dp vs. real 16px | radius (`full`) ✅ |
| **Switch** | `default/sm` sizes; track 32×18.4px (default) / 24×14px (sm); thumb 16px/12px | single size only | checked, focus (crisp ring), disabled | ❌ no size variant; track 40×24dp vs. real 32×18.4px; thumb 18dp vs. real 16px | radius (`full`) ✅ |
| **Toggle** | `default/outline` × `default/sm/lg` (heights 36/32/40px) | `Default/Outline`, single size | hover, checked, focus (crisp ring), disabled | ❌ no size variants at all | radius should be `shapes.lg`, not `shapes.md` |
| **Slider** | single style, thumb `size-4` (16px), track `h-1.5` (6px), ring on both hover and focus | single style | hover + focus ring (crisp, matches real `hover:ring-4`/`focus-visible:ring-4`), disabled | ✅ thumb 16dp = real 16px; ✅ track height 6dp = real 6px | thumb background is intentionally theme-aware (`colors.background`) vs. real's hardcoded `bg-white`, which is arguably a real shadcn quirk we improved on rather than a gap |
| **ToggleGroup** | Radix `ToggleGroup` primitive; real CSS strips per-item corners/borders (`first:rounded-l-md`, `last:rounded-r-md`, middle items get none) and raises the focused item's `z-index` (`focus-visible:z-10`) so its ring isn't clipped by a neighbor | `ShadcnToggleGroup` computes per-item asymmetric `ToggleCorners` (first/middle/last) and passes them to both the item's own shape *and* its ring corners | inherits Toggle's states | ✅ **re-verified 2026-07-07** against `toggle-group.tsx` — our per-corner logic matches real shadcn's corner-stripping intent | z-index/ring-clipping-by-sibling on focus not verified live (couldn't reliably force a keyboard-focus repro in this pass) — worth a follow-up check |
| **ButtonGroup** | Now an official shadcn/ui registry component (`button-group.tsx`), not a bespoke pattern. Real CSS strips *each child's own* corners/left-border per position (`[&>*:not(:first-child)]:rounded-l-none`, `[&>*:not(:last-child)]:rounded-r-none`, `:not(:first-child)]:border-l-0`) — the container itself has **no** border or radius of its own; `[&>*]:focus-visible:z-10` for the same ring-clipping reason as ToggleGroup | New `ShadcnButtonGroup(items: List<ButtonGroupItem>)` overload computes per-position `ButtonGroupCorners` (first/middle/last, orientation-aware) and passes them to both the button's own `style { shape(...) }` *and* its new `ring*Corner` params (mirroring `ShadcnButton`'s existing pattern from `ShadcnToggle`). The original flexible-children overload (mixed Input/separator compositions) is kept as-is, still drawing one shared border | inherits Button's states | ✅ **fixed 2026-07-07** for the `items` overload — verified live: two `Secondary` items render as one seamless pill with a flat inner seam, not two overlapping rounded corners. The flexible-children overload still has the original limitation (documented in its own doc comment) since arbitrary child content can't be corner-stripped generically. Per-side border stripping (only `Ghost` has no border to strip) and `focus-visible:z-10` ring-clipping-by-sibling are still not implemented — same caveats as ToggleGroup |
| **InputGroup** | Now an official shadcn/ui registry component. Real CSS: the container owns the single border (`InputGroupInput` is `border-0 focus-visible:ring-0 flex-1`), and a `has-[:focus-visible]` selector swaps the *container's* border color and draws the ring around the whole group when the inner input is focused | `ShadcnInputGroup` owns the single border/shape (its own container style, no contentPadding — the field keeps its variant padding); tracks focus-within via `onFocusEvent { it.hasFocus }` to swap border color and draw `shadcnFocusRing` on the container; sets an internal `LocalInsideInputGroup` so a nested `ShadcnTextField`/`ShadcnTextarea` automatically drops its own border/background/ring (no more `variant = Ghost` juggling); field slot wrapped in `weight(1f)` so trailing addons keep intrinsic width | focus-within on container ✅ | ✅ **fixed 2026-07-07** — three real bugs found via catalog screenshots and fixed: (1) inner field drew its own border+ring on focus (double border), (2) trailing addon got squeezed into one-character-per-line vertical text because the field's hardcoded `fillMaxWidth()` ate the row, (3) `BasicTextField` value text/cursor weren't themed (near-invisible in dark mode) — now passed explicitly via `textStyle`/`cursorBrush`, same lesson as `ChipVariant.contentColor`. Verified via `input_group_*` goldens | remaining gap: no `variant` parameter (container hardcoded to the Default look) — not yet requested |
| **Textarea** | Not a distinct shadcn/ui primitive — a plain `<textarea>` sharing Input's classes | composition wrapper around `ShadcnTextField`-equivalent styling | inherits TextField's states | — | not re-verified against real source this pass |

---

## 5. Methodology notes

- oklch → hex conversion: OKLab is computed from `(L, C, H)`, converted to linear LMS
  via the standard matrices, cubed, converted to linear sRGB, then gamma-encoded
  (`c ≤ 0.0031308 → 12.92c`, else `1.055·c^(1/2.4) − 0.055`), rounded to the nearest
  integer per channel. No perceptual gamut mapping is applied.
- "Match" (✅) means the values are within rounding/anti-aliasing tolerance (roughly
  ≤2 per 8-bit channel, or ≤1 `dp`/`px` step). "⚠️" means visually close but a real,
  intentional-looking difference. "❌" means a real, actionable mismatch.
- This document reflects a single point-in-time comparison (commit `d8ace42`,
  2026-07-06). Re-run the same fetch against the current shadcn-ui/ui commit before
  trusting it for a release — shadcn/ui's own defaults do change between versions.
- Updated 2026-07-07: color/radius fixes verified as landed; ToggleGroup re-verified
  against real source; ButtonGroup and InputGroup re-verified and found to have real,
  unaddressed gaps (see §4). Confirmed ButtonGroup and InputGroup are now official
  shadcn/ui registry components, not "not distinct primitives" as originally noted here.

---

## 6. `ShadcnStylePreset` (Vega/Nova/...) vs. the old Default/New York split

Real shadcn/ui's `npx shadcn create` replaced the old two-style choice (`default` /
`new-york`) with a larger named set. Researched 2026-07-07:

- **Vega** = the classic look, i.e. what `default` used to mean.
- **Nova** = reduced padding/margins for compact layouts — this is the direct successor
  to `new-york` (which historically also meant "tighter/more compact") and is now
  shadcn's own new default.
- **Maia**, **Luma** = soft, generously rounded, relaxed spacing.
- **Lyra** = zero border radius, boxy, pairs with monospace fonts.
- **Mira** = the most compact option, for dense/data-table UIs.
- **Sera** = editorial/typographic (distinct font pairing in real shadcn, which we
  approximate with just a type-scale bump since we don't ship alternate font assets).
- **Rhea** = newest (May 2026); in real shadcn this adjusts size/gap/density directly
  without changing what Tailwind's spacing utilities mean elsewhere in the app — a
  distinction that doesn't apply to us since we don't generate Tailwind-scale utility
  classes at all.
- Legacy `default`/`new-york`/`new-york-v4` values now resolve to `radix-vega` in real
  shadcn for backward compatibility.

**Implication for `ShadcnRing`:** the `Default`/`NewYork` ring presets added this
session (2dp+2dp-offset vs. 1dp+no-offset) map conceptually to Vega and Nova
respectively, given Nova's lineage from `new-york`. They are **not** currently wired
into `ShadcnStylePreset` automatically — picking "Nova" in the style dropdown does not
yet also switch the ring to `ShadcnRing.NewYork`. Doing so would mean adding a
`ring: ShadcnRing` field to `ShadcnStylePreset` the same way `shapes`/`spacing`/
`typography` already work.
