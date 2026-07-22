package site.siredvin.peripheralworks.data

import net.minecraft.data.PackOutput
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.data.ComputerLanguageProvider
import java.util.stream.Stream

abstract class ModLanguageProvider(output: PackOutput, locale: String) :
    ComputerLanguageProvider(
        output,
        PeripheralWorksCore.MOD_ID,
        locale,
        ModPlatform.holder,
        *ModText.entries.toTypedArray(),
        *ModTooltip.entries.toTypedArray(),
        *ModEnergiesText.entries.toTypedArray(),
    ) {

    companion object {
        private val extraExpectedKeys: MutableList<String> = mutableListOf()

        fun addExpectedKey(key: String) {
            extraExpectedKeys.add(key)
        }
    }

    override fun getExpectedKeys(): Stream<String> = Stream.concat(super.getExpectedKeys(), extraExpectedKeys.stream())
}
