package site.siredvin.peripheralworks.integrations.ars_nouveau

import com.hollingsworth.arsnouveau.common.block.tile.MobJarTile
import dan200.computercraft.api.lua.LuaFunction
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation

class MobJarPlugin(val be: MobJarTile) : IPeripheralPlugin {
    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any> {
        val entity = be.entity ?: return emptyMap()
        return mapOf(
            "entity" to LuaRepresentation.forEntity(entity),
        )
    }
}
