package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.stacks.AEFluidKey
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStack
import site.siredvin.broccolium.modules.storage.fluid.toForge

object AEFluidKeyFactory {
    fun of(stack: AgnosticFluidStack): AEFluidKey = requireNotNull(AEFluidKey.of(stack.copyWithCount(1.0).toForge()))
}
