package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.integrations.igtooltip.ClientRegistration
import appeng.api.integrations.igtooltip.CommonRegistration
import appeng.api.integrations.igtooltip.TooltipProvider
import appeng.integration.modules.igtooltip.TooltipIds
import appeng.integration.modules.igtooltip.blocks.GridNodeStateDataProvider

class MENetworkPeripheralTooltipProvider : TooltipProvider {
    override fun registerCommon(registration: CommonRegistration) {
        registration.addBlockEntityData(MENetworkPeripheralBlockEntity::class.java, GridNodeStateDataProvider())
    }

    override fun registerClient(registration: ClientRegistration) {
        registration.addBlockEntityBody(
            MENetworkPeripheralBlockEntity::class.java,
            MENetworkPeripheralBlock::class.java,
            TooltipIds.GRID_NODE_STATE,
            GridNodeStateDataProvider(),
        )
    }
}
