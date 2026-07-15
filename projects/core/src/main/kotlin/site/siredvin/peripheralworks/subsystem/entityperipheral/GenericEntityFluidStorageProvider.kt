package site.siredvin.peripheralworks.subsystem.entityperipheral

import net.minecraft.world.entity.Entity
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStorageLookup
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.plugins.FluidStoragePlugin
import site.siredvin.tweakium.modules.plugins.PeripheralPluginUtils

object GenericEntityFluidStorageProvider : EntityPeripheralPluginProvider {
    override val pluginType: String
        get() = PeripheralPluginUtils.Type.FLUID_STORAGE
    override val conflictWith: Set<String>
        get() = setOf(PeripheralPluginUtils.Type.FLUID_STORAGE)

    override fun provide(entity: Entity): IPeripheralPlugin? {
        val entityStorage = AgnosticFluidStorageLookup.extractFromEntity(entity.level(), entity, null) ?: return null
        return FluidStoragePlugin(entity.level(), entityStorage, PeripheralWorksConfig.fluidStorageTransferLimit)
    }
}
