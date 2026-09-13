import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";

/** Native identity; entityIds includes the primary entity and registered subtypes. */
export type NeuralModelIdentity = {
    modelId: string;
    entityId: string;
    entityIds: string[];
};

export type NeuralModelProgression = {
    /** Lowercase native tier name, including Extra HNN's autonomous through omnipotent tiers. */
    rank: string;
    /** Total accumulated native data (not progress within the current tier). */
    data: number;
    /** Cumulative threshold for the current tier. */
    tierData: number;
    /** Cumulative next-tier threshold; null/nil at maximum rank. */
    nextTierData: number | null;
    /** Data still needed for the next tier; null/nil at maximum rank. */
    remainingData: number | null;
    maxRank: boolean;
    dataPerKill: number;
    /** Effective simulation energy cost in FE per tick. */
    simulationCost: number;
    iterations: number;
};

export type NeuralModelDetail =
    | { kind: "single"; models: [NeuralModelIdentity]; progression: NeuralModelProgression }
    | { kind: "combined"; models: NeuralModelIdentity[]; progression: NeuralModelProgression };

/** Detailed queries only. Absent on blank, malformed, unresolved and unrelated items.
 * Combined models preserve constituent order/duplicates and have one shared progression.
 * Nullable fields are absent in Lua when nil; inspection never changes the stack.
 * Upstream item-name failures on malformed models are not intercepted and can fail the query.
 */
export type NeuralModelItemDetail = ItemDetail & { dataModel?: NeuralModelDetail };

/** @noSelf */
export interface LootFabricator extends IPeripheral {
    /** All covered Minecraft entity IDs, sorted and unique, independent of the input slot.
     * Subtypes are included. Model IDs are NOT accepted by these methods.
     */
    getEntities(): string[];
    /** Native 1-based selection order; counts are per prediction, not per machine batch.
     * A unique primary-entity model takes precedence over subtype matches.
     * Unknown, malformed, model-less or ambiguous entity IDs raise Lua errors.
     * Reloads can change this ordering: query again before selecting.
     */
    getLoot(entity: string): ItemDetail[];
    /** Returns the 1-based selection, or null/nil if unset or stale. */
    getSelectedLoot(entity: string): number | null;
    /** Select a finite integer in 1..getLoot(entity).length, or clear with null/nil.
     * Zero and invalid indices raise errors without changing state. Lua omission also clears.
     * Entity aliases sharing a native model share the selection on this machine.
     * Persists through native saves; does not consume resources or produce loot itself.
     * @example fabricator.setSelectedLoot("minecraft:zombie", 1);
     * @example fabricator.setSelectedLoot("minecraft:zombie", null);
     */
    setSelectedLoot(entity: string, index: number | null): void;
}

/** Extra HNN V1–V4 fabricators share the same selection API and per-prediction loot counts.
 * @noSelf
 */
export interface UltimateLootFabricator extends LootFabricator {}

export const lootFabricatorProvider = new IPeripheralProvider<LootFabricator>("loot_fabricator");
export const ultimateLootFabricatorProvider = new IPeripheralProvider<UltimateLootFabricator>("ultimate_loot_fabricator");
