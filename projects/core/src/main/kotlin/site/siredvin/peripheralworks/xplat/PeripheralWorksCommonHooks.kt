package site.siredvin.peripheralworks.xplat

import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.setup.*

object PeripheralWorksCommonHooks {

    fun onRegister() {
        BlockEntityTypes.doSomething()
        Items.doSomething()
        Blocks.doSomething()
        RecipeSerializers.doSomething()
        PocketUpgradeSerializers.doSomething()
        TurtleUpgradeSerializers.doSomething()
        ModPlatform.registerCreativeTab(
            ResourceLocation(PeripheralWorksCore.MOD_ID, "tab"),
            PeripheralWorksCore.configureCreativeTab(PeripheraliumPlatform.createTabBuilder()).build(),
        )
    }

    fun registerUpgradesInCreativeTab(output: CreativeModeTab.Output) {
        ModPlatform.holder.turtleSerializers.forEach {
            val upgrade = PeripheraliumPlatform.getTurtleUpgrade(XplatRegistries.TURTLE_SERIALIZERS.getKey(it.get()).toString())
            if (upgrade != null) {
                PeripheraliumPlatform.createTurtlesWithUpgrade(UpgradeData.ofDefault(upgrade)).forEach(output::accept)
            }
        }
        ModPlatform.holder.pocketSerializers.forEach {
            val upgrade = PeripheraliumPlatform.getPocketUpgrade(XplatRegistries.POCKET_SERIALIZERS.getKey(it.get()).toString())
            if (upgrade != null) {
                PeripheraliumPlatform.createPocketsWithUpgrade(UpgradeData.ofDefault(upgrade)).forEach(output::accept)
            }
        }
    }
}
