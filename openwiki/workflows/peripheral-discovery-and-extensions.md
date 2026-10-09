---
type: workflow
title: Peripheral discovery and extension workflow
description: Explains loader fallback discovery, priority/conflict selection, agnostic storage plugins and stateful hub upgrade composition.
tags: [peripherals, plugins, storage, upgrades, extension]
verified:
  - by: openwiki/0.7.1
    at: 2026-10-09T21:19:27.304Z
sources:
  - id: openwiki-source-762178e29747676741723890
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/api/PeripheralPluginProvider.kt
  - id: openwiki-source-22ec0050c25a2233b82b564d
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/ComputerCraftProxy.kt
  - id: openwiki-source-436ae136f91edabf041dff5b
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/GenericProvider.kt
  - id: openwiki-source-71d77a97464c33b05f64adf2
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/modem/PeripheralHubPeripheral.kt
  - id: openwiki-source-a34172c1d30f8dc724247c50
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/peripherals/PeripheraliumHubPeripheral.kt
  - id: openwiki-source-3968b0f2e074fee625b4adc3
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/peripherals/turtles/TurtlePeripheraliumHubPeripheral.kt
  - id: openwiki-source-a593391ecf4764c683e013f1
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/plugins/specific/SpecificPluginProviders.kt
  - id: openwiki-source-ade86cd46e615ca63025142a
    resource: repo://projects/fabric/src/main/kotlin/site/siredvin/peripheralworks/FabricPeripheralWorks.kt
  - id: openwiki-source-f22a29edb5b417017ce5f148
    resource: repo://projects/forge/src/main/kotlin/site/siredvin/peripheralworks/ForgePeripheralWorks.kt
generated: { by: "hermes", at: "2026-10-09T21:19:27.304Z" }
---

# Peripheral discovery and extension workflow

## From world lookup to Lua methods

Fabric's peripheral fallback first asks block entities implementing `IPeripheralProvider` for their owned peripheral; otherwise it calls the shared eager provider. Forge tries an owned entity peripheral and, if none is returned, uses the shared lazy supplier wrapped in LazyOptional. Fabric returns the owned-provider result directly, even if null; the two fallback paths are not identical.

`ComputerCraftProxy` rejects the shared IGNORE tag, collects eligible plugins and returns no peripheral when none apply. It chooses a block-entity owner for `IPeripheralBlockEntity`, a raw entity owner for ordinary block entities, or a position/world owner for blocks without entities. `PluggablePeripheral` uses the block registry ID as its primary type, carries a side, delegates method/plugin behavior to Tweakium's OwnedPeripheral and identifies itself with the additional constructor marker `upw`.

The lazy path captures selected plugins when creating the supplier, then reads block state/entity when evaluating it. Do not treat that supplier as automatically recomputing plugin eligibility against a later world state.

## Provider precedence is behavior

`PeripheralPluginProvider` defaults to priority 100; lower priority wins. Adding a provider sorts the list. Collection skips a provider if its declared type has been selected/denied or its conflicts intersect selected types. A successful plugin is indexed using `additionalType` when supplied, otherwise provider type, and its provider conflicts become denied types. A provider returning null does not reserve its type or deny conflicts.

A concrete invariant is protected jukebox handling: `SpecificProtectedPluginProviders` has priority 50 and conflicts with item-storage/inventory, preventing generic storage from bypassing specialized jukebox behavior when the specific plugin is enabled. Ordinary vanilla plugins cover note blocks, powered rails, lecterns, beacons and jukeboxes under their feature config gates.

Choose a type and precedence deliberately for new providers; registering another provider with a similar name is not sufficient to establish coexistence. Inspect `collectPlugins` and neighboring providers for `additionalType` and conflict behavior.

## Generic storage adaptation

`GenericProvider.kt` bridges Broccolium world lookups into Tweakium plugins:

- Item lookup prefers an inventory plugin for nonempty slotted storage with inventory enabled, otherwise an item-storage plugin if enabled. Its conflicts prevent duplicated inventory/item-storage surfaces.
- Fluid lookup returns no plugin when disabled or absent, otherwise uses the configured transfer limit.
- Energy lookup chooses FullEnergyPlugin if always-transferable is enabled or the energy unit is in the transferable registry; otherwise it exposes read-oriented EnergyPlugin.

Capabilities and Fabric transfer adapters live below the agnostic lookup boundary. Shared providers should not import loader APIs. Effective config getter mismatches are documented in [operations](../operations/configuration-and-release.md); trace getters rather than assuming each advertised toggle is independently wired.

## Owned peripherals and hub composition

Owned feature peripherals such as scanner, recipe registry and network manager bind their own feature state/owner and expose LuaFunctions. They are distinct from arbitrary-block PluggablePeripheral discovery. Common setup registers turtle/pocket upgrade serializers, with feature-specific wrappers supplying appropriate owners.

`PeripheralHubPeripheral` composes remote peripherals into records rather than merging arbitrary plugins into one flat method table. It maintains a concurrent name map, synchronizes attach/remove/purge with computer attachment, and uses weak peripheral identity tracking to avoid duplicate attachment. `callRemote` resolves a named record and delegates arguments after remote name/method; missing names raise LuaException. Names, methods and type queries expose this hub surface.

Peripheralium hubs persist connected upgrade IDs, active mode and per-upgrade compound state. Turtle reconstruction looks up IDs and reconnects only resolvable upgrades through LocalTurtleWrapper; duplicate upgrade IDs are rejected. Equipping validates a 1-based storage slot, checks eligibility, removes one item, and restores it to the slot on an unsuccessful equip result. Unequipping returns the upgrade item to inventory or world through the existing helper.

Current source uses `activeUpgrades.size > maxUpdateCount` for the capacity precheck, not `>=`. Therefore the configured nominal count should not be documented as a proven strict maximum. Preserve and test item accounting and failure paths if that behavior is changed in a separate code task.

## Smallest useful extension route

1. Decide whether this is an owned feature, arbitrary-block plugin, entity adapter or upgrade.
2. Reuse the matching owner and agnostic lookup seam; avoid new platform abstractions for an already-supported storage type.
3. Define config eligibility, provider priority/conflicts, side sensitivity and unavailable behavior.
4. Register at the correct loader lifecycle stage behind optional-mod presence where needed.
5. Update the source TypeScript API, not generated declarations/Lua, for public method changes.
6. Add a focused Kotlin invariant and actual Lua fixture call, then test both loader paths.

Relevant sources are `ComputerCraftProxy.kt`, `GenericProvider.kt`, `plugins/specific/SpecificPluginProviders.kt`, `modem/PeripheralHubPeripheral.kt`, and the shared/turtle PeripheraliumHubPeripheral classes. See [runtime ownership](../architecture/runtime-and-platforms.md), [optional integration adapters](../integrations/optional-mods.md), and [verification](../testing/gametests-and-fixtures.md).
