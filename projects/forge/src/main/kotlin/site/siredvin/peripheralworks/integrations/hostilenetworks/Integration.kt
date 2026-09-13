package site.siredvin.peripheralworks.integrations.hostilenetworks

import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.VanillaDetailRegistries
import dev.shadowsoffire.hostilenetworks.data.CachedModel
import dev.shadowsoffire.hostilenetworks.data.ModelTier
import dev.shadowsoffire.hostilenetworks.item.DataModelItem
import dev.shadowsoffire.hostilenetworks.tile.LootFabTileEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.HostileNetworksConfiguration
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.NeuralModelNameGuard
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class Integration : Runnable {
    object Provider : PeripheralPluginProvider {
        override val pluginType: String = "loot_fabricator"
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!HostileNetworksConfiguration.enabled) return null
            val tile = level.getBlockEntity(pos) as? LootFabTileEntity ?: return null
            return LootFabricatorPlugin(pluginType, tile::getSelectedDrop, tile::setSelection)
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(Provider)
        NeuralModelNameGuard.register { stack ->
            HostileNetworksConfiguration.enabled && stack.item is DataModelItem && NeuralModels.storedModels(stack, false) == null
        }
        VanillaDetailRegistries.ITEM_STACK.addProvider(
            DetailProvider { data, stack ->
                if (HostileNetworksConfiguration.enabled && stack.item is DataModelItem && NeuralModels.validProgress(stack)) {
                    val models = NeuralModels.storedModels(stack, false)
                    if (models != null) {
                        val cached = CachedModel(stack.copy(), 0)
                        val maxRank = cached.tier == ModelTier.SELF_AWARE
                        data["dataModel"] = mapOf(
                            "kind" to "single",
                            "models" to models.map(NeuralModels::identity),
                            "progression" to NeuralModels.progression(
                                cached.tier.name.lowercase(),
                                cached.data,
                                cached.tierData,
                                if (maxRank) null else cached.nextTierData,
                                cached.dataPerKill,
                                models.single().simCost(),
                                DataModelItem.getIters(stack),
                            ),
                        )
                    }
                }
            },
        )
    }
}
