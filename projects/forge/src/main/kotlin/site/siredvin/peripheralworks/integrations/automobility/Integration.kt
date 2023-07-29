package site.siredvin.peripheralworks.integrations.automobility

import io.github.foundationgames.automobility.entity.AutomobileEntity
import net.minecraft.world.entity.Entity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralLookup
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralPluginProvider

class Integration : Runnable {

    object AutomobilePluginProvider : EntityPeripheralPluginProvider {
        override val pluginType: String
            get() = "automobile"

        override fun provide(entity: Entity): IPeripheralPlugin? {
            if (entity is AutomobileEntity) return AutomobilePlugin(entity)
            return null
        }
    }
    override fun run() {
        EntityPeripheralLookup.addProvider(AutomobilePluginProvider)
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}
