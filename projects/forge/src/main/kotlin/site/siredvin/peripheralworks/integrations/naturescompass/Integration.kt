package site.siredvin.peripheralworks.integrations.naturescompass

import com.chaosthedude.naturescompass.NaturesCompass
import dan200.computercraft.api.client.ComputerCraftAPIClient
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.RegistryObject
import site.siredvin.peripheralium.computercraft.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.peripheralium.computercraft.turtle.PeripheralTurtleUpgrade
import site.siredvin.peripheralworks.ForgePeripheralWorksClient
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.Registries
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.data.ModPocketUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModTurtleUpgradeDataProvider

class Integration: Runnable {

    private fun forTurtle(turtle: ITurtleAccess, side: TurtleSide): NaturesCompassPeripheral<TurtlePeripheralOwner> {
        return NaturesCompassPeripheral(TurtlePeripheralOwner(turtle, side), Configuration.enableNaturesCompassTurtleUpgrade)
    }

    private var NATURES_COMPASS_TURTLE: RegistryObject<TurtleUpgradeSerialiser<PeripheralTurtleUpgrade<NaturesCompassPeripheral<TurtlePeripheralOwner>>>>? = null
    private var NATURES_COMPASS_POCKET: RegistryObject<PocketUpgradeSerialiser<PocketNaturesCompassUpgrade>>? = null

    override fun run() {
        NATURES_COMPASS_TURTLE = Registries.TURTLE_SERIALIZER.register(
            NaturesCompassPeripheral.TYPE
        ) {
            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheralTurtleUpgrade.dynamic(stack.item, ::forTurtle) {
                    ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE)
                }
            }
        }
        NATURES_COMPASS_POCKET = Registries.POCKET_SERIALIZER.register(
            NaturesCompassPeripheral.TYPE
        ) {
            PocketUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PocketNaturesCompassUpgrade(stack)
            }
        }


        ModTurtleUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE),
                NATURES_COMPASS_TURTLE!!.get(), NaturesCompass.naturesCompass
            )
        }
        ModPocketUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                PocketNaturesCompassUpgrade.TYPE,
                NATURES_COMPASS_POCKET!!.get(), NaturesCompass.naturesCompass
            )
        }

        ForgePeripheralWorksClient.registerHook { ComputerCraftAPIClient.registerTurtleUpgradeModeller(
            NATURES_COMPASS_TURTLE!!.get(), TurtleUpgradeModeller.flatItem()
        ) }

        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}