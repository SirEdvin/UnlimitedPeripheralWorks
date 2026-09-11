import type { AE2CableAPI, AE2ImportBusObject } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";
import type { InventoryViewAPI } from "@siredvin/typed-peripheral-api/inventory_view";

/** @noSelf **/
interface TestApi { ok(marker?: string): void; }
declare const test: TestApi;

const check = (value: unknown, message: string): void => { if (!value) throw message; };
const item = (name: string) => ({ type: "item" as const, name });

const inventoryName = "right";
const itemInventory = peripheral.wrap(inventoryName) as InventoryViewAPI;
check(itemInventory, "Fixture inventory did not become available");

const cable = peripheral.wrap("front") as AE2CableAPI;
check(cable, "Fixture cable did not become available");
check(cable.getSides().length === 1 && cable.getSides()[0] === "south", "Cable did not report its supported side");
const [device, error] = cable.getSide("south");
check(device && !error, "Fixture import bus did not become available");
const target = device as AE2ImportBusObject;
check(target.getDeviceType() === "import_bus", `Expected import_bus, got ${target.getDeviceType()}`);
target.setFilter(1, item("minecraft:iron_ingot"));
const [fuzzyWithoutCard] = pcall(() => target.getFuzzyMode());
check(!fuzzyWithoutCard, "Fuzzy mode was available without a Fuzzy Card");
check(target.pullUpgrade(inventoryName, 4, 1, 2) === 1, "Fuzzy Card did not transfer into the Import Bus");
target.setFuzzyMode("percent_50");
const [validFuzzyMode, fuzzyModeError] = pcall(() => target.setFuzzyMode("invalid" as any));
check(!validFuzzyMode && `${fuzzyModeError}`.includes("ignore_all") && `${fuzzyModeError}`.includes("percent_25"), "Invalid fuzzy mode did not list valid values");
target.setRedstoneMode("high_signal");
check(target.getFilter(1)?.name === "minecraft:iron_ingot" && target.getFuzzyMode() === "percent_50" &&
    target.getRedstoneMode() === "high_signal", "Import Bus settings did not apply");
check(target.pullUpgrade(inventoryName, 1, 1, 1) === 1, "Import Bus upgrade did not transfer");
test.ok();
