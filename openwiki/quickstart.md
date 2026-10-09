---
type: quickstart
title: Repository quickstart and task map
description: Start here to route development, integration, configuration and verification work to evidence-grounded repository guides.
tags: [quickstart, navigation, setup, architecture]
verified:
  - by: openwiki/0.7.1
    at: 2026-10-09T21:19:27.304Z
sources:
  - id: openwiki-source-ede6d87351d544e147a5ad29
    resource: repo://.github/workflows/gametest.yml
  - id: openwiki-source-2a9daaac1604f238ef4c63fb
    resource: repo://build.gradle.kts
  - id: openwiki-source-b68c69ba290f0fee793ec69b
    resource: repo://gradle.properties
  - id: openwiki-source-81d5f1627e19148569f46f81
    resource: repo://gradle/libs.versions.toml
  - id: openwiki-source-9ccd932b426a80129c387139
    resource: repo://gradle/wrapper/gradle-wrapper.properties
  - id: openwiki-source-9f94e9d408596b133a409713
    resource: repo://projects/forge/build.gradle.kts
  - id: openwiki-source-96b2de50e0c14ebf72078c74
    resource: repo://projects/typed-peripheral-unlimitedperipheralworks/build.gradle.kts
  - id: openwiki-source-4438469adfcfe67ff731b458
    resource: repo://projects/typed-peripheral-unlimitedperipheralworks/tsconfig.json
  - id: openwiki-source-e620d7484b72a53c7fa812cd
    resource: repo://settings.gradle.kts
generated: { by: "hermes", at: "2026-10-09T21:19:27.304Z" }
---

# Repository quickstart and task map

## What this checkout contains

Unlimited Peripheral Works adds peripherals and mod integrations to CC:Tweaked. This checkout targets Minecraft 1.20.1 and mod version 1.9.2, with Java 17, Kotlin 2.0.0 and Gradle wrapper 8.14. The version catalog selects CC:Tweaked 1.116.1, Fabric Loader 0.17.2 and Forge 47.3.39. These are checkout baselines, not latest-version recommendations.

The five modules are shared `core`, `fabric`, `forge`, `typed-peripheral-unlimitedperipheralworks` and `typescript-tests`. Shared AE2 integration/test source trees are compiled into loader modules against their respective AE2 dependency versions. Loader startup installs shared platform facades before configuration and registrations.

## First useful steps

1. Inspect the working branch/status and use the checked-in Gradle wrapper with Java 17.
2. Read [setup and build](workflows/setup-and-build.md) for dependencies and build side effects. In particular, Forge compilation copies some Fabric integrations into Forge sources, and typed API generation writes Lua/declarations in its project tree.
3. For application changes, run the timed multi-loader build and relevant server/client GameTests; for wiki-only changes, validate the wiki without triggering unrelated generation.
4. Keep complete logs under ignored build output, record command/exit/duration/log path, and stop clients/servers after collection.

A root `build` is not sufficient runtime verification. Root `gameTest` invokes both loader server tasks; root `clientGameTest` invokes both client tasks. The default PR workflow runs minimal server GameTests but not the full integration/client matrix.

## Choose the task, not a directory dump

| If you need to… | Start with… | Then follow… |
| --- | --- | --- |
| Understand boot order, dependency ownership or loader boundaries | [Runtime architecture](architecture/runtime-and-platforms.md) | [Optional integrations](integrations/optional-mods.md) |
| Add a block plugin, storage adapter or combined upgrade | [Peripheral discovery and extensions](workflows/peripheral-discovery-and-extensions.md) | [Test fixtures](testing/gametests-and-fixtures.md) |
| Debug wired membership, remote targets, favorites or UI requests | [Network manager and configurator](workflows/network-manager-and-configurator.md) | [Configuration](operations/configuration-and-release.md) |
| Change scans, sensors, observers, anchor appearance or recipe APIs | [Sensing, world and recipes](workflows/sensing-world-and-recipes.md) | [Peripheral ownership](workflows/peripheral-discovery-and-extensions.md) |
| Work on AE2 subscriptions/crafting/P2P or Forge machine integrations | [Optional integrations](integrations/optional-mods.md) | [Dependency matrices](testing/gametests-and-fixtures.md) |
| Prepare a cold environment or investigate generated-source changes | [Setup/build](workflows/setup-and-build.md) | [Operations](operations/configuration-and-release.md) |
| Reproduce a runtime failure or add regression coverage | [GameTests and Lua fixtures](testing/gametests-and-fixtures.md) | The corresponding feature workflow |
| Inspect effective TOML settings, diagnostics or release contracts | [Configuration and release](operations/configuration-and-release.md) | [Setup/build](workflows/setup-and-build.md) |

## Evidence and known edges

Pages explain current code and tests, including limitations rather than silently correcting them. Notable routes include effective enable-setting mismatches, hub capacity precheck behavior, proxy face-sensitive reconnect behavior, observer event/capacity limitations and reality-forger batch/clear validation differences. These observations are documented, not application fixes performed during wiki generation.

Repository source/tests remain authoritative. OpenWiki's claims/provenance record bounded source evidence; retrieval helps find the relevant flow, but does not replace inspection after code changes. Application test descriptions are coverage contracts, not a claim that this wiki-generation run executed Gradle or Minecraft tests.

Use OpenWiki search for a concrete question and read the returned complete sections. For example, “How do AE2 crafting requests revalidate access after calculation?” should lead to the integration lifecycle, while “Why did a build change Forge integration files?” should lead to setup/build hazards. Follow conceptual neighbors only as needed.
