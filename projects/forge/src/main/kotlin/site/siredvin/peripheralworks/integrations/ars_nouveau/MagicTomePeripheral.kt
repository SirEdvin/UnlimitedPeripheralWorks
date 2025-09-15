package site.siredvin.peripheralworks.integrations.ars_nouveau

import com.hollingsworth.arsnouveau.api.spell.ISpellCaster
import com.hollingsworth.arsnouveau.common.items.CasterTome
import com.hollingsworth.arsnouveau.common.items.SpellBook
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.forge.TweakedForgeFakePlayer
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.player.FakePlayerProviderPocket
import site.siredvin.tweakium.modules.player.FakePlayerProxy

class MagicTomePeripheral(peripheralOwner: PocketPeripheralOwner, val casterTome: ItemStack, override val isEnabled: Boolean) : OwnedPeripheral<PocketPeripheralOwner>(TYPE, peripheralOwner) {
    companion object {
        const val TYPE = "magic_tome"
    }

    val spellCaster: ISpellCaster by lazy {
        if (casterTome.`is`(ItemsRegistry.CASTER_TOME.get())) {
            return@lazy CasterTome.TomeSpellCaster(casterTome)
        }
        return@lazy SpellBook.BookCaster(casterTome)
    }

    @LuaFunction(mainThread = true)
    fun getSpells(): Map<Int, MutableMap<String, Any>> = spellCaster.spells.mapKeys { it.key + 1 }.mapValues { LuaRepresentation.forSpell(it.value) }

    @LuaFunction(mainThread = true)
    fun getSelectedSlot(): Int = spellCaster.currentSlot + 1

    @LuaFunction(mainThread = true)
    fun size(): Int = spellCaster.maxSlots

    @LuaFunction(mainThread = true)
    fun getSelectedSpell(): MutableMap<String, Any> = LuaRepresentation.forSpell(spellCaster.spell)

    @LuaFunction(mainThread = true)
    fun select(slot: Int) {
        spellCaster.currentSlot = slot - 1
    }

    @LuaFunction(mainThread = true)
    fun getMana(): Map<String, Any> {
        return CapabilityRegistry.getMana(peripheralOwner.owner).map {
            return@map mapOf(
                "current" to it.currentMana,
                "max" to it.maxMana,
                "bookTier" to it.bookTier,
                "glyphBonus" to it.glyphBonus,
            )
        }.orElse(
            mapOf(
                "current" to 0,
                "max" to 0,
                "bookTier" to 0,
                "glyphBonus" to 0,
            ),
        )
    }

    @LuaFunction(mainThread = true)
    fun cast() {
        if (peripheralOwner.owner == null) {
            throw LuaException("Cannot find player for some reason, so cannot cast")
        }
        val fakePlayer = TweakedForgeFakePlayer(peripheralOwner.level!! as ServerLevel, peripheralOwner.owner!!.gameProfile, peripheralOwner.owner!!)
        val proxy = FakePlayerProxy(fakePlayer)
        FakePlayerProviderPocket.withPlayerTweaked(
            peripheralOwner.pocket,
            {
                it.fakePlayer.moveTo(it.fakePlayer.x, it.fakePlayer.y + 0.5, it.fakePlayer.z, fakePlayer.originalPlayer!!.yRot, fakePlayer.originalPlayer.xRot)
                spellCaster.castSpell(peripheralOwner.level!!, it.fakePlayer, InteractionHand.MAIN_HAND, Component.literal("Well, cast failed"))
            },
            { proxy },
        )
    }
}
