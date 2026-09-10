import { ExtendedInventoryAPI } from "@siredvin/typed-peripheral-api/inventory_extended";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { Fallible } from "../types";
import { AE2Pattern } from "./ae2";

/** Exact item/fluid variant. Unknown IDs, unsafe counts and malformed SNBT raise Lua errors. */
export type AE2PatternResource = {
    type: "item" | "fluid";
    /** Registered resource ID, e.g. minecraft:stone or minecraft:water. */
    name: string;
    /** Finite positive safe integer representable by AE2; fluids use whole millibuckets on both loaders. */
    count: number;
    /** Exact resource data as compound SNBT, NOT an ItemDetail.nbt hash.
     * Minecraft 1.20 uses resource NBT; 1.21 uses a data-component patch
     * (e.g. {"minecraft:custom_data":{custom:"value"}}), including removed components.
     * Maximum 65536 UTF-8 bytes, depth 64.
     */
    snbt?: string;
};
/** Recipe-backed ingredients must be single items, not fluids. */
export type AE2PatternIngredient = AE2PatternResource & { type: "item"; count: 1 };
/** Nonempty, dense, ordered arrays within the installed AE2 version's entry limits. No flags are accepted. */
export type AE2ProcessingDefinition = { inputs: AE2PatternResource[]; outputs: AE2PatternResource[] };
/** Shaped/shapeless crafting uses native matching and result assembly; callers cannot supply outputs. */
export type AE2CraftingDefinition = {
    /** Registered crafting recipe ID. */
    recipeId: string;
    /** Sparse row-major 3x3 grid, Lua slots 1 through 9. Missing slots are empty. */
    grid: LuaTable<number, AE2PatternIngredient>;
    /** Defaults to false. */
    substitute?: boolean;
    /** Defaults to false. */
    substituteFluids?: boolean;
};
/** Explicit native stonecutting recipe; substitute defaults to false. Outputs are recipe-derived. */
export type AE2StonecuttingDefinition = { recipeId: string; input: AE2PatternIngredient; substitute?: boolean };
/** Explicit native smithing recipe; substitute defaults to false. Outputs are recipe-derived. */
export type AE2SmithingDefinition = {
    recipeId: string;
    template: AE2PatternIngredient;
    base: AE2PatternIngredient;
    addition: AE2PatternIngredient;
    substitute?: boolean;
};
export type AE2PatternKind = "crafting" | "processing" | "stonecutting" | "smithing";
/**
 * Read-only snapshot. Invalid native patterns are reported without repair or mutation.
 * Encoded definitions preserve grid positions, processing order/duplicates and canonical SNBT,
 * independently of AE2's condensed inputs/outputs summaries and ingredient alternatives.
 * After clearing, pass definition to the matching encoder. Unsafe or unsupported representations
 * produce state="invalid" rather than losing information.
 */
export type AE2PedestalPattern =
    | { state: "empty" }
    | { state: "blank"; item: ItemDetail }
    | { state: "invalid"; item: ItemDetail; type?: AE2PatternKind; error: string }
    | ({ state: "encoded"; item: ItemDetail } & AE2Pattern & (
        | { type: "processing"; definition: AE2ProcessingDefinition }
        | { type: "crafting"; definition: AE2CraftingDefinition }
        | { type: "stonecutting"; definition: AE2StonecuttingDefinition }
        | { type: "smithing"; definition: AE2SmithingDefinition }
    ));

/**
 * One-slot pattern editor; no ME network is required. Insert a blank/native encoded pattern
 * manually or through the inherited extended inventory API.
 * Every encoder requires exactly one blank pattern: inspect, explicitly clear, then re-encode.
 * There is no bulk encoding or in-place flag modification, and no ingredients are consumed.
 * Failed operations preserve the held stack and NBT. Missing/invalid recipes, mismatched inputs
 * and occupied pedestals return nil,reason; malformed arguments, unknown fields/resource IDs,
 * unsafe numbers and invalid SNBT raise Lua errors.
 *
 * @example Lua: insert one blank pattern before running.
 * local pedestal = assert(peripheral.find("peripheralworks:ae2_pattern_pedestal"))
 * assert(pedestal.encodeProcessingPattern({
 *   inputs = {{type = "item", name = "minecraft:cobblestone", count = 1}},
 *   outputs = {{type = "item", name = "minecraft:diamond", count = 1, snbt = '{custom:"Example"}'}},
 * }))
 * local saved = pedestal.getPattern()
 * assert(saved.state == "encoded" and saved.type == "processing")
 * assert(pedestal.clearPattern()) -- explicitly discard the old encoding
 * assert(pedestal.encodeProcessingPattern(saved.definition))
 * -- This only authors an instruction: it creates neither diamonds nor a machine to make them.
 *
 * @noSelf
 */
export interface AE2PatternPedestal extends ExtendedInventoryAPI {
    /** Read-only, including when a native recipe is missing or a pattern is corrupt. */
    getPattern(): AE2PedestalPattern;
    /**
     * Destructively discard encoded data/tags, even from malformed native patterns, returning one blank.
     * Returns false without mutation when already blank; empty, unsupported or over-count contents fail.
     */
    clearPattern(): Fallible<boolean>;
    /** Blank-only. Arbitrary transformations are allowed: no recipe, machine, or material-balance validation. */
    encodeProcessingPattern(definition: AE2ProcessingDefinition): Fallible<true>;
    /** Blank-only. Native recipe/AE2 validation derives outputs; no real ingredients are consumed. */
    encodeCraftingPattern(definition: AE2CraftingDefinition): Fallible<true>;
    /** Blank-only; output is derived from the selected native recipe. */
    encodeStonecuttingPattern(definition: AE2StonecuttingDefinition): Fallible<true>;
    /** Blank-only; native assembly preserves recipe-defined NBT. */
    encodeSmithingTablePattern(definition: AE2SmithingDefinition): Fallible<true>;
}

/** Invalid arguments throw; operational failures return nil,error. Clear before re-encoding. */
export const ae2PatternPedestalProvider = new IPeripheralProvider<AE2PatternPedestal>("peripheralworks:ae2_pattern_pedestal");
