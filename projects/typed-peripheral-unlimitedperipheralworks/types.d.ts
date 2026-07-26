export type Position = {
    x: number;
    y: number;
    z: number;
};
export type BlockState = {
    name: string;
    state?: LuaTable<string, string | number | boolean>;
};
export type EntityDetail = LuaTable<string, any> & Position;
