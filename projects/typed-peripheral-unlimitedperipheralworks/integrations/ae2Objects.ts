import { AE2NetworkAPI } from "./ae2";
import { Fallible } from "../types";

export type AE2Direction = "north" | "south" | "east" | "west" | "up" | "down";
export type AE2PushDirection = AE2Direction | "all";

export type AE2DeviceType =
    | "interface"
    | "import_bus"
    | "export_bus"
    | "storage_bus"
    | "formation_plane"
    | "storage_level_emitter"
    | "energy_level_emitter"
    | "pattern_provider";

export type AE2Resource = {
    type: "item" | "fluid";
    name: string;
};

export type AE2Stack = AE2Resource & {
    /** Item count or fluid amount in millibuckets. */
    count: number;
};

export type AE2StockRow = {
    target?: AE2Stack;
    stored?: AE2Stack;
};

export type AE2FuzzyMode =
    | "ignore_all"
    | "percent_99"
    | "percent_75"
    | "percent_50"
    | "percent_25";

export type AE2RedstoneMode = "ignore" | "low_signal" | "high_signal" | "signal_pulse";
export type AE2EmitterMode = "low_signal" | "high_signal";
export type AE2SchedulingMode = "default" | "round_robin" | "random";
export type AE2AccessMode = "no_access" | "read" | "write" | "read_write";
export type AE2StorageFilterMode = "none" | "extractable_only";
export type AE2PatternLockMode =
    | "none"
    | "lock_until_pulse"
    | "lock_while_high"
    | "lock_while_low"
    | "lock_until_result";

/** @noSelf **/
export interface AE2DeviceObject {
    getDeviceType(): AE2DeviceType;
}

/** @noSelf **/
export interface AE2UpgradeableObject {
    listUpgrades(): LuaTable<number, ItemDetail>;
    getUpgrade(slot: number): ItemDetail | null;
    pullUpgrade(fromName: string, fromSlot: number, limit?: number, toSlot?: number): number;
    pushUpgrade(toName: string, fromSlot: number, limit?: number, toSlot?: number): number;
}

/** @noSelf **/
export interface AE2PriorityObject {
    getPriority(): number;
    setPriority(priority: number): void;
}

/** @noSelf **/
export interface AE2FuzzyObject {
    /** Requires a Fuzzy Card installed in the device. */
    getFuzzyMode(): AE2FuzzyMode;
    /** Requires a Fuzzy Card installed in the device. */
    setFuzzyMode(mode: AE2FuzzyMode): void;
}

/** @noSelf **/
export interface AE2RedstoneControlledObject {
    getRedstoneMode(): AE2RedstoneMode;
    setRedstoneMode(mode: AE2RedstoneMode): void;
}

/** @noSelf **/
export interface AE2FilterObject {
    listFilters(): LuaTable<number, AE2Resource>;
    getFilter(slot: number): AE2Resource | null;
    setFilter(slot: number, resource: AE2Resource): void;
    clearFilter(slot: number): void;
}

/** @noSelf **/
export interface AE2InterfaceObject
    extends AE2DeviceObject,
        AE2UpgradeableObject,
        AE2PriorityObject,
        AE2FuzzyObject {
    listStock(): LuaTable<number, AE2StockRow>;
    getStock(slot: number): AE2StockRow | null;
    setStock(slot: number, target: AE2Stack): void;
    clearStock(slot: number): void;
}

/** @noSelf **/
export interface AE2ImportBusObject
    extends AE2DeviceObject,
        AE2UpgradeableObject,
        AE2FilterObject,
        AE2FuzzyObject,
        AE2RedstoneControlledObject {}

/** @noSelf **/
export interface AE2ExportBusObject
    extends AE2DeviceObject,
        AE2UpgradeableObject,
        AE2FilterObject,
        AE2FuzzyObject,
        AE2RedstoneControlledObject {
    isCraftOnly(): boolean;
    setCraftOnly(craftOnly: boolean): void;
    getSchedulingMode(): AE2SchedulingMode;
    setSchedulingMode(mode: AE2SchedulingMode): void;
}

/** @noSelf **/
export interface AE2StorageBusObject
    extends AE2DeviceObject,
        AE2UpgradeableObject,
        AE2FilterObject,
        AE2PriorityObject,
        AE2FuzzyObject {
    getAccessMode(): AE2AccessMode;
    setAccessMode(mode: AE2AccessMode): void;
    getStorageFilterMode(): AE2StorageFilterMode;
    setStorageFilterMode(mode: AE2StorageFilterMode): void;
    shouldFilterOnExtract(): boolean;
    setFilterOnExtract(filterOnExtract: boolean): void;
}

/** @noSelf **/
export interface AE2FormationPlaneObject
    extends AE2DeviceObject,
        AE2UpgradeableObject,
        AE2FilterObject,
        AE2PriorityObject,
        AE2FuzzyObject {
    shouldPlaceBlocks(): boolean;
    setPlaceBlocks(placeBlocks: boolean): void;
}

/** @noSelf **/
export interface AE2LevelEmitterObject extends AE2DeviceObject {
    getEmitterMode(): AE2EmitterMode;
    setEmitterMode(mode: AE2EmitterMode): void;
    isEmitting(): boolean;
}

/** @noSelf **/
export interface AE2StorageLevelEmitterObject
    extends AE2LevelEmitterObject,
        AE2UpgradeableObject,
        AE2FuzzyObject {
    getMonitoredResource(): AE2Resource | null;
    setMonitoredResource(resource: AE2Resource): void;
    clearMonitoredResource(): void;
    getThreshold(): number;
    setThreshold(threshold: number): void;
    getThresholdUnit(): "item" | "millibucket" | "ae_internal";
    shouldCraftViaRedstone(): boolean;
    setCraftViaRedstone(craftViaRedstone: boolean): void;
}

/** @noSelf **/
export interface AE2EnergyLevelEmitterObject extends AE2LevelEmitterObject {
    getThreshold(): number;
    setThreshold(threshold: number): void;
}

/** @noSelf **/
export interface AE2PatternProviderObject extends AE2DeviceObject, AE2PriorityObject {
    listPatterns(): LuaTable<number, ItemDetail>;
    getPattern(slot: number): ItemDetail | null;
    pullPattern(fromName: string, fromSlot: number, limit?: number, toSlot?: number): number;
    pushPattern(toName: string, fromSlot: number, limit?: number, toSlot?: number): number;
    isBlocking(): boolean;
    setBlocking(blocking: boolean): void;
    isVisibleInPatternAccessTerminal(): boolean;
    setVisibleInPatternAccessTerminal(visible: boolean): void;
    getPatternLockMode(): AE2PatternLockMode;
    setPatternLockMode(mode: AE2PatternLockMode): void;
}

export type AE2CableDevice =
    | AE2InterfaceObject
    | AE2ImportBusObject
    | AE2ExportBusObject
    | AE2StorageBusObject
    | AE2FormationPlaneObject
    | AE2StorageLevelEmitterObject
    | AE2EnergyLevelEmitterObject
    | AE2PatternProviderObject;

/** @noSelf **/
export interface AE2CableAPI extends IPeripheral {
    getSides(): AE2Direction[];
    getSide(side: AE2Direction): Fallible<AE2CableDevice>;
}

/** @noSelf **/
export interface AE2InterfacePeripheral extends AE2NetworkAPI, AE2InterfaceObject {}

/** @noSelf **/
export interface AE2PatternProviderPeripheral extends AE2NetworkAPI, AE2PatternProviderObject {
    getPushDirection(): AE2PushDirection;
    setPushDirection(direction: AE2PushDirection): void;
}
