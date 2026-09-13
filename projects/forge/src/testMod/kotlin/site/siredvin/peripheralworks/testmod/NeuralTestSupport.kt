package site.siredvin.peripheralworks.testmod

import dan200.computercraft.api.detail.VanillaDetailRegistries
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity
import dev.shadowsoffire.hostilenetworks.Hostile
import dev.shadowsoffire.hostilenetworks.data.ModelTierRegistry
import dev.shadowsoffire.hostilenetworks.item.DataModelItem
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.neoforged.neoforge.capabilities.Capabilities
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.integrations.hostilenetworks.LootFabricatorPlugin
import site.siredvin.peripheralworks.integrations.hostilenetworks.NeuralModels
import site.siredvin.testiarium.api.thenExecuteFailFast
import site.siredvin.testiarium.cct.CctComputerState

object NeuralTestSupport {
    fun single(maximum: Boolean): ItemStack = ItemStack(Hostile.Items.DATA_MODEL.value()).apply {
        val model = NeuralModels.resolve("minecraft:zombie").get()
        DataModelItem.setStoredModel(this, model)
        DataModelItem.setData(this, model.getRequiredData(if (maximum) ModelTierRegistry.getMaxTier() else ModelTierRegistry.getSortedTiers()[1]))
        DataModelItem.setIters(this, 7)
    }

    fun runLua(helper: GameTestHelper, block: Block, labelSuffix: String, combined: List<ItemStack>? = null) {
        val computer = (0..4).flatMap { x -> (0..3).flatMap { y -> (0..4).map { z -> helper.level.getBlockEntity(helper.absolutePos(BlockPos(x, y, z))) } } }.filterIsInstance<ComputerBlockEntity>().single()
        val label = "peripheralworksgametests.$labelSuffix"
        computer.setLabel(label)
        val pos = computer.blockPos.relative(Direction.NORTH)
        helper.level.setBlockAndUpdate(pos, block.defaultBlockState())
        val tile = checkNotNull(helper.level.getBlockEntity(pos))
        val plugin = ComputerCraftProxy.collectPlugins(helper.level, pos, Direction.SOUTH).values.filterIsInstance<LootFabricatorPlugin>().single()
        val inventory = checkNotNull(helper.level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.SOUTH))
        val energy = checkNotNull(helper.level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, Direction.SOUTH))
        val chestPos = computer.blockPos.relative(Direction.WEST)
        helper.level.setBlockAndUpdate(chestPos, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState())
        val chest = helper.level.getBlockEntity(chestPos) as ChestBlockEntity
        val normal = listOf(single(false), single(true))
        val samples = normal + (combined ?: normal.map(ItemStack::copy)) + listOf(
            ItemStack(Hostile.Items.BLANK_DATA_MODEL.value()),
            single(false).apply {
                remove(Hostile.Components.DATA_MODEL)
                set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Malformed model fixture"))
            },
            single(false).apply { DataModelItem.setData(this, -1) },
            ItemStack(Items.DIRT),
        )
        samples.forEachIndexed(chest::setItem)
        val before = samples.map(ItemStack::copy)
        val sourceModel = NeuralModels.resolve("minecraft:zombie").get()
        val prediction = sourceModel.predictionDrop
        val expected = sourceModel.fabDrops()[1].copy()
        val initialEnergy = energy.maxEnergyStored
        var saved = CompoundTag()
        helper.startSequence().thenIdle(5)
            .thenExecute { computer.createServerComputer().turnOn() }
            .thenWaitUntil {
                val state = CctComputerState.get(label) ?: throw GameTestAssertException("Computer has not started")
                if (!state.isDone(CctComputerState.DONE)) throw GameTestAssertException("Lua has not completed")
                state.check(CctComputerState.DONE)
            }
            .thenExecuteFailFast {
                before.forEachIndexed { index, stack -> check(ItemStack.matches(stack, chest.getItem(index))) { "Item query mutated NBT in slot $index" } }
                check(plugin.getSelectedLoot("minecraft:zombie") == 2)
                check(energy.energyStored == 0 && (0 until inventory.slots).all { inventory.getStackInSlot(it).isEmpty })
                fun select(entity: String, index: Int) = plugin.setSelectedLoot(entity, dan200.computercraft.api.lua.ObjectArguments(entity, index))
                select("minecraft:husk", 1)
                check(plugin.getSelectedLoot("minecraft:zombie") == 1)
                select("minecraft:creeper", 1)
                select("minecraft:zombie", 2)
                check(plugin.getSelectedLoot("minecraft:creeper") == 1)
                val otherPos = pos.relative(Direction.EAST)
                helper.level.setBlockAndUpdate(otherPos, block.defaultBlockState())
                val other = ComputerCraftProxy.collectPlugins(helper.level, otherPos, Direction.SOUTH).values.filterIsInstance<LootFabricatorPlugin>().single()
                check(other.getSelectedLoot("minecraft:zombie") == null)
                val registries = helper.level.registryAccess()
                saved = tile.saveWithoutMetadata(registries)
                val loaded = checkNotNull(tile.type.create(pos, tile.blockState))
                loaded.loadWithComponents(saved, registries)
                check(loaded.saveWithoutMetadata(registries).getCompound("saved_selections") == saved.getCompound("saved_selections"))
                check(tile.getUpdateTag(registries).getCompound("saved_selections") == saved.getCompound("saved_selections"))
                tile.loadWithComponents(saved, registries)
                check(plugin.getSelectedLoot("minecraft:zombie") == 2)
                check(inventory.insertItem(0, prediction.copy(), false).isEmpty)
                energy.receiveEnergy(initialEnergy, false)
            }
            .thenIdle(2)
            .thenExecuteFailFast {
                plugin.setSelectedLoot("minecraft:zombie", dan200.computercraft.api.lua.ObjectArguments("minecraft:zombie", 1))
            }
            .thenIdle(2)
            .thenExecuteFailFast {
                plugin.setSelectedLoot("minecraft:zombie", dan200.computercraft.api.lua.ObjectArguments("minecraft:zombie", 2))
            }
            .thenWaitUntil {
                if ((1 until inventory.slots).all { inventory.getStackInSlot(it).isEmpty }) throw GameTestAssertException("Fabricator has not produced loot")
            }
            .thenExecuteFailFast {
                val outputs = (1 until inventory.slots).map(inventory::getStackInSlot).filterNot(ItemStack::isEmpty)
                check(outputs.sumOf { it.count } == expected.count && outputs.all { ItemStack.isSameItemSameComponents(it, expected) })
                check(inventory.getStackInSlot(0).isEmpty && energy.energyStored < initialEnergy)
                check(tile.saveWithoutMetadata(helper.level.registryAccess()).getCompound("saved_selections") == saved.getCompound("saved_selections"))
            }.thenSucceed()
    }

    fun checkDetail(stack: ItemStack): Map<*, *> {
        val before = stack.copy()
        val result = VanillaDetailRegistries.ITEM_STACK.getDetails(stack)
        check(ItemStack.matches(stack, before)) { "Detail query mutated item" }
        check(!VanillaDetailRegistries.ITEM_STACK.getBasicDetails(stack).containsKey("dataModel"))
        val model = result["dataModel"] as? Map<*, *>
        if (model != null) {
            val identities = model["models"] as List<*>
            for (identity in identities) {
                val fields = identity as Map<*, *>
                check(fields.keys == setOf("modelId", "entityId")) { "Unexpected model identity fields" }
                check(fields["entityId"] is String && fields["modelId"] is String)
            }
        }
        return result
    }
}
