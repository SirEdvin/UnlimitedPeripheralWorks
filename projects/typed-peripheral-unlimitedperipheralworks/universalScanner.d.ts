import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { OperationApi } from "@siredvin/typed-peripheral-api/operations";
import { ScanApi } from "@siredvin/typed-peripheral-api/scan";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface UniversalScanner extends ScanApi, OperationApi, ConfigurationAPI<object> {
}
export declare const universalScannerProvider: IPeripheralProvider<UniversalScanner>;
