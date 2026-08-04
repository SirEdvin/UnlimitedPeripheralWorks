import type {
    AE2CableAPI,
    AE2EnergyLevelEmitterObject,
    AE2ExportBusObject,
    AE2FormationPlaneObject,
    AE2ImportBusObject,
    AE2InterfacePeripheral,
    AE2PatternProviderPeripheral,
    AE2StorageBusObject,
    AE2StorageLevelEmitterObject,
} from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects";
import type { InventoryViewAPI } from "@siredvin/typed-peripheral-api/inventory_view";

/** @noSelf **/
interface TestApi {
    ok(marker?: string): void;
}
declare const test: TestApi;

const check = (value: unknown, message: string): void => {
    if (!value) throw message;
};
const fails = (action: (this: void) => void, message: string): void => {
    const [ok] = pcall(action);
    check(!ok, message);
};
const item = (name: string) => ({ type: "item" as const, name });
const fluid = (name: string) => ({ type: "fluid" as const, name });
const sides = ["top", "bottom", "left", "right", "front", "back"];
let inventory: InventoryViewAPI | undefined;
let inventoryName = "";
let cable: AE2CableAPI | undefined;
let directInterface: AE2InterfacePeripheral | undefined;
let directProvider: AE2PatternProviderPeripheral | undefined;
let seen = "";
for (let attempt = 0; attempt < 100; attempt++) {
    seen = "";
    for (const side of sides) {
        const wrapped = peripheral.wrap(side) as any;
        if (!wrapped) continue;
        seen += `${side}:${peripheral.getType(side)} `;
        if (wrapped.getSide) cable = wrapped;
        else if (wrapped.listStock) directInterface = wrapped;
        else if (wrapped.listPatterns) directProvider = wrapped;
        else if (wrapped.list) {
            inventory = wrapped as InventoryViewAPI;
            inventoryName = side;
        }
    }
    if (inventory && cable && directInterface && directProvider) break;
    sleep(0.05);
}
const requirePeripheral = (value: unknown, name: string): void => {
    if (!value) throw `Fixture ${name} did not become available`;
};
requirePeripheral(inventory, "inventory");
requirePeripheral(cable, `cable (${seen})`);
requirePeripheral(directInterface, "Interface");
requirePeripheral(directProvider, "Pattern Provider");

const aeCable = cable as AE2CableAPI;
const aeInterface = directInterface as AE2InterfacePeripheral;
const aeProvider = directProvider as AE2PatternProviderPeripheral;
const itemInventory = inventory as InventoryViewAPI;

aeInterface.setStock(1, { ...item("minecraft:iron_ingot"), count: 32 });
const initialStock = aeInterface.getStock(1);
check(initialStock?.target?.count === 32, `direct interface stock callback returned ${textutils.serialize(initialStock)}`);
aeInterface.setStock(2, { ...fluid("minecraft:water"), count: 1000 });
check(aeInterface.getStock(2)?.target?.count === 1000, "fluid stock was not exposed as 1000 mB");
aeInterface.setPriority(12);
aeInterface.setFuzzyMode("percent_75");
check(aeInterface.getPriority() === 12 && aeInterface.getFuzzyMode() === "percent_75", "interface settings did not apply");
fails(
    () => aeInterface.setStock(1, { type: "item", name: "minecraft:not_a_real_item", count: 1 }),
    "unknown stock resource was accepted",
);
check(aeInterface.getStock(1)?.target?.count === 32, "invalid stock resource partially mutated the interface");
aeInterface.clearStock(1);
check(aeInterface.getStock(1) === null, "direct interface stock did not clear");

aeProvider.setPriority(9);
aeProvider.setBlocking(true);
aeProvider.setVisibleInPatternAccessTerminal(false);
aeProvider.setPatternLockMode("lock_while_high");
aeProvider.setPushDirection("east");
check(
    aeProvider.getPriority() === 9 && aeProvider.isBlocking() &&
        !aeProvider.isVisibleInPatternAccessTerminal() &&
        aeProvider.getPatternLockMode() === "lock_while_high" && aeProvider.getPushDirection() === "east",
    "direct pattern provider settings did not apply",
);
check(aeProvider.pullPattern(inventoryName, 6, 1, 1) === 1, "encoded pattern did not transfer into the provider");
check(aeProvider.getPattern(1) !== null, "encoded pattern slot was not updated");
check(aeProvider.pullPattern(inventoryName, 5, 1, 2) === 0, "non-pattern item transferred into the provider");
check(itemInventory.list()[5]?.name === "minecraft:stone", "rejected pattern mutated its source slot");
check(aeProvider.pushPattern(inventoryName, 1, 1, 12) === 1, "encoded pattern did not transfer out of the provider");

const [south, southError] = aeCable.getSide("south");
check(south && !southError, "south Export Bus was not resolved");
const exportBus = south as AE2ExportBusObject;
const [empty, emptyError] = aeCable.getSide("north");
check(empty === null && !!emptyError, "empty cable side did not return nil and an error");
fails(() => (aeCable as any).getSide("sideways"), "invalid cable direction was accepted");

exportBus.setFilter(1, item("minecraft:iron_ingot"));
exportBus.setFuzzyMode("percent_50");
exportBus.setRedstoneMode("high_signal");
exportBus.setSchedulingMode("round_robin");
check(
    exportBus.getFilter(1)?.name === "minecraft:iron_ingot" && exportBus.getFuzzyMode() === "percent_50" &&
        exportBus.getRedstoneMode() === "high_signal" && exportBus.getSchedulingMode() === "round_robin",
    "Export Bus callbacks did not apply",
);
fails(
    () => exportBus.setFilter(1, { ...item("minecraft:gold_ingot"), count: 1 } as any),
    "amount-bearing filter was accepted",
);
check(exportBus.getFilter(1)?.name === "minecraft:iron_ingot", "invalid filter partially mutated the Export Bus");
check(exportBus.pullUpgrade(inventoryName, 1, 1, 1) === 1, "Capacity Card did not transfer through the side object");
const expandedSlots = exportBus.getFilterSlotCount();
check(expandedSlots === 27, `Capacity Card produced ${expandedSlots} slots with ${textutils.serialize(exportBus.listUpgrades())}`);
exportBus.setFilter(27, item("minecraft:diamond"));
check(exportBus.pullUpgrade(inventoryName, 3, 1, 2) === 1, "Crafting Card did not transfer into the Export Bus");
exportBus.setCraftOnly(true);
exportBus.setFilter(10, item("minecraft:gold_ingot"));
check(exportBus.isCraftOnly() && exportBus.getFilter(10)?.name === "minecraft:gold_ingot", "craft-only slot 10 configuration failed");
check(exportBus.pullUpgrade(inventoryName, 5, 1, 3) === 0, "invalid upgrade card was accepted");
check(itemInventory.list()[5]?.name === "minecraft:stone", "rejected upgrade mutated its source slot");
check(exportBus.pushUpgrade(inventoryName, 1, 1, 10) === 1, "Capacity Card did not transfer out through originating computer access");
check(exportBus.getFilterSlotCount() === 18, "Capacity Card removal did not shrink active filters");
check(exportBus.pullUpgrade(inventoryName, 10, 1, 1) === 1, "Capacity Card could not be restored");
check(exportBus.getFilterSlotCount() === 27 && exportBus.getFilter(27) === null, "inactive filter was not cleared on shrink");

const [east] = aeCable.getSide("east");
const storageBus = east as AE2StorageBusObject;
storageBus.setFilter(1, item("minecraft:cobblestone"));
storageBus.setPriority(7);
storageBus.setAccessMode("read");
storageBus.setStorageFilterMode("extractable_only");
storageBus.setFilterOnExtract(true);
check(
    storageBus.getFilter(1)?.name === "minecraft:cobblestone" && storageBus.getPriority() === 7 &&
        storageBus.getAccessMode() === "read" && storageBus.getStorageFilterMode() === "extractable_only" &&
        storageBus.shouldFilterOnExtract(),
    "Storage Bus callbacks did not apply",
);

const [west] = aeCable.getSide("west");
const formationPlane = west as AE2FormationPlaneObject;
formationPlane.setFilter(1, item("minecraft:stone"));
formationPlane.setPriority(4);
formationPlane.setPlaceBlocks(false);
check(formationPlane.getPriority() === 4 && !formationPlane.shouldPlaceBlocks(), "Formation Plane callbacks did not apply");

const [up] = aeCable.getSide("up");
const storageEmitter = up as AE2StorageLevelEmitterObject;
storageEmitter.setMonitoredResource(fluid("minecraft:water"));
storageEmitter.setThreshold(4000);
storageEmitter.setEmitterMode("high_signal");
storageEmitter.setCraftViaRedstone(true);
check(
    storageEmitter.getThreshold() === 4000 && storageEmitter.getThresholdUnit() === "millibucket" &&
        storageEmitter.getEmitterMode() === "high_signal" && storageEmitter.shouldCraftViaRedstone(),
    "storage emitter fluid normalization or callbacks failed",
);
check(exportBus.pushUpgrade(inventoryName, 2, 1, 11) === 1, "Crafting Card could not be staged for the emitter");
check(storageEmitter.pullUpgrade(inventoryName, 11, 1, 1) === 1, "emitter upgrade transfer failed");
check(storageEmitter.pullUpgrade(inventoryName, 4, 1, 1) === 0, "emitter accepted a card beyond its one-slot limit");
check(itemInventory.list()[4]?.name === "ae2:fuzzy_card", "card-limit rejection mutated the source inventory");

const [down] = aeCable.getSide("down");
const energyEmitter = down as AE2EnergyLevelEmitterObject;
energyEmitter.setThreshold(250);
energyEmitter.setEmitterMode("high_signal");
check(energyEmitter.getThreshold() === 250 && energyEmitter.getEmitterMode() === "high_signal", "energy emitter callbacks did not apply");

test.ok("same-kind");
while (exportBus.getFilter(1) !== null) sleep(0.05);
exportBus.setFilter(1, item("minecraft:gold_ingot"));
check(exportBus.getFilter(1)?.name === "minecraft:gold_ingot", "side object did not operate on same-kind replacement");

test.ok("different-kind");
while (pcall(() => exportBus.getDeviceType())[0]) sleep(0.05);
const [replacement] = aeCable.getSide("south");
const importBus = replacement as AE2ImportBusObject;
check(importBus.getDeviceType() === "import_bus", "different-kind replacement was not visible through getSide");

test.ok("removed");
while (pcall(() => importBus.getDeviceType())[0]) sleep(0.05);
test.ok();
