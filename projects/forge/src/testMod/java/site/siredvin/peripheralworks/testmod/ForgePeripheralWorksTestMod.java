package site.siredvin.peripheralworks.testmod;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.LoadingErrorScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import site.siredvin.testiarium.ForgeTestiarium;
import site.siredvin.testiarium.Testiarium;
import site.siredvin.testiarium.cct.CctComputers;
import site.siredvin.testiarium.cct.CctFixtureCommands;

@Mod("peripheralworks_testmod")
public final class ForgePeripheralWorksTestMod {
    public ForgePeripheralWorksTestMod() {
        CctComputers.INSTANCE.initialize();
        MinecraftForge.EVENT_BUS.addListener((ServerStartingEvent event) -> {
            CctComputers.INSTANCE.reset();
            CctFixtureCommands.INSTANCE.importFiles(event.getServer());
        });
        Testiarium.register(PeripheralWorksGameTests.class);
        Testiarium.register(ForgeRecipeConditionsGameTests.class);
        try {
            if (ModList.get().isLoaded("gtceu")) {
                Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.GTCEUPeripheralGameTests"));
                Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.GTCEUInfoGameTests"));
            }
            if (ModList.get().isLoaded("hostilenetworks")) {
                Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.NeuralNetworksGameTests"));
            }
            if (ModList.get().isLoaded("extrahnn")) {
                Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.ExtraNeuralNetworksGameTests"));
            }
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
        if (ModList.get().isLoaded("ae2")) {
            try {
                Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.AE2GameTests"));
                Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.AE2CraftingJobsGameTests"));
                Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.AE2ConfigurableObjectsGameTests"));
                Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.AE2WirelessTerminalGameTests"));
                Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.AE2PatternPedestalGameTests"));
            } catch (ClassNotFoundException exception) {
                throw new IllegalStateException(exception);
            }
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientTests::register);
        ForgeTestiarium.registerTests();
    }

    private static final class ClientTests {
        private static void register() {
            Testiarium.register(NetworkManagerClientGameTests.class);
            if (ModList.get().isLoaded("ae2")) {
                try {
                    Testiarium.register(Class.forName("site.siredvin.peripheralworks.testmod.AE2PatternPedestalClientGameTests"));
                } catch (ClassNotFoundException exception) {
                    throw new IllegalStateException(exception);
                }
            }
            if (System.getProperty("testiarium.client") == null) {
                return;
            }
            // Fixture jars without pack.mcmeta make Forge open a warning screen; Testiarium only starts from the title screen.
            MinecraftForge.EVENT_BUS.addListener((ScreenEvent.Opening event) -> {
                if (event.getScreen() instanceof LoadingErrorScreen) {
                    event.setNewScreen(new TitleScreen());
                }
            });
        }
    }
}
