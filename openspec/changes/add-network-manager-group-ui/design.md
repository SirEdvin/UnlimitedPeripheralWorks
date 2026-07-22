## Context

The Ultimate Configurator stores an active mode and target block position. In network manager mode it currently renders synchronized peripheral/group data, cycles an item-local overlay range on swing, and uses a renamed name tag to toggle membership. Authoritative groups, colors, and memberships are persisted by `NetworkManagerBlockEntity` and exposed through Lua, but no native screen or serverbound UI mutation path exists.

The change spans common item interaction, a client screen, local client preferences, loader-neutral packets, block entity mutations, rendering, localization, and tests. The SFM label gun is a behavioral reference for searchable selection and inline creation, not a dependency or code source.

## Goals / Non-Goals

**Goals:**

- Make all routine group management and membership assignment available from the configurator.
- Keep group names, colors, and memberships authoritative on the server and compatible with Lua access.
- Keep delimiter and range authoritative on the network manager while hierarchy expansion follows the Ultimate Configurator.
- Reuse the existing synchronized network manager state and loader-neutral networking abstractions.
- Validate every UI mutation on the server against the held configurator and its bound network manager.

**Non-Goals:**

- Creating parent groups for hierarchy path segments.
- Creating or mutating groups through virtual hierarchy nodes.
- Synchronizing hierarchy expansion state between different configurators.
- Adding SFM as a dependency or duplicating its visual implementation exactly.
- Managing peripherals outside the network attached to the selected manager.

## Decisions

### Use a non-container client screen

Open a native `Screen` when the player uses the configurator on air in network manager mode. The screen reads the already synchronized target `NetworkManagerBlockEntity`; if the target is unavailable client-side, opening fails with feedback instead of introducing a menu solely for data transport.

The screen has a group-management tab and a membership tab. Group management provides searchable selection/creation, rename, color editing, confirmed deletion, and settings. Membership lists all synchronized peripheral names for the selected group and toggles membership through serverbound messages.

Alternative: use a container menu. Rejected because there is no inventory or menu-specific state, and existing block entity synchronization plus explicit mutation messages cover the requirement with less machinery.

### Keep mutations authoritative and centralized

Add block entity mutation operations for create, rename, delete, color, and membership changes, and have both UI packets and Lua methods route through them where semantics overlap. Each serverbound message identifies the manager position and requested operation. The handler verifies that the sender holds the Ultimate Configurator, that it is in network manager mode bound to that position, and that the target is a loaded network manager before applying validated input and synchronizing changes.

Group names are non-empty, length-limited strings. Rename is atomic and preserves color and membership. Confirmed deletion removes the group and its memberships. Peripheral membership mutations reject names not currently attached to that manager. Existing group-change events continue for membership additions/removals, including removals caused by deletion; rename and color changes do not synthesize membership events.

Alternative: let the client edit synchronized NBT directly. Rejected because it permits stale or unauthorized writes and would bypass Lua-visible state and events.

### Split server data, item selection, and local presentation state

- Server block entity: real group names, colors, memberships, delimiter, and overlay range.
- Configurator NBT: selected full group name and expanded hierarchy paths.

When a selected group is renamed, a successful UI response updates the held configurator selection to the new name. When it is deleted, the selection is cleared. Other configurators with stale selections are handled safely by rejecting assignment until a valid group is selected.

Alternative: store all settings on the item. Rejected because delimiter and range belong to each network manager. Expansion state remains on the configurator because it is transient UI state.

### Derive a virtual hierarchy from full group names

The configured delimiter splits each real group name into path segments. Intermediate paths are generated in memory for display only; selecting a leaf always resolves to its original full group name. Lua `get` queries aggregate peripherals from real groups beneath the requested path. Empty delimiters disable hierarchy and show a flat list. Empty path segments are displayed consistently but do not create server-side groups.

Search matches full group names and presents matching leaves with enough path context to distinguish them. Renaming edits the full group name, so hierarchy placement changes naturally.

Alternative: persist a separate hierarchy model. Rejected because it duplicates information and creates synchronization and migration problems without affecting group behavior.

### Move assignment and range controls into the new workflow

Using the configurator on air opens the screen. Clicking an attached peripheral with a valid selected group toggles membership without requiring a name tag. Swing no longer changes range; range is edited in the settings area, stored on the manager, and consumed by the overlay renderer from synchronized state.

The overlay continues to render authoritative group labels/colors and uses the selected manager's synchronized range. Name-tag assignment is removed rather than retained as a second, conflicting selection mechanism.

## Risks / Trade-offs

- [The manager chunk is not loaded on the client when opening the UI] -> Refuse to open and show actionable feedback; do not display stale invented state.
- [Two players edit a group concurrently] -> Validate against current server state, apply each operation atomically on the server thread, and rely on block entity synchronization to refresh both screens.
- [Rename or deletion leaves stale selections on other configurators] -> Validate selected groups on every assignment and require reselection when stale.
- [A delimiter produces ambiguous or empty path segments] -> Preserve full names as identity and treat hierarchy strictly as presentation.
- [Expansion paths become stale after rebinding] -> Clear them whenever the configurator target changes or is removed.
- [Existing automation depends on non-empty group deletion being rejected] -> Keep the existing Lua `removeGroup` contract unless separately changed; confirmed destructive deletion is exposed through the validated UI mutation path.

## Migration Plan

Existing block entity group NBT requires no migration. Existing managers use the default delimiter and range when those fields are absent. Existing configurators gain no selected group until the player chooses one in the UI.

Rollback restores the old interactions without transforming server group data. New configurator selection and expansion tags are harmless if ignored by an older build.

## Open Questions

None.
