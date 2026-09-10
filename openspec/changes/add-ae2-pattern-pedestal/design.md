## Context

See proposal.md for motivation and specs/ae2-pattern-pedestal/spec.md for the public contract.

The repository targets Minecraft 1.20.1, Kotlin 2.0, CC:Tweaked 1.116.1, AE2 Fabric 15.0.7-beta and Forge 15.2.13. Read-only `javap` inspection of both installed AE2 artifacts confirms all four `PatternDetailsHelper` encoders and native pattern classes exist on both loaders. Signatures alone do not establish validation behavior: implementation must trace the corresponding AE2 pattern terminal encoding and decode paths before connecting Lua inputs.

Existing reuse points:

- `AbstractItemPedestal` provides insertion/removal/drop interactions; `AbstractItemPedestalBlockEntity` provides saved inventory and client synchronization.
- `PedestalInventoryPlugin` supplies ordinary Lua inventory methods.
- `FabricCustomSlottedStorage` and `ForgeCustomSlottedStorage` implement native storage and persistence. Their capacity is stack-size times scale, and neither receives the declared pedestal item filter. Reusing the current constructor unchanged would violate both the one-item limit and insertion filtering.
- `AE2Helper.patternToMap` provides decoded input alternatives and outputs; `parseResource` validates registry IDs and amounts but does not parse tags. Do not silently extend its existing callers' public contracts.
- AE2 `Registration` has optional block/entity, recipe, model, loot, and language hooks. `PeripheralWorksClientCore` has renderer registration but its renderer selection currently enumerates known types explicitly.

## Goals / Non-Goals

**Goals:** One stored item is the source of truth; no persisted second copy of editable pattern data. Use native AE2 encoding and validation, keep shared behavior in `core/src/ae2Integration`, and preserve existing peripheral contracts.

**Non-Goals:** No ME node, recipe-search API, remote inventory samples, custom GUI, bulk encoding, custom recipe engine, addon-pattern support, arbitrary item creation, or silent in-place editing. Encoding produces pattern metadata, not its described ingredients or outputs.

## Decisions

### A small extension of pedestal storage, not an independent inventory system

Add optional acceptance/capacity parameters to the existing platform storage factory and adapters with defaults preserving every existing caller. The pattern pedestal requests an absolute capacity of one and a native-pattern predicate; apply them to the underlying native storage, not just a Lua wrapper, so automation cannot bypass them. Inspect transaction simulation and replacement hooks in installed storage APIs before implementing an internal single-slot replacement. Use existing transaction/set-slot primitives; do not implement replace as an unchecked extract-then-insert sequence.

Reusing `holdingStacks = 1` was rejected: it allows an entire blank-pattern stack. Globally switching all existing pedestals to their currently unused `itemFilter` was also rejected as unrelated behavior change. Shared support is appropriate, but the new pedestal explicitly opts in.

Keep AE2 item recognition in the optional integration. Native blank and encoded item identity governs insertion and clearing; decode success is not required, allowing recovery after recipe removal. Addon patterns are rejected rather than guessing how many blanks they represent.

### Native encoding with a single guarded commit

All pattern methods execute on the server thread. Each encoder checks the current stack is exactly one native blank, parses and constructs a candidate without changing storage, then replaces that same blank through a non-yielding guarded commit. Recheck state immediately before replacement if any helper can defer work. Notify persistence and clients using the existing storage change path. No lock manager or job queue is needed.

For processing, call `PatternDetailsHelper.encodeProcessingPattern` with the supplied ordered GenericStacks. Enforce only well-formed descriptors, positive safe amounts, native format limits, and successful native construction/decoding. Do not look up recipes, check machines, compare input/output value, or enforce conservation.

For crafting/stonecutting/smithing, trace and reuse the installed AE2 terminal's recipe matching, native assembly and `PatternDetailsHelper` encoding/decode flow. Resolve the explicit `recipeId` against the current world's recipe manager; derive output using the supplied concrete input stacks. Avoid copying AE2 validity logic. Helper encoding alone must not be assumed to validate recipes. Decode the candidate with native validation enabled before committing, and reject a null/invalid result. Use AE2's exact substitution and remainder semantics. Do not fake a recipe or trust a Lua output to satisfy an encoder signature.

An explicit recipe ID avoids ambiguous selection (especially stonecutting). Looking up recipes by output or adding a recipe browser is unnecessary scope.

### Lua contract

The new primary type is `peripheralworks:ae2_pattern_pedestal`, using the repository's owned-peripheral naming convention. Reuse the pedestal inventory plugin; no `ae2_network_access` type is appropriate for a standalone editor.

Methods:

| Method | Argument | Success |
| --- | --- | --- |
| `getPattern()` | none | state snapshot |
| `clearPattern()` | none | true if cleared, false if already blank |
| `encodeProcessingPattern(definition)` | ordered inputs and outputs | true |
| `encodeCraftingPattern(definition)` | recipeId, sparse grid, substitution flags | true |
| `encodeStonecuttingPattern(definition)` | recipeId, input, substitution flag | true |
| `encodeSmithingTablePattern(definition)` | recipeId, template/base/addition, substitution flag | true |

The user confirmed `encodeProcessingPattern` (not the earlier `encodeProcessingPatttern` spelling). See the spec for precise argument shapes and errors. Use `Fallible` in TypeScript for operational failures; malformed argument tables throw `LuaException`.

Examples of intended Lua calls (documentation only):

```lua
pedestal.encodeProcessingPattern({
  inputs = {{type = "item", name = "minecraft:cobblestone", count = 1}},
  outputs = {{type = "item", name = "minecraft:diamond", count = 1}},
}) -- Deliberately legal: processing patterns are descriptions, not executable recipes.

pedestal.encodeCraftingPattern({
  recipeId = "minecraft:crafting_table",
  grid = {
    [1] = {type = "item", name = "minecraft:oak_planks", count = 1},
    [2] = {type = "item", name = "minecraft:oak_planks", count = 1},
    [4] = {type = "item", name = "minecraft:oak_planks", count = 1},
    [5] = {type = "item", name = "minecraft:oak_planks", count = 1},
  },
  substitute = false,
  substituteFluids = false,
}) -- Output is native recipe-derived, never supplied by Lua.
```

### Exact variants and inspection

Planning interpretation of the user's follow-up: NBT-sensitive variants are supported, with permissive processing semantics and native validation for recipe-backed patterns. Choose optional compound `snbt` on resource descriptors rather than inventory references: it keeps the API self-contained, permits arbitrary processing descriptions, and introduces no inventory lookup dependency. This is a design choice, not a claim the user explicitly selected an SNBT option.

Parse via the native SNBT parser; use an existing depth-limited reader if available, otherwise add a bounded precheck before invoking a recursive parser. Bound UTF-8 size and nesting as specified, accounting for quoted strings and escapes. Tags apply to resources only, never the enclosing pattern item. Reject unknown descriptor/definition fields so `nbt` hashes or user-supplied crafting outputs cannot silently disappear. Reuse registry/amount conversion where possible without changing other AE2 APIs. Reject unrepresentable amounts instead of truncating, including native-to-Lua conversions during inspection.

`getPattern()` returns an envelope, not a changed global AE2 pattern shape. Reuse `AE2Helper.patternToMap` for the existing human-readable summary. Add `definition` with concrete original slots and optional SNBT separately: decoded alternatives alone cannot reconstruct a crafting grid. Use native sparse input accessors and recipe/flag information from the native pattern representation. Trace the native encoding serialization where an accessor is absent; do not use aggregated `inputs` to reconstruct geometry or substitute variants. SNBT serialization need only preserve equivalent tags, not original text formatting.

For an invalid pattern return item details, native type and error, without promising a reconstructable definition. Oversized or unrepresentable externally authored data must report a descriptive inspection error rather than silently corrupt values. Clearing remains possible regardless of decoding success.

### Registration and presentation

Use AE2 optional `Registration` and loader setup patterns. Keep any client rendering hook behind client initialization; extending only the extra-renderer list is insufficient because current renderer selection has an explicit fallback error. Reuse `PedestalTileRenderer`, pedestal geometry and existing texture assets initially, with AE2-themed material selection and a distinct display name. No bespoke animation pipeline.

Proposed low-stakes recipe default: one existing item pedestal plus one AE2 blank pattern yields one pattern pedestal, using an AE2-conditioned recipe. Register loot, models, creative availability and English/Ukrainian descriptions using current provider hooks. The recipe consumes its blank as a component; it does not prefill the inventory.

Do not gate this standalone device behind network-only `enableMEInterface`. Use integration availability and the existing overall enablement conventions; no speculative new tuning settings.

## Risks / Trade-offs

- Native encoder signatures match but validation details can differ across AE2 versions → inspect both native terminal paths, verify positive and negative encodings through real native decoding and multi-loader GameTests.
- Shared inventory changes can affect ordinary pedestals → preserve defaults and add regression checks for their existing stack capacity and transfer behavior.
- Lua/SNBT parsing is a trust boundary even for arbitrary processing recipes → bound size/depth, reject malformed types and unsafe numbers, never replace storage before construction succeeds.
- Malformed saved data can bypass insertion checks → guard read/mutation paths against unsupported or over-count stacks, preserve recoverable data and allow ordinary extraction; never silently truncate contents on load.
- Native encoding metadata may change between versions → isolate any unavoidable serialization access inside the AE2 implementation and test round-trip definitions on each loader.
- Clearing discards tags intentionally → document it as destructive and require an explicit call; never clear automatically on an encoding failure.

## Migration Plan

Planning only in this change preparation. Before implementation, create a feature branch from the intended `1.20` base, retaining these artifacts. Add the new block and optional storage parameters without rewriting existing saves. Generate resources from providers, compile typed fixtures, run both loaders' GameTests and the full build, then commit/push the feature branch and open a PR targeting `1.20` when delivery is authorized. Do not merge or push feature work directly to the base.

There is no automatic downgrade migration for a newly introduced block. Before removing the feature from an existing world, extract patterns and remove its pedestals or restore a backup; do not promise older versions can preserve unknown block entities.

## Implementation evidence and remaining verification

The installed Fabric 15.0.7-beta and Forge 15.2.13 bytecode was traced using `javap -c -p -constants`, including `PatternEncodingTermMenu`, `PatternDetailsHelper`, all four native pattern implementations and their encoding serializers. Both versions expose 81 processing input slots and 27 output slots. Processing stores ordered sparse `GenericStack` arrays with long amounts; crafting stores nine concrete item slots. Native serializers use `in`, `out`, `recipe`, `substitute` and (crafting only) `substituteFluids`; crafting's package-private serializer requires reading `recipe` from a copied native pattern tag because `AECraftingPattern` has no public recipe-ID accessor.

The terminal builds processing patterns directly from its configured resources without machine/recipe checks. Its crafting path derives the output from the current recipe; smithing assembles a native three-slot container; stonecutting resolves the selected recipe ID and its native result. The pedestal uses explicit recipe lookup, native `matches`/`assemble`, the same four `PatternDetailsHelper` encoders, and native decoding before replacement. The decoder's boolean means **auto-recovery**, not validation: `false` still validates native patterns and avoids repair writes during inspection. No custom recipe-validation implementation was added.

The existing Fabric item-storage wrapper mutates the source stack passed to `store`. Manual pedestal insertion now passes a copy so the original hand remains available for transfer comparison and synchronization. Native adapters retain unrestricted defaults, add opt-in capacity/filtering, and use native transactional/direct slot replacement with an expected-stack check. Simulation, failed replacement, ordinary capacity and one-item limits pass on both loaders.

Verified commands (all complete logs are local, untracked):
- `timeout --foreground 10m ./gradlew :fabric:compileKotlin :forge:compileKotlin --no-daemon -PminimalTestEnvironment`: exit 0, 15 seconds, `build/pattern-compile-20260905-194030.log`.
- `timeout --foreground 5m ./gradlew :typescript-tests:compileTestLua --no-daemon`: exit 0, 36 seconds, `build/pattern-typescript-20260905-194601.log`.
- `timeout --foreground 10m xvfb-run -a ./gradlew :fabric:runData :forge:runData --no-daemon -PminimalTestEnvironment`: exit 0, 44 seconds, `build/pattern-datagen-retry-20260905-200013.log`. The preceding full-environment invocation hit the tool's 420-second limit; no successful exit is claimed for it. Unrelated generated deletions and cache churn from the minimal run were restored.
- `timeout --foreground 20m xvfb-run -a ./gradlew :typescript-tests:compileTestLua gameTest --no-daemon -PminimalTestEnvironment`: exit 0, 84 seconds, `build/pattern-expanded-tests-20260905-200457.log`; 47 tests passed on each loader, including real Lua calls, round trips, competing encoders, malformed arguments and storage/persistence regressions.
- After adding native recipe/limit coverage, root `gameTest` exited 1 in 64 seconds (`build/pattern-native-tests-20260905-201019.log`): Fabric passed 48/48; Forge passed the new pedestal tests but failed existing `AE2ConfigurableObjectsGameTests.exportBus` with `Export Bus side configuration is wrong`.
- A root retry exited 1 in 31 seconds (`build/pattern-native-tests-retry-20260905-201213.log`): Fabric passed the new pedestal tests but failed existing `AE2ConfigurableObjectsGameTests.storageLevelEmitter` with `emitter upgrade did not transfer`; Forge did not rerun after this task failure. These changing failures were unresolved at that checkpoint; the investigation and fix are recorded below.

### Completed verification after the investigation

The user authorized investigation of the existing AE2 failures. Diagnostics showed that a supposedly failed upgrade transfer had already removed the source and populated the destination. Both suites passed after preserving their saved worlds and starting fresh. Saved powered fixture computers were rebooting before replacement and rerunning scripts against new fixtures. Both server run configurations now use unique working directories under their module's `build/gametest-runs/`, removed by ordinary build cleanup; old test worlds were preserved under the root `build/gametest-world-backups/`. No inventory retry or relaxed assertion was introduced.

Client tests exposed late Forge renderer registration and a missing `smooth_quartz` texture. Client initialization is now idempotent and runs before Forge registers renderers; the provider uses the actual vanilla smooth-quartz texture, `minecraft:block/quartz_block_bottom`. Tests assert renderer/model/texture availability, synchronized item data, and screenshot file creation. Both screenshots were visually inspected. Large integral fluid amounts now divide in integer space before converting to doubles, avoiding lost millibuckets near Lua's exact-integer limit; a native regression covers the maximum safe count.

Final evidence:
- `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 23 seconds; `build/pattern-verified-build-20260905-210837.log`.
- `timeout --foreground 20m xvfb-run -a ./gradlew gameTest --no-daemon -PminimalTestEnvironment`: exit 0, 97 seconds; `build/pattern-verified-server-20260905-210900.log`. JUnit reports confirm **49 tests passed on each loader**, no failures or skips, including the prior export-bus/emitter failures and the precision regression.
- `timeout --foreground 10m xvfb-run -a ./gradlew :fabric:runPeripheralWorksClientGameTest :forge:runClientGameTest --no-daemon -PminimalTestEnvironment -PtestiariumTags=ae2-pattern-client`: exit 0, 102 seconds; `build/pattern-final-client-20260905-205939.log`. One client test passed per loader; screenshots are untracked at each module's `build/screenshots/network-manager-client/screenshots/ae2-pattern-pedestal.png`.
- From `projects/typed-peripheral-unlimitedperipheralworks`, `timeout --foreground 3m npm run build -- --outDir ../../build/typed-pattern`: exit 0, 1 second; `build/pattern-typed-build-20260905-204924.log`.
- AE2-absent startup: 29 common tests passed on each loader. Forge's successful run is in `build/pattern-no-ae2-final-20260905-205225.log` (root invocation exit 0, 85 seconds); Fabric's corrected runtime-classpath exclusion run is in `build/pattern-fabric-no-ae2-20260905-205551.log` (exit 0, 29 seconds). Both explicitly report `AE2=false, pedestal=false`; the earlier Fabric run still loaded AE2 and is not counted as absence evidence.
- Both minimal-environment datagen tasks passed (exit 0, 34 seconds; `build/pattern-final-datagen-20260905-204850.log`). A subsequent full-environment generation produced the committed resources and restored all other integrations' translations, but its process did not terminate after all providers completed and hit the 360-second tool timeout (`build/pattern-full-datagen-20260905-210135.log`). This shutdown limitation is disclosed, not represented as a successful full-environment task exit. Generated cache churn was restored, and generated resources were not hand-edited.

The local absence-check init script excludes `maven.modrinth:ae2` from runtime configurations before evaluation. Loom also exposes its remapped AE2 JAR through the run task's internal classpath, so the Fabric run task classpath is filtered during task configuration, before it becomes final. The script was passed with `-I /tmp/upw-no-ae2.init.gradle` and `-PtestiariumTags=peripheralworks`; production dependency declarations were not changed.

Implementation was committed and pushed on `feat/ae2-pattern-pedestal`. Review PR: https://github.com/SirEdvin/UnlimitedPeripheralWorks/pull/100, targeting `1.20`; the remote commit identity and PR head/base were read back and verified. The PR remains open, not merged.
