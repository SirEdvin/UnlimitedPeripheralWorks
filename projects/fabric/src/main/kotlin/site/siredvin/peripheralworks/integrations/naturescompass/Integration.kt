package site.siredvin.peripheralworks.integrations.naturescompass

import com.chaosthedude.naturescompass.NaturesCompass
import dan200.computercraft.api.client.ComputerCraftAPIClient
import dan200.computercraft.api.client.turtle.TurtleUpgradeModeller
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleAccess
import dan200.computercraft.api.turtle.TurtleSide
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import dan200.computercraft.shared.ComputerCraft
import dan200.computercraft.shared.platform.PlatformHelper
import dan200.computercraft.shared.platform.PlatformHelperImpl
import net.fabricmc.fabric.mixin.registry.sync.RegistriesAccessor
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralium.computercraft.peripheral.owner.TurtlePeripheralOwner
import site.siredvin.peripheralium.computercraft.turtle.PeripheralTurtleUpgrade
import site.siredvin.peripheralworks.FabricPeripheralWorksClient
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.data.ModPocketUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModTurtleUpgradeDataProvider

class Integration: Runnable {

    fun forTurtle(turtle: ITurtleAccess, side: TurtleSide): NaturesCompassPeripheral<TurtlePeripheralOwner> {
        return NaturesCompassPeripheral(TurtlePeripheralOwner(turtle, side), Configuration.enableNaturesCompassTurtleUpgrade)
    }


    var NATURES_COMPASS_TURTLE: TurtleUpgradeSerialiser<PeripheralTurtleUpgrade<NaturesCompassPeripheral<TurtlePeripheralOwner>>>? = null
    var NATURES_COMPASS_POCKET: PocketUpgradeSerialiser<PocketNaturesCompassUpgrade>? = null

    override fun run() {
//        println(RegistriesAccessor.getROOT().keySet())
        val turtleSerializerRegister = RegistriesAccessor.getROOT().get(TurtleUpgradeSerialiser.REGISTRY_ID.location()) as Registry<TurtleUpgradeSerialiser<*>>
        val pocketSerializerRegister = RegistriesAccessor.getROOT().get(PocketUpgradeSerialiser.REGISTRY_ID.location()) as Registry<PocketUpgradeSerialiser<*>>
        val upgradeID = ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE)
        NATURES_COMPASS_TURTLE = Registry.register(
            turtleSerializerRegister, upgradeID,
            TurtleUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PeripheralTurtleUpgrade.dynamic(stack.item, ::forTurtle) {
                    upgradeID
            }
        })
        NATURES_COMPASS_POCKET = Registry.register(
            pocketSerializerRegister, upgradeID,
            PocketUpgradeSerialiser.simpleWithCustomItem { _, stack ->
                return@simpleWithCustomItem PocketNaturesCompassUpgrade(stack)
            }
        )

        ModTurtleUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                ResourceLocation(PeripheralWorksCore.MOD_ID, NaturesCompassPeripheral.TYPE),
                NATURES_COMPASS_TURTLE, NaturesCompass.NATURES_COMPASS_ITEM
            )
        }
        ModPocketUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                PocketNaturesCompassUpgrade.TYPE,
                NATURES_COMPASS_POCKET, NaturesCompass.NATURES_COMPASS_ITEM
            )
        }

        FabricPeripheralWorksClient.registerHook { ComputerCraftAPIClient.registerTurtleUpgradeModeller(
            NATURES_COMPASS_TURTLE, TurtleUpgradeModeller.flatItem()
        ) }
    }
}