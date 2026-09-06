package site.siredvin.peripheralworks.testmod

import appeng.api.crafting.PatternDetailsHelper
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import appeng.core.definitions.AEItems
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import site.siredvin.peripheralworks.integrations.ae2.AE2PatternPedestalBlockEntity
import site.siredvin.peripheralworks.integrations.ae2.Registration
import site.siredvin.testiarium.api.ClientGameTest
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.fixture.client.thenOnClient
import site.siredvin.testiarium.fixture.client.thenScreenshot

@TestGroup("ae2-pattern-client")
class AE2PatternPedestalClientGameTests {
    @ClientGameTest(template = "empty", timeoutTicks = 600)
    fun rendersSynchronizedPattern(helper: GameTestHelper) {
        val pos = BlockPos(1, 1, 1)
        val absolute = helper.absolutePos(pos)
        val screenshot = java.io.File(System.getProperty("testiarium.screenshots"), "screenshots/ae2-pattern-pedestal.png")
        screenshot.parentFile.mkdirs()
        screenshot.delete()
        val pattern = PatternDetailsHelper.encodeProcessingPattern(arrayOf(GenericStack(AEItemKey.of(Items.STONE), 1)), arrayOf(GenericStack(AEItemKey.of(Items.DIAMOND), 1)))
        helper.startSequence()
            .thenExecute {
                helper.setBlock(pos, Registration.PATTERN_PEDESTAL.get())
                val entity = helper.getBlockEntity(pos) as AE2PatternPedestalBlockEntity
                entity.storage.store(AEItems.BLANK_PATTERN.stack(), false)
                check(entity.replacePattern(entity.storedStack.copy(), pattern))
                helper.level.players().first().teleportTo(helper.level, absolute.x + 0.5, absolute.y + 1.5, absolute.z + 3.5, 180f, 25f)
            }
            .thenIdle(20)
            .thenOnClient {
                val entity = minecraft.level!!.getBlockEntity(absolute) as AE2PatternPedestalBlockEntity
                check(ItemStack.matches(entity.storedStack, pattern)) { "Encoded item did not synchronize to client" }
                check(minecraft.blockEntityRenderDispatcher.getRenderer(entity) != null) { "Pedestal renderer is missing" }
                val model = minecraft.blockRenderer.getBlockModel(entity.blockState)
                check(model !== minecraft.modelManager.missingModel) { "Pedestal model is missing" }
                @Suppress("DEPRECATION")
                val particle = model.particleIcon
                check(particle.contents().name().toString() == "ae2:block/quartz_block") { "Pedestal must use the certus quartz texture" }
                check(ItemStack(Registration.PATTERN_PEDESTAL.get()).hoverName.string == "Pattern pedestal") { "Pedestal display name is wrong" }
            }
            .thenScreenshot("ae2-pattern-pedestal")
            .thenWaitUntil {
                if (!screenshot.isFile) throw net.minecraft.gametest.framework.GameTestAssertException("Screenshot has not been saved")
            }
            .thenSucceed()
    }
}
