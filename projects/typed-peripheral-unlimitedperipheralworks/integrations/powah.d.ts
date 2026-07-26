import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface PowahEnergyStorageAPI extends IPeripheral {
    getEnergyTransfer(): number;
}
/** @noSelf **/
export interface PowahGeneratorAPI extends PowahEnergyStorageAPI {
    getEnergyGeneration(): number;
}
/** @noSelf **/
export interface PowahReactorAPI extends PowahGeneratorAPI {
    inspect(): LuaTable<string, any>;
    toggleAutoMode(): void;
}
/** @noSelf **/
export interface PowahEnderCellAPI extends PowahEnergyStorageAPI {
    getChannel(): number;
    setChannel(channel: number): void;
    getMaxChannel(): number;
}
/** @noSelf **/
export interface PowahRedstoneControlAPI extends IPeripheral {
    getRedstoneMode(): string;
    setRedstoneMode(mode: string): void;
}
export declare const powahExtraProvider: IPeripheralProvider<PowahEnergyStorageAPI | PowahGeneratorAPI | PowahReactorAPI | PowahEnderCellAPI>;
export declare const powahRedstoneProvider: IPeripheralProvider<PowahRedstoneControlAPI>;
