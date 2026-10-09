---
type: integration
title: Optional integrations and AE2 lifecycle
description: Covers mod-presence boundaries, AE2 resources and asynchronous services, and Forge-specific GTCEu and neural-network adapters.
tags: [integrations, ae2, gtceu, lifecycle, optional-mods]
verified:
  - by: openwiki/0.7.1
    at: 2026-10-09T21:19:27.304Z
sources:
  - id: openwiki-source-73e8bbeee0b43b9c69affab1
    resource: repo://projects/core/src/ae2Integration/kotlin/site/siredvin/peripheralworks/integrations/ae2/AE2CraftingJobs.kt
  - id: openwiki-source-d9a80f4e73a80a2d70d16373
    resource: repo://projects/core/src/ae2Integration/kotlin/site/siredvin/peripheralworks/integrations/ae2/AE2StorageSubscriptions.kt
  - id: openwiki-source-4831c95d26860958bad85eb1
    resource: repo://projects/core/src/ae2Integration/kotlin/site/siredvin/peripheralworks/integrations/ae2/Integration.kt
  - id: openwiki-source-1658bd710c662d38561a9929
    resource: repo://projects/core/src/ae2Integration/kotlin/site/siredvin/peripheralworks/integrations/ae2/Registration.kt
  - id: openwiki-source-84d7bd071702eb72e55d9fa6
    resource: repo://projects/core/src/testMod/kotlin/site/siredvin/peripheralworks/testmod/PeripheralWorksGameTests.kt
  - id: openwiki-source-ade86cd46e615ca63025142a
    resource: repo://projects/fabric/src/main/kotlin/site/siredvin/peripheralworks/FabricPeripheralWorks.kt
  - id: openwiki-source-f22a29edb5b417017ce5f148
    resource: repo://projects/forge/src/main/kotlin/site/siredvin/peripheralworks/ForgePeripheralWorks.kt
  - id: openwiki-source-6e5c009508bf7d9bca04a02d
    resource: repo://projects/forge/src/main/kotlin/site/siredvin/peripheralworks/integrations/gtceu/Integration.kt
  - id: openwiki-source-ff58357406138a27fe1f1d23
    resource: repo://projects/forge/src/main/kotlin/site/siredvin/peripheralworks/integrations/gtceu/MachineInfo.kt
generated: { by: "hermes", at: "2026-10-09T21:19:27.304Z" }
---

# Optional integrations and AE2 lifecycle

## Presence is a runtime boundary

Loader entrypoints explicitly invoke `maybeLoadIntegration` for supported mod IDs. Keep optional API references behind those entrypoints; a compile-only dependency does not mean the mod is installed in a player world. Forge loads Extra HNN only inside the Hostile Networks presence check. Configuration discovery separately filters integration configuration objects by installed mod ID, so configuration section availability and registration stage are related but distinct mechanisms.

AE2 is compiled from shared `projects/core/src/ae2Integration/kotlin` into each loader. The version catalog selects AE2 Fabric `15.0.7-beta` and Forge `15.2.13`; never assume the same upstream binary APIs on both.

## AE2 registration and resource loading

Early `Registration` creates the pattern pedestal, ME network peripheral, their block entities, wired-network P2P item and wireless turtle/pocket upgrade registrations. Its data-generation hooks wrap recipes in loader-specific AE2 conditions and place AE2 block loot in the optional built-in resource pack.

Fabric registers `resourcepacks/ae2` as always-enabled only with AE2 installed; Forge adds it only for server data and AE2 presence. This protects absent-mod startup from parsing AE2-only loot. `PeripheralWorksGameTests.optionalAE2Resources` checks registrations, recipe and loot presence in both cases while preserving base loot tables.

Ordinary integration setup conditionally registers agnostic ME item/fluid/energy lookups and the ME plugin suite. Storage adapters require a grid storage/energy service; missing grid services produce no lookup result. With the ME interface enabled, providers add network operations, storage subscriptions, crafting jobs and configurable cable/interface/pattern-provider objects. P2P attunement is registered separately.

## Storage event lifecycle

`AE2StorageSubscriptionTracker` owns named item/fluid definitions and an ephemeral baseline of amounts. Definitions serialize to NBT; loading bounds the definition count, skips malformed entries and clears the baseline. Item filters accept strings/tables, fluid filters resolve registered names, and filter depth, value count, string and encoded-size limits constrain persistence.

Observation requires both at least one definition and an attached event sink. The watcher resets and baselines against grid cached inventory when activating or changing grids. Baseline creation does not replay inventory as changes. Later matching public-amount changes emit `ae2_storage_change` with subscription name, resource, previous amount and current amount. Last detach removes the sink; stop resets the watcher and baseline. Copy-on-write sinks protect attachment-thread iteration without making all tracker state generally thread-safe.

Subscription replacement compiles and validates before updating the definition map; if the definitions-changed callback throws, the previous definition is restored. Preserve that atomicity and the wire/persistence bounds when extending filter representations.

## Crafting job lifecycle

`scheduleCrafting` rejects nonpositive amounts and checks overflow converting public fluid amounts. It schedules main-thread tasks through CC:Tweaked, starts AE2 asynchronous calculation, yields while the future is unfinished, and revalidates the resolved context on later polls. It re-creates the action source after calculation before submission. Exceptions or abandoned callbacks cancel the pending calculation; neither computer nor server thread waits on an unfinished future.

Submission reports missing items, an absent target CPU or AE2 rejection rather than pretending a job exists. Standalone output returns to network storage; the implementation may recover the link from the CPU when AE2 does not return it directly. The weak-key, in-memory job map scopes lookup to the crafting service. It is not a durable job database. CPU/link state distinguishes running, done and canceled jobs; repeated cancellation and canceling completed jobs return explicit failures.

Machine access verifies entity identity, removal and loaded chunk. Wireless access resolves a session and performs operations with the computer owner's player action source. Follow those existing access paths rather than bypassing them with raw service calls.

## Forge integration examples

GTCEu `Integration` registers workable, controllable and machine plugins plus energy-info, turbine, cover and central-monitor capability adapters. Capability resolution tries unsided access before the requested side. `MachineInfo.collect` re-resolves capabilities on every call because multiblock formation/hatches can change. It groups machine identity, status, recipe, energy, maintenance and multiblock state; unformed controllers omit formed-only energy/tier data. Exact stored/capacity EU strings accompany numeric values, and maintenance bits mean fixed problems, not outstanding ones. The repository pins GTCEu `mc1.20.1-7.5.3`.

Hostile Networks registers a config-gated loot fabricator provider and config-gated item details for valid data-model progress. This is different from exposing arbitrary item NBT: the adapter normalizes identity and progression through `NeuralModels`.

## Verification and extension route

Relevant focused suites include `AE2GameTests` for transfers, subscription persistence/corruption/bounds/concurrent attachment, disconnected calls and P2P cleanup; `AE2CraftingJobsGameTests` for task handoffs/revalidation and terminated calculations; pattern pedestal, configurable objects and wireless terminal suites; Forge `GTCEUInfoGameTests`, `GTCEUPeripheralGameTests`, `NeuralNetworksGameTests` and `ExtraNeuralNetworksGameTests`.

For a new adapter, trace loader stage and mod presence first, then config discovery, provider precedence, typed bindings and optional-dependency test matrix. See [architecture](../architecture/runtime-and-platforms.md), [plugin discovery](../workflows/peripheral-discovery-and-extensions.md), and [test environments](../testing/gametests-and-fixtures.md).
