package site.siredvin.peripheralworks.testmod

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.cct.thenLua

@TestGroup("peripheralworks")
class PeripheralWorksGameTests {
    @GameTest(template = "peripheralworksgametests.universal_scanner", timeoutTicks = 1200)
    fun universalScanner(helper: GameTestHelper) = helper.thenLua().thenSucceed()
}
