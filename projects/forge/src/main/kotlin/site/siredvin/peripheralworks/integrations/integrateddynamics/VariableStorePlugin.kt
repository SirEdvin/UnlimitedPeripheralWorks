package site.siredvin.peripheralworks.integrations.integrateddynamics

import dan200.computercraft.shared.util.NBTUtil
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext
import org.cyclops.integrateddynamics.api.item.IVariableFacade
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade
import site.siredvin.peripheralium.api.storage.SlottedStorage
import site.siredvin.peripheralium.extra.plugins.AbstractInventoryPlugin
import site.siredvin.peripheralium.storage.ItemHandlerWrapper

class VariableStorePlugin(private val store: BlockEntityVariablestore) : AbstractInventoryPlugin() {

    override val level: Level
    override val storage: SlottedStorage
    private val context: ValueDeseralizationContext

    init {
        storage = ItemHandlerWrapper(store.inventory.itemHandler)
        level = store.level!!
        context = ValueDeseralizationContext.of(level)
    }

    fun parseEntry(facade: IVariableFacade): Map<String, Any> {
        val dataMap = mutableMapOf<String, Any>()
        dataMap["id"] = facade.id
        dataMap["type"] = facade.outputType.typeName
        dataMap["isDynamic"] = facade is OperatorVariableFacade
        if (facade.label != null) {
            dataMap["label"] = facade.label!!
        }
        return dataMap
    }

    fun extractFacade(stack: ItemStack): IVariableFacade? {
        return stack.getCapability(VariableFacadeHolderConfig.CAPABILITY).map {
            it.getVariableFacade(context)
        }.orElse(null)
    }

    override fun listImpl(): Map<Int, Map<String, *>> {
        val records = mutableMapOf<Int, Map<String, *>>()
        store.inventory.itemStacks.forEachIndexed { index, itemStack ->
            val facade = extractFacade(itemStack)
            if (facade != null) {
                records[index + 1] = parseEntry(facade)
            }
        }
        return records
    }

    override fun getItemDetailImpl(slot: Int): Map<String, *>? {
        val facade: IVariableFacade = extractFacade(storage.getItem(slot)) ?: return null
        if (store.network == null) {
            return null
        }
        val variable = facade.getVariable<IValue>(NetworkHelpers.getPartNetworkChecked(store.network))
            ?: return null
        val value: IValue = try {
            variable.value
        } catch (e: EvaluationException) {
            return null
        }
        val valueData = HashMap<String, Any?>(4)
        valueData["type"] = value.type.typeName
        valueData["id"] = facade.id
        valueData["value"] = NBTUtil.toLua(value.type.serialize(value))
        valueData["dynamic"] = facade is OperatorVariableFacade
        if (facade.label != null) {
            valueData["label"] = facade.label
        }
        return valueData
    }
}
