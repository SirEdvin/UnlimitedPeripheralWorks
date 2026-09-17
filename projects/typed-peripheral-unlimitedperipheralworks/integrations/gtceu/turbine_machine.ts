import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** Turbine reads backed by GTCEu 7.0.2's native machine/rotor APIs. @noSelf **/
export interface TurbineMachine extends ConfigurationAPI<object> {
    hasRotor(): boolean;
    getRotorSpeed(): number;
    getMaxRotorHolderSpeed(): number;
    getTotalEfficiency(): number;
    /** Active recipe's nominal output EU/t (also while waiting/suspended); zero when inactive. */
    getCurrentProduction(): number;
    getOverclockVoltage(): number;
    getRotorDurabilityPercent(): number;
}

export const turbineMachineProvider = new IPeripheralProvider<TurbineMachine>(
    "gtceu:turbine_machine",
    () => null
);
