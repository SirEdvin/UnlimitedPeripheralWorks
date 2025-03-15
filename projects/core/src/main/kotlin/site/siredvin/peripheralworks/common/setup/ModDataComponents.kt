package site.siredvin.peripheralworks.common.setup

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import dan200.computercraft.impl.PocketUpgrades
import dan200.computercraft.impl.TurtleUpgrades
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.components.PeripheralUpgrades
import site.siredvin.peripheralworks.xplat.ModPlatform

object ModDataComponents {
    val POCKET_UPGRADES = ModPlatform.registerDataComponent(
        ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "pocket_upgrades"),
        DataComponentType.builder<PeripheralUpgrades<IPocketUpgrade>>()
            .persistent(
                PocketUpgrades.instance().upgradeDataCodec().listOf().xmap(
                    { upgrades: MutableList<UpgradeData<IPocketUpgrade>> -> PeripheralUpgrades(upgrades) },
                    { upgrades: PeripheralUpgrades<IPocketUpgrade> -> upgrades.upgrades },
                ),
            )
            .networkSynchronized(
                PocketUpgrades.instance().upgradeDataStreamCodec().apply(ByteBufCodecs.list()).map(
                    { upgrades: MutableList<UpgradeData<IPocketUpgrade>> -> PeripheralUpgrades(upgrades) },
                    { upgrades: PeripheralUpgrades<IPocketUpgrade> -> upgrades.upgrades },
                ),
            ),
    )
    val TURTLE_UPGRADES = ModPlatform.registerDataComponent(
        ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "turtle_upgrades"),
        DataComponentType.builder<PeripheralUpgrades<ITurtleUpgrade>>()
            .persistent(
                TurtleUpgrades.instance().upgradeDataCodec().listOf().xmap(
                    { upgrades: MutableList<UpgradeData<ITurtleUpgrade>> -> PeripheralUpgrades(upgrades) },
                    { upgrades: PeripheralUpgrades<ITurtleUpgrade> -> upgrades.upgrades },
                ),
            )
            .networkSynchronized(
                TurtleUpgrades.instance().upgradeDataStreamCodec().apply(ByteBufCodecs.list()).map(
                    { upgrades: MutableList<UpgradeData<ITurtleUpgrade>> -> PeripheralUpgrades(upgrades) },
                    { upgrades: PeripheralUpgrades<ITurtleUpgrade> -> upgrades.upgrades },
                ),
            ),
    )

    fun doSomething() {}
}
