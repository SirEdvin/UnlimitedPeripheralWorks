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

const inventoryName = "right";
const itemInventory = peripheral.wrap(inventoryName) as InventoryViewAPI;
check(itemInventory, "Fixture inventory did not become available");

const cable = peripheral.wrap("front") as AE2CableAPI;
check(cable, "Fixture cable did not become available");
const [device, error] = cable.getSide("south");
check(device && !error, "Fixture export bus did not become available");
const target = device as AE2ExportBusObject;
check(target.getDeviceType() === "export_bus", `Expected export_bus, got ${target.getDeviceType()}`);
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
