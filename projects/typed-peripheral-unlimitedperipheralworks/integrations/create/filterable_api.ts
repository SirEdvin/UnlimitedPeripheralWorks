/** @noSelf **/
export declare interface CreateFilterableAPI extends IPeripheral {
    getFilterName(): string | null;
    setFilterItem(id: string): Result;
    clearFilterItem(): Result;
}
