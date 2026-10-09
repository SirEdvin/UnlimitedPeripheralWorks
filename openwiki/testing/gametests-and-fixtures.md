---
type: testing
title: Cross-loader GameTests and Lua fixtures
description: Explains Testiarium registration, server/client runs, optional-mod matrices, TypeScript-to-Lua fixtures and diagnostic outputs.
tags: [testing, gametest, fixtures, typescript, loaders]
verified:
  - by: openwiki/0.7.1
    at: 2026-10-09T21:19:27.304Z
sources:
  - id: openwiki-source-ede6d87351d544e147a5ad29
    resource: repo://.github/workflows/gametest.yml
  - id: openwiki-source-2a9daaac1604f238ef4c63fb
    resource: repo://build.gradle.kts
  - id: openwiki-source-12ad14ab31d394aff9897647
    resource: repo://projects/core/build.gradle.kts
  - id: openwiki-source-355ddfc48b45d2417df899a6
    resource: repo://projects/fabric/build.gradle.kts
  - id: openwiki-source-9f94e9d408596b133a409713
    resource: repo://projects/forge/build.gradle.kts
  - id: openwiki-source-bfa794deadfcd83e54131947
    resource: repo://projects/typescript-tests/build.gradle.kts
  - id: openwiki-source-6c350f01cbd7e0fb9d9531f0
    resource: repo://projects/typescript-tests/build.mjs
  - id: openwiki-source-863acc614b19f09a08e7a315
    resource: repo://projects/typescript-tests/tsconfig.json
generated: { by: "hermes", at: "2026-10-09T21:19:27.304Z" }
---

# Cross-loader GameTests and Lua fixtures

## What actually exercises the API

The root `gameTest` task depends on Forge `runGameTestServer` and Fabric `runPeripheralWorksGameTest`. Root `clientGameTest` depends on both loader client GameTest tasks. These are Minecraft/Testiarium runs, not merely JVM unit tests.

Core `testMod` provides shared Kotlin tests, structure resources and computer fixtures; each loader adds test-mod metadata/entrypoints and its adapter-specific tests. Shared `src/ae2Test/kotlin` is added to loader test compilation; Forge `src/gtceuTest/kotlin` is added in a normal environment or when GTCEu testing is requested. Fabric test registration uses AE2 presence checks and client gating before reflectively registering those suites.

Two layers complement one another: Kotlin GameTests arrange/inspect Minecraft state and assert lifecycle contracts; `thenLua` fixtures exercise APIs on actual CC computers. For example the TypeScript network-manager fixture calls group, color, delimiter, range, lookup and removal methods, while Kotlin tests verify persistence and server behavior.

## Reproducible server run

From the Git root, use the project convention:

```sh
mkdir -p build
LOG="build/gametests-$(date +%Y%m%d-%H%M%S).log"
timeout --foreground 20m xvfb-run -a ./gradlew gameTest --no-daemon -PminimalTestEnvironment >"$LOG" 2>&1
```

Minimal mode excludes unrelated integrations; it is not an absence-of-all-mods environment. AE2 remains available by default. Property switches are presence-based where the Gradle file uses `isPresent`: supplying `-PminimalTestEnvironment=false` still enables minimal mode.

The default minimal tag selection is `peripheralworks,ae2,ae2-configurable-peripherals`; normal runs default to `peripheralworks`. Override with `-PtestiariumTags=...` for the suite you need. Match tags to registered suites and installed dependencies rather than interpreting an empty filtered run as coverage.

Both server configurations use UUID-based `build/gametest-runs/...` directories. Saved fixture computers reboot before tests replace them, so world isolation is part of correctness, not disposable optimization. Do not force unrelated reruns to share an old world.

## Optional dependency matrix

Use the appropriate loader task with explicit tags and the same timeout/log wrapper:

- No AE2: `-PminimalTestEnvironment -PtestWithoutAE2 -PtestiariumTags=peripheralworks`. Both loader builds require minimal mode for `testWithoutAE2`; AE2 remains compile-only but is not added as a runtime implementation in this mode.
- GTCEu on Forge: `:forge:runGameTestServer -PminimalTestEnvironment -PgtceuTestEnvironment -PtestiariumTags=gtceu`. This adds GTCEu/LDLib and GTCEu test sources.
- Hostile Networks on Forge: `:forge:runGameTestServer -PminimalTestEnvironment -PneuralTestEnvironment=hnn -PtestiariumTags=neural`.
- Extra HNN on Forge: select `-PneuralTestEnvironment=extra` and tags from `ExtraNeuralNetworksGameTests`.
- Disabled neural behavior: `-PneuralTestEnvironment=hnn -PneuralTestDisabled -PtestiariumTags=neural-disabled`; the Forge task writes disabled integration sections into the fresh run config before startup. Include minimal mode for a focused environment.

`neuralTestEnvironment` accepts only `none`, `hnn` or `extra`. Invalid values fail configuration. The class-level and method-level test tags are source contracts; inspect them before combining filters.

## Client runs

For shared configurator/network-manager UI coverage, run `clientGameTest --no-daemon -PminimalTestEnvironment` under the same 20-minute `xvfb-run` wrapper. Defaults select `network-manager-client`. Loader configs enable Testiarium client mode and write client reports/screenshots. Other client suites, including AE2 pattern rendering, require explicit relevant tags.

`NetworkManagerClientGameTests` exercises detached target history/favorites, synchronized selection, name length limits, and attached-mode screen precedence. It uses real UI widgets and waits for server-side state rather than treating a screenshot alone as proof. Interactive fixture clients are separately configured as Forge `runTestClient` and Fabric `runPeripheralWorksTestClient`; stop them after inspection.

## TypeScript fixture pipeline

`:typescript-tests:compileTestLua` first builds the local typed-peripheral package, then installs fixture dependencies with npm ci. `build.mjs` recreates `build/generated/test-lua` and a Soteria source copy, invoking TypeScriptToLua once per declared fixture entrypoint. Nonzero compiler status exits the build. `tsconfig.json` targets Lua 5.1 with inline library imports and leaves `cc.expect` unresolved for runtime availability.

Core test resource processing depends on fixture compilation and copies generated Lua under `computer/tests`. Edit `projects/typescript-tests/src`, not copied resources or build output. After fixture edits, run:

```sh
mkdir -p build
LOG="build/typescript-tests-$(date +%Y%m%d-%H%M%S).log"
timeout --foreground 5m ./gradlew :typescript-tests:compileTestLua --no-daemon >"$LOG" 2>&1
```

Compilation proves fixture generation, not execution; follow it with the relevant GameTest suite.

## Focused regression routes

- `PeripheralWorksGameTests`: optional AE2 resources, constrained storage replacement/persistence, installed config discovery, configurator and network-manager invariants.
- `AE2GameTests`: subscription bounds/corruption/attachment, real transfers, disconnected calls, P2P cleanup.
- `AE2CraftingJobsGameTests`: deterministic task handoffs, action-source reacquisition, lost/relinked access, exceptions and termination; its harness leaves production scheduling untouched.
- AE2 configurable-object/wireless/pattern suites: computer APIs and corresponding Minecraft state.
- Forge neural suites: disabled integration, model details, registry reloads and real Lua/production behavior.
- Forge GTCEu suites: capability methods and grouped machine information.

## Reports and CI limits

Server XML reports are `projects/{forge,fabric}/build/test-results/peripheralworks-gametest.xml`. Client reports use `network-manager-client-gametest.xml`, with screenshots under each loader's `build/screenshots/network-manager-client`.

The pull-request GameTests workflow sets Java 17, invokes the root minimal server test under Xvfb with a 1200-second timeout and uploads XML/HTML report paths even after failure. It does not run the root client task or explicitly select neural/GTCEu matrices. Green default PR CI therefore does not establish every integration/client path.

Record command, exit code, duration, full log path and relevant error window. This wiki generation researched test source; it is not a claim that the application test suites were executed. See [build hazards](../workflows/setup-and-build.md), [integration lifecycle](../integrations/optional-mods.md) and [network workflow](../workflows/network-manager-and-configurator.md).
