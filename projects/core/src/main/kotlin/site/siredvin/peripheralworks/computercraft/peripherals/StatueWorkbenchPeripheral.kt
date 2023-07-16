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
    fun setStatueName(name: String?): MethodResult {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) return MethodResult.of(null, "Cannot find statue on top of workbench")
        opStatue.ifPresent { statue -> statue.name = name }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun getStatueName(): MethodResult {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) return MethodResult.of(null, "Cannot find statue on top of workbench")
        val tileEntity: FlexibleStatueBlockEntity = opStatue.get()
        return MethodResult.of(tileEntity.name)
    }

    @LuaFunction(mainThread = true)
    fun setAuthor(author: String?): MethodResult {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) return MethodResult.of(null, "Cannot find statue on top of workbench")
        opStatue.ifPresent { statue -> statue.author = author }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun getAuthor(): MethodResult {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) return MethodResult.of(null, "Cannot find statue on top of workbench")
        val tileEntity: FlexibleStatueBlockEntity = opStatue.get()
        return MethodResult.of(tileEntity.author)
    }

    @LuaFunction(mainThread = true)
    fun setLightLevel(lightLevel: Int): MethodResult {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) return MethodResult.of(null, "Cannot find statue on top of workbench")
        opStatue.ifPresent { statue -> statue.lightLevel = lightLevel }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun getLightLevel(): MethodResult {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) return MethodResult.of(null, "Cannot find statue on top of workbench")
        val tileEntity: FlexibleStatueBlockEntity = opStatue.get()
        return MethodResult.of(tileEntity.lightLevel)
    }

    @LuaFunction(mainThread = true)
    fun getCubes(): MethodResult {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) return MethodResult.of(null, "Cannot find statue on top of workbench")
        val tileEntity: FlexibleStatueBlockEntity = opStatue.get()
        val quadList: QuadList = tileEntity.bakedQuads
            ?: return MethodResult.of(emptyList<Any>())
        return MethodResult.of(quadList.toLua())
    }

    @LuaFunction(mainThread = true)
    @Throws(LuaException::class)
    fun setCubes(cubes: Map<*, *>): MethodResult {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) return MethodResult.of(null, "Cannot find statue on top of workbench")
        val quadList: QuadList = convertToQuadList(cubes)
        if (quadList.list.size > PeripheralWorksConfig.flexibleStatueMaxQuads) {
            return MethodResult.of(
                null,
                java.lang.String.format("You cannot send more then %d quads", PeripheralWorksConfig.flexibleStatueMaxQuads),
            )
        }
        val tileEntity = opStatue.get()
        tileEntity.setBakedQuads(quadList, false)
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun resetCubes(): MethodResult {
        val opStatue: Optional<FlexibleStatueBlockEntity> = getStatue()
        if (!opStatue.isPresent) return MethodResult.of(null, "Cannot find statue on top of workbench")
        val tileEntity = opStatue.get()
        tileEntity.clear(skipUpdate = false)
        return MethodResult.of(true)
    }
}
