package site.siredvin.peripheralworks.integrations.hostilenetworks

import dan200.computercraft.api.detail.VanillaDetailRegistries
import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dev.shadowsoffire.hostilenetworks.data.DataModel
import dev.shadowsoffire.placebo.reload.DynamicHolder
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class LootFabricatorPlugin(
    override val additionalType: String,
    private val getSelection: (DataModel) -> Int,
    private val setSelection: (DynamicHolder<DataModel>, Int) -> Unit,
) : IPeripheralPlugin {
    @LuaFunction(mainThread = true)
    fun getEntities(): List<String> = NeuralModels.entities()

    @LuaFunction(mainThread = true)
    fun getLoot(entity: String): List<Map<String, Any>> = NeuralModels.resolve(entity).get().fabDrops().map {
        VanillaDetailRegistries.ITEM_STACK.getDetails(it.copy())
    }

    @LuaFunction(mainThread = true)
    fun getSelectedLoot(entity: String): Int? {
        val model = NeuralModels.resolve(entity).get()
        return getSelection(model).takeIf { it in model.fabDrops().indices }?.plus(1)
    }

    @LuaFunction(mainThread = true)
    fun setSelectedLoot(entity: String, arguments: IArguments) {
        val model = NeuralModels.resolve(entity)
        val index = arguments.optDouble(1, Double.NaN)
        val selection = if (arguments.get(1) == null) {
            -1
        } else {
            if (!index.isFinite() || index % 1.0 != 0.0 || index < 1 || index > model.get().fabDrops().size) {
                throw LuaException("Loot index must be an integer between 1 and ${model.get().fabDrops().size}, or nil")
            }
            index.toInt() - 1
        }
        setSelection(model, selection)
    }
}
