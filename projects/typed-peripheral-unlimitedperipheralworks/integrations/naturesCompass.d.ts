import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
export type NatureCompassResult = {
    x: number;
    z: number;
    distance: number;
    biome: string;
};
/** @noSelf **/
export interface NaturesCompass extends ConfigurationAPI<object> {
    getBiomes(): string[];
    scheduleSearch(biome: string): Result;
    getState(): string;
    getResult(): NatureCompassResult | null;
}
export declare const naturesCompassProvider: IPeripheralProvider<NaturesCompass>;
