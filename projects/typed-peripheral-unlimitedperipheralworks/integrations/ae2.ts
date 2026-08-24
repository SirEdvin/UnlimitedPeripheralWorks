import { Fallible } from "../types";
import { ItemQuery } from "@siredvin/typed-peripheral-api/item_storage";
import { ExtendedItemDetail } from "@siredvin/typed-peripheral-base";

export type AE2CraftingCPU = {
    name?: string;
    capacity: number;
    storage: number;
    isBusy: boolean;
};
export type AE2PatternItem = ItemDetail & { type: "item" };
export type AE2PatternFluid = {
    type: "fluid";
    name: string;
    amount: number;
    precise_amount: number;
    nbt?: string;
};
export type AE2PatternStack = AE2PatternItem | AE2PatternFluid;
export type AE2PatternInput = AE2PatternStack | { variants: AE2PatternStack[] };
export type AE2Pattern = { inputs: AE2PatternInput[]; outputs: AE2PatternStack[] };
export type AE2Crafting = {
    target: object;
    amount: number;
    progress: number;
    CPU?: string;
};
export type AE2CraftingJob = {
    id: string;
    state: "running" | "done" | "canceled";
    target: object;
    amount: number;
};
export type AE2StorageSubscription =
    | { name: string; type: "item"; filter?: ItemQuery }
    | { name: string; type: "fluid"; filter?: string };
export type AE2StorageSubscriptionResource =
    | (Omit<ExtendedItemDetail, "count"> & { type: "item" })
    | { type: "fluid"; name: string };

/** @noSelf **/
export interface AE2StorageSubscriptionAPI {
    subscribe(name: string, type: "item", filter?: ItemQuery): void;
    subscribe(name: string, type: "fluid", filter?: string): void;
    unsubscribe(name: string): boolean;
    getSubscriptions(): AE2StorageSubscription[];
}

/** @noSelf **/
export interface AE2NetworkAPI extends IPeripheral, AE2StorageSubscriptionAPI {
    getAverageEnergyDemand(): number;
    getAverageEnergyIncome(): number;
    getChannelEnergyDemand(): number;
    getChannelInformation(): { maxChannels?: number; usedChannels?: number };
    getCraftingCPUs(): AE2CraftingCPU[];
    getCraftableItems(): (Omit<ItemDetail, "count"> & { registryID: string })[];
    getCraftableFluids(): { name: string }[];
    getPatternsFor(mode: "item" | "fluid", id: string): AE2Pattern[];
    getActiveCraftings(): Fallible<AE2Crafting[]>;
    getCraftingJob(jobId: string): Fallible<AE2CraftingJob>;
    getCraftingJobs(): AE2CraftingJob[];
    cancelCrafting(jobId: string): Fallible<boolean>;
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
}
