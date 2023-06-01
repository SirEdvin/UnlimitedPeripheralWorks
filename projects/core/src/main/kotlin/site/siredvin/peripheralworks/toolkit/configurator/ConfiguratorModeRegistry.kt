package site.siredvin.peripheralworks.toolkit.configurator

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.setup.Blocks
import java.util.function.Predicate

object ConfiguratorModeRegistry {
    private val REGISTRY = mutableMapOf<ResourceLocation, ConfigurationMode>()
    private val CONDITION_REGISTRY = mutableMapOf<Predicate<BlockState>, ResourceLocation>()

    init {
        register(RemoteObserverMode.modeID, RemoteObserverMode) {it.`is`(Blocks.REMOTE_OBSERVER.get())}
        register(PeripheralProxyMode.modeID, PeripheralProxyMode) {it.`is`(Blocks.PERIPHERAL_PROXY.get())}
    }

    fun register(modeID: ResourceLocation, builder: ConfigurationMode, condition: Predicate<BlockState>) {
        REGISTRY[modeID] = builder
        CONDITION_REGISTRY[condition] = modeID
    }

    fun get(modeID: ResourceLocation): ConfigurationMode? {
        return REGISTRY[modeID]
    }

    fun get(state: BlockState): ConfigurationMode? {
        CONDITION_REGISTRY.forEach { (predicate, modID) ->
            if (predicate.test(state))
                return get(modID)
        }
        return null
    }
}