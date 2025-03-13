package site.siredvin.peripheralworks.computercraft.pocket

import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.common.setup.ModPocketUpgrades
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.tweakium.modules.pocket.StatefulPeripheralPocketUpgrade

class UltimateSensorPocketUpgrade(stack: ItemStack) :
    StatefulPeripheralPocketUpgrade<UltimateSensorPeripheral>(UltimateSensorPeripheral.UPGRADE_ID, stack, UltimateSensorPeripheral::of, {
        @Suppress("UNCHECKED_CAST")
        ModPocketUpgrades.ULTIMATE_SENSOR.get() as UpgradeType<StatefulPeripheralPocketUpgrade<UltimateSensorPeripheral>>
    })
