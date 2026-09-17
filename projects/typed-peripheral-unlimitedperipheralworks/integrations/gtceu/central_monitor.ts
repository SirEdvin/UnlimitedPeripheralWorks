import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** Native objects: methods on unsupported module types return nil or do nothing. @noSelf **/
export interface MonitorModule {
    getType(): string;
    getCurrentText(): string | null;
    setPlaceholderText(text: string): void;
    getScale(): number | null;
    setScale(scale: number): void;
    getImageUrl(): string | null;
    setImageUrl(url: string): void;
}

/** @noSelf **/
export interface MonitorGroup {
    getName(): string;
    getModule(): MonitorModule;
}

/** GTCEu 7.5.3 native API, delegated without changing return values. @noSelf **/
export interface CentralMonitor extends ConfigurationAPI<object> {
    getGroups(): MonitorGroup[];
}

export const centralMonitorProvider = new IPeripheralProvider<CentralMonitor>(
    "gtceu:central_monitor",
    () => null
);
