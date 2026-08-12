import type { AE2CableAPI, AE2StorageBusObject } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";

/** @noSelf **/
interface TestApi { ok(marker?: string): void; }
declare const test: TestApi;

const check = (value: unknown, message: string): void => { if (!value) throw message; };
const item = (name: string) => ({ type: "item" as const, name });

const cable = peripheral.wrap("front") as AE2CableAPI;
check(cable, "Fixture cable did not become available");
const [device, error] = cable.getSide("south");
check(device && !error, "Fixture storage bus did not become available");
const target = device as AE2StorageBusObject;
check(target.getDeviceType() === "storage_bus", `Expected storage_bus, got ${target.getDeviceType()}`);
target.setFilter(1, item("minecraft:cobblestone"));
target.setPriority(7);
target.setAccessMode("read");
target.setStorageFilterMode("extractable_only");
target.setFilterOnExtract(true);
check(target.getFilter(1)?.name === "minecraft:cobblestone" && target.getPriority() === 7 && target.getAccessMode() === "read" &&
    target.getStorageFilterMode() === "extractable_only" && target.shouldFilterOnExtract(), "Storage Bus settings did not apply");
test.ok();
