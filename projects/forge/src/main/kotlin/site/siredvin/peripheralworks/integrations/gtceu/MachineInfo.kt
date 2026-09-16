package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.forge.GTCapability
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.recipe.RecipeHelper
import com.gregtechceu.gtceu.config.ConfigHolder
import net.minecraft.core.Direction
import net.minecraftforge.common.capabilities.Capability

internal object MachineInfo {
    fun collect(machine: MetaMachine, side: Direction): Map<String, Any> {
        // Resolve on every call: multiblock formation and hatch capabilities can change.
        fun <T : Any> capability(type: Capability<T>): T? {
            val entity = machine.holder.self()
            return entity.getCapability(type).resolve().orElse(null)
                ?: entity.getCapability(type, side).resolve().orElse(null)
        }

        val controller = machine as? IMultiController
        val formed = controller?.isFormed != false
        val identity = mutableMapOf<String, Any>("id" to machine.definition.id.toString())
        if (machine is ITieredMachine && formed) identity["tier"] = machine.tier
        if (machine is IOverclockMachine && formed) {
            identity["overclockTier"] = machine.overclockTier
            identity["overclockVoltage"] = machine.overclockVoltage
        }
        val result = mutableMapOf<String, Any>("machine" to identity)

        val status = mutableMapOf<String, Any>()
        capability(GTCapability.CAPABILITY_WORKABLE)?.let { status["active"] = it.isActive }
        capability(GTCapability.CAPABILITY_CONTROLLABLE)?.let { status["workingEnabled"] = it.isWorkingEnabled }
        capability(GTCapability.CAPABILITY_RECIPE_LOGIC)?.let { logic ->
            status["state"] = logic.status.serializedName
            status["working"] = logic.isWorking
            status["suspendAfterFinish"] = logic.isSuspendAfterFinish
            val recipe = logic.lastRecipe.takeIf { !logic.isIdle && formed }
            val recipeInfo = mutableMapOf<String, Any>(
                "hasRecipe" to (recipe != null),
                "progressTicks" to logic.progress,
                "durationTicks" to logic.duration,
            )
            if (recipe != null) {
                val energy = RecipeHelper.getRealEUtWithIO(recipe)
                recipe.id?.let { recipeInfo["id"] = it.toString() }
                recipeInfo["euPerTick"] = energy.totalEU
                recipeInfo["energyDirection"] = if (energy.isInput) "input" else "output"
            }
            result["recipe"] = recipeInfo
        }
        if (status.isNotEmpty()) result["status"] = status

        // The controller UI aggregates its recipe energy hatches, not its own block capability.
        val container = if (machine is WorkableElectricMultiblockMachine && formed) {
            machine.energyContainer
        } else if (formed) {
            capability(GTCapability.CAPABILITY_ENERGY_CONTAINER)
        } else {
            null
        }
        val energyProvider = if (formed) capability(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER) ?: container else null
        energyProvider?.let { provider ->
            val info = provider.energyInfo
            val energy = mutableMapOf<String, Any>(
                "storedEU" to info.stored().toDouble(),
                "capacityEU" to info.capacity().toDouble(),
                "storedEUExact" to info.stored().toString(),
                "capacityEUExact" to info.capacity().toString(),
                "inputEUPerSecond" to provider.inputPerSec,
                "outputEUPerSecond" to provider.outputPerSec,
            )
            container?.let {
                energy["inputVoltage"] = it.inputVoltage
                energy["inputAmperage"] = it.inputAmperage
                energy["outputVoltage"] = it.outputVoltage
                energy["outputAmperage"] = it.outputAmperage
            }
            result["energy"] = energy
        }

        val maintenance = capability(GTCapability.CAPABILITY_MAINTENANCE_MACHINE)
            ?: controller?.takeIf { it.isFormed }?.parts?.filterIsInstance<IMaintenanceMachine>()?.firstOrNull()
        maintenance?.let {
            val enabled = ConfigHolder.INSTANCE.machines.enableMaintenance
            // GTCEu's bitmask marks FIXED problems with 1, not outstanding problems.
            val tools = listOf("wrench", "screwdriver", "soft_mallet", "hard_hammer", "wire_cutter", "crowbar")
            val problems = tools.filterIndexed { index, _ -> enabled && (it.maintenanceProblems.toInt() shr index and 1) == 0 }
            result["maintenance"] = mapOf(
                "enabled" to enabled,
                "hasProblems" to it.hasMaintenanceProblems(),
                "problemCount" to it.numMaintenanceProblems,
                "problems" to problems,
                "taped" to it.isTaped,
                "fullAuto" to it.isFullAuto,
            )
        }
        controller?.let {
            val multiblock = mutableMapOf<String, Any>("formed" to it.isFormed)
            if (machine is WorkableElectricMultiblockMachine) multiblock["batchEnabled"] = machine.isBatchEnabled
            result["multiblock"] = multiblock
        }
        return result
    }
}
