import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface Machine extends ConfigurationAPI<object> {
    getRecipeTypes(): string[];
}
export declare const machineProvider: IPeripheralProvider<Machine>;
