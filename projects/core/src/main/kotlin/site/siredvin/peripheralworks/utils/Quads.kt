package site.siredvin.peripheralworks.utils

import dan200.computercraft.api.lua.LuaException
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import org.joml.Vector3f
import site.siredvin.peripheralworks.common.block.FlexibleStatue
import java.io.*

data class QuadData(val x1: Float, val x2: Float, val y1: Float, val y2: Float, val z1: Float, val z2: Float, val texture: ResourceLocation, val tint: Int, val opacity: Float) {

    constructor(start: Vector3f, end: Vector3f, texture: ResourceLocation, tint: Int, opacity: Float) : this(
        start.x,
        end.x,
        start.y,
        end.y,
        start.z,
        end.z,
        texture,
        tint,
        opacity,
    )

    constructor(data: CompoundTag) : this(
        data.getFloat("x1"),
        data.getFloat("x2"),
        data.getFloat("y1"),
        data.getFloat("y2"),
        data.getFloat("z1"),
        data.getFloat("z2"),
        ResourceLocation(data.getString("texture")),
        data.getInt("tint"),
        if (data.contains("opacity")) data.getFloat("opacity") else 1f,
    )

    val start: Vector3f
        get() = Vector3f(x1, y1, z1)

    val end: Vector3f
        get() = Vector3f(x2, y2, z2)

    val uv: FloatArray
        get() = floatArrayOf(x1 / 4, z1 / 4, x2 / 4, z2 / 4)

    val shape: VoxelShape
        get() = Shapes.box(
            (x1 / 16).toDouble(),
            (y1 / 16).toDouble(),
            (z1 / 16).toDouble(),
            (x2 / 16).toDouble(),
            (y2 / 16).toDouble(),
            (z2 / 16).toDouble(),
        )

    fun toLua(): Map<String, Any> {
        return mapOf(
            "x1" to x1,
            "x2" to x2,
            "y1" to y1,
            "y2" to y2,
            "z1" to z1,
            "z2" to z2,
            "texture" to texture.toString(),
            "tint" to tint,
            "opacity" to opacity,
        )
    }

    fun toTag(): CompoundTag {
        val data = CompoundTag()
        data.putFloat("x1", x1)
        data.putFloat("x2", x2)
        data.putFloat("y1", y1)
        data.putFloat("y2", y2)
        data.putFloat("z1", z1)
        data.putFloat("z2", z2)
        data.putInt("tint", tint)
        data.putString("texture", texture.toString())
        data.putFloat("opacity", opacity.coerceAtMost(1f))
        return data
    }

    fun toAABB(): AABB {
        return AABB(
            x1.toDouble(),
            y1.toDouble(),
            z1.toDouble(),
            x2.toDouble(),
            y2.toDouble(),
            z2.toDouble(),
        )
    }
}

data class QuadList(val list: List<QuadData>) {

    constructor(data: ListTag) : this(
        data.mapNotNull {
            if (it !is CompoundTag) return@mapNotNull null
            QuadData(it)
        },
    )

    val shape: VoxelShape
        get() = list.stream().map(QuadData::shape).reduce(Shapes::or).orElse(Shapes.empty())

    fun toLua(): List<Map<String, Any>> {
        return list.map(QuadData::toLua)
    }

    fun toTag(): ListTag {
        val base = ListTag()
        list.forEach { base.add(it.toTag()) }
        return base
    }
}

private const val MAX_QUAD_VECTOR = 32.0f
private const val MIN_QUAD_VECTOR = -32.0f

@Throws(LuaException::class)
private fun buildVector(x: Float, y: Float, z: Float, min: Float, max: Float): Vector3f {
    if (x < min || y < min || z < min) throw LuaException(String.format("Coordinate lower then %.2f", min))
    if (x > max || y > max || z > max) throw LuaException(String.format("Coordinate bigger then %.2f", min))
    return Vector3f(x, y, z)
}

@Throws(LuaException::class)
fun convertToStartVector(table: Map<*, *>, min: Float, max: Float): Vector3f {
    if (!table.containsKey("x1") || !table.containsKey("y1") || !table.containsKey("z1")) throw LuaException("Table should have start coordinates")
    val x = table["x1"]
    val y = table["y1"]
    val z = table["z1"]
    if (x !is Number || y !is Number || z !is Number) throw LuaException("Table should have start coordinates")
    return buildVector(x.toFloat(), y.toFloat(), z.toFloat(), min, max)
}

@Throws(LuaException::class)
fun convertToEndVector(table: Map<*, *>, min: Float, max: Float): Vector3f {
    if (!table.containsKey("x2") || !table.containsKey("y2") || !table.containsKey("z2")) throw LuaException("Table should have end coordinates")
    val x = table["x2"]
    val y = table["y2"]
    val z = table["z2"]
    if (x !is Number || y !is Number || z !is Number) throw LuaException("Table should have end coordinates")
    return buildVector(x.toFloat(), y.toFloat(), z.toFloat(), min, max)
}

@Throws(LuaException::class)
fun convertToQuadData(table: Map<*, *>): QuadData {
    val startVector = convertToStartVector(table, MIN_QUAD_VECTOR, MAX_QUAD_VECTOR)
    val endVector = convertToEndVector(table, MIN_QUAD_VECTOR, MAX_QUAD_VECTOR)
    val texture = if (table.containsKey("texture")) {
        ResourceLocation(table["texture"].toString())
    } else {
        FlexibleStatue.WHITE_TEXTURE
    }
    val tint = if (table.containsKey("tint")) {
        (table["tint"] as Number).toInt()
    } else {
        0xFFFFFF
    }
    val opacity = if (table.containsKey("opacity")) {
        (table["opacity"] as Number).toFloat().coerceAtMost(1f)
    } else {
        1f
    }
    return QuadData(startVector, endVector, texture, tint, opacity)
}

@Throws(LuaException::class)
fun convertToQuadList(table: Map<*, *>): QuadList {
    val data: MutableList<QuadData> = ArrayList()
    for (value in table.values) {
        if (value !is Map<*, *>) throw LuaException("Table should be quad list")
        data.add(convertToQuadData(value))
    }
    return QuadList(data)
}
