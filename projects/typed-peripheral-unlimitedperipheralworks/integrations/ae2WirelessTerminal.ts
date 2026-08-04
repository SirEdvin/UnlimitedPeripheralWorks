import { FuelApi } from "@siredvin/typed-peripheral-api/fuel";
import { ItemQuery } from "@siredvin/typed-peripheral-api/item_storage";
import { Fallible } from "../types";
import { AE2CraftingJob } from "./ae2";
import {
    ExtendedItemDetail,
    IPeripheralProvider,
    ShortItemDetail,
} from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface AE2WirelessTerminalAPI extends FuelApi {
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
    scheduleCrafting(
        mode: "item" | "fluid",
        id: string,
        amount?: number,
        targetCPU?: string
    ): LuaMultiReturn<
        | [true, string]
        | [null, string]
        | [false, string, LuaTable<string, number>]
    >;
    getCraftingJob(jobId: string): Fallible<AE2CraftingJob>;
    getCraftingJobs(): AE2CraftingJob[];
    cancelCrafting(jobId: string): Fallible<boolean>;
}

export const ae2WirelessTerminalProvider =
    new IPeripheralProvider<AE2WirelessTerminalAPI>("ae2_wireless_terminal");
