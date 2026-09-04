package site.siredvin.peripheralworks.integrations.projecte

import dan200.computercraft.api.detail.DetailProvider
import dan200.computercraft.api.detail.VanillaDetailRegistries
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import moze_intel.projecte.PECore
import moze_intel.projecte.gameObjs.registries.PEItems
import moze_intel.projecte.utils.EMCHelper
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksClientCore
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.client.turtle.ScaledItemModeller
import site.siredvin.peripheralworks.common.configuration.integration.ProjectEConfiguration
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModPocketUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModTurtleUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.tweakium.modules.turtle.PeripheralTurtleUpgrade
import kotlin.collections.set

class Integration : Runnable {

    companion object {
        val UPGRADE_ID = ResourceLocation.fromNamespaceAndPath(
            PeripheralWorksCore.MOD_ID,
            TransmutationTabletPeripheral.TYPE,
        )
    }

    private fun forTurtle(turtle: ITurtleAccess, side: TurtleSide): TransmutationTabletPeripheral<TurtlePeripheralOwner> = TransmutationTabletPeripheral(TurtlePeripheralOwner(turtle, side), ProjectEConfiguration.enableTransmutationTabletTurtleUpgrade)

    override fun run() {
        val turtleUpgradeSup = ModPlatform.registerTurtleUpgrade(
            UPGRADE_ID,
            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheralTurtleUpgrade.dynamic(stack.item, ::forTurtle) {
                    UPGRADE_ID
                }
            },
        )
        ModTurtleUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, TransmutationTabletPeripheral.TYPE),
                turtleUpgradeSup.get(),
                PEItems.TRANSMUTATION_TABLET.get(),
            ).requireMod(PECore.MODID)
        }
        PeripheralWorksClientCore.EXTRA_TURTLE_MODEL_PROVIDERS.add {
            @Suppress("UNCHECKED_CAST")
            Pair(turtleUpgradeSup.get() as TurtleUpgradeSerialiser<ITurtleUpgrade>, ScaledItemModeller(0.5f))
        }

        val pocketUpgrade = ModPlatform.registerPocketUpgrade(
            UPGRADE_ID,
            PocketUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PocketTransmutationTabletUpgrade(stack)
            },
        )
        ModPocketUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                PocketTransmutationTabletUpgrade.TYPE,
                pocketUpgrade.get(),
                PEItems.TRANSMUTATION_TABLET.get(),
            ).requireMod(PECore.MODID)
        }

        VanillaDetailRegistries.ITEM_STACK.addProvider(
            DetailProvider { data, stack ->
                val sellValue = EMCHelper.getEmcSellValue(stack)
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
