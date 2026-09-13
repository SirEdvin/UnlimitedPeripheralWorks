# Verification

Implementation targets Minecraft 1.20.1 / Forge 47, CC:Tweaked 1.116.1, HNN 5.3.3, Extra HNN 1.2.2 (CurseForge file 8004392), and Placebo 8.6.3. Fabric has no HNN/Extra HNN dependencies. Published artifact metadata and mapped APIs were inspected before implementation.

## Original local runs (before guard removal)

All commands ran from the repository root, with complete output redirected to the log below. Every listed run exited **0**. The `build/` logs and reports are intentionally not committed.

| Verification | Command | Duration | Log |
|---|---|---|---|
| HNN + Extra HNN | `timeout --foreground 20m xvfb-run -a ./gradlew :core:spotlessApply :forge:spotlessApply :forge:runGameTestServer --no-daemon -PminimalTestEnvironment -PneuralTestEnvironment=extra -PtestiariumTags=neural` | 64s | `build/hnn-final-extra-20260913-110233.log` |
| HNN only | `timeout --foreground 20m xvfb-run -a ./gradlew :forge:runGameTestServer --no-daemon -PminimalTestEnvironment -PneuralTestEnvironment=hnn -PtestiariumTags=neural` | 55s | `build/hnn-final-only-20260913-110355.log` |
| Both installed, integrations disabled before startup | `timeout --foreground 20m xvfb-run -a ./gradlew :forge:runGameTestServer --no-daemon -PminimalTestEnvironment -PneuralTestEnvironment=extra -PneuralTestDisabled -PtestiariumTags=neural-disabled` | 51s | `build/hnn-final-disabled-20260913-110451.log` |
| Both loaders, neural mods absent | `timeout --foreground 20m xvfb-run -a ./gradlew gameTest --no-daemon -PminimalTestEnvironment` | 98s | `build/hnn-final-regression-20260913-110542.log` |
| Full multi-loader build | `timeout --foreground 10m xvfb-run -a ./gradlew build --no-daemon` | 48s | `build/hnn-final-build-20260913-110720.log` |
| TypeScript definitions and Lua fixtures | `timeout --foreground 5m ./gradlew :typescript-tests:compileTestLua --no-daemon` | 10s | `build/hnn-final-typescript-20260913-110835.log` |

`git diff --check` also passed. Test servers terminate through the Gradle runs; no persistent development server was launched.

## Executed tests and coverage

All report counts below have zero failures and zero skipped tests.

- HNN + Extra HNN: **8 tests** (`build/hnn-extra-gametest.xml`):
  - `NeuralNetworksGameTests.itemDetails`
  - `NeuralNetworksGameTests.luaAndProduction`
  - `NeuralNetworksGameTests.reloadedModels`
  - `ExtraNeuralNetworksGameTests.combinedDetailsAndMalformedNames`
  - `ExtraNeuralNetworksGameTests.ultimateV1`
  - `ExtraNeuralNetworksGameTests.ultimateV2`
  - `ExtraNeuralNetworksGameTests.ultimateV3`
  - `ExtraNeuralNetworksGameTests.ultimateV4`
- HNN-only: the **3 HNN tests** above (`build/hnn-only-gametest.xml`).
- Disabled integration startup: **2 tests**, each integration's `disabledIntegration` (`build/neural-disabled-gametest.xml`).
- Absent neural dependencies: **55 Forge** and **54 Fabric** existing regression tests (`build/hnn-forge-regression.xml`, `build/hnn-fabric-regression.xml`).

The Lua fixtures exercise all selection methods, index validation, nil/null and omitted-argument clearing, entity validation, item details and read-only NBT snapshots. Native assertions cover per-model/per-machine isolation, subtype aliasing, save/load, synchronization tag contents, changes during processing, actual selected output and ordinary energy/prediction consumption. Synchronization was verified through native state and update-tag content, not a visual GUI screenshot.

Native reload tests cover changed/removed/empty/duplicate drop entries, subtype ambiguity, primary precedence, stale indices and custom progression/cost data. HNN rejects duplicate primary entity models at registry load; the ambiguity fixture therefore uses overlapping subtypes with distinct primary entities rather than weakening native validation.

Detail tests cover every ordinary and Extra HNN tier, four ordered combined constituents with duplicates, maximum-tier null semantics, malformed and unresolved models and provider coexistence. The user subsequently requested removal of the name guard. Current malformed-name fixtures use custom names to reach enrichment; an explicit regression verifies the native empty-constituent exception remains unhandled. No production name interception remains.

## Packaging

Inspected `projects/forge/build/libs/peripheralworks-forge-1.20.1-1.9.0.jar`: contains both integration entrypoints. The corresponding Fabric JAR contains neither Forge integration entrypoint. Dependencies remain external/optional rather than bundled integration mods.

## Development failures resolved

- Added a module-restricted CurseMaven repository because the existing proxy did not resolve the Extra HNN artifact.
- Corrected Kotlin warnings/formatting and test fixture placement to the existing structure lookup pattern.
- Corrected native tier-data fixtures to the four serialized thresholds (the initial zero is implicit).
- Updated the existing exhaustive configuration-discovery regression for the two new definitions.
- Prepared disabled configuration in the exact Forge run directory rather than the task's pre-launch working directory.

These earlier failures are not counted as passing runs. Final runs above validate the corrected implementation.

## Verification after removing the guard

The name guard, mixin, registration and native exception handling were removed at the user's request. Provider validation remains unchanged. The native empty-constituent display-name exception is explicitly asserted through the real detail registry; custom-named malformed fixtures then exercise enrichment rejection without changing production name behavior.

All commands below exited 0; XML reports record zero failures and skips.

| Command | Duration | Log | Result |
|---|---|---|---|
| `timeout --foreground 20m xvfb-run -a ./gradlew :core:spotlessApply :forge:spotlessApply :typescript-tests:compileTestLua :forge:runGameTestServer --no-daemon -PminimalTestEnvironment -PneuralTestEnvironment=extra -PtestiariumTags=neural` | 85s | `build/hnn-no-guard-extra-20260913-112445.log` | 8 tests |
| `timeout --foreground 20m xvfb-run -a ./gradlew :forge:runGameTestServer --no-daemon -PminimalTestEnvironment -PneuralTestEnvironment=hnn -PtestiariumTags=neural` | 59s | `build/hnn-no-guard-only-20260913-112628.log` | 3 tests |
| `timeout --foreground 20m xvfb-run -a ./gradlew :forge:runGameTestServer --no-daemon -PminimalTestEnvironment -PneuralTestEnvironment=extra -PneuralTestDisabled -PtestiariumTags=neural-disabled` | 51s | `build/hnn-no-guard-disabled-20260913-112727.log` | 2 tests |
| `timeout --foreground 20m xvfb-run -a ./gradlew gameTest --no-daemon -PminimalTestEnvironment` | 96s | `build/hnn-no-guard-regression-20260913-112818.log` | 55 Forge / 54 Fabric tests |
| `timeout --foreground 10m xvfb-run -a ./gradlew build --no-daemon` | 47s | `build/hnn-no-guard-build-20260913-112955.log` | Passed |
| `timeout --foreground 10m ./gradlew :core:clean :fabric:clean :forge:clean build --no-daemon` | 74s | `build/hnn-no-guard-clean-build-20260913-113127.log` | Passed |

A packaging inspection found stale, unregistered guard classes in the incremental Fabric artifact. The clean multi-loader rebuild removed stale outputs; inspection verified neither final JAR contains those classes or their mixin registration. Saved post-removal reports are `build/hnn-no-guard-{extra,only,disabled,regression,fabric}.xml`.
