# Minecraft 1.21.1 port verification

## Merge scope

- Base: `1.21` at `bfc8ec580f117760334424452194bb744c273076`.
- Merged source: `1.20` at `4bb44757e19c84a3d7dc6c87f7fe0533d27c4e0c`.
- Preserve a real two-parent merge rather than cherry-picking the incoming feature.
- Incoming behavior: HNN/Extra HNN fabricator selection, ordinary/combined model item details, configuration, TypeScript definitions and executable regression fixtures; version becomes 1.9.1.
- Retain the target's Minecraft 1.21.1, Java 21, Fabric/NeoForge toolchain, integration versions, existing source exclusions and target-specific changelog entries.

## Version-specific adaptations

Verified against HNN 6.5.1 (`ikdLP02G`), Extra HNN 2.2.5 (`8531861`) and Placebo 9.9.2 (`1Ypo4tf4`). Both neural mods remain optional; NeoForge metadata declares their minimum compatible versions.

- Use NeoForge configuration types and block capabilities.
- Read typed data components and validate bound holders, constituent counts and nonnegative progression without mutating stacks.
- Use native `DataModelInstance`, dynamic HNN model tiers, and `ExtraDataModelInstance`.
- Resolve entity models separately from HNN 6.5 block models. Preserve the entity-based API from 1.20 rather than misrepresent block identity. Block models and combined items containing them do not receive this extension.
- Delegate selection and clearing to native `setFixedDrop`; this replaces a queue with fixed selection for the requested model.
- Pass registry providers to native block-entity serialization and compare item components in tests.
- Reload authentic resource JSON; use native path-only tier keys and attunement rules for overlapping entity models.
- Keep the existing native-name failure boundary: no compatibility mixin or exception interception.

The public API and limitations are documented beside the TypeScript definitions in `projects/typed-peripheral-unlimitedperipheralworks/integrations/hostileNetworks.ts`.

## Local validation

All final commands exited 0. Durations are complete wrapper/process wall times. Full logs are local ignored build artifacts, relative to the repository root.

| Check | Command | Duration | Result | Log |
| --- | --- | --- | --- | --- |
| TypeScript fixtures | `timeout --foreground 5m ./gradlew :typescript-tests:compileTestLua --no-daemon` | 8 s | Passed | `build/port-typescript-20260913-140619.log` |
| Full multi-loader build | `timeout --foreground 10m ./gradlew build --no-daemon` | 15 s | Passed, including formatting checks | `build/port-final-build-20260913-140627.log` |
| Fabric + NeoForge regression, neural mods absent | `timeout --foreground 20m xvfb-run -a ./gradlew gameTest --no-daemon -PminimalTestEnvironment` | 59 s | 58 NeoForge + 32 Fabric passed, zero skipped | `build/port-gametests-20260913-140327.log` |
| HNN + Extra HNN | `timeout --foreground 20m xvfb-run -a ./gradlew :forge:runGameTestServer --no-daemon -PminimalTestEnvironment -PneuralTestEnvironment=extra -PtestiariumTags=neural` | 35 s | 8 passed, zero skipped | `build/port-neural-20260913-135933.log` |
| HNN without Extra HNN | `timeout --foreground 20m xvfb-run -a ./gradlew :forge:runGameTestServer --no-daemon -PminimalTestEnvironment -PneuralTestEnvironment=hnn -PtestiariumTags=neural` | 29 s | 3 passed, zero skipped | `build/port-neural-hnn-20260913-140031.log` |
| Both integrations disabled with mods installed | `timeout --foreground 20m xvfb-run -a ./gradlew :forge:runGameTestServer --no-daemon -PminimalTestEnvironment -PneuralTestEnvironment=extra -PneuralTestDisabled -PtestiariumTags=neural-disabled` | 28 s | 2 passed, zero skipped | `build/port-neural-disabled-20260913-140100.log` |

JUnit XML counts were checked against actual testcase entries for the HNN-only, disabled and both regression reports; the combined neural run reported eight passing tests. Neural coverage includes real typed Lua calls, all four ultimate fabricators, item details across native tiers, duplicate constituents, invalid input, unchanged stacks, native production/resource consumption, per-machine selections, persistence/update tags and registry reload/ambiguity/precedence.

Initial failures were corrected rather than skipped: old Forge configuration/imports, registry-holder accessors, generic test-helper access, nullable capability directions, formatting, and native path-only tier keys in reload fixtures. Fresh GameTest worlds log an initial missing `server.properties` file before generating defaults; all successful runs completed and shut down normally.

Inspected both final 1.9.1 JARs: neural integration classes are present only on NeoForge; neither contains bundled HNN/Extra HNN implementations or testmod/Testiarium classes. Packaged NeoForge metadata contains both optional minimum-version constraints. `git diff --check` passes.

No production client visual validation was performed: this port changes server-side peripheral integration and item-detail enrichment, not rendering. The repository's hosted PR workflow runs the core multi-loader regression; the explicit neural environment matrix above was executed locally.
