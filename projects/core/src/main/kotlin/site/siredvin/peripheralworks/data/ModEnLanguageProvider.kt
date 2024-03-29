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
        add(Items.ULTIMATE_CONFIGURATOR.get(), "Ultimate configurator", "§3§oOne tool to configure them all. Crouch click on any block to check if it configurable")
        add(Items.PERIPHERALIUM_HUB.get(), "Peripheralium hub")
        add(Items.NETHERITE_PERIPHERALIUM_HUB.get(), "Netherite peripheralium hub")
        add(Items.ANALYZER.get(), "Analyzer", "§3§oWIP tool that now can only display block entity type of blocks")
        add(Items.ENTITY_CARD.get(), "Entity card", "§3§oCan record unique signature of some living or ephemeral entities")

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
        add(Blocks.INFORMATIVE_REGISTRY.get(), "Informative registry", "§3§oKnowledge device, that holds power to list all items in existence")
        add(Blocks.FLEXIBLE_STATUE.get(), "Flexible statue")
        add(Blocks.STATUE_WORKBENCH.get(), "Statue workbench", "§3§oImagination your only limit. And also 48 point size limit, but this is not so important")
        add(Blocks.ENTITY_LINK.get(), "Entity link", "§3§oCan help you to create interspace link to any entity, you just need to insert card")

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
        add(ModText.TARGET_ENTITY, "Entity %s recorded in card, currently at %s")
        add(ModText.ENTITY_CANNOT_BE_STORED, "This entity cannot be recorded inside card")
        add(ModText.SOMETHING_STORED_INSIDE_CARD, "§e§oCard pointing at entity, use to know more")
        add(ModText.ITEM_IS_NOT_SUITABLE_FOR_UPGRADE, "Item is not suitable for upgrade")
        add(ModText.ENTITY_LINK_UPGRADES, "List of entity link upgrades:")
        add(ModText.ENTITY_LINK_DOES_NOT_HAVE_UPGRADES, "Entity link doesn't have any upgrades")
        add(ModText.ENTITY_LINK_UPGRADE_SCANNER, "Scanner")

        add(ModText.TECH_REBORN_ENERGY, "Tech reborn energy")

        add(ModTooltip.ITEM_DISABLED, "  §4Item disabled in configuration")
        add(ModTooltip.PERIPHERALIUM_HUB_MAX_PERIPHERALS, "  §6Max upgrades inside hub: %s")
        add(ModTooltip.PERIPHERALIUM_HUB_STORED, "  §6Stored upgrades:")
        add(ModTooltip.PERIPHERALIUM_HUB_POCKET, "  §6Active mode: pocket computer")
        add(ModTooltip.PERIPHERALIUM_HUB_TURTLE, "  §6Active mode: turtle")
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
        add(ModTooltip.FLEXIBLE_STATUE_AUTHOR, "Author: %s")
        add(ModTooltip.ENTITY_LINK_MODE, "  Entity link configuration")

        addUpgrades(PeripheraliumHubPeripheral.ID, "Hub")
        addUpgrades(PeripheraliumHubPeripheral.NETHERITE_ID, "Netherite Hub")
        addUpgrades(UniversalScannerPeripheral.UPGRADE_ID, "Scanning")
        addUpgrades(UltimateSensorPeripheral.UPGRADE_ID, "Sensing")

        hooks.forEach { it.accept(this) }
    }
}
