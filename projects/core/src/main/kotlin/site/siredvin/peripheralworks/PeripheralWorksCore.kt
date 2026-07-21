package site.siredvin.peripheralworks
import dan200.computercraft.api.ComputerCraftAPI
import net.minecraft.world.item.CreativeModeTab
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import site.siredvin.broccolium.modules.storage.item.AgnosticItemStorageLookup
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.computercraft.*
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificPluginProvider
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificProtectedPluginProviders
import site.siredvin.peripheralworks.data.ModText
import site.siredvin.peripheralworks.subsystem.entityperipheral.EntityPeripheralLookup
import site.siredvin.peripheralworks.subsystem.entityperipheral.GenericEntityFluidStorageProvider
import site.siredvin.peripheralworks.subsystem.entityperipheral.GenericEntityStorageProvider
import site.siredvin.peripheralworks.utils.MinecartUtils
import site.siredvin.peripheralworks.xplat.*
import site.siredvin.tweakium.modules.peripheral.util.CreativeTabUtil

@Suppress("UNUSED")
object PeripheralWorksCore {
    const val MOD_ID = "peripheralworks"
    const val NETWORK_VERSION = "1.0"

    var logger: Logger = LogManager.getLogger(MOD_ID)

    fun configureCreativeTab(builder: CreativeModeTab.Builder): CreativeModeTab.Builder = builder.icon { Blocks.PERIPHERAL_CASING.get().asItem().defaultInstance }
        .title(ModText.CREATIVE_TAB.text)
        .displayItems { context, output ->
            ModPlatform.holder.blocks.forEach { output.accept(it.get()) }
            ModPlatform.holder.items.forEach { output.accept(it.get()) }
            CreativeTabUtil.enrichCreativeTabWithUpgrades(MOD_ID, output, context.holders)
        }

    fun configure(platform: ModInnerPlatform, ingredients: ModRecipeIngredients, blocks: ModBlocksReference) {
        ModPlatform.configure(platform)
        ModRecipeIngredients.configure(ingredients)
        ModBlocksReference.configure(blocks)
        AgnosticItemStorageLookup.addBlockLookup(MinecartUtils::minecartExtractor)
        ComputerCraftProxy.addProvider(StorageProvider)
        ComputerCraftProxy.addProvider(FluidStorageProvider)
        ComputerCraftProxy.addProvider(EnergyStorageProvider)
        ComputerCraftProxy.addProvider(SpecificPluginProvider)
        ComputerCraftProxy.addProvider(SpecificProtectedPluginProviders)
        EntityPeripheralLookup.addProvider(GenericEntityStorageProvider)
        EntityPeripheralLookup.addProvider(GenericEntityFluidStorageProvider)
        ComputerCraftAPI.registerRefuelHandler(EnergyRefuelHandler)
    }
}
