import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface WorkableMachine extends ConfigurationAPI<object> {
    isActive(): boolean;
}

export const workableProvider = new IPeripheralProvider<WorkableMachine>(
    "gtceu:workable",
    () => null
);
