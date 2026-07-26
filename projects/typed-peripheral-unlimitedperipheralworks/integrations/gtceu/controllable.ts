import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface ControllableMachine extends ConfigurationAPI<object> {
    isWorkingEnabled(): boolean;
    setWorkingEnabled(value: boolean): void;
    setSuspendAfterFinish(value: boolean): void;
}

export const controllableProvider = new IPeripheralProvider<ControllableMachine>(
    "gtceu:controllable",
    () => null
);
export const workableProvider = controllableProvider;
