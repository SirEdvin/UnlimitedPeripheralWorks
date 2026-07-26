import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { Machine } from "./machine";
import { Fallible } from "../../types";
/** @noSelf **/
export interface MultiblockMachine extends Machine {
    getPartNames(): Fallible<string[]>;
}
export declare const multiblockMachineProvider: IPeripheralProvider<MultiblockMachine>;
