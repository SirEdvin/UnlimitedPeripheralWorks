## Context

The Ultimate Configurator currently persists one active configurator mode, block position, and dimension. Crouch-right-clicking air clears that binding and mode-specific item data; a detached configurator has no air-use behavior. Four configuration modes are registered from block-state predicates, and existing client screens use loader-neutral screen-opening hooks plus validated serverbound messages.

This change adds item-local target history and favorites. Records can outlive loaded chunks, removed blocks, and dimension changes, so stored identity and every mutation must be validated without loading chunks or trusting client screen state.

## Goals / Non-Goals

**Goals:**

- Preserve three distinct recently attached targets and up to 16 favorites on each configurator.
- Make detached configurators a fast, native UI entry point for reattachment and favorite management.
- Keep the item stack's server-side NBT authoritative while providing responsive client controls.
- Safely retain cross-dimension and temporarily unavailable records.

**Non-Goals:**

- Loading chunks, teleporting players, or remotely configuring a stored target.
- Sharing target lists between configurators or players.
- Preserving mode-specific screen selections and rendering preferences per history entry.
- Adding a container menu, external dependency, or Lua API.

## Decisions

### Store compact target records on the item

Store recent and favorite lists as separate NBT lists. Each target record contains the configurator mode ID, dimension ID, and block position; favorite records may additionally contain a custom name. A target's identity is its dimension and position. Attaching at an existing identity replaces its stored mode and moves it to the front of the recent list, then trims the list to three entries.

Favorites are independent of recent-list eviction and are ordered by most recent favorite action. Favoriting inserts a target at the front, duplicate favorite requests do not create another record, and the server rejects additions after 16 entries. Selecting or renaming a favorite does not reorder it.

Alternative considered: keep one list with recent/favorite flags. Rejected because trimming recent history would either delete favorites or require extra retention rules, while two small lists directly match the UI sections.

### Open a non-container screen only while detached

A normal right-click on air with no active mode opens a client `Screen` through the existing loader-neutral client platform. The screen reads the held stack's recent and favorite NBT and displays two sections. Default rows show the translated target block name, dimension, and coordinates. A named favorite displays only its custom name, as requested.

Each row provides immediate selection. Non-favorite recent rows provide a favorite action, while favorite rows provide an edit action that opens a focused child screen for renaming or removing the favorite. Submitting an empty name removes the custom name and restores the default label; non-empty names are limited to 64 characters. The screen remains non-pausing and closes after a successful selection.

Alternative considered: add a `Menu` and synchronized container. Rejected because no inventory slots or continuously authoritative world data are involved; normal item synchronization and explicit mutations are smaller.

### Route all changes through validated serverbound actions

Selection, favorite toggling, and rename submission use a loader-neutral serverbound message carrying the requested action and target identity rather than accepting replacement NBT from the client. The handler requires the player's main-hand item to be an Ultimate Configurator and detached, then resolves the referenced target from its current NBT.

Favorite and rename actions mutate only records already present in the server's recent or favorite lists. Rename additionally requires the target to be a favorite and enforces the 64-character limit. The item is synchronized after successful mutation.

Selection requires the player to be in the target dimension, the target position to be loaded, and the current block state to resolve to the stored configurator mode. A successful selection uses the same active-mode save path as direct attachment, updates recent history, synchronizes the item, and closes the screen. A rejected selection leaves the item detached and provides feedback.

Alternative considered: have the client directly edit item NBT or attach from historical data without checking the world. Rejected because either path permits stale or forged targets and bypasses current block compatibility.

### Record history at the shared attachment path

The shared active-mode save operation records recent history, covering both direct crouch-click attachment and menu selection once. Detaching clears only active mode and mode-specific transient data; it does not clear recent targets or favorites.

Alternative considered: update history in each interaction caller. Rejected because it duplicates ordering and trimming behavior and can miss future attachment paths.

## Risks / Trade-offs

- [A stored block is removed, changed, unloaded, or in another dimension] -> Keep the record visible and editable, but reject selection until the player is in the dimension and the loaded block matches the stored mode.
- [A client acts on a stale row after item state changes] -> Resolve identity against current server NBT and require a detached main-hand configurator for every action.
- [Mode IDs or malformed records become invalid after upgrades] -> Ignore invalid records when reading and bound both lists whenever writing.
- [Long dimensions or coordinates overflow a row] -> Use clipped or scrolling row presentation while retaining the full value in hover text where needed.

## Migration Plan

Existing configurators have no history or favorite tags and therefore open an empty detached menu. The first successful attachment initializes recent history; no data fixer is required. Rolling back leaves unknown item tags that older versions ignore.

## Open Questions

None.
