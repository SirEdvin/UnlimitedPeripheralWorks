package site.siredvin.peripheralworks.testmod

import com.google.gson.JsonParser
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.ICondition
import net.minecraftforge.fml.ModList
import site.siredvin.testiarium.api.TestGroup

@TestGroup("peripheralworks")
class ForgeRecipeConditionsGameTests {
    @Suppress("DEPRECATION")
    @GameTest(template = "empty")
    fun optionalAE2Recipes(helper: GameTestHelper) {
        val installed = ModList.get().isLoaded("ae2")
        listOf("ae2_pattern_pedestal", "me_network_peripheral").forEach { name ->
            val id = ResourceLocation("peripheralworks", name)
            val resource = helper.level.server.resourceManager.getResource(ResourceLocation("peripheralworks", "recipes/$name.json")).orElseThrow()
            val json = resource.openAsReader().use { JsonParser.parseReader(it).asJsonObject }
            check(json.has("conditions") && !json.has("forge:conditions")) { "$id must use Forge's recipe condition field" }
            check(CraftingHelper.processConditions(json, "conditions", ICondition.IContext.EMPTY) == installed) { "$id was not gated by AE2 presence" }
            check(helper.level.recipeManager.byKey(id).isPresent == installed) { "$id had incorrect recipe-manager presence" }
        }
        helper.succeed()
    }
}
