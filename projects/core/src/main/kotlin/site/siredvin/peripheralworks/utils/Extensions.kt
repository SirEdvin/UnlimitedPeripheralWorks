package site.siredvin.peripheralworks.utils

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.data.models.blockstates.VariantProperties.Rotation
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import site.siredvin.tweakium.modules.peripheral.representation.LuaInterpretation
import java.lang.IllegalArgumentException
import java.util.Arrays
import java.util.stream.Collectors
import kotlin.collections.get
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun VoxelShape.rotate(rotation: Rotation): VoxelShape {
    val rotationAngle = when (rotation) {
        Rotation.R0 -> 0.0
        Rotation.R90 -> PI / 2
        Rotation.R180 -> PI
        Rotation.R270 -> -PI / 2
    }
    var movingShape = Shapes.empty()
    this.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
        val x1 = (minZ - 0.5) * sin(rotationAngle) + (minX - 0.5) * cos(rotationAngle) + 0.5
        val x2 = (maxZ - 0.5) * sin(rotationAngle) + (maxX - 0.5) * cos(rotationAngle) + 0.5
        val z1 = (minZ - 0.5) * cos(rotationAngle) - (minX - 0.5) * sin(rotationAngle) + 0.5
        val z2 = (maxZ - 0.5) * cos(rotationAngle) - (maxX - 0.5) * sin(rotationAngle) + 0.5
        movingShape = Shapes.or(
            movingShape,
            Shapes.box(
                kotlin.math.min(x1, x2),
                minY,
                kotlin.math.min(z1, z2),
                kotlin.math.max(x1, x2),
                maxY,
                kotlin.math.max(z1, z2),
            ),
        )
    }
    return movingShape
}

fun Direction.getRotation(to: Direction): Rotation = when (to) {
    this -> Rotation.R0
    this.clockWise -> Rotation.R270
    this.counterClockWise -> Rotation.R90
    this.opposite -> Rotation.R180
    else -> throw IllegalArgumentException("Only horizontal rotation are supported")
}

fun VoxelShape.rotate(from: Direction, to: Direction): VoxelShape {
    if (from !== to) {
        return this.rotate(from.getRotation(to))
    }
    return this
}

fun IArguments.getBlockPos(index: Int): BlockPos = LuaInterpretation.asBlockPos(this.getTable(index))

fun IArguments.getVec3i(index: Int): Vec3i {
    val table = this.getTable(index)
    if (!table.containsKey("x") || !table.containsKey("y") || !table.containsKey("z")) throw LuaException("Table should be block position table")
    val x = table["x"]
    val y = table["y"]
    val z = table["z"]
    if (x !is Number || y !is Number || z !is Number) throw LuaException("Table should be block position table")
    return Vec3i(x.toInt(), y.toInt(), z.toInt())
}

fun IArguments.optDirection(index: Int): Direction? {
    val directionArgument = this.optString(index)
    return if (directionArgument.isEmpty) {
        null
    } else {
        try {
            Direction.valueOf(
                directionArgument.get().uppercase(),
            )
        } catch (exc: IllegalArgumentException) {
            val allValues = Arrays.stream(Direction.entries.toTypedArray()).map { mode -> mode.name.lowercase() }.collect(
                Collectors.toList(),
            ).joinToString(", ")
            throw LuaException("Vertical direction should be one of: $allValues")
        }
    }
}
