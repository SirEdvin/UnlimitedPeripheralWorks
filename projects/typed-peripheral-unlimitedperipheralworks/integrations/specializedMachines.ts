import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface ModernIndustrializationCrafter extends IPeripheral {
    isBusy(): boolean;
    getCraftingInformation():
        | {
              progress: number;
              currentEfficiency: number;
              maxEfficiency: number;
              baseRecipeCost: number;
              currentRecipeCost: number;
          }
        | object;
}

/** @noSelf **/
export interface AlloyForgeryController extends IPeripheral {
    inspect(): {
        fuel: number;
        currentSmeltTime: number;
        smeltProgress: number;
    };
}

export const miCrafterProvider =
    new IPeripheralProvider<ModernIndustrializationCrafter>("mi_crafter");
export const alloyForgeryProvider =
    new IPeripheralProvider<AlloyForgeryController>("alloy_forgery");
