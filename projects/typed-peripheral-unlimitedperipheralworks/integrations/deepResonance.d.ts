import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface DeepResonanceCrystal extends IPeripheral {
    inspect(): {
        strength: number;
        efficiency: number;
        power: number;
        purity: number;
        glowing: boolean;
    };
}
/** @noSelf **/
export interface DeepResonanceGenerator extends IPeripheral {
    inspect(): {
        energy: number;
        lastRfPerTick: number;
        isActive: boolean;
    };
}
export declare const resonatingCrystalProvider: IPeripheralProvider<DeepResonanceCrystal>;
export declare const deepResonanceGeneratorProvider: IPeripheralProvider<DeepResonanceGenerator>;
