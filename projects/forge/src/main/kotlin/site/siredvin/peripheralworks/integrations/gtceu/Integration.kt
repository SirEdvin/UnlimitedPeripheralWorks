package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper
import com.gregtechceu.gtceu.api.item.IGTTool
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour
import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.VanillaDetailRegistries
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import kotlin.math.max

class Integration : Runnable {

    object WorkablePeripheralPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = WorkablePeripheralPlugin.TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val workable = GTCapabilityHelper.getWorkable(level, pos, side) ?: return null
            return WorkablePeripheralPlugin(workable)
        }
    }

    object ControllablePeripheralPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = ControllablePeripheralPlugin.TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val controllable = GTCapabilityHelper.getControllable(level, pos, side) ?: return null
            return ControllablePeripheralPlugin(controllable)
        }
    }

    object MachinePeripheralPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = MachinePlugin.TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is IMachineBlockEntity) {
                val definition = blockEntity.definition
                val metaMachine = blockEntity.metaMachine
                if (metaMachine is MultiblockControllerMachine && definition is MultiblockMachineDefinition) {
                    return MultiblockMachinePlugin(definition, metaMachine)
                }
                return MachinePlugin(blockEntity.definition)
            }
            return null
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(WorkablePeripheralPluginProvider)
        ComputerCraftProxy.addProvider(ControllablePeripheralPluginProvider)
        ComputerCraftProxy.addProvider(MachinePeripheralPluginProvider)

        RecipeRegistryToolkit.registerRecipeSerializer(GTRecipe::class.java, GTCEURecipeTransformer())

//        InformativeRegistryPeripheral.addList("gtceuMultiblock", "GTCEU multiblock registry", { level ->
//            MethodResult.of(GTRegistries.MACHINES.filter { it is MultiblockMachineDefinition }.map { it.id.toString() })
//        }, { level, id ->
//            val machine = GTRegistries.MACHINES.get(ResourceLocation.tryParse(id)) as? MultiblockMachineDefinition ?: return@addList MethodResult.of(null, "Cannot find machine " + id)
//            val info = mutableMapOf<String, Any>()
//            info["id"] = "id"
//            info["isGenerator"] = machine.isGenerator
//            info["shapes"] = machine.shapes.get().map {
//                it.blocks.map { it1 -> it1.map { it2 -> it2.map { blockInfo -> LuaRepresentation.forBlockState(blockInfo.blockState) } } }
//            }
//            return@addList MethodResult.of(info)
//        })

        VanillaDetailRegistries.ITEM_STACK.addProvider(
            DetailProvider { data, stack ->
                val item = stack.item
                if (item is IGTTool) {
                    val stats = item.toolStats
                    val gregData = mutableMapOf<String, Any>()
                    val remainingDamage = item.getTotalMaxDurability(stack) - stack.damageValue + 1
                    if (stats.isSuitableForCrafting(stack)) {
                        gregData["craftingUses"] = remainingDamage / max(1, stats.getDamagePerCraftingAction(stack))
                    }
                    gregData["maxUses"] = item.getTotalMaxDurability(stack)
                    gregData["generalUses"] = remainingDamage
                    data["gtceu"] = gregData
                }
                if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                    data["gtceu"] = mapOf(
                        "circuitConfiguration" to IntCircuitBehaviour.getCircuitConfiguration(stack),
                    )
                }
            },
        )
    }
}
