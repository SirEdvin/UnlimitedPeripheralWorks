## Why

Computers can inspect patterns in AE2 pattern providers but cannot author patterns on a dedicated workstation. A single-pattern pedestal enables scripted inspection and editing without silently overwriting encoded patterns.

## What Changes

- Add an AE2-only pattern pedestal holding exactly one item, accessible through normal pedestal interaction and inventory automation.
- Expose Lua inspection, explicit reversion to blank, and separate blank-only encoders for crafting, processing, stonecutting, and smithing table patterns on both loaders.
- Allow item/fluid descriptors with optional SNBT for exact variants. Processing patterns describe arbitrary transformations: do not require matching recipes, material balance, or machine availability. Preserve structural, numeric, and serialization safety checks.
- Delegate recipe-backed validation and output derivation to the native Minecraft/AE2 mechanisms used by AE2, rather than implementing a second recipe validator.
- Preserve stored patterns on errors, persist successful changes, and reuse existing pedestal rendering, inventory conventions, and AE2 representations.
- Add typed API documentation, Lua fixtures, and multi-loader regression coverage.

## Capabilities

### New Capabilities

- `ae2-pattern-pedestal`: Single-pattern storage, inspection, explicit clearing, four native encoding methods, and their Lua contracts.

### Modified Capabilities

None. Existing pattern-provider methods and pattern representations remain compatible.

## Impact

Shared AE2 behavior belongs in `projects/core/src/ae2Integration/`; optional registration and datagen use existing AE2 integration hooks. Pedestal storage needs a real one-item limit and insertion predicate at both native storage boundaries: the current `holdingStacks = 1` means one stack, not one item, and `itemFilter` is not enforced by the current storage factories. Reuse existing infrastructure with backward-compatible defaults rather than changing unrelated pedestal behavior.

Affected supporting areas include loader storage adapters, client renderer registration, conditional recipes/models/localizations, `projects/typed-peripheral-unlimitedperipheralworks/`, `projects/typescript-tests/`, and `projects/core/src/ae2Test/`. No new dependency, ME grid node, channel, custom GUI, bulk editor, or addon-pattern encoder is required.
