package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.tweakium.modules.peripheral.api.IOwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.MutablePeripheralBlockEntity

abstract class RegistryAwarePeripheralBlockEntity<T : IOwnedPeripheral<*>>(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : MutablePeripheralBlockEntity<T>(type, pos, state) {
    private var serializationRegistries: HolderLookup.Provider? = null

    protected val itemRegistries: HolderLookup.Provider
        get() = serializationRegistries ?: checkNotNull(level) { "Item serialization requires a registry provider or an attached level" }.registryAccess()

    // Tweakium's internal-data callbacks omit the native provider. Preserve it even before setLevel,
    // without retaining a client's registry across connections or relying on an integrated server.
    private fun <R> withRegistries(provider: HolderLookup.Provider, action: () -> R): R {
        val previous = serializationRegistries
        serializationRegistries = provider
        try {
            return action()
        } finally {
            serializationRegistries = previous
        }
    }

    override fun loadAdditional(compound: CompoundTag, provider: HolderLookup.Provider) = withRegistries(provider) {
        super.loadAdditional(compound, provider)
    }

    override fun saveAdditional(compound: CompoundTag, provider: HolderLookup.Provider) = withRegistries(provider) {
        super.saveAdditional(compound, provider)
    }

    override fun getUpdateTag(provider: HolderLookup.Provider): CompoundTag = withRegistries(provider) {
        super.getUpdateTag(provider)
    }
}
