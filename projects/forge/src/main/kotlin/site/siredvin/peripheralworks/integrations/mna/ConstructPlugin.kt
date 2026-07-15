package site.siredvin.peripheralworks.integrations.mna

import com.mna.Registries
import com.mna.api.entities.construct.ai.parameter.ConstructTaskIntegerParameter
import com.mna.api.entities.construct.ai.parameter.ConstructTaskItemStackParameter
import com.mna.entities.constructs.ai.ConstructMove
import com.mna.entities.constructs.ai.ConstructPlaceItem
import com.mna.entities.constructs.ai.ConstructTakeItem
import com.mna.entities.constructs.animated.Construct
import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.utils.getVec3i
import site.siredvin.peripheralworks.utils.optDirection
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.ext.getItemStack

class ConstructPlugin(private val entity: Construct) : IPeripheralPlugin {
    @LuaFunction(mainThread = true)
    fun move(arguments: IArguments) {
        val player = entity.owner ?: return
        val currentCommand = entity.currentCommand
        if (currentCommand != null && currentCommand.isFinished) {
            throw LuaException("Current command is still now finished")
        }
        val vector = arguments.getVec3i(1)
        val newCommand = ConstructMove(entity, ResourceLocation.parse("none"))
        newCommand.setDesiredLocation(entity.blockPosition().mutable().move(vector))
        entity.setCurrentCommand(player, newCommand)
    }

    @LuaFunction(mainThread = true)
    fun store(arguments: IArguments) {
        val player = entity.owner ?: return
        val currentCommand = entity.currentCommand
        if (currentCommand != null && currentCommand.isFinished) {
            throw LuaException("Current command is still now finished")
        }
        val vector = arguments.getVec3i(1)
        val direction = arguments.optDirection(2) ?: Direction.NORTH
        val newCommand = ConstructPlaceItem(entity, ResourceLocation.parse("none"))
        newCommand.setTileEntity(entity.blockPosition().mutable().move(vector), direction)
        entity.setCurrentCommand(player, newCommand)
    }

    @LuaFunction(mainThread = true)
    fun take(arguments: IArguments) {
        val player = entity.owner ?: return
        val currentCommand = entity.currentCommand
        if (currentCommand != null && currentCommand.isFinished) {
            throw LuaException("Current command is still now finished")
        }
        val vector = arguments.getVec3i(1)
        val direction = arguments.optDirection(2) ?: Direction.NORTH
        val itemToTake = arguments.getItemStack(3)
        val amount = arguments.optInt(4, 64)
        val newCommand = ConstructTakeItem(entity, ResourceLocation.parse("none"))
        val filter = ConstructTaskItemStackParameter("take.stack")
        val amountParameter = ConstructTaskIntegerParameter("take.quantity")
        filter.stack = itemToTake.copyWithCount(amount)
        amountParameter.value = amount
        newCommand.parameters.add(filter)
        newCommand.parameters.add(amountParameter)
        newCommand.setTileEntity(entity.blockPosition().mutable().move(vector), direction)
        entity.setCurrentCommand(player, newCommand)
    }

    @LuaFunction(mainThread = true)
    fun listCommands(): Map<String, Any> = Registries.ConstructTasks.get().entries.associate {
        Pair(it.key.toString(), it.value)
    }

    @LuaFunction(mainThread = true)
    fun diagnose(): List<String> = entity.diagnostics.messages.map { it.message }

    @LuaFunction(mainThread = true)
    fun getCommand(): Map<String, Any> {
        val command = entity.currentCommand
        return mapOf(
            "name" to command.type.toString(),
            "isFinished" to command.isFinished,
            "isSuccess" to command.isSuccess,
            "isStart" to command.isSuccess,
        )
    }
}
