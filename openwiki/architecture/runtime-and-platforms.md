---
type: architecture
title: Runtime architecture and platform boundaries
description: Explains Fabric and Forge startup, shared peripheral registration, external library ownership and client-only extension seams.
tags: [architecture, loaders, lifecycle, platforms]
verified:
  - by: openwiki/0.7.1
    at: 2026-10-09T21:19:27.304Z
sources:
  - id: openwiki-source-da591d1b2345a79222b53be9
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/PeripheralWorksClientCore.kt
  - id: openwiki-source-27ed0f4c9ee7386cff9a2c48
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/PeripheralWorksCore.kt
  - id: openwiki-source-9fd0d7567ca649706fd9b4ab
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/xplat/ModPlatform.kt
  - id: openwiki-source-355ddfc48b45d2417df899a6
    resource: repo://projects/fabric/build.gradle.kts
  - id: openwiki-source-ade86cd46e615ca63025142a
    resource: repo://projects/fabric/src/main/kotlin/site/siredvin/peripheralworks/FabricPeripheralWorks.kt
  - id: openwiki-source-9f94e9d408596b133a409713
    resource: repo://projects/forge/build.gradle.kts
  - id: openwiki-source-f22a29edb5b417017ce5f148
    resource: repo://projects/forge/src/main/kotlin/site/siredvin/peripheralworks/ForgePeripheralWorks.kt
generated: { by: "hermes", at: "2026-10-09T21:19:27.304Z" }
---

# Runtime architecture and platform boundaries

## Responsibility map

Unlimited Peripheral Works is a Minecraft mod whose shared Kotlin implementation adds owned peripherals, generic peripheral plugins and integration adapters to CC:Tweaked. `settings.gradle.kts` maps five Gradle projects to `projects/`: core, Fabric, Forge, typed peripheral bindings and TypeScript test fixtures. The loaders are adapters, not independent copies of the main feature implementation.

The external libraries form important boundaries:

- CC:Tweaked owns computer, peripheral, wired-node and upgrade APIs.
- Broccolium provides platform registries/toolkit and agnostic item, fluid and energy lookups used by core.
- Tweakium provides owned peripherals, peripheral owners/plugins, standard storage plugins and the computer platform base.
- Peripheralium has loader bootstrap dependencies and supplies shared integration infrastructure used by the project.

These are dependency boundaries visible in imports and Gradle bundles; their internal implementations are not part of this repository. Do not infer their guarantees solely from UPW call sites.

## Startup flow

The Fabric metadata selects `FabricPeripheralWorks` as its Kotlin main initializer and a separate client initializer. Forge selects the `@Mod(peripheralworks)` object with KotlinForForge.

Both loader paths first call `PeripheralWorksCore.configure` with loader-specific platform, recipe ingredient and block-reference implementations. Core installs these facades, registers minecart item lookup, generic item/fluid/energy and specific block providers, entity storage providers, and an energy refuel handler. `ModPlatform.baseInnerPlatform` fails if accessed before configuration.

Next, loaders initialize `ConfigHolder` using their mod-presence predicate, register a common ForgeConfigSpec, and run AE2's early `Registration` only if available. Shared `PeripheralWorksCommonHooks.onRegister` initializes block entities, items, blocks, recipes, upgrade serializers, energy definitions and the creative tab.

Fabric completes most integration loading during `onInitialize`; Forge distributes it between constructor registration, `registrySetup` and `commonSetup`. For example Forge's registry stage loads Integrated Dynamics, Nature's Compass, Ars Nouveau and ProjectE, while common setup loads GTCEu and neural-network integrations. Preserve stage selection when adding registrations that depend on loader registries.

The common after-configuration hook registers common-energy to turtle-fuel conversion when enabled. Peripheral fallback registration then connects arbitrary world blocks to the shared plugin pipeline. See [peripheral discovery](../workflows/peripheral-discovery-and-extensions.md).

## Loader-specific transport and storage

`ModInnerPlatform` extends the external computer platform contract with mod information, message-type construction, server packet creation, constrained slotted storage and compare-and-replace item operations. Keep common feature code against this seam; capability and Fabric transfer APIs belong in their respective loader modules.

Fabric registers server message receivers and invokes common handlers through `ServerNetworkContext`. Forge installs `ForgeNetworkHandler` at construction. Validation lives in common message handlers, rather than being replaced by transport assumptions.

The shared AE2 source tree is deliberately compiled into both loader Kotlin tasks (`src/ae2Integration/kotlin`) instead of being ordinary core main sources. This allows each loader to compile against its own AE2 dependency version. It is a shared implementation boundary, not a third loader.

## Client boundary

`PeripheralWorksClientCore` holds renderer/model suppliers and client hooks. Hooks registered before initialization are queued; later hooks execute immediately; initialization is idempotent. Separate `ModClientPlatform` configuration and loader client entrypoints keep rendering and screens away from ordinary server feature paths. New common registrations should not eagerly instantiate client-only implementations.

## Evidence and navigation

Key entrypoints: `PeripheralWorksCore.kt:33-46`, `FabricPeripheralWorks.kt:53-97`, `ForgePeripheralWorks.kt:71-96,119-162`, `xplat/PeripheralWorksCommonHooks.kt:25-44`, `xplat/ModPlatform.kt:6-27`, and `PeripheralWorksClientCore.kt:74-79,119-127` under their respective source roots.

Continue with [optional integrations](../integrations/optional-mods.md), [configuration lifecycle](../operations/configuration-and-release.md), and [setup/build](../workflows/setup-and-build.md). The [test guide](../testing/gametests-and-fixtures.md) explains how both adapters are exercised.
