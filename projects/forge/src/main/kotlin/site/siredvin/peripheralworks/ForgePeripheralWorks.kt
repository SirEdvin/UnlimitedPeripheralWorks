package site.siredvin.peripheralworks

import dan200.computercraft.api.ForgeComputerCraftAPI
import dan200.computercraft.api.peripheral.IPeripheralProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import site.siredvin.peripheralworks.common.configuration.ConfigHolder
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.computercraft.EnergyStorageProvider
import site.siredvin.peripheralworks.computercraft.FluidStorageProvider
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT


@Mod(PeripheralWorksCore.MOD_ID)
object ForgePeripheralWorks {

    init {
        PeripheralWorksCore.configure()
        ComputerCraftProxy.addProvider(FluidStorageProvider)
        ComputerCraftProxy.addProvider(EnergyStorageProvider)
        val eventBus = MOD_CONTEXT.getKEventBus()
        eventBus.addListener(this::commonSetup)
        val context = ModLoadingContext.get()
        context.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC, "${PeripheralWorksCore.MOD_ID}.toml")
    }

    fun commonSetup(event: FMLCommonSetupEvent) {
        // So, potentail source of problem is pretty simple
        // My config files are need some extra registration, probably?
        ForgeComputerCraftAPI.registerPeripheralProvider(IPeripheralProvider { world, pos, side ->
            val supplier = ComputerCraftProxy.lazyPeripheralProvider(world, pos, side)
                ?: return@IPeripheralProvider LazyOptional.empty()
            return@IPeripheralProvider LazyOptional.of { supplier.get() }
        })
    }
}