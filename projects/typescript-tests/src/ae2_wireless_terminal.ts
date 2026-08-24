import { ae2WirelessTerminalProvider } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/ae2WirelessTerminal";

/** @noSelf **/
interface TestApi {
    ok(marker?: string): void;
}
declare const test: TestApi;

const check = (value: unknown, message: string): void => {
    if (!value) throw message;
};

const terminal = ae2WirelessTerminalProvider.findOrThrow();
terminal.subscribe("stone", "item", { name: "minecraft:stone" });
terminal.subscribe("water", "fluid", "minecraft:water");
const subscriptions = terminal.getSubscriptions();
check(subscriptions[0]?.name === "stone" && subscriptions[1]?.name === "water", "Subscriptions were not listed by name");
terminal.subscribe("water", "fluid");
check(terminal.unsubscribe("missing") === false, "Unknown subscription was removed");
check(terminal.unsubscribe("water") === true, "Fluid subscription was not removed");
sleep(0.1);
const [missingJob, missingJobError] = terminal.getCraftingJob("missing");
check(missingJob === null && typeof missingJobError === "string" && missingJobError.includes("not found"), "Missing crafting job was not reported");
const [missingCancel, missingCancelError] = terminal.cancelCrafting("missing");
check(missingCancel === null && typeof missingCancelError === "string" && missingCancelError.includes("not found"), "Missing crafting job cancellation was not reported");
terminal.getCraftingJobs();
const typecheckCraftingRequest = (): void => {
    const [scheduled, jobId] = terminal.scheduleCrafting("item", "minecraft:stone", 1);
    if (scheduled) terminal.getCraftingJob(jobId);
};
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
failure = "";
while (!failure.includes("unavailable")) {
    const [ok, error] = pcall(() => terminal.items());
    failure = ok ? "" : `${error}`;
    if (!failure) sleep(0.05);
}
test.ok();
