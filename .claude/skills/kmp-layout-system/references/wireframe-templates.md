# Wireframe Templates

Part of `kmp-layout-system`. Load this file when working on: wireframe templates.

---

Use whichever pattern matches the screen. Replace every `<placeholder>` with the
project's real component name, size, label, or content.

### Pattern A — narrow nav + secondary panel + main area

```
┌──────────┬──────────────────┬──────────────────────────────────────────────┐
│ <Nav>    │ <Side Panel>     │ <Main Area>                                  │
│ <N> dp   │ <N> dp           │ flex 1                                       │
├──────────┼──────────────────┼──────────────────────────────────────────────┤
│          │                  │                                              │
│ [nav-1]* │ <item>           │ <primary content>                            │
│ [nav-2]  │ <item>           │ <primary content>                            │
│ [nav-3]  │ <item>           │                                              │
│          │                  │                                              │
│          │                  │──────────────────────────────────────────────│
│ [nav-4]  │                  │ <action row>                                 │
│ [nav-5]  │                  │──────────────────────────────────────────────│
│          │                  │ <input area>                                 │
└──────────┴──────────────────┴──────────────────────────────────────────────┘
Legend: [nav-1] = <name>  [nav-2] = <name>  [nav-3] = <name>
        [nav-4] = <name>  [nav-5] = <name>  * = active
```

### Pattern B — narrow nav + main area (secondary panel hidden)

```
┌──────────┬────────────────────────────────────────────────────────────┐
│ <Nav>    │ <Main Area>                                                │
│ <N> dp   │ flex 1  (<Side Panel> not rendered)                        │
├──────────┼────────────────────────────────────────────────────────────┤
│          │                                                            │
│ [nav-1]  │ [tab] <Tab A>  [tab] <Tab B>  [tab] <Tab C>                │
│          ├────────────────────────────────────────────────────────────┤
│ [nav-2]* │                                                            │
│          │  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐            │
│          │  │        │  │        │  │        │  │        │            │
│          │  └────────┘  └────────┘  └────────┘  └────────┘            │
│          │  <label>      <label>      <label>     <label>             │
│          │                                                            │
│ [nav-3]  │                                                            │
│ [nav-4]  │                                                            │
└──────────┴────────────────────────────────────────────────────────────┘
Legend: [nav-1] = <name>  [nav-2] = <name>  [nav-3] = <name>
        [nav-4] = <name>  * = active
```

### Pattern C — modal / sheet overlay

```
┌──────────┬────────────────────────────────────────────────────────────┐
│ <Nav>    │ [canvas stays in place — no swap]                          │
│ <N> dp   │                                                            │
├──────────┼────────────────────────────────────────────────────────────┤
│          │                                                            │
│ [nav-1]  │     ┌──────────────────────────────────────────────────┐   │
│          │     │ <Sheet title>                                  X │   │
│ [nav-2]  │     │ ──────────────────────────────────────────────── │   │
│          │     │ <content line>                                   │   │
│ [nav-3]* │     │ <content line>                                   │   │
│ [nav-4]  │     │ <content line>                                   │   │
│          │     └──────────────────────────────────────────────────┘   │
└──────────┴────────────────────────────────────────────────────────────┘
Legend: [nav-1] = <name>  [nav-2] = <name>  [nav-3] = <name>
        [nav-4] = <name>  * = active
```

### Pattern D — full-screen (no persistent nav)

For login, onboarding, splash, or any screen where no nav chrome is visible.

```
┌────────────────────────────────────────────────────────────────────────┐
│ <Screen Title>                                                         │
│ full width                                                             │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  <header / hero content>                                               │
│                                                                        │
│  <content row>                                       [scroll]          │
│  <content row>                                                         │
│  ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~   │
│                                                                        │
│  [ <Primary action>                                                  ] │
│  <secondary action>                                                    │
└────────────────────────────────────────────────────────────────────────┘
```

---

