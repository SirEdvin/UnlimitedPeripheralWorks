import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface Lantern extends IPeripheral {
    isEnabled(): boolean;
    toggle(): Result;
}
export declare const lanternProvider: IPeripheralProvider<Lantern>;
