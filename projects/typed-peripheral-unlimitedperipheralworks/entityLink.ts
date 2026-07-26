import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { EntityDetail } from "./types";

/** @noSelf **/
export interface EntityLink extends IPeripheral {
    isEntityFound(): boolean;
    inspect(): EntityDetail | null;
}

export const entityLinkProvider = new IPeripheralProvider<EntityLink>(
    "entity_link"
);
