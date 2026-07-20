package site.siredvin.peripheralworks.testmod

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.cct.thenLua

@TestGroup("peripheralworks")
class PeripheralWorksGameTests {
    @GameTest(template = "peripheralworksgametests.universal_scanner", timeoutTicks = 1200)
    fun universalScanner(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.ultimate_sensor", timeoutTicks = 1200)
    fun ultimateSensor(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.item_pedestal", timeoutTicks = 1200)
    fun itemPedestal(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.map_pedestal", timeoutTicks = 1200)
    fun mapPedestal(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.display_pedestal", timeoutTicks = 1200)
    fun displayPedestal(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.remote_observer", timeoutTicks = 1200)
    fun remoteObserver(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.peripheral_proxy", timeoutTicks = 1200)
    fun peripheralProxy(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.reality_forger", timeoutTicks = 1200)
    fun realityForger(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.recipe_registry", timeoutTicks = 1200)
    fun recipeRegistry(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.informative_registry", timeoutTicks = 1200)
    fun informativeRegistry(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.statue_workbench", timeoutTicks = 1200)
    fun statueWorkbench(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.entity_link", timeoutTicks = 1200)
    fun entityLink(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.network_manager", timeoutTicks = 1200)
    fun networkManager(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.hologram_projector", timeoutTicks = 1200)
    fun hologramProjector(helper: GameTestHelper) = helper.thenLua().thenSucceed()
}
