package site.siredvin.peripheralworks.computercraft

import net.minecraft.world.level.block.entity.BlockEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.computercraft.peripheral.IntegrationPeripheral

class NewIntegrationPeripheral(private val target: BlockEntity?): IntegrationPeripheral() {
    override fun getType(): String {
        return "mutable_peripheral"
    }

    override fun getTarget(): Any? {
        return target
    }

    fun reallyAddPlugin(plugin: IPeripheralPlugin) {
        addPlugin(plugin)
    }
}