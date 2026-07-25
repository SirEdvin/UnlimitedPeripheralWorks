## Why

Detaching an Ultimate Configurator currently discards convenient access to its previous target, forcing players to revisit blocks whenever they want to switch configurations. A detached-item menu with recent and favorite targets makes repeated configuration work faster while keeping each configurator self-contained.

## What Changes

- Record all favorites plus the three most recently attached distinct non-favorite blocks on the Ultimate Configurator, ordered by last use.
- Allow up to 16 recorded targets to be favorited and unfavorited from a detached configurator menu.
- Open a native target-selection menu when the player right-clicks air with a detached Ultimate Configurator.
- Reattach immediately when the player selects a valid stored target.
- Display each stored target's configurator type and coordinates, including its dimension, and allow favorites to be renamed.
- Hide a renamed favorite's coordinates behind its custom name and mark it with a golden outline; clearing the name restores the type-and-coordinate label.
- Provide a Settings tab for naming the configurator and choosing favorite text and box render styles, with per-favorite color overrides.
- Render favorites in the world while a detached Ultimate Configurator is held.
- Persist recent targets, favorites, and favorite names in the Ultimate Configurator's NBT.

## Capabilities

### New Capabilities

- `configurator-target-history`: Detached Ultimate Configurator target history, favorites, naming, validation, and selection UI.

### Modified Capabilities

None.

## Impact

- Affects Ultimate Configurator attachment and air-use behavior, item NBT, client screen opening, loader-neutral networking, localization, and client/server GameTests.
- Adds no external dependencies and does not change configured block data or Lua APIs.
