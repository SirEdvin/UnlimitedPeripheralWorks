package site.siredvin.peripheralworks.testmod

import net.minecraft.client.renderer.ViewArea
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.common.block.FlexibleStatue
import site.siredvin.peripheralworks.common.blockentity.FlexibleStatueBlockEntity
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.utils.QuadData
import site.siredvin.peripheralworks.utils.QuadList
import site.siredvin.testiarium.api.ClientGameTest
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.fixture.client.thenOnClient

@TestGroup("statue-client")
class StatueUpdateClientGameTests {
    @ClientGameTest(template = "empty", timeoutTicks = 600)
    fun invalidatesRenderSectionAfterLoadingChangedAndClearedData(helper: GameTestHelper) {
        val pos = BlockPos(1, 1, 1)
        val absolute = helper.absolutePos(pos)
        helper.startSequence()
            .thenExecute {
                helper.setBlock(pos, Blocks.FLEXIBLE_STATUE.get().defaultBlockState().setValue(FlexibleStatue.CONFIGURED, true))
            }
            .thenIdle(20)
            .thenOnClient {
                val entity = minecraft.level!!.getBlockEntity(absolute) as FlexibleStatueBlockEntity
                // Locate by type so this test-only inspection does not depend on mapped private field names.
                val field = minecraft.levelRenderer.javaClass.declaredFields.single { it.type == ViewArea::class.java }
                field.isAccessible = true
                val area = field.get(minecraft.levelRenderer) as ViewArea
                val section = area.chunks.single { it.origin.x == (absolute.x and -16) && it.origin.y == (absolute.y and -16) && it.origin.z == (absolute.z and -16) }
                val state = entity.blockState
                for (size in listOf(16f, 8f, 4f)) {
                    val quads = QuadList(listOf(QuadData(0f, size, 0f, size, 0f, size, ResourceLocation("minecraft", "block/stone"), 0xFFFFFF, 1f)))
                    val tag = CompoundTag().apply { put(FlexibleStatueBlockEntity.BAKED_QUADS_TAG, quads.toTag()) }
                    section.setNotDirty()
                    entity.load(tag)
                    check(entity.bakedQuads == quads) { "Statue data did not load" }
                    check(entity.blockState == state) { "Fixture must exercise unchanged blockstate" }
                    check(section.isDirty) { "Loading statue size $size did not invalidate its render section" }
                }
                section.setNotDirty()
                entity.load(CompoundTag())
                check(entity.bakedQuads == null && entity.blockShape == null) { "Clear packet retained stale geometry" }
                check(section.isDirty) { "Clearing statue did not invalidate rendering" }
            }
            .thenSucceed()
    }
}
