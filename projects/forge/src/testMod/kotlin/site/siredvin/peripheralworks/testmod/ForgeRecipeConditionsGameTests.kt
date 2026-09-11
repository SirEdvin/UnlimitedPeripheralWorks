package site.siredvin.peripheralworks.testmod

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.ModList
import net.neoforged.neoforge.common.conditions.ICondition
import site.siredvin.testiarium.api.TestGroup

@TestGroup("peripheralworks")
class ForgeRecipeConditionsGameTests {
    @Suppress("DEPRECATION")
    @GameTest(template = "empty")
    fun optionalAE2Recipes(helper: GameTestHelper) {
        val installed = ModList.get().isLoaded("ae2")
        listOf("ae2_pattern_pedestal", "me_network_peripheral").forEach { name ->
            val id = ResourceLocation.fromNamespaceAndPath("peripheralworks", name)
            val resource = helper.level.server.resourceManager.getResource(ResourceLocation.fromNamespaceAndPath("peripheralworks", "recipe/$name.json")).orElseThrow()
            val json = resource.openAsReader().use { JsonParser.parseReader(it).asJsonObject }
            check(json.has("neoforge:conditions") && !json.has("forge:conditions")) { "$id must use NeoForge's recipe condition field" }
            check(ICondition.conditionsMatched(JsonOps.INSTANCE, json) == installed) { "$id was not gated by AE2 presence" }
            check(helper.level.recipeManager.byKey(id).isPresent == installed) { "$id had incorrect recipe-manager presence" }
        }
        helper.succeed()
    }
}
