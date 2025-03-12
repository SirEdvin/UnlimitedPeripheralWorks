package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.nbt.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.peripheralworks.common.block.BasePedestal
import site.siredvin.peripheralworks.common.blockentity.DisplayPedestalBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.RepresentationMode
import java.util.*

class DisplayPedestalPeripheral(private val blockEntity: DisplayPedestalBlockEntity) :
    OwnedPeripheral<BlockEntityPeripheralOwner<DisplayPedestalBlockEntity>>(
        TYPE,
        BlockEntityPeripheralOwner(blockEntity, facingProperty = BasePedestal.FACING),
    ) {
    companion object {
        const val TYPE = "display_pedestal"
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableDisplayPedestal

    @LuaFunction(mainThread = true)
    fun getItem(): Map<String, Any>? {
        if (blockEntity.storedStack.isEmpty) {
            return null
        }
        return LuaRepresentation.forItemStack(blockEntity.storedStack, RepresentationMode.FULL)
    }

    @LuaFunction(mainThread = true)
    fun setItem(id: String, name: Optional<String>, nbtData: Optional<String>): MethodResult {
        val item = PlatformRegistries.ITEMS.get(ResourceLocation(id))
        if (item == Items.AIR) {
            return MethodResult.of(null, "Cannot find item with id $id")
        }
        val stack = ItemStack(item)
        if (nbtData.isPresent) {
            stack.tag = TagParser.parseTag(nbtData.get())
        }
        if (name.isPresent) {
            stack.setHoverName(Component.literal(name.get()))
        }
        blockEntity.storedStack = stack
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun isLabelRendered(): Boolean = blockEntity.renderLabel

    @LuaFunction(mainThread = true)
    fun isItemRendered(): Boolean = blockEntity.renderItem

    @LuaFunction(mainThread = true)
    fun setLabelRendered(value: Boolean) {
        blockEntity.renderLabel = value
    }

    @LuaFunction(mainThread = true)
    fun setItemRendered(value: Boolean) {
        blockEntity.renderItem = value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DisplayPedestalPeripheral) return false
        if (!super.equals(other)) return false

        if (blockEntity != other.blockEntity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + blockEntity.hashCode()
        return result
    }
}
