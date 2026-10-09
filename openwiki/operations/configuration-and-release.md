---
type: operations
title: Configuration, diagnostics and release operations
description: Documents common config initialization, effective-setting caveats, runtime limits, permission-gated diagnostics and repository publishing paths.
tags: [operations, configuration, diagnostics, release]
verified:
  - by: openwiki/0.7.1
    at: 2026-10-09T21:19:27.304Z
sources:
  - id: openwiki-source-2f129f28d96febccca13e572
    resource: repo://.github/workflows/publish-npm.yml
  - id: openwiki-source-84fff43a35b825a5ff7d8a29
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/commands/DebugCommands.kt
  - id: openwiki-source-9c434bfec2e272cb3017b37e
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/configuration/ConfigHolder.kt
  - id: openwiki-source-d8f7c5ba1bbb04e7f4e92000
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/configuration/integration/AE2Configuration.kt
  - id: openwiki-source-d534f325176ec49ffc4eb69c
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/configuration/IntegrationConfiguration.kt
  - id: openwiki-source-64c04be7e1f6f035dc001d22
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/configuration/IntegrationConfigurationDiscovery.kt
  - id: openwiki-source-1474d8b32e87220ce08c2c38
    resource: repo://projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/configuration/PeripheralWorksConfig.kt
  - id: openwiki-source-96b2de50e0c14ebf72078c74
    resource: repo://projects/typed-peripheral-unlimitedperipheralworks/build.gradle.kts
generated: { by: "hermes", at: "2026-10-09T21:19:27.304Z" }
---

# Configuration, diagnostics and release operations

## Configuration ownership and timing

`ConfigHolder.initialize` constructs exactly one common spec after discovering installed integration configurations. Access before initialization and a second initialization both fail explicitly. Forge registers that spec as COMMON in `peripheralworks.toml`; Fabric registers it through Forge Config API Port. Fabric's call does not specify a filename, so its default naming is owned by that dependency rather than declared here.

Discovery scans the configuration package from file, union or JAR locations, selects top-level `*Configuration.class` singleton implementations, filters by `modID`, sorts by section name, and rejects duplicate names. Unsupported resource protocols fail rather than silently suppressing sections. Optional integration getters use declared defaults until their config values exist and the spec is loaded.

The builder groups base settings, generic/specific plugins, `plugins.Peripherals`, operations and installed integrations. Preserve exact key spelling and capitalization when editing TOML; the base cooldown key is `cooldownTreshholdLevel`, not a corrected spelling.

## Limits that affect callers

Generic item transfer defaults to 1024 per operation. Energy transfer is bounded per operation; fluid limits are scaled with the platform's fluid compact divider. Full energy transfer is enabled by default through `energyAlwaysTransferable`; disabling it allows only registry-marked transferable units. Turtle energy conversion defaults to 250 energy units per fuel point and is installed by the after-configuration hook when enabled.

Other defaults in `PeripheralWorksConfig.CommonConfig` include regular/netherite hub capacities of 3/7 upgrades, observer and proxy ranges of 16 and capacities of 8, reality-forger range 24, statue quad limit 256, and hologram limit 30 entities at distance 16. The builder validates bounds; raising a limit does not remove validation in the feature implementation. The statue quad comment specifically says it applies to newly created statues.

AE2 limits are owned by `integrations.ae2`: defaults of 16 subscriptions and 1024 persisted item-filter values, each with a positive range. The tracker also imposes wire/encoded filter bounds; increasing value count does not waive those. See [optional integrations](../integrations/optional-mods.md).

## Current effective-setting caveats

The current source contains mismatches between names and getter wiring. These are observations, not fixes made by this wiki:

- `enableGenericEnergyStorage` reads `enableGenericFluidStorage`.
- `enableUltimateSensor` reads `enableUniversalScanner`.
- `enableDisplayPedestal` reads `enableMapPedestal`.
- The proxy enable config value is defined under the same `enableRemoteObserver` key as the observer, rather than a distinct proxy key.

Consequently, do not promise independent toggles merely because separate fields or descriptive names exist. Trace the actual feature getter, builder key and call site when debugging effective configuration. The evidence is `PeripheralWorksConfig.kt:14-29,55-68,236-247`.

## Diagnostics and safe collection

The shared `upw` command tree requires permission level 3 and a player source. `printTrackedBlocks` sends tracked positions to that player; `toggleTracking` toggles block-event tracking; `inspectItem` displays the held item's NBT. Console invocation without a player does not perform those actions. Treat item NBT and coordinates as potentially private when sharing diagnostics.

The analyzer's shared right-click hook shows entity class/type to server players. Proxy and network-manager lifecycle diagnostics also use `PeripheralWorksCore.logger`; search the responsible block entity when a target is missing rather than assuming a network outage.

Keep complete Gradle logs in ignored `build/` with explicit timeouts, inspect only the relevant failure window, record command/exit/duration/log path, and stop development servers/clients after collecting results. Exact local commands and generated-artifact hazards are in [setup/build](../workflows/setup-and-build.md). JUnit reports and isolated test worlds are described in [testing](../testing/gametests-and-fixtures.md).

## Publishing contracts in this checkout

Root build plugins configure release handling for branch `1.20` and Forge JarJar. Fabric mod publishing selects `remapJar` and lists CC:Tweaked, Fabric Language Kotlin and Forge Config API Port requirements; Forge selects `jarJar` and lists CC:Tweaked and KotlinForForge. These declarations delegate publication implementation to external `site.siredvin` plugins; this repository is not sufficient evidence for their credential names or exact release commands.

The checked-in npm workflow is explicit: push tags matching `v1.20.1-*`, verify that the tag commit is an ancestor of branch `1.20`, install/build the typed package using Node 24, derive its version by removing `v1.20.1-`, then publish. Versions containing a hyphen use dist-tag `next`; others use the default npm publish tag. The workflow requests `id-token: write` and no token is hard-coded. Local Gradle Node tasks use Node 22.14.0, a separate baseline from release CI.

Do not confuse the source package's `0.5.0` version with its tag-derived release version. No release or version bump is needed for a documentation-only branch. Review changes before invoking broad build/release tasks, especially source-copy and generated-Lua tasks.

See [network/configurator lifecycle](../workflows/network-manager-and-configurator.md) for persistence and server validation, and [quickstart](../quickstart.md) for task routing.
