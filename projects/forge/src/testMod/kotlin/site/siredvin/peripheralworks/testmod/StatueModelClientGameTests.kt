package site.siredvin.peripheralworks.testmod

import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraftforge.client.model.data.ModelData
import site.siredvin.peripheralworks.client.model.FlexibleStatueModel
import site.siredvin.peripheralworks.client.model.ItemFlexibleStatueModel
import site.siredvin.peripheralworks.utils.QuadData
import site.siredvin.peripheralworks.utils.QuadList
import site.siredvin.testiarium.api.ClientGameTest
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.fixture.client.thenOnClient
import kotlin.math.abs

class StatueModelClientGameTests {
    @TestGroup("statue-faces-client")
    @ClientGameTest(template = "empty")
    fun preservesAllFacesAcrossRotationsAndItemPasses(helper: GameTestHelper) {
        helper.startSequence().thenOnClient {
            val cubes = QuadList(listOf(cube(0f, 16f, 0f, 16f, 0f, 16f), cube(2f, 6f, 3f, 11f, 4f, 12f), cube(-8f, 0f, 0f, 8f, 16f, 24f)))
            for (facing in Direction.Plane.HORIZONTAL) {
                val data = ModelData.builder().with(FlexibleStatueModel.QUADS, cubes).with(FlexibleStatueModel.FACING, facing).build()
                val quads = FlexibleStatueModel.getQuads(null, null, RandomSource.create(), data, null)
                check(quads.size == 18) { "$facing must emit exactly six unculled faces per cube, got ${quads.size}" }
                check(quads.groupingBy { it.direction }.eachCount().values.all { it == 3 }) { "Missing rotated face at $facing" }
                Direction.entries.forEach { side ->
                    check(FlexibleStatueModel.getQuads(null, side, RandomSource.create(), data, null).isEmpty()) { "Neighbor $side can incorrectly cull geometry at $facing" }
                }
            }
            val item = ItemFlexibleStatueModel(cubes)
            check(item.getQuads(null, null, RandomSource.create(), ModelData.EMPTY, null).size == 18)
            Direction.entries.forEach { check(item.getQuads(null, it, RandomSource.create(), ModelData.EMPTY, null).isEmpty()) }
        }.thenSucceed()
    }

    @TestGroup("statue-uv-client")
    @ClientGameTest(template = "empty")
    fun usesPixelScaleUvsOnEveryFace(helper: GameTestHelper) {
        helper.startSequence().thenOnClient {
            for (cube in listOf(cube(0f, 16f, 0f, 16f, 0f, 16f), cube(2f, 6f, 3f, 11f, 5f, 7f))) {
                val model = ItemFlexibleStatueModel(QuadList(listOf(cube)))
                // Collect all passes so this test also diagnoses UVs on the old, side-bucketed model.
                val quads = Direction.entries.flatMap { model.getQuads(null, it, RandomSource.create(), ModelData.EMPTY, null) } + model.getQuads(null, null, RandomSource.create(), ModelData.EMPTY, null)
                check(quads.map { it.direction }.toSet().size == 6)
                for (quad in quads) {
                    val u = (0..3).map { Float.fromBits(quad.vertices[it * 8 + 4]) }
                    val v = (0..3).map { Float.fromBits(quad.vertices[it * 8 + 5]) }
                    val expected = when (quad.direction) {
                        Direction.DOWN -> floatArrayOf(cube.x1, 16f - cube.z2, cube.x2, 16f - cube.z1)
                        Direction.UP -> floatArrayOf(cube.x1, cube.z1, cube.x2, cube.z2)
                        Direction.NORTH -> floatArrayOf(16f - cube.x2, 16f - cube.y2, 16f - cube.x1, 16f - cube.y1)
                        Direction.SOUTH -> floatArrayOf(cube.x1, 16f - cube.y2, cube.x2, 16f - cube.y1)
                        Direction.WEST -> floatArrayOf(cube.z1, 16f - cube.y2, cube.z2, 16f - cube.y1)
                        Direction.EAST -> floatArrayOf(16f - cube.z2, 16f - cube.y2, 16f - cube.z1, 16f - cube.y1)
                    }
                    val sprite = quad.sprite
                    val actual = listOf((u.min() - sprite.u0) / (sprite.u1 - sprite.u0) * 16, (v.min() - sprite.v0) / (sprite.v1 - sprite.v0) * 16, (u.max() - sprite.u0) / (sprite.u1 - sprite.u0) * 16, (v.max() - sprite.v0) / (sprite.v1 - sprite.v0) * 16)
                    // FaceBakery slightly shrinks UVs to prevent atlas bleeding.
                    check(actual.zip(expected.toList()).all { (a, e) -> abs(a - e) < 0.15f }) { "Wrong ${quad.direction} UVs: $actual, expected ${expected.toList()}" }
                }
            }
        }.thenSucceed()
    }
}

private fun cube(x1: Float, x2: Float, y1: Float, y2: Float, z1: Float, z2: Float) = QuadData(x1, x2, y1, y2, z1, z2, ResourceLocation.parse("minecraft:block/stone"), 0xFFFFFF, 1f)
