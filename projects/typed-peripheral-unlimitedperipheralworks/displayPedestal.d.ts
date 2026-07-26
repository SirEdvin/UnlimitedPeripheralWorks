import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface DisplayPedestal extends IPeripheral {
    getItem(): ItemDetail | null;
    setItem(id: string, name?: string, nbtData?: string): Result;
    isLabelRendered(): boolean;
    setLabelRendered(value: boolean): void;
    isItemRendered(): boolean;
    setItemRendered(value: boolean): void;
}
export declare const displayPedestalProvider: IPeripheralProvider<DisplayPedestal>;
