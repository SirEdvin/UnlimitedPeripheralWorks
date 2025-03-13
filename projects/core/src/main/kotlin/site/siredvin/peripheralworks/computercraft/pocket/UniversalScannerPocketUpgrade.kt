package site.siredvin.peripheralworks.computercraft.pocket

import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.common.setup.ModPocketUpgrades
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.tweakium.modules.pocket.StatefulPeripheralPocketUpgrade

class UniversalScannerPocketUpgrade(stack: ItemStack) :
    StatefulPeripheralPocketUpgrade<UniversalScannerPeripheral>(UniversalScannerPeripheral.UPGRADE_ID, stack, UniversalScannerPeripheral::of, {
        @Suppress("UNCHECKED_CAST")
        ModPocketUpgrades.UNIVERSAL_SCANNER.get() as UpgradeType<StatefulPeripheralPocketUpgrade<UniversalScannerPeripheral>>
    })
