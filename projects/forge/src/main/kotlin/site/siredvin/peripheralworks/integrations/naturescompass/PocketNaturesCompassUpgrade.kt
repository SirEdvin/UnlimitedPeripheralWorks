package site.siredvin.peripheralworks.integrations.naturescompass

import dan200.computercraft.api.pocket.IPocketAccess
import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.integration.NaturesCompassConfiguration
import site.siredvin.tweakium.modules.peripheral.owner.PocketPeripheralOwner
import site.siredvin.tweakium.modules.pocket.BasePocketUpgrade
import java.util.function.Supplier

class PocketNaturesCompassUpgrade(stack: ItemStack, private val typeSupplier: Supplier<UpgradeType<PocketNaturesCompassUpgrade>>) :
    BasePocketUpgrade<NaturesCompassPeripheral<PocketPeripheralOwner>>(
        TYPE,
        stack,
    ) {

    companion object {
        val TYPE = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE)
    }

    override fun getType(): UpgradeType<out IPocketUpgrade> = typeSupplier.get()

    override fun getPeripheral(access: IPocketAccess): NaturesCompassPeripheral<PocketPeripheralOwner> = NaturesCompassPeripheral(PocketPeripheralOwner(access), NaturesCompassConfiguration.enableNaturesCompassPocketUpgrade)
}
