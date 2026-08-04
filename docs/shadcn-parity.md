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
| **InputGroup** | Now an official shadcn/ui registry component. Real CSS: the container owns the single border (`InputGroupInput` is `border-0 focus-visible:ring-0 flex-1`), and a `has-[:focus-visible]` selector swaps the *container's* border color and draws the ring around the whole group when the inner input is focused | `ShadcnInputGroup` owns the single border/shape (its own container style, no contentPadding — the field keeps its variant padding); tracks focus-within via `onFocusEvent { it.hasFocus }` to swap border color and draw a ring (`dropShadow(theme.focusRingShadow())`) on the container; sets an internal `LocalInsideInputGroup` so a nested `ShadcnTextField`/`ShadcnTextarea` automatically drops its own border/background/ring (no more `variant = Ghost` juggling); field slot wrapped in `weight(1f)` so trailing addons keep intrinsic width | focus-within on container ✅ | ✅ **fixed 2026-07-07** — three real bugs found via catalog screenshots and fixed: (1) inner field drew its own border+ring on focus (double border), (2) trailing addon got squeezed into one-character-per-line vertical text because the field's hardcoded `fillMaxWidth()` ate the row, (3) `BasicTextField` value text/cursor weren't themed (near-invisible in dark mode) — now passed explicitly via `textStyle`/`cursorBrush`, same lesson as `ChipVariant.contentColor`. Verified via `input_group_*` goldens | remaining gap: no `variant` parameter (container hardcoded to the Default look) — not yet requested |
| **Textarea** | Not a distinct shadcn/ui primitive — a plain `<textarea>` sharing Input's classes | composition wrapper around `ShadcnTextField`-equivalent styling | inherits TextField's states | — | not re-verified against real source this pass |
| **InputOTP** | Active slot is `data-[active=true]:border-ring data-[active=true]:ring-[3px] data-[active=true]:ring-ring/50` (verified against real `input-otp.tsx`) — border *color* swaps to `--ring`, width stays 1px, plus the same shared `ring-[3px] ring-ring/50` every other focusable component draws | `ShadcnInputOTP`'s active `OtpSlot` | idle, active (crisp shared ring via `dropShadow`) | 36dp slot size, no real counterpart found (real registry doesn't fix a slot size) | ✅ **fixed 2026-07-09** — the active slot previously grew its own border to a hardcoded 2dp and skipped `shadcnFocusRing` entirely: a real parity gap (real shadcn keeps border width at 1px and layers the shared offset ring on top) *and* a real inconsistency (the only component with its own bespoke ring value instead of the shared one). Border width now stays 1dp; `dropShadow(theme.focusRingShadow())` added to a plain conditional in its Style block (no real per-slot focus event to hook a `focused { }` predicate to). Zero screenshot coverage of the active state before this fix (`focused_light`/`focused_dark` added, verified pixel-level: 3px ring at `(208,208,208)` outside a solid `borderFocus`-colored 1dp border, matching every other component) |
| **Card** | No variant/size prop — single style, `rounded-xl border bg-card py-6 shadow-sm` (border **and** shadow always together); 6 sub-components: `Card`/`CardHeader`(grid, `has-data-[slot=card-action]`)/`CardTitle`/`CardDescription`/`CardAction`(grid-positioned top-right slot)/`CardContent`/`CardFooter` | `CardVariant.Default/Elevated/Filled` × `CardSize.Default/Sm` (axes real has none of); only `ShadcnCardHeader(title: String, description: String?, action)` — no separate `CardTitle`/`CardDescription`/`CardContent`/`CardFooter`/`CardAction` composables | none (static) | n/a — real has no size/variant axis | ❌ `Default` variant has border no shadow, `Elevated` has shadow no border (`CardStyles.kt:54-71`) — real always ships both together, ours splits them across variants that can't coexist; `title`/`description` locked to `String`, no slot for icon+text/formatted content; no addressable `CardAction`/`CardContent`/`CardFooter` |
| **Text** | *(not a real shadcn/ui component — Tailwind text utilities only)* | `ShadcnTextStyle` — 10 fixed presets (Display/Title/Body/Label × Large/Medium/Small), each an atomic fontSize+fontWeight+lineHeight+letterSpacing bundle; `ShadcnText(text, style, muted, maxLines, overflow, color)` has **no per-call fontWeight/fontFamily/letterSpacing/textAlign override** — family is theme-preset-level only | n/a | n/a | Library-specific design decision, not a port. Real Tailwind's independently composable axes (weight scale, family, letter-spacing, align all stack on any size) aren't available — e.g. "BodyMedium but bold" isn't expressible via any param. Worth documenting as a deliberate trade-off |
| **Label** | Single style, `text-sm font-medium`; disabled dims via CSS `peer-disabled:opacity-50` (external, sibling-driven); no required-field indicator | `ShadcnLabel(text, required, disabled)` — single style, matches | real: disabled = opacity-50 dim (externally driven); ours: disabled swaps text to `muted` — and the `required` asterisk stays full-opacity `colors.error` even when `disabled = true` | n/a | `required` asterisk is our own addition (fine) but not wired into the `disabled` state — a disabled+required label shows a full-brightness red asterisk |
| **Avatar** | `size: default/sm/lg` (32/24/40px); `AvatarImage`/`AvatarFallback`; `AvatarBadge` auto-sized per ancestor size; `AvatarGroup` (automatic `-space-x-2` overlap + `ring-2 ring-background` on every child via CSS); separate `AvatarGroupCount` "+N" overflow bubble | `ShadcnAvatarSize.Sm/Default/Lg` = 24/32/40dp ✅ match; `ShadcnAvatarBadge` = 8/10/12dp per size ✅ match; `ShadcnAvatarGroup` is a plain `Row` — no overlap, no automatic ring, doc comment tells the caller to hand-roll `Modifier.offset(...)`; **no `AvatarGroupCount` equivalent exists** | none (static) | ✅ size scale matches exactly | `AvatarGroup`'s overlap+ring is automatic in real, manual boilerplate in ours; `AvatarGroupCount` is a fully missing component |
| **AspectRatio** | Thin Radix passthrough, zero styling/variant/state | `Box(modifier.aspectRatio(ratio))` — equally thin | none either side | n/a | ✅ full parity, both deliberately unstyled |
| **Separator** | `orientation` (horizontal/vertical) + `decorative` boolean (Radix a11y prop, `role="separator"` vs `role="none"`) | `ShadcnSeparatorOrientation.Horizontal/Vertical` matches orientation 1:1; **no `decorative`/semantic-role equivalent** | none (static) | ✅ 1px both sides | Missing `decorative` — no way to mark a separator meaningful-vs-decorative for a11y tooling |
| **Kbd** | Accepts arbitrary children (text+icon together, icons auto-sized); `h-5 w-fit min-w-5 px-1 gap-1`; context-aware style swap inside `TooltipContent`; `KbdGroup` also a real element | `ShadcnKbd(text: String)` — locked to `String`, no composable slot (so no icon-in-chip support); `ShadcnKbdGroup(content)` does take a real slot | none (static) | height/min-width match (20dp = 20px); **padding `spacing.xxs`(2dp) vs. real `px-1`(4px) — half of real**; `KbdGroup` gap same 2dp-vs-4dp gap | Padding/gap pick the wrong step on the (otherwise-matching) spacing scale — should be `spacing.xs` not `xxs`. No icon-slot means no analog to real's icon-in-kbd auto-sizing |
| **Item** | `variant: default/outline/muted` × `size: default/sm`; item itself is focusable/interactive (`focus-visible:ring-[3px]`, nested link gets `hover:bg-accent/50`); `ItemMedia` has 3rd `variant: image` (`size-10 rounded-sm`, object-cover) beyond `default`/`icon`; ships `ItemHeader`/`ItemFooter` slots | `ShadcnItemVariant.Default/Outline/Muted` — names match; `ShadcnItemMediaVariant.Default/Icon` — no `Image` case | ❌ **none** — plain `Row` with `.border()`/`.background()`, no `MutableInteractionSource`/`styleable`/`focused{}` anywhere, so **zero hover or focus ring** | no `size` variant at all — always uses the `default`-equivalent spacing | Missing: `size` variant, `ItemMedia.Image`, `ItemHeader`/`ItemFooter` slots, and every interactive state |
| **Empty** | `EmptyMedia` has `variant: default/icon` — `default` is transparent (no visual), `icon` adds a `size-10 rounded-lg bg-muted` circle | `ShadcnEmptyMedia` unconditionally applies the `icon`-look styling — **no `variant` param at all**, so real's transparent `default` look is unreachable | n/a (static) | media size/radius match (40dp/8dp = real 40px/8px) | Container padding fixed `32dp` vs. real's responsive `p-6`→`md:p-12` (24px→48px) — sizing note, not a state gap |
| **Alert** | `variant: default/destructive`; `AlertTitle` is `line-clamp-1` (single-line truncate); `AlertDescription` renders arbitrary children, gets `text-destructive/90` (dimmed red) under `destructive` | `AlertVariant.Default/Destructive` — names match; `ShadcnAlert(title, description, icon, variant)` — `title`/`description` locked to `String`, no children slot (no way to add an inline action) | none (static) | n/a | ❌ **real bug, not just a gap**: `ShadcnAlert.kt:61` renders description via `ShadcnText(description, muted = true)` — `muted = true` always resolves to `onSurfaceVariant`, **overriding** the ambient `contentColor(colors.error)` the `Destructive` style sets. So the destructive description renders the same neutral gray as `Default`, in every theme, always — real shows dimmed red. Also no `maxLines` cap on the title (real truncates at 1 line, ours wraps) |
| **Progress** | Single style, `value: 0-100`, `h-2 w-full rounded-full`, animated fill | `ShadcnProgress(value: Float)`, single style, `animateFloatAsState` fill | ✅ animated fill matches real's `transition-all` | ✅ height 8dp = 8px; ✅ full radius | ✅ full parity — only difference is `0f..1f` vs. real's raw `0-100`, a units difference not a gap |
| **Skeleton** | Single style, `animate-pulse rounded-md` | `ShadcnSkeleton`, single style, alpha 1↔0.5 pulse | ✅ pulse present both sides (ours 1000ms reverse vs. real's 2s cycle — same visual effect) | ✅ radius matches | ✅ full parity — real ships zero extra props to miss |
| **Spinner** | Wraps Lucide `Loader2Icon`, `size-4 animate-spin`, `role="status" aria-label="Loading"` | `ShadcnSpinner`, hand-drawn `Canvas` arc, continuous rotation | ✅ rotation matches (both 1s linear infinite) | ✅ default 16dp = real 16px | Missing `role`/`aria-label` equivalent — no `contentDescription`/semantics param, so a screen-reader user gets nothing for a bare spinner |
| **Toast** (vs. real `sonner.tsx`, the library shadcn now wraps) | `toast()`/`.success()`/`.error()`/`.warning()`/`.info()`/`.loading()` (each with an icon); `toast.promise()` mutating one toast through 3 states by id; `action`/`cancel` buttons per toast; swipe-to-dismiss; auto-collapsing stack that expands on hover; configurable `position`; `richColors`/`closeButton`; per-toast duration override; pause-on-hover | `ShadcnToastVariant.Default/Success/Error/Warning/Info` — names line up 1:1 with sonner's helpers; `durationMillis = 4000L` default happens to match sonner's real default exactly | auto-dismiss + fade/slide enter-exit — no pause-on-hover, no swipe-to-dismiss | fixed width roughly matches sonner's ~356px default | Biggest real deviation in this batch: no promise-based state transitions, no action/cancel buttons, no swipe-to-dismiss, no stacking/expand-on-hover (permanent stack instead), no `position` choice (hardcoded bottom-end), no `richColors`/`closeButton`; variants render as a plain 8dp dot instead of real's per-variant icon |
| **Collapsible** | Bare Radix passthrough, `open`/`onOpenChange`/`defaultOpen`/**`disabled`** | `ShadcnCollapsible(expanded, onExpandedChange, trigger, content)` — `AnimatedVisibility`, deliberate baked-in top padding (documented workaround for a Compose fade/height timing glitch) | expanded/collapsed only | n/a | Missing `disabled` param entirely |
| **Accordion** | `type: single/multiple` + **`collapsible`** boolean (for `single`, defaults `false` — open item can't self-close unless opted in); trigger has `hover:underline`/`disabled:opacity-50`; content uses a fixed `0.2s ease-out` keyframe | `ShadcnAccordionType.Single/Multiple` — `Single` unconditionally closes on re-click (behaves like real's `collapsible=true` always, no way to get real's non-collapsible default); no `disabled` field; chevron/content animate via spring, not a fixed-duration ease-out | open/closed, focus (ring). Missing: hover, disabled | chevron 14dp (Vega) vs. real 16px — close | Missing `collapsible` toggle (inverted default from real), missing hover/disabled, animation curve doesn't match real's fixed timing |
| **Tabs** | `TabsList` has a **`variant`**: `default` (pill track) or **`line`** (underline indicator) — ours only has the pill look; `Tabs.Root` supports **`orientation: horizontal/vertical`** end-to-end — ours doesn't; trigger has `hover:text-foreground`/`disabled:opacity-50`, active tab gets `shadow-sm` | `ShadcnTabsList(items, selected, onSelectedChange)` — one hardcoded look, no `variant`, always a `Row` | selected, focus (ring). Missing: hover, disabled | real `h-9`(36px) fixed track height; ours grows to content | Missing `line` variant entirely, missing vertical orientation, missing per-trigger hover/disabled |
| **Breadcrumb** | `BreadcrumbLink` accepts `asChild`+children (can carry icons/custom links), `hover:text-foreground`; list wraps (`flex flex-wrap`); ellipsis is a `size-9`(36px) target with `sr-only "More"` | `ShadcnBreadcrumbLink(text: String, onClick)` — locked to plain string, no icon slot, no hover feedback; `Row` doesn't wrap; ellipsis is a bare icon, no fixed touch target or accessible label | none — no hover/focus indication anywhere | ellipsis real 36×36px target vs. ours a bare 14dp glyph | Can't carry an icon/composable in a link, no hover feedback, no wrap, no accessible ellipsis label |
| **Tooltip** | Dual show-on-hover **and** show-on-keyboard-focus (Radix); `sideOffset={0}`; renders a real `Arrow`; `align` stays Radix default `center`, independent of `side` | Opens purely via `hoverable()` — **no focus-triggered display at all**; inherits shared `offset = 4.dp` instead of real's `0`; no arrow (documented); `side`+`align` collapsed into one `Top/Bottom/Start/End` enum | hover only. Missing: keyboard-focus trigger | padding ~2px off vertically (`spacing.xs`=4dp vs. real's 6px) | Missing keyboard-focus trigger is a real a11y gap; offset mismatch (4dp vs. real 0); no independent `align` axis |
| **Popover** | `align="center"` default, `sideOffset={4}`, fixed `w-72`(288px); ships `PopoverAnchor` (separate anchor vs. trigger) + `PopoverHeader`/`Title`/`Description` slots; `modal` (focus trap) toggle | `ShadcnPopover(expanded, onDismissRequest, placement)` — width ✅ matches (288dp), offset ✅ matches (4dp); no `align`, no separate anchor, no header/title/description slots, no `modal` | open/closed only | ✅ width and padding both match real exactly | Missing `align` axis, `PopoverAnchor`, header/title/description slots, `modal` behavior |
| **HoverCard** | Real Radix defaults: **`openDelay=700ms`, `closeDelay=300ms`** — only opens after ~0.7s continuous hover, lingers ~0.3s after pointer-out; `align="center"`, `sideOffset={4}`, fixed `w-64`(256px) | Toggles purely on `collectIsHoveredAsState()` — **instant** open, instant close, no delay of any kind | hover only, 0ms delay both directions | ✅ width (256dp) and padding both match real exactly | ❌ **headline gap**: missing the open/close delay entirely — the whole point of HoverCard vs. Tooltip is that delay; ours is behaviorally identical to an instant-hover Tooltip. Also missing independent `align` axis |
| **DropdownMenu** | 15 exports: `Trigger/Content/Group/Item(inset,variant=default\|destructive)/CheckboxItem/RadioGroup/RadioItem/Label(inset)/Separator/Shortcut/Sub/SubTrigger/SubContent`; full keyboard nav (arrow/Home/End/typeahead) from Radix | `ShadcnDropdownMenuItem(label,onClick,enabled,destructive)`, `Label`, `Separator` — 5 pieces; `clickable(indication=null)` means **no hover/press visual**; **no keyboard nav** | enabled/disabled + destructive text color only | ❌ fixed `.width(224.dp)` always; real is `min-w-[8rem]`(128px) and grows/shrinks with content | Missing `CheckboxItem`, `RadioGroup`/`RadioItem`, `Sub`/`SubTrigger`/`SubContent` (**no submenu support at all**), `Shortcut`, `inset`, `Group`; zero focus ring unlike every other focusable component in the library |
| **ContextMenu** | Same 15-export shape as DropdownMenu on `ContextMenuPrimitive`; opens via right-click **or** long-press (touch) **or** the OS context-menu key | `ShadcnContextMenu` detects right-click via `pointerInput`+`isSecondaryPressed`, reuses `ShadcnDropdownMenuScope` (inherits its missing sub-components) | same as DropdownMenu: text-color only, no hover/press/focus, no keyboard nav | fixed `.width(224.dp)`, same grow-to-fit gap as DropdownMenu | No touch long-press, no keyboard-triggered open; same CheckboxItem/RadioItem/Sub/Shortcut gaps (shared root cause with DropdownMenu) |
| **Command** | `Command/CommandDialog/CommandInput/CommandList/CommandEmpty/CommandGroup/CommandItem/CommandShortcut/CommandSeparator`, built on `cmdk` — real fuzzy `command-score` filtering, full keyboard nav (arrows move selection, Enter selects) | Single `ShadcnCommand(groups, placeholder, emptyText)` — plain case-insensitive substring filter, not fuzzy scoring; **no keyboard navigation at all** — no arrow-key highlight, no Enter-to-select | text filtering only, pointer-click-to-select only | fixed `.width(280.dp)`; no scroll-height cap (real caps at `max-h-[300px]`) | **No `CommandDialog` (⌘K palette) equivalent at all** — the single biggest gap, since that's cmdk's headline use case. Missing keyboard-driven selection undercuts the component's whole reason to exist |
| **Menubar** | 16 exports on `MenubarPrimitive`; Radix roving-focus means **hovering** an adjacent trigger switches the open menu, no extra click needed | `ShadcnMenubarMenu`/`ShadcnMenubar` — reuses DropdownMenu's items (same missing CheckboxItem/RadioItem/Sub/Shortcut); toggle-on-click only, **no hover-to-switch between open menus**, no keyboard nav | open-highlight per trigger, click-toggle only | real bar fixed `h-9`(36px); ours has no fixed height | Missing the hover-switches-open-menu interaction that makes a real menubar feel native, plus the same CheckboxItem/Sub/Shortcut gaps as DropdownMenu |
| **NavigationMenu** | 8 exports + `navigationMenuTriggerStyle` helper; real `Viewport` is one shared, absolutely-positioned container all panels animate into; `Indicator` renders the active-trigger caret | Single `ShadcnNavigationMenu(items)` — each item with a panel gets its **own** independently-positioned popup, not a shared viewport (already self-documented as an approximation); no `Indicator`; **no keyboard nav** despite Radix providing full roving-tabindex | open-highlight + focus ring (the one component in this menu batch that does have a focus ring) | real trigger fixed `h-9`(36px); ours has no fixed height | No `List`/`Item`/`Trigger`/`Content`/`Link`/`Indicator`/`Viewport` split — flattened into one data-driven composable; per-item-popup-vs-shared-viewport gap already flagged in the file's own doc comment |
| **Dialog** | `Trigger/Portal/Close/Overlay/Content(showCloseButton)/Header/Footer(showCloseButton)/Title/Description`; overlay+content both animate open/close (`fade-in-0`/`zoom-in-95`, `duration-200`) | `ShadcnDialog(visible, onDismissRequest, showCloseButton, dismissOnClickOutside, closeIcon)` — library-wide `visible: Boolean` pattern instead of Trigger/Portal composables (not itself a gap) | open/close via `visible`; close button has a focus ring | ❌ fixed `.width(400.dp)` always; real is responsive, `max-w-lg`(512px) cap that shrinks on narrow viewports | ❌ **zero open/close animation anywhere** — `ShadcnModalOverlay` just `if (!visible) return`s, no fade/zoom for either the scrim or the card. Notable since Sheet/Drawer (below) do animate — Dialog is the outlier |
| **AlertDialog** | `size: default/sm` (`max-w-lg`/`max-w-xs`); new `AlertDialogMedia` icon slot (`size-16 rounded-md bg-muted`); `Action`/`Cancel` are real `Button` wrappers via `asChild` | Thin wrapper over `ShadcnDialog` (`showCloseButton=false, dismissOnClickOutside=false`) — doc comment explicitly rationalizes skipping dedicated `Action`/`Cancel` composables since real's are just styled Buttons | inherits Dialog's states — including its zero-animation gap | same fixed 400dp as Dialog, no size variant | Missing `size` prop and `AlertDialogMedia` slot (both added to the real registry since an earlier pass) — genuinely new divergence, not stale. `Action`/`Cancel`-as-Button omission is a defensible, already-documented design call |
| **Sheet** | `Trigger/Close/Portal/Overlay/Content(side: top/right/bottom/left, showCloseButton)/Header/Footer/Title/Description`; close X shown **by default**; both entry+exit animate (slide-in/slide-out per side); left/right `w-3/4 sm:max-w-sm`(75%/384px cap) | `ShadcnSheet(visible, onDismissRequest, side: Top/Bottom/Start/End)` — reuses Dialog's Header/Title/Description/Footer | ✅ entrance slide-in matches real's direction; ❌ **no exit animation** (documented: `ShadcnModalOverlay` tears the Popup down instantly, no window to animate an exit in); ❌ **no close (X) button at all** — `ShadcnDialog` has `showCloseButton`, `ShadcnSheet` has no equivalent param | ❌ Start/End fixed `.width(320.dp)` regardless of screen — real's 75%-viewport/384px-cap rule isn't matched either way | Missing close button is the standout item — real shows one by default |
| **Drawer** | Built on the `vaul` package — real drag-to-dismiss physics live inside `vaul` itself; handle bar shown only for `bottom` direction; top/bottom capped `max-h-[80vh]` with margin off the far edge; left/right `w-3/4 sm:max-w-sm` | `ShadcnDrawer(visible, onDismissRequest, direction, dismissThresholdFraction=0.3f)` — **real drag-to-dismiss is genuinely implemented**: `Modifier.draggable` + `Animatable` offset + a pure, unit-tested `shouldDismissDrawer()`; handle bar shown only for `Bottom`, matching real | ✅ drag animates live, spring-back under threshold, dismiss over threshold (confirms this is real, not a stub — the one place in the whole sweep where the "obviously unreplicated" assumption was wrong); ⚠️ dismiss decision is purely distance-based, no velocity/fling term like real vaul | ❌ Start/End fixed `.width(320.dp)`, same 75%/384px-cap mismatch as Sheet; ❌ Top/Bottom has **no height cap and no margin off the far edge** — can grow to fill the entire screen, real never exceeds 80vh | Rounded-corner-per-direction logic correctly mirrors real. No close button in either real Drawer or ours (not a gap — real Drawer doesn't default one) |
| **Select** | `Select/SelectGroup/SelectValue/SelectTrigger(size: sm/default)/SelectContent/SelectLabel/SelectItem/SelectSeparator/SelectScrollUpButton/SelectScrollDownButton`; trigger fixed `h-9`(default)/`h-8`(sm); `disabled:opacity-50` | `ShadcnSelect<T>(value, options, onValueChange, label, placeholder, variant=Default, icon)` — flat list only; `SelectVariant` has only `Default`; **no `disabled` param at all** | focus ring, hover/pressed via Style API | No size variant, no fixed trigger height (grows to content padding) vs. real's explicit `h-9`/`h-8` | No `SelectGroup`/`SelectLabel`/`SelectSeparator` — grouped/labeled option lists aren't representable; no scroll up/down buttons; no `disabled`. Own KDoc already discloses "no keyboard nav/typeahead yet," still accurate |
| **Combobox** | ⚠️ **Stale assumption corrected**: real `combobox.tsx` is *no longer* a Popover+Command recipe — as of a recent commit it's a first-class Base-UI-backed registry component (`Combobox/ComboboxValue/Trigger/Clear/Input/Content/List/Item/Group/Label/Empty/Separator/Chips/Chip`), with search built into the trigger, multi-select chips, a clear button, and grouping | `ShadcnCombobox<T>` — one hand-rolled composable, single-select only, trigger button + separate dropdown panel with search field inside (matching the *old* recipe shape) | expanded/collapsed, filter-as-you-type, row select; **no `disabled`**, no clear button, no chips/multi-select, no grouping | fixed `.width(200.dp)` on both trigger and panel, kept in sync only by matching literals — real anchors width to the trigger (`w-(--anchor-width)`) | Two findings: (1) real shadcn/ui moved on from the recipe shape ours still matches — worth flagging in `ComboboxDoc.kt` the way `DatePickerDoc.kt` already discloses its own recipe status; (2) **internal doc inconsistency**: `ShadcnCommand`'s own KDoc claims it's "the shared building block behind... `ShadcnCombobox`" — false as written, `ShadcnComboBox.kt` never calls `ShadcnCommand`, it reimplements filtering/rows from scratch |
| **DatePicker** | Confirmed still a recipe, not a component (`ui.shadcn.com` docs verified live): `Popover > PopoverTrigger(Button variant="outline", icon + date-or-placeholder) > PopoverContent > Calendar(mode="single")` | `DatePickerDoc.kt` composes `ShadcnButton(Outline)` + `ShadcnPopover` + `ShadcnCalendar` — matches the real recipe almost line-for-line, including the same ternary logic for the placeholder text | open/closed, date-selected vs. placeholder mirrored exactly, closes on select | n/a — a recipe has no sizing of its own | ✅ the clean case in this whole sweep — already discloses its own recipe status in its doc comment. One *inherited* (not new) gap: `ShadcnCalendar` has no `mode` param (single-date only) |
| **Table** | `Table/Header/Body/Footer/Row/Head/Cell/Caption`; wraps itself in an `overflow-x-auto` scroll container; row `hover:bg-muted/50`; `TableFooter` is `border-t bg-muted/50 font-medium` | `ShadcnTable`/`HeaderRow`/`Row`/`HeadCell`/`Cell`/`Caption` — `selected`/`muted` bool flags exist, no hover state | `selected` swaps row bg; `muted` dims cell text | header/body padding matches (`spacing.sm`=8dp=8px) | ❌ no `TableFooter` equivalent; ❌ no hover state; ❌ no horizontal-scroll container — `ShadcnTable` is a plain `Column`, long rows just overflow instead of scrolling |
| **Pagination** | `Pagination/Content/Item/Link(isActive→variant swap)/Previous/Next(icon + "Previous"/"Next" text, hidden only below `sm:` breakpoint, not absent)/Ellipsis` | Single monolithic `ShadcnPagination(items, currentPage, onPageChange, hasPrevious, hasNext)` — no separate `PaginationLink`/`Item`/`Content` composables exposed | active→Outline, inactive→Ghost variant swap matches real; prev/next `enabled` dims tint | page buttons 36dp match real's `size="icon"`(36px); ❌ prev/next forced into the same icon-only 36dp box vs. real's wider text-bearing button | **No "Previous"/"Next" text label ever renders** — real always renders it in the DOM (just hidden below a breakpoint, not absent); no sub-primitives for custom markup/ordering |
| **ScrollArea** | `Root`+`Viewport`(has a `focus-visible:ring-[3px]`)+`ScrollBar`(one explicit element per axis)+`Corner`; thumb `rounded-full`, bar `w-2.5`/`h-2.5`(10px) | `ShadcnScrollArea(orientation: Vertical/Horizontal/Both)` — single composable, `Both` renders both bars internally; thumb drag reimplemented manually | drag-to-scroll on thumb matches Radix's draggable thumb; no hover-reveal/auto-hide | thumb thickness 6dp vs. real 10px ❌ mismatch | No focus-visible ring on the viewport; no `Corner` element — a live gap since `Both` can show two bars with nothing filling the corner |
| **Chart** | Type-agnostic wrapper: `ChartContainer`(injects per-series CSS color vars, wraps Recharts) + `ChartTooltip`/`ChartTooltipContent`(indicator styles, formatters) + `ChartLegend`; actual chart types delegated to Recharts children, not baked in | `ShadcnChartContainer`(aspect-ratio box only) + `ShadcnBarChart`/`LineChart` drawn directly via `Canvas` — architecturally inverted (chart type baked in vs. real's type-agnostic wrapper) | ❌ **no tooltip of any kind** — real's largest sub-component (~150 lines) has zero equivalent, no hover/interaction state anywhere | aspect ratio 16:9 matches real's `aspect-video` default | No light/dark per-series color switching (real's `ChartConfig.theme` has no analog — fixed single `Color` per series); no pie/area/radar variants |
| **Calendar** | Forwards react-day-picker's entire prop surface: `mode: single/multiple/range`, `disabled` predicate, `numberOfMonths`, `captionLayout` (dropdown month/year), `showWeekNumber` | `ShadcnCalendar(year, month, onMonthChange, selected: ShadcnCalendarDate?, onSelectedChange, today)` — single month, single-date only | selected (filled), today (border only — real fills today unless also selected), focused/roving cursor ring (no arrow-key nav wired, self-documented) | cell 36dp vs. real's default 32px — close, and real's is itself overridable | ❌ **no `multiple`/`range` mode at all** (no range_start/middle/end concept) — the single largest capability gap in this batch, by design (no kotlinx-datetime dependency); no multi-month, no dropdown caption, no `disabled` dates predicate |
| **Carousel** | embla-carousel-react: `opts`/`plugins` (incl. autoplay), `orientation: horizontal/vertical`, keyboard arrow nav; Prev/Next self-positioned `absolute rounded-full size-8` | `ShadcnCarousel(state: PagerState, orientation)` wraps `HorizontalPager`/`VerticalPager` — **orientation is supported**; `ShadcnCarouselPrevious`/`Next`; `ShadcnCarouselDots` is our own addition (no real counterpart, like Chip) | prev/next `enabled` maps to real's `canScrollPrev`/`canScrollNext` | Prev/Next 32dp match real's `size-8`(32px) | ❌ no autoplay; ❌ no loop (`coerceAtLeast/AtMost` clamps instead of wrapping, real exposes embla's `opts.loop`); ❌ no keyboard arrow-key handling; Prev/Next must be manually offset by the caller instead of self-positioning like real |
| **Resizable** | `ResizablePanelGroup`+`ResizablePanel`(arbitrary N panels, passthrough to `react-resizable-panels` — `defaultSize`/`minSize`/`maxSize`/`collapsible`/`onResize` per panel) + `ResizableHandle`(keyboard-focusable, arrow-key resizing, `focus-visible:ring-1`) | `ShadcnResizablePanelGroup` hardcoded to exactly **two** panes (already self-documented as a deliberate scope reduction); `ShadcnResizableHandle` — drag-to-resize only, unit-tested | drag-to-resize only; **no keyboard/focus state on the handle at all** | handle 1dp matches real's 1px; grip icon roughly matches real's `h-4 w-3`, orientation-swapped correctly | Hardcoded to two panes vs. real's arbitrary-N passthrough; no per-panel min/max/default size; no keyboard-driven resize or focus ring on the handle |
| **Sidebar** | 21 exports: `Provider`(cookie-persisted state, Cmd/Ctrl+B shortcut, mobile breakpoint→renders a Sheet instead)+`Sidebar`(`side`, `variant: sidebar/floating/inset`, `collapsible: offcanvas/icon/none`)+`Trigger`/`Rail`(separate drag strip)/`Inset`/`Input`/`Header`/`Footer`/`Separator`/`Content`/`Group`/`GroupLabel`/`GroupAction`/`GroupContent`/`Menu`/`MenuItem`/`MenuButton`(variant/size/tooltip-when-collapsed)/`MenuAction`/`MenuBadge`/`MenuSkeleton`/`MenuSub`/`MenuSubItem`/`MenuSubButton`+`useSidebar()` | 10 composables: `ShadcnSidebarProvider`(`expanded`/`onExpandedChange` only — no cookie/shortcut/mobile-switch, self-documented as deliberate) + `Sidebar`(single `width`, offcanvas-only) + `Inset`/`Trigger`/`Header`/`Footer`/`Group`(optional label)/`MenuItem`(inline `badge: String?`)/`Menu` | expanded/collapsed (animated width) matches real's `data-state`; active-item highlight matches real's `data-[active=true]`; ❌ collapsed content is fully unmounted rather than kept-mounted-but-hidden like real | default width 240dp vs. real's 256px default — both just defaults, not hardcoded | ❌ only one collapse mode (no `icon` persistent-rail mode, no `variant`, no `side`); ❌ **entire nested-submenu family absent** (`MenuSub`/`SubItem`/`SubButton`) despite nested nav being a common real pattern; ❌ no `Rail`/`Input`/`GroupAction`/`GroupContent`/`MenuAction`/`MenuSkeleton`; no mobile/Sheet switch, no cookie persistence, no keyboard shortcut — the largest single reduction in the whole sweep, already self-documented as intentional |
| **Field** | `FieldSet/FieldLegend(variant: legend/label)/FieldGroup/Field(orientation: vertical/horizontal/responsive)/FieldContent/FieldLabel(card-style `has-data-[state=checked]` variant)/FieldTitle/FieldDescription/FieldSeparator/FieldError(accepts an `errors` array, dedupes, renders single message or a list)` | `ShadcnFieldGroup`/`ShadcnField(orientation: Vertical/Horizontal)`/`ShadcnFieldLabel`/`Description`/`Error`(single `String` only)/`Separator` | required/disabled forwarded to label; vertical/horizontal orientation renders Column/Row; no `responsive` orientation | spacing close-but-not-identical (24dp vs. real's 28px `gap-7`; 8/12dp vs. real's 12px `gap-3`) — same tolerance class as §3 | Mostly already-acknowledged (own doc comments cite Compose having no native `<fieldset>` semantics): no `responsive` orientation, no `FieldSet`/`FieldLegend`/`FieldContent`, no card-style checked `FieldLabel` variant; `FieldError` takes one `String` not real's deduped array+list. No-form-library stance is intentional and documented |
| **Stepper** | *(not a real shadcn/ui registry component — community/shadcn-studio pattern, no comparison possible)* | `ShadcnStepper(steps, currentStep, orientation, showLabels)` — completed/active/upcoming states, purely presentational (mirrors Tabs/Accordion's stance per its own KDoc) | n/a | n/a — no real counterpart to size against | Modeled on shadcn-studio's community stepper per its own KDoc, not a shadcn/ui port. Minor: vertical variant's fixed connector height isn't measured against actual label height, so a long two-line description could visually disconnect |
| **Message** (AI Elements, real source: `vercel/ai-elements` `message.tsx`) | `Message`(`max-w-[95%]`, only `from="user"` gets right-align) + `MessageContent`(bubble chrome applied **only** for `from="user"` — assistant renders as plain unstyled text) + `MessageActions`/`Action`(hover-revealed tooltip buttons) + full `MessageBranch*` family (branch/regenerate navigator) + `MessageResponse`(markdown) + `MessageToolbar`. **No avatar rendering anywhere in real `Message`** | `ShadcnMessage`(avatar+content slots, `Start`/`End` align) + `ShadcnMessageAvatar` + `ShadcnMessageGroup`/`Header`/`Footer` | none — purely structural, no hover-actions, no branch-nav, no toolbar | real caps at `max-w-[95%]` on `Message` itself; ours caps width on `ShadcnBubble` instead — different anchor point | Missing `MessageActions`/`Action`, the entire `MessageBranch*` navigator, `MessageToolbar`. Our avatar-in-Message-row is a shadcn-compose invention — real `Message` never touches an avatar, that's left fully to the consumer |
| **Bubble** | *No `bubble.tsx` exists in `vercel/ai-elements` (confirmed via repo tree + empty commit history)* — the real bubble look is inlined into `message.tsx`'s `MessageContent`, `user`-only, single `bg-secondary` treatment, no variant system, no "reactions" concept anywhere in the repo | `ShadcnBubble`/`Group`/`Content`(6 variants) + `ShadcnBubbleReactions`(reaction pill) | n/a — static variants | `fillMaxWidth(0.8f)` vs. real's differently-anchored `max-w-[95%]` on `Message` | **shadcn-compose-only addition**, no real source to diff — an intentional divergence. `ShadcnBubbleReactions` has zero real counterpart anywhere in ai-elements |
| **Attachment** (real source: `attachments.tsx`, plural) | `Attachments`(`variant: grid/inline/list`) + `Attachment`(data-driven over already-resolved file data — **no lifecycle-state enum in real at all**) + `AttachmentPreview`(category-dispatched icon) + `AttachmentInfo` + `AttachmentRemove` + `AttachmentHoverCard*` + `AttachmentEmpty` | `ShadcnAttachmentState.Idle/Uploading/Processing/Error/Done` + `Size.Default/Sm/Xs` + `Orientation.Horizontal/Vertical` + `Media`/`Content`/`Title`(shimmers while uploading)/`Description`/`Actions`/`Group` | Idle/Uploading/Processing/Error/Done vs. real's none (real is purely presentation over resolved data, ours adds an upload-lifecycle axis real doesn't have) | thumbnail fixed 40dp all orientations vs. real's per-variant sizes (`grid` 96px/`inline` 20px/`list` 48px) | Two different design axes, not a strict superset/subset: real varies by `grid`/`inline`/`list` layout+sizing+hover-card; ours varies by `orientation`(H/V) crossed with a lifecycle-state axis real doesn't have. Worth flagging that "orientation" and "variant" don't actually correspond, not just listing missing props |
| **Marker** | *No `marker.tsx` exists in `vercel/ai-elements` (confirmed via repo tree + empty commit history)* | `ShadcnMarker`(`Default`/`Separator`/`Border`) + `MarkerIcon`/`Content` | n/a | n/a | **shadcn-compose-only addition**, no real source to diff — same disposition as Bubble |
| **MessageScroller** (real source: `conversation.tsx`) | Wraps `use-stick-to-bottom`'s `StickToBottom`(`initial="smooth" resize="smooth"`, `role="log"`) + `ConversationContent` + `ConversationEmptyState` + `ConversationScrollButton`(conditional on `!isAtBottom`) + `ConversationDownload`(exports transcript to `.md`) | `ShadcnMessageScroller` + `ShadcnMessageScrollerButton`(`Start`/`End` direction) | Threshold-driven button visibility + sticky release-on-manual-scroll (one-way, matches real's "release, don't silently re-engage" semantics). Auto-follow/animated-initial-scroll semantics already verified this session against `conversation.tsx`'s `initial="smooth" resize="smooth"` — correct | n/a | Real gaps not previously flagged: no `ConversationEmptyState` equivalent, no `ConversationDownload`(markdown-export) equivalent |
| **Shimmer** (real source: `shimmer.tsx` — genuinely exists in ai-elements, not a `ui.shadcn.com/docs/utils` page as originally assumed) | `Shimmer` — text-only React component (`children: string`, `as: ElementType`), framer-motion `backgroundPosition` sweep over a `background-clip: text` gradient; `spread` scales the gradient band width with the text's own character count | `Modifier.shadcnShimmer(enabled, color, durationMillis=2000)` — a general-purpose `Modifier`, not text-restricted; `BlendMode.SrcAtop` over an offscreen layer achieves the same "paints only over opaque content" effect, generalized to any composable | n/a — continuous animation, gated by `enabled` | Duration matches (2000ms = real's 2s default) | Real's `spread` scales band width with content length; ours uses a fixed `size.width * 0.6f` regardless of content — reasonable generalization overall, this one behavior has no analog |
| **ScrollFade** | **No real source found anywhere** — not in `vercel/ai-elements` (full repo tree checked), not a live `ui.shadcn.com/docs/utils` page (direct curl confirms 404, not just a redirect chain) | `Modifier.shadcnScrollFade(scrollState, orientation, edgeSize=24.dp)` — masks scrollable edges via `BlendMode.DstIn` alpha ramps, clamped so opposite edges never overlap on a short container | leading/trailing edge fade toggles by scroll position, both fade mid-scroll, each sharpens at its own end | n/a | Reporting as a standalone shadcn-compose capability — genuinely no real source exists to compare against, confirmed by direct lookup, not assumed |

**Cross-cutting findings from the full sweep (2026-08-04), ranked by how many components they touch or how badly they undercut the component's stated purpose:**

1. **`ShadcnAlert` destructive-variant bug, not just a gap**: `ShadcnText(description, muted = true)` inside `ShadcnAlert` unconditionally resolves to `onSurfaceVariant`, overriding the ambient `contentColor(colors.error)` the `Destructive` style sets — the destructive description renders identical neutral gray to `Default`, in every theme, always. Real shows dimmed red. This is a real rendering bug, worth fixing before the other, lower-priority gaps in this list.
2. **`HoverCard` has zero open/close delay** — real's `openDelay=700ms`/`closeDelay=300ms` is the entire reason HoverCard exists as a component distinct from Tooltip; ours is behaviorally identical to an instant-hover Tooltip.
3. **`Dialog`/`AlertDialog` have no open/close animation at all**, while `Sheet`/`Drawer` (same overlay family) do — an inconsistency within the library, not just vs. real shadcn.
4. **Menu-family root cause, fixable once**: `DropdownMenu`, `ContextMenu`, and `Menubar` all funnel through `ShadcnDropdownMenuScope`'s row composables, so the missing `CheckboxItem`/`RadioItem`/`RadioGroup`/`Sub`/`SubTrigger`/`SubContent`/`Shortcut`/`inset` surface is one gap, not three — closing it in `ShadcnDropdownMenu.kt` closes it everywhere that reuses it.
5. **No keyboard navigation anywhere** across `DropdownMenu`/`ContextMenu`/`Command`/`Menubar`/`NavigationMenu`/`Select`/`Accordion`/`Tabs`/`Resizable`'s handle — real gets this for free from Radix's roving-focus primitives; shadcn-compose has no equivalent layer anywhere in the whole sweep.
6. **`Combobox` matches a stale real recipe** — real shadcn/ui moved from "Popover + Command" to a first-class Base-UI-backed component (multi-select chips, clear button, grouping) sometime before this pass; `ShadcnCombobox` still matches the old shape. Separately, `ShadcnCommand`'s own KDoc falsely claims `ShadcnCombobox` is built on it — it isn't, `ShadcnCombobox` reimplements filtering from scratch.
7. **`Sheet` has no close button at all** (real shows one by default); **`Item` has zero interactive states** (no hover, no focus ring, despite real being fully focusable); **`Chart` has zero tooltip support** (real's largest sub-component).
8. **Drawer's drag-to-dismiss is a real, positive surprise** — genuinely implemented and unit-tested, not a stub, the one place in the whole sweep where "obviously unreplicated" was the wrong assumption going in.
9. **`Sidebar` is the single largest surface reduction** (10 composables vs. real's 21 exports — no icon-collapse mode, no nested submenu family, no mobile/Sheet breakpoint switch) but this is already self-documented as a deliberate scope decision in the component's own doc comments, not an oversight.

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
- **Updated 2026-08-04**: full sweep of the remaining 50 components (everything not
  covered by the original 14-component pass), run against `shadcn-ui/ui@main` (commits
  `f0cad1d`/current `main` at fetch time, not pinned to `d8ace42` — re-run before
  trusting this for a release, per the note above) and, for the AI Elements family,
  `vercel/ai-elements@main` (not `shadcn-ui/ui` — that family lives in a separate repo).
  None of this pass's findings are token/color/radius items (§1-§3 already covered
  those); all of it is component-API surface (missing sub-components, missing states,
  missing props) — a different axis the original pass didn't check. See the findings
  list at the end of §4 for the highest-priority items.

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

**`ShadcnRing` genuinely does vary per preset -- two separate findings, two corrections.**

*Finding 1 (2026-07-08):* an earlier session wired `Vega -> ShadcnRing.Default` and
`Nova -> ShadcnRing.NewYork`, analogized from real shadcn's *retired* two-style
`shadcn init` picker (`Default`/`New York`), which real shadcn now collapses to
`radix-vega` for back-compat -- not something the current 8-style system exposes at
all, and only applied to 2 of 8 presets besides. Reverted: `ring` became a flat,
preset-independent `ShadcnTheme(ring = ...)` parameter, and `ShadcnRing.Default`/
`.NewYork` were deleted outright (zero real callers anywhere in the codebase, no
catalog UI path to reach them either).

*Finding 2 (2026-07-09):* that revert was **also wrong**, just in the opposite
direction -- it assumed ring doesn't vary by style at all, without ever checking the
*current* 8-style system's own real CSS. It does vary. Verified directly against
`shadcn-ui/ui`'s `apps/v4/registry/styles/style-<name>.css` files (not the retired
axis, not an analogy -- each style's own `.cn-button` rule, checked file by file):

| Style | ring width | ring opacity |
|---|---|---|
| Vega | 3px | 50% |
| Nova | 3px | 50% |
| Maia | 3px | 50% |
| Lyra | 1px | 50% |
| Mira | 2px | 30% |
| Luma | 3px | 30% |
| Sera | 2px | 30% |
| Rhea | 3px | 30% |

`ring-offset-*` appears in none of the 8 files -- offset stays 0 for every style, real
and ours. `ShadcnStylePreset` now carries these real per-style values via a `ring:
ShadcnRing` field again (same shape as the Finding-1 revert undid, but populated from
verified source this time, not a cross-axis analogy). `ShadcnTheme(ring = preset.ring)`
by default -- still independently overridable, same pattern as `baseRadius`.
**Lesson generalized:** "we checked and it doesn't vary" is only trustworthy if the
check was against the *actual axis in question* -- Finding 1's revert checked the
retired two-style system (irrelevant) and concluded the current system was flat without
ever looking at the current system's own files.

---

## 7. Focus ring implementation: `Modifier.shadcnFocusRing` retired in favor of `dropShadow`

Removed entirely (2026-07-09). The custom `Modifier.drawWithContent` + manual per-corner
`RoundRect` reimplementation (~90 lines, `styles/FocusRing.kt`) was justified by a claim
that the Style API's `dropShadow` "always rasterizes through an offscreen bitmap...
visibly blurs a ring this thin," even at `radius = 0.dp`. That claim was never actually
tested — checked directly this session via a screenshot test of
`dropShadow(radius = 0.dp, spread = 3.dp)`: perfectly crisp (only ordinary single-pixel
edge anti-aliasing, the same any vector draw call has), zero effect on the element's
measured layout size, exactly matching real CSS `box-shadow`. The custom modifier was
solving a problem that didn't exist.

Every focusable component now calls `dropShadow(theme.focusRingShadow())` directly
inside its own `focused { }` block (`ShadcnThemeData.focusRingShadow()` in
`styles/FocusRing.kt` builds the `Shadow` value: `radius = 0.dp`,
`spread = ring.width + ring.offset`, color = `colors.borderFocus` at `ring.opacity`).
Components whose "active" state is a *computed* boolean rather than a real per-node
focus event (`ShadcnInputOTP`'s `OtpSlot`, `ShadcnInputGroup`'s `hasFocusWithin`,
`StylePresetMatrixTest`'s ring swatch) use a plain `if (isActive) dropShadow(...)`
inside the `Style { }` body instead, since there's no real `InteractionSource` to drive
a `focused { }` predicate.

`dropShadow` always follows the Style's own **final resolved** `shape()` — including a
later `style`-parameter override from `ShadcnButtonGroup`/`ShadcnToggleGroup` (their
per-position corner-stripped shape, merged in via `.styleable(...)`'s
override-not-additive cascade) — so `ShadcnButton`/`ShadcnToggle` need no special-case
shape handling at all; the group's own corner-stripping "just works" for the ring too.
Verified via `ButtonGroupScreenshotTest.with_label_focused`: the ring appears only at
the outer rounded ends, never at the internal flush seam between grouped items —
pixel-checked directly (`(229,229,229)` ring band at the outer edge, no separate band at
the seam). This also means `LocalGroupCorners` (still provided by
`ShadcnButtonGroup`/`ShadcnToggleGroup`) has **no remaining reader** — only the old ring
modifier ever consumed it. Candidate for a follow-up cleanup, not yet done.

**Lesson generalized:** the same failure mode as §6 — a specific, falsifiable technical
claim ("dropShadow blurs at radius=0") was carried forward across sessions and cited as
justification for real code complexity, without ever being tested. Confirmed with a
one-off screenshot test, in under a minute, once someone actually asked "why not just
use dropShadow?"
