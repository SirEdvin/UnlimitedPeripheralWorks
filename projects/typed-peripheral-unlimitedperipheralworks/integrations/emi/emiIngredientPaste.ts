import * as events from "@siredvin/cc-events";
import { EMIFluidIngredient, EMIIngredient, EMIItemIngredient } from "./base";

export const EMI_INGREDIENT_PASTE = "emi_ingredient_paste";

export class EmiIngredientPasteEvent extends events.BaseEvent<
    [EMIItemIngredient | EMIFluidIngredient]
> {
    getIngridient(): EMIItemIngredient | EMIFluidIngredient {
        return this.args[0];
    }

    public static create(ingredient: EMIItemIngredient | EMIFluidIngredient) {
        return new EmiIngredientPasteEvent(EMI_INGREDIENT_PASTE, [ingredient]);
    }
}

events.eventInitializers.set(
    EMI_INGREDIENT_PASTE,
    (name: string, args: [EMIItemIngredient | EMIFluidIngredient]) =>
        new EmiIngredientPasteEvent(name, args)
);
