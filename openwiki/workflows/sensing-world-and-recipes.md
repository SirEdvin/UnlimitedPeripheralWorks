---
type: workflow
title: Sensing, world operations and registry queries
description: Explains owner-based scanning, sensor registries, observer events, reality-anchor mutations and extensible recipe serialization.
tags: [scanning, world, recipes, events, validation]
verified:
  - by: openwiki/0.7.1
    at: 2026-10-09T21:19:27.304Z
sources:
  - id: openwiki-source-0ec9674500f94bb99cc65620
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/blockentity/RemoteObserverBlockEntity.kt
  - id: openwiki-source-181cf0afbad994da5dffd0cd
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/operations/SphereOperations.kt
  - id: openwiki-source-322fff2c2f7b28c8b1909014
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/peripherals/InformativeRegistryPeripheral.kt
  - id: openwiki-source-a6423dafbaad2e849412d03f
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/peripherals/RealityForgerPeripheral.kt
  - id: openwiki-source-73b4fba9914b05667b84dae0
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/peripherals/RecipeRegistryPeripheral.kt
  - id: openwiki-source-1177934677c2d36c845922bc
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/peripherals/RemoteObserverPeripheral.kt
  - id: openwiki-source-29a92ffce8c697a28a2ffcad
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/peripherals/UltimateSensorPeripheral.kt
  - id: openwiki-source-7ade93d70d974934a15e995f
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/computercraft/peripherals/UniversalScannerPeripheral.kt
  - id: openwiki-source-5c4689e54b3d42050d95240e
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/subsystem/recipe/RecipeRegistryToolkit.kt
generated: { by: "hermes", at: "2026-10-09T21:19:27.304Z" }
---

# Sensing, world operations and registry queries

## Owner-based scanning

UniversalScannerPeripheral supports turtle, pocket and stationary block owners. Every factory attaches operation support with the configured cooldown threshold; portable factories additionally attach fuel. Initialization chooses portable versus stationary scan operation by fuel-boon presence, then attaches Tweakium's ScanningBoon for blocks, living entities, dropped items and players using the configured maximum radius.

UPW owns this wiring and operation configuration, not the external scanning implementation. `SphereOperations` defines portable defaults of cooldown 1000, free radius 8 and maximum radius 16, and stationary defaults of cooldown 500 and free/maximum radius 24. Treat these as raw operation values: the external operation API controls time units and execution semantics. Cost is zero within free radius; beyond it the implementation computes the difference in cube volumes times configured extra cost and floors the result. It is not a mathematical sphere-volume calculation despite the enum name.

A source caveat: stationary default extra block cost is 0.0 while the config value's declared minimum is 0.1. Its normal free/max radii coincide, so this does not by itself prove a charged default scan; inspect loaded config and the external validation implementation when changing those radii.

## Ultimate sensor extension registry

UltimateSensorPeripheral maintains named analyzers and inspectors; duplicate names throw at registration. Built-ins include dimension analysis and dimension/biome/weather/orientation/time/light/calendar/chunk inspection. Each WrappedCall either invokes its function directly or through owner.withOperation when an operation is supplied. Lua analyze/inspect methods run on the main thread and reject unknown names; list methods expose the registered name sets.

Register extensions before creating peripheral instances: construction collects the operations currently present in both registries. Reuse SensorCollection and the existing operation wrapper rather than duplicating owner/world access.

## Observer persistence and event consumption

RemoteObserverPeripheral converts Lua coordinates relative to the block's position/facing. Adding a position checks `isPosApplicable`, which rejects self and out-of-range positions. The block entity stores tracked positions and text/box styles in NBT, registers positions with BlockStateUpdateEventBus, re-registers on load and unregisters on removal/destruction. Its tick converts previous/current states and properties into a `block_change` event with relative position.

Important current-source limits: block-entity `addPosToTrack` directly appends without a duplicate/capacity check; the Lua add method checks range but does not independently enforce configured capacity. Also `handleTick` traverses the shared event bus without filtering to this observer's tracked list. Do not document strict per-observer event isolation or universal capacity enforcement without a separate source change and regression test. The configurator's selection path has its own constraints; caller-level validation is not automatically a block-entity invariant.

## Reality forger mutation boundary

RealityForgerPeripheral changes FlexibleRealityAnchorBlockEntity mimic state and supported appearance flags rather than replacing every target world block with the requested block. Position parsing uses owner-relative/facing interpretation.

For `forgeRealityPiecesInner`, every requested position is checked for configured radius and anchor identity before interpreting the requested state, checking the forbidden block tag and applying changes. This prevalidation protects one request from a later invalid position in that same request. `batchForgeRealityPieces` applies requests sequentially and stops on failure; it is not a transaction across the entire batch, so earlier records may already be changed.

`forgeReality` traverses nearby anchors and applies the requested state; `detectAnchors` reports relative anchor positions. `clearAnchors` either traverses nearby anchors or accepts explicit positions and clears matching mimic state. The explicit clear-position branch has no matching radius check in this source. Do not extrapolate the forge-pieces validation to every method. Preserve tag validation and verify all sibling mutation paths when evolving this API.

## Recipe registry data flow

RecipeRegistryPeripheral gets runtime recipes from the world's recipe manager and delegates normalization to RecipeRegistryToolkit. `get` returns nil for invalid/missing IDs; `getRaw` reads server resource JSON, converts it via NBT and returns nil plus an error when reading/parsing fails. Raw resource lookup and runtime recipe lookup answer different questions, especially with generated or transformed recipes.

The toolkit owns registries for recipe transformers, value serializers, type-specific search predicates and excluded type IDs. A matching registered recipe class transforms a recipe; otherwise DefaultRecipeTransformer is used. Serializers handle ingredients, item stacks, items, fluids, JSON, entity types and NBT. These registries are class-based mutable maps; do not assume deterministic most-specific dispatch among overlapping class registrations.

Type selection accepts null or `*` for all types, a namespaced type name, a nonnamespaced string prefix, or a table of explicit types; invalid input raises LuaException. The Lua `getTypes` hides excluded types, but exclusion is not an authorization boundary for other selectors. `search` resolves a non-air item and searches result items across selected types, returning serialized matches. Custom toolkit predicates are a separate `findRecipesForType` extension path; do not claim Lua search automatically invokes them.

## Informative registry and privacy scope

InformativeRegistryPeripheral exposes registered lists/descriptors for tags, mods, items, blocks, fluids and other data. `addList` is an integration seam and overwrites an existing name rather than rejecting duplicates. The mod-list and mod-description paths apply `informativeRegistryModBlocklist`; that setting does not imply every registry query is filtered by mod namespace.

## Verification route

Computer fixtures under `projects/typescript-tests/src` cover scanner, sensor, observer, reality-forger and registry methods. The reality-forger fixture discovers a real anchor, forges individual/batch/all appearances and clears selected/all anchors. These representative successful calls are not proof of all failure bounds. For security or partial-update changes, add explicit negative GameTests and inspect the corresponding shared Kotlin setup.

See [peripheral ownership](peripheral-discovery-and-extensions.md), [configuration and limits](../operations/configuration-and-release.md), [network targets](network-manager-and-configurator.md), and [fixture execution](../testing/gametests-and-fixtures.md).
