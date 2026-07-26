import { Position } from "../types";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface ManaAndArtificeConstructAPI extends IPeripheral {
    move(position: Position): Result;
    store(position: Position, direction?: string): Result;
    take(
        position: Position,
        direction: string | undefined,
        item: string,
        amount?: number
    ): Result;
    listCommands(): string[];
    diagnose(): LuaTable<string, any>;
    getCommand(): string;
}

/** @noSelf **/
export interface AutomobilityAutomobileAPI extends IPeripheral {
    rotate(delta: number): void;
    boost(power: number, time: number): void;
}

export const constructProvider =
    new IPeripheralProvider<ManaAndArtificeConstructAPI>("construct");
export const automobileProvider =
    new IPeripheralProvider<AutomobilityAutomobileAPI>("automobile");
