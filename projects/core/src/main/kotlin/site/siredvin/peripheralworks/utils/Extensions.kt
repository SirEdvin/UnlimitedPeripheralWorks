package site.siredvin.peripheralworks.utils

import net.minecraft.core.Direction
import net.minecraft.data.models.blockstates.VariantProperties.Rotation
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.lang.IllegalArgumentException
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

fun Direction.getRotation(to: Direction): Rotation {
    return when (to) {
        this -> Rotation.R0
        this.clockWise -> Rotation.R270
        this.counterClockWise -> Rotation.R90
        this.opposite -> Rotation.R180
        else -> throw IllegalArgumentException("Only horizontal rotation are supported")
    }
}

fun VoxelShape.rotate(from: Direction, to: Direction): VoxelShape {
    if (from !== to) {
        return this.rotate(from.getRotation(to))
    }
    return this
}
