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
const detailed = terminal.items();
terminal.items(true);
const filtered = terminal.items(false, { name: "minecraft:stone" });
check(detailed[1]?.name === "minecraft:stone" && filtered[1]?.name === "minecraft:stone", "Stone was not listed");
const initialFuel = terminal.getFuelLevel();
check(terminal.pullItem("minecraft:stone", 5, 1) === 5, "Stone was not pulled into slot 1");
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
test.ok("returned");
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
