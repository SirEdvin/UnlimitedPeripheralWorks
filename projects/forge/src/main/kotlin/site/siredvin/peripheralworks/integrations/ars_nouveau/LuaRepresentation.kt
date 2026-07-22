package site.siredvin.peripheralworks.integrations.ars_nouveau

import com.hollingsworth.arsnouveau.api.spell.Spell

object LuaRepresentation {
    @Suppress("DEPRECATION")
    fun forSpell(spell: Spell): MutableMap<String, Any> {
        val data = mutableMapOf<String, Any>()
        data["name"] = spell.name()
        data["display"] = spell.displayString
        data["color"] = spell.color().color
        spell.sound().sound?.soundName?.string.let {
            if (it != null) {
                data["sound"] = it
            }
        }
        data["cost"] = spell.cost
        return data
    }
}
