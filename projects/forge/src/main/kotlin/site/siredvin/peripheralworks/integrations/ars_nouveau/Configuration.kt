package site.siredvin.peripheralworks.integrations.ars_nouveau

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.api.IForgeConfigHandler

object Configuration : IForgeConfigHandler {
    override val name: String
        get() = "ars_nouveau"

    private var enableCasterTomePocketUpgradeConfig: ForgeConfigSpec.BooleanValue? = null

    val enableCasterTomePocketUpgrade: Boolean
        get() = enableCasterTomePocketUpgradeConfig?.get() != false

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableCasterTomePocketUpgradeConfig = builder.comment("Enables usage of caster tome as pocket computer upgrade")
            .define("enableCasterTomePocketUpgrade", true)
    }
}
