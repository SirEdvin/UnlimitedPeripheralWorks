import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { Position } from "./types";

export type RemoteObserverConfiguration = {
    maxRange: number;
    textStyle: string;
    boxStyle: string;
};

/** @noSelf **/
export interface RemoteObserver
    extends ConfigurationAPI<RemoteObserverConfiguration> {
    setTextStyle(value: string): Result;
    setBoxStyle(value: string): Result;
    addPosition(position: Position): Result;
    removePosition(position: Position): Result;
    getPositions(): Position[];
}

export const remoteObserverProvider = new IPeripheralProvider<RemoteObserver>(
    "remote_observer"
);
