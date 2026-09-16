import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** GTCEu 7.4.1 native API, delegated without changing return values. @noSelf **/
export interface EnergyInfo extends ConfigurationAPI<object> {
    /** Stored EU and capacity; Lua numbers may lose precision for large values. */
    getEnergyStored(): number;
    getEnergyCapacity(): number;
    /** EU per second, not Forge Energy or EU per tick. */
    getInputPerSec(): number;
    getOutputPerSec(): number;
}

export const energyInfoProvider = new IPeripheralProvider<EnergyInfo>(
    "gtceu:energy_info",
    () => null
);
