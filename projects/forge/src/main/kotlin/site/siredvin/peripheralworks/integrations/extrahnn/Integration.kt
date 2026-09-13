package site.siredvin.peripheralworks.integrations.extrahnn

import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.VanillaDetailRegistries
import net.lmor.extrahnn.data.ExtraCachedModel
import net.lmor.extrahnn.data.ExtraModelTier
import net.lmor.extrahnn.item.ExtraDataModelItem
import net.lmor.extrahnn.tile.UltimateLootFabTileEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.ExtraHNNConfiguration
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.integrations.hostilenetworks.LootFabricatorPlugin
import site.siredvin.peripheralworks.integrations.hostilenetworks.NeuralModels
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class Integration : Runnable {
    object Provider : PeripheralPluginProvider {
        override val pluginType: String = "ultimate_loot_fabricator"
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!ExtraHNNConfiguration.enabled) return null
            val tile = level.getBlockEntity(pos) as? UltimateLootFabTileEntity ?: return null
            return LootFabricatorPlugin(pluginType, tile::getSelectedDrop, tile::setSelection)
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(Provider)
        VanillaDetailRegistries.ITEM_STACK.addProvider(
            DetailProvider { data, stack ->
                if (ExtraHNNConfiguration.enabled && stack.item is ExtraDataModelItem && NeuralModels.validProgress(stack)) {
                    val models = NeuralModels.storedModels(stack, true)
                    if (models != null) {
                        val cached = ExtraCachedModel(stack.copy(), 0)
                        val maxRank = cached.tier == ExtraModelTier.OMNIPOTENT
                        data["dataModel"] = mapOf(
                            "kind" to "combined",
                            "models" to models.map(NeuralModels::identity),
                            "progression" to NeuralModels.progression(
                                cached.tier.name.lowercase(),
                                cached.data,
                                cached.tierData,
                                if (maxRank) null else cached.nextTierData,
                                cached.dataPerKill,
                                cached.simCost(),
                                ExtraDataModelItem.getIters(stack),
                            ),
                        )
                    }
                }
            },
        )
    }
}
