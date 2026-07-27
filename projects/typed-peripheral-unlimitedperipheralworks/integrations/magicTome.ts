import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

export type SpellDetail = {
    name: string;
    display: string;
    color: number;
    sound?: string;
    cost: number;
};
export type ManaDetail = {
    current: number;
    max: number;
    bookTier: number;
    glyphBonus: number;
};

/** @noSelf **/
export interface MagicTome extends IPeripheral {
    getSpells(): LuaTable<number, SpellDetail>;
    getSelectedSlot(): number;
    size(): number;
    getSelectedSpell(): SpellDetail;
    select(slot: number): void;
    getMana(): ManaDetail;
    cast(): void;
}

export const magicTomeProvider = new IPeripheralProvider<MagicTome>(
    "magic_tome"
);
