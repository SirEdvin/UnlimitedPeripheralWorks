import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { InventoryViewAPI } from "@siredvin/typed-peripheral-api/inventory_view";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface TransmutationTable
    extends ConfigurationAPI<object>,
        InventoryViewAPI {
    getAvailableItems(): (ItemDetail & { EMC: number })[];
    getEMC(): number;
    syntize(id: string, amount: number): Result;
    transmute(slot: number): Result;
}

export const transmutationTableProvider =
    new IPeripheralProvider<TransmutationTable>(
        "transmutation_table",
        () => null
    );
