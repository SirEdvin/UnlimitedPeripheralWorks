import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { ExtendedInventoryAPI } from "@siredvin/typed-peripheral-api/inventory_extended";
import { FluidStorageAPI } from "@siredvin/typed-peripheral-api/fluid_storage";
import { ItemStorageAPI } from "@siredvin/typed-peripheral-api/item_storage";
import { EntityDetail } from "./types";
import {
    AutomobilityAutomobileAPI,
    ManaAndArtificeConstructAPI,
} from "./integrations/entityLink";

/** @noSelf **/
export interface EntityLink
    extends IPeripheral,
        Partial<ManaAndArtificeConstructAPI>,
        Partial<AutomobilityAutomobileAPI>,
        Partial<ExtendedInventoryAPI>,
        Partial<ItemStorageAPI>,
        Partial<FluidStorageAPI> {
    isEntityFound(): boolean;
    inspect(): EntityDetail | null;
}

export const entityLinkProvider = new IPeripheralProvider<EntityLink>(
    "entity_link"
);
