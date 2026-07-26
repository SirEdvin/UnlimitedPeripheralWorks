import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface Lantern extends IPeripheral {
    isEnabled(): boolean;
    toggle(): Result;
}

export const lanternProvider = new IPeripheralProvider<Lantern>("lantern");
