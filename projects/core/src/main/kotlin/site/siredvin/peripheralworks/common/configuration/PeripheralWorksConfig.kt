package site.siredvin.peripheralworks.common.configuration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralium.api.config.IConfigHandler
import site.siredvin.peripheralium.api.config.IOperationAbilityConfig
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.computercraft.operations.InspectOperations
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations

object PeripheralWorksConfig: IOperationAbilityConfig {

    private val INTEGRATION_CONFIGURATIONS: MutableMap<String, IConfigHandler> = mutableMapOf()

    override val isInitialCooldownEnabled: Boolean
        get() = ConfigHolder.COMMON_CONFIG.IS_INITIAL_COOLDOWN_ENABLED.get()
    override val initialCooldownSensetiveLevel: Int
        get() = ConfigHolder.COMMON_CONFIG.INITIAL_COOLDOWN_SENSENTIVE_LEVEL.get()
    override val cooldownTrasholdLevel: Int
        get() = ConfigHolder.COMMON_CONFIG.COOLDOWN_TRASHOLD_LEVEL.get()

    val enableGenericInventory: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_GENERIC_INVENTORY.get()
    val enableGenericItemStorage: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_GENERIC_ITEM_STORAGE.get()
    val itemStorageTransferLimit: Int
        get() = ConfigHolder.COMMON_CONFIG.ITEM_STORAGE_TRANSFER_LIMIT.get()
    val enableGenericFluidStorage: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_GENERIC_FLUID_STORAGE.get()
    val fluidStorageTransferLimit: Int
        get() = ConfigHolder.COMMON_CONFIG.FLUID_STORAGE_TRANSFER_LIMIT.get()

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

    val enablePeripheraliumHubs: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_PERIPHERALIUM_HUBS.get()

    val peripheraliumHubUpgradeCount: Int
        get() = ConfigHolder.COMMON_CONFIG.PERIPHERALIUM_HUB_UPGRADE_COUNT.get()

    val netheritePeripheraliumHubUpgradeCount: Int
        get() = ConfigHolder.COMMON_CONFIG.NETHERITE_PERIPHERALIUM_HUB_UPGRADE_COUNT.get()

    val enableUniversalScanner: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_UNIVERSAL_SCANNER.get()

    val enableUltimateSensor: Boolean
        get() = ConfigHolder.COMMON_CONFIG.ENABLE_UNIVERSAL_SCANNER.get()

    fun registerIntegrationConfiguration(configuration: IConfigHandler) {
        INTEGRATION_CONFIGURATIONS[configuration.name] = configuration
    }

    class CommonConfig internal constructor(builder: ForgeConfigSpec.Builder) {

        // Generic configuration
        var IS_INITIAL_COOLDOWN_ENABLED: ForgeConfigSpec.BooleanValue
        var INITIAL_COOLDOWN_SENSENTIVE_LEVEL: ForgeConfigSpec.IntValue
        var COOLDOWN_TRASHOLD_LEVEL: ForgeConfigSpec.IntValue
        // Generic plugins
        var ENABLE_GENERIC_INVENTORY: ForgeConfigSpec.BooleanValue
        var ENABLE_GENERIC_ITEM_STORAGE: ForgeConfigSpec.BooleanValue
        var ENABLE_GENERIC_FLUID_STORAGE: ForgeConfigSpec.BooleanValue
        val ITEM_STORAGE_TRANSFER_LIMIT: ForgeConfigSpec.IntValue
        val FLUID_STORAGE_TRANSFER_LIMIT: ForgeConfigSpec.IntValue
        // Specific plugins
        var ENABLE_BEACON: ForgeConfigSpec.BooleanValue
        var ENABLE_NOTEBLOCK: ForgeConfigSpec.BooleanValue
        var ENABLE_LECTERN: ForgeConfigSpec.BooleanValue
        var ENABLE_JUKEBOX: ForgeConfigSpec.BooleanValue
        var ENABLE_POWERED_RAIL: ForgeConfigSpec.BooleanValue
        // Peripheralium hubs
        val ENABLE_PERIPHERALIUM_HUBS: ForgeConfigSpec.BooleanValue
        val PERIPHERALIUM_HUB_UPGRADE_COUNT: ForgeConfigSpec.IntValue
        val NETHERITE_PERIPHERALIUM_HUB_UPGRADE_COUNT: ForgeConfigSpec.IntValue
        val ENABLE_UNIVERSAL_SCANNER: ForgeConfigSpec.BooleanValue
        val ENABLE_ULTIMATE_SENSOR: ForgeConfigSpec.BooleanValue

        init {
            builder.push("base")
            IS_INITIAL_COOLDOWN_ENABLED = builder.comment("Enables initial cooldown on peripheral initialization")
                .define("isInitialCooldownEnabled", true)
            INITIAL_COOLDOWN_SENSENTIVE_LEVEL = builder.comment("Determinates initial cooldown sensentive level, values lower then this value will not trigger initial cooldown")
                .defineInRange("initialCooldownSensetiveLevel", 6000, 0, Int.MAX_VALUE)
            COOLDOWN_TRASHOLD_LEVEL = builder.comment("Determinates trashold for cooldown to be stored")
                .defineInRange("cooldownTrashholdLevel", 100, 0, Int.MAX_VALUE)
            builder.pop()
            builder.push("plugins")
            builder.push("generic")
            ENABLE_GENERIC_INVENTORY = builder.comment("Enables generic integration for inventory types of block entities")
                .define("enableGenericInventory", true)
            ENABLE_GENERIC_ITEM_STORAGE = builder.comment("Enables generic integration for item storages, that are not covered by inventory integration")
                .define("enableGenericItemStorage", true)
            ENABLE_GENERIC_FLUID_STORAGE = builder.comment("Enables generic integration for fluid storages")
                .define("enableGenericFluidStorage", true)
            ITEM_STORAGE_TRANSFER_LIMIT = builder.comment("Limits max item transfer per one operation")
                .defineInRange("itemStorageTransferLimit", 128, 1, Int.MAX_VALUE)
            FLUID_STORAGE_TRANSFER_LIMIT = builder.comment("Limits max fluid transfer per one operation")
                .defineInRange("fluidStorageTransferLimit", 65500 * PeripheraliumPlatform.fluidCompactDivider.toInt(), 1, Int.MAX_VALUE)
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
            builder.pop()
            builder.push("Peripherals")
            ENABLE_PERIPHERALIUM_HUBS = builder.comment("Enables peripheralium hubs (regular one and netherite versions), which allows you to use many peripherals in one")
                .define("enablePeripheraliumHubs", true)
            PERIPHERALIUM_HUB_UPGRADE_COUNT = builder.comment("Regulare amount of upgrades that can be installed on peripheralium hub")
                .defineInRange("peripheraliumHubUpgradeCount", 3, 1, 64)
            NETHERITE_PERIPHERALIUM_HUB_UPGRADE_COUNT  = builder.comment("Regulare amount of upgrades that can be installed on netherite peripheralium hub")
                .defineInRange("netheritePeripheraliumHubUpgradeCount", 7, 1, 64)
            ENABLE_UNIVERSAL_SCANNER = builder.comment("Enables universal scanner")
                .define("enableUniversalScanner", true)
            ENABLE_ULTIMATE_SENSOR = builder.comment("Enables ultimate sensor")
                .define("enableUltimateSensor", true)
            builder.pop().pop()
            builder.push("operations")
            register(SphereOperations.values(), builder)
            register(InspectOperations.values(), builder)
            builder.pop()
            builder.push("integrations")
            INTEGRATION_CONFIGURATIONS.entries.forEach {
                builder.push(it.key)
                it.value.addToConfig(builder)
                builder.pop()
            }
            builder.pop()
        }

        private fun register(data: Array<out IConfigHandler>, builder: ForgeConfigSpec.Builder) {
            for (handler in data) {
                handler.addToConfig(builder)
            }
        }
    }
}