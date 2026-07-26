import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { BlockState, Position } from "./types";
export type RealityFlags = {
    playerPassable?: boolean;
    lightPassable?: boolean;
    skyLightPassable?: boolean;
    invisible?: boolean;
    lightLevel?: number;
};
export type RealityBatch = [Position[], BlockState, RealityFlags?];
/** @noSelf **/
export interface RealityForger extends ConfigurationAPI<{
    interactionRadius: number;
}> {
    detectAnchors(): Position[];
    clearAnchors(positions?: Position[]): Result;
    forgeReality(blockState: BlockState, flags?: RealityFlags): Result;
    forgeRealityPieces(positions: Position[], blockState: BlockState, flags?: RealityFlags): Result;
    batchForgeRealityPieces(batch: RealityBatch[]): Result;
}
export declare const realityForgerProvider: IPeripheralProvider<RealityForger>;
