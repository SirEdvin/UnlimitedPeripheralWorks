import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { EmiRecipe } from "./integrations/emi/emiRecipePaste";
/** @noSelf **/
export interface RecipeRegistry extends ConfigurationAPI<object> {
    getTypes(): string[];
    get(recipeID: string): LuaTable<string, any> | null;
    getEMI(recipeID: string): EmiRecipe | null;
    getRaw(recipeID: string): LuaTable<string, any> | null;
    list(types: string | string[]): string[];
    search(itemID: string, types: string | string[]): LuaTable<string, any>[];
}
export declare const recipeRegistryProvider: IPeripheralProvider<RecipeRegistry>;
