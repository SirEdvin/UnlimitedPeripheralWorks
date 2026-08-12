import type { AE2PatternProviderPeripheral } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";
import type { InventoryViewAPI } from "@siredvin/typed-peripheral-api/inventory_view";

/** @noSelf **/
interface TestApi { ok(marker?: string): void; }
declare const test: TestApi;

const check = (value: unknown, message: string): void => { if (!value) throw message; };

const inventoryName = "right";
const itemInventory = peripheral.wrap(inventoryName) as InventoryViewAPI;
check(itemInventory, "Fixture inventory did not become available");

const target = peripheral.wrap("front") as AE2PatternProviderPeripheral;
check(target, "Fixture pattern provider did not become available");
check(target.getDeviceType() === "pattern_provider", `Expected pattern_provider, got ${target.getDeviceType()}`);
target.setPriority(9);
target.setBlocking(true);
target.setVisibleInPatternAccessTerminal(false);
target.setPatternLockMode("lock_while_high");
target.setPushDirection("east");
check(target.getPriority() === 9 && target.isBlocking() && !target.isVisibleInPatternAccessTerminal() &&
    target.getPatternLockMode() === "lock_while_high" && target.getPushDirection() === "east", "pattern provider settings did not apply");
check(target.pullPattern(inventoryName, 6, 1, 1) === 1 && target.getPattern(1) !== null, "encoded pattern did not transfer into the provider");
check(target.pullPattern(inventoryName, 5, 1, 2) === 0 && itemInventory.list()[5]?.name === "minecraft:stone", "non-pattern transfer mutated its source");
check(target.pushPattern(inventoryName, 1, 1, 12) === 1, "encoded pattern did not transfer out of the provider");
test.ok();
