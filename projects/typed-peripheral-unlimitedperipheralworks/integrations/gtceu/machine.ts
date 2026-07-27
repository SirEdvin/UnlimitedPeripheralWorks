import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface Machine extends ConfigurationAPI<object> {
    getRecipeTypes(): string[];
}

export const machineProvider = new IPeripheralProvider<Machine>(
    "gtceu:machine",
    () => null
);
