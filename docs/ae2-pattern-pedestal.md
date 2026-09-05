# AE2 Pattern Pedestal

Place one AE2 blank or native encoded pattern on this pedestal. It works without an ME network and exposes `peripheralworks:ae2_pattern_pedestal` plus the usual one-slot extended inventory API. Existing encoded patterns cannot be edited in place: inspect, explicitly clear, then encode the replacement.

## Methods

| Method | Result |
| --- | --- |
| `getPattern()` | A snapshot with `state` equal to `empty`, `blank`, `encoded`, or `invalid` |
| `clearPattern()` | `true`, `false` when already blank, or `nil, reason` |
| `encodeProcessingPattern({inputs={...}, outputs={...}})` | `true`, or `nil, reason` |
| `encodeCraftingPattern({recipeId=..., grid={...}, substitute=false, substituteFluids=false})` | `true`, or `nil, reason` |
| `encodeStonecuttingPattern({recipeId=..., input=..., substitute=false})` | `true`, or `nil, reason` |
| `encodeSmithingTablePattern({recipeId=..., template=..., base=..., addition=..., substitute=false})` | `true`, or `nil, reason` |

Every encoder requires exactly one blank pattern. Clearing one blank returns `false` without changing it. Clearing even a malformed native encoded pattern returns one blank. Empty, unsupported, or over-count contents cannot be encoded or cleared. There is no bulk encoding or in-place flag modification.

Failed operations leave the held stack, including its NBT, unchanged. Operational failures (missing/invalid recipes, mismatched recipe inputs, occupied pedestal) return `nil, reason`; malformed argument shapes, unsafe numbers, unknown resource IDs or SNBT raise a Lua error. Inspection of an invalid native pattern returns `{state="invalid", item=..., type=..., error=...}`, without editing or repairing it.

## Ingredients

An item is `{type="item", name="minecraft:stone", count=1, snbt="{...}"}`. A processing fluid is `{type="fluid", name="minecraft:water", count=1000, snbt="{...}"}`. `type`, `name`, and `count` are required. Fluid counts are always whole millibuckets on both Fabric and Forge. Recipe-backed ingredients must be single items, not fluids.

IDs must be registered resources. Amounts must be finite positive integers, exactly representable by Lua and the native AE2 fields. Processing arrays are nonempty, ordered and dense, within the installed AE2 version's entry limits. They describe arbitrary transformations: no machine recipe or ingredient availability is checked, and no ingredients are consumed.

Optional `snbt` is a compound SNBT string, at most 65,536 UTF-8 bytes and 64 container levels deep. Malformed or excessive SNBT is rejected. The existing item-detail `nbt` field is a fingerprint, not an accepted encoding field. Inspection returns canonical SNBT for exact item/fluid variants; definitions requiring unsafe or unsupported representation return `state="invalid"` rather than losing information.

## Recipes and inspection

Crafting `grid` is a sparse table indexed 1–9, row-major across a 3×3 grid. Supply a registered crafting `recipeId` and concrete input descriptors; both shaped and shapeless recipes use native matching and result assembly. Stonecutting and smithing similarly use their explicit recipe IDs and native validation. Callers cannot specify their outputs.

Crafting definitions accept `substitute` and `substituteFluids`; stonecutting and smithing accept `substitute`. These optional booleans default to false. Other options, including processing flags, are rejected.

Encoded inspection returns `state="encoded"`, `item`, `type`, `definition`, and the existing AE2 `inputs`/`outputs` summary fields, including alternatives. Recipe IDs and substitution flags live inside `definition`. The definition retains crafting grid positions and processing array order/duplicates independently of AE2's condensed summary. After an explicit clear, pass it back to the corresponding encoder.

```lua
local pedestal = peripheral.find("peripheralworks:ae2_pattern_pedestal")
assert(pedestal, "No pattern pedestal connected")

-- Insert one blank pattern by hand or through the inventory API first.
assert(pedestal.encodeProcessingPattern({
  inputs = {{type = "item", name = "minecraft:cobblestone", count = 1}},
  outputs = {{type = "item", name = "minecraft:diamond", count = 1, snbt = '{custom:"Example"}'}},
}))
local saved = pedestal.getPattern()
assert(saved.state == "encoded" and saved.type == "processing")
assert(pedestal.clearPattern()) -- explicit permission to discard the old encoding
assert(pedestal.encodeProcessingPattern(saved.definition))
```

The example only authors a processing instruction. It does not create diamonds or provide a machine capable of executing that transformation.
