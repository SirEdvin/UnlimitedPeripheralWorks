package site.siredvin.peripheralworks.integrations.ars_nouveau

import com.hollingsworth.arsnouveau.api.spell.AbstractCaster
import com.hollingsworth.arsnouveau.api.spell.ItemCasterProvider
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry
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

    var spellCaster: AbstractCaster<*> = (casterTome.item as ItemCasterProvider).getSpellCaster(casterTome)

    @LuaFunction(mainThread = true)
    fun getSpells(): Map<Int, MutableMap<String, Any>> = spellCaster.spells.slots().mapKeys { it.key + 1 }.mapValues { LuaRepresentation.forSpell(it.value) }

    @LuaFunction(mainThread = true)
    fun getSelectedSlot(): Int = spellCaster.currentSlot + 1

    @LuaFunction(mainThread = true)
    fun size(): Int = spellCaster.maxSlots

    @LuaFunction(mainThread = true)
    fun getSelectedSpell(): MutableMap<String, Any> = LuaRepresentation.forSpell(spellCaster.spell)

    @LuaFunction(mainThread = true)
    fun select(slot: Int) {
        spellCaster = spellCaster.setCurrentSlot(slot - 1)
        spellCaster.saveToStack(casterTome)
        peripheralOwner.pocket.upgradeData = casterTome.componentsPatch
    }

    @LuaFunction(mainThread = true)
    fun getMana(): Map<String, Any> {
        val owner = peripheralOwner.owner
        if (owner == null) {
            return mapOf(
                "current" to 0,
                "max" to 0,
                "bookTier" to 0,
                "glyphBonus" to 0,
            )
        }
        val mana = CapabilityRegistry.getMana(owner)
        return mapOf(
            "current" to mana.currentMana,
            "max" to mana.maxMana,
            "bookTier" to mana.bookTier,
            "glyphBonus" to mana.glyphBonus,
        )
    }

    @LuaFunction(mainThread = true)
    fun cast() {
        if (peripheralOwner.owner == null) {
            throw LuaException("Cannot find player for some reason, so cannot cast")
        }
        val fakePlayer = TweakedForgeFakePlayer(peripheralOwner.level!! as ServerLevel, peripheralOwner.owner!!.gameProfile, peripheralOwner.owner!!)
        val ownerMana = CapabilityRegistry.getMana(peripheralOwner.owner!!)
        val fakeMana = CapabilityRegistry.getMana(fakePlayer)
        fakeMana.setMana(ownerMana.currentMana)
        fakeMana.maxMana = ownerMana.maxMana
        fakeMana.bookTier = ownerMana.bookTier
        fakeMana.glyphBonus = ownerMana.glyphBonus
        fakeMana.reserve = ownerMana.reserve
        val proxy = FakePlayerProxy(fakePlayer)
        try {
            FakePlayerProviderPocket.withPlayerTweaked(
                peripheralOwner.pocket,
                {
                    it.fakePlayer.moveTo(it.fakePlayer.x, it.fakePlayer.y + 0.5, it.fakePlayer.z, fakePlayer.originalPlayer!!.yRot, fakePlayer.originalPlayer.xRot)
                    it.fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, casterTome)
                    spellCaster.castSpell(peripheralOwner.level!!, it.fakePlayer, InteractionHand.MAIN_HAND, Component.literal("Well, cast failed"))
                },
                { proxy },
            )
        } finally {
            ownerMana.setMana(fakeMana.currentMana)
            ownerMana.reserve = fakeMana.reserve
        }
    }
}
