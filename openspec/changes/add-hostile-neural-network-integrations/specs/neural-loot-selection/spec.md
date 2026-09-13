## Purpose

Allow computers to inspect and select native fabrication loot using Minecraft entity IDs on HNN and Extra HNN fabricators.

## ADDED Requirements

### Requirement: Entity-based discovery and resolution
Supported fabricators SHALL expose `getEntities(): string[]` as a sorted, duplicate-free dense Lua sequence of registered Minecraft entity resource IDs covered by currently loaded data models, including valid subtype entities, independently of inserted predictions. Methods SHALL accept fully qualified Minecraft entity IDs, not model IDs. Resolution SHALL prefer a unique primary-entity model over subtype matches, otherwise require a unique subtype model. Ambiguous matches SHALL raise a descriptive Lua error rather than choose an arbitrary model. Discovery SHALL include ambiguous entity IDs; querying them SHALL report ambiguity. Entities with no model, malformed IDs and unknown entities SHALL raise descriptive errors. Reloaded model data SHALL be used by subsequent calls.

#### Scenario: Discover entities without an input
- **WHEN** an empty fabricator is queried while models are loaded
- **THEN** discovery returns all covered registered entity IDs, including subtypes, sorted without duplicates

#### Scenario: Entity aliases and ambiguity
- **WHEN** two entity IDs resolve to the same model
- **THEN** they address the same native selection on that fabricator
- **AND** if an entity has multiple equally preferred matching models, its lookup fails explicitly

#### Scenario: Data reload
- **WHEN** model definitions are added, removed or changed by reload
- **THEN** subsequent discovery and loot queries reflect the current definitions without retaining stale model objects

### Requirement: Ordered native loot information
`getLoot(entity): ItemDetail[]` SHALL return a dense 1-based sequence of CC:Tweaked-compatible item details in native fabrication-drop order, preserving counts and distinct entries even when item IDs repeat. Counts SHALL describe one prediction's native output, not ultimate-machine batch throughput. A valid model with no drops SHALL return an empty sequence.

#### Scenario: Selection order on both machine families
- **WHEN** base or ultimate loot fabricators query an entity with several drops
- **THEN** index 1 describes the native first drop and index 2 the second, with their native counts and item variant details

### Requirement: Nullable selection and safe mutation
`getSelectedLoot(entity): number | null` SHALL return the currently valid 1-based selection or Lua nil / TypeScript null when unset or no longer in range. `setSelectedLoot(entity, index: number | null): void` SHALL select a valid finite integer index or clear the selection for nil/null (including an omitted Lua argument). Zero, negative, fractional, non-finite, wrong-type and out-of-range indices SHALL fail without mutation; native clamping SHALL NOT leak into the public API. Calls SHALL update only the addressed model on the addressed fabricator, persist across save/reload, and synchronize through the native machine behavior. Selection SHALL be configurable without inserting predictions and SHALL NOT create loot, consume items or bypass native processing.

#### Scenario: Set and clear
- **WHEN** a computer selects index 2 on a model with at least two drops and later clears it with nil
- **THEN** reading the selection returns 2 and then nil respectively
- **AND** other models and other fabricators retain their selections

#### Scenario: Invalid input is non-destructive
- **WHEN** a setter receives an invalid entity or index
- **THEN** it raises a Lua error and leaves saved selections, inventory and energy unchanged

#### Scenario: Native processing and persistence
- **WHEN** a valid selection is made and the fabricator is saved and reloaded
- **THEN** the selection remains visible to Lua and the native GUI
- **AND** subsequent native processing produces the selected output under ordinary resource and energy rules

### Requirement: Optional base and extra integrations
The contract SHALL be available on HNN Loot Fabricators and every Ultimate Loot Fabricator variant supplied by the pinned Forge 1.20.1 Extra HNN release, including V1–V4. HNN integration SHALL function without Extra HNN. Missing or disabled integrations SHALL NOT cause class-loading failures or remove unrelated inventory/energy APIs. Fabric SHALL remain operational without these Forge integrations.

#### Scenario: Optional dependency matrix
- **WHEN** Forge runs with neither integration mod, HNN alone, or HNN plus Extra HNN
- **THEN** only available enabled integrations are registered and unrelated peripherals continue working
- **AND** Fabric starts without either Forge-only dependency

### Requirement: Typed and exercised public contract
The TypeScript definition project SHALL export and document both fabricator contracts, entity-ID semantics, nullable values, ordering and failures. Executable TypeScript-to-Lua fixtures and native GameTests SHALL verify discovery, every method, persistence, failure atomicity, real production and all supported ultimate variants. Root multi-loader GameTests and the build SHALL pass before implementation is considered complete.

#### Scenario: Typed Lua client
- **WHEN** a typed fixture discovers an entity, reads its loot, selects it and clears it
- **THEN** it compiles and runs through real peripheral calls with the specified index and null/nil semantics
