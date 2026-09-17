import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { MachineInfo } from "./machine_info";

/** @noSelf **/
export interface Machine extends ConfigurationAPI<object> {
    getRecipeTypes(): string[];
    /** Read-only grouped snapshot. Unformed controllers omit energy/maintenance
     * sections which depend on connected hatches. Example:
     * const info = machine.getInfo();
     * if (info.maintenance?.hasProblems) print(info.maintenance.problemCount);
     */
    getInfo(): MachineInfo;
}

export const machineProvider = new IPeripheralProvider<Machine>(
    "gtceu:machine",
    () => null
);
