import { Fallible } from "../types";
import { AE2CraftingJob } from "./ae2";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface AE2CraftingMonitorAPI extends IPeripheral {
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

export const ae2CraftingMonitorProvider =
    new IPeripheralProvider<AE2CraftingMonitorAPI>("ae2_crafting_monitor");
