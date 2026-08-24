import { FuelApi } from "@siredvin/typed-peripheral-api/fuel";
import { ItemQuery } from "@siredvin/typed-peripheral-api/item_storage";
import {
    ExtendedItemDetail,
    IPeripheralProvider,
    ShortItemDetail,
} from "@siredvin/typed-peripheral-base";
import { AE2NetworkAccessAPI } from "./ae2";

/** @noSelf **/
export interface AE2WirelessTerminalAPI extends FuelApi, AE2NetworkAccessAPI {
    items(): LuaTable<number, ExtendedItemDetail>;
    items(
        detailed: true,
        filter?: ItemQuery
    ): LuaTable<number, ExtendedItemDetail>;
    items(
        detailed: false,
        filter?: ItemQuery
    ): LuaTable<number, ShortItemDetail>;
    pullItem(itemQuery?: ItemQuery, limit?: number, toSlot?: number): number;
    pushItem(fromSlotOrItemQuery?: number | ItemQuery, limit?: number): number;
}

export const ae2WirelessTerminalProvider =
    new IPeripheralProvider<AE2WirelessTerminalAPI>("ae2_wireless_terminal");
