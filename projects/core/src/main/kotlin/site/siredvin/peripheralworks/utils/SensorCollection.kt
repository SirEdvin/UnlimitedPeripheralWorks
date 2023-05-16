package site.siredvin.peripheralworks.utils

import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.commands.TimeCommand
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.LightLayer
import site.siredvin.peripheralium.api.peripheral.IPeripheralOwner
import site.siredvin.peripheralium.util.world.ScanUtils
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import net.minecraft.world.level.WorldGenLevel

import net.minecraft.world.level.levelgen.WorldgenRandom
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform


object SensorCollection {

    const val MIN_Y = -64
    const val MAX_Y = 320
    const val VANILLA_SLIME_CHUNK_SALT = 987234911L

    fun inspectDimension(owner: IPeripheralOwner): MethodResult {
        val level = owner.level ?: return MethodResult.of(null, "Something isn't loaded correctly")
        return MethodResult.of(level.dimension().location().toString())
    }

    fun analyzeDimensions(owner: IPeripheralOwner): MethodResult {
        val level = owner.level as? ServerLevel ?: return MethodResult.of(null, "Something isn't loaded correctly")
        val server = level.server
        return MethodResult.of(server.allLevels.map { it.dimension().location().toString() })
    }

    fun inspectBiome(owner: IPeripheralOwner): MethodResult {
        val level = owner.level as? ServerLevel ?: return MethodResult.of(null, "Something isn't loaded correctly")
        return MethodResult.of(level.getBiome(owner.pos).unwrapKey().map { it.location().toString() }.orElse("unknown"))
    }

    fun inspectWeather(owner: IPeripheralOwner): MethodResult {
        val level = owner.level as? ServerLevel ?: return MethodResult.of(null, "Something isn't loaded correctly")
        if (level.isThundering)
            return MethodResult.of("thunder")
        if (level.isRaining)
            return MethodResult.of("rain")
        return MethodResult.of("stable")
    }

    fun inspectOrientationAngle(owner: IPeripheralOwner): MethodResult {
        return when (owner.facing) {
            Direction.NORTH -> MethodResult.of(0)
            Direction.SOUTH -> MethodResult.of(180)
            Direction.WEST -> MethodResult.of(270)
            Direction.EAST -> MethodResult.of(90)
            else -> MethodResult.of(null, "Cannot determinate angle to north pole")
        }
    }

    fun inspectTime(owner: IPeripheralOwner): MethodResult {
        val level = owner.level as? ServerLevel ?: return MethodResult.of(null, "Something isn't loaded correctly")
        return MethodResult.of(level.dayTime)
    }

    fun inspectLight(owner: IPeripheralOwner): MethodResult {
        val level = owner.level as? ServerLevel ?: return MethodResult.of(null, "Something isn't loaded correctly")
        return MethodResult.of(mapOf(
            "light" to level.getBrightness(LightLayer.BLOCK, owner.pos.above()),
            "sunlight" to level.getBrightness(LightLayer.SKY, owner.pos.above()),
        ))
    }

    private fun decodeMoonPhase(level: Level): String {
        return when (level.moonPhase) {
            0 -> "Full moon"
            1 -> "Waning gibbous"
            2 -> "Third quarter"
            3 -> "Waning crescent"
            4 -> "New moon"
            5 -> "Waxing crescent"
            6 -> "First quarter"
            7 -> "Waxing gibbous"
            else -> "Unknown moon phase"
        }
    }

    fun inspectCalendar(owner: IPeripheralOwner): MethodResult {
        val level = owner.level as? ServerLevel ?: return MethodResult.of(null, "Something isn't loaded correctly")
        val inspectionData = mutableMapOf<String, Any>()
        inspectionData["day"] = level.dayTime / 24000L % 2147483647L
        if (level.dimension() == Level.OVERWORLD) {
            inspectionData["moonPhase"] = decodeMoonPhase(level)
        }
        return MethodResult.of(inspectionData)
    }

    fun inspectChunk(owner: IPeripheralOwner): MethodResult {
        val level = owner.level as? ServerLevel ?: return MethodResult.of(null, "Something isn't loaded correctly")
        val information = mutableMapOf<String, Any>()
        val ores = mutableMapOf<String, Int>()
        val chunkPos = ChunkPos(owner.pos)
        for (x in chunkPos.minBlockX..chunkPos.maxBlockX) {
            for (z in chunkPos.minBlockZ..chunkPos.maxBlockZ) {
                for (y in MIN_Y..MAX_Y) {
                    val blockState = level.getBlockState(BlockPos(x, y, z))
                    if (!blockState.isAir && PeripheraliumPlatform.isOre(blockState)) {
                        val key = XplatRegistries.BLOCKS.getKey(blockState.block).toString()
                        if (!ores.containsKey(key))
                            ores[key] = 0
                        ores[key] = ores[key]!! + 1
                    }
                }
            }
        }
        information["oresDistribution"] = ores
        information["isSlime"] = WorldgenRandom.seedSlimeChunk(chunkPos.x, chunkPos.z, level.seed, VANILLA_SLIME_CHUNK_SALT).nextInt(10) == 0;
        return MethodResult.of(information)
    }
}