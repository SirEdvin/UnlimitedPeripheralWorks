package site.siredvin.peripheralworks.integrations.mna

import com.mna.entities.constructs.animated.Construct
import net.minecraft.world.entity.Entity
import site.siredvin.broccolium.modules.storage.item.AgnosticItemHandlerWrapper
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.broccolium.modules.storage.item.api.AgnosticItemStorageEntityExtractor
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralLookup
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralPluginProvider
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class Integration : Runnable {

    object ConstructPluginProvider : EntityPeripheralPluginProvider {
        override val pluginType: String
            get() = "construct"

        override fun provide(entity: Entity): IPeripheralPlugin? {
            if (entity is Construct) return ConstructPlugin(entity)
            return null
        }
    }

    override fun run() {
        EntityPeripheralLookup.addProvider(ConstructPluginProvider)
        AgnosticItemStorageLookup.addItemStorageExtractor(
            AgnosticItemStorageEntityExtractor { level, entity ->
                if (entity is Construct) {
                    return@AgnosticItemStorageEntityExtractor AgnosticItemHandlerWrapper(entity)
                }
                return@AgnosticItemStorageEntityExtractor null
            },
        )
    }
}
