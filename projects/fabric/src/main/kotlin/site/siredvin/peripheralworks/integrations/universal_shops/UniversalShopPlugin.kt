package site.siredvin.peripheralworks.integrations.universal_shops

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import eu.pb4.universalshops.registry.TradeShopBlockEntity
import eu.pb4.universalshops.registry.TradeShopBlockEntity.HologramMode
import eu.pb4.universalshops.trade.PriceHandler.Free
import eu.pb4.universalshops.trade.PriceHandler.SingleItem
import eu.pb4.universalshops.trade.StockHandler
import eu.pb4.universalshops.trade.StockHandler.SelectedItem
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.util.representation.LuaInterpretation
import site.siredvin.peripheralium.util.representation.LuaRepresentation

class UniversalShopPlugin(private val blockEntity: TradeShopBlockEntity) : IPeripheralPlugin {
    override val additionalType: String
        get() = "universal_shop"

    @LuaFunction(mainThread = true)
    fun getHologramMode(): String = blockEntity.hologramMode.name

    @LuaFunction(mainThread = true)
    fun setHologramMode(mode: String) {
        val enumMode = try {
            HologramMode.valueOf(mode)
        } catch (ignored: IllegalArgumentException) {
            throw LuaException("There is no hologram mode $mode, there is only: ${HologramMode.values().joinToString { it.toString() }}")
        }
        blockEntity.hologramMode = enumMode
        blockEntity.setChanged()
    }

    @LuaFunction(mainThread = true)
    fun getPrice(): Map<String, Any> {
        val priceHandler = blockEntity.priceHandler
        val info = mutableMapOf<String, Any>(
            "type" to priceHandler.definition.type,
        )
        if (priceHandler is SingleItem) {
            info["price"] = LuaRepresentation.forItemStack(priceHandler.itemStack)
        }
        return info
    }

    @LuaFunction(mainThread = true)
    fun setPrice(type: String, itemHint: Any?): MethodResult {
        return when (type) {
            "free" -> {
                blockEntity.priceHandler = Free.DEFINITION.createInitial(blockEntity)
                MethodResult.of(true)
            }
            "single_item" -> {
                if (itemHint == null) throw LuaException("With type $type you should use second argument that describes item")
                val item = LuaInterpretation.asItemStack(itemHint)
                val newPriceHandler = SingleItem.DEFINITION.createInitial(blockEntity) as SingleItem
                newPriceHandler.itemStack = item
                blockEntity.priceHandler = newPriceHandler
                MethodResult.of(true)
            }
            else -> MethodResult.of(null, "For now only free and single_item type are supported")
        }
    }

    @LuaFunction(mainThread = true)
    fun getStock(): Map<String, Any> {
        val stockHandler = blockEntity.stockHandler
        val info = mutableMapOf<String, Any>(
            "type" to stockHandler.definition.type,
        )
        if (stockHandler is StockHandler.SingleItem) {
            info["price"] = LuaRepresentation.forItemStack(stockHandler.itemStack)
        }
        return info
    }

    @LuaFunction(mainThread = true)
    fun setStock(type: String, itemHint: Any?): MethodResult {
        return when (type) {
            "selected_item" -> {
                blockEntity.stockHandler = SelectedItem.DEFINITION.createInitial(blockEntity)
                MethodResult.of(true)
            }
            "single_item" -> {
                if (itemHint == null) throw LuaException("With type $type you should use second argument that describes item")
                val item = LuaInterpretation.asItemStack(itemHint)
                val newStockHandler = StockHandler.SingleItem.DEFINITION.createInitial(blockEntity) as StockHandler.SingleItem
                newStockHandler.itemStack = item
                blockEntity.stockHandler = newStockHandler
                MethodResult.of(true)
            }
            else -> MethodResult.of(null, "For now only free and single_item type are supported")
        }
    }
}
