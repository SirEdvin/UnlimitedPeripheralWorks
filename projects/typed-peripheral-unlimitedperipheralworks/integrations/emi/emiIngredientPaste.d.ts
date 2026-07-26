import * as events from "@siredvin/cc-events";
import { EMIFluidIngredient, EMIItemIngredient } from "./base";
export declare const EMI_INGREDIENT_PASTE = "emi_ingredient_paste";
export declare class EmiIngredientPasteEvent extends events.BaseEvent<[
    EMIItemIngredient | EMIFluidIngredient
]> {
    getIngridient(): EMIItemIngredient | EMIFluidIngredient;
    static create(ingredient: EMIItemIngredient | EMIFluidIngredient): EmiIngredientPasteEvent;
}
