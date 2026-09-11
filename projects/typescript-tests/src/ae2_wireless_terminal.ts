import { ae2WirelessTerminalProvider } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2WirelessTerminal";
import { ae2NetworkAccessProvider } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2";

/** @noSelf **/
interface TestApi {
    ok(marker?: string): void;
}
declare const test: TestApi;

const check = (value: unknown, message: string): void => {
    if (!value) throw message;
};

const terminal = ae2WirelessTerminalProvider.findOrThrow();
const networkAccess = ae2NetworkAccessProvider.findOrThrow();
networkAccess.subscribe("stone", "item", { name: "minecraft:stone" });
networkAccess.subscribe("water", "fluid", "minecraft:water");
const subscriptions = networkAccess.getSubscriptions();
check(subscriptions[0]?.name === "stone" && subscriptions[1]?.name === "water", "Subscriptions were not listed by name");
networkAccess.subscribe("water", "fluid");
check(networkAccess.unsubscribe("missing") === false, "Unknown subscription was removed");
check(networkAccess.unsubscribe("water") === true, "Fluid subscription was not removed");
sleep(0.1);
const [missingJob, missingJobError] = networkAccess.getCraftingJob("missing");
check(missingJob === null && typeof missingJobError === "string" && missingJobError.includes("not found"), "Missing crafting job was not reported");
const [missingCancel, missingCancelError] = networkAccess.cancelCrafting("missing");
check(missingCancel === null && typeof missingCancelError === "string" && missingCancelError.includes("not found"), "Missing crafting job cancellation was not reported");
networkAccess.getCraftingJobs();
const [invalidCraft, invalidCraftError] = networkAccess.scheduleCrafting("item", "minecraft:stone", 0);
check(invalidCraft === null && invalidCraftError.includes("positive"), "Invalid crafting amount was accepted");
const detailed = terminal.items();
terminal.items(true);
const filtered = terminal.items(false, { name: "minecraft:stone" });
check(detailed[1]?.name === "minecraft:stone" && filtered[1]?.name === "minecraft:stone", "Stone was not listed");
const initialFuel = terminal.getFuelLevel();
check(terminal.pullItem("minecraft:stone", 5, 1) === 5, "Stone was not pulled into slot 1");
const [, subscriptionName, resource, previousAmount, currentAmount] = os.pullEvent("ae2_storage_change");
check(subscriptionName === "stone" && resource.type === "item" && resource.name === "minecraft:stone", "Unexpected storage change resource");
check(previousAmount === 64 && currentAmount === 59, "Unexpected storage change amounts");
check(terminal.pullItem("minecraft:dirt") === 0, "Missing dirt unexpectedly moved");
check(terminal.pushItem(16, 3) === 3, "Gold was not pushed from slot 16");
check(terminal.getFuelLevel() === initialFuel - 3, "Transfers did not consume one fuel each");
const fuelAfterTransfers = terminal.getFuelLevel();
check(!pcall(() => terminal.pushItem(17))[0], "Invalid slot was accepted");
const beforeInvalidStone = terminal.items(false, { name: "minecraft:stone" })[1]?.count;
check(!pcall(() => terminal.pushItem(0, 1))[0], "Source slot zero was accepted");
check(!pcall(() => terminal.pullItem("minecraft:stone", 1, 0))[0], "Destination slot zero was accepted");
check(terminal.items(false, { name: "minecraft:stone" })[1]?.count === beforeInvalidStone &&
    terminal.items(false, { name: "minecraft:gold_ingot" })[1]?.count === 3, "Invalid slots changed network inventory");
check(terminal.getFuelLevel() === fuelAfterTransfers, "Invalid slot consumed fuel");
terminal.items();
check(terminal.getFuelLevel() === fuelAfterTransfers, "Item listing consumed fuel");
check(terminal.getFuelMaxLevel() >= terminal.getFuelLevel(), "Fuel exceeded its maximum");
check(terminal.getFuelConsumptionRate() === 1, "Unexpected fuel consumption rate");
terminal.setFuelConsumptionRate(1);
test.ok("initial");
while (terminal.getFuelLevel() !== 0) sleep(0.05);
check(!pcall(() => terminal.pushItem("minecraft:dirt"))[0], "Transfer succeeded without fuel");
check(terminal.getFuelLevel() === 0, "Failed transfer changed fuel");
test.ok("empty-fuel");
while (terminal.getFuelLevel() === 0) sleep(0.05);
check(terminal.pushItem("minecraft:dirt") === 0, "Valid zero-move transfer returned items");
check(terminal.getFuelLevel() === 1, "Fuel-disabled transfer consumed fuel");
test.ok("disabled");
while (terminal.getFuelLevel() === 1) sleep(0.05);
check(terminal.pushItem("minecraft:dirt") === 0, "Valid zero-move transfer returned items");
check(terminal.getFuelLevel() === 1, "Valid zero-move transfer did not consume fuel");
test.ok("restored");
let failure = "";
while (!failure.includes("outside wireless range")) {
    const [ok, error] = pcall(() => terminal.items());
    failure = ok ? "" : `${error}`;
    if (!failure) sleep(0.05);
}
test.ok("out-of-range");
while (!pcall(() => terminal.items())[0]) sleep(0.05);
const baselineTimer = os.startTimer(0.1);
while (true) {
    const event = os.pullEvent();
    check(event[0] !== "ae2_storage_change", "Range recovery replayed a storage change");
    if (event[0] === "timer" && event[1] === baselineTimer) break;
}
test.ok("returned");
const [, recoveredName, recoveredResource, recoveredOld, recoveredNew] = os.pullEvent("ae2_storage_change");
check(recoveredName === "stone" && recoveredResource.name === "minecraft:stone", "Storage event was not restored after range recovery");
check(recoveredOld === 60 && recoveredNew === 61, "Range recovery did not use the new baseline");
test.ok("post-return-change");
failure = "";
while (!failure.includes("outside wireless range")) {
    const [ok, error] = pcall(() => terminal.items());
    failure = ok ? "" : `${error}`;
    if (!failure) sleep(0.05);
}
test.ok("inactive");
while (!pcall(() => terminal.items())[0]) sleep(0.05);
test.ok("reactivated");
while (!terminal.items(false, { name: "minecraft:dirt" })[1]) sleep(0.05);
const [scheduledCancel, cancelId, missingInputs] = networkAccess.scheduleCrafting("item", "minecraft:diamond", 1);
check(scheduledCancel === true, `Crafting submission failed: ${cancelId}; missing=${textutils.serialize(missingInputs)}; storage=${textutils.serialize(terminal.items())}`);
const [runningCancel] = networkAccess.getCraftingJob(cancelId);
check(runningCancel?.state === "running" && runningCancel.amount === 1, "Submitted crafting job was not tracked");
const [canceled] = networkAccess.cancelCrafting(cancelId);
check(canceled === true && networkAccess.getCraftingJob(cancelId)[0]?.state === "canceled", "Crafting job was not canceled");
test.ok("crafting-canceled");
// A persistent marker cannot be dropped while test.ok is awaiting its task_complete event.
while (!terminal.items(false, { name: "minecraft:iron_nugget" })[1]) sleep(0.05);
const [scheduledComplete, completeId] = networkAccess.scheduleCrafting("item", "minecraft:diamond", 1);
check(scheduledComplete === true, `Second crafting submission failed: ${completeId}`);
test.ok("crafting-running");
while (networkAccess.getCraftingJob(completeId)[0]?.state !== "done") sleep(0.05);
const [cancelDone] = networkAccess.cancelCrafting(completeId);
check(cancelDone === false, "Completed crafting job was canceled");
test.ok("crafting-complete");
failure = "";
while (!failure.includes("unavailable")) {
    const [ok, error] = pcall(() => terminal.items());
    failure = ok ? "" : `${error}`;
    if (!failure) sleep(0.05);
}
test.ok();
