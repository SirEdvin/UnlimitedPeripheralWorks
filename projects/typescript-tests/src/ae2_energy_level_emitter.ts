import type { AE2CableAPI, AE2EnergyLevelEmitterObject } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";

/** @noSelf **/
interface TestApi { ok(marker?: string): void; }
declare const test: TestApi;

const check = (value: unknown, message: string): void => { if (!value) throw message; };

const cable = peripheral.wrap("front") as AE2CableAPI;
check(cable, "Fixture cable did not become available");
const [device, error] = cable.getSide("south");
check(device && !error, "Fixture energy level emitter did not become available");
const target = device as AE2EnergyLevelEmitterObject;
check(target.getDeviceType() === "energy_level_emitter", `Expected energy_level_emitter, got ${target.getDeviceType()}`);
target.setThreshold(250);
target.setEmitterMode("high_signal");
check(target.getThreshold() === 250 && target.getEmitterMode() === "high_signal", "Energy Level Emitter settings did not apply");
test.ok();
