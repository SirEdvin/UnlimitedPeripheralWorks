import * as events from "@siredvin/cc-events";
import { EMIIngredient } from "./base";
export declare const EMI_RECIPE_PASTE = "emi_recipe_paste";
export declare interface EmiRecipe {
    inputs: EMIIngredient[];
    outputs: EMIIngredient[];
    catalysts: EMIIngredient[];
    category: string;
    extra?: object;
    id: string;
}
export declare class EMIRecipePasteEvent extends events.BaseEvent<[EmiRecipe]> {
    getRecipe(): EmiRecipe;
    static create(recipe: EmiRecipe): EMIRecipePasteEvent;
}
