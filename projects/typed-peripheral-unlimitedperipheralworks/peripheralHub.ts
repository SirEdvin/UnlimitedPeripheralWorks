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

export const peripheralProxyProvider = new IPeripheralProvider<PeripheralProxy>(
    "peripheral_proxy"
);
export const peripheralHubProvider = new IPeripheralProvider<PeripheralHub>(
    "peripheral_hub"
);
export const peripheraliumHubProvider =
    new IPeripheralProvider<PeripheraliumHub>("peripheralium_hub");
export const netheritePeripheraliumHubProvider =
    new IPeripheralProvider<PeripheraliumHub>("netherite_peripheralium_hub");
