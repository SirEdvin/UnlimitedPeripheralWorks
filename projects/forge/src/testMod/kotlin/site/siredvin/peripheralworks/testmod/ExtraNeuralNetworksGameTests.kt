package site.siredvin.peripheralworks.testmod

import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry
import net.lmor.extrahnn.ExtraHostile
import net.lmor.extrahnn.common.item.ExtraDataModelItem
import net.lmor.extrahnn.data.ExtraDataModelInstance
import net.lmor.extrahnn.data.ExtraModelTier
import net.minecraft.core.component.DataComponents
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
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
        helper.setBlock(pos, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V1.value())
        val plugins = site.siredvin.peripheralworks.computercraft.ComputerCraftProxy.collectPlugins(helper.level, helper.absolutePos(pos), net.minecraft.core.Direction.NORTH)
        check(plugins.isNotEmpty() && "ultimate_loot_fabricator" !in plugins)
        check(!NeuralTestSupport.checkDetail(combined(ExtraModelTier.INTELLIGENT)).containsKey("dataModel"))
        helper.succeed()
    }

    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400, batch = "neural")
    fun ultimateV1(helper: GameTestHelper) = NeuralTestSupport.runLua(helper, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V1.value(), "extra_hnn_v1", samples())

    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400, batch = "neural")
    fun ultimateV2(helper: GameTestHelper) = NeuralTestSupport.runLua(helper, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V2.value(), "extra_hnn_v2", samples())

    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400, batch = "neural")
    fun ultimateV3(helper: GameTestHelper) = NeuralTestSupport.runLua(helper, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V3.value(), "extra_hnn_v3", samples())

    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400, batch = "neural")
    fun ultimateV4(helper: GameTestHelper) = NeuralTestSupport.runLua(helper, ExtraHostile.Blocks.ULTIMATE_LOOT_FABRICATOR_V4.value(), "extra_hnn_v4", samples())

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
            check(progress["simulationCost"] == ExtraDataModelInstance(stack).simCost())
            check(progress["maxRank"] == (tier == ExtraModelTier.OMNIPOTENT))
            if (tier == ExtraModelTier.OMNIPOTENT) check(progress["nextTierData"] == null && progress["remainingData"] == null)
        }
        for (ids in listOf(emptyList(), listOf("missing:model"), listOf("hostilenetworks:zombie", "hostilenetworks:skeleton"), List(4) { "missing:model" })) {
            val malformed = combined(ExtraModelTier.AUTONOMOUS).apply {
                ExtraDataModelItem.setStoredModels(this, ids.map { DataModelRegistry.INSTANCE.holder(ResourceLocation.parse(it)) })
            }
            if (ids.isEmpty()) {
                val before = malformed.copy()
                val failure = runCatching { NeuralTestSupport.checkDetail(malformed) }.exceptionOrNull()
                check(failure is IndexOutOfBoundsException) { "Upstream name failures must remain unhandled" }
                check(ItemStack.matches(malformed, before))
            }
            if (ids == List(4) { "missing:model" }) {
                val result = NeuralTestSupport.checkDetail(malformed)
                check(result["displayName"] == malformed.item.getName(malformed).string) { "Safe native broken-model name was replaced" }
            }
            // Custom names let the real detail registry reach our provider despite upstream name bugs.
            malformed.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Custom model name"))
            val result = NeuralTestSupport.checkDetail(malformed)
            check(!result.containsKey("dataModel") && result["displayName"] == "Custom model name")
        }
        helper.succeed()
    }

    companion object {
        fun combined(tier: ExtraModelTier): ItemStack = ItemStack(ExtraHostile.Items.EXTRA_DATA_MODEL.value()).apply {
            ExtraDataModelItem.setStoredModels(this, listOf("minecraft:zombie", "minecraft:skeleton", "minecraft:zombie", "minecraft:creeper").map { NeuralModels.resolve(it) })
            ExtraDataModelItem.setData(this, tier.data().requiredData())
            ExtraDataModelItem.setIters(this, 7)
        }
        fun samples(): List<ItemStack> = listOf(combined(ExtraModelTier.INTELLIGENT), combined(ExtraModelTier.OMNIPOTENT))
    }
}
