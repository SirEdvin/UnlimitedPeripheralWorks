package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType.*
import dan200.computercraft.api.lua.LuaFunction

class CreateBlazeBurnerPeripheralPlugin(blockEntity: BlazeBurnerBlockEntity) : CreateSmartBlockPeripheralPlugin<BlazeBurnerBlockEntity>(blockEntity) {

    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any?> = mapOf(
        "fuelType" to when (blockEntity.activeFuel) {
            NONE -> "none"
            NORMAL -> "normal"
            SPECIAL -> "special"
            null -> null
        },
        "remainingBurnTime" to blockEntity.remainingBurnTime,
    )
}
