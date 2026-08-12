import type { AE2CableAPI, AE2FormationPlaneObject } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";

/** @noSelf **/
interface TestApi { ok(marker?: string): void; }
declare const test: TestApi;

const check = (value: unknown, message: string): void => { if (!value) throw message; };
const item = (name: string) => ({ type: "item" as const, name });

const cable = peripheral.wrap("front") as AE2CableAPI;
check(cable, "Fixture cable did not become available");
const [device, error] = cable.getSide("south");
check(device && !error, "Fixture formation plane did not become available");
const target = device as AE2FormationPlaneObject;
check(target.getDeviceType() === "formation_plane", `Expected formation_plane, got ${target.getDeviceType()}`);
target.setFilter(1, item("minecraft:stone"));
target.setPriority(4);
target.setPlaceBlocks(false);
check(target.getFilter(1)?.name === "minecraft:stone" && target.getPriority() === 4 && !target.shouldPlaceBlocks(), "Formation Plane settings did not apply");
test.ok();
