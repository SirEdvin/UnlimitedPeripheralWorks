import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

let DUMMY_NAME = "";
let DUMMY_AUTHOR = "";
let DUMMY_LIGHT_LEVEL = 0;
let DUMMY_CUBES = [];

/** @noSelf **/
export interface StatueWorkbench extends IPeripheral {
    isPresent(): Boolean;
    setStatueName(name: string);
    getStatueName(): string;
    setAuthor(author: string);
    getAuthor(): string;
    setLightLevel(level: number);
    getLightLevel(): number;
    setCubes(cubes: Array<Cube>);
    getCubes(): Array<Cube>;
    reset();
}

/** @noSelf **/
export class DummyStatueWorkbench implements StatueWorkbench {
    isPresent(): Boolean {
        return true;
    }
    setStatueName(name: string) {
        DUMMY_NAME = name;
    }
    getStatueName(): string {
        return DUMMY_NAME;
    }
    setAuthor(author: string) {
        DUMMY_AUTHOR = author;
    }
    getAuthor(): string {
        return DUMMY_AUTHOR;
    }
    setLightLevel(level: number) {
        DUMMY_LIGHT_LEVEL = level;
    }
    getLightLevel(): number {
        return DUMMY_LIGHT_LEVEL;
    }
    setCubes(cubes: Cube[]) {
        DUMMY_CUBES = cubes;
    }
    getCubes(): Cube[] {
        return DUMMY_CUBES;
    }
    reset() {
        DUMMY_AUTHOR = "";
        DUMMY_CUBES = [];
        DUMMY_NAME = "";
        DUMMY_LIGHT_LEVEL = 0;
    }
}

export const statueWorkbenchProvider = new IPeripheralProvider<StatueWorkbench>(
    "statue_workbench",
    () => new DummyStatueWorkbench()
);
