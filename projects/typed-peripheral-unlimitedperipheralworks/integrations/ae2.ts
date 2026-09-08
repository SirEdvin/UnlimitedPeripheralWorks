import { Fallible } from "../types";
import { ItemQuery } from "@siredvin/typed-peripheral-api/item_storage";
import {
    ExtendedItemDetail,
    IPeripheralProvider,
} from "@siredvin/typed-peripheral-base";

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
    /** Names are limited to 65535 modified-UTF-8 bytes. Item filters must fit both
     * the configured value limit (including table keys) and 65536 encoded NBT bytes.
     * Rejected registrations/replacements leave the existing definition unchanged. */
    subscribe(name: string, type: "item", filter?: ItemQuery): void;
    subscribe(name: string, type: "fluid", filter?: string): void;
    unsubscribe(name: string): boolean;
    getSubscriptions(): AE2StorageSubscription[];
}

/** @noSelf **/
export interface AE2CraftingAPI {
    getCraftingJob(jobId: string): Fallible<AE2CraftingJob>;
    getCraftingJobs(): AE2CraftingJob[];
    cancelCrafting(jobId: string): Fallible<boolean>;
    /** Yields while AE2 calculates without blocking the server tick. Access is rechecked
     * before submission; losing the link/range or changing networks rejects the request.
     * Standalone jobs return their output to network storage. Job IDs are process-local
     * and only retained while their native AE2 links remain reachable. */
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

export const ae2NetworkAccessProvider =
    new IPeripheralProvider<AE2NetworkAccessAPI>("ae2_network_access");

/** @noSelf **/
export interface AE2NetworkAccessAPI extends IPeripheral, AE2CraftingAPI, AE2StorageSubscriptionAPI {}

/** @noSelf **/
export interface AE2NetworkAPI extends IPeripheral, AE2CraftingAPI {
    getAverageEnergyDemand(): number;
    getAverageEnergyIncome(): number;
    getChannelEnergyDemand(): number;
    getChannelInformation(): { maxChannels?: number; usedChannels?: number };
    getCraftingCPUs(): AE2CraftingCPU[];
    getCraftableItems(): (Omit<ItemDetail, "count"> & { registryID: string })[];
    getCraftableFluids(): { name: string }[];
    getPatternsFor(mode: "item" | "fluid", id: string): AE2Pattern[];
    getActiveCraftings(): Fallible<AE2Crafting[]>;
}
