package site.siredvin.peripheralworks.integrations.naturescompass

import com.chaosthedude.naturescompass.NaturesCompass
import dan200.computercraft.api.client.ComputerCraftAPIClient
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralium.computercraft.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.peripheralium.computercraft.turtle.PeripheralTurtleUpgrade
import site.siredvin.peripheralworks.PeripheralWorksClientCore
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import java.util.function.Consumer

class Integration: Runnable {

    companion object {
        val UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE)
    }

    fun forTurtle(turtle: ITurtleAccess, side: TurtleSide): NaturesCompassPeripheral<TurtlePeripheralOwner> {
        return NaturesCompassPeripheral(TurtlePeripheralOwner(turtle, side), Configuration.enableNaturesCompassTurtleUpgrade)
    }

    override fun run() {
        PeripheralWorksPlatform.registerTurtleUpgrade(
            UPGRADE_ID, TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheralTurtleUpgrade.dynamic(stack.item, ::forTurtle) {
                    UPGRADE_ID
                }
            }, { dataProvider, serializer ->
                dataProvider.simpleWithCustomItem(
                    ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE),
                    serializer, NaturesCompass.NATURES_COMPASS_ITEM
                )
            },
            listOf(Consumer{
                PeripheralWorksClientCore.registerHook { ComputerCraftAPIClient.registerTurtleUpgradeModeller(
                    it.get(), TurtleUpgradeModeller.flatItem()
                ) }
            })
        )
        PeripheralWorksPlatform.registerPocketUpgrade(
            UPGRADE_ID, PocketUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PocketNaturesCompassUpgrade(stack)
            }
        ) { dataProvider, serializer ->
            dataProvider.simpleWithCustomItem(
                PocketNaturesCompassUpgrade.TYPE,
                serializer, NaturesCompass.NATURES_COMPASS_ITEM
            )
        }
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
    }
}