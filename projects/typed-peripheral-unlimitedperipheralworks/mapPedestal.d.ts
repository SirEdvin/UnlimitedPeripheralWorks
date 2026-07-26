import { ExtendedInventoryAPI } from "@siredvin/typed-peripheral-api/inventory_extended";
import { OperationApi } from "@siredvin/typed-peripheral-api/operations";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { Position } from "./types";
export type MapData = {
    colors: number[];
    scale: number;
    banners: {
        name?: string;
        pos: Position;
        color: string;
    }[];
};
/** @noSelf **/
export interface MapPedestal extends ExtendedInventoryAPI, OperationApi {
    getData(): MapData;
    updateData(): Result;
}
export declare const mapPedestalProvider: IPeripheralProvider<MapPedestal>;
