import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { CreateFilterableAPI } from "./filterable_api";
/** @noSelf **/
export interface CreateScrollOptionAPI extends IPeripheral {
    getScrollValue(): number;
    setScrollValue(value: number): void;
}
/** @noSelf **/
export interface CreateLinearActuatorAPI extends IPeripheral {
    inspect(): {
        movementSpeed: number;
        isRunning: boolean;
    };
}
/** @noSelf **/
export interface CreateBlazeBurnerAPI extends IPeripheral {
    inspect(): {
        fuelType: string;
        remainingBurnTime: number;
    };
}
export declare const createFilterProvider: IPeripheralProvider<CreateFilterableAPI>;
export declare const createScrollProvider: IPeripheralProvider<CreateScrollOptionAPI>;
export declare const createProvider: IPeripheralProvider<CreateLinearActuatorAPI | CreateBlazeBurnerAPI>;
