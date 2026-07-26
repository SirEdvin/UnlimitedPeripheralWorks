import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface StatueWorkbench extends IPeripheral {
    isPresent(): Boolean;
    setStatueName(name: string): any;
    getStatueName(): string;
    setAuthor(author: string): any;
    getAuthor(): string;
    setLightLevel(level: number): any;
    getLightLevel(): number;
    setCubes(cubes: Array<Cube>): any;
    getCubes(): Array<Cube>;
    reset(): any;
}
/** @noSelf **/
export declare class DummyStatueWorkbench implements StatueWorkbench {
    isPresent(): Boolean;
    setStatueName(name: string): void;
    getStatueName(): string;
    setAuthor(author: string): void;
    getAuthor(): string;
    setLightLevel(level: number): void;
    getLightLevel(): number;
    setCubes(cubes: Cube[]): void;
    getCubes(): Cube[];
    reset(): void;
}
export declare const statueWorkbenchProvider: IPeripheralProvider<StatueWorkbench>;
