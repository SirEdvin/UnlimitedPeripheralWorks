import type { AE2CableAPI, AE2StorageLevelEmitterObject } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";
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

check(object.getDeviceType() === "storage_level_emitter", `Expected storage_level_emitter, got ${object.getDeviceType()}`);
    const target = object as AE2StorageLevelEmitterObject;
    target.setMonitoredResource(fluid("minecraft:water"));
    target.setThreshold(4000);
    target.setEmitterMode("high_signal");
    target.setCraftViaRedstone(true);
    check(target.getThreshold() === 4000 && target.getThresholdUnit() === "millibucket" && target.getEmitterMode() === "high_signal" &&
        target.shouldCraftViaRedstone(), "Storage Level Emitter settings did not apply");
    check(target.pullUpgrade(inventoryName, 3, 1, 1) === 1, "emitter upgrade did not transfer");
    check(target.pullUpgrade(inventoryName, 4, 1, 1) === 0 && itemInventory.list()[4]?.name === "ae2:fuzzy_card", "emitter accepted a second card");
test.ok();

