package site.siredvin.peripheralworks
import net.minecraft.world.item.CreativeModeTab
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import site.siredvin.peripheralium.api.storage.ExtractorProxy
import site.siredvin.peripheralium.util.text
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.StorageProvider
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificPluginProvider
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificProtectedPluginProviders
import site.siredvin.peripheralworks.utils.MinecartUtils
import site.siredvin.peripheralworks.xplat.ModRecipeIngredients
import site.siredvin.peripheralworks.xplat.PeripheralWorksCommonHooks
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform

@Suppress("UNUSED")
object PeripheralWorksCore {
    const val MOD_ID = "peripheralworks"

    var LOGGER: Logger = LogManager.getLogger(MOD_ID)

    fun configureCreativeTab(builder: CreativeModeTab.Builder): CreativeModeTab.Builder {
        return builder.icon { Blocks.PERIPHERAL_CASING.get().asItem().defaultInstance }
            .title(text(MOD_ID, "creative_tab"))
            .displayItems { _, output ->
                PeripheralWorksPlatform.ITEMS.forEach { output.accept(it.get()) }
                PeripheralWorksPlatform.BLOCKS.forEach { output.accept(it.get()) }
                PeripheralWorksCommonHooks.registerUpgradesInCreativeTab(output)
            }
    }

    fun configure(platform: PeripheralWorksPlatform, ingredients: ModRecipeIngredients) {
        PeripheralWorksPlatform.configure(platform)
        ModRecipeIngredients.configure(ingredients)
        ExtractorProxy.addStorageExtractor(MinecartUtils::minecartExtractor)
        ComputerCraftProxy.addProvider(StorageProvider)
        ComputerCraftProxy.addProvider(SpecificPluginProvider)
        ComputerCraftProxy.addProvider(SpecificProtectedPluginProviders)
    }
}
