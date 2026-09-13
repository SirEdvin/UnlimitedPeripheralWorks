## Why

CC:Tweaked computers cannot currently choose Hostile Neural Networks fabrication outputs or inspect model progression through Unlimited Peripheral Works. Supporting both HNN and Extra Hostile Neural Networks enables automated loot selection and informed monitoring without parsing private item NBT in Lua.

## What Changes

- Add optional Forge 1.20.1 plugins for HNN Loot Fabricators and Extra HNN Ultimate Loot Fabricators, including all supported V1–V4 variants.
- Expose `getEntities()`, `getLoot(entity)`, `getSelectedLoot(entity)` and `setSelectedLoot(entity, index)` using Minecraft entity resource IDs and 1-based native loot ordering. Unset selections return Lua nil / TypeScript null; nil/null clears selections.
- Add CC:Tweaked ItemDetail enrichment for ordinary models and Extra HNN combined models, including identity, native rank and data progression.
- Add TypeScript contracts and executable fixtures, native integration GameTests, optional-dependency coverage, and multi-loader regression verification.
- Require implementation on a separate feature branch from `1.20`, followed by a PR targeting `1.20`; no direct base-branch implementation or push.

## Capabilities

### New Capabilities
- `neural-loot-selection`: Entity-addressed loot selection on base and ultimate fabricators.
- `neural-model-item-details`: Read-only native model identity and progression in detailed item queries, including combined models.

### Modified Capabilities
None.

## Impact

Forge integration registration, dependency catalog/build configuration, integration config definitions, CC:Tweaked plugin and detail-provider hooks, `projects/typed-peripheral-unlimitedperipheralworks`, TypeScript-to-Lua fixtures, and Forge integration GameTests. HNN, its required Placebo dependency, and Extra HNN must be version-pinned during implementation. Fabric acquires no Forge-only dependencies and remains covered by root build/GameTests. No production code changes are part of this planning change.
