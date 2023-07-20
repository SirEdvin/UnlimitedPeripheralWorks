package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralworks.common.blockentity.FlexibleStatueBlockEntity
import site.siredvin.peripheralworks.common.blockentity.StatueWorkbenchBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.utils.QuadList
import site.siredvin.peripheralworks.utils.convertToQuadList
import java.util.*

class StatueWorkbenchPeripheral(
    blockEntity: StatueWorkbenchBlockEntity,
) : OwnedPeripheral<BlockEntityPeripheralOwner<StatueWorkbenchBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {
    companion object {
        const val TYPE = "statue_workbench"
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableStatueWorkbench

    protected fun getStatue(): Optional<FlexibleStatueBlockEntity> {
        val blockEntity = level!!.getBlockEntity(pos.offset(0, 1, 0))
        return if (blockEntity !is FlexibleStatueBlockEntity) Optional.empty() else Optional.of(blockEntity)
    }

    @LuaFunction(mainThread = true)
    fun isPresent(): Boolean {
        return getStatue().isPresent
    }

    @LuaFunction(mainThread = true)
    fun setStatueName(name: String) {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) throw LuaException("There is no statue on top of workbench")
        opStatue.ifPresent { statue -> statue.name = name }
    }

    @LuaFunction(mainThread = true)
    fun getStatueName(): String {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) throw LuaException("There is no statue on top of workbench")
        val tileEntity: FlexibleStatueBlockEntity = opStatue.get()
        return tileEntity.name ?: ""
    }

    @LuaFunction(mainThread = true)
    fun setAuthor(author: String) {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) throw LuaException("There is no statue on top of workbench")
        opStatue.ifPresent { statue -> statue.author = author }
    }

    @LuaFunction(mainThread = true)
    fun getAuthor(): String {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) throw LuaException("There is no statue on top of workbench")
        val tileEntity: FlexibleStatueBlockEntity = opStatue.get()
        return tileEntity.author ?: ""
    }

    @LuaFunction(mainThread = true)
    fun setLightLevel(lightLevel: Int) {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) throw LuaException("There is no statue on top of workbench")
        opStatue.ifPresent { statue -> statue.lightLevel = lightLevel }
    }

    @LuaFunction(mainThread = true)
    fun getLightLevel(): Int {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) throw LuaException("There is no statue on top of workbench")
        val tileEntity: FlexibleStatueBlockEntity = opStatue.get()
        return tileEntity.lightLevel
    }

    @LuaFunction(mainThread = true)
    fun getCubes(): List<Map<*, *>> {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) throw LuaException("There is no statue on top of workbench")
        val tileEntity: FlexibleStatueBlockEntity = opStatue.get()
        val quadList: QuadList = tileEntity.bakedQuads
            ?: return emptyList()
        return quadList.toLua()
    }

    @LuaFunction(mainThread = true)
    @Throws(LuaException::class)
    fun setCubes(cubes: Map<*, *>) {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) throw LuaException("There is no statue on top of workbench")
        val quadList: QuadList = convertToQuadList(cubes)
        if (quadList.list.size > PeripheralWorksConfig.flexibleStatueMaxQuads) {
            throw LuaException("You cannot send more then ${PeripheralWorksConfig.flexibleStatueMaxQuads} quads")
        }
        val tileEntity = opStatue.get()
        tileEntity.setBakedQuads(quadList, false)
    }

    @LuaFunction(mainThread = true)
    fun reset() {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) throw LuaException("There is no statue on top of workbench")
        val tileEntity = opStatue.get()
        tileEntity.name = null
        tileEntity.author = null
        tileEntity.clear(skipUpdate = false)
    }
}
