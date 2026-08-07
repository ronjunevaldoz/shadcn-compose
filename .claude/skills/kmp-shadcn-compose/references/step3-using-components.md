# Step 3: Using components

Part of `kmp-shadcn-compose`. Load this file when working on: step 3: using components.

---

### The one rule that matters more than any example below

**Never call a `Shadcn*` component with a parameter you haven't verified exists on its
real signature.** Do not assume a parameter exists by analogy to Jetpack Compose's own
`TextField`/Material components, to HTML/CSS attributes, or to another `Shadcn*`
component's shape — every component here has its own specific, independently-designed
API. Two confirmed, real examples of what guessing produces, found by fetching the
actual source rather than trusting a table like the one below:

- `ShadcnTextField` has **no `singleLine` parameter** — a real project's implementation
  used `singleLine = false` (a real Compose `TextField`/`BasicTextField` parameter,
  assumed to carry over) and it would not compile. The real multi-line component is the
  separate `ShadcnTextarea` (see below) — not a parameter toggle on `ShadcnTextField`.
- The component commonly assumed to be `ShadcnTabs` is actually named **`ShadcnTabsList`**
  — this skill's own component table said `ShadcnTabs` until this was checked against
  the real source.

Before writing a call to any component **not** shown with a verified signature below,
fetch the real one first — one command, no need to remember the file path or grep
pattern by hand:
```bash
python3 skills/kmp-shadcn-compose/scripts/fetch_component_signature.py <ComponentName>
```
It handles the two cases that break a naive lookup: a component living in a
differently-named file (checks the obvious filename first, then searches every
component file), and nested parens in a default value (uses a balanced-paren scan, not
a single-level regex, so the signature isn't truncated early). Or, if the project
already resolves the dependency, read it directly from the Gradle cache / IDE-decompiled
sources. Never skip this to save a lookup — a wrong guess costs more time than the
lookup would have.

### Verified signatures (checked against real source, 2026-07-12)

```kotlin
ShadcnButton(onClick = {}) { ShadcnText("Click me") }
ShadcnButton(onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Sm) { ShadcnText("Outline") }
ShadcnButton(onClick = {}, variant = ButtonVariant.Destructive) { ShadcnText("Delete") }
// ButtonVariant: Default | Outline | Secondary | Ghost | Destructive | Link — 6 variants, 5 sizes

ShadcnTextField(value = text, onValueChange = { text = it }, placeholder = "Email")
// value, onValueChange, modifier, enabled, label, placeholder, leadingIcon, trailingIcon,
// isError, supportingText, variant, style, keyboardOptions, keyboardActions, visualTransformation
// NO singleLine parameter — this is a single-line-only field by design.

ShadcnTextarea(value = prompt, onValueChange = { prompt = it }, placeholder = "Describe the scene")
// value, onValueChange, modifier, enabled, label, placeholder, isError, supportingText,
// variant, style, keyboardOptions, keyboardActions — the multi-line equivalent of
// ShadcnTextField above; wraps it internally. Use this for an HTML wireframe's <textarea>,
// never ShadcnTextField with a guessed multi-line parameter.

ShadcnSelect(value = selected, options = listOf("A", "B"), onValueChange = { selected = it }, label = { it })
// fun <T> ShadcnSelect(value: T?, options: List<T>, onValueChange: (T) -> Unit, modifier,
// label: (T) -> String = { it.toString() }, placeholder: String, variant, style, icon)

ShadcnCard(header = { ShadcnCardHeader(title = "Title") }) { ShadcnText("Body content") }
// fun ShadcnCard(modifier, variant, size, style, header: (@Composable () -> Unit)?,
// footer: (@Composable () -> Unit)?, content: @Composable ColumnScope.() -> Unit)
// Slot-based — header/footer are optional composable slots, not string parameters.
// ShadcnCardHeader(title, description, action, modifier) is a separate helper composable.

ShadcnCheckbox(checked = isChecked, onCheckedChange = { isChecked = it })
// checked, onCheckedChange: ((Boolean) -> Unit)?, modifier, indeterminate, enabled, style

ShadcnSwitch(checked = isOn, onCheckedChange = { isOn = it })
// checked, onCheckedChange: ((Boolean) -> Unit)?, modifier, enabled, style

ShadcnAvatar { ShadcnAvatarFallback("JD") }
// fun ShadcnAvatar(modifier, size: ShadcnAvatarSize, content: @Composable BoxScope.() -> Unit)
// Slot-based, with separate companion composables: ShadcnAvatarFallback(text, modifier),
// ShadcnAvatarBadge(modifier), ShadcnAvatarGroup(modifier) { content }.

ShadcnTabsList(items = tabItems, selected = selectedId, onSelectedChange = { selectedId = it })
// NOT "ShadcnTabs" — items: List<ShadcnTabItem>, selected: String, onSelectedChange: (String) -> Unit, modifier
```

See the
[component catalog](https://github.com/ronjunevaldoz/shadcn-compose/blob/main/docs/components.md)
for the full 70+ component list; each entry links to a live usage page in the library's own
catalog app (`app/shared/.../catalog/docs/*Doc.kt`) — treat that catalog app as the
authoritative usage reference for anything not verified above, not a guess from the name
alone.

No icon-library dependency is bundled *into* shadcn-compose — every component draws from
this library's own tokens for color/shape, not icon art. An icon needed in a screen built
with these components comes from a separate dependency:
[`heroicons-compose`](https://github.com/ronjunevaldoz/heroicons-compose)
(`io.github.ronjunevaldoz:heroicons-outline:<version>`, Maven Central, Heroicons compiled
to CMP `ImageVector` — Outline variant only today; Solid/Mini/Micro not yet built), or
`kmp-imagevector-generator` for anything Heroicons doesn't cover. Do not
assume any icon set ships automatically with the `shadcn-compose` dependency itself.

### Density/sizing requests — reach for the library's own parameters first

This isn't only about the literal word "compact." Any request that's really about
density or sizing — "make this compact," "tighter," "denser," "smaller," "more
breathing room," "roomier" — has two real levers already built into this library.
Check both before writing a single custom `Style { }` override:

1. **Whole-app/whole-screen density → `ShadcnTheme`'s `preset` parameter.** The preset
   table earlier in this skill is not cosmetic — `Mira` ("made for compact interfaces,"
   tightest timings) and `Nova` ("reduced padding and margins," snappy) both compress
   spacing/timing across every component at once. If the request is about the app's
   overall feel, changing `preset` is very likely the actual fix — not a per-component
   override, and not introducing a custom modifier at all.
2. **One component's size → its own preset `Size` enum.** Every sized `Shadcn*`
   component ships one. Verified against real source (2026-07-31):

```kotlin
sealed interface ButtonSize {
    data object Xs : ButtonSize    // 28.dp height — the compact preset
    data object Sm : ButtonSize    // 32.dp height
    data object Md : ButtonSize    // 36.dp height — default
    data object Lg : ButtonSize    // 40.dp height
    data object Icon : ButtonSize  // 36×36.dp square, icon-only
}
```

`ShadcnCard` and `ShadcnAvatar` (`ShadcnAvatarSize`) also take a `size` parameter — verify
each component's actual `Size` enum with `fetch_component_signature.py` before assuming it
does or doesn't have one; do not assume a component lacks a preset just because one isn't
shown above. Only reach for a custom `Style { }` override when the request needs a value
neither lever covers (an exact one-off `height`, not a general density change) — and say
so explicitly when doing it, since it's the exception, not the norm.

---

