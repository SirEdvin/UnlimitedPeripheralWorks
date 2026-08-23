import type { AE2CableAPI, AE2StorageLevelEmitterObject } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";
import type { InventoryViewAPI } from "@siredvin/typed-peripheral-api/inventory_view";

/** @noSelf **/
interface TestApi { ok(marker?: string): void; }
declare const test: TestApi;

const check = (value: unknown, message: string): void => { if (!value) throw message; };
const fluid = (name: string) => ({ type: "fluid" as const, name });

const inventoryName = "right";
const itemInventory = peripheral.wrap(inventoryName) as InventoryViewAPI;
check(itemInventory, "Fixture inventory did not become available");

const cable = peripheral.wrap("front") as AE2CableAPI;
check(cable, "Fixture cable did not become available");
const [device, error] = cable.getSide("south");
check(device && !error, "Fixture storage level emitter did not become available");
const target = device as AE2StorageLevelEmitterObject;
check(target.getDeviceType() === "storage_level_emitter", `Expected storage_level_emitter, got ${target.getDeviceType()}`);
target.setMonitoredResource(fluid("minecraft:water"));
target.setThreshold(4000);
target.setEmitterMode("high_signal");
target.setCraftViaRedstone(true);
check(target.getThreshold() === 4000 && target.getThresholdUnit() === "millibucket" && target.getEmitterMode() === "high_signal" &&
    target.shouldCraftViaRedstone(), "Storage Level Emitter settings did not apply");
check(target.pullUpgrade(inventoryName, 3, 1, 1) === 1, "emitter upgrade did not transfer");
check(target.pullUpgrade(inventoryName, 4, 1, 1) === 0 && itemInventory.list()[4]?.name === "ae2:fuzzy_card", "emitter accepted a second card");
test.ok();
