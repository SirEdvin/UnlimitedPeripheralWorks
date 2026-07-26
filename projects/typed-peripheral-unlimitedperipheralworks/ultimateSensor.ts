import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { OperationApi } from "@siredvin/typed-peripheral-api/operations";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

export type SensorInspector =
    | "dimension"
    | "biome"
    | "weather"
    | "orientation"
    | "time"
    | "light"
    | "calendar"
    | "chunk";

/** @noSelf **/
export interface UltimateSensor extends OperationApi, ConfigurationAPI<object> {
    listAnalyzers(): string[];
    listInspectors(): SensorInspector[];
    analyze(name: "dimension"): string[];
    inspect(name: SensorInspector): any;
}

export const ultimateSensorProvider = new IPeripheralProvider<UltimateSensor>(
    "ultimate_sensor"
);
