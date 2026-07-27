import { ExtendedInventoryAPI } from "@siredvin/typed-peripheral-api/inventory_extended";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface ItemPedestal extends ExtendedInventoryAPI {}

export const itemPedestalProvider = new IPeripheralProvider<ItemPedestal>(
    "item_pedestal"
);
