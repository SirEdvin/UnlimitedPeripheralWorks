/** @noSelf **/
export declare interface CreateFilterableAPI extends IPeripheral {
    getFilterName(): string;
    setFilterItem(id: string): any;
    clearFilterItem(): any;
}
