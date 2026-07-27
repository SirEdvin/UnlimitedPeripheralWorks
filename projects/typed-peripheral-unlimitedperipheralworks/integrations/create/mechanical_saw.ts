import { ExtendedInventoryAPI } from "@siredvin/typed-peripheral-api/inventory_extended";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { CreateFilterableAPI } from "./filterable_api";

/** @noSelf **/
export interface CreateMechanicalSaw
    extends ExtendedInventoryAPI,
        CreateFilterableAPI {}

export const mechanicalSawProvider =
    new IPeripheralProvider<CreateMechanicalSaw>(
        "create:mechanical_saw",
        () => null
    );
