import { ItemStorageAPI } from "@siredvin/typed-peripheral-api/item_storage";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface OccultismStorage extends ItemStorageAPI {
    getMaxSlots(): number;
    getUsedSlots(): number;
    isBlacklisted(itemID: string): boolean;
}

/** @noSelf **/
export interface OccultismGoldenBowl extends IPeripheral {
    isBusy(): boolean;
    getCraftingInformation(): LuaTable<string, any> | null;
}

export const occultismProvider =
    new IPeripheralProvider<OccultismGoldenBowl>("occultism");
