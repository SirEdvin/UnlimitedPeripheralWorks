import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { FluidDetail, IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface InformativeRegistry extends ConfigurationAPI<object> {
    list(
        listName:
            | "list"
            | "item"
            | "fluid"
            | "block"
            | "itemTags"
            | "fluidTags"
            | "blockTags"
    ): string[];
    describe(
        listName: "itemTags" | "blockTags" | "fluidTags",
        tag: string
    ): string[];
    describe(listName: "item", id: string): ItemDetail;
    describe(listName: "fluid", id: string): FluidDetail;
    describe(listName: "list" | "block", id: string): LuaTable;
}

export const informativeRegistryProvider =
    new IPeripheralProvider<InformativeRegistry>(
        "informative_registry",
        () => null
    );
