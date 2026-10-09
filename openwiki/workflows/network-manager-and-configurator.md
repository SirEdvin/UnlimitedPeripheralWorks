---
type: workflow
title: Network manager, proxy and configurator
description: Traces wired topology, group and target persistence, proxy refresh/cleanup and the server-authoritative configurator workflow.
tags: [networking, configurator, persistence, validation, lifecycle]
verified:
  - by: openwiki/0.7.1
    at: 2026-10-09T21:19:27.304Z
sources:
  - id: openwiki-source-7a3a8e464c2ffb11e1c6e33a
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/blockentity/NetworkManagerBlockEntity.kt
  - id: openwiki-source-241128cf9df5147c26c9b962
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/blockentity/PeripheralProxyBlockEntity.kt
  - id: openwiki-source-39d8404c2def6a53af20e247
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/item/UltimateConfigurator.kt
  - id: openwiki-source-51c351c53a91f5ec6a346bde
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/peripherals/NetworkManagerPeripheral.kt
  - id: openwiki-source-38f6663c2a489bea83a2e814
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/networking/ConfiguratorTargetActionMessage.kt
  - id: openwiki-source-a01de05495b3416c676061dd
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/networking/NetworkManagerGroupMessage.kt
  - id: openwiki-source-326d9b1015178bd4bec88c0c
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/subsystem/configurator/PeripheralProxyMode.kt
generated: { by: "hermes", at: "2026-10-09T21:19:27.304Z" }
---

# Network manager, proxy and configurator

## Distinct responsibilities

The network manager observes named peripherals and their positions on a CC wired network, stores groups/settings, publishes its own peripheral and supplies display data. The proxy exposes selected remote world peripherals into a wired node. The ultimate configurator selects a target-specific mode and edits targets or manager groups through common server handlers. None of these is a replacement for CC:Tweaked's underlying node graph.

## Network manager flow

`NetworkManagerWiredElement` forwards attach/detach callbacks to the block entity. Position extraction accepts a BlockPos or BlockEntity target; unsupported targets are logged and omitted from the location map. Changes schedule synchronization, guarded against removal and server shutdown in the peripheral-sync path.

On a server block tick, pending connection refresh connects loaded adjacent wired elements on all sides except the block's facing side. The manager lazily creates its owned peripheral, allocates a stable name through CC's `ServerContext` ID allocator, publishes that name to its node and pushes pending data. Removal/destruction removes the node.

Groups own sets of peripheral names, color and DEFAULT/SHOW/HIDE visibility. Group names must be nonempty and at most 64 characters; colors range from -1 through 0xffffff. Delimiter length is at most 16 and range is 4..128. `groupPeripherals` unions the exact group and descendants whose names begin with `group + delimiter`; an empty delimiter disables descendant-prefix matching. It is a naming hierarchy, not a separate stored tree.

Membership mutations validate group and live peripheral existence. UI membership requests can supply expected prior membership, rejecting stale repeated edits. Successful changes enqueue `network_manager_group_change` events. The Lua API's `get` filters stored group names to currently live peripherals; detach removes a live location, not all stored group membership. Lua `removeGroup` refuses groups containing live peripherals, while block-entity `deleteGroup` used by the UI can delete a populated group and emits removal events. Preserve this intentional caller distinction.

NBT saves peripheral display positions, the manager's name, delimiter/range and group definitions. Load truncates delimiter length, clamps range and restores groups; client load rebuilds the display cache with aliases and sorted group names. Runtime live peripheral discovery remains separate from the restored client display map.

## Proxy flow and limits

Configurator proxy mode runs mutation on the server. It rejects self-targets, forbidden-tag blocks, positions outside configured range, additions beyond capacity and targets without a peripheral. It resolves the clicked face and toggles an existing record or adds a new one.

Each remote record persists position, direction and optional allocated peripheral name; listener/peripheral attachment flags and displayed item stacks are runtime caches. Tracking registers block-state observation, attaches the remote peripheral and publishes selected records to the proxy node. Missing initial targets defer connection attempts to ticks. Block-state events untrack/re-resolve the stored face; if no peripheral remains, the record is removed and data is synchronized.

The proxy connects only on the opposite side of its orientation. Removal/destruction purges peripheral records, unregisters tracked positions and removes its wired node. One current-source asymmetry matters when debugging face-sensitive targets: `connectBlockPos` initially resolves with NORTH, while event refresh uses the persisted direction. The wiki records this behavior without changing it.

## Configurator interaction and stored state

With no active mode, crouch-use on a supported block binds its configuration mode. Normal air-use opens detached target history; crouch air-use clears the active mode. With an active mode, block clicks and air misses delegate to that mode, preserving attached-mode precedence. Off-hand use delegates to ordinary item behavior.

The item stores active mode, block position and dimension plus recent/favorite targets. History retains at most three nonfavorites and sixteen favorites, deduplicated by dimension/position. Favorite rename and color changes validate limits and existing membership; an empty name restores the default label. Selection only accepts a stored history target in the current dimension, already loaded, whose current block resolves to the stored mode. It does not force-load a destination or silently reinterpret a changed block.

## Server authority and transport

`NetworkManagerGroupMessage` bounds wire strings and verifies the held configurator, active network-manager mode, exact target, active dimension and loaded block before retrieving the manager. Most edits also require the selected group. It validates enum indices and routes membership through expected-state checks, then reports success/failure and updates selected-group state.

`ConfiguratorTargetActionMessage` accepts detached configurators only. Selection, favorites and settings reuse item methods; invalid style names or colors return rejection, and successful changes broadcast inventory updates. The client screens are presentation and intent senders, not authorities for world mutation. These checks establish specific eligibility, not a blanket claim of region protection or distance-based authorization.

## Tests and extension seams

Shared `PeripheralWorksGameTests` covers manager/configurator state and persistence, while `NetworkManagerClientGameTests` drives actual screens and waits for server synchronization. `projects/typescript-tests/src/network_manager.ts` covers the Lua surface. New operations should reuse block-entity/item validation, add a bounded message operation, update the relevant screen and typed API if needed, then exercise both server and client paths.

See [peripheral discovery](peripheral-discovery-and-extensions.md), [configuration caveats](../operations/configuration-and-release.md), and [GameTest execution](../testing/gametests-and-fixtures.md).
