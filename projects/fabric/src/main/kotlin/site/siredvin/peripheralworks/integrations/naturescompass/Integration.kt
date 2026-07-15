package site.siredvin.peripheralworks.integrations.naturescompass

import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.resources.ResourceLocation
import site.siredvin.broccolium.modules.platform.api.RegistryEntry
import site.siredvin.peripheralworks.PeripheralWorksClientCore
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.turtle.PeripheralTurtleUpgrade

class Integration : Runnable {

    companion object {
        val UPGRADE_ID = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE)
    }

    private fun forTurtle(turtle: ITurtleAccess, side: TurtleSide): NaturesCompassPeripheral<TurtlePeripheralOwner> = NaturesCompassPeripheral(TurtlePeripheralOwner(turtle, side), Configuration.enableNaturesCompassTurtleUpgrade)

    override fun run() {
        lateinit var turtleUpgradeSup: RegistryEntry<UpgradeType<PeripheralTurtleUpgrade<NaturesCompassPeripheral<TurtlePeripheralOwner>>>>
        turtleUpgradeSup = ModPlatform.registerTurtleUpgrade(
            UPGRADE_ID,
            UpgradeType.simpleWithCustomItem { stack ->
                PeripheralTurtleUpgrade.dynamic(stack.item, ::forTurtle, { turtleUpgradeSup.get() }) { UPGRADE_ID }
            },
        )
        PeripheralWorksClientCore.EXTRA_TURTLE_MODEL_PROVIDERS.add {
            @Suppress("UNCHECKED_CAST")
            Pair(turtleUpgradeSup.get() as UpgradeType<ITurtleUpgrade>, TurtleUpgradeModeller.flatItem())
        }

        lateinit var pocketUpgrade: RegistryEntry<UpgradeType<PocketNaturesCompassUpgrade>>
        pocketUpgrade = ModPlatform.registerPocketUpgrade(
            UPGRADE_ID,
            UpgradeType.simpleWithCustomItem { stack ->
                PocketNaturesCompassUpgrade(stack) { pocketUpgrade.get() }
            },
        )
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
