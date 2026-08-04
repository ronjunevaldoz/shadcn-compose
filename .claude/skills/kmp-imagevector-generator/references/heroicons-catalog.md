# Heroicons Catalog Reference

Source of truth: https://github.com/tailwindlabs/heroicons (`optimized/` directory).
Snapshot taken 2026-07-07 against the `master` branch — **324 icon names**, verified
identical across Outline, Solid, and Mini; Micro excludes 8 (listed below).

Use this file to validate an icon name + variant **before** constructing a fetch URL.
Do not guess an icon name — if it is not in the list below, it does not exist in this
icon set; check https://heroicons.com in a browser or re-fetch the GitHub directory
listing instead of hallucinating a plausible-sounding name.

---

## Variant keywords

| Keyword (as the user says it) | Size  | Style  | Repo path                     | Stroke/fill |
|--------------------------------|-------|--------|--------------------------------|-------------|
| **Outline**                    | 24×24 | Line   | `optimized/24/outline/<name>.svg` | `stroke="currentColor"`, `fill="none"` |
| **Solid**                       | 24×24 | Filled | `optimized/24/solid/<name>.svg`   | `fill="currentColor"` |
| **Mini** (Solid Mini)            | 20×20 | Filled | `optimized/20/solid/<name>.svg`   | `fill="currentColor"` |
| **Micro** (Solid Micro)          | 16×16 | Filled | `optimized/16/solid/<name>.svg`   | `fill="currentColor"` |

Raw fetch URL template:
```
https://raw.githubusercontent.com/tailwindlabs/heroicons/master/optimized/<size>/<style>/<name>.svg
```

Example — "bell, solid, mini":
```
https://raw.githubusercontent.com/tailwindlabs/heroicons/master/optimized/20/solid/bell.svg
```

**Micro exclusions** — these 8 icons exist in Outline/Solid/Mini but have no Micro (16×16)
variant; fall back to Mini if a Micro fetch 404s on one of these:
```
arrow-left-on-rectangle, arrow-right-on-rectangle, arrow-small-down, arrow-small-left,
arrow-small-right, arrow-small-up, minus-small, plus-small
```

---

## Full icon name list (324, alphabetical, Outline/Solid/Mini)

```
academic-cap, adjustments-horizontal, adjustments-vertical, archive-box, archive-box-arrow-down,
archive-box-x-mark, arrow-down, arrow-down-circle, arrow-down-left, arrow-down-on-square,
arrow-down-on-square-stack, arrow-down-right, arrow-down-tray, arrow-left, arrow-left-circle,
arrow-left-end-on-rectangle, arrow-left-on-rectangle, arrow-left-start-on-rectangle,
arrow-long-down, arrow-long-left, arrow-long-right, arrow-long-up, arrow-path,
arrow-path-rounded-square, arrow-right, arrow-right-circle, arrow-right-end-on-rectangle,
arrow-right-on-rectangle, arrow-right-start-on-rectangle, arrow-small-down, arrow-small-left,
arrow-small-right, arrow-small-up, arrow-top-right-on-square, arrow-trending-down,
arrow-trending-up, arrow-turn-down-left, arrow-turn-down-right, arrow-turn-left-down,
arrow-turn-left-up, arrow-turn-right-down, arrow-turn-right-up, arrow-turn-up-left,
arrow-turn-up-right, arrow-up, arrow-up-circle, arrow-up-left, arrow-up-on-square,
arrow-up-on-square-stack, arrow-up-right, arrow-up-tray, arrow-uturn-down, arrow-uturn-left,
arrow-uturn-right, arrow-uturn-up, arrows-pointing-in, arrows-pointing-out, arrows-right-left,
arrows-up-down, at-symbol, backspace, backward, banknotes, bars-2, bars-3, bars-3-bottom-left,
bars-3-bottom-right, bars-3-center-left, bars-4, bars-arrow-down, bars-arrow-up, battery-0,
battery-100, battery-50, beaker, bell, bell-alert, bell-slash, bell-snooze, bold, bolt, bolt-slash,
book-open, bookmark, bookmark-slash, bookmark-square, briefcase, bug-ant, building-library,
building-office, building-office-2, building-storefront, cake, calculator, calendar,
calendar-date-range, calendar-days, camera, chart-bar, chart-bar-square, chart-pie,
chat-bubble-bottom-center, chat-bubble-bottom-center-text, chat-bubble-left,
chat-bubble-left-ellipsis, chat-bubble-left-right, chat-bubble-oval-left,
chat-bubble-oval-left-ellipsis, check, check-badge, check-circle, chevron-double-down,
chevron-double-left, chevron-double-right, chevron-double-up, chevron-down, chevron-left,
chevron-right, chevron-up, chevron-up-down, circle-stack, clipboard, clipboard-document,
clipboard-document-check, clipboard-document-list, clock, cloud, cloud-arrow-down, cloud-arrow-up,
code-bracket, code-bracket-square, cog, cog-6-tooth, cog-8-tooth, command-line, computer-desktop,
cpu-chip, credit-card, cube, cube-transparent, currency-bangladeshi, currency-dollar,
currency-euro, currency-pound, currency-rupee, currency-yen, cursor-arrow-rays,
cursor-arrow-ripple, device-phone-mobile, device-tablet, divide, document, document-arrow-down,
document-arrow-up, document-chart-bar, document-check, document-currency-bangladeshi,
document-currency-dollar, document-currency-euro, document-currency-pound, document-currency-rupee,
document-currency-yen, document-duplicate, document-magnifying-glass, document-minus,
document-plus, document-text, ellipsis-horizontal, ellipsis-horizontal-circle, ellipsis-vertical,
envelope, envelope-open, equals, exclamation-circle, exclamation-triangle, eye, eye-dropper,
eye-slash, face-frown, face-smile, film, finger-print, fire, flag, folder, folder-arrow-down,
folder-minus, folder-open, folder-plus, forward, funnel, gif, gift, gift-top, globe-alt,
globe-americas, globe-asia-australia, globe-europe-africa, h1, h2, h3, hand-raised,
hand-thumb-down, hand-thumb-up, hashtag, heart, home, home-modern, identification, inbox,
inbox-arrow-down, inbox-stack, information-circle, italic, key, language, lifebuoy, light-bulb,
link, link-slash, list-bullet, lock-closed, lock-open, magnifying-glass, magnifying-glass-circle,
magnifying-glass-minus, magnifying-glass-plus, map, map-pin, megaphone, microphone, minus,
minus-circle, minus-small, moon, musical-note, newspaper, no-symbol, numbered-list, paint-brush,
paper-airplane, paper-clip, pause, pause-circle, pencil, pencil-square, percent-badge, phone,
phone-arrow-down-left, phone-arrow-up-right, phone-x-mark, photo, play, play-circle, play-pause,
plus, plus-circle, plus-small, power, presentation-chart-bar, presentation-chart-line, printer,
puzzle-piece, qr-code, question-mark-circle, queue-list, radio, receipt-percent, receipt-refund,
rectangle-group, rectangle-stack, rocket-launch, rss, scale, scissors, server, server-stack, share,
shield-check, shield-exclamation, shopping-bag, shopping-cart, signal, signal-slash, slash,
sparkles, speaker-wave, speaker-x-mark, square-2-stack, square-3-stack-3d, squares-2x2,
squares-plus, star, stop, stop-circle, strikethrough, sun, swatch, table-cells, tag, ticket, trash,
trophy, truck, tv, underline, user, user-circle, user-group, user-minus, user-plus, users,
variable, video-camera, video-camera-slash, view-columns, viewfinder-circle, wallet, wifi, window,
wrench, wrench-screwdriver, x-circle, x-mark
```

---

## Freshness

Heroicons ships new icons periodically. If a requested name is not in this list:
1. Re-fetch the live directory listing before declaring it unsupported:
   ```bash
   curl -s https://api.github.com/repos/tailwindlabs/heroicons/contents/optimized/24/outline \
     | python3 -c "import json,sys; print('\n'.join(sorted(d['name'][:-4] for d in json.load(sys.stdin))))"
   ```
2. If it's genuinely new, fetch it directly by name via the raw URL template above —
   don't wait for this file to be regenerated to unblock the task.
3. Regenerate this file (re-run the snapshot script above for all four variants) if more
   than a handful of names have drifted, and bump the snapshot date.
