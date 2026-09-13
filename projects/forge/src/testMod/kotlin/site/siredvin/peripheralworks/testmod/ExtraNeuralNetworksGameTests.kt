package site.siredvin.peripheralworks.testmod

import net.lmor.extrahnn.ExtraHostile
import net.lmor.extrahnn.data.ExtraCachedModel
import net.lmor.extrahnn.data.ExtraModelTier
import net.lmor.extrahnn.item.ExtraDataModelItem
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.integrations.hostilenetworks.NeuralModels
import site.siredvin.testiarium.api.TestGroup

@TestGroup("neural")
class ExtraNeuralNetworksGameTests {
    @TestGroup("neural-disabled")
    @GameTest(template = "empty")
    fun disabledIntegration(helper: GameTestHelper) {
        check(!site.siredvin.peripheralworks.common.configuration.integration.ExtraHNNConfiguration.enabled)
        val pos = net.minecraft.core.BlockPos(1, 1, 1)
        helper.setBlock(pos, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V1.get())
        val plugins = site.siredvin.peripheralworks.computercraft.ComputerCraftProxy.collectPlugins(helper.level, helper.absolutePos(pos), net.minecraft.core.Direction.NORTH)
        check(plugins.isNotEmpty() && "ultimate_loot_fabricator" !in plugins)
        check(!NeuralTestSupport.checkDetail(combined(ExtraModelTier.INTELLIGENT)).containsKey("dataModel"))
        helper.succeed()
    }

    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400, batch = "neural")
    fun ultimateV1(helper: GameTestHelper) = NeuralTestSupport.runLua(helper, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V1.get(), "extra_hnn_v1", samples())

    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400, batch = "neural")
    fun ultimateV2(helper: GameTestHelper) = NeuralTestSupport.runLua(helper, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V2.get(), "extra_hnn_v2", samples())

    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400, batch = "neural")
    fun ultimateV3(helper: GameTestHelper) = NeuralTestSupport.runLua(helper, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V3.get(), "extra_hnn_v3", samples())

    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400, batch = "neural")
    fun ultimateV4(helper: GameTestHelper) = NeuralTestSupport.runLua(helper, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V4.get(), "extra_hnn_v4", samples())

    @GameTest(template = "empty", batch = "neural")
    fun combinedDetailsAndMalformedNames(helper: GameTestHelper) {
        for (tier in ExtraModelTier.values()) {
            val stack = combined(tier)
            val nativeName = stack.hoverName.string
            val result = NeuralTestSupport.checkDetail(stack)
            check(result["displayName"] == nativeName)
            val detail = result["dataModel"] as Map<*, *>
            check(detail["kind"] == "combined")
            val identities = detail["models"] as List<*>
            check(identities.size == 4 && identities[0] == identities[2])
            val progress = detail["progression"] as Map<*, *>
            check(progress["rank"] == tier.name.lowercase())
            check(progress["data"] == tier.data().requiredData())
            check(progress["dataPerKill"] == tier.data().dataPerKill())
            check(progress["simulationCost"] == ExtraCachedModel(stack, 0).simCost())
            check(progress["maxRank"] == (tier == ExtraModelTier.OMNIPOTENT))
            if (tier == ExtraModelTier.OMNIPOTENT) check(progress["nextTierData"] == null && progress["remainingData"] == null)
        }
        for (ids in listOf(emptyList(), listOf("INVALID!"), listOf("hostilenetworks:zombie", "hostilenetworks:skeleton"), List(4) { "missing:model" })) {
            val malformed = combined(ExtraModelTier.AUTONOMOUS).apply {
                getOrCreateTagElement("data_model").put("ids", ListTag().apply { ids.forEach { add(StringTag.valueOf(it)) } })
            }
            val result = NeuralTestSupport.checkDetail(malformed)
            check(!result.containsKey("dataModel") && result["displayName"] is String)
            if (ids == List(4) { "missing:model" }) {
                check(result["displayName"] == malformed.item.getName(malformed).string) { "Safe native broken-model name was replaced" }
            }
            malformed.setHoverName(net.minecraft.network.chat.Component.literal("Custom model name"))
            check(NeuralTestSupport.checkDetail(malformed)["displayName"] == "Custom model name")
        }
        helper.succeed()
    }

    companion object {
        fun combined(tier: ExtraModelTier): ItemStack = ItemStack(ExtraHostile.Items.EXTRA_DATA_MODEL.get()).apply {
            ExtraDataModelItem.setStoredModels(this, listOf("minecraft:zombie", "minecraft:skeleton", "minecraft:zombie", "minecraft:creeper").map { NeuralModels.resolve(it).get() })
            ExtraDataModelItem.setData(this, tier.data().requiredData())
            ExtraDataModelItem.setIters(this, 7)
        }
        fun samples(): List<ItemStack> = listOf(combined(ExtraModelTier.INTELLIGENT), combined(ExtraModelTier.OMNIPOTENT))
    }
}
