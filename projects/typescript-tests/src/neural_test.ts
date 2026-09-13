import { LootFabricator, NeuralModelItemDetail } from "../../typed-peripheral-unlimitedperipheralworks/integrations/hostileNetworks";

export function runNeuralTest(type: string): void {
    const [found] = peripheral.find(type);
    if (!found) throw `Missing ${type}`;
    const fab = found as LootFabricator;
    const entities = fab.getEntities();
    if (entities.length === 0) throw "No model entities";
    for (let i = 1; i < entities.length; i++) {
        if (entities[i - 1] >= entities[i]) throw "Entities not sorted and unique";
    }
    const entity = "minecraft:zombie";
    const loot = fab.getLoot(entity);
    if (loot.length < 2) throw "Missing ordered zombie loot";
    fab.setSelectedLoot(entity, null);
    if (fab.getSelectedLoot(entity) !== null) throw "Clear did not return nil";
    for (let i = 1; i <= loot.length; i++) {
        fab.setSelectedLoot(entity, i);
        if (fab.getSelectedLoot(entity) !== i) throw "Index mapping failed";
        if (!loot[i - 1].name || loot[i - 1].count < 1) throw "Invalid loot details";
    }
    fab.setSelectedLoot(entity, 2);
    for (const index of [0, -1, 0.5, loot.length + 1, math.huge, 0 / 0]) {
        const [ok] = pcall(() => fab.setSelectedLoot(entity, index));
        if (ok || fab.getSelectedLoot(entity) !== 2) throw "Invalid index mutated selection";
    }
    for (const invalid of ["zombie", "NOT:VALID!", "minecraft:missing", "minecraft:boat"]) {
        const [ok] = pcall(() => fab.setSelectedLoot(invalid, 1));
        if (ok || fab.getSelectedLoot(entity) !== 2) throw "Invalid entity accepted";
    }
    const name = peripheral.getName(found);
    const [wrongType] = pcall(() => peripheral.call(name, "setSelectedLoot", entity, "1"));
    if (wrongType) throw "Wrong type accepted";
    peripheral.call(name, "setSelectedLoot", entity);
    if (fab.getSelectedLoot(entity) !== null) throw "Omitted argument did not clear";
    fab.setSelectedLoot(entity, 2);

    const [inventory] = peripheral.find("inventory", (n: string) => n !== name);
    if (!inventory) throw "Missing model chest";
    const chest = peripheral.getName(inventory);
    for (let slot = 1; slot <= 4; slot++) {
        const [raw] = peripheral.call(chest, "getItemDetail", slot);
        const detail = raw as NeuralModelItemDetail;
        if (!detail || !detail.dataModel) throw `Missing model details in slot ${slot}`;
        const model = detail.dataModel;
        if (model.models.length !== (model.kind === "combined" ? 4 : 1)) throw "Wrong constituent count";
        if (model.progression.iterations !== 7) throw "Wrong iterations";
        if (model.progression.maxRank !== (slot % 2 === 0)) throw "Wrong maximum flag";
        if (model.progression.maxRank && (model.progression.nextTierData !== null || model.progression.remainingData !== null)) throw "Max tier not nullable";
    }
    for (let slot = 5; slot <= 8; slot++) {
        const [raw] = peripheral.call(chest, "getItemDetail", slot);
        const detail = raw as NeuralModelItemDetail;
        if (!detail || detail.dataModel || !detail.displayName) throw `Unsafe invalid model in slot ${slot}`;
    }
}
