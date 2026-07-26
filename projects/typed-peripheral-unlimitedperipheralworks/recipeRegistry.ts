import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { EmiRecipe } from "./integrations/emi/emiRecipePaste";

/** @noSelf **/
export interface RecipeRegistry extends ConfigurationAPI<object> {
    get(recipeID: string): LuaTable<string, any> | null;
    getEMI(recipeID: string): EmiRecipe | null;
    getRaw(recipeID: string): LuaTable<string, any> | null;
}

export const recipeRegistryProvider = new IPeripheralProvider<RecipeRegistry>(
    "recipe_registry",
    () => null
);
