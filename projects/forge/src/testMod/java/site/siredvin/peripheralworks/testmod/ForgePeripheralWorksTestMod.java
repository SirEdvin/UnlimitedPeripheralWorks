package site.siredvin.peripheralworks.testmod;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import site.siredvin.testiarium.Testiarium;
import site.siredvin.testiarium.cct.CctComputers;
import site.siredvin.testiarium.cct.CctFixtureCommands;
import site.siredvin.testiarium.fixture.client.ForgeClientTestHooks;

@Mod("peripheralworks_testmod")
public final class ForgePeripheralWorksTestMod {
    public ForgePeripheralWorksTestMod() {
        CctComputers.INSTANCE.initialize();
        NeoForge.EVENT_BUS.addListener(ForgePeripheralWorksTestMod::onServerStarting);
        Testiarium.register(PeripheralWorksGameTests.class);
        Testiarium.register(ForgeRecipeConditionsGameTests.class);
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
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientTests.register();
        }
    }

    private static void onServerStarting(ServerStartingEvent event) {
        CctComputers.INSTANCE.reset();
        CctFixtureCommands.INSTANCE.importFiles(event.getServer());
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
            ForgeClientTestHooks.register();
            // ponytail: NeoForge's headless loading overlay never opens a title screen, so trigger Testiarium after resources settle.
            CompletableFuture.delayedExecutor(20, TimeUnit.SECONDS).execute(() -> Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().setOverlay(null);
                site.siredvin.testiarium.fixture.client.ClientTestHooks.onOpenScreen(new TitleScreen());
            }));
        }
    }
}
