import { Position } from "../types";

/** @noSelf **/
export interface ManaAndArtificeConstructAPI extends IPeripheral {
    move(ignored: any, position: Position): void;
    store(ignored: any, position: Position, direction?: string): void;
    take(
        ignored: any,
        position: Position,
        direction: string | undefined,
        item: string,
        amount?: number
    ): void;
    listCommands(): LuaTable<string, string>;
    diagnose(): string[];
    getCommand(): {
        name: string;
        isFinished: boolean;
        isSuccess: boolean;
        isStart: boolean;
    };
}

/** @noSelf **/
export interface AutomobilityAutomobileAPI extends IPeripheral {
    rotate(delta: number): void;
    boost(power: number, time: number): void;
}
