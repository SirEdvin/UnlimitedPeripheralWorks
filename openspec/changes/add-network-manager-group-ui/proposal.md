## Why

Network manager groups can currently be assigned in-world only by combining the Ultimate Configurator with renamed name tags, while creation and maintenance require Lua calls. A native UI is needed so players can discover, organize, and manage network groups without external items or computers.

## What Changes

- Add a searchable network manager group UI opened by using an Ultimate Configurator in network manager mode on air.
- Allow players to select or create groups and use the selected group when toggling peripheral membership in-world.
- Add group management for rename, confirmed deletion including memberships, and color editing.
- Add a membership view that lists all peripherals connected to the selected network manager and allows membership changes.
- Display groups as a client-side hierarchy derived by splitting group names with a configurable delimiter; hierarchy nodes are visual only and do not create server groups.
- Persist delimiter, overlay range, and hierarchy presentation settings locally per client, dimension, and network manager position.
- Store the selected assignment group on the Ultimate Configurator.
- Move overlay range configuration into the UI.
- **BREAKING**: Remove renamed name tags as the network manager group assignment mechanism and remove swing-to-cycle range control.

## Capabilities

### New Capabilities
- `network-manager-group-ui`: Configurator-driven group selection, editing, membership management, client-side hierarchy, and overlay settings for a bound network manager.

### Modified Capabilities

None.

## Impact

- Affects the Ultimate Configurator network manager mode, client rendering, network manager block entity group mutations, and loader-neutral networking.
- Adds a native Minecraft screen and client-local settings persistence for network manager presentation preferences.
- Extends server validation and mutation paths for group creation, rename, deletion, color, and membership updates while preserving the existing Lua-facing group API unless explicitly superseded by the new mutation behavior.
- Requires localized UI text and automated checks for hierarchy parsing and authoritative group mutations on both supported loaders.
