## Context

See proposal.md for motivation. The project targets Minecraft 1.20.1, Java 17, CC:Tweaked 1.116.1, Fabric and Forge. Existing Forge integrations register plugins through `ComputerCraftProxy.addProvider`; ProjectE's `Integration.kt` demonstrates `VanillaDetailRegistries.ITEM_STACK.addProvider`. Public APIs live in `projects/typed-peripheral-unlimitedperipheralworks`, not just the fixture project.

Upstream source inspected during planning:
- https://github.com/Shadows-of-Fire/Hostile-Neural-Networks/tree/1.20 : `LootFabTileEntity`, `DataModel`. Native selections are per data-model holder, zero-based, with -1 for unset; `setSelection` synchronizes and marks dirty but clamps inputs. Data models include primary and subtype entities and ordered `fabDrops`.
- https://github.com/lentel27/Extra-Hostile-Neural-Networks/tree/1.20.1 : branch properties identify Forge 47 / Minecraft 1.20.1 and mod version 1.2.2. `UltimateLootFabTileEntity` is a separate class (not a subclass of the HNN fabricator), has V1–V4 configuration, and exposes analogous selection methods. It still processes ordinary HNN predictions.
- Extra HNN `ExtraDataModelItem` stores ordered constituent IDs plus shared data/iteration counters. `ExtraCachedModel` derives its own native tier and sums constituent simulation costs. Its validity check alone does not reject an empty list. Do not mistake the repository's current 1.21.1 master branch for this target.

These are source observations, not runtime verification or release-artifact pins. Implementation must resolve compatible published artifacts and inspect the exact pinned sources before coding.

## Goals / Non-Goals

Goals: native state ownership, defensive entity resolution, minimal adapters, identical Lua selection semantics across machines, and faithful combined-model details.

Non-goals: Fabric ports of these Forge mods, simulation-chamber control, creating/upgrading models, editing NBT, changing recipes or native throughput, prediction/deep-learner detail support, or a generic machine abstraction framework.

## Decisions

### Optional Forge boundaries
Create integration packages under `projects/forge/src/main/kotlin/site/siredvin/peripheralworks/integrations/hostilenetworks` and `extrahnn`, following existing startup/config discovery. Keep optional API types out of always-loaded configuration definitions and shared core. HNN and Extra HNN registration gates are separate; disabling Extra HNN must not disable ordinary HNN support. Extra HNN registration requires both mods. Reuse existing integration enablement conventions rather than extending config schema during startup. Pin HNN, Extra HNN and required Placebo artifacts in the catalog and Forge compile/runtime/test dependency sets, never Fabric. Alternative rejected: unconditional shared-core imports, which break absent-mod/Fabric loading.

### Small shared selection logic with two native adapters
Use one Forge-side HNN-aware resolver/validation implementation and small block-specific adapters or callbacks for native read/write operations. Extra HNN adapters must remain in the Extra HNN package. Do not duplicate public argument/index logic, depend on inheritance that does not exist, use reflection, or rewrite the machine's saved NBT. Execute registry/world-sensitive calls on the server thread using existing plugin conventions. Validate before native `setSelection`; translate 1-based indices to zero-based and nil to -1. Invoke the native synchronization path, preserving native processing/reset behavior. Test changing selection during operation: the Extra HNN implementation has additional output-check state, so verify effective production rather than assuming a changed map is sufficient. If a narrow compatibility fix is necessary, document evidence and avoid replacing processing logic.

### Entity IDs are public; model IDs remain diagnostic
The user explicitly selected Minecraft entity IDs. Resolve current model registry entries per call initially, avoiding reload-invalid caches. Prefer a unique primary-entity model, then a unique subtype match; fail on ties instead of inventing loot merging or registry-order precedence. List covered registered entity IDs even if a lookup reports an ambiguity. Two entities resolving to one model necessarily share a machine selection; document this in TypeScript. Model IDs appear only in item identity details. Native ordered drop stacks become ordinary item detail snapshots, with per-prediction counts for both machine families.

### Explicit nullable API
Use `getSelectedLoot(entity: string): number | null` and `setSelectedLoot(entity: string, index: number | null): void`. Lua nil/omission clears; zero is invalid. A stale out-of-range native selection reads as nil without a read-time repair. Invalid entities remain errors, not indistinguishable nil results. Reject all invalid numeric inputs before mutation, including fractions and non-finite values. This is preferable to native clamping or a magic integer sentinel.

### One namespaced item extension
Use the `dataModel` discriminated union defined in the item-details spec. Both kinds share ordered identity records and one progression object; ordinary models have one identity, combined models preserve four stored identities and duplicates. Do not fabricate per-constituent experience. Read native getters/cached-model views without invoking mutators; work from copies if native constructors can mutate. Validate constituent count and bound holders before constructing a combined view. Expose stable native rank names, cumulative thresholds, remaining data, data-per-kill, FE/tick and iterations. At maximum, omit Lua next-tier fields (typed nullable), rather than call unsafe next-tier logic. Catch narrow malformed-data failures at the provider boundary without hiding unrelated programming errors. Blank, invalid and unrelated items retain ordinary details without the extension.

### Types and tests are first-class delivery
The user approved a narrowly gated compatibility guard for malformed model display names: CC:Tweaked's built-in detail provider calls native display-name code before our additional provider, and the pinned Extra HNN 1.2.2 implementation indexes malformed constituent lists unsafely. Guard only model item display-name failures, preserve valid names and item NBT, and test through the actual detail registry. Keep optional targets behind mod/config gates; do not suppress exceptions from unrelated item types or detail providers.

Add proposed `integrations/hostileNetworks.ts` public contracts and exports using existing `@noSelf` and item-extension patterns. Include both machine interfaces, identity/progression types, single/combined union, optional extended ItemDetail type and examples alongside the types. Do not create duplicate API documentation pages. Fixtures go in `projects/typescript-tests/src`; Forge-native setup belongs in the Forge test mod, with loader-independent fixture/harness code in core where appropriate. Integration-enabled Forge tests are mandatory in addition to minimal root tests: a minimal run alone cannot exercise absent optional machines.

## Risks / Trade-offs

- Entity-to-model ambiguity → deterministic primary/subtype policy and explicit errors, tested with datapack fixtures; never merge incompatible selections.
- Upstream sources may differ from downloaded releases → pin compatible 1.20.1 artifacts and inspect their signatures/state flow before implementation.
- Extra HNN is not just faster HNN → test every registered V1–V4 fabricator and its independent combined-model tier logic, including maximum rank.
- Malformed NBT or unloaded model holders → validate before native getters, preserve base item details and prove item snapshots unchanged.
- Model reload changes order or bounds → consult live definitions; report stale indices as nil and test reordered/removed loot. Native indices remain order-based, not stable item IDs.
- Optional-mod test coverage silently skipped → record installed dependency set and executed test names/counts separately for absent-mod, HNN-only and HNN-plus-Extra runs.

## Migration Plan

Planning artifacts only in this workflow. On explicit apply, create `feat/hostile-neural-network-integrations` from intended `1.20` base before implementation and carry these artifacts onto it, preserving unrelated changes. Implement with optional integrations and no save-format migration: native machines continue owning their state. Verify fixtures, integration runtimes, root multi-loader GameTests and full build with explicit timeouts and complete saved logs. Commit and push the feature branch and open a PR targeting `1.20`; do not merge or push implementation directly to the base. Rollback is disabling the new integration or reverting its commit; native selection/model saves remain readable by the source mods.
