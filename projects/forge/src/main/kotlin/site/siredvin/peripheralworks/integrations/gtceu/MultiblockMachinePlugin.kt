package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import site.siredvin.peripheralworks.utils.extractPosition

class MultiblockMachinePlugin(private val def: MultiblockMachineDefinition, private val meta: MultiblockControllerMachine) : MachinePlugin(def) {
    companion object {
        val TYPE = "gtceu:multiblock_machine"
    }
    override val additionalType: String
        get() = TYPE

    @LuaFunction(mainThread = true)
    fun getPartNames(access: IComputerAccess): MethodResult {
        if (!meta.isFormed) {
            return MethodResult.of(null, "Multiblock is not formed yet")
        }
        val level = meta.level ?: return MethodResult.of(null, "Something is not formed")
        val positions = meta.partPositions.toSet()
        val peripherals = mutableListOf<String>()
        access.availablePeripherals.forEach {
            if (positions.contains(it.value.extractPosition())) {
                peripherals.add(it.key)
            }
        }
        return MethodResult.of(peripherals)
    }
}
