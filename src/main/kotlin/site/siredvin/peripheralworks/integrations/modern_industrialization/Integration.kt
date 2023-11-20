package site.siredvin.peripheralworks.integrations.modern_industrialization

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.data.BlockTagsProvider

class Integration : Runnable {
    override fun run() {
        ComputerCraftProxy.addProvider(EnergyStoragePlugin.Provider())
        ComputerCraftProxy.addProvider(CraftingMachinePlugin.Provider())
        PeripheralWorksConfig.registerIntegrationConfiguration("modern_industrialization", Configuration)
        BlockTagsProvider.DEFERRED_FLUID_STORAGE_BLOCKS_SUP.add { Registry.BLOCK.get(ResourceLocation("modern_industrialization", "large_tank")) }
    }
}
