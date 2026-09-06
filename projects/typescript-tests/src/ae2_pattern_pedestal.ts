import type { AE2PatternPedestal, AE2PatternIngredient, AE2ProcessingDefinition } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2PatternPedestal";
import type { Fallible } from "@siredvin/typed-peripheral-unlimitedperipheralworks/types";

/** @noSelf */
interface TestApi { ok(marker?: string): void; }
declare const test: TestApi;
const check = (value: unknown, message: string): void => { if (!value) throw message; };
const sameDefinition = (left: unknown, right: unknown): boolean => {
    if (left === right) return true;
    if (type(left) !== "table" || type(right) !== "table") return false;
    const a = left as Record<string, unknown>, b = right as Record<string, unknown>;
    return Object.keys(a).length === Object.keys(b).length && Object.keys(a).every(key => sameDefinition(a[key], b[key]));
};
const target = peripheral.wrap("front") as AE2PatternPedestal;
check(target && peripheral.hasType("front", "peripheralworks:ae2_pattern_pedestal"), "Missing pattern pedestal");
check(target.getPattern().state === "empty", "Initial state must be empty");
check(target.pullItems("right", 2, 64) === 0, "Unrelated item was accepted");
check(target.pullItems("right", 1, 64) === 1, "Must insert exactly one blank");
check(target.pullItems("right", 1, 64) === 0, "Occupied slot accepted another blank");
check(target.getPattern().state === "blank", "Blank state is wrong");
const [unchanged] = target.clearPattern();
check(unchanged === false, "Clearing a blank should return false");
const item = (name: string, snbt?: string): AE2PatternIngredient => ({ type: "item", name, count: 1, snbt });
const processing: AE2ProcessingDefinition = {
    inputs: [item("minecraft:cobblestone", '{custom:"value"}'), { type: "fluid", name: "minecraft:water", count: 1000, snbt: '{custom:"fluid"}' }, item("minecraft:cobblestone", '{custom:"value"}')],
    outputs: [item("minecraft:diamond"), { type: "fluid", name: "minecraft:lava", count: 250 }],
};
const grid = new LuaTable<number, AE2PatternIngredient>();
for (const slot of [1, 2, 4, 5]) grid.set(slot, item("minecraft:oak_planks"));
const crafting = { recipeId: "minecraft:crafting_table", grid, substitute: false, substituteFluids: false };
const stonecutting = { recipeId: "minecraft:stone_slab_from_stone_stonecutting", input: item("minecraft:stone"), substitute: false };
const smithing = {
    recipeId: "minecraft:netherite_sword_smithing",
    template: item("minecraft:netherite_upgrade_smithing_template"),
    base: item("minecraft:diamond_sword", '{Damage:7,display:{Name:\'{"text":"Exact sword"}\'}}'),
    addition: item("minecraft:netherite_ingot"), substitute: false,
};
const encoders: (() => Fallible<true>)[] = [
    () => target.encodeProcessingPattern(processing), () => target.encodeCraftingPattern(crafting),
    () => target.encodeStonecuttingPattern(stonecutting), () => target.encodeSmithingTablePattern(smithing),
];
for (const encode of encoders) {
    const [encoded, error] = encode();
    check(encoded === true, `Encoding failed: ${error}`);
    const pattern = target.getPattern();
    check(pattern.state === "encoded", "Encoded pattern could not be read");
    for (const overwrite of encoders) {
        const [rejected, reason] = overwrite();
        check(rejected === null && reason !== undefined, "Encoded pattern was overwritten");
    }
    if (pattern.state === "encoded") {
        check(pattern.outputs.length > 0, "No native output");
        if (pattern.type === "processing") check(pattern.definition.inputs[0].snbt !== undefined && pattern.definition.inputs[1].count === 1000, "Exact processing variants lost");
        if (pattern.type === "crafting") check(pattern.definition.grid.has(5) && !pattern.definition.grid.has(3), "Crafting geometry lost");
        if (pattern.type === "smithing") check(pattern.definition.base.snbt !== undefined, "Smithing base NBT lost");
    }
    const [cleared] = target.clearPattern();
    check(cleared === true && target.getPattern().state === "blank", "Clear did not produce a blank");
    if (pattern.state === "encoded") {
        let restored: true | null = null;
        if (pattern.type === "processing") {
            check(pattern.definition.inputs.length === 3 && pattern.definition.inputs[2].name === "minecraft:cobblestone", "Processing order or duplicates lost");
            [restored] = target.encodeProcessingPattern(pattern.definition);
        } else if (pattern.type === "crafting") [restored] = target.encodeCraftingPattern(pattern.definition);
        else if (pattern.type === "stonecutting") [restored] = target.encodeStonecuttingPattern(pattern.definition);
        else [restored] = target.encodeSmithingTablePattern(pattern.definition);
        const restoredPattern = target.getPattern();
        check(restored === true && restoredPattern.state === "encoded", "Definition could not be re-encoded");
        if (restoredPattern.state === "encoded") {
            check(restoredPattern.type === pattern.type && sameDefinition(pattern.definition, restoredPattern.definition), "Clear/re-encode changed the pattern definition");
        }
        target.clearPattern();
    }
}
const shapelessGrid = new LuaTable<number, AE2PatternIngredient>();
shapelessGrid.set(9, item("minecraft:oak_log"));
const [shapeless] = target.encodeCraftingPattern({ recipeId: "minecraft:oak_planks", grid: shapelessGrid, substitute: true, substituteFluids: true });
check(shapeless === true, "Native shapeless recipe rejected");
const shapelessPattern = target.getPattern();
check(shapelessPattern.state === "encoded" && shapelessPattern.type === "crafting" && shapelessPattern.definition.substitute && shapelessPattern.definition.substituteFluids && shapelessPattern.definition.grid.has(9), "Substitution flags or sparse position lost");
target.clearPattern();
let successes = 0;
parallel.waitForAll(
    () => { const [result] = target.encodeProcessingPattern(processing); if (result) successes++; },
    () => { const [result] = target.encodeCraftingPattern(crafting); if (result) successes++; },
);
check(successes === 1, "Competing calls both consumed the same blank");
target.clearPattern();
const [missingRecipe] = target.encodeCraftingPattern({ ...crafting, recipeId: "minecraft:missing_recipe" });
check(missingRecipe === null && target.getPattern().state === "blank", "Missing recipe consumed blank");
for (const [label, slots, material] of [
    ["wrong ingredients", [1, 2, 4, 5], "minecraft:stone"],
    ["wrong layout", [1, 3, 7, 9], "minecraft:oak_planks"],
] as const) {
    const invalidGrid = new LuaTable<number, AE2PatternIngredient>();
    for (const slot of slots) invalidGrid.set(slot, item(material));
    const before = target.getPattern();
    const [result, reason] = target.encodeCraftingPattern({ ...crafting, grid: invalidGrid });
    check(result === null && reason !== undefined, `Crafting table accepted ${label}`);
    check(sameDefinition(before, target.getPattern()), `Rejected ${label} changed the blank pattern`);
}
const [wrongType] = target.encodeCraftingPattern({ ...crafting, recipeId: stonecutting.recipeId });
const [wrongInputs] = target.encodeStonecuttingPattern({ ...stonecutting, input: item("minecraft:diamond") });
const [wrongSmithing] = target.encodeSmithingTablePattern({ ...smithing, addition: item("minecraft:stone") });
check(wrongType === null && wrongInputs === null && wrongSmithing === null && target.getPattern().state === "blank", "Invalid recipe-backed call consumed blank");
const [forgedOutput] = pcall(() => target.encodeCraftingPattern({ ...crafting, output: item("minecraft:diamond") } as typeof crafting));
check(!forgedOutput && target.getPattern().state === "blank", "Caller-supplied crafting output accepted");
const invalidDefinitions = [
    { inputs: [], outputs: [item("minecraft:stone")] },
    { inputs: [{ type: "item", name: "minecraft:not_an_item", count: 1 }], outputs: [item("minecraft:stone")] },
    { inputs: [{ type: "item", name: "minecraft:stone", count: -1 }], outputs: [item("minecraft:stone")] },
    { inputs: [item("minecraft:stone", "not SNBT")], outputs: [item("minecraft:stone")] },
    { inputs: [{ type: "item", name: "minecraft:stone", count: 1, nbt: "hash" }], outputs: [item("minecraft:stone")] },
    { inputs: [item("minecraft:stone", "[1,2]")], outputs: [item("minecraft:stone")] },
    { inputs: [item("minecraft:stone", "{a:".repeat(65) + "1" + "}".repeat(65))], outputs: [item("minecraft:stone")] },
    { inputs: [item("minecraft:stone", '{a:"' + "x".repeat(65536) + '"}')], outputs: [item("minecraft:stone")] },
    { inputs: [{ type: "item", name: "minecraft:stone", count: 1.5 }], outputs: [item("minecraft:stone")] },
    { inputs: [{ type: "item", name: "minecraft:stone", count: 9007199254740992 }], outputs: [item("minecraft:stone")] },
    { inputs: [item("minecraft:stone")], outputs: [item("minecraft:stone")], substitute: true },
];
for (const definition of invalidDefinitions) {
    const [ok] = pcall(() => target.encodeProcessingPattern(definition as AE2ProcessingDefinition));
    check(!ok && target.getPattern().state === "blank", "Malformed processing definition was accepted or consumed blank");
}
check(target.pushItems("right", 1, 64, 3) === 1, "Failed to extract blank");
const [empty] = target.clearPattern();
check(empty === null && target.getPattern().state === "empty", "Empty pedestal should not create blank");
for (const encode of encoders) { const [result] = encode(); check(result === null, "Empty pedestal encoded a pattern"); }
test.ok();
