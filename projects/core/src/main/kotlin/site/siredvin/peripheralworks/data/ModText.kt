package site.siredvin.peripheralworks.data

import site.siredvin.peripheralium.data.language.TextRecord
import site.siredvin.peripheralworks.PeripheralWorksCore

enum class ModText : TextRecord {
    CREATIVE_TAB,
    REMOTE_OBSERVER_NOT_SELF,
    REMOTE_OBSERVER_TOO_FAR,
    REMOTE_OBSERVER_TOO_MANY,
    REMOTE_OBSERVER_BLOCK_ADDED,
    REMOTE_OBSERVER_BLOCK_REMOVED,
    PERIPHERAL_PROXY_NOT_SELF,
    PERIPHERAL_PROXY_TOO_FAR,
    PERIPHERAL_PROXY_TOO_MANY,
    PERIPHERAL_PROXY_IS_NOT_A_PERIPHERAL,
    PERIPHERAL_PROXY_FORBIDDEN,
    PERIPHERAL_PROXY_BLOCK_ADDED,
    PERIPHERAL_PROXY_BLOCK_REMOVED,
    TECH_REBORN_ENERGY,
    ;

    override val textID: String by lazy {
        String.format("text.%s.%s", PeripheralWorksCore.MOD_ID, name.lowercase())
    }
}
