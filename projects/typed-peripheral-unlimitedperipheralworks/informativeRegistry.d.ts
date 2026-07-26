import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface InformativeRegistry extends ConfigurationAPI<object> {
    list(listName: "list" | "item" | "fluid" | "block" | "itemTags" | "fluidTags" | "blockTags" | "entityTypeTags" | "mods" | "entity"): string[];
    describe(listName: "itemTags" | "blockTags" | "fluidTags" | "entityTypeTags", tag: string): string[] | null;
    describe(listName: "item", id: string): (Omit<ItemDetail, "count"> & {
        registryID: string;
    }) | null;
    describe(listName: "fluid", id: string): {
        name: string;
        registryID: string;
    } | null;
    describe(listName: "list", id: string): string | null;
    describe(listName: "block" | "mods" | "entity", id: string): LuaTable | null;
}
export declare const informativeRegistryProvider: IPeripheralProvider<InformativeRegistry>;
