## Purpose

Allow computers to inspect and author one native AE2 pattern at a time, with explicit clearing before replacement and native validation for recipe-backed patterns.

## ADDED Requirements

### Requirement: Dedicated single-pattern pedestal

The system SHALL provide `peripheralworks:ae2_pattern_pedestal` as a block and peripheral on Fabric and Forge when AE2 is installed. It SHALL operate without an ME network, energy, or channel. Its inventory SHALL have one slot, numbered 1 in Lua, with capacity exactly one item. It SHALL accept only AE2 blank patterns and the four native encoded pattern items, including native patterns that can no longer decode. Manual interaction, Lua inventory transfers, and loader-native automation SHALL enforce the same acceptance and capacity rules. Rejected insertion SHALL leave the source unchanged except for any one item successfully transferred.

#### Scenario: Insert a stack of blanks
- **WHEN** an empty pedestal receives a stack of blank patterns through any supported insertion path
- **THEN** exactly one blank is inserted and all remaining items stay at the source

#### Scenario: Reject an unrelated item or additional pattern
- **WHEN** an unrelated item is inserted, or any item is inserted into an occupied pedestal
- **THEN** no item moves and the stored pattern is unchanged

#### Scenario: Optional dependency absent
- **WHEN** either loader starts without AE2
- **THEN** the pedestal is unavailable and no AE2-dependent class causes startup failure

### Requirement: Inspect without modifying patterns

The peripheral SHALL expose `getPattern()` returning a snapshot with `state` equal to `empty`, `blank`, `encoded`, or `invalid`. Occupied states SHALL include `item` using existing item-detail conventions. Encoded and invalid native patterns SHALL include `type` equal to `crafting`, `processing`, `stonecutting`, or `smithing`. A decodable pattern SHALL include `inputs` and `outputs` using the existing AE2 pattern representation, including alternatives, plus `definition` containing its ordered concrete encoding data. Recipe-backed definitions SHALL include their recipe ID and substitution flags. Invalid patterns SHALL include an explanatory `error`; reads SHALL NOT clear or repair them. Concrete definitions SHALL preserve NBT via `snbt`, without changing the meaning of existing item-detail `nbt` hashes.

#### Scenario: Inspect a crafting pattern
- **WHEN** `getPattern()` reads a valid crafting pattern
- **THEN** it reports the recipe, exact crafting-grid positions, concrete item variants, outputs, and substitution flags without changing the item

#### Scenario: Missing recipe
- **WHEN** a native recipe-backed pattern cannot decode after a recipe is removed
- **THEN** inspection reports `invalid`, retains its item details and native type, and does not mutate the pattern

### Requirement: Blank-only atomic encoding

The peripheral SHALL expose `encodeProcessingPattern(definition)`, `encodeCraftingPattern(definition)`, `encodeStonecuttingPattern(definition)`, and `encodeSmithingTablePattern(definition)`. Every encoder SHALL require exactly one blank pattern at execution time, produce exactly one encoded pattern on success, and return `true`. No encoder SHALL overwrite an encoded or invalid pattern, even with identical data. All validation and construction SHALL finish before replacement. Failed calls SHALL preserve the complete original stack. The API SHALL expose no in-place ingredient, output, or substitution setters.

#### Scenario: Reject overwrite with each encoder
- **WHEN** any encoder is invoked with any encoded or invalid native pattern present
- **THEN** it returns `nil, error` instructing the caller to revert first and preserves the pattern

#### Scenario: Empty pedestal
- **WHEN** any encoder runs without a blank pattern
- **THEN** it returns `nil, error` and creates no item

#### Scenario: Competing calls
- **WHEN** two computers encode the same blank pattern
- **THEN** at most one succeeds and the other observes the encoded state without replacing it

### Requirement: Concrete Lua resource descriptors

Encoding resources SHALL use `{type = "item" | "fluid", name = registryId, count = positiveInteger, snbt = optionalCompoundSNBT}`. Counts SHALL be exact finite positive integers representable by Lua and the relevant native AE2 field; fluid counts SHALL be millibuckets on both loaders. Omitted SNBT SHALL mean an untagged variant. Supplied SNBT SHALL describe only the resource's tag, not an enclosing ItemStack or pattern payload. The API SHALL reject unknown IDs, malformed tables, unsupported keys, non-compound or malformed SNBT, and numeric overflow rather than silently dropping data. SNBT SHALL be limited to 65536 UTF-8 bytes per descriptor and nesting depth 64. Ordinary item-detail `nbt` hashes SHALL NOT be accepted as SNBT.

#### Scenario: Exact variant
- **WHEN** a processing descriptor includes valid item or fluid SNBT
- **THEN** the encoded resource retains that tag and inspection returns an equivalent SNBT representation

#### Scenario: Unsafe or malformed input
- **WHEN** a descriptor contains an invalid ID, invalid count, excessive SNBT, or a hash in place of SNBT
- **THEN** the call raises a descriptive Lua argument error without consuming the blank

### Requirement: Arbitrary processing transformations

`encodeProcessingPattern` SHALL accept a definition with dense ordered `inputs` and `outputs` arrays of resource descriptors, each with at least one entry and within native AE2 slot and amount limits. Both items and fluids SHALL be supported. It SHALL NOT require a real recipe, installed machine, available ingredients, material conservation, or an output derivable from the inputs. Native pattern-format constraints and safe parsing SHALL still apply. Entries and their order SHALL be preserved without application-level deduplication.

#### Scenario: Fictional processing recipe
- **WHEN** a blank is encoded with structurally valid inputs and unrelated outputs for which no recipe or machine exists
- **THEN** encoding succeeds and inspection reflects the requested transformation

#### Scenario: Native format limits
- **WHEN** an input or output array exceeds native AE2 limits or is empty
- **THEN** encoding fails without consuming or altering the blank

### Requirement: Native recipe-backed encoding

Crafting definitions SHALL contain `recipeId`, `grid`, and optional `substitute` and `substituteFluids` booleans defaulting to false. `grid` SHALL be a sparse Lua table indexed 1 through 9 in row-major order, with omitted slots empty and occupied slots containing item descriptors with count 1. Stonecutting definitions SHALL contain `recipeId`, `input` (an item descriptor with count 1), and optional `substitute` defaulting to false. Smithing definitions SHALL contain `recipeId`, `template`, `base`, `addition` (item descriptors with count 1), and optional `substitute` defaulting to false. Recipe-backed methods SHALL resolve the named recipe in the current world, use native Minecraft/AE2 matching, assembly, and pattern validation, and derive outputs rather than accepting caller-supplied outputs. They SHALL require no actual ingredient consumption. They SHALL NOT introduce a parallel custom recipe-validity system or bypass native NBT, recipe type, remainder, or substitution semantics.

#### Scenario: Valid native recipes
- **WHEN** each recipe-backed encoder receives concrete inputs accepted by its named native recipe and AE2
- **THEN** it produces a usable native pattern with the native output and requested supported substitution flags

#### Scenario: Invalid native recipe or input
- **WHEN** a recipe is missing, has the wrong type, does not match the supplied inputs, produces an empty result, or cannot be encoded as a valid native pattern
- **THEN** the method returns `nil, error` and the blank is unchanged

#### Scenario: Forged crafting output
- **WHEN** Lua supplies an output field to a recipe-backed encoder
- **THEN** the call raises an argument error instead of using the supplied output

### Requirement: Explicit reversion

`clearPattern()` SHALL replace exactly one native encoded pattern, including an invalid one, with exactly one untagged AE2 blank pattern and return `true`. This explicitly discards its encoded data and other item tags. A blank SHALL return `false` without changing it; empty or unsupported contents SHALL return `nil, error` unchanged. Clearing SHALL not depend on successful recipe decoding. Viewing and clearing SHALL be the only pattern-editing operations allowed while an encoded pattern is present; normal physical/inventory removal remains available.

#### Scenario: Edit through explicit clearing
- **WHEN** a computer reads a pattern, calls `clearPattern()`, modifies its saved concrete definition, and calls an encoder
- **THEN** the existing pattern is replaced only through the explicit encoded-to-blank-to-encoded sequence

#### Scenario: Recover an invalid native pattern
- **WHEN** a native encoded pattern has corrupt encoding data or a missing recipe and `clearPattern()` is called
- **THEN** exactly one blank is returned in the same slot without requiring decode success

### Requirement: Persistence and Lua compatibility

Successful mutations SHALL persist and synchronize the displayed item. Unloading and reloading SHALL preserve the stored stack, and breaking the pedestal SHALL drop its contents exactly once. Existing pedestal inventory Lua conventions SHALL remain available. Malformed arguments SHALL raise Lua errors; operational failures SHALL return `nil, error`, except the documented inspection invalid state. TypeScript contracts and executable Lua fixtures SHALL document these distinctions and all four methods. Existing AE2 pattern-provider behavior SHALL remain unchanged.

#### Scenario: Reload and break
- **WHEN** an encoded pedestal is saved, reloaded, and then broken
- **THEN** its exact encoded item survives reload and is dropped once without duplication

#### Scenario: Existing API compatibility
- **WHEN** existing scripts inspect pattern providers or use ordinary item/map pedestals
- **THEN** their return shapes, inventory capacity, and transfer behavior remain unchanged
