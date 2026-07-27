export type Position = { x: number; y: number; z: number };

export type BlockState = {
    name: string;
    state?: LuaTable<string, string | number | boolean>;
};

export type EntityDetail = LuaTable<string, any> & Position;

export type Fallible<T, F = null> = LuaMultiReturn<[T] | [F, string]>;
