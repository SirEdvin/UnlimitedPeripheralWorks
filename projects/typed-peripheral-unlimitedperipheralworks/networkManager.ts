import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** @noSelf **/
export interface NetworkManager extends ConfigurationAPI<object> {
    getGroups(): string[];
    addGroup(group: string): Result;
    removeGroup(group: string): Result;
    add(group: string, peripheral: string): Result;
    remove(group: string, peripheral: string): Result;
    setGroupColor(group: string, color: number): Result;
    get(group: string): LuaMultiReturn<string[]>;
    getDistanceBetween(
        first: string,
        second: string
    ): { x: number; y: number; z: number };
}

export const networkManagerProvider = new IPeripheralProvider<NetworkManager>(
    "network_manager",
    () => null
);
