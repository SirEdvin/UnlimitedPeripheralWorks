package site.siredvin.peripheralworks.integrations.deepresonance

import mcjty.deepresonance.modules.core.CoreModule
import mcjty.deepresonance.modules.tank.blocks.TankTileEntity
import mcjty.deepresonance.util.LiquidCrystalData
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStack
import site.siredvin.broccolium.modules.storage.fluid.ForgeAgnosticFluidStorage
import site.siredvin.broccolium.modules.storage.fluid.toForge
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.plugins.FluidStoragePlugin

class TankPlugin(target: TankTileEntity) : FluidStoragePlugin(target.level!!, ForgeAgnosticFluidStorage(target.fluidHandler), PeripheralWorksConfig.fluidStorageTransferLimit) {

    override fun fluidInformation(fluid: AgnosticFluidStack): MutableMap<String, Any?> {
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
