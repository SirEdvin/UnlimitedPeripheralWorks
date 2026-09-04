package site.siredvin.peripheralworks.common.configuration.integration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.common.configuration.IntegrationConfiguration

object AE2Configuration : IntegrationConfiguration {

    override val modID: String
        get() = "ae2"

    private const val DEFAULT_MAX_SUBSCRIPTIONS = 16
    private const val DEFAULT_MAX_ITEM_FILTER_SIZE = 1024

    private var enableMeInterfaceConfig: ForgeConfigSpec.BooleanValue? = null
    private var enableStorageIntegrationConfig: ForgeConfigSpec.BooleanValue? = null
    private var maxSubscriptionsConfig: ForgeConfigSpec.IntValue? = null
    private var maxItemFilterSizeConfig: ForgeConfigSpec.IntValue? = null

    val enableMEInterface: Boolean
        get() = enableMeInterfaceConfig?.get() ?: true

    val enableStorageIntegrations: Boolean
        get() = enableStorageIntegrationConfig?.get() ?: true

    val maxSubscriptions: Int
        get() = maxSubscriptionsConfig?.get() ?: DEFAULT_MAX_SUBSCRIPTIONS

    val maxItemFilterSize: Int
        get() = maxItemFilterSizeConfig?.get() ?: DEFAULT_MAX_ITEM_FILTER_SIZE

    override val name: String
        get() = "ae2"

    override fun addToConfig(builder: ForgeConfigSpec.Builder) {
        enableMeInterfaceConfig = builder.comment("Enables the ME network peripheral").define("enableMEInterface", true)
        enableStorageIntegrationConfig = builder.comment("Enables me integration with storages").define("enableStorageIntegrations", true)
        maxSubscriptionsConfig = builder.comment("Maximum AE2 storage subscriptions per peripheral or computer upgrade")
            .defineInRange("maxSubscriptions", DEFAULT_MAX_SUBSCRIPTIONS, 1, Int.MAX_VALUE)
        maxItemFilterSizeConfig = builder.comment("Maximum number of values in a persisted AE2 item subscription filter")
            .defineInRange("maxItemFilterSize", DEFAULT_MAX_ITEM_FILTER_SIZE, 1, Int.MAX_VALUE)
    }
}
