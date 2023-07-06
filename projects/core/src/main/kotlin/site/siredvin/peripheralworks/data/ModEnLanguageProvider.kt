package site.siredvin.peripheralworks.data

import net.minecraft.data.PackOutput
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheraliumHubPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.peripheralworks.computercraft.peripherals.UniversalScannerPeripheral
import java.util.function.Consumer

class ModEnLanguageProvider(
    output: PackOutput,
) : ModLanguageProvider(output, "en_us") {

    companion object {
        private val hooks: MutableList<Consumer<ModEnLanguageProvider>> = mutableListOf()

        fun addHook(hook: Consumer<ModEnLanguageProvider>) {
            hooks.add(hook)
        }
    }

    override fun addTranslations() {
        add(Items.ULTIMATE_CONFIGURATOR.get(), "Peripheralium hub")
        add(Items.PERIPHERALIUM_HUB.get(), "Netherite peripheralium hub")
        add(Items.NETHERITE_PERIPHERALIUM_HUB.get(), "Ultimate configurator", "§3§oOne tool to configure them all. Crouch click on any block to check if it configurable")

        add(Blocks.PERIPHERAL_CASING.get(), "Peripheral casing")
        add(Blocks.UNIVERSAL_SCANNER.get(), "Universal scanner", "§3§oBest tool to inspect surroundings, if you don't have third eye. Works with turtles and pocket computers")
        add(Blocks.ULTIMATE_SENSOR.get(), "Ultimate sensor", "§3§oLimitless possibilities for understanding world in one little box. Works with turtles and pocket computers")
        add(Blocks.ITEM_PEDESTAL.get(), "Item pedestal", "§3§oCute pedestal, that can store only single item stack. But seems you can get more information about this stack now")
        add(Blocks.MAP_PEDESTAL.get(), "Map pedestal", "§3§oCute pedestal, that can store only single item stack. Infused with compass, it now can understand maps")
        add(Blocks.DISPLAY_PEDESTAL.get(), "Display pedestal", "§3§oCute pedestal, that cannot store anything, but can display literally anything")
        add(Blocks.REMOTE_OBSERVER.get(), "Remote observer", "§3§oJust like observer, but can see a little further")
        add(Blocks.PERIPHERAL_PROXY.get(), "Peripheral proxy", "§3§oAllows you to connect peripherals at the small distances without wired network")
        add(Blocks.FLEXIBLE_REALITY_ANCHOR.get(), "Flexible reality anchor")
        add(Blocks.REALITY_FORGER.get(), "Reality forger", "§3§oComplex device, that can forge reality anchors to something else")
        add(Blocks.RECIPE_REGISTRY.get(), "Recipe registry", "§3§oKnowledge device, that holds power to understand any recipe but sometimes luck power to provide information about it")

        add(ModText.CREATIVE_TAB, "UnlimitedPeripheralWorks")

        add(ModText.REMOTE_OBSERVER_NOT_SELF, "Cannot connect remote observer to itself")
        add(ModText.REMOTE_OBSERVER_TOO_FAR, "Remote observer too far from this block")
        add(ModText.REMOTE_OBSERVER_TOO_MANY, "Too many blocks already connected to this remote observer")
        add(ModText.REMOTE_OBSERVER_BLOCK_ADDED, "Remote observer now tracks this block")
        add(ModText.REMOTE_OBSERVER_BLOCK_REMOVED, "Remote observer will not track this block from now")

        add(ModText.PERIPHERAL_PROXY_NOT_SELF, "Cannot connect peripheral proxy to itself")
        add(ModText.PERIPHERAL_PROXY_TOO_MANY, "Too many peripherals already connected to this peripheral proxy")
        add(ModText.PERIPHERAL_PROXY_TOO_FAR, "Peripheral proxy too far from this block")
        add(ModText.PERIPHERAL_PROXY_IS_NOT_A_PERIPHERAL, "This block does not contains a peripheral")
        add(ModText.PERIPHERAL_PROXY_FORBIDDEN, "This block is forbidden to add to peripheral proxy")
        add(ModText.PERIPHERAL_PROXY_BLOCK_ADDED, "This peripheral is connected to peripheral proxy")
        add(ModText.PERIPHERAL_PROXY_BLOCK_REMOVED, "This peripheral is disconnected from peripheral proxy")
        add(ModText.DEFINITELY_NOT, "§3§oDefinitely not a ")

        add(ModText.TECH_REBORN_ENERGY, "Tech reborn energy")

        add(ModTooltip.ITEM_DISABLED, "  §4Item disabled in configuration")
        add(ModTooltip.PERIPHERALIUM_HUB_MAX_PERIPHERALS, "  §6Max upgrades inside hub: %s")
        add(ModTooltip.UNIVERSAL_SCANNER_FREE_RANGE, "  §6Cost-free scan range: %s")
        add(ModTooltip.UNIVERSAL_SCANNER_MAX_RANGE, "  §6Max scan range: %s")
        add(ModTooltip.REMOTE_OBSERVER_MODE, "  Remote observer configuration")
        add(ModTooltip.PERIPHERAL_PROXY_MODE, "  Peripheral proxy configuration")
        add(ModTooltip.ACTIVE_CONFIGURATION_MODE, "Active configuration mode:")
        add(ModTooltip.CONFIGURATION_TARGET_BLOCK, "Target block %s")
        add(ModTooltip.REMOTE_OBSERVER_RANGE, "  §6Max range of observed block: %s")
        add(ModTooltip.REMOTE_OBSERVER_MAX_CAPACITY, "  §6Max connected observed block count: %s")
        add(ModTooltip.PERIPHERAL_PROXY_RANGE, "  §6Max range of connected peripherals: %s")
        add(ModTooltip.PERIPHERAL_PROXY_MAX_CAPACITY, "  §6Max connected peripherals count: %s")
        add(ModTooltip.REALITY_FORGER_RANGE, "  §6Max forging range: %s")

        addUpgrades(PeripheraliumHubPeripheral.ID, "Hub")
        addUpgrades(PeripheraliumHubPeripheral.NETHERITE_ID, "Netherite Hub")
        addUpgrades(UniversalScannerPeripheral.UPGRADE_ID, "Scanning")
        addUpgrades(UltimateSensorPeripheral.UPGRADE_ID, "Sensing")

        hooks.forEach { it.accept(this) }
    }
}
