import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { EntityDetail } from "../types";
/** @noSelf **/
export interface EasyTrader extends IPeripheral {
    hasVillager(): boolean;
    inspect(): EntityDetail | null;
}
/** @noSelf **/
export interface EasyAutoTrader extends EasyTrader {
    getSelectedOffer(): number;
    setSelectedOffer(index: number): Result;
}
export declare const easyTraderProvider: IPeripheralProvider<EasyTrader>;
export declare const easyAutoTraderProvider: IPeripheralProvider<EasyAutoTrader>;
