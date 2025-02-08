package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.content.contraptions.elevator.ElevatorPulleyBlockEntity
import com.simibubi.create.content.contraptions.piston.LinearActuatorBlockEntity
import dan200.computercraft.api.lua.LuaFunction

class CreateLinearActuatorPeripheralPlugin(
    blockEntity: LinearActuatorBlockEntity,
) : CreateSmartBlockPeripheralPlugin<LinearActuatorBlockEntity>(blockEntity) {

    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any> {
        return mapOf(
            "movementSpeed" to blockEntity.movementSpeed,
            "isRunning" to blockEntity.running,
        )
    }

    @LuaFunction(mainThread = true)
    fun toggleAssembly() {
        if (blockEntity is ElevatorPulleyBlockEntity) {
            blockEntity.clicked()
        } else {
            if (blockEntity.running) {
                blockEntity.disassemble()
            } else {
                blockEntity.assembleNextTick = true
            }
        }
    }
}
