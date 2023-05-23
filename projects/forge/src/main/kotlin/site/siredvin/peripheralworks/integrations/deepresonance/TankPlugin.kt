package site.siredvin.peripheralworks.integrations.deepresonance

import mcjty.deepresonance.modules.core.CoreModule
import mcjty.deepresonance.modules.tank.blocks.TankTileEntity
import mcjty.deepresonance.util.LiquidCrystalData
import net.minecraftforge.fluids.FluidStack
import site.siredvin.peripheralium.extra.plugins.ForgeFluidStoragePlugin
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

class TankPlugin(private val target: TankTileEntity): ForgeFluidStoragePlugin(target.fluidHandler, PeripheralWorksConfig.fluidStorageTransferLimit) {

    override fun fluidInformation(stack: FluidStack): MutableMap<String, Any> {
        val base = super.fluidInformation(stack)
        if (stack.fluid.isSame(CoreModule.LIQUID_CRYSTAL.get())) {
            val data = LiquidCrystalData.fromStack(stack)
            base["purity"] = data.purity
            base["quality"] = data.quality
            base["strength"] = data.strength
            base["efficiency"] = data.efficiency
        }
        return base
    }
}