package site.siredvin.peripheralworks.integrations.projecte

import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.VanillaDetailRegistries
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.upgrades.UpgradeType
import moze_intel.projecte.api.proxy.IEMCProxy
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksClientCore
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.client.turtle.ScaledItemModeller
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.turtle.PeripheralTurtleUpgrade
import java.util.function.Supplier
import kotlin.collections.set

class Integration : Runnable {

    companion object {
        val UPGRADE_ID = ResourceLocation.fromNamespaceAndPath(
            PeripheralWorksCore.MOD_ID,
            TransmutationTabletPeripheral.TYPE,
        )
    }

    private fun forTurtle(turtle: ITurtleAccess, side: TurtleSide): TransmutationTabletPeripheral<TurtlePeripheralOwner> = TransmutationTabletPeripheral(TurtlePeripheralOwner(turtle, side), Configuration.enableTransmutationTabletTurtleUpgrade)

    override fun run() {
        lateinit var turtleUpgradeSup: Supplier<UpgradeType<PeripheralTurtleUpgrade<TransmutationTabletPeripheral<TurtlePeripheralOwner>>>>
        turtleUpgradeSup = ModPlatform.registerTurtleUpgrade(
            UPGRADE_ID,
            UpgradeType.simpleWithCustomItem { stack ->
                PeripheralTurtleUpgrade.dynamic(stack.item, ::forTurtle, { turtleUpgradeSup.get() }) { UPGRADE_ID }
            },
        )
        PeripheralWorksClientCore.EXTRA_TURTLE_MODEL_PROVIDERS.add {
            @Suppress("UNCHECKED_CAST")
            Pair(turtleUpgradeSup.get() as UpgradeType<ITurtleUpgrade>, ScaledItemModeller(0.5f))
        }

        lateinit var pocketUpgrade: Supplier<UpgradeType<PocketTransmutationTabletUpgrade>>
        pocketUpgrade = ModPlatform.registerPocketUpgrade(
            UPGRADE_ID,
            UpgradeType.simpleWithCustomItem { stack ->
                PocketTransmutationTabletUpgrade(stack) { pocketUpgrade.get() }
            },
        )

        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)

        VanillaDetailRegistries.ITEM_STACK.addProvider(
            DetailProvider { data, stack ->
                val sellValue = IEMCProxy.INSTANCE.getSellValue(stack)
                if (sellValue != 0L) {
                    data["EMC"] = sellValue
                }
            },
        )

        ModEnLanguageProvider.addHook {
            it.addUpgrades(UPGRADE_ID, "Transmutating")
        }
        ModUaLanguageProvider.addHook {
            it.addTurtle(UPGRADE_ID, "Перетворююча")
            it.addPocket(UPGRADE_ID, "Перетворюючий")
        }
    }
}
