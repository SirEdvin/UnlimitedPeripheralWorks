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
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModPocketUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModTurtleUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform

class Integration : Runnable {

    companion object {
        val UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE)
    }

    private fun forTurtle(turtle: ITurtleAccess, side: TurtleSide): NaturesCompassPeripheral<TurtlePeripheralOwner> {
        return NaturesCompassPeripheral(TurtlePeripheralOwner(turtle, side), Configuration.enableNaturesCompassTurtleUpgrade)
    }

    override fun run() {
        val turtleUpgradeSup = PeripheralWorksPlatform.registerTurtleUpgrade(
            UPGRADE_ID,
            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheralTurtleUpgrade.dynamic(stack.item, ::forTurtle) {
                    UPGRADE_ID
                }
            },
        )
        ModTurtleUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE),
                turtleUpgradeSup.get(),
                NaturesCompass.naturesCompass,
            )
        }
        PeripheralWorksClientCore.addHook {
            ComputerCraftAPIClient.registerTurtleUpgradeModeller(
                turtleUpgradeSup.get(),
                TurtleUpgradeModeller.flatItem(),
            )
        }

        val pocketUpgrade = PeripheralWorksPlatform.registerPocketUpgrade(
            UPGRADE_ID,
            PocketUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PocketNaturesCompassUpgrade(stack)
            },
        )
        ModPocketUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                PocketNaturesCompassUpgrade.TYPE,
                pocketUpgrade.get(),
                NaturesCompass.naturesCompass,
            )
        }

        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)

        ModEnLanguageProvider.addHook {
            it.addUpgrades(UPGRADE_ID, "Nature Compassing")
        }
        ModUaLanguageProvider.addHook {
            it.addTurtle(UPGRADE_ID, "Природновідчуваюча")
            it.addPocket(UPGRADE_ID, "Природовідчуваючий")
        }
    }
}
