## Purpose

Expose reliable, read-only data-model identity and progression through detailed CC:Tweaked item queries for HNN and Extra HNN.

## ADDED Requirements

### Requirement: Native ordinary-model details
Detailed item queries for valid HNN data model items SHALL include `dataModel` with `kind: "single"`, `models` (one identity record), and `progression`. Identity records SHALL contain `modelId`, primary Minecraft `entityId`, and sorted valid `entityIds` including subtypes. Progression SHALL contain `rank` (stable lowercase native tier name), `data` (native accumulated data), `tierData` (current tier's cumulative threshold), `nextTierData` (next cumulative threshold or null at maximum), `remainingData` (nonnegative data needed for the next tier or null at maximum), `maxRank` (boolean), `dataPerKill`, `simulationCost` (FE/tick), and `iterations` (native simulation iteration counter). Values SHALL come from native model/configuration semantics rather than hardcoded progression tables. Lua nil corresponds to TypeScript null; nil-valued table fields are absent in Lua.

#### Scenario: Ordinary model progression
- **WHEN** a detailed inventory query inspects a valid model below maximum rank
- **THEN** it exposes the correct model/entity identity, native rank, accumulated data, thresholds, remaining data and simulation statistics

#### Scenario: Maximum rank
- **WHEN** a maximum-rank model is inspected
- **THEN** `maxRank` is true, `nextTierData` and `remainingData` are nil/null, and no nonexistent next tier is accessed

### Requirement: Extra HNN combined-model details
Valid Extra HNN combined model items SHALL use the same `dataModel` namespace with `kind: "combined"`, an ordered `models` sequence preserving stored constituents and duplicates, and one `progression` record for the combined item's native shared progress. Native Extra HNN tiers, including its highest tier, and effective simulation/data statistics SHALL be preserved; combined items SHALL NOT be flattened to the first model or represented as independently progressing constituent models.

#### Scenario: Combined model inspection
- **WHEN** an Extra HNN combined model with four constituents is inspected
- **THEN** all four identities appear in stored order and one progression record reflects the Extra HNN item's native shared data and rank

#### Scenario: Extra HNN highest tier
- **WHEN** a combined model at the highest Extra HNN tier is inspected
- **THEN** its native tier name is exposed without coercion into the ordinary HNN tier range and maximum-rank null semantics apply

### Requirement: Defensive read-only enrichment
Providers SHALL preserve existing item fields and other providers' details, SHALL NOT mutate item NBT or progression during inspection, and SHALL NOT introduce this enrichment into basic inventory listings. Unrelated items, blank models, and invalid or unresolved model items SHALL omit `dataModel`. A malformed combined item SHALL omit the entire extension rather than return a misleading valid subset. Malformed or missing IDs, invalid constituent counts, removed registry entries and malformed progression SHALL NOT crash ordinary item queries. Prediction and deep-learner items are outside this extension's scope.

#### Scenario: Invalid and unrelated items
- **WHEN** detailed queries inspect blank, unrelated, malformed or unresolved model items
- **THEN** ordinary item details remain usable without `dataModel` and the item is unchanged

#### Scenario: Multiple providers and repeated reads
- **WHEN** valid models are inspected repeatedly with other detail providers registered
- **THEN** ordinary fields and other extensions remain intact and before/after item NBT is identical

### Requirement: Optional integration and typed regression coverage
HNN details SHALL work without Extra HNN, and Extra HNN classes SHALL be loaded only when that integration is available and enabled. The TypeScript definition project SHALL export a discriminated single/combined detail union and the optional ItemDetail extension without weakening unrelated item types. Executable fixtures and native GameTests SHALL cover ordinary and combined items, rank transitions, maximum tiers, custom native configuration, malformed data, provider coexistence and read-only behavior.

#### Scenario: Typed inspection
- **WHEN** a typed client narrows a detailed item by presence of `dataModel` and its `kind`
- **THEN** it can inspect typed constituent identities and nullable progression values on both ordinary and combined model fixtures
