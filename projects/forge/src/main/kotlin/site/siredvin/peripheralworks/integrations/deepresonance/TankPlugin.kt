package site.siredvin.peripheralworks.integrations.deepresonance

import mcjty.deepresonance.modules.core.CoreModule
import mcjty.deepresonance.modules.tank.blocks.TankTileEntity
import mcjty.deepresonance.util.LiquidCrystalData
import site.siredvin.peripheralium.extra.plugins.FluidStoragePlugin
import site.siredvin.peripheralium.storages.fluid.ForgeFluidStorage
import site.siredvin.peripheralium.storages.fluid.toForge
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

class TankPlugin(target: TankTileEntity) : FluidStoragePlugin(target.level!!, ForgeFluidStorage(target.fluidHandler), PeripheralWorksConfig.fluidStorageTransferLimit) {

    override fun fluidInformation(fluid: site.siredvin.peripheralium.storages.fluid.FluidStack): MutableMap<String, Any?> {
        val base = super.fluidInformation(fluid)
        if (fluid.fluid.isSame(CoreModule.LIQUID_CRYSTAL.get())) {
            val data = LiquidCrystalData.fromStack(fluid.toForge())
            base["purity"] = data.purity
            base["quality"] = data.quality
            base["strength"] = data.strength
            base["efficiency"] = data.efficiency
        }
        return base
    }
}
