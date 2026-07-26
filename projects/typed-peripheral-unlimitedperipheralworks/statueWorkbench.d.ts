import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
/** @noSelf **/
export interface StatueWorkbench extends IPeripheral {
    isPresent(): boolean;
    setStatueName(name: string): void;
    getStatueName(): string;
    setAuthor(author: string): void;
    getAuthor(): string;
    setLightLevel(level: number): void;
    getLightLevel(): number;
    setCubes(cubes: Array<Cube>): void;
    getCubes(): Array<Cube>;
    reset(): void;
}
/** @noSelf **/
export declare class DummyStatueWorkbench implements StatueWorkbench {
    isPresent(): boolean;
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
