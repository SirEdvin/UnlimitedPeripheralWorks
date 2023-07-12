package site.siredvin.peripheralworks.data

import site.siredvin.peripheralium.data.language.TextRecord
import site.siredvin.peripheralworks.PeripheralWorksCore

enum class ModTooltip : TextRecord {
    ITEM_DISABLED,
    PERIPHERALIUM_HUB_MAX_PERIPHERALS,
    PERIPHERALIUM_HUB_STORED,
    PERIPHERALIUM_HUB_TURTLE,
    PERIPHERALIUM_HUB_POCKET,
    UNIVERSAL_SCANNER_FREE_RANGE,
    UNIVERSAL_SCANNER_MAX_RANGE,
    REMOTE_OBSERVER_MODE,
    PERIPHERAL_PROXY_MODE,
    ACTIVE_CONFIGURATION_MODE,
    CONFIGURATION_TARGET_BLOCK,
    REMOTE_OBSERVER_RANGE,
    REMOTE_OBSERVER_MAX_CAPACITY,
    PERIPHERAL_PROXY_RANGE,
    PERIPHERAL_PROXY_MAX_CAPACITY,
    REALITY_FORGER_RANGE,
    FLEXIBLE_STATUE_AUTHOR,
    ;

    override val textID: String by lazy {
        String.format("tooltip.%s.%s", PeripheralWorksCore.MOD_ID, name.lowercase())
    }
}
