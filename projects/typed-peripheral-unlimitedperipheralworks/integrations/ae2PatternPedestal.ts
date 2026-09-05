import { ExtendedInventoryAPI } from "@siredvin/typed-peripheral-api/inventory_extended";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { Fallible } from "../types";
import { AE2Pattern } from "./ae2";

export type AE2PatternResource = {
    type: "item" | "fluid";
    name: string;
    /** Positive safe integer; fluid quantities are millibuckets on both loaders. */
    count: number;
    /** Resource tag as compound SNBT, NOT an ItemDetail.nbt hash. Maximum 65536 UTF-8 bytes, depth 64. */
    snbt?: string;
};
export type AE2PatternIngredient = AE2PatternResource & { type: "item"; count: 1 };
export type AE2ProcessingDefinition = { inputs: AE2PatternResource[]; outputs: AE2PatternResource[] };
export type AE2CraftingDefinition = {
    recipeId: string;
    /** Sparse row-major 3x3 grid, Lua slots 1 through 9. Missing slots are empty. */
    grid: LuaTable<number, AE2PatternIngredient>;
    substitute?: boolean;
    substituteFluids?: boolean;
};
export type AE2StonecuttingDefinition = { recipeId: string; input: AE2PatternIngredient; substitute?: boolean };
export type AE2SmithingDefinition = {
    recipeId: string;
    template: AE2PatternIngredient;
    base: AE2PatternIngredient;
    addition: AE2PatternIngredient;
    substitute?: boolean;
};
export type AE2PatternKind = "crafting" | "processing" | "stonecutting" | "smithing";
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

/** @noSelf */
export interface AE2PatternPedestal extends ExtendedInventoryAPI {
    /** Read-only, including when a native recipe is missing or a pattern is corrupt. */
    getPattern(): AE2PedestalPattern;
    /** Destructively discard encoded data/tags, returning one blank. False when already blank. */
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
