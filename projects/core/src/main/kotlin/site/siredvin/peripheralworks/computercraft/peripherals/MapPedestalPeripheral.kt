package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.Direction
import net.minecraft.world.item.Items
import net.minecraft.world.item.MapItem
import net.minecraft.world.level.material.MaterialColor
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralworks.common.block.BasePedestal
import site.siredvin.peripheralworks.common.blockentity.MapPedestalBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.operations.UnconditionalFreeOperations
import site.siredvin.peripheralworks.computercraft.plugins.PedestalInventoryPlugin

class MapPedestalPeripheral(private val blockEntity: MapPedestalBlockEntity): OwnedPeripheral<BlockEntityPeripheralOwner<MapPedestalBlockEntity>>(
    TYPE, BlockEntityPeripheralOwner(blockEntity, facingProperty = BasePedestal.FACING)
)  {
    companion object {
        const val TYPE = "map_pedestal"
    }

    init {
        peripheralOwner.attachOperations(config = PeripheralWorksConfig)
        addPlugin(PedestalInventoryPlugin(blockEntity))
        addOperations(listOf(UnconditionalFreeOperations.UPDATE_MAP, UnconditionalFreeOperations.EXTRACT_MAP))
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableMapPedestal

    @LuaFunction(mainThread = true)
    fun getData(): MethodResult {
        return peripheralOwner.withOperation(UnconditionalFreeOperations.EXTRACT_MAP, null, {
            val savedData =
                MapItem.getSavedData(blockEntity.storedStack, peripheralOwner.level!!) ?: return@withOperation MethodResult.of(
                    null,
                    "Cannot get information from map"
                )
            val data = mutableMapOf<String, Any>()
            val facing = if (peripheralOwner.facing == Direction.UP || peripheralOwner.facing == Direction.DOWN) {
                Direction.EAST
            } else {
                peripheralOwner.facing
            }
            data["colors"] = savedData.colors.map { MaterialColor.getColorFromPackedId(it.toInt()) }
            data["scale"] = savedData.scale
            data["banners"] = savedData.banners.map {
                val mapData = mutableMapOf<String, Any>()
                if (it.name != null)
                    mapData["name"] = it.name!!.string
                mapData["pos"] = LuaRepresentation.forBlockPos(it.pos, facing, peripheralOwner.pos)
                mapData["color"] = it.color.getName()
                return@map mapData
            }
            return@withOperation MethodResult.of(data)
        })
    }

    @LuaFunction(mainThread = true)
    fun updateData(): MethodResult {
        return peripheralOwner.withOperation(UnconditionalFreeOperations.UPDATE_MAP, null, {
            val savedData = MapItem.getSavedData(blockEntity.storedStack, peripheralOwner.level!!) ?:
            return@withOperation MethodResult.of(null, "Cannot get information from map")
            peripheralOwner.owner?.let { (Items.FILLED_MAP as MapItem).update(peripheralOwner.level!!, it, savedData) }
            return@withOperation MethodResult.of(true)
        })
    }
}