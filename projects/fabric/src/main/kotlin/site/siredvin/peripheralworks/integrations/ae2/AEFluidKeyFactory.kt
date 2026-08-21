package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.stacks.AEFluidKey
import site.siredvin.broccolium.modules.storage.fluid.AgnosticFluidStack
import site.siredvin.broccolium.modules.storage.fluid.toVariant

object AEFluidKeyFactory {
    fun of(stack: AgnosticFluidStack): AEFluidKey = AEFluidKey.of(stack.toVariant())!!
}
