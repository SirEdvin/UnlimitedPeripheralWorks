package site.siredvin.peripheralworks.utils

import com.mojang.math.Axis
import net.minecraft.core.Direction
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import org.joml.Vector3f
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// Helpers for render from https://github.com/SwitchCraftCC/sc-library/blob/1.20.1/src/main/kotlin/io/sc3/library/ext/BoxExt.kt

val unitBox = AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
val unitCube: List<List<Vector3f>> = listOf(
    listOf(Vector3f(0.0f, 0.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f), Vector3f(1.0f, 0.0f, 0.0f), Vector3f(1.0f, 0.0f, 1.0f)),
    listOf(Vector3f(0.0f, 1.0f, 0.0f), Vector3f(0.0f, 1.0f, 1.0f), Vector3f(1.0f, 1.0f, 1.0f), Vector3f(1.0f, 1.0f, 0.0f)),
    listOf(Vector3f(1.0f, 1.0f, 0.0f), Vector3f(1.0f, 0.0f, 0.0f), Vector3f(0.0f, 0.0f, 0.0f), Vector3f(0.0f, 1.0f, 0.0f)),
    listOf(Vector3f(0.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 1.0f), Vector3f(1.0f, 0.0f, 1.0f), Vector3f(1.0f, 1.0f, 1.0f)),
    listOf(Vector3f(0.0f, 1.0f, 0.0f), Vector3f(0.0f, 0.0f, 0.0f), Vector3f(0.0f, 0.0f, 1.0f), Vector3f(0.0f, 1.0f, 1.0f)),
    listOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(1.0f, 0.0f, 1.0f), Vector3f(1.0f, 0.0f, 0.0f), Vector3f(1.0f, 1.0f, 0.0f)),
)

fun Vec3.rotate(axis: Axis, angle: Float): Vec3 = when (axis) {
    Axis.XP -> xRot(angle)
    Axis.XN -> xRot(-angle)
    Axis.YP -> yRot(angle)
    Axis.YN -> yRot(-angle)
    Axis.ZP -> zRot(angle)
    Axis.ZN -> zRot(-angle)
    else -> this
}

fun AABB.rotateTowards(facing: Direction): AABB = rotateY(
    when (facing) {
        Direction.EAST -> 3
        Direction.SOUTH -> 2
        Direction.WEST -> 1
        else -> 0
    },
)

fun AABB.rotate(facing: Direction) = when (facing) {
    Direction.UP -> this
    Direction.DOWN -> rotateX(2)
    else -> rotateX(1).rotateTowards(facing)
}

fun AABB.rotate(axis: Axis, count: Int): AABB {
    val angle = count * Math.PI.toFloat() / 2
    val min = Vec3(minX - 8, minY - 8, minZ - 8).rotate(axis, angle)
    val max = Vec3(maxX - 8, maxY - 8, maxZ - 8).rotate(axis, angle)

    return AABB(
        (min(min.x + 8, max.x + 8) * 32).roundToInt() / 32.0,
        (min(min.y + 8, max.y + 8) * 32).roundToInt() / 32.0,
        (min(min.z + 8, max.z + 8) * 32).roundToInt() / 32.0,
        (max(min.x + 8, max.x + 8) * 32).roundToInt() / 32.0,
        (max(min.y + 8, max.y + 8) * 32).roundToInt() / 32.0,
        (max(min.z + 8, max.z + 8) * 32).roundToInt() / 32.0,
    )
}

fun AABB.rotateX(count: Int) = rotate(Axis.XP, count)
fun AABB.rotateY(count: Int) = rotate(Axis.YP, count)
fun AABB.rotateZ(count: Int) = rotate(Axis.ZP, count)

fun AABB.toDiv16(): AABB =
    AABB(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0)

fun AABB.toMul16(): AABB =
    AABB(minX * 16.0, minY * 16.0, minZ * 16.0, maxX * 16.0, maxY * 16.0, maxZ * 16.0)

fun AABB.toDiv16VoxelShape(): VoxelShape =
    Shapes.box(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0)

val AABB.faces: List<List<Vector3f>>
    get() = unitCube.map { face ->
        face.map { vertex ->
            Vector3f(
                max(minX.toFloat() / 16.0f, min(maxX.toFloat() / 16.0f, vertex.x)),
                max(minY.toFloat() / 16.0f, min(maxY.toFloat() / 16.0f, vertex.y)),
                max(minZ.toFloat() / 16.0f, min(maxZ.toFloat() / 16.0f, vertex.z)),
            )
        }
    }
