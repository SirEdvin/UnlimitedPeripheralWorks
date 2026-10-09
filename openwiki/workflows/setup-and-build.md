---
type: workflow
title: Developer setup and build pipeline
description: Documents checkout-pinned Java/Gradle/loader versions, module responsibilities, dependency resolution, generated outputs and build side effects.
tags: [setup, build, gradle, toolchain, generated-code]
verified:
  - by: openwiki/0.7.1
    at: 2026-10-09T21:19:27.304Z
sources:
  - id: openwiki-source-ede6d87351d544e147a5ad29
    resource: repo://.github/workflows/gametest.yml
  - id: openwiki-source-8037e2358a2c4f9b2c722a11
    resource: repo://AGENTS.md
  - id: openwiki-source-2a9daaac1604f238ef4c63fb
    resource: repo://build.gradle.kts
  - id: openwiki-source-b68c69ba290f0fee793ec69b
    resource: repo://gradle.properties
  - id: openwiki-source-81d5f1627e19148569f46f81
    resource: repo://gradle/libs.versions.toml
  - id: openwiki-source-9ccd932b426a80129c387139
    resource: repo://gradle/wrapper/gradle-wrapper.properties
  - id: openwiki-source-12ad14ab31d394aff9897647
    resource: repo://projects/core/build.gradle.kts
  - id: openwiki-source-5534dec8e82d204d9eeedf4f
    resource: repo://projects/fabric/src/main/kotlin/site/siredvin/peripheralworks/data/FabricDataGenerators.kt
  - id: openwiki-source-9f94e9d408596b133a409713
    resource: repo://projects/forge/build.gradle.kts
  - id: openwiki-source-5442ce039f0641a20aa4b511
    resource: repo://projects/forge/src/main/kotlin/site/siredvin/peripheralworks/data/ForgeDataGenerators.kt
  - id: openwiki-source-96b2de50e0c14ebf72078c74
    resource: repo://projects/typed-peripheral-unlimitedperipheralworks/build.gradle.kts
  - id: openwiki-source-db09c9cd8d1216a654322e42
    resource: repo://projects/typed-peripheral-unlimitedperipheralworks/package.json
  - id: openwiki-source-4438469adfcfe67ff731b458
    resource: repo://projects/typed-peripheral-unlimitedperipheralworks/tsconfig.json
  - id: openwiki-source-e620d7484b72a53c7fa812cd
    resource: repo://settings.gradle.kts
generated: { by: "hermes", at: "2026-10-09T21:19:27.304Z" }
---

# Developer setup and build pipeline

## Checkout baseline

Use Java 17 and the checked-in Gradle wrapper. The wrapper selects Gradle 8.14; `gradle.properties` sets Minecraft 1.20.1, mod version 1.9.2, official Kotlin style, a 4 GiB Gradle heap and daemon disabled. The root shaking configuration and catalog select Kotlin 2.0.0. These are repository settings, not claims about the latest upstream releases.

The catalog selects CC:Tweaked 1.116.1, Fabric Loader 0.17.2, Fabric API 0.92.6+1.20.1 and Forge 47.3.39. Testiarium is 0.1.1. Broccolium 1.4.5, Tweakium 1.4.10 and Peripheralium 1.4.4 are the shared library baseline. AE2 and other optional dependency versions are loader-specific. Preserve this version family when diagnosing compile/link failures.

Local Gradle Node tasks download Node 22.14.0 into `.gradle/nodejs`, use `.gradle/npm` for npm tooling and install lockfiles with npm ci. A separate manual Node installation is not the declared prerequisite for those tasks. Server/client GameTests require Xvfb via `xvfb-run` on headless Linux; GNU `timeout` is used by the project commands.

## Module/build boundaries

`settings.gradle.kts` includes five modules and maps them to `projects/<name>`:

- `core`: shared Minecraft/peripheral implementation, resources and testMod. Uses the external vanilla/publishing plugins and both shared access-widener files.
- `fabric`: Fabric API adapters, entrypoints and metadata; configures common core, access widener and remapped publication.
- `forge`: Forge capabilities, lifecycle and metadata; configures common core, access transformers, mixins and JarJar publication.
- `typed-peripheral-unlimitedperipheralworks`: TypeScript source API compiled into declarations/Lua.
- `typescript-tests`: compiled Lua computer fixtures consumed by core test resource processing.

Root `site.siredvin.root`/release plugins and module shaking plugins provide significant task setup externally. Distinguish declarations here from those plugins' implementation. Some build-plugin/catalog selectors use dynamic versions; a checkout baseline does not prove every remote resolution is immutable.

## Dependency resolution

Plugin management uses SirEdvin's Minecraft Maven proxy, with Gradle Plugin Portal restricted to the Node plugin group and a Mixin plugin-to-module resolution rule. Core and loader repositories include mavenLocal and the Maven proxy; Forge also adds a narrowly filtered CurseMaven module for Extra HNN.

This means a warm developer Maven-local/cache environment can mask repository resolution problems. Inspect both settings plugin repositories and project repositories when diagnosing cold-machine setup. Do not infer missing artifacts from a plugin failure without locating which resolution phase failed. No credentials are required by any hard-coded repository declaration shown here, but network reachability and remote artifacts remain operational prerequisites.

## Timed build workflow

From the root, preserve complete output quietly:

```sh
mkdir -p build
LOG="build/gradle-$(date +%Y%m%d-%H%M%S).log"
timeout --foreground 10m ./gradlew build --no-daemon >"$LOG" 2>&1
```

Record exit code, elapsed duration and log path; inspect the relevant error window. Increase timeout only when required. Use the actual root `gameTest` task for both loader server tests and the distinct `clientGameTest` task for client coverage; a successful build alone is not computer/runtime/UI verification. Commands and matrix properties are in [testing](../testing/gametests-and-fixtures.md).

## Build side effects to review first

Forge has Copy tasks for Powah, Additional Lanterns, Automobility and KubeJS which copy Fabric integration sources into Forge main sources. Forge `compileKotlin`, Spotless Java and Spotless Kotlin tasks depend on these copies. `copyCreate` exists but is not among those dependency lists or `fullCopy`. A build or formatting check may therefore update tracked Forge source files; do not silently accept unrelated changes.

The typed module's compile task emits Lua into its project directory; its TypeScript config enables declaration generation, Lua 5.1, require-minimal libraries and library mode. Assemble depends on compilation. Its clean task deletes matching Lua files outside node_modules, including files in the working source tree. Do not treat broad clean/build invocations as pure read-only validation.

For documentation-only tasks that forbid application-source changes, validate documentation directly rather than invoking source-copy/generation tasks unnecessarily. Before and after application verification, inspect `git status` and explain any generated source delta.

## Data and API authoring

Fabric datagen and Forge GatherData delegate to shared `ModDataProviders` through loader generator sinks. Author resources through those shared providers and hooks, not direct edits to generated loader resources. Optional AE2 data hooks are part of early integration registration; recipes use loader conditions and optional loot packs.

For public API changes, edit TypeScript sources under `projects/typed-peripheral-unlimitedperipheralworks`, regenerate through `compileTypeScript` and inspect resulting declarations/Lua. The package's npm test script is a placeholder that exits failure; it is not the real peripheral regression suite. Use the fixture compiler and GameTests instead. Do not run the package's broad clean as a substitute for understanding generated output ownership.

Keep build output, logs, run worlds, EULA files, node_modules and temporary screenshots out of commits. The ignore rules cover `.gradle`, build, node_modules and run directories; they are not permission to modify unrelated user work.

Continue with [quickstart/task routing](../quickstart.md), [test pipeline](../testing/gametests-and-fixtures.md), and [configuration/release operations](../operations/configuration-and-release.md).
