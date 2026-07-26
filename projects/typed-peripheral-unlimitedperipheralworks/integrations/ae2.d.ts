import { Fallible } from "../types";
export type AE2CraftingCPU = {
    name?: string;
    capacity: number;
    storage: number;
    isBusy: boolean;
};
export type AE2Pattern = {
    inputs: object[];
    outputs: object[];
};
export type AE2Crafting = {
    target: object;
    amount: number;
    progress: number;
    CPU?: string;
};
/** @noSelf **/
export interface AE2NetworkAPI extends IPeripheral {
    getAverageEnergyDemand(): number;
    getAverageEnergyIncome(): number;
    getChannelEnergyDemand(): number;
    getChannelInformation(): {
        maxChannels?: number;
        usedChannels?: number;
    };
    getCraftingCPUs(): AE2CraftingCPU[];
    getCraftableItems(): (Omit<ItemDetail, "count"> & {
        registryID: string;
    })[];
    getCraftableFluids(): {
        name: string;
    }[];
    getPatternsFor(mode: "item" | "fluid", id: string): AE2Pattern[];
    getActiveCraftings(): Fallible<AE2Crafting[]>;
    scheduleCrafting(mode: "item" | "fluid", id: string, amount?: number, targetCPU?: string): LuaMultiReturn<[true] | [null, string] | [false, string, LuaTable<string, number>]>;
}
