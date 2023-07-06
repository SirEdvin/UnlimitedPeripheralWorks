package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.blockentity.InformativeRegistryBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import java.util.function.Supplier

class InformativeRegistryPeripheral(
    blockEntity: InformativeRegistryBlockEntity,
) : OwnedPeripheral<BlockEntityPeripheralOwner<InformativeRegistryBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {

    companion object {
        val TYPE = "informative_registry"
        private val EXTRACTORS = mutableMapOf<String, Supplier<MethodResult>>()

        fun addExtractor(name: String, extractor: Supplier<MethodResult>) {
            EXTRACTORS[name] = extractor
        }

        init {
            addExtractor("item") {
                MethodResult.of(XplatRegistries.ITEMS.keySet().map(ResourceLocation::toString))
            }
            addExtractor("block") {
                MethodResult.of(XplatRegistries.BLOCKS.keySet().map(ResourceLocation::toString))
            }
            addExtractor("fluid") {
                MethodResult.of(XplatRegistries.FLUIDS.keySet().map(ResourceLocation::toString))
            }
            addExtractor("list") {
                MethodResult.of(EXTRACTORS.keys)
            }
        }
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableInformativeRegistry

    @LuaFunction
    fun list(target: String): MethodResult {
        val extractor = EXTRACTORS[target] ?: throw LuaException("Cannot list $target, there is not function for it")
        return extractor.get()
    }
}
