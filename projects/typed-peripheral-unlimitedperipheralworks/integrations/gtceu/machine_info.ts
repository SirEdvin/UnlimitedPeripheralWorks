/** A server-thread snapshot from GTCEu 7.5.3, independent of Jade installation.
 * Unsupported sections/fields are absent (nil in Lua). Numeric energy values may
 * lose precision beyond Lua's exact integer range; use the decimal Exact fields
 * for lossless stored/capacity values provided by GTCEu.
 */
export interface MachineInfo {
    machine: {
        id: string;
        /** Native tier index. Dynamic tier/overclock fields are omitted on unformed controllers. */
        tier?: number;
        overclockTier?: number;
        overclockVoltage?: number;
    };
    status?: {
        active?: boolean;
        workingEnabled?: boolean;
        working?: boolean;
        state?: "idle" | "working" | "waiting" | "suspend";
        suspendAfterFinish?: boolean;
    };
    energy?: {
        storedEU: number;
        capacityEU: number;
        storedEUExact: string;
        capacityEUExact: string;
        inputEUPerSecond: number;
        outputEUPerSecond: number;
        /** Native container limits, NOT measured consumption. Multiblock values
         * use GTCEu's aggregated recipe energy container (input, otherwise output),
         * including its native voltage/amperage compaction rules.
         */
        inputVoltage?: number;
        inputAmperage?: number;
        outputVoltage?: number;
        outputAmperage?: number;
    };
    recipe?: {
        /** False when idle/unformed, even if GTCEu caches a previous recipe. */
        hasRecipe: boolean;
        progressTicks: number;
        durationTicks: number;
        /** Present only for a current recipe with a native identifier. */
        id?: string;
        /** Modified recipe's nominal EU/t, not measured draw while waiting/suspended.
         * Steam machines also use GTCEu's internal recipe EU equivalent here.
         */
        euPerTick?: number;
        energyDirection?: "input" | "output";
    };
    /** Direct maintenance hatch, or the first maintenance part of a formed controller. */
    maintenance?: {
        enabled: boolean;
        hasProblems: boolean;
        problemCount: number;
        /** Outstanding repairs, not GTCEu's inverted fixed-problem bitmask.
         * Empty when maintenance is disabled in GTCEu configuration.
         */
        problems: MaintenanceProblem[];
        taped: boolean;
        fullAuto: boolean;
    };
    multiblock?: {
        formed: boolean;
        batchEnabled?: boolean;
    };
}

export type MaintenanceProblem =
    | "wrench"
    | "screwdriver"
    | "soft_mallet"
    | "hard_hammer"
    | "wire_cutter"
    | "crowbar";
