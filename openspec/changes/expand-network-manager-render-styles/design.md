## Context

The Ultimate Configurator currently stores one four-way visualization mode, while each network manager group stores a visibility override. The overlay always renders text for visible peripherals. The requested styles are player presentation preferences, so they belong on the configurator rather than in shared manager state.

## Goals / Non-Goals

**Goals:**

- Configure selected-hierarchy, other-group, and ungrouped rendering independently.
- Render independent none, regular, or bold text and none, outline, filled, or flare box effects.
- Reuse native Minecraft box rendering.
- Preserve existing configurator behavior when reading the old visualization-mode tag.

**Non-Goals:**

- Add new render libraries or custom models.
- Add per-group render styles.
- Change group membership, hierarchy identity, group colors, range, or delimiter persistence.
- Remove legacy group visibility data from saved managers.

## Decisions

### Store three render styles on the configurator

Define three targets, selected groups, other groups, and ungrouped peripherals, with independent three-value text and box style enums. Persist both styles for every target in configurator NBT and synchronize changes through the existing validated network-manager packet.

Alternative: persist styles on the manager. Rejected because these settings control an individual player's current overlay and should not affect other viewers.

### Resolve one target and color per peripheral

A peripheral with any membership in the selected group or a descendant uses the selected styles. Otherwise a grouped peripheral uses the other-groups styles; a peripheral without memberships uses the ungrouped styles. For colored boxes, use the first matching group in sorted order and fall back to white when the group has no color; ungrouped peripherals use white. This deterministic precedence avoids drawing overlapping boxes for multi-group peripherals.

Alternative: render once per membership. Rejected because identical geometry overlaps, produces unstable blended colors, and adds no useful information beyond text labels.

### Keep text neutral

Peripheral and extra-name text retain neutral label colors. Each group-name line and Groups-tab row uses that group's configured color, defaulting to white. Outline, filled, and flare box effects use the resolved group color. Text and box styles render independently so a category can show either or both.

Render text once through the `SEE_THROUGH` font path. Draw all box effects first and text last so labels remain visible through blocks and cannot be composited beneath boxes.

### Present one row per category

Show the category once as a static row label followed by separate text and box buttons. Keep the existing three-row layout and use the same bidirectional cycling button for both controls.

### Draw diagnostic boxes immediately

Draw box geometry with vanilla line and position-color shaders while depth testing is disabled instead of queuing depth-tested render types. Use a thicker line width for outlines and stronger translucent alpha for filled boxes so both remain legible through intervening blocks.

Submit `addChainedFilledBoxVertices` as a triangle strip, matching the topology emitted by the vanilla helper.

### Cycle style buttons in both directions

Use one native button subclass for all six controls that retains normal left-click behavior and handles right click as the previous enum value. Both directions wrap at the ends and use the same validated setting packet.

### Read old visualization settings as defaults

When a new per-target style tag is absent, derive it from the old four-way visualization mode. Once edited, each new target tag overrides only that target. This preserves existing configurators without a data fixer or eager mutation.

Alternative: reset every existing configurator to text for all categories. Rejected because persisted selected-only and ungrouped-only behavior would change unexpectedly.

## Risks / Trade-offs

- [A peripheral belongs to groups with different colors] -> Use the first matching sorted group for stable output; add explicit color priority only if users need it.
- [Filled boxes obscure blocks] -> Keep them translucent but visually stronger, with depth disabled to match diagnostic-overlay behavior.
- [Legacy group visibility overrides remain saved but unused] -> Leave the data intact for rollback compatibility instead of adding a migration solely to delete it.

## Migration Plan

Existing configurators derive missing styles from their old visualization mode. Existing manager group visibility fields remain readable and writable but no longer affect the overlay. Rolling back restores the old selector and ignores the new style tags.

## Open Questions

None.
