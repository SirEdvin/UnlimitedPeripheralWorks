package site.siredvin.peripheralworks.data

import net.minecraft.data.PackOutput
import site.siredvin.peripheralium.data.language.LanguageProvider
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.stream.Stream

abstract class ModLanguageProvider(output: PackOutput, locale: String) : LanguageProvider(
    output,
    PeripheralWorksCore.MOD_ID,
    locale,
    ModPlatform.holder,
    *ModText.values(),
    *ModTooltip.values(),
) {

    companion object {
        private val extraExpectedKeys: MutableList<String> = mutableListOf()

        fun addExpectedKey(key: String) {
            extraExpectedKeys.add(key)
        }
    }

    override fun getExpectedKeys(): Stream<String> {
        return Stream.concat(super.getExpectedKeys(), extraExpectedKeys.stream())
    }
}
