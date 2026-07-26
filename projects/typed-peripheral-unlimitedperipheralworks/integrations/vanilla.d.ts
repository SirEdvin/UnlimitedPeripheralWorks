import { ExtendedInventoryAPI } from "@siredvin/typed-peripheral-api/inventory_extended";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { EntityDetail } from "../types";
/** @noSelf **/
export interface Lectern extends IPeripheral {
    hasBook(): boolean;
    getPageCount(): number;
    getActivePage(): number;
    setActivePage(page: number): void;
    getText(): string[];
    isBookEditable(): boolean;
    addPage(text?: string): Result;
    removePage(page: number): Result;
    editPage(page: number, text: string): Result;
    ejectBook(toName: string): Result;
    injectBook(fromName: string, query?: string | object): Result;
}
/** @noSelf **/
export interface Beacon extends IPeripheral {
    getLevel(): number;
    getPossiblePowers(): string[];
    getPowers(): object[];
    configure(primaryPower: string, fromInventory: string, paymentQuery: string | object, regenerationSecondary?: boolean): Result;
}
/** @noSelf **/
export interface NoteBlock extends IPeripheral {
    getNote(): number;
    setNote(note: number): void;
    getInstrument(): string;
    setInstrument(instrument: string): void;
    play(): void;
}
/** @noSelf **/
export interface PoweredRail extends ExtendedInventoryAPI {
    isPowered(): boolean;
    pushMinecarts(reverse?: boolean): void;
    getMinecarts(): EntityDetail[];
}
/** @noSelf **/
export interface Jukebox extends IPeripheral {
    getDisc(): ItemDetail | null;
    replay(): void;
    stop(): void;
    ejectDisc(toName: string): Result;
    injectDisc(fromName: string, query?: string | object): Result;
}
export declare const lecternProvider: IPeripheralProvider<Lectern>;
export declare const beaconProvider: IPeripheralProvider<Beacon>;
export declare const noteBlockProvider: IPeripheralProvider<NoteBlock>;
export declare const poweredRailProvider: IPeripheralProvider<PoweredRail>;
export declare const jukeboxProvider: IPeripheralProvider<Jukebox>;
