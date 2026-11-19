// Magic-tome upgrades keep CC's deprecated item-less data API.
@file:Suppress("DEPRECATION")

package site.siredvin.peripheralworks.testmod

import com.hollingsworth.arsnouveau.api.spell.SpellCaster
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeData
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import site.siredvin.testiarium.api.TestGroup

@TestGroup("peripheralworks")
class ArsMagicTomeGameTests {
    @GameTest(template = "empty")
    fun magicTomeDefaultItemsAreSuitable(helper: GameTestHelper) {
        val upgrades = helper.level.registryAccess().lookupOrThrow(IPocketUpgrade.REGISTRY)
        listOf("novice", "apprentice", "archmage", "caster").forEach { tier ->
            val holder = upgrades.getOrThrow(ResourceKey.create(IPocketUpgrade.REGISTRY, ResourceLocation.fromNamespaceAndPath("peripheralworks", "${tier}_magic_tome")))
            val upgrade = holder.value()
            // CC:Tweaked throws here when the crafting item is not suitable.
            UpgradeData.ofDefault(holder)
            check(upgrade.isItemSuitable(upgrade.craftingItem)) { "$tier magic tome rejects its own crafting item" }
            val scribed = upgrade.craftingItem.copy()
            scribed.set(DataComponentRegistry.SPELL_CASTER.get(), SpellCaster(1, "flavor", false, "name", 3))
            check(upgrade.isItemSuitable(scribed)) { "$tier magic tome rejects a book with its own spells" }
            check(!upgrade.isItemSuitable(ItemStack(Items.STICK))) { "$tier magic tome accepts a stick" }
        }
        helper.succeed()
    }
}
