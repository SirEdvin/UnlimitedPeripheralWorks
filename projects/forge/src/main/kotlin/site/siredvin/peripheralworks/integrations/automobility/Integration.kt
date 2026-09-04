package site.siredvin.peripheralworks.integrations.automobility

import io.github.foundationgames.automobility.entity.AutomobileEntity
import net.minecraft.world.entity.Entity
import site.siredvin.peripheralworks.common.configuration.integration.AutomobilityConfiguration
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralLookup
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralPluginProvider
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class Integration : Runnable {

    object AutomobilePluginProvider : EntityPeripheralPluginProvider {
        override val pluginType: String
            get() = "automobile"

        override fun provide(entity: Entity): IPeripheralPlugin? {
            if (AutomobilityConfiguration.enableAutomobile && entity is AutomobileEntity) return AutomobilePlugin(entity)
            return null
        }
    }
    override fun run() {
        EntityPeripheralLookup.addProvider(AutomobilePluginProvider)
    }
}
