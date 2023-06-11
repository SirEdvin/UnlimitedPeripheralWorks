package site.siredvin.peripheralworks.xplat

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.setup.*

object PeripheralWorksCommonHooks {

    fun onRegister() {
        BlockEntityTypes.doSomething()
        Items.doSomething()
        Blocks.doSomething()
        PocketUpgradeSerializers.doSomething()
        TurtleUpgradeSerializers.doSomething()
        PeripheralWorksPlatform.registerCreativeTab(
            ResourceLocation(PeripheralWorksCore.MOD_ID, "tab"),
            PeripheralWorksCore.configureCreativeTab(PeripheraliumPlatform.createTabBuilder()).build(),
        )
    }

    fun registerUpgradesInCreativeTab(output: CreativeModeTab.Output) {
        PeripheralWorksPlatform.turtleUpgrades.forEach {
            val upgrade = PeripheraliumPlatform.getTurtleUpgrade(it.toString())
            if (upgrade != null) {
                PeripheraliumPlatform.createTurtlesWithUpgrade(upgrade).forEach(output::accept)
            }
        }
        PeripheralWorksPlatform.pocketUpgrades.forEach {
            val upgrade = PeripheraliumPlatform.getPocketUpgrade(it.toString())
            if (upgrade != null) {
                PeripheraliumPlatform.createPocketsWithUpgrade(upgrade).forEach(output::accept)
            }
        }
    }
}
