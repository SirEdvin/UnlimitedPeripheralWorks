import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** GTCEu 7.4.1 native API, delegated without changing return values. @noSelf **/
export interface TurbineMachine extends ConfigurationAPI<object> {
    hasRotor(): boolean;
    getRotorSpeed(): number;
    getMaxRotorHolderSpeed(): number;
    getTotalEfficiency(): number;
    getCurrentProduction(): number;
    getOverclockVoltage(): number;
    getRotorDurabilityPercent(): number;
}

export const turbineMachineProvider = new IPeripheralProvider<TurbineMachine>(
    "gtceu:turbine_machine",
    () => null
);
