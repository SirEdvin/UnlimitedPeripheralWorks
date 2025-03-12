package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

abstract class CreateSmartBlockPeripheralPlugin<T : SmartBlockEntity>(
    protected val blockEntity: T,
) : IPeripheralPlugin
