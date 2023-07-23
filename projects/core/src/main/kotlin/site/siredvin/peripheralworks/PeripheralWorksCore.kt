package site.siredvin.peripheralworks
import net.minecraft.world.item.CreativeModeTab
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import site.siredvin.peripheralium.storages.item.ItemStorageExtractor
import site.siredvin.peripheralium.xplat.BaseInnerPlatform
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.EnergyStorageProvider
import site.siredvin.peripheralworks.computercraft.FluidStorageProvider
import site.siredvin.peripheralworks.computercraft.StorageProvider
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificPluginProvider
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificProtectedPluginProviders
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.utils.MinecartUtils
import site.siredvin.peripheralworks.xplat.*

@Suppress("UNUSED")
object PeripheralWorksCore {
    const val MOD_ID = "peripheralworks"

    var LOGGER: Logger = LogManager.getLogger(MOD_ID)

    fun configureCreativeTab(builder: CreativeModeTab.Builder): CreativeModeTab.Builder {
        return builder.icon { Blocks.PERIPHERAL_CASING.get().asItem().defaultInstance }
            .title(ModText.CREATIVE_TAB.text)
            .displayItems { _, output ->
                ModPlatform.holder.blocks.forEach { output.accept(it.get()) }
                ModPlatform.holder.items.forEach { output.accept(it.get()) }
                PeripheralWorksCommonHooks.registerUpgradesInCreativeTab(output)
            }
    }

    fun configure(platform: BaseInnerPlatform, ingredients: ModRecipeIngredients, blocks: ModBlocksReference) {
        ModPlatform.configure(platform)
        ModRecipeIngredients.configure(ingredients)
        ModBlocksReference.configure(blocks)
        ItemStorageExtractor.addStorageExtractor(MinecartUtils::minecartExtractor)
        ComputerCraftProxy.addProvider(StorageProvider)
        ComputerCraftProxy.addProvider(FluidStorageProvider)
        ComputerCraftProxy.addProvider(EnergyStorageProvider)
        ComputerCraftProxy.addProvider(SpecificPluginProvider)
        ComputerCraftProxy.addProvider(SpecificProtectedPluginProviders)
    }
}
