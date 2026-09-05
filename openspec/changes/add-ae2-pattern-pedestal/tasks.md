## 1. Establish native reuse and delivery scope

- [x] 1.1 Create a feature branch from the intended `1.20` base before implementation, preserving planning artifacts; verify branch and worktree with `git status --short --branch`.
- [x] 1.2 Trace both installed AE2 versions' terminal encode, native recipe matching/assembly, pattern decode, sparse input and tag serialization paths; record verified method/field references and native slot/amount limits in design.md, confirming all four encoders and that recipe validation is not assumed from signatures alone.
- [x] 1.3 Inspect native storage mutation and simulation APIs plus every factory caller; verify a one-slot replacement approach preserves transaction semantics and existing pedestal defaults before changing the storage path.

## 2. Pedestal and native inventory

- [x] 2.1 Add backward-compatible optional acceptance and absolute capacity parameters through `ModInnerPlatform`, both platform factories and native storage adapters; verify one-item capacity and filtering through native insertion/simulation tests while ordinary pedestal capacity remains unchanged.
- [x] 2.2 Add the pattern pedestal block/entity under shared optional AE2 sources, using existing interaction, persistence and inventory plugin behavior; verify manual insert/remove, Lua pull/push, native automation, unsupported-item rejection, and over-count saved-data rejection by mutation methods without silently deleting stored items.
- [x] 2.3 Register the optional block/entity, conditional recipe, loot, creative entry, models, renderer and English/Ukrainian descriptions using existing hooks; verify both loader datagen tasks and client rendering, and dedicated-server startup without client-class linkage.

## 3. Lua pattern operations

- [x] 3.1 Implement strict descriptor/definition parsing with native SNBT parsing, byte/depth bounds, exact safe amounts, item/fluid identity and millibucket conversion; verify valid tagged variants and malformed keys, tables, IDs, hashes, SNBT, non-finite/fractional/overflow amounts all leave the blank unchanged.
- [x] 3.2 Implement `getPattern()` with empty/blank/encoded/invalid states, existing input/output representations and separate concrete definitions; verify all four native types retain positions, recipe identity, flags and equivalent SNBT, including inspect-clear-reencode round trips and missing-recipe errors.
- [x] 3.3 Implement the common blank-only guarded replacement and `clearPattern()`; verify every encoder rejects every encoded type and invalid patterns, competing calls cannot overwrite a successful encoding, clear recovers invalid native patterns, and blank/empty results match the contract.
- [x] 3.4 Implement `encodeProcessingPattern()` through native AE2 encoding without recipe, machine, conservation or ingredient-availability checks; verify a fictional cobblestone-to-diamond transformation succeeds, mixed item/fluid inputs and outputs preserve order/tags/amounts, and native structural-limit failures preserve the blank.
- [x] 3.5 Implement `encodeCraftingPattern()` using native matching, assembly, encoding and validated decoding; verify shaped and shapeless recipes, sparse grid positions, tagged items, supported substitutions and native remainders, plus wrong recipe/type/input/output-field rejection without mutation.
- [x] 3.6 Implement `encodeStonecuttingPattern()` using the explicit native recipe and derived output; verify valid recipes, ambiguous alternative recipe selection by ID, substitutions, invalid inputs and missing recipes against AE2 decoding.
- [x] 3.7 Implement `encodeSmithingTablePattern()` using native template/base/addition matching and assembly; verify supported smithing recipe variants and NBT preservation against native results, plus wrong template/base/addition, missing recipes and invalid outputs without mutation.

## 4. Contracts and regression coverage

- [x] 4.1 Add typed API contracts and provider registration under `projects/typed-peripheral-unlimitedperipheralworks/`, documenting SNBT versus NBT hashes, sparse slots, fluid units, method results, arbitrary processing semantics and destructive explicit clearing; verify the package's existing type/build checks and matching runtime method discovery.
- [x] 4.2 Add self-contained TypeScript-to-Lua pedestal fixtures and shared AE2 GameTests using existing fixture conventions; verify real Lua calls cover all four encoders, reads, clears, argument errors and blank-only protection on both loaders without replacing native AE2 results with mocks.
- [x] 4.3 Add integration regression coverage for save/reload, render synchronization, break/drop exactly once, inventory transfers and native transaction simulation, optional-AE2 absence, and unchanged ordinary pedestal/pattern-provider behavior; verify each scenario through the appropriate GameTest or bounded runtime smoke check.

## 5. Full verification and delivery

- [x] 5.1 Run `:typescript-tests:compileTestLua --no-daemon` with the documented five-minute timeout and complete saved log; verify exit 0 and report duration/log path.
- [x] 5.2 Run root `gameTest --no-daemon -PminimalTestEnvironment` through `xvfb-run -a` with the documented twenty-minute timeout and complete saved log; verify both Fabric and Forge execute the new and existing AE2 tests, report results/duration/log path, and stop development processes.
- [x] 5.3 Run the timed multi-loader `build --no-daemon` with the documented ten-minute timeout and complete saved log; verify exit 0, report duration/log path, and inspect only relevant failure windows if needed.
- [x] 5.4 Run `openspec validate add-ae2-pattern-pedestal --strict` and `git diff --check`; verify all requirements are covered, generated outputs came from their providers, and no logs/build/run artifacts or unrelated changes are staged.
- [ ] 5.5 Commit and push completed implementation only on the feature branch and open a PR targeting `1.20` when delivery is authorized; verify remote commit identity and read back the PR target/URL, then report the URL without merging directly.
