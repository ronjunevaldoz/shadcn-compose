---
name: kotlin-multiplatform-graphics-modifiers
description: >
  Jetpack Compose graphics modifiers for KMP and CMP — use graphicsLayer for
  transforms, clipping, alpha, elevation, and layer effects; use Canvas, drawBehind,
  and drawWithCache for actual custom drawing. Recommended for workflow editors,
  node-based UIs, and other surfaces where a composable needs a transformable shell
  around a custom-drawn interior.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-13'
  keywords:
    - graphicsLayer
    - graphics modifier
    - Canvas
    - drawBehind
    - drawWithCache
    - Compose graphics
    - workflow node
    - node editor
    - custom drawing
    - KMP UI
    - CMP graphics
---

## When to Use This Skill

Use this skill when you need to:
- Build a workflow node, graph editor, or draggable surface in Compose
- Decide between `graphicsLayer`, `Canvas`, `drawBehind`, and `drawWithCache`
- Add transforms, clipping, elevation, alpha, or rotation without redrawing layout
- Improve performance for repeated drawing objects like paths, brushes, and shaders

**Recommended default:** use `Modifier.graphicsLayer` for the node shell and `Canvas`
or `drawWithCache` for the graph drawing itself.

**Trigger keywords:** graphicsLayer, Canvas, drawBehind, drawWithCache, custom drawing,
workflow node, node editor, graph editor, drag transform, layer effects, elevation.

**Freshness rule:** Compose graphics and drawing APIs change with each CMP release — recheck
the Compose developer docs before upgrading or copying snippets.

---

## Recommendation First

For a workflow node, split the surface into two parts:

1. **Node shell**: `graphicsLayer` for scale, alpha, clip, shadow, rotation, and lift.
2. **Graph drawing**: `Canvas` or `drawWithCache` for connectors, ports, grid, and paths.

Why:
- `graphicsLayer` does not change measure or placement, so it is good for visual effects
- `Canvas` is the right tool for actual rendering
- `drawWithCache` avoids rebuilding paths and brushes on every frame

Do not replace every canvas workflow with `graphicsLayer`. It improves the container, not
the drawing system.

---

## Project Structure

Use a structure that separates node chrome from the graph surface:

```text
feature/workflow/
  ui/
    WorkflowEditorScreen.kt
    graph/
      WorkflowGraphCanvas.kt
      WorkflowConnections.kt
    node/
      WorkflowNode.kt
      WorkflowNodeSurface.kt
      WorkflowNodeControls.kt
```

Rules:
- `WorkflowNodeSurface` owns `graphicsLayer`
- `WorkflowGraphCanvas` owns custom drawing
- `WorkflowNodeControls` owns handles, buttons, and stateful interactions
- keep drawing logic out of the top-level screen

---

## Workflow Node Pattern

Use `graphicsLayer` for the node itself:

```kotlin
@Composable
fun WorkflowNode(
    selected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = if (selected) 1.02f else 1f
                scaleY = if (selected) 1.02f else 1f
                alpha = if (selected) 1f else 0.92f
                shadowElevation = if (selected) 16f else 4f
                clip = true
            }
    ) {
        content()
    }
}
```

Use `Canvas` or `drawWithCache` for edges and cached drawing:

```kotlin
@Composable
fun WorkflowConnections(
    modifier: Modifier = Modifier,
    paths: List<Path>,
) {
    Canvas(modifier = modifier) {
        paths.forEach { path ->
            drawPath(
                path = path,
                color = Color(0xFF6C7EFF),
            )
        }
    }
}
```

Use `drawWithCache` when the shape or brush should be reused:

```kotlin
Modifier.drawWithCache {
    val stroke = Stroke(width = 2.dp.toPx())
    val path = Path().apply {
        // build or reuse path geometry here
    }
    onDrawBehind {
        drawPath(path = path, color = Color.Gray, style = stroke)
    }
}
```

---

## Choosing the Right Tool

### Use `graphicsLayer` when:
- the content should scale, rotate, fade, clip, or cast elevation
- you want a reusable visual wrapper around a composable
- you do not need to change layout size or position

### Use `Canvas` when:
- you are drawing the graph, lines, shapes, or node connectors
- you need direct `DrawScope` access
- layout is already decided and only the paint phase changes

### Use `drawWithCache` when:
- the drawing uses expensive objects like `Path`, `Brush`, or `Shader`
- the cached objects depend on size or small state inputs

### Use `drawBehind` when:
- you want a simple background or underline behind normal content
- caching is not needed

---

## Testing

```kotlin
// Graphics modifiers are visual — test with Roborazzi screenshot regression.
// Interaction tests are not applicable here; focus on visual contracts.

@Test fun `graphicslayer_rotation_45deg screenshot`() {
    captureRoboImage("graphics_rotation_45deg.png") {
        AppTheme {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer { rotationZ = 45f }
                    .background(AppTheme.colors.primary),
            )
        }
    }
}

@Test fun `drawbehind_rounded_border screenshot`() {
    captureRoboImage("graphics_drawbehind_border.png") {
        AppTheme {
            val color = AppTheme.colors.primary
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = color,
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    },
            )
        }
    }
}

@Test fun `graphicslayer_alpha_50_percent screenshot`() {
    captureRoboImage("graphics_alpha_50.png") {
        AppTheme {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer { alpha = 0.5f }
                    .background(AppTheme.colors.primary),
            )
        }
    }
}
```

---

## Common Anti-Patterns

- using `graphicsLayer` to fake the whole graph rendering
- redrawing expensive paths every frame without caching
- changing layout or hit areas when only a transform is needed
- putting workflow node geometry directly inside screen composables
- using `drawWithCache` when the draw objects are trivial and do not need caching

If the problem is geometry, use drawing. If the problem is presentation, use a layer.

## Reference

See `references/workflow-node-zoom.png` for the workflow editor mockup with the toolbar
outside the zoomed graph surface and sample zoom levels.

---

## Related Skills

- `kotlin-multiplatform-design-system` — design tokens and variants drive the visual style of workflow nodes
- `kotlin-multiplatform-compose-slot-api` — node content areas are often implemented as composable slots
- `kotlin-multiplatform-mvi` — draggable node state (position, selection) is managed through MVI state

---

## Output Style

When asked about graphics modifiers or custom drawing, respond in this order:
1. recommendation (graphicsLayer vs Canvas vs drawWithCache — pick the right tool)
2. code snippet (the modifier or Canvas call for the stated use case)
3. why that tool fits
4. main alternative

Lead with the decision table when the user is choosing between tools. Keep snippets to one modifier or one Canvas block.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-13 | Initial release. |
