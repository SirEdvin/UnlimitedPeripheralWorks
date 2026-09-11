package site.siredvin.peripheralworks.testmod

import com.electronwill.nightconfig.core.UnmodifiableConfig
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootDataType
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraftforge.common.ForgeConfigSpec
import site.siredvin.peripheralworks.client.configurator.NetworkManagerGroupHierarchy
import site.siredvin.peripheralworks.common.block.NetworkManager
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity
import site.siredvin.peripheralworks.common.configuration.ConfigHolder
import site.siredvin.peripheralworks.common.configuration.IntegrationConfigurationDiscovery
import site.siredvin.peripheralworks.common.item.UltimateConfigurator
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.ConfiguratorTarget
import site.siredvin.peripheralworks.subsystem.configurator.NetworkManagerMode
import site.siredvin.peripheralworks.subsystem.configurator.PeripheralProxyMode
import site.siredvin.peripheralworks.subsystem.configurator.RemoteObserverMode
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.testiarium.api.TestGroup
import site.siredvin.testiarium.cct.thenLua

@TestGroup("peripheralworks")
class PeripheralWorksGameTests {
    @GameTest(template = "empty")
    fun optionalAE2Resources(helper: GameTestHelper) {
        val ae2Present = BuiltInRegistries.BLOCK.containsKey(ResourceLocation("ae2", "controller"))
        val server = helper.level.server
        val loot = server.lootData
        listOf("ae2_pattern_pedestal", "me_network_peripheral").forEach { name ->
            val id = ResourceLocation("peripheralworks", name)
            val table = ResourceLocation("peripheralworks", "blocks/$name")
            check(BuiltInRegistries.BLOCK.containsKey(id) == ae2Present) { "$id block registration does not follow AE2 presence" }
            check(BuiltInRegistries.ITEM.containsKey(id) == ae2Present) { "$id item registration does not follow AE2 presence" }
            check(server.resourceManager.getResource(ResourceLocation("peripheralworks", "loot_tables/blocks/$name.json")).isPresent == ae2Present) {
                "$id loot resource must not be exposed to parsing without AE2"
            }
            check(loot.getKeys(LootDataType.TABLE).contains(table) == ae2Present) { "$id loot table presence does not follow AE2 presence" }
            check(server.recipeManager.byKey(id).isPresent == ae2Present) { "$id recipe presence does not follow AE2 presence" }
            if (ae2Present) {
                check(loot.getLootTable(table) !== LootTable.EMPTY) { "$id loot table failed to load" }
                val block = BuiltInRegistries.BLOCK.get(id)
                val drops = Block.getDrops(block.defaultBlockState(), helper.level, helper.absolutePos(BlockPos(1, 1, 1)), null)
                check(drops.size == 1 && drops.single().`is`(block.asItem()) && drops.single().count == 1) { "$id must drop exactly itself" }
            }
        }
        check(BuiltInRegistries.ITEM.containsKey(ResourceLocation("peripheralworks", "wired_network_p2p_tunnel")) == ae2Present)
        val baseTable = ResourceLocation("peripheralworks", "blocks/item_pedestal")
        check(loot.getKeys(LootDataType.TABLE).contains(baseTable) && loot.getLootTable(baseTable) !== LootTable.EMPTY) { "Base loot tables must remain available" }
        println("Optional AE2 registrations, recipes and loot verified: AE2=$ae2Present")
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun constrainedPedestalStorage(helper: GameTestHelper) {
        val platform = ModPlatform.baseInnerPlatform
        val stone = net.minecraft.world.item.Items.STONE
        val dirt = net.minecraft.world.item.Items.DIRT
        var changes = 0
        val (saved, storage) = platform.createSlottedItemStorage(1, 1, { changes++ }, 1) { it.`is`(stone) }
        check(storage.getLimit(0) == 1L)
        check(storage.store(ItemStack(dirt, 5), false).count == 5)
        check(storage.get(0).isEmpty && changes == 0)
        check(storage.store(ItemStack(stone, 5), true).count == 4)
        check(storage.get(0).isEmpty && changes == 0)
        check(storage.store(ItemStack(stone, 5), false).count == 4)
        check(storage.get(0).count == 1 && changes > 0)
        val original = storage.get(0).copy()
        val beforeReplace = changes
        check(!platform.replaceSlottedItem(saved, 0, ItemStack(dirt), ItemStack(stone)))
        check(!platform.replaceSlottedItem(saved, 0, original, ItemStack(stone, 2)))
        check(!platform.replaceSlottedItem(saved, 0, original, ItemStack(dirt)))
        check(ItemStack.matches(storage.get(0), original) && changes == beforeReplace)
        val tagged = original.copy().apply { orCreateTag.putString("variant", "replacement") }
        check(platform.replaceSlottedItem(saved, 0, original, tagged))
        check(ItemStack.matches(storage.get(0), tagged) && changes > beforeReplace)
        val (loaded, loadedStorage) = platform.createSlottedItemStorage(1, 1, {}, 1) { it.`is`(stone) }
        loaded.load(saved.save())
        check(ItemStack.matches(loadedStorage.get(0), tagged))
        val (_, ordinary) = platform.createSlottedItemStorage(1, 1, {})
        check(ordinary.store(ItemStack(stone, 64), false).isEmpty)
        check(ordinary.get(0).count == 64)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun integrationConfigurationsAreDiscoveredAndFiltered(helper: GameTestHelper) {
        val discovered = IntegrationConfigurationDiscovery.discover { true }
        check(
            discovered.associate { it.modID to it.name } ==
                mapOf(
                    "additionallanterns" to "additionallanterns",
                    "ae2" to "ae2",
                    "alloy_forgery" to "alloy_forgery",
                    "ars_nouveau" to "ars_nouveau",
                    "automobility" to "automobility",
                    "create" to "create",
                    "deepresonance" to "deep_resonance",
                    "easy_villagers" to "easy_villagers",
                    "embers" to "embers",
                    "fluxnetworks" to "flux_networks",
                    "integrateddynamics" to "integrateddynamics",
                    "modern_industrialization" to "modern_industrialization",
                    "naturescompass" to "naturescompass",
                    "occultism" to "occultism",
                    "powah" to "powah",
                    "projecte" to "projecte",
                    "theurgy" to "theurgy",
                    "toms_storage" to "toms_storage",
                    "universal_shops" to "universal_shops",
                ),
        )
        val selected = IntegrationConfigurationDiscovery.discover { it == "ae2" || it == "powah" }
        check(selected.map { it.modID }.toSet() == setOf("ae2", "powah"))

        val activeIntegrations = ConfigHolder.commonSpec.values
            .get<UnmodifiableConfig>("integrations")
            ?.valueMap()
            ?.keys
            .orEmpty()
        check(activeIntegrations.all(discovered.map { it.name }.toSet()::contains))
        if ("ae2" in activeIntegrations) {
            val ae2Subscriptions = ConfigHolder.commonSpec.spec
                .get<ForgeConfigSpec.ValueSpec>("integrations.ae2.maxSubscriptions")
            check(ae2Subscriptions.default == 16)
            check(!ae2Subscriptions.test(0) && ae2Subscriptions.test(1) && ae2Subscriptions.test(Int.MAX_VALUE))
        }
        if ("powah" in activeIntegrations) {
            val powahEnergy = ConfigHolder.commonSpec.spec.get<ForgeConfigSpec.ValueSpec>("integrations.powah.enableEnergy")
            check(powahEnergy.default == true)
        }
        helper.succeed()
    }

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
    fun networkManagerDefersPeripheralSync(helper: GameTestHelper) {
        val manager = getNetworkManager(helper)
        var toggling = false

        helper.startSequence()
            .thenIdle(2)
            .thenExecute {
                manager.peripherals["monitor_0"] = BlockPos.ZERO
                toggling = manager.blockState.getValue(NetworkManager.TOGGLING)
                manager.detachPeripheral("monitor_0")
                check(manager.blockState.getValue(NetworkManager.TOGGLING) == toggling) { "Peripheral sync was not deferred" }
            }
            .thenIdle(1)
            .thenExecute {
                check(manager.blockState.getValue(NetworkManager.TOGGLING) != toggling) { "Deferred peripheral sync did not run" }
            }
            .thenSucceed()
    }

    @GameTest(template = "empty")
    fun networkManagerNodeRemovalDoesNotMutateSurvivorSynchronously(helper: GameTestHelper) {
        val survivorPos = BlockPos(1, 1, 1)
        val removedPos = BlockPos(1, 2, 1)
        helper.setBlock(survivorPos, Blocks.NETWORK_MANAGER.get())
        helper.setBlock(removedPos, Blocks.NETWORK_MANAGER.get())
        val survivor = helper.getBlockEntity(survivorPos) as NetworkManagerBlockEntity
        val absoluteRemovedPos = helper.absolutePos(removedPos)

        helper.startSequence()
            .thenIdle(5)
            .thenExecute {
                check(absoluteRemovedPos in survivor.peripherals.values) { "Network managers did not connect" }
                val toggling = survivor.blockState.getValue(NetworkManager.TOGGLING)
                helper.setBlock(removedPos, net.minecraft.world.level.block.Blocks.AIR)
                check(absoluteRemovedPos !in survivor.peripherals.values) { "Removed network manager remained attached" }
                check(survivor.blockState.getValue(NetworkManager.TOGGLING) == toggling) {
                    "Node removal synchronously mutated the surviving network manager"
                }
            }
            .thenSucceed()
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

    @GameTest(template = "empty")
    fun configuratorRecentTargetsAreUniqueBoundedAndRetained(helper: GameTestHelper) {
        val configurator = Items.ULTIMATE_CONFIGURATOR.get() as UltimateConfigurator
        val stack = ItemStack(configurator)
        val positions = (1..4).map { helper.absolutePos(BlockPos(it, 1, 1)) }
        configurator.saveActiveMode(stack, RemoteObserverMode, positions[0], helper.level)
        configurator.saveActiveMode(stack, PeripheralProxyMode, positions[1], helper.level)
        configurator.saveActiveMode(stack, NetworkManagerMode, positions[2], helper.level)
        configurator.saveActiveMode(stack, RemoteObserverMode, positions[3], helper.level)
        check(configurator.getRecentTargets(stack).map { it.pos } == listOf(positions[3], positions[2], positions[1]))

        configurator.saveActiveMode(stack, PeripheralProxyMode, positions[2], helper.level)
        val recent = configurator.getRecentTargets(stack)
        check(recent.map { it.pos } == listOf(positions[2], positions[3], positions[1]))
        check(recent.first().modeID == PeripheralProxyMode.modeID)
        check(configurator.toggleFavorite(stack, recent.first()) == UltimateConfigurator.FavoriteResult.ADDED)
        configurator.clearActiveMode(stack)
        check(configurator.getActiveMode(stack) == null)
        check(configurator.getTargetHistory(stack) == recent)
        check(configurator.getRecentTargets(stack) == recent.drop(1))
        check(configurator.getFavoriteTargets(stack).single().matches(recent.first()))
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun configuratorFavoritesAreOrderedBoundedAndNamed(helper: GameTestHelper) {
        val configurator = Items.ULTIMATE_CONFIGURATOR.get() as UltimateConfigurator
        val stack = ItemStack(configurator)
        repeat(UltimateConfigurator.MAX_FAVORITE_TARGETS) { index ->
            configurator.saveActiveMode(stack, RemoteObserverMode, helper.absolutePos(BlockPos(index + 1, 1, 1)), helper.level)
            check(configurator.toggleFavorite(stack, configurator.getRecentTargets(stack).first()) == UltimateConfigurator.FavoriteResult.ADDED)
        }
        val favorites = configurator.getFavoriteTargets(stack)
        check(favorites.size == UltimateConfigurator.MAX_FAVORITE_TARGETS)
        check(favorites.first().pos == helper.absolutePos(BlockPos(16, 1, 1)))
        check(configurator.getTargetHistory(stack).map { it.pos } == (16 downTo 1).map { helper.absolutePos(BlockPos(it, 1, 1)) })

        configurator.saveActiveMode(stack, RemoteObserverMode, helper.absolutePos(BlockPos(1, 1, 1)), helper.level)
        check(configurator.getTargetHistory(stack).first().pos == helper.absolutePos(BlockPos(1, 1, 1)))

        configurator.saveActiveMode(stack, RemoteObserverMode, helper.absolutePos(BlockPos(17, 1, 1)), helper.level)
        check(configurator.toggleFavorite(stack, configurator.getRecentTargets(stack).first()) == UltimateConfigurator.FavoriteResult.LIMIT)
        check(configurator.getTargetHistory(stack).size == UltimateConfigurator.MAX_FAVORITE_TARGETS + 1)
        val target = favorites.first()
        val validName = "n".repeat(ConfiguratorTarget.MAX_NAME_LENGTH)
        check(configurator.renameFavorite(stack, target, validName))
        check(!configurator.renameFavorite(stack, target, validName + "n"))
        check(configurator.getFavoriteTargets(stack).first().name == validName)
        check(configurator.renameFavorite(stack, target, ""))
        check(configurator.getFavoriteTargets(stack).first().name == null)
        check(configurator.setFavoriteColor(stack, target, 0x123456, true))
        check(configurator.setFavoriteColor(stack, target, 0x654321, false))
        check(configurator.getFavoriteTargets(stack).first().textColor == 0x123456)
        check(configurator.getFavoriteTargets(stack).first().boxColor == 0x654321)
        check(configurator.getFavoriteTextStyle(stack) == TextStyle.REGULAR)
        check(configurator.getFavoriteBoxStyle(stack) == BoxStyle.OUTLINE)
        check(configurator.setSettings(stack, "Field Kit", TextStyle.BOLD, BoxStyle.FLARE))
        check(stack.hoverName.string == "Field Kit")
        check(configurator.getFavoriteTextStyle(stack) == TextStyle.BOLD)
        check(configurator.getFavoriteBoxStyle(stack) == BoxStyle.FLARE)
        check(configurator.toggleFavorite(stack, target) == UltimateConfigurator.FavoriteResult.REMOVED)
        check(configurator.getFavoriteTargets(stack).size == UltimateConfigurator.MAX_FAVORITE_TARGETS - 1)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun configuratorIgnoresMalformedAndExcessTargets(helper: GameTestHelper) {
        val configurator = Items.ULTIMATE_CONFIGURATOR.get() as UltimateConfigurator
        val stack = ItemStack(configurator)
        val dimension = helper.level.dimension().location()
        val valid = ConfiguratorTarget(RemoteObserverMode.modeID, dimension, BlockPos(1, 2, 3))
        stack.orCreateTag.put(
            UltimateConfigurator.RECENT_TARGETS,
            ListTag().apply {
                add(CompoundTag())
                add(valid.toNBT())
                add(valid.toNBT())
                add(ConfiguratorTarget(RemoteObserverMode.modeID, dimension, BlockPos(2, 2, 3)).toNBT().apply { putString("name", "x".repeat(65)) })
                add(ConfiguratorTarget(RemoteObserverMode.modeID, dimension, BlockPos(3, 2, 3)).toNBT())
                add(ConfiguratorTarget(RemoteObserverMode.modeID, dimension, BlockPos(4, 2, 3)).toNBT())
            },
        )
        val recent = configurator.getRecentTargets(stack)
        check(recent.map { it.pos } == listOf(BlockPos(1, 2, 3), BlockPos(2, 2, 3), BlockPos(3, 2, 3)))
        check(recent[1].name == null)

        stack.orCreateTag.put(
            UltimateConfigurator.FAVORITE_TARGETS,
            ListTag().apply {
                repeat(20) { add(ConfiguratorTarget(RemoteObserverMode.modeID, dimension, BlockPos(it, 1, 1)).toNBT()) }
            },
        )
        check(configurator.getFavoriteTargets(stack).size == UltimateConfigurator.MAX_FAVORITE_TARGETS)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun configuratorSelectionValidatesStoredAndWorldTargets(helper: GameTestHelper) {
        val configurator = Items.ULTIMATE_CONFIGURATOR.get() as UltimateConfigurator
        val stack = ItemStack(configurator)
        val relativePos = BlockPos(1, 1, 1)
        val pos = helper.absolutePos(relativePos)
        helper.setBlock(relativePos, Blocks.PERIPHERAL_PROXY.get())
        configurator.saveActiveMode(stack, PeripheralProxyMode, pos, helper.level)
        configurator.clearActiveMode(stack)
        val target = configurator.getRecentTargets(stack).single()
        check(configurator.selectTarget(stack, helper.level, target) == UltimateConfigurator.SelectionResult.SUCCESS)
        check(configurator.getActiveMode(stack)?.second == pos)

        configurator.clearActiveMode(stack)
        helper.setBlock(relativePos, Blocks.REMOTE_OBSERVER.get())
        check(configurator.selectTarget(stack, helper.level, target) == UltimateConfigurator.SelectionResult.UNAVAILABLE)
        check(configurator.getActiveMode(stack) == null)
        check(configurator.selectTarget(stack, helper.level, target.copy(pos = pos.offset(1, 0, 0))) == UltimateConfigurator.SelectionResult.REJECTED)

        val otherDimension = target.copy(dimensionID = ResourceLocation("minecraft", "the_nether"))
        stack.orCreateTag.put(UltimateConfigurator.RECENT_TARGETS, ListTag().apply { add(otherDimension.toNBT()) })
        check(configurator.selectTarget(stack, helper.level, otherDimension) == UltimateConfigurator.SelectionResult.UNAVAILABLE)

        val unloaded = target.copy(pos = BlockPos(30_000_000, 1, 30_000_000))
        stack.orCreateTag.put(UltimateConfigurator.RECENT_TARGETS, ListTag().apply { add(unloaded.toNBT()) })
        check(!helper.level.isLoaded(unloaded.pos))
        check(configurator.selectTarget(stack, helper.level, unloaded) == UltimateConfigurator.SelectionResult.UNAVAILABLE)
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
