package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.integrations.igtooltip.BaseClassRegistration
import appeng.api.integrations.igtooltip.TooltipProvider
import site.siredvin.broccolium.modules.base.block.GenericBlockEntityBlock

class MENetworkPeripheralTooltipProvider : TooltipProvider {
    override fun registerBlockEntityBaseClasses(registration: BaseClassRegistration) {
        registration.addBaseBlockEntity(MENetworkPeripheralBlockEntity::class.java, GenericBlockEntityBlock::class.java)
    }
}
