import * as events from "@siredvin/cc-events";
import { EMIIngredient } from "./base";

export const EMI_RECIPE_PASTE = "emi_recipe_paste";

export declare interface EmiRecipe {
    inputs: EMIIngredient[];
    outputs: EMIIngredient[];
    catalysts: EMIIngredient[];
    category: string;
    extra?: object;
    id: string;
}

export class EMIRecipePasteEvent extends events.BaseEvent<[EmiRecipe]> {
    getRecipe(): EmiRecipe {
        return this.args[0];
    }

    public static create(recipe: EmiRecipe) {
        return new EMIRecipePasteEvent(EMI_RECIPE_PASTE, [recipe]);
    }
}

events.eventInitializers.set(
    EMI_RECIPE_PASTE,
    (name: string, args: [EmiRecipe]) => new EMIRecipePasteEvent(name, args)
);
