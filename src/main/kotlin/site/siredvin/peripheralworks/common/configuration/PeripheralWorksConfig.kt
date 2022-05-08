package site.siredvin.peripheralworks.common.configuration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.IConfigHandler

object PeripheralWorksConfig {

    private val INTEGRATION_CONFIGURATIONS: MutableMap<String, IConfigHandler> = mutableMapOf()

    val enableGenericInventory: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_GENERIC_INVENTORY.get()
    val enableGenericItemStorage: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_GENERIC_ITEM_STORAGE.get()
    val enableGenericFluidStorage: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_GENERIC_FLUID_STORAGE.get()

    val enableBeacon: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_BEACON.get()
    val enableNoteBlock: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_NOTEBLOCK.get()
    val enableLectern: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_LECTERN.get()
    val enableJukebox: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_JUKEBOX.get()
    val enablePoweredRail: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_POWERED_RAIL.get()

    fun registerIntegrationConfiguration(name: String, configuration: IConfigHandler) {
        INTEGRATION_CONFIGURATIONS[name] = configuration
    }

    class CommonConfig internal constructor(builder: ForgeConfigSpec.Builder) {

        // Generic plugins
        var ENABLE_GENERIC_INVENTORY: ForgeConfigSpec.BooleanValue
        var ENABLE_GENERIC_ITEM_STORAGE: ForgeConfigSpec.BooleanValue
        var ENABLE_GENERIC_FLUID_STORAGE: ForgeConfigSpec.BooleanValue
        // Specific plugins
        var ENABLE_BEACON: ForgeConfigSpec.BooleanValue
        var ENABLE_NOTEBLOCK: ForgeConfigSpec.BooleanValue
        var ENABLE_LECTERN: ForgeConfigSpec.BooleanValue
        var ENABLE_JUKEBOX: ForgeConfigSpec.BooleanValue
        var ENABLE_POWERED_RAIL: ForgeConfigSpec.BooleanValue

        init {
            builder.push("plugins")
            builder.push("generic")
            ENABLE_GENERIC_INVENTORY = builder.comment("Enables generic integration for inventory types of block entities")
                .define("enableGenericInventory", true)
            ENABLE_GENERIC_ITEM_STORAGE = builder.comment("Enables generic integration for item storages, that are not covered by inventory integration")
                .define("enableGenericItemStorage", true)
            ENABLE_GENERIC_FLUID_STORAGE = builder.comment("Enables generic integration for fluid storages")
                .define("enableGenericFluidStorage", true)
            builder.pop()
            builder.push("specific")
            ENABLE_BEACON = builder.comment("Enables integration for minecraft beacon")
                .define("enableBeacon", true)
            ENABLE_NOTEBLOCK = builder.comment("Enables integration for minecraft note block")
                .define("enableNoteBlock", true)
            ENABLE_LECTERN = builder.comment("Enables integration for minecraft lectern")
                .define("enableLectern", true)
            ENABLE_JUKEBOX = builder.comment("Enables integration for minecraft jukebox")
                .define("enableJukebox", true)
            ENABLE_POWERED_RAIL = builder.comment("Enables integration for minecraft powered rail")
                .define("enablePoweredRail", true)
            builder.pop().pop()
            builder.push("integrations")
            INTEGRATION_CONFIGURATIONS.entries.forEach {
                builder.push(it.key)
                it.value.addToConfig(builder)`
                builder.pop()
            }
            builder.pop()
        }
    }
}