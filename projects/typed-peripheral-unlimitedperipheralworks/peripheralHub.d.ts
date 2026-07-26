import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface PeripheralHub extends IPeripheral {
    getNamesRemote(): string[];
    isPresentRemote(name: string): boolean;
    getTypeRemote(name: string): string | null;
    hasTypeRemote(name: string, type: string): boolean;
    getMethodsRemote(name: string): string[] | null;
    callRemote(name: string, method: string, ...args: any[]): LuaMultiReturn<any[]>;
}
/** @noSelf **/
export interface PeripheralProxy extends PeripheralHub {
    setTextStyle(value: string): Result;
    setBoxStyle(value: string): Result;
}
/** @noSelf **/
export interface PeripheraliumHub extends PeripheralHub {
    isUpgrade(slot: number): boolean;
    equip(slot: number): Result;
    unequip(upgradeID: string): Result;
    getUpgrades(): string[];
}
export declare const peripheralProxyProvider: IPeripheralProvider<PeripheralProxy>;
export declare const peripheralHubProvider: IPeripheralProvider<PeripheralHub>;
export declare const peripheraliumHubProvider: IPeripheralProvider<PeripheraliumHub>;
export declare const netheritePeripheraliumHubProvider: IPeripheralProvider<PeripheraliumHub>;
