import { ItemStorageAPI } from "@siredvin/typed-peripheral-api/item_storage";
/** @noSelf **/
export interface OccultismStorage extends ItemStorageAPI {
    getMaxSlots(): number;
    getUsedSlots(): number;
    isBlacklisted(itemID: string): boolean;
}
/** @noSelf **/
export interface OccultismGoldenBowl extends IPeripheral {
    isBusy(): boolean;
    getCraftingInformation(): {
        pentacle: string;
        ritual: string;
        itemUseFulfilled: boolean;
        sacrificeFulfilled: boolean;
        leftTime: number;
    } | null;
}
