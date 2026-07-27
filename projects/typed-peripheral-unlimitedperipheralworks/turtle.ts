import { EnergyStorageAPI } from "@siredvin/typed-peripheral-api/energy_storage";
import { ExtendedInventoryAPI } from "@siredvin/typed-peripheral-api/inventory_extended";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface TweakedTurtle extends ExtendedInventoryAPI, EnergyStorageAPI {
    turnOn(): void;
    shutdown(): void;
    reboot(): void;
    getID(): number;
    isOn(): boolean;
    getLabel(): string | null;
}

export const tweakedTurtleProvider = new IPeripheralProvider<TweakedTurtle>(
    "turtle"
);
