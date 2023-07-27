package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.util.radiusCorrect
import site.siredvin.peripheralium.util.representation.LuaInterpretation
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralium.util.world.ScanUtils
import site.siredvin.peripheralworks.common.block.FlexibleRealityAnchor
import site.siredvin.peripheralworks.common.blockentity.FlexibleRealityAnchorBlockEntity
import site.siredvin.peripheralworks.common.blockentity.RealityForgerBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.tags.BlockTags
import java.util.*

class RealityForgerPeripheral(
    blockEntity: RealityForgerBlockEntity,
) : OwnedPeripheral<BlockEntityPeripheralOwner<RealityForgerBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {

    companion object {
        const val TYPE = "reality_forger"
        private val FLAG_MAPPING = mapOf(
            "playerPassable" to FlexibleRealityAnchor.PLAYER_PASSABLE,
            "lightPassable" to FlexibleRealityAnchor.LIGHT_PASSABLE,
            "skyLightPassable" to FlexibleRealityAnchor.SKY_LIGHT_PASSABLE,
            "invisible" to FlexibleRealityAnchor.INVISIBLE,
        )
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableRealityForger

    private val interactionRadius: Int
        get() = PeripheralWorksConfig.realityForgerMaxRange

    override val peripheralConfiguration: MutableMap<String, Any>
        get() {
            val data = super.peripheralConfiguration
            data["interactionRadius"] = interactionRadius
            return data
        }

    private fun parseMap(realityMirror: FlexibleRealityAnchorBlockEntity, table: Map<*, *>) {
        table.forEach {
            if (it.value is Boolean) {
                val targetProperty = FLAG_MAPPING[it.key.toString()]
                if (targetProperty != null) {
                    realityMirror.setBooleanStateValue(targetProperty, it.value as Boolean)
                }
            } else if (it.key == "lightLevel" && it.value is Number) {
                realityMirror.lightLevel = (it.value as Number).toInt()
            }
        }
    }

    private fun forgeRealityTileEntity(
        realityMirror: FlexibleRealityAnchorBlockEntity,
        targetState: BlockState?,
        appearanceTable: Map<*, *>,
        flagTable: Optional<Map<*, *>>,
    ) {
        parseMap(realityMirror, appearanceTable)
        flagTable.ifPresent { parseMap(realityMirror, it) }
        if (targetState != null) {
            realityMirror.setMimic(targetState)
        } else {
            realityMirror.pushInternalDataChangeToClient()
        }
    }

    @LuaFunction(mainThread = true)
    fun detectAnchors(): List<Map<String, Any>> {
        val data = mutableListOf<Map<String, Any>>()
        ScanUtils.traverseBlocks(level!!, pos, interactionRadius, { _, pos ->
            val blockEntity = level!!.getBlockEntity(pos)
            if (blockEntity is FlexibleRealityAnchorBlockEntity) {
                data.add(LuaRepresentation.forBlockPos(pos, this.peripheralOwner.facing, this.pos))
            }
        })
        return data
    }

    @LuaFunction(mainThread = true)
    fun clearAnchors(arguments: IArguments): MethodResult {
        val poses: MutableList<BlockPos> = mutableListOf()
        val posesTable = arguments.optTable(0)
        if (posesTable.isEmpty) {
            ScanUtils.traverseBlocks(level!!, pos, interactionRadius, { _, pos ->
                val blockEntity = level!!.getBlockEntity(pos)
                if (blockEntity is FlexibleRealityAnchorBlockEntity) {
                    poses.add(pos)
                }
            })
        } else {
            for (value in posesTable.get().values) {
                if (value !is Map<*, *>) throw LuaException("First argument should be list of block positions")
                poses.add(LuaInterpretation.asBlockPos(peripheralOwner.pos, value, peripheralOwner.facing))
            }
        }
        poses.forEach {
            (level?.getBlockEntity(it) as? FlexibleRealityAnchorBlockEntity)?.setMimic(null)
        }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    @Throws(LuaException::class)
    fun forgeRealityPieces(arguments: IArguments): MethodResult {
        val poses: MutableList<BlockPos> = mutableListOf()
        for (value in arguments.getTable(0).values) {
            if (value !is Map<*, *>) throw LuaException("First argument should be list of block positions")
            poses.add(LuaInterpretation.asBlockPos(peripheralOwner.pos, value, peripheralOwner.facing))
        }
        val entities: MutableList<FlexibleRealityAnchorBlockEntity> = ArrayList<FlexibleRealityAnchorBlockEntity>()
        for (pos in poses) {
            if (!radiusCorrect(pos, peripheralOwner.pos, interactionRadius)) {
                return MethodResult.of(null, "One of blocks are too far away")
            }
            val entity = level!!.getBlockEntity(pos) as? FlexibleRealityAnchorBlockEntity
                ?: return MethodResult.of(
                    false,
                    "One of provided coordinate are not correct",
                )
            entities.add(entity)
        }
        val table = arguments.getTable(1)
        val targetState = LuaInterpretation.asBlockState(table)
        if (targetState.`is`(BlockTags.REALITY_FORGER_FORBIDDEN)) {
            throw LuaException("You cannot use this block, is blocklisted")
        }
        val flags = arguments.optTable(2)
        entities.forEach {
            forgeRealityTileEntity(
                it,
                targetState,
                table,
                flags,
            )
        }
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    @Throws(LuaException::class)
    fun forgeReality(arguments: IArguments): MethodResult {
        val table = arguments.getTable(0)
        val targetState = LuaInterpretation.asBlockState(table)
        if (level == null) {
            return MethodResult.of(null, "Level is not loaded, what?")
        }
        if (targetState.`is`(BlockTags.REALITY_FORGER_FORBIDDEN)) {
            throw LuaException("You cannot use this block, is blocklisted")
        }
        ScanUtils.traverseBlocks(level!!, pos, interactionRadius, { _, blockPos ->
            val blockEntity = level!!.getBlockEntity(blockPos)
            if (blockEntity is FlexibleRealityAnchorBlockEntity) {
                forgeRealityTileEntity(blockEntity, targetState, table, arguments.optTable(1))
            }
        })
        return MethodResult.of(true)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RealityForgerPeripheral) return false
        if (!super.equals(other)) return false

        if (isEnabled != other.isEnabled) return false
        if (interactionRadius != other.interactionRadius) return false
        if (peripheralOwner != other.peripheralOwner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isEnabled.hashCode()
        result = 31 * result + interactionRadius
        result = 31 * result + peripheralOwner.hashCode()
        return result
    }


}
