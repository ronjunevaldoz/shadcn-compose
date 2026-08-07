# Filled Example

Part of `kmp-layout-system`. Load this file when working on: filled example.

---

The templates above filled in for a generic messaging app (3 screens shown):

**`docs/layout-system/inbox.md`**

```
+----------+------------------+----------------------------------------------+
| Left Nav | Thread List      | Message View                                 |
| 48 dp    | 240 dp           | flex 1                                       |
+----------+------------------+----------------------------------------------+
|          |                  |                                              |
| [ch]*    | Alice            | [bubble] Hey, are you free tonight?          |
|          | Bob              | [bubble] Yeah! What did you have in mind?    |
| [cont]   | Team Alpha       |                                              |
|          |                  |                                              |
|          |                  |----------------------------------------------|
| [sett]   |                  | [ Type a message...              ]  [Send]   |
+----------+------------------+----------------------------------------------+
Legend: [ch] = Chats  [cont] = Contacts  [sett] = Settings  * = active
```

**`docs/layout-system/contacts.md`** — Thread List hidden

```
+----------+------------------------------------------------------------+
| Left Nav | Contacts                                                   |
| 48 dp    | flex 1  (Thread List not rendered)                         |
+----------+------------------------------------------------------------+
|          |                                                            |
| [ch]     | [tab] All  [tab] Favorites  [tab] Groups                   |
|          +------------------------------------------------------------+
| [cont]*  |                                                            |
|          |  Alice Romano          alice@example.com                   |
|          |  Bob Tanaka            bob@example.com                     |
|          |  Team Alpha            3 members                           |
|          |                                                            |
| [sett]   |                                                            |
+----------+------------------------------------------------------------+
Legend: [ch] = Chats  [cont] = Contacts  [sett] = Settings  * = active
```

---

## Screen File Format

Each screen file follows this structure:

```
# <Screen name>

## Components

| Component      | Width   | Visible         | Notes                     |
|----------------|---------|-----------------|---------------------------|
| <Component A>  | <N> dp  | <always / when> | <short note>              |
| <Component B>  | flex 1  | Yes             | <short note>              |

---

## <Variant name>

<wireframe here>

---

## Interaction notes

- <tap / swipe / gesture> → <what happens>
- <state change> → <how it looks>
```

---

