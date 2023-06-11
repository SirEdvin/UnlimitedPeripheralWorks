package site.siredvin.peripheralworks.data

import net.minecraft.data.PackOutput
import site.siredvin.peripheralium.data.language.LanguageProvider
import site.siredvin.peripheralium.data.language.toPocketTranslationKey
import site.siredvin.peripheralium.data.language.toTurtleTranslationKey
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import java.util.stream.Stream

abstract class ModLanguageProvider(output: PackOutput, locale: String) : LanguageProvider(
    output,
    PeripheralWorksCore.MOD_ID,
    locale,
    PeripheralWorksPlatform.holder,
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
        val baseStream = super.getExpectedKeys()
        val moreStream = Stream.of(
            extraExpectedKeys.stream(),
            PeripheralWorksPlatform.turtleUpgrades.stream().map { XplatRegistries.TURTLE_SERIALIZERS.getKey(it.get()).toTurtleTranslationKey() },
            PeripheralWorksPlatform.pocketUpgrades.stream().map { XplatRegistries.POCKET_SERIALIZERS.getKey(it.get()).toPocketTranslationKey() },
        ).flatMap { it }
        return Stream.concat(baseStream, moreStream)
    }
}
