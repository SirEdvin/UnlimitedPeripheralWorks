package site.siredvin.peripheralworks.data

import site.siredvin.broccolium.modules.data.api.TextRecord
import site.siredvin.peripheralworks.PeripheralWorksCore

enum class ModEnergiesText : TextRecord {
    MERCURY_FLUX,
    EMBER,
    SOURCE,
    ;

    override val textID: String by lazy {
        String.format("text.%s.%s", PeripheralWorksCore.MOD_ID, name.lowercase())
    }
}
