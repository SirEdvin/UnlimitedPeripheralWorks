import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { Machine } from "./machine";
import { Fallible } from "../../types";

/** @noSelf **/
export interface MultiblockMachine extends Machine {
    getPartNames(): Fallible<string[]>;
}

export const multiblockMachineProvider = new IPeripheralProvider<MultiblockMachine>(
    "gtceu:multiblock_machine",
    () => null
);
