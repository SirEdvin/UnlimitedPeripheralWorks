package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.blockentity.InformativeRegistryBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import java.util.function.Function
import java.util.function.Supplier

class InformativeRegistryPeripheral(
    blockEntity: InformativeRegistryBlockEntity,
) : OwnedPeripheral<BlockEntityPeripheralOwner<InformativeRegistryBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {

    companion object {
        val TYPE = "informative_registry"
        private val EXTRACTORS = mutableMapOf<String, Supplier<MethodResult>>()
        private val DESCRIPTORS = mutableMapOf<String, Function<String, MethodResult>>()
        private val LIST_DESCRIPTIONS = mutableMapOf<String, String>()

        fun addList(name: String, description: String, extractor: Supplier<MethodResult>, descriptor: Function<String, MethodResult>) {
            LIST_DESCRIPTIONS[name] = description
            DESCRIPTORS[name] = descriptor
            EXTRACTORS[name] = extractor
        }

        init {
            addList(
                "item",
                "Minecraft items",
                {
                    MethodResult.of(XplatRegistries.ITEMS.keySet().map(ResourceLocation::toString))
                },
                {
                    MethodResult.of(LuaRepresentation.forItem(XplatRegistries.ITEMS.get(ResourceLocation(it))))
                },
            )

            addList(
                "block",
                "Minecraft blocks",
                {
                    MethodResult.of(XplatRegistries.BLOCKS.keySet().map(ResourceLocation::toString))
                },
                {
                    MethodResult.of(LuaRepresentation.forBlockState(XplatRegistries.BLOCKS.get(ResourceLocation(it)).defaultBlockState()))
                },
            )

            addList(
                "fluid",
                "Minecraft fluids",
                {
                    MethodResult.of(XplatRegistries.FLUIDS.keySet().map(ResourceLocation::toString))
                },
                {
                    MethodResult.of(LuaRepresentation.forFluid(XplatRegistries.FLUIDS.get(ResourceLocation(it))))
                },
            )
            addList(
                "list",
                "Lists of all possible lists",
                {
                    MethodResult.of(LIST_DESCRIPTIONS.keys)
                },
                {
                    MethodResult.of(LIST_DESCRIPTIONS[it])
                },
            )
        }
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableInformativeRegistry

    @LuaFunction
    fun list(target: String): MethodResult {
        val extractor = EXTRACTORS[target] ?: throw LuaException("Cannot list $target, there is not function for it")
        return extractor.get()
    }

    @LuaFunction
    fun describe(target: String, id: String): MethodResult {
        val descriptor = DESCRIPTORS[target] ?: throw LuaException("Cannot describe $target, there is not function for it")
        return descriptor.apply(id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InformativeRegistryPeripheral) return false
        if (!super.equals(other)) return false

        if (isEnabled != other.isEnabled) return false
        if (peripheralOwner != other.peripheralOwner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isEnabled.hashCode()
        result = 31 * result + peripheralOwner.hashCode()
        return result
    }
}
