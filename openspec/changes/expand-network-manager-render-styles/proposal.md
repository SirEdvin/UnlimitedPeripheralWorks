## Why

The network manager overlay currently applies one visibility mode to every peripheral and only renders labels. Players need independent presentation choices for the selected hierarchy, other groups, and ungrouped peripherals so the overlay can emphasize useful network structure without hiding unrelated categories globally.

## What Changes

- Replace the single overlay visualization selector with independent selected-group, other-groups, and ungrouped render settings.
- Add independent text styles (none, regular, bold) and box styles (none, outline, filled) for every overlay category.
- Let render-style buttons cycle forward with left click and backward with right click.
- Make box styles visually strong and visible through intervening blocks.
- Apply the selected-group style to the selected group and all descendants.
- Use each group's configured color for box styles, defaulting to white; keep text labels free of group-color styling.
- Remove the now-redundant per-group visibility override control and rendering behavior.

## Capabilities

### New Capabilities
- `network-manager-render-styles`: Per-category network overlay styling and hierarchy-aware rendering.

### Modified Capabilities

None.

## Impact

- Affects Ultimate Configurator NBT, the network manager settings screen, serverbound setting synchronization, client overlay rendering, localization, and client GameTests.
- Existing configurators fall back to text for all categories; no new dependency or manager data migration is required.
