# Step 4: Composing a real screen from multiple components

Part of `kmp-shadcn-compose`. Load this file when working on: step 4: composing a real screen from multiple components.

---

Knowing one component's signature isn't the same as knowing how several fit together into
a good screen. Worked example — a settings-style list of rows inside a card, every symbol
below individually verified against real source (`fetch_component_signature.py`), not
copied wholesale from the library's own KDoc usage examples:

```kotlin
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun AccountSettingsCard(members: List<Member>, onView: (Member) -> Unit) {
    ShadcnCard(
        header = { ShadcnCardHeader(title = "Team members", description = "${members.size} people") },
    ) {
        ShadcnItemGroup {
            members.forEach { member ->
                ShadcnItem(variant = ShadcnItemVariant.Outline) {
                    ShadcnAvatar { ShadcnAvatarFallback(member.initials) }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        ShadcnItemTitle(member.name)
                        ShadcnItemDescription(member.email)
                    }
                    ShadcnButton(onClick = { onView(member) }) { ShadcnText("View") }
                }
            }
        }
    }
}
```

What's real here and why it's shaped this way:
- `ShadcnCard`'s `content` lambda is `ColumnScope` — `ShadcnItemGroup` drops straight in,
  no wrapper needed.
- `ShadcnItemGroup` **already paints a hairline separator between each `ShadcnItem`** —
  confirmed in its own KDoc ("Vertically stacks a list of ShadcnItems with a hairline
  separator between each"). Adding a manual `ShadcnSeparator()` between items double-draws
  the divider. `ShadcnSeparator` is for dividing unrelated sections, not rows inside a group
  — that's already handled.
- `ShadcnItem`'s `content` lambda is `RowScope`, not a set of named slots — a `Row`/`Column`
  and plain `Modifier.weight()` inside it is the normal way to lay out avatar/text/action,
  the same as composing any other `RowScope` content.

**A real trap this example exists to name**: `ShadcnItem`'s own KDoc usage example shows
`ShadcnItemMedia { }`, `ShadcnItemContent { }`, and `ShadcnItemActions { }` as if they were
real slot composables. Checked against the actual file
(`ShadcnItem.kt`) — **none of the three exist anywhere in the repo.** Only
`ShadcnItem`, `ShadcnItemGroup`, `ShadcnItemTitle`, `ShadcnItemDescription`, and
`ShadcnItemSeparator` are real functions. Even the library's own official KDoc example is
not a substitute for `fetch_component_signature.py` — this is the concrete case proving why.

For a split view (list + detail), the same discipline applies: wrap the two panes in
`ShadcnResizablePanelGroup` (verified in the Component Keyword Matrix's Layout & structure
row) rather than a bare `Row` with manual weights — it gets a draggable divider and clamped
min/max weights for free, matching what `kmp-layout-system`'s Pattern A
wireframe expects for a nav+side+main layout.

### Layout-pattern lookup when nothing here covers the shape needed

[Shadcn Studio](https://shadcnstudio.com/) — **not the official shadcn/ui project, and not
this library.** Verified directly (2026-07-13): a third-party, independently-run paid
catalog ($99–$849 one-time) of 800+ UI blocks and 20+ page templates built on real
shadcn/ui, explicitly "not affiliated with shadcn/ui." Useful here for exactly one thing —
seeing how a layout *shape* (a dashboard, a pricing table, a settings page) is typically
structured — never as a source to copy code from.

**Why code can't be copied from it directly**: its output is React/JSX + Tailwind, not
Kotlin/Compose. Treat any block from it exactly like an HTML/CSS wireframe — run it through
`kmp-layout-system`'s "Translating an External HTML/CSS Wireframe" mapping
table, then verify every resulting `Shadcn*` component name with
`fetch_component_signature.py` before using it, the same discipline Step 3/Step 4 already
require for this library's own components. A block behind the paid tier is not accessible
without payment — don't assume free access to a specific block by name.

---

