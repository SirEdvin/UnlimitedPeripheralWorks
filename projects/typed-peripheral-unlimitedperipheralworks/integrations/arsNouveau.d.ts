import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { EntityDetail } from "../types";
/** @noSelf **/
export interface ArsNouveauMobJar extends IPeripheral {
    inspect(): {
        entity: EntityDetail;
    } | object;
}
export declare const mobJarProvider: IPeripheralProvider<ArsNouveauMobJar>;
