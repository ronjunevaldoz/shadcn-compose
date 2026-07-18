# Component catalog

Every entry maps to a real shadcn/ui `base/*` component (or is a deliberate, documented deviation —
see [Registry parity](../README.md#registry-parity) in the README). **Keywords** are search terms an
agent or developer might actually type — match on any of them, not just the component name. Full
usage examples for every entry live in the catalog app (`/app/shared/.../catalog/docs/*Doc.kt`) and
render live at `installation`/`introduction` plus one page per component.

For machine-readable access to this same data — properties, parity family, and public preview
image URLs for every component/state/theme combination (useful for comparing another library's
components against this one) — see [`component-metadata.json`](component-metadata.json) and
[`component-metadata.md`](component-metadata.md) for its schema and how to use it.

### Core primitives

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnButton` | Trigger an action; 6 variants (default/outline/secondary/ghost/destructive/link), 5 sizes | button, CTA, submit, click handler, icon button |
| `ShadcnCard` | Bordered content container with optional header/footer slots | card, panel, box, container, login form, settings section |
| `ShadcnBadge` | Small status/count label, usually inline with text | badge, tag, status pill, count indicator, label chip |
| `ShadcnChip` | Compact, often removable/selectable token (filter chips, multi-select tags) | chip, filter tag, removable tag, token, pill button |
| `ShadcnTextField` | Single-line text input (real shadcn's `input.tsx`) | text field, input box, text input, form field |
| `ShadcnText` | Themed text with the design system's typography scale | text, label, typography, heading, body copy |
| `ShadcnIcon` | Renders any `ImageVector` (heroicons, custom vectors, ...), auto-tinted to match its surroundings the same way `ShadcnText` does -- no icon-library dependency itself, BYO vector | icon, icon button, vector icon, tinted icon, icon color |

### Forms & inputs

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnLabel` | Accessible field label, pairs with any input | label, form label, field title |
| `ShadcnCheckbox` | Boolean toggle, supports indeterminate | checkbox, tick box, boolean input, agree to terms |
| `ShadcnRadioGroup` | Pick exactly one of N mutually-exclusive options | radio button, radio group, single choice, option list |
| `ShadcnSwitch` | On/off toggle (real shadcn's `switch.tsx`, iOS-style pill) | switch, toggle switch, on/off, settings toggle |
| `ShadcnToggle` | Pressable two-state button (bold/italic-style toolbar buttons) | toggle button, pressed state, formatting toolbar button |
| `ShadcnSlider` | Drag a thumb to pick a value/range on a track | slider, range input, volume slider, value picker |
| `ShadcnToggleGroup` | A row of `Toggle`s where selection is mutually exclusive (or multi-select) | toggle group, segmented control, button group toggle |
| `ShadcnInputGroup` | Text field with leading/trailing addons (icon, button, unit label) | input group, input with icon, prefixed input, input addon |
| `ShadcnButtonGroup` | Visually joined row of buttons with shared/flush corners | button group, split button, joined buttons, toolbar |
| `ShadcnTextarea` | Multi-line text input | textarea, multiline input, comment box, message box |
| `ShadcnField` / `ShadcnFieldGroup` | Label + control + description/error layout for building forms (consolidates real shadcn's `form.tsx`) | form field, field group, form builder, validation error text |
| `ShadcnInputOTP` | Boxed one-time-passcode input, fills left-to-right | OTP input, verification code, 2FA code, PIN input |

### Data display

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnAvatar` | Circular user/entity image with fallback initials | avatar, profile picture, user icon, initials circle |
| `ShadcnAspectRatio` | Constrain a child to a fixed width:height ratio | aspect ratio, 16:9 box, responsive image container |
| `ShadcnSeparator` | Thin dividing line, horizontal or vertical | separator, divider, hairline, horizontal rule |
| `ShadcnKbd` | Styled keyboard-key label | kbd, keyboard shortcut, hotkey badge |
| `ShadcnItem` / `ShadcnItemGroup` | List row with media/content/actions slots (order rows, settings rows) | list item, row, list tile, settings row |
| `ShadcnEmpty` | Empty-state placeholder (icon/title/description/action) | empty state, no results, zero state, placeholder screen |

### Feedback

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnAlert` | Inline banner for a message that needs attention | alert, banner, warning box, info box, callout |
| `ShadcnProgress` | Determinate progress bar | progress bar, loading bar, upload progress |
| `ShadcnSkeleton` | Pulsing placeholder shape while content loads | skeleton, loading placeholder, shimmer, content loader |
| `ShadcnSpinner` | Indeterminate rotating loading indicator | spinner, loading spinner, activity indicator |
| `ShadcnToast` / `ShadcnToaster` | Transient stacked notification (real shadcn's `sonner.tsx`) | toast, snackbar, notification popup, sonner |

### Disclosure & navigation

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnCollapsible` | Single show/hide section behind a trigger | collapsible, expandable section, show more/less |
| `ShadcnAccordion` | Multiple collapsible sections, FAQ-style | accordion, FAQ, expandable list, collapsible group |
| `ShadcnTabs` | Switch between panels via a tab strip | tabs, tab bar, tabbed interface, segmented view |
| `ShadcnBreadcrumb` | "Home › Section › Page" navigation trail | breadcrumb, nav trail, path navigation |

### Overlays & navigation

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnTooltip` | Text-only hint on hover/focus (inverted colors, matches real shadcn) | tooltip, hover hint, help text popup |
| `ShadcnPopover` | Click-triggered anchored panel for arbitrary rich content | popover, floating panel, dropdown panel |
| `ShadcnHoverCard` | Hover-triggered rich preview panel (unlike Tooltip, not text-only) | hover card, preview card, hover preview |
| `ShadcnDropdownMenu` | Anchored list of actions | dropdown menu, action menu, kebab menu, overflow menu |
| `ShadcnContextMenu` | Right-click-triggered menu at the cursor | context menu, right-click menu |
| `ShadcnDialog` | Centered modal for focused tasks | dialog, modal, popup window |
| `ShadcnAlertDialog` | Modal that blocks until the user confirms/cancels a destructive action | confirm dialog, delete confirmation, alert dialog |
| `ShadcnSheet` | Modal panel sliding in from a screen edge | sheet, side panel, slide-in panel |
| `ShadcnDrawer` | Sliding panel with real drag-to-dismiss (distinct from Sheet) | drawer, bottom sheet, swipe to dismiss, mobile drawer |
| `ShadcnCombobox` | Searchable/filterable single-select dropdown | combobox, searchable dropdown, autocomplete, filterable select |
| `ShadcnSelect` | Plain (non-searchable) dropdown select | select, dropdown, picker, plain select list |
| Date Picker (recipe: `ShadcnPopover` + `ShadcnCalendar`) | Pick a date via a calendar popup | date picker, calendar picker, choose a date |
| `ShadcnCommand` | Searchable/filterable action list (⌘K palette building block) | command palette, cmd+k, quick actions, fuzzy search menu |
| `ShadcnMenubar` | Desktop-app-style "File Edit View" horizontal menu bar | menubar, app menu bar, desktop menu |
| `ShadcnNavigationMenu` | Top-level site nav with optional flyout panels | navigation menu, nav bar, mega menu |

### Data & layout

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnTable` | Bordered data table with header/rows | table, data table, grid, rows and columns |
| `ShadcnPagination` | Page-number navigation control | pagination, page numbers, next/prev pages |
| `ShadcnScrollArea` | Custom-styled scrollable viewport with a draggable thumb | scroll area, custom scrollbar, scrollable container |
| `ShadcnChart` / `ShadcnBarChart` | Config-driven bar/line chart (Canvas-drawn, no Material) | chart, bar chart, graph, data visualization |
| `ShadcnCalendar` | Month grid date picker | calendar, date grid, month view |
| `ShadcnCarousel` | Swipeable/paged content slider | carousel, slider, image slider, paged content |
| `ShadcnResizablePanelGroup` | Two panes divided by a draggable handle | resizable panels, split pane, drag to resize |
| `ShadcnSidebar` | Collapsible side navigation rail with grouped menu sections | sidebar, side nav, app shell nav, collapsible rail |

### AI Elements

Chat/AI-assistant UI primitives (mirrors real shadcn's "AI Elements" family).

| Component | Use case | Keywords |
|---|---|---|
| `ShadcnMessage` / `ShadcnMessageGroup` | One chat row: avatar + content, mirrored for the sender's own messages | chat message, message row, avatar + bubble layout |
| `ShadcnBubble` / `ShadcnBubbleContent` / `ShadcnBubbleReactions` | Colored chat bubble with variants, self-aligned, optional emoji-reaction pill | chat bubble, speech bubble, message bubble, emoji reactions |
| `ShadcnAttachment` / `ShadcnAttachmentGroup` | File-attachment chip for a chat composer's upload tray (upload/processing/error states) | file attachment, upload chip, attached file, upload progress |
| `ShadcnMarker` | Labeled divider in a chat transcript (date separator, pinned-messages banner) | date separator, chat divider, section marker |
| `ShadcnMessageScroller` | Auto-follows new messages to the bottom, releases on manual scroll, floating jump-to-bottom button | chat scroll view, auto-scroll chat, AI streaming scroll, jump to bottom, sticky scroll |

### Utils

Modifiers, not standalone components (mirrors real shadcn's `docs/utils/*` pages).

| Utility | Use case | Keywords |
|---|---|---|
| `Modifier.shadcnShimmer()` | Sweeping highlight over text for "generating response"/"thinking" states | shimmer, loading text animation, generating response, thinking indicator |
| `Modifier.shadcnScrollFade()` | Fades a scrollable container's edges based on scroll position, hinting overflow | scroll fade, edge fade, overflow hint, fade mask, scroll shadow |
