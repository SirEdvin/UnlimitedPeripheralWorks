package site.siredvin.peripheralworks.integrations.embers

import com.rekindled.embers.api.capabilities.EmbersCapabilities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class Integration : Runnable {

    object EmberStorageProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "ember_storage"
        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos) ?: return null
            if (Configuration.enableEmberStorage) {
                val emberCapability = blockEntity.getCapability(EmbersCapabilities.EMBER_CAPABILITY)
                if (emberCapability.isPresent) {
                    return EmberStoragePlugin(level, emberCapability.resolve().get())
                }
            }
            return null
        }
    }

    override fun run() {
        ComputerCraftProxy.addProvider(EmberStorageProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}
