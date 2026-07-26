import { FluidStorageAPI } from "@siredvin/typed-peripheral-api/fluid_storage";
import { ExtendedInventoryAPI } from "@siredvin/typed-peripheral-api/inventory_extended";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { CreateFilterableAPI } from "./filterable_api";
/** @noSelf **/
export interface CreateBasin extends ExtendedInventoryAPI, CreateFilterableAPI, FluidStorageAPI {
}
export declare const basinProvider: IPeripheralProvider<CreateBasin>;
