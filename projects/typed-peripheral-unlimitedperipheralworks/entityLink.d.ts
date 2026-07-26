import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { EntityDetail } from "./types";
/** @noSelf **/
export interface EntityLink extends IPeripheral {
    isEntityFound(): boolean;
    inspect(): EntityDetail | null;
}
export declare const entityLinkProvider: IPeripheralProvider<EntityLink>;
