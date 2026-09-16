import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** GTCEu 7.4.1 native API, delegated without changing return values. @noSelf **/
export interface CoverHolder extends ConfigurationAPI<object> {
    /** Absolute face (e.g. north), line 1..100. Returns success and message. */
    setBufferedText(side: string, line: number, text: string): LuaMultiReturn<[boolean, string]>;
    /** Returns success and expanded text, or false and the native error message. */
    parsePlaceholders(side: string, text: string): LuaMultiReturn<[boolean, string]>;
}

export const coverHolderProvider = new IPeripheralProvider<CoverHolder>(
    "gtceu:cover_holder",
    () => null
);
