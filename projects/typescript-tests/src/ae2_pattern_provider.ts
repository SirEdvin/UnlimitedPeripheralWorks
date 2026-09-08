import type { AE2PatternProviderPeripheral } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";
import type { AE2PatternFluid, AE2PatternItem } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2";
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
// @ts-expect-error Legacy pattern providers deliberately have no subscription capability.
check(target.subscribe === undefined, "Legacy pattern provider exposed subscriptions");
check(target.getCraftingJobs().length === 0, "Disconnected pattern provider returned crafting jobs");
check(target.getDeviceType() === "pattern_provider", `Expected pattern_provider, got ${target.getDeviceType()}`);
target.setPriority(9);
target.setBlocking(true);
target.setVisibleInPatternAccessTerminal(false);
target.setPatternLockMode("lock_while_high");
target.setPushDirection("east");
check(target.getPriority() === 9 && target.isBlocking() && !target.isVisibleInPatternAccessTerminal() &&
    target.getPatternLockMode() === "lock_while_high" && target.getPushDirection() === "east", "pattern provider settings did not apply");
check(target.pullPattern(inventoryName, 6, 1, 1) === 1, "encoded pattern did not transfer into the provider");
const encodedPattern = target.getPattern(1);
check(encodedPattern?.pattern.inputs.length === 2 && encodedPattern.pattern.outputs.length === 2,
    "encoded pattern details are missing");
const pattern = encodedPattern!.pattern;
const itemInput = pattern.inputs[0] as AE2PatternItem;
const fluidInput = pattern.inputs[1] as AE2PatternFluid;
const itemOutput = pattern.outputs[0] as AE2PatternItem;
const fluidOutput = pattern.outputs[1] as AE2PatternFluid;
check(itemInput.type === "item" && itemInput.name === "minecraft:cobblestone" && itemInput.count === 2,
    "encoded pattern item input is wrong");
check(fluidInput.type === "fluid" && fluidInput.name === "minecraft:water" && fluidInput.amount === 1000,
    "encoded pattern fluid input is wrong");
check(itemOutput.type === "item" && itemOutput.name === "minecraft:stone" && itemOutput.count === 1,
    "encoded pattern item output is wrong");
check(fluidOutput.type === "fluid" && fluidOutput.name === "minecraft:lava" && fluidOutput.amount === 250,
    "encoded pattern fluid output is wrong");
check(target.pullPattern(inventoryName, 5, 1, 2) === 0 && itemInventory.list()[5]?.name === "minecraft:stone", "non-pattern transfer mutated its source");
check(target.pushPattern(inventoryName, 1, 1, 12) === 1, "encoded pattern did not transfer out of the provider");
test.ok();
