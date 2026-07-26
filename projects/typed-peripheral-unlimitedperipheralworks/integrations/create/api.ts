import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { CreateFilterableAPI } from "./filterable_api";

/** @noSelf **/
export interface CreateScrollOptionAPI extends IPeripheral {
    getScrollValue(): number;
    setScrollValue(value: number): void;
}

/** @noSelf **/
export interface CreateLinearActuatorAPI extends IPeripheral {
    inspect(): { movementSpeed: number; isRunning: boolean };
}

/** @noSelf **/
export interface CreateBlazeBurnerAPI extends IPeripheral {
    inspect(): { fuelType: string; remainingBurnTime: number };
}

export const createFilterProvider =
    new IPeripheralProvider<CreateFilterableAPI>("create_filter");
export const createScrollProvider =
    new IPeripheralProvider<CreateScrollOptionAPI>("create_scroll");
export const createProvider = new IPeripheralProvider<
    CreateLinearActuatorAPI | CreateBlazeBurnerAPI
>("create");
