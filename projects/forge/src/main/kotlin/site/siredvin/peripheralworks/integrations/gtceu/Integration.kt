package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.forge.GTCapability
import com.gregtechceu.gtceu.api.item.IGTTool
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour
import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.VanillaDetailRegistries
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import kotlin.math.max

class Integration : Runnable {

    object WorkablePeripheralPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = WorkablePeripheralPlugin.TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            val capability = blockEntity?.getCapability(GTCapability.CAPABILITY_WORKABLE)
            if (capability != null && capability.isPresent) {
                return WorkablePeripheralPlugin(capability.resolve().get())
            }
            return null
        }
    }

    object ControllablePeripheralPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = ControllablePeripheralPlugin.TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            val capability = blockEntity?.getCapability(GTCapability.CAPABILITY_CONTROLLABLE)
            if (capability != null && capability.isPresent) {
                return ControllablePeripheralPlugin(capability.resolve().get())
            }
            return null
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(WorkablePeripheralPluginProvider)
        ComputerCraftProxy.addProvider(ControllablePeripheralPluginProvider)

        VanillaDetailRegistries.ITEM_STACK.addProvider(
            DetailProvider { data, stack ->
                val item = stack.item
                val tag = stack.tag
                if (tag != null) {
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
                }
            },
        )
    }
}
