package site.siredvin.peripheralworks.testmod

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.common.item.behavior.TurbineRotorBehaviour
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine
import com.gregtechceu.gtceu.common.recipe.builder.GTRecipeBuilder
import com.gregtechceu.gtceu.config.ConfigHolder
import com.gregtechceu.gtceu.data.item.GTItems
import com.gregtechceu.gtceu.data.material.GTMaterials
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IDynamicPeripheral
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.integrations.gtceu.MachinePlugin
import site.siredvin.peripheralworks.integrations.gtceu.TurbineMachinePeripheralPlugin
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.cct.CctComputerState

@TestGroup("gtceu")
class GTCEUInfoGameTests {
    @Suppress("DEPRECATION")
    @GameTest(template = "empty")
    fun machineSnapshot(helper: GameTestHelper) {
        val machine = placeGTMachine(helper, "lv_macerator")
        // NeoForge defers the holder's onLoad, which builds native recipe capability proxies.
        helper.startSequence().thenIdle(2).thenExecute {
            val plugin = machinePlugin(machine)
            val entity = machine.holder.self()
            val energy = checkNotNull(GTCapabilityHelper.getEnergyContainer(helper.level, machine.pos, null))
            val logic = checkNotNull(GTCapabilityHelper.getRecipeLogic(helper.level, machine.pos, null))
            energy.addEnergy(1234)
            val idle = plugin.getInfo()
            check(section(idle, "machine")["id"] == "gtceu:lv_macerator")
            check(section(idle, "machine")["tier"] == 1)
            check(section(idle, "energy")["storedEUExact"] == "1234")
            check(section(idle, "energy")["inputVoltage"] == energy.inputVoltage)
            check(section(idle, "energy")["inputAmperage"] == energy.inputAmperage)
            check(section(idle, "status")["state"] == "idle")
            check(section(idle, "recipe")["hasRecipe"] == false)
            check("maintenance" !in idle && "multiblock" !in idle)
            val before = entity.saveWithoutMetadata(helper.level.registryAccess())
            check(plugin.getInfo() == idle)
            check(entity.saveWithoutMetadata(helper.level.registryAccess()) == before) { "Snapshot mutated machine data" }

            for (eu in listOf(32L, -32L)) {
                val recipe = GTRecipeBuilder(ResourceLocation.fromNamespaceAndPath("peripheralworks_testmod", "info"), machine.definition.recipeTypes.first())
                    .duration(100).EUt(eu).build()
                logic.setupRecipe(recipe)
                check(logic.isWorking && logic.lastRecipe != null) { "Native recipe fixture did not start" }
                logic.progress = 7
                val running = plugin.getInfo()
                check(section(running, "recipe")["hasRecipe"] == true)
                check(section(running, "recipe")["id"] == recipe.id.toString())
                check(section(running, "recipe")["progressTicks"] == 7)
                check(section(running, "recipe")["durationTicks"] == 100)
                check(section(running, "recipe")["euPerTick"] == 32L)
                check(section(running, "recipe")["energyDirection"] == if (eu > 0) "input" else "output")
                for (state in listOf(RecipeLogic.Status.WAITING, RecipeLogic.Status.SUSPEND, RecipeLogic.Status.IDLE)) {
                    logic.setStatus(state)
                    val snapshot = plugin.getInfo()
                    check(section(snapshot, "status")["state"] == state.serializedName)
                    check(section(snapshot, "recipe")["hasRecipe"] == (state != RecipeLogic.Status.IDLE))
                    if (state == RecipeLogic.Status.IDLE) check("id" !in section(snapshot, "recipe"))
                }
                logic.resetRecipeLogic()
            }
            check(section(idle, "recipe")["hasRecipe"] == false) { "Earlier snapshot was mutated" }
        }.thenSucceed()
    }

    @GameTest(template = "empty", batch = "gtceu-maintenance")
    fun maintenanceAndControllerLifecycle(helper: GameTestHelper) {
        val controller = placeGTMachine(helper, "electric_blast_furnace") as WorkableElectricMultiblockMachine
        val plugin = machinePlugin(controller)
        val hatch = placeGTMachine(helper, "maintenance_hatch", BlockPos(1, 1, 2)) as IMaintenanceMachine
        val energyHatch = placeGTMachine(helper, "lv_energy_input_hatch", BlockPos(2, 1, 1))
        val energy = checkNotNull(GTCapabilityHelper.getEnergyContainer(helper.level, energyHatch.pos, null))
        energy.addEnergy(128)
        check(energy.energyStored == 128L) { "Energy hatch fixture was not charged" }
        val original = ConfigHolder.INSTANCE.machines.enableMaintenance
        try {
            ConfigHolder.INSTANCE.machines.enableMaintenance = true
            hatch.maintenanceProblems = 0
            hatch.setTaped(true)
            val hatchPlugin = machinePlugin(hatch.self())
            val broken = section(hatchPlugin.getInfo(), "maintenance")
            check(broken["problemCount"] == 6 && broken["hasProblems"] == true)
            check(broken["problems"] == listOf("wrench", "screwdriver", "soft_mallet", "hard_hammer", "wire_cutter", "crowbar"))
            hatch.setMaintenanceFixed(0)
            check(section(hatchPlugin.getInfo(), "maintenance")["problemCount"] == 5)
            check((section(hatchPlugin.getInfo(), "maintenance")["problems"] as List<*>).first() == "screwdriver")
            val unformed = plugin.getInfo()
            check(section(unformed, "multiblock")["formed"] == false)
            check("maintenance" !in unformed && "energy" !in unformed)
            check("tier" !in section(unformed, "machine"))

            // Feed native formation callbacks their match context; this tests aggregation/lifecycle,
            // not the blast furnace's block-pattern matcher.
            controller.multiblockState.matchContext.set("parts", setOf(hatch, energyHatch as IMultiPart))
            controller.onStructureFormed()
            val formed = plugin.getInfo()
            check(section(formed, "multiblock")["formed"] == true)
            check(section(formed, "multiblock")["batchEnabled"] == controller.isBatchEnabled)
            check(section(formed, "maintenance") == section(hatchPlugin.getInfo(), "maintenance"))
            check(section(formed, "energy")["storedEUExact"] == "128")
            check(section(formed, "energy")["inputVoltage"] == controller.energyContainer.inputVoltage)
            ConfigHolder.INSTANCE.machines.enableMaintenance = false
            val disabled = section(plugin.getInfo(), "maintenance")
            check(disabled["enabled"] == false && disabled["hasProblems"] == false && disabled["problemCount"] == 0)
            check(disabled["problems"] == emptyList<String>())
            ConfigHolder.INSTANCE.machines.enableMaintenance = true
            hatch.maintenanceProblems = IMaintenanceMachine.NO_PROBLEMS
            check(section(plugin.getInfo(), "maintenance")["hasProblems"] == false)
            controller.onStructureInvalid()
            val invalid = plugin.getInfo()
            check(section(invalid, "multiblock")["formed"] == false)
            check("maintenance" !in invalid && "energy" !in invalid)
        } finally {
            controller.onStructureInvalid()
            ConfigHolder.INSTANCE.machines.enableMaintenance = original
        }
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun turbineLifecycle(helper: GameTestHelper) {
        val turbine = placeGTMachine(helper, "steam_large_turbine") as LargeTurbineMachine
        val plugin = ComputerCraftProxy.collectPlugins(helper.level, turbine.pos, Direction.NORTH)[TurbineMachinePeripheralPlugin.TYPE] as TurbineMachinePeripheralPlugin
        val peripheral = ComputerCraftProxy.lazyPeripheralProvider(helper.level, turbine.pos, Direction.NORTH)!!.get() as IDynamicPeripheral
        check(TurbineMachinePeripheralPlugin.TYPE in peripheral.additionalTypes)
        check(plugin.hasRotor().result.contentDeepEquals(arrayOf(false)))
        check(plugin.getRotorSpeed().result.contentDeepEquals(arrayOf(0)))
        val rotor = placeGTMachine(helper, "hv_rotor_holder", BlockPos(1, 1, 2)) as RotorHolderPartMachine
        val outputHatch = placeGTMachine(helper, "hv_energy_output_hatch", BlockPos(2, 1, 1)) as IMultiPart
        val stack = GTItems.TURBINE_ROTOR.asStack()
        checkNotNull(TurbineRotorBehaviour.getBehaviour(stack)).setPartMaterial(stack, GTMaterials.Steel)
        rotor.rotorStack = stack
        try {
            turbine.multiblockState.matchContext.set("parts", setOf(rotor, outputHatch))
            turbine.onStructureFormed()
            rotor.setRotorSpeed(1234)
            val expected = mapOf<String, Any>(
                "hasRotor" to rotor.hasRotor(),
                "getRotorSpeed" to rotor.rotorSpeed,
                "getMaxRotorHolderSpeed" to rotor.maxRotorHolderSpeed,
                "getTotalEfficiency" to rotor.totalEfficiency,
                "getCurrentProduction" to 0L,
                "getOverclockVoltage" to turbine.overclockVoltage,
                "getRotorDurabilityPercent" to rotor.rotorDurabilityPercent,
            )
            for ((name, value) in expected) {
                check(name in peripheral.methodNames) { "Missing turbine method $name" }
                val actual = plugin.javaClass.getMethod(name).invoke(plugin) as MethodResult
                check(actual.result.contentDeepEquals(arrayOf(value))) { "Incorrect turbine value for $name" }
            }
            val recipe = GTRecipeBuilder(ResourceLocation.fromNamespaceAndPath("peripheralworks_testmod", "turbine_info"), turbine.recipeType)
                .duration(100).EUt(-128L).build()
            turbine.recipeLogic.setupRecipe(recipe)
            check(turbine.isActive)
            check(plugin.getCurrentProduction().result.contentDeepEquals(arrayOf(128L)))
            turbine.recipeLogic.setStatus(RecipeLogic.Status.WAITING)
            // Like GTCEu's display, this reports nominal production while the recipe is active.
            check(plugin.getCurrentProduction().result.contentDeepEquals(arrayOf(128L)))
            turbine.recipeLogic.setStatus(RecipeLogic.Status.IDLE)
            check(plugin.getCurrentProduction().result.contentDeepEquals(arrayOf(0L)))
        } finally {
            turbine.onStructureInvalid()
        }
        check(plugin.hasRotor().result.contentDeepEquals(arrayOf(false)))
        check(plugin.getRotorSpeed().result.contentDeepEquals(arrayOf(0)))
        helper.succeed()
    }

    @Suppress("DEPRECATION")
    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 1200)
    fun luaSnapshot(helper: GameTestHelper) {
        val computer = (0..4).flatMap { x -> (0..3).flatMap { y -> (0..4).map { z -> helper.level.getBlockEntity(helper.absolutePos(BlockPos(x, y, z))) } } }.filterIsInstance<ComputerBlockEntity>().single()
        val label = "peripheralworksgametests.gtceu"
        computer.setLabel(label)
        val pos = computer.blockPos.relative(Direction.NORTH)
        helper.level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("gtceu", "lv_macerator")).defaultBlockState())
        val machine = (helper.level.getBlockEntity(pos) as IMachineBlockEntity).metaMachine
        machinePlugin(machine)
        checkNotNull(GTCapabilityHelper.getEnergyContainer(helper.level, pos, null)).addEnergy(1234)
        helper.startSequence().thenIdle(5)
            .thenExecute { computer.createServerComputer().turnOn() }
            .thenWaitUntil {
                val state = CctComputerState.get(label) ?: throw GameTestAssertException("Computer has not started")
                if (!state.isDone(CctComputerState.DONE)) throw GameTestAssertException("Lua has not completed")
                state.check(CctComputerState.DONE)
            }.thenSucceed()
    }
}

@Suppress("DEPRECATION")
private fun placeGTMachine(helper: GameTestHelper, name: String, pos: BlockPos = BlockPos(1, 1, 1)): MetaMachine {
    val id = ResourceLocation.fromNamespaceAndPath("gtceu", name)
    check(BuiltInRegistries.BLOCK.containsKey(id)) { "Missing fixture $id" }
    helper.setBlock(pos, BuiltInRegistries.BLOCK.get(id))
    return (helper.getBlockEntity(pos) as IMachineBlockEntity).metaMachine
}

private fun machinePlugin(machine: MetaMachine): MachinePlugin {
    val plugins = ComputerCraftProxy.collectPlugins(machine.level!!, machine.pos, Direction.NORTH)
    val peripheral = ComputerCraftProxy.lazyPeripheralProvider(machine.level!!, machine.pos, Direction.NORTH)!!.get() as IDynamicPeripheral
    check(peripheral.methodNames.count { it == "getInfo" } == 1) { "getInfo must be bound exactly once" }
    return plugins.values.filterIsInstance<MachinePlugin>().single()
}

private fun section(info: Map<String, Any>, name: String): Map<*, *> = info[name] as Map<*, *>
