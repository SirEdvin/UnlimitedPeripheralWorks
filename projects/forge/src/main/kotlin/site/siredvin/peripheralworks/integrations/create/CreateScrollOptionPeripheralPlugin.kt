package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour
import dan200.computercraft.api.lua.LuaFunction

class CreateScrollOptionPeripheralPlugin(blockEntity: SmartBlockEntity, private val behaviour: ScrollValueBehaviour) :
    CreateSmartBlockPeripheralPlugin<SmartBlockEntity>(
        blockEntity,
    ) {
    @LuaFunction(mainThread = true)
    fun getScrollValue(): Int = behaviour.getValue()

    @LuaFunction(mainThread = true)
    fun setScrollValue(value: Int) {
        behaviour.setValue(value)
    }
}
