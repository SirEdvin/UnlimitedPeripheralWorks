package site.siredvin.peripheralworks.integrations.ae2

import appeng.blockentity.grid.AENetworkBlockEntity
import appeng.core.definitions.AEItems
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.broccolium.modules.storage.base.api.AgnosticStorage
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorage
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStorageLookup
import site.siredvin.broccolium.modules.storage.fluid.api.AgnosticFluidStorage
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.peripheralworks.PeripheralWorksClientCore
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModTurtleUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.xplat.ModPlatform

class Integration : Runnable {

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun extractItemStorage(level: Level, pos: BlockPos, entity: BlockEntity?, direction: Direction?): AgnosticStorage<ItemStack, Int>? {
            if (entity !is AENetworkBlockEntity) return null
            val inventory = entity.mainNode.grid?.storageService?.inventory ?: return null
            return AEItemStorage(inventory, entity)
        }

        @Suppress("UNUSED_PARAMETER")
        fun extractFluidStorage(level: Level, pos: BlockPos, entity: BlockEntity?, direction: Direction?): AgnosticFluidStorage? {
            if (entity !is AENetworkBlockEntity) return null
            val inventory = entity.mainNode.grid?.storageService?.inventory ?: return null
            return AEFluidStorage(inventory, entity)
        }

        @Suppress("UNUSED_PARAMETER")
        fun extractEnergyStorage(level: Level, pos: BlockPos, entity: BlockEntity?, direction: Direction?): AgnosticEnergyStorage? {
            if (entity !is AENetworkBlockEntity) return null
            val energyService = entity.mainNode.grid?.energyService ?: return null
            return AEEnergyStorage(energyService, entity)
        }
    }

    override fun run() {
        val wirelessTerminalUpgrade = ModPlatform.registerTurtleUpgrade(
            AE2WirelessTerminalUpgrade.UPGRADE_ID,
            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack -> AE2WirelessTerminalUpgrade(stack) },
        )
        ModTurtleUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(AE2WirelessTerminalUpgrade.UPGRADE_ID, wirelessTerminalUpgrade.get(), AEItems.WIRELESS_TERMINAL.asItem()).requireMod("ae2")
        }
        PeripheralWorksClientCore.EXTRA_TURTLE_MODEL_PROVIDERS.add {
            @Suppress("UNCHECKED_CAST")
            Pair(wirelessTerminalUpgrade.get() as TurtleUpgradeSerialiser<ITurtleUpgrade>, TurtleUpgradeModeller.flatItem())
        }
        ModEnLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.UPGRADE_ID, "Wireless") }
        ModUaLanguageProvider.addHook { it.addTurtle(AE2WirelessTerminalUpgrade.UPGRADE_ID, "Бездротова") }

        if (Configuration.enableStorageIntegrations) {
            AgnosticItemStorageLookup.addBlockLookup(::extractItemStorage)
            AgnosticFluidStorageLookup.addBlockLookup(::extractFluidStorage)
            AgnosticEnergyStorageLookup.addBlockLookup(::extractEnergyStorage)
        }
        if (Configuration.enableMEInterface) {
            ComputerCraftProxy.addProvider(MENetworkBlockPlugin.Provider)
            ComputerCraftProxy.addProvider(AE2CableObjectProvider)
            ComputerCraftProxy.addProvider(AE2InterfaceObjectProvider)
            ComputerCraftProxy.addProvider(AE2PatternProviderObjectProvider)
        }
    }
}
