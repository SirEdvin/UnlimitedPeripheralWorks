/** @noSelf **/
export interface PowahEnergyStorageAPI extends IPeripheral {
    getEnergyTransfer(): number;
}

/** @noSelf **/
export interface PowahGeneratorAPI extends PowahEnergyStorageAPI {
    getEnergyGeneration(): number;
}

/** @noSelf **/
export interface PowahReactorAPI extends PowahGeneratorAPI {
    inspect(): {
        autoMode: boolean;
        currentCarbon: number;
        maxCarbon: number;
        currentRedstone: number;
        maxRedstone: number;
        currentUranium: number;
        maxUranium: number;
        uraniumConsumption: number;
        energyProduction: number;
        currentSolidCoolant: number;
        maxSolidCoolant: number;
        coolantTemp: number;
        solidCoolantTemp: number;
        currentTemp: number;
        maxTemp: number;
    };
    toggleAutoMode(): void;
}

/** @noSelf **/
export interface PowahEnderCellAPI extends PowahEnergyStorageAPI {
    getChannel(): number;
    setChannel(channel: number): void;
    getMaxChannel(): number;
}

/** @noSelf **/
export interface PowahRedstoneControlAPI extends IPeripheral {
    getRedstoneMode(): string;
    setRedstoneMode(mode: string): void;
}
