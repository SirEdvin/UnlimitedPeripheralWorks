package site.siredvin.peripheralworks.testmod

import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import site.siredvin.peripheralworks.client.configurator.NetworkManagerGroupHierarchy
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.cct.thenLua

@TestGroup("peripheralworks")
class PeripheralWorksGameTests {
    @GameTest(template = "empty")
    fun peripheralProxyRenderSettingsPersistAndFallback(helper: GameTestHelper) {
        val firstPos = BlockPos(1, 1, 1)
        val secondPos = BlockPos(2, 1, 1)
        helper.setBlock(firstPos, Blocks.PERIPHERAL_PROXY.get())
        helper.setBlock(secondPos, Blocks.PERIPHERAL_PROXY.get())
        val proxy = helper.getBlockEntity(firstPos) as PeripheralProxyBlockEntity
        val loaded = helper.getBlockEntity(secondPos) as PeripheralProxyBlockEntity
        check(proxy.textStyle == TextStyle.REGULAR && proxy.boxStyle == BoxStyle.FLARE)
        proxy.setTextStyle(TextStyle.BOLD)
        proxy.setBoxStyle(BoxStyle.OUTLINE)
        loaded.loadInternalData(proxy.saveInternalData(CompoundTag()), null)
        check(loaded.textStyle == TextStyle.BOLD && loaded.boxStyle == BoxStyle.OUTLINE)
        loaded.loadInternalData(
            CompoundTag().apply {
                putString(PeripheralProxyBlockEntity.TEXT_STYLE_TAG, "INVALID")
                putString(PeripheralProxyBlockEntity.BOX_STYLE_TAG, BoxStyle.FILLED.name)
            },
            null,
        )
        check(loaded.textStyle == TextStyle.REGULAR && loaded.boxStyle == BoxStyle.FILLED)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun remoteObserverRenderSettingsPersistAndFallback(helper: GameTestHelper) {
        val firstPos = BlockPos(1, 1, 1)
        val secondPos = BlockPos(2, 1, 1)
        helper.setBlock(firstPos, Blocks.REMOTE_OBSERVER.get())
        helper.setBlock(secondPos, Blocks.REMOTE_OBSERVER.get())
        val observer = helper.getBlockEntity(firstPos) as RemoteObserverBlockEntity
        val loaded = helper.getBlockEntity(secondPos) as RemoteObserverBlockEntity
        check(observer.textStyle == TextStyle.NONE && observer.boxStyle == BoxStyle.FLARE)
        observer.setTextStyle(TextStyle.BOLD)
        observer.setBoxStyle(BoxStyle.OUTLINE)
        loaded.loadInternalData(observer.saveInternalData(CompoundTag()), null)
        check(loaded.textStyle == TextStyle.BOLD && loaded.boxStyle == BoxStyle.OUTLINE)
        loaded.loadInternalData(
            CompoundTag().apply {
                putString(RemoteObserverBlockEntity.TEXT_STYLE_TAG, TextStyle.REGULAR.name)
                putString(RemoteObserverBlockEntity.BOX_STYLE_TAG, "INVALID")
            },
            null,
        )
        check(loaded.textStyle == TextStyle.REGULAR && loaded.boxStyle == BoxStyle.FLARE)
        helper.succeed()
    }

    private fun getNetworkManager(helper: GameTestHelper): NetworkManagerBlockEntity {
        val pos = BlockPos(1, 1, 1)
        helper.setBlock(pos, Blocks.NETWORK_MANAGER.get())
        return helper.getBlockEntity(pos) as NetworkManagerBlockEntity
    }

    @GameTest(template = "empty")
    fun networkManagerRenamePreservesState(helper: GameTestHelper) {
        val manager = getNetworkManager(helper)
        manager.peripherals["monitor_0"] = BlockPos.ZERO
        check(manager.createGroup("old") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.setGroupColor("old", 0x123456) == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.setGroupMembership("old", "monitor_0", true) == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        val original = manager.peripheralGroups.getValue("old")

        check(manager.renameGroup("old", "new") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.peripheralGroups["new"] === original)
        check(original.color == 0x123456 && original.peripherals == setOf("monitor_0"))
        check(manager.setGroupVisibility("new", NetworkManagerBlockEntity.GroupVisibility.SHOW) == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(original.visibility == NetworkManagerBlockEntity.GroupVisibility.SHOW)
        check(NetworkManagerBlockEntity.PeripheralGroup.fromNBT(original.toNBT()).visibility == NetworkManagerBlockEntity.GroupVisibility.SHOW)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun networkManagerDestructiveDelete(helper: GameTestHelper) {
        val manager = getNetworkManager(helper)
        manager.peripherals["monitor_0"] = BlockPos.ZERO
        check(manager.createGroup("group") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.setGroupMembership("group", "monitor_0", true) == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.deleteGroup("group") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(!manager.peripheralGroups.containsKey("group"))
        check(manager.deleteGroup("group") == NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun networkManagerMembershipValidation(helper: GameTestHelper) {
        val manager = getNetworkManager(helper)
        manager.peripherals["monitor_0"] = BlockPos.ZERO
        check(manager.createGroup("group") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.setGroupMembership("group", "missing", true) == NetworkManagerBlockEntity.GroupOperationResult.PERIPHERAL_MISSING)
        check(manager.setGroupMembership("missing", "monitor_0", true) == NetworkManagerBlockEntity.GroupOperationResult.GROUP_MISSING)
        check(manager.toggleGroup("group", "monitor_0") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.peripheralGroups.getValue("group").peripherals.contains("monitor_0"))
        check(manager.toggleGroup("group", "monitor_0") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(!manager.peripheralGroups.getValue("group").peripherals.contains("monitor_0"))
        check(manager.setGroupMembership("group", "monitor_0", true, true) == NetworkManagerBlockEntity.GroupOperationResult.PERIPHERAL_NOT_PRESENT)
        check(!manager.peripheralGroups.getValue("group").peripherals.contains("monitor_0"))
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun networkManagerConfigurationAndHierarchyQuery(helper: GameTestHelper) {
        val manager = getNetworkManager(helper)
        manager.peripherals["monitor_0"] = BlockPos.ZERO
        manager.peripherals["printer_0"] = BlockPos.ZERO
        check(manager.createGroup("test2/a1") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.createGroup("test2/a2") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.setGroupMembership("test2/a1", "monitor_0", true) == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.setGroupMembership("test2/a2", "printer_0", true) == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.groupPeripherals("test2") == setOf("monitor_0", "printer_0"))
        check(manager.setDelimiter(".") == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.groupPeripherals("test2").isEmpty())
        check(manager.setRange(64) == NetworkManagerBlockEntity.GroupOperationResult.SUCCESS)
        check(manager.delimiter == "." && manager.range == 64)
        helper.succeed()
    }

    @GameTest(template = "empty")
    @TestGroup("network-manager-hierarchy")
    fun networkManagerGroupHierarchyFlatMode(helper: GameTestHelper) {
        val hierarchy = NetworkManagerGroupHierarchy.build(listOf("factory/ore", "storage"), "")
        check(hierarchy.roots.map { it.segment } == listOf("factory/ore", "storage"))
        check(hierarchy.roots.map { it.group?.fullName } == listOf("factory/ore", "storage"))
        helper.succeed()
    }

    @GameTest(template = "empty")
    @TestGroup("network-manager-hierarchy")
    fun networkManagerGroupHierarchyNestedPaths(helper: GameTestHelper) {
        val hierarchy = NetworkManagerGroupHierarchy.build(listOf("factory/ore/iron", "factory/ore/gold"), "/")
        val ore = hierarchy.roots.single().children.single()
        check(ore.path == listOf("factory", "ore"))
        check(ore.children.map { it.group?.fullName } == listOf("factory/ore/gold", "factory/ore/iron"))
        helper.succeed()
    }

    @GameTest(template = "empty")
    @TestGroup("network-manager-hierarchy")
    fun networkManagerGroupHierarchySearch(helper: GameTestHelper) {
        val hierarchy = NetworkManagerGroupHierarchy.build(listOf("factory/ore/iron", "storage"), "/")
        check(hierarchy.search("ORE").map { it.fullName } == listOf("factory/ore/iron"))
        check(hierarchy.search("").map { it.fullName } == listOf("factory/ore/iron", "storage"))
        helper.succeed()
    }

    @GameTest(template = "empty")
    @TestGroup("network-manager-hierarchy")
    fun networkManagerGroupHierarchyEmptyAndAmbiguousSegments(helper: GameTestHelper) {
        val hierarchy = NetworkManagerGroupHierarchy.build(listOf("", "/a", "a", "a/", "a//b"), "/")
        check(hierarchy.leaves.map { it.fullName } == listOf("", "/a", "a", "a/", "a//b"))
        check(
            hierarchy.leaves.associate { it.fullName to it.segments } == mapOf(
                "" to listOf(""),
                "/a" to listOf("", "a"),
                "a" to listOf("a"),
                "a/" to listOf("a", ""),
                "a//b" to listOf("a", "", "b"),
            ),
        )
        check(hierarchy.roots.single { it.segment == "a" }.group?.fullName == "a")
        helper.succeed()
    }

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
