import type { AE2CableAPI, AE2ExportBusObject } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";
import type { InventoryViewAPI } from "@siredvin/typed-peripheral-api/inventory_view";

/** @noSelf **/
interface TestApi { ok(marker?: string): void; }
declare const test: TestApi;

const check = (value: unknown, message: string): void => { if (!value) throw message; };
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
for (let attempt = 0; attempt < 100; attempt++) {
    for (const side of sides) {
        const wrapped = peripheral.wrap(side) as any;
        if (!wrapped) continue;
        if (wrapped.getSide) {
            const [device] = (wrapped as AE2CableAPI).getSide("south");
            if (device) object = device;
        } else if (wrapped.listStock || wrapped.listPatterns) {
            object = wrapped;
        } else if (wrapped.list) {
            inventory = wrapped as InventoryViewAPI;
            inventoryName = side;
        }
    }
    if (inventory && object) break;
    sleep(0.05);
}
check(inventory, "Fixture inventory did not become available");
check(object, "Fixture configurable object did not become available");
const itemInventory = inventory as InventoryViewAPI;

check(object.getDeviceType() === "export_bus", `Expected export_bus, got ${object.getDeviceType()}`);
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
test.ok();

