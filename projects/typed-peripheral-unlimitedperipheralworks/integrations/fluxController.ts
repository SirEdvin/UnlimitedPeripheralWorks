import { EnergyStorageAPI } from "@siredvin/typed-peripheral-api/energy_storage";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

export type FluxConnection = {
    customName: string;
    maxTransferLimit: number;
    transferBuffer: number;
    transferChange: number;
    surgeMode: boolean;
    deviceType: string;
    isChunkLoaded: boolean;
};
export type FluxNetwork = {
    name: string;
    id: number;
    color: number;
    securityLevel: string;
};
export type FluxStatistics = {
    controllerCount: number;
    pointCount: number;
    plugCount: number;
    storageCount: number;
    totalBuffer: number;
    totalEnergy: number;
    energyInput: number;
    energyOutput: number;
    averageTick: number;
};

/** @noSelf **/
export interface FluxController extends EnergyStorageAPI {
    getConnections(): FluxConnection[];
    getNetwork(): FluxNetwork;
    getStatistic(): FluxStatistics;
}

export const fluxControllerProvider = new IPeripheralProvider<FluxController>(
    "flux_controller"
);
