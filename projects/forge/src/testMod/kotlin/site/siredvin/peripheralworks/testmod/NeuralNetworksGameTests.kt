package site.siredvin.peripheralworks.testmod

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import dan200.computercraft.api.detail.VanillaDetailRegistries
import dan200.computercraft.api.lua.LuaException
import dev.shadowsoffire.hostilenetworks.Hostile
import dev.shadowsoffire.hostilenetworks.data.DataModel
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry
import dev.shadowsoffire.hostilenetworks.data.ModelTier
import dev.shadowsoffire.hostilenetworks.item.DataModelItem
import dev.shadowsoffire.placebo.reload.DynamicRegistry
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.InactiveProfiler
import net.minecraft.util.profiling.ProfilerFiller
import site.siredvin.peripheralworks.integrations.hostilenetworks.NeuralModels
import site.siredvin.testiarium.api.TestGroup

@TestGroup("neural")
class NeuralNetworksGameTests {
    @TestGroup("neural-disabled")
    @GameTest(template = "empty")
    fun disabledIntegration(helper: GameTestHelper) {
        check(!site.siredvin.peripheralworks.common.configuration.integration.HostileNetworksConfiguration.enabled)
        val pos = net.minecraft.core.BlockPos(1, 1, 1)
        helper.setBlock(pos, Hostile.Blocks.LOOT_FABRICATOR.get())
        val plugins = site.siredvin.peripheralworks.computercraft.ComputerCraftProxy.collectPlugins(helper.level, helper.absolutePos(pos), net.minecraft.core.Direction.NORTH)
        check(plugins.isNotEmpty() && "loot_fabricator" !in plugins)
        check(!NeuralTestSupport.checkDetail(NeuralTestSupport.single(false)).containsKey("dataModel"))
        helper.succeed()
    }

    @GameTest(template = "peripheralworksgametests.ae2_configurable_objects", timeoutTicks = 2400, batch = "neural")
    fun luaAndProduction(helper: GameTestHelper) = NeuralTestSupport.runLua(helper, Hostile.Blocks.LOOT_FABRICATOR.get(), "hnn")

    @GameTest(template = "empty", batch = "neural")
    fun itemDetails(helper: GameTestHelper) {
        val model = NeuralModels.resolve("minecraft:zombie").get()
        for (tier in ModelTier.values()) {
            val stack = NeuralTestSupport.single(false)
            val amount = model.getTierData(tier)
            DataModelItem.setData(stack, amount)
            val extension = NeuralTestSupport.checkDetail(stack)["dataModel"] as Map<*, *>
            val progress = extension["progression"] as Map<*, *>
            check(progress["rank"] == tier.name.lowercase() && progress["data"] == amount)
            check(progress["tierData"] == amount && progress["dataPerKill"] == model.getDataPerKill(tier))
            check(progress["simulationCost"] == model.simCost())
            if (tier != ModelTier.SELF_AWARE) check((progress["remainingData"] as Number).toInt() == model.getTierData(tier.next()) - amount)
        }
        val valid = NeuralTestSupport.single(false)
        val expectedName = valid.hoverName.string
        check(NeuralTestSupport.checkDetail(valid)["displayName"] == expectedName)
        for (id in listOf("BAD!", "missing:model", "")) {
            val broken = valid.copy().apply { getOrCreateTagElement("data_model").putString("id", id) }
            check(!NeuralTestSupport.checkDetail(broken).containsKey("dataModel"))
        }
        val observer = dan200.computercraft.api.detail.DetailProvider<net.minecraft.world.item.ItemStack> { data, stack ->
            if (stack.item == Hostile.Items.DATA_MODEL.get()) data["neuralTestCoexistence"] = true
        }
        VanillaDetailRegistries.ITEM_STACK.addProvider(observer)
        check(NeuralTestSupport.checkDetail(valid)["neuralTestCoexistence"] == true)
        helper.succeed()
    }

    @GameTest(template = "empty", batch = "neural-reload")
    fun reloadedModels(helper: GameTestHelper) {
        val registry = DataModelRegistry.INSTANCE
        val original = registry.keys.associateWith { DataModel.CODEC.encodeStart(JsonOps.INSTANCE, registry.getValue(it)).getOrThrow(false) { error(it) } }
        val apply = DynamicRegistry::class.java.getDeclaredMethod("apply", Map::class.java, ResourceManager::class.java, ProfilerFiller::class.java).apply { isAccessible = true }
        fun reload(values: Map<ResourceLocation, JsonElement>) {
            apply.invoke(registry, values, helper.level.server.resourceManager, InactiveProfiler.INSTANCE)
        }
        fun fails(action: () -> Unit) {
            try {
                action()
                error("Expected ambiguous model error")
            } catch (_: LuaException) { }
        }
        try {
            val zombieId = checkNotNull(registry.getKey(NeuralModels.resolve("minecraft:zombie").get()))
            val custom = original.getValue(zombieId).deepCopy().asJsonObject
            custom.addProperty("sim_cost", 321)
            custom.add("tier_data", JsonArray().apply { listOf(10, 20, 30, 40).forEach(::add) })
            custom.add(
                "fabricator_drops",
                JsonArray().apply {
                    add(
                        JsonObject().apply {
                            addProperty("item", "minecraft:iron_ingot")
                            addProperty("count", 3)
                        },
                    )
                    add(
                        JsonObject().apply {
                            addProperty("item", "minecraft:iron_ingot")
                            addProperty("count", 7)
                        },
                    )
                },
            )
            reload(original + (zombieId to custom))
            val plugin = site.siredvin.peripheralworks.integrations.hostilenetworks.LootFabricatorPlugin("test", { 3 }, { _, _ -> })
            check(plugin.getLoot("minecraft:zombie").map { it["count"] } == listOf(3, 7))
            check(plugin.getSelectedLoot("minecraft:zombie") == null)
            check(NeuralModels.resolve("minecraft:husk").id == zombieId)
            check(NeuralModels.resolve("minecraft:zombie").get().simCost() == 321)
            val customProgress = (NeuralTestSupport.checkDetail(NeuralTestSupport.single(false))["dataModel"] as Map<*, *>)["progression"] as Map<*, *>
            check(customProgress["tierData"] == 10 && customProgress["nextTierData"] == 20 && customProgress["simulationCost"] == 321)
            val empty = custom.deepCopy().apply { add("fabricator_drops", JsonArray()) }
            reload(original + (zombieId to empty))
            check(plugin.getLoot("minecraft:zombie").isEmpty())
            val duplicateId = ResourceLocation.fromNamespaceAndPath("peripheralworks_testmod", "duplicate_zombie")
            // Native registration rejects duplicate primary types; overlapping subtypes are allowed.
            val duplicate = custom.deepCopy().apply {
                addProperty("entity", "minecraft:boat")
                addProperty("sim_cost", 322)
            }
            reload(original + (zombieId to custom) + (duplicateId to duplicate))
            check(NeuralModels.resolve("minecraft:zombie").id == zombieId)
            fails { NeuralModels.resolve("minecraft:husk") }
            check("minecraft:zombie" in NeuralModels.entities())
            val primaryHusk = duplicate.deepCopy().apply {
                addProperty("entity", "minecraft:husk")
                add("variants", JsonArray())
            }
            reload(original + (duplicateId to primaryHusk))
            check(NeuralModels.resolve("minecraft:husk").id == duplicateId)
            reload(original - zombieId)
            fails { NeuralModels.resolve("minecraft:zombie") }
        } finally {
            reload(original)
        }
        check(NeuralModels.resolve("minecraft:zombie").get().fabDrops().isNotEmpty())
        helper.succeed()
    }
}
