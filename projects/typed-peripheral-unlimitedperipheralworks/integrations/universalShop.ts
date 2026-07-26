import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

export type ShopItemSetting = { type: string; price?: ItemDetail };

/** @noSelf **/
export interface UniversalShop extends IPeripheral {
    getHologramMode(): string;
    setHologramMode(mode: string): void;
    getPrice(): ShopItemSetting;
    setPrice(type: "free" | "single_item", itemHint?: object): Result;
    getStock(): ShopItemSetting;
    setStock(type: "selected_item" | "single_item", itemHint?: object): Result;
}

export const universalShopProvider = new IPeripheralProvider<UniversalShop>(
    "universal_shop"
);
