package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.modem.PeripheralHubPeripheral
import site.siredvin.peripheralworks.subsystem.configurator.BoxStyle
import site.siredvin.peripheralworks.subsystem.configurator.TextStyle
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner

class PeripheralProxyPeripheral(private val blockEntity: PeripheralProxyBlockEntity) : PeripheralHubPeripheral<BlockEntityPeripheralOwner<PeripheralProxyBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {

    companion object {
        const val TYPE = "peripheral_proxy"
    }

    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enablePeripheralProxy

    override val peripheralConfiguration: MutableMap<String, Any>
        get() = super.peripheralConfiguration.apply {
            put("textStyle", blockEntity.textStyle.name.lowercase())
            put("boxStyle", blockEntity.boxStyle.name.lowercase())
        }

    override fun getAdditionalTypes(): Set<String> = setOf("peripheral_hub")

    @LuaFunction(mainThread = true)
    fun setTextStyle(value: String): MethodResult = TextStyle.entries.firstOrNull { it.name.lowercase() == value }
        ?.let {
            blockEntity.setTextStyle(it)
            MethodResult.of(true)
        }
        ?: MethodResult.of(false, "Invalid text style: $value")

    @LuaFunction(mainThread = true)
    fun setBoxStyle(value: String): MethodResult = BoxStyle.entries.firstOrNull { it.name.lowercase() == value }
        ?.let {
            blockEntity.setBoxStyle(it)
            MethodResult.of(true)
        }
        ?: MethodResult.of(false, "Invalid box style: $value")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PeripheralProxyPeripheral) return false
        if (!super.equals(other)) return false

        if (blockEntity != other.blockEntity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + blockEntity.hashCode()
        return result
    }
}
