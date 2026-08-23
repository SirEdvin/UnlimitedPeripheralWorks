import type { AE2InterfacePeripheral } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";

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

const target = peripheral.wrap("front") as AE2InterfacePeripheral;
check(target, "Fixture interface did not become available");
check(target.getDeviceType() === "interface", `Expected interface, got ${target.getDeviceType()}`);
target.setStock(1, { ...item("minecraft:iron_ingot"), count: 32 });
check(target.getStock(1)?.target?.count === 32, "interface item stock did not apply");
target.setStock(2, { ...fluid("minecraft:water"), count: 1000 });
check(target.getStock(2)?.target?.count === 1000, "interface fluid stock did not apply");
target.setPriority(12);
check(target.pullUpgrade("right", 4, 1, 1) === 1, "Fuzzy Card did not transfer into the interface");
target.setFuzzyMode("percent_75");
check(target.getPriority() === 12 && target.getFuzzyMode() === "percent_75", "interface settings did not apply");
fails(() => target.setStock(1, { type: "item", name: "minecraft:not_a_real_item", count: 1 }), "unknown stock resource was accepted");
check(target.getStock(1)?.target?.count === 32, "invalid stock resource partially mutated the interface");
target.clearStock(1);
check(target.getStock(1) === null, "interface stock did not clear");
test.ok();
