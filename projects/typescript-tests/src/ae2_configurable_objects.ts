import type {
    AE2CableAPI,
    AE2EnergyLevelEmitterObject,
    AE2ExportBusObject,
    AE2FormationPlaneObject,
    AE2ImportBusObject,
    AE2InterfacePeripheral,
    AE2PatternProviderPeripheral,
    AE2StorageBusObject,
    AE2StorageLevelEmitterObject,
} from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";
import type { InventoryViewAPI } from "@siredvin/typed-peripheral-api/inventory_view";

/** @noSelf **/
interface TestApi {
    ok(marker?: string): void;
}
declare const test: TestApi;

const check = (value: unknown, message: string): void => {
    if (!value) throw message;
};
const fails = (action: (this: void) => void, message: string): void => {
    const [ok] = pcall(action);
    check(!ok, message);
};
const item = (name: string) => ({ type: "item" as const, name });
const fluid = (name: string) => ({ type: "fluid" as const, name });
const sides = ["top", "bottom", "left", "right", "front", "back"];
let inventory: InventoryViewAPI | undefined;
let inventoryName = "";
let object: any;
let seen = "";
for (let attempt = 0; attempt < 100; attempt++) {
    seen = "";
    for (const side of sides) {
        const wrapped = peripheral.wrap(side) as any;
        if (!wrapped) continue;
        seen += `${side}:${peripheral.getType(side)} `;
        if (wrapped.list) {
            inventory = wrapped as InventoryViewAPI;
            inventoryName = side;
        } else if (wrapped.getSide) {
            const cable = wrapped as AE2CableAPI;
            const [device] = cable.getSide("south");
            if (device) object = device;
        } else {
            object = wrapped;
        }
    }
    if (inventory && object) break;
    sleep(0.05);
}
check(inventory, "Fixture inventory did not become available");
check(object, `Fixture configurable object did not become available (${seen})`);
const itemInventory = inventory as InventoryViewAPI;

switch (object.getDeviceType()) {
    case "interface": {
        const target = object as AE2InterfacePeripheral;
        target.setStock(1, { ...item("minecraft:iron_ingot"), count: 32 });
        check(target.getStock(1)?.target?.count === 32, "interface item stock did not apply");
        target.setStock(2, { ...fluid("minecraft:water"), count: 1000 });
        check(target.getStock(2)?.target?.count === 1000, "interface fluid stock did not apply");
        target.setPriority(12);
        target.setFuzzyMode("percent_75");
        check(target.getPriority() === 12 && target.getFuzzyMode() === "percent_75", "interface settings did not apply");
        fails(() => target.setStock(1, { type: "item", name: "minecraft:not_a_real_item", count: 1 }), "unknown stock resource was accepted");
        check(target.getStock(1)?.target?.count === 32, "invalid stock resource partially mutated the interface");
        target.clearStock(1);
        check(target.getStock(1) === null, "interface stock did not clear");
        break;
    }
    case "pattern_provider": {
        const target = object as AE2PatternProviderPeripheral;
        target.setPriority(9);
        target.setBlocking(true);
        target.setVisibleInPatternAccessTerminal(false);
        target.setPatternLockMode("lock_while_high");
        target.setPushDirection("east");
        check(target.getPriority() === 9 && target.isBlocking() && !target.isVisibleInPatternAccessTerminal() &&
            target.getPatternLockMode() === "lock_while_high" && target.getPushDirection() === "east", "pattern provider settings did not apply");
        check(target.pullPattern(inventoryName, 6, 1, 1) === 1 && target.getPattern(1) !== null, "encoded pattern did not transfer into the provider");
        check(target.pullPattern(inventoryName, 5, 1, 2) === 0 && itemInventory.list()[5]?.name === "minecraft:stone", "non-pattern transfer mutated its source");
        check(target.pushPattern(inventoryName, 1, 1, 12) === 1, "encoded pattern did not transfer out of the provider");
        break;
    }
    case "import_bus": {
        const target = object as AE2ImportBusObject;
        target.setFilter(1, item("minecraft:iron_ingot"));
        target.setFuzzyMode("percent_50");
        target.setRedstoneMode("high_signal");
        check(target.getFilter(1)?.name === "minecraft:iron_ingot" && target.getFuzzyMode() === "percent_50" &&
            target.getRedstoneMode() === "high_signal", "Import Bus settings did not apply");
        check(target.pullUpgrade(inventoryName, 1, 1, 1) === 1, "Import Bus upgrade did not transfer");
        break;
    }
    case "export_bus": {
        const target = object as AE2ExportBusObject;
        target.setFilter(1, item("minecraft:iron_ingot"));
        target.setFuzzyMode("percent_50");
        target.setRedstoneMode("high_signal");
        target.setSchedulingMode("round_robin");
        check(target.getFilter(1)?.name === "minecraft:iron_ingot" && target.getFuzzyMode() === "percent_50" &&
            target.getRedstoneMode() === "high_signal" && target.getSchedulingMode() === "round_robin", "Export Bus settings did not apply");
        fails(() => target.setFilter(1, { ...item("minecraft:gold_ingot"), count: 1 } as any), "amount-bearing filter was accepted");
        check(target.getFilter(1)?.name === "minecraft:iron_ingot", "invalid filter partially mutated the Export Bus");
        check(target.pullUpgrade(inventoryName, 1, 1, 1) === 1 && target.getFilterSlotCount() === 27, "Capacity Card did not expand filters");
        target.setFilter(27, item("minecraft:diamond"));
        check(target.pullUpgrade(inventoryName, 3, 1, 2) === 1, "Crafting Card did not transfer into the Export Bus");
        target.setCraftOnly(true);
        target.setFilter(10, item("minecraft:gold_ingot"));
        check(target.isCraftOnly() && target.getFilter(10)?.name === "minecraft:gold_ingot", "craft-only slot 10 configuration failed");
        check(target.pullUpgrade(inventoryName, 5, 1, 3) === 0 && itemInventory.list()[5]?.name === "minecraft:stone", "invalid upgrade mutated its source");
        check(target.pushUpgrade(inventoryName, 1, 1, 10) === 1 && target.getFilterSlotCount() === 18, "Capacity Card removal did not shrink filters");
        check(target.pullUpgrade(inventoryName, 10, 1, 1) === 1 && target.getFilter(27) === null, "inactive filter was not cleared on shrink");
        break;
    }
    case "storage_bus": {
        const target = object as AE2StorageBusObject;
        target.setFilter(1, item("minecraft:cobblestone"));
        target.setPriority(7);
        target.setAccessMode("read");
        target.setStorageFilterMode("extractable_only");
        target.setFilterOnExtract(true);
        check(target.getFilter(1)?.name === "minecraft:cobblestone" && target.getPriority() === 7 && target.getAccessMode() === "read" &&
            target.getStorageFilterMode() === "extractable_only" && target.shouldFilterOnExtract(), "Storage Bus settings did not apply");
        break;
    }
    case "formation_plane": {
        const target = object as AE2FormationPlaneObject;
        target.setFilter(1, item("minecraft:stone"));
        target.setPriority(4);
        target.setPlaceBlocks(false);
        check(target.getFilter(1)?.name === "minecraft:stone" && target.getPriority() === 4 && !target.shouldPlaceBlocks(), "Formation Plane settings did not apply");
        break;
    }
    case "storage_level_emitter": {
        const target = object as AE2StorageLevelEmitterObject;
        target.setMonitoredResource(fluid("minecraft:water"));
        target.setThreshold(4000);
        target.setEmitterMode("high_signal");
        target.setCraftViaRedstone(true);
        check(target.getThreshold() === 4000 && target.getThresholdUnit() === "millibucket" && target.getEmitterMode() === "high_signal" &&
            target.shouldCraftViaRedstone(), "Storage Level Emitter settings did not apply");
        check(target.pullUpgrade(inventoryName, 3, 1, 1) === 1, "emitter upgrade did not transfer");
        check(target.pullUpgrade(inventoryName, 4, 1, 1) === 0 && itemInventory.list()[4]?.name === "ae2:fuzzy_card", "emitter accepted a second card");
        break;
    }
    case "energy_level_emitter": {
        const target = object as AE2EnergyLevelEmitterObject;
        target.setThreshold(250);
        target.setEmitterMode("high_signal");
        check(target.getThreshold() === 250 && target.getEmitterMode() === "high_signal", "Energy Level Emitter settings did not apply");
        break;
    }
    default:
        throw `Unexpected configurable object ${object.getDeviceType()}`;
}

test.ok();
