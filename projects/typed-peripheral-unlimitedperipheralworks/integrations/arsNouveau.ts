import { EntityDetail } from "../types";

/** @noSelf **/
export interface ArsNouveauMobJar extends IPeripheral {
    inspect(): { entity: EntityDetail } | object;
}
