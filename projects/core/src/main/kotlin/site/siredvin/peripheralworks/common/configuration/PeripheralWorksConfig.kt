package site.siredvin.peripheralworks.common.configuration

import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.api.IForgeConfigHandler
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations
import site.siredvin.peripheralworks.computercraft.operations.UnconditionalFreeOperations

object PeripheralWorksConfig {

    private val INTEGRATION_CONFIGURATIONS: MutableMap<String, IForgeConfigHandler> = mutableMapOf()

    val cooldownTresholdLevel: Int
        get() = ConfigHolder.commonConfig.cooldownThresholdLevel.get()

    val enableGenericInventory: Boolean
        get() = ConfigHolder.commonConfig.enableGenericInventory.get()
    val enableGenericItemStorage: Boolean
        get() = ConfigHolder.commonConfig.enableGenericItemStorage.get()
    val itemStorageTransferLimit: Int
        get() = ConfigHolder.commonConfig.itemStorageTransferLimit.get()
    val enableGenericFluidStorage: Boolean
        get() = ConfigHolder.commonConfig.enableGenericFluidStorage.get()
    val fluidStorageTransferLimit: Int
        get() = ConfigHolder.commonConfig.fluidStorageTransferLimit.get()
    val enableGenericEnergyStorage: Boolean
        get() = ConfigHolder.commonConfig.enableGenericFluidStorage.get()
    val energyStorageTransferLimit: Int
        get() = ConfigHolder.commonConfig.fluidStorageTransferLimit.get()
    val energyAlwaysTransferable: Boolean
        get() = ConfigHolder.commonConfig.energyAlwaysTransferable.get()

    val enableBeacon: Boolean
        get() = ConfigHolder.commonConfig.enableBeacon.get()
    val enableNoteBlock: Boolean
        get() = ConfigHolder.commonConfig.enableNotebook.get()
    val enableLectern: Boolean
        get() = ConfigHolder.commonConfig.enableLentern.get()
    val enableJukebox: Boolean
        get() = ConfigHolder.commonConfig.enableJukebox.get()
    val enablePoweredRail: Boolean
        get() = ConfigHolder.commonConfig.enablePoweredRail.get()

    val enablePeripheraliumHubs: Boolean
        get() = ConfigHolder.commonConfig.enablePeripheraliumHubs.get()

    val peripheraliumHubUpgradeCount: Int
        get() = ConfigHolder.commonConfig.peripheraliumHubUpgradeCount.get()

    val netheritePeripheraliumHubUpgradeCount: Int
        get() = ConfigHolder.commonConfig.netheritePeripheraliumHubUpgradeCount.get()

    val enableUniversalScanner: Boolean
        get() = ConfigHolder.commonConfig.enableUniversalScanner.get()

    val enableUltimateSensor: Boolean
        get() = ConfigHolder.commonConfig.enableUniversalScanner.get()

    val enableItemPedestal: Boolean
        get() = ConfigHolder.commonConfig.enableItemPedestal.get()

    val enableMapPedestal: Boolean
        get() = ConfigHolder.commonConfig.enableMapPedestal.get()

    val enableDisplayPedestal: Boolean
        get() = ConfigHolder.commonConfig.enableMapPedestal.get()

    val enableRemoteObserver: Boolean
        get() = ConfigHolder.commonConfig.enableRemoteObserver.get()

    val remoteObserverMaxRange: Int
        get() = ConfigHolder.commonConfig.removeObserverMaxRange.get()

    val remoteObserverMaxCapacity: Int
        get() = ConfigHolder.commonConfig.removeObserverMaxCapacity.get()

    val enablePeripheralProxy: Boolean
        get() = ConfigHolder.commonConfig.enablePeripheralProxy.get()

    val peripheralProxyMaxRange: Int
        get() = ConfigHolder.commonConfig.peripheralProxyMaxRange.get()

    val peripheralProxyMaxCapacity: Int
        get() = ConfigHolder.commonConfig.peripheralProxyMaxCapacity.get()

    val enableRealityForger: Boolean
        get() = ConfigHolder.commonConfig.enableRealityForger.get()

    val realityForgerMaxRange: Int
        get() = ConfigHolder.commonConfig.realityForgerMaxRange.get()

    val enableRecipeRegistry: Boolean
        get() = ConfigHolder.commonConfig.enableRecipeRegistry.get()

    val enableInformativeRegistry: Boolean
        get() = ConfigHolder.commonConfig.enableInformativeRegistry.get()

    val enableStatueWorkbench: Boolean
        get() = ConfigHolder.commonConfig.enableStatueWorkbench.get()

    val flexibleStatueMaxQuads: Int
        get() = ConfigHolder.commonConfig.flexibleStatueMaxQuads.get()

    val enableEntityLink: Boolean
        get() = ConfigHolder.commonConfig.enableEntityLinks.get()

    fun registerIntegrationConfiguration(configuration: IForgeConfigHandler) {
        INTEGRATION_CONFIGURATIONS[configuration.name] = configuration
    }

    class CommonConfig internal constructor(builder: ForgeConfigSpec.Builder) {

        // Generic configuration
        var cooldownThresholdLevel: ForgeConfigSpec.IntValue

        // Generic plugins
        var enableGenericInventory: ForgeConfigSpec.BooleanValue
        var enableGenericItemStorage: ForgeConfigSpec.BooleanValue
        var enableGenericFluidStorage: ForgeConfigSpec.BooleanValue
        var enableGenericEnergyStorage: ForgeConfigSpec.BooleanValue
        val itemStorageTransferLimit: ForgeConfigSpec.IntValue
        val fluidStorageTransferLimit: ForgeConfigSpec.IntValue
        val energyStorageTransferLimit: ForgeConfigSpec.IntValue
        val energyAlwaysTransferable: ForgeConfigSpec.BooleanValue

        // Specific plugins
        var enableBeacon: ForgeConfigSpec.BooleanValue
        var enableNotebook: ForgeConfigSpec.BooleanValue
        var enableLentern: ForgeConfigSpec.BooleanValue
        var enableJukebox: ForgeConfigSpec.BooleanValue
        var enablePoweredRail: ForgeConfigSpec.BooleanValue

        // Peripheralium hubs
        val enablePeripheraliumHubs: ForgeConfigSpec.BooleanValue
        val peripheraliumHubUpgradeCount: ForgeConfigSpec.IntValue
        val netheritePeripheraliumHubUpgradeCount: ForgeConfigSpec.IntValue
        val enableUniversalScanner: ForgeConfigSpec.BooleanValue
        val enableUltimateSensor: ForgeConfigSpec.BooleanValue
        val enableItemPedestal: ForgeConfigSpec.BooleanValue
        val enableMapPedestal: ForgeConfigSpec.BooleanValue
        val enableDisplayPedestal: ForgeConfigSpec.BooleanValue
        val enableRemoteObserver: ForgeConfigSpec.BooleanValue
        val removeObserverMaxRange: ForgeConfigSpec.IntValue
        val removeObserverMaxCapacity: ForgeConfigSpec.IntValue
        val enablePeripheralProxy: ForgeConfigSpec.BooleanValue
        val peripheralProxyMaxRange: ForgeConfigSpec.IntValue
        val peripheralProxyMaxCapacity: ForgeConfigSpec.IntValue
        val enableRealityForger: ForgeConfigSpec.BooleanValue
        val realityForgerMaxRange: ForgeConfigSpec.IntValue
        val enableRecipeRegistry: ForgeConfigSpec.BooleanValue
        val enableInformativeRegistry: ForgeConfigSpec.BooleanValue
        val enableStatueWorkbench: ForgeConfigSpec.BooleanValue
        val flexibleStatueMaxQuads: ForgeConfigSpec.IntValue
        val enableEntityLinks: ForgeConfigSpec.BooleanValue

        init {
            builder.push("base")
            cooldownThresholdLevel = builder.comment("Determinates treshold for cooldown to be stored")
                .defineInRange("cooldownTreshholdLevel", 100, 0, Int.MAX_VALUE)
            builder.pop()
            builder.push("plugins")
            builder.push("generic")
            enableGenericInventory = builder.comment("Enables generic integration for inventory types of block entities")
                .define("enableGenericInventory", true)
            enableGenericItemStorage = builder.comment("Enables generic integration for item storages, that are not covered by inventory integration")
                .define("enableGenericItemStorage", true)
            enableGenericFluidStorage = builder.comment("Enables generic integration for fluid storages")
                .define("enableGenericFluidStorage", true)
            enableGenericEnergyStorage = builder.comment("Enables generic integration for energy storages")
                .define("enableGenericEnergyStorage", true)
            itemStorageTransferLimit = builder.comment("Limits max item transfer per one operation")
                .defineInRange("itemStorageTransferLimit", 128, 1, Int.MAX_VALUE)
            fluidStorageTransferLimit = builder.comment("Limits max fluid transfer per one operation")
                .defineInRange("fluidStorageTransferLimit", 65500 * PlatformToolkit.get().fluidCompactDivider.toInt(), 1, Int.MAX_VALUE)
            energyStorageTransferLimit = builder.comment("Limits max fluid transfer per one operation")
                .defineInRange("fluidStorageTransferLimit", 262144, 1, Int.MAX_VALUE)
            energyAlwaysTransferable = builder.comment("Make any energy transferable, even if it is not usually allowed by CC:Tweaked itself")
                .define("energyAlwaysTransferable", false)
            builder.pop()
            builder.push("specific")
            enableBeacon = builder.comment("Enables integration for minecraft beacon")
                .define("enableBeacon", true)
            enableNotebook = builder.comment("Enables integration for minecraft note block")
                .define("enableNoteBlock", true)
            enableLentern = builder.comment("Enables integration for minecraft lectern")
                .define("enableLectern", true)
            enableJukebox = builder.comment("Enables integration for minecraft jukebox")
                .define("enableJukebox", true)
            enablePoweredRail = builder.comment("Enables integration for minecraft powered rail")
                .define("enablePoweredRail", true)
            builder.pop()
            builder.push("Peripherals")
            enablePeripheraliumHubs = builder.comment("Enables peripheralium hubs (regular one and netherite versions), which allows you to use many peripherals in one")
                .define("enablePeripheraliumHubs", true)
            peripheraliumHubUpgradeCount = builder.comment("Regulare amount of upgrades that can be installed on peripheralium hub")
                .defineInRange("peripheraliumHubUpgradeCount", 3, 1, 64)
            netheritePeripheraliumHubUpgradeCount = builder.comment("Regulare amount of upgrades that can be installed on netherite peripheralium hub")
                .defineInRange("netheritePeripheraliumHubUpgradeCount", 7, 1, 64)
            enableUniversalScanner = builder.comment("Enables universal scanner")
                .define("enableUniversalScanner", true)
            enableUltimateSensor = builder.comment("Enables ultimate sensor")
                .define("enableUltimateSensor", true)
            enableItemPedestal = builder.comment("Enables item pedestal nbt reading")
                .define("enableItemPedestal", true)
            enableMapPedestal = builder.comment("Enables map pedestal, for detail map information reading")
                .define("enableMapPedestal", true)
            enableDisplayPedestal = builder.comment("Enables display pedestal")
                .define("enableDisplayPedestal", true)
            enableRemoteObserver = builder.comment("Enables remote observer")
                .define("enableRemoteObserver", true)
            removeObserverMaxRange = builder.comment("Max range for remote observer")
                .defineInRange("remoteObserverMaxRange", 16, 1, 128)
            removeObserverMaxCapacity = builder.comment("Max capacity for remote observer")
                .defineInRange("remoteObserverMaxCapacity", 8, 1, 128)
            enablePeripheralProxy = builder.comment("Enables remote observer")
                .define("enableRemoteObserver", true)
            peripheralProxyMaxRange = builder.comment("Max range for peripheral proxy")
                .defineInRange("peripheralProxyMaxRange", 16, 1, 128)
            peripheralProxyMaxCapacity = builder.comment("Max capacity for peripheral proxy")
                .defineInRange("peripheralProxyMaxCapacity", 8, 1, 128)
            enableRealityForger = builder.comment("Enable reality forger")
                .define("enableRealityForger", true)
            realityForgerMaxRange = builder.comment("Reality forger max range for forging")
                .defineInRange("realityForgerMaxRange", 24, 1, 256)
            enableRecipeRegistry = builder.comment("Enables recipe registry")
                .define("enableRecipeRegistry", true)
            enableInformativeRegistry = builder.comment("Enables informative registry")
                .define("enableInformativeRegistry", true)
            enableStatueWorkbench = builder.comment("Enables statue workbench")
                .define("enableStatueWorkbench", true)
            flexibleStatueMaxQuads = builder.comment("Max quads amount for flexible statue, will be applied only for newest ones")
                .defineInRange("flexibleStatueMaxQuads", 256, 64, Int.MAX_VALUE)
            enableEntityLinks = builder.comment("Enables entity link")
                .define("enableEntityLink", true)
            builder.pop().pop()
            builder.push("operations")
            register(SphereOperations.entries.toTypedArray(), builder)
            register(UnconditionalFreeOperations.entries.toTypedArray(), builder)
            builder.pop()
            builder.push("integrations")
            INTEGRATION_CONFIGURATIONS.entries.forEach {
                builder.push(it.key)
                it.value.addToConfig(builder)
                builder.pop()
            }
            builder.pop()
        }

        private fun register(data: Array<out IForgeConfigHandler>, builder: ForgeConfigSpec.Builder) {
            for (handler in data) {
                handler.addToConfig(builder)
            }
        }
    }
}
