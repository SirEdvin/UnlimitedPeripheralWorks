package site.siredvin.peripheralworks.testmod

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.testiarium.api.TestGroup

@TestGroup("peripheralworks")
class PeripheralWorksGameTests {
    @GameTest(template = "peripheralworksgametests.smoke")
    fun coreContentIsRegistered(helper: GameTestHelper) {
        check(Blocks.NETWORK_MANAGER.get().descriptionId == "block.peripheralworks.network_manager")
        helper.succeed()
    }
}
