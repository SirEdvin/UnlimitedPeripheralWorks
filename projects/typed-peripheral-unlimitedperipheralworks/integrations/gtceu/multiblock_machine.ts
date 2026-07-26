import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { Machine } from "./machine";

/** @noSelf **/
export interface MultiblockMachine extends Machine {
    getPartNames(): string[];
}

export const multiblockMachineProvider = new IPeripheralProvider<MultiblockMachine>(
    "gtceu:multiblock_machine",
    () => null
);
