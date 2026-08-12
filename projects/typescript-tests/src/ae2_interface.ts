import type { AE2CableAPI, AE2InterfacePeripheral } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";
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

check(object.getDeviceType() === "interface", `Expected interface, got ${object.getDeviceType()}`);
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
test.ok();

