package site.siredvin.peripheralworks.testmod

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.cct.thenLua

@TestGroup("peripheralworks")
class PeripheralWorksGameTests {
    @GameTest(template = "peripheralworksgametests.universal_scanner", batch = "peripheralworksgametests.universal_scanner", timeoutTicks = 2400)
    fun universalScanner(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.ultimate_sensor", batch = "peripheralworksgametests.ultimate_sensor", timeoutTicks = 2400)
    fun ultimateSensor(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.item_pedestal", batch = "peripheralworksgametests.item_pedestal", timeoutTicks = 2400)
    fun itemPedestal(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.map_pedestal", batch = "peripheralworksgametests.map_pedestal", timeoutTicks = 2400)
    fun mapPedestal(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.display_pedestal", batch = "peripheralworksgametests.display_pedestal", timeoutTicks = 2400)
    fun displayPedestal(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.remote_observer", batch = "peripheralworksgametests.remote_observer", timeoutTicks = 2400)
    fun remoteObserver(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.peripheral_proxy", batch = "peripheralworksgametests.peripheral_proxy", timeoutTicks = 2400)
    fun peripheralProxy(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.reality_forger", batch = "peripheralworksgametests.reality_forger", timeoutTicks = 2400)
    fun realityForger(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.recipe_registry", batch = "peripheralworksgametests.recipe_registry", timeoutTicks = 2400)
    fun recipeRegistry(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.informative_registry", batch = "peripheralworksgametests.informative_registry", timeoutTicks = 2400)
    fun informativeRegistry(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.statue_workbench", batch = "peripheralworksgametests.statue_workbench", timeoutTicks = 2400)
    fun statueWorkbench(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.entity_link", batch = "peripheralworksgametests.entity_link", timeoutTicks = 2400)
    fun entityLink(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.network_manager", batch = "peripheralworksgametests.network_manager", timeoutTicks = 2400)
    fun networkManager(helper: GameTestHelper) = helper.thenLua().thenSucceed()

    @GameTest(template = "peripheralworksgametests.hologram_projector", batch = "peripheralworksgametests.hologram_projector", timeoutTicks = 2400)
    fun hologramProjector(helper: GameTestHelper) = helper.thenLua().thenSucceed()
}
