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
        fuelType: string | null;
        remainingBurnTime: number;
    };
}
