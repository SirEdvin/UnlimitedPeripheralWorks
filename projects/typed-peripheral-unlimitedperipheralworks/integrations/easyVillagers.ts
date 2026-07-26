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

export const easyTraderProvider = new IPeripheralProvider<EasyTrader>(
    "easy_trader"
);
export const easyAutoTraderProvider = new IPeripheralProvider<EasyAutoTrader>(
    "easy_auto_trader"
);
