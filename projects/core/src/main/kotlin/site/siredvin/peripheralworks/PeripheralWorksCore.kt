package site.siredvin.peripheralworks
import net.minecraft.world.item.CreativeModeTab
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import site.siredvin.peripheralium.api.storage.ExtractorProxy
import site.siredvin.peripheralium.util.text
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.StorageProvider
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificPluginProvider
import site.siredvin.peripheralworks.computercraft.plugins.specific.SpecificProtectedPluginProviders
import site.siredvin.peripheralworks.utils.MinecartUtils
import site.siredvin.peripheralworks.xplat.ModRecipeIngredients
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform


@Suppress("UNUSED")
object PeripheralWorksCore {
    const val MOD_ID = "peripheralworks"

    var LOGGER: Logger = LogManager.getLogger(MOD_ID)

    fun configureCreativeTab(builder: CreativeModeTab.Builder): CreativeModeTab.Builder {
        return builder.icon { Blocks.PERIPHERAL_CASING.get().asItem().defaultInstance }
            .title(text(MOD_ID, "creative_tab"))
            .displayItems { _, output ->
                output.accept(Items.PERIPHERALIUM_HUB.get().defaultInstance)
                output.accept(Items.NETHERITE_PERIPHERALIUM_MODEM.get().defaultInstance)
                output.accept(Blocks.PERIPHERAL_CASING.get().asItem().defaultInstance)
                output.accept(Blocks.UNIVERSAL_SCANNER.get().asItem().defaultInstance)
                output.accept(Blocks.ULTIMATE_SENSOR.get().asItem().defaultInstance)
                output.accept(Blocks.ITEM_PEDESTAL.get().asItem().defaultInstance)
                output.accept(Blocks.MAP_PEDESTAL.get().asItem().defaultInstance)
                output.accept(Blocks.DISPLAY_PEDESTAL.get().asItem().defaultInstance)
                PeripheraliumPlatform.getTurtleUpgrade(PeripheraliumHubPeripheral.ID.toString())
                    ?.let { PeripheraliumPlatform.createTurtlesWithUpgrade(it).forEach(output::accept) }
                PeripheraliumPlatform.getTurtleUpgrade(PeripheraliumHubPeripheral.NETHERITE_ID.toString())
                    ?.let { PeripheraliumPlatform.createTurtlesWithUpgrade(it).forEach(output::accept) }
                PeripheraliumPlatform.getTurtleUpgrade(UniversalScannerPeripheral.UPGRADE_ID.toString())
                    ?.let { PeripheraliumPlatform.createTurtlesWithUpgrade(it).forEach(output::accept) }
                PeripheraliumPlatform.getTurtleUpgrade(UltimateSensorPeripheral.UPGRADE_ID.toString())
                    ?.let { PeripheraliumPlatform.createTurtlesWithUpgrade(it).forEach(output::accept) }

                PeripheraliumPlatform.getPocketUpgrade(PeripheraliumHubPeripheral.ID.toString())
                    ?.let { PeripheraliumPlatform.createPocketsWithUpgrade(it).forEach(output::accept) }
                PeripheraliumPlatform.getPocketUpgrade(PeripheraliumHubPeripheral.NETHERITE_ID.toString())
                    ?.let { PeripheraliumPlatform.createPocketsWithUpgrade(it).forEach(output::accept) }
                PeripheraliumPlatform.getPocketUpgrade(UniversalScannerPeripheral.UPGRADE_ID.toString())
                    ?.let { PeripheraliumPlatform.createPocketsWithUpgrade(it).forEach(output::accept) }
                PeripheraliumPlatform.getPocketUpgrade(UltimateSensorPeripheral.UPGRADE_ID.toString())
                    ?.let { PeripheraliumPlatform.createPocketsWithUpgrade(it).forEach(output::accept) }
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