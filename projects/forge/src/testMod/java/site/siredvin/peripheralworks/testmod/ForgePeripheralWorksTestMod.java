package site.siredvin.peripheralworks.testmod;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import site.siredvin.testiarium.ForgeTestiarium;
import site.siredvin.testiarium.Testiarium;
import site.siredvin.testiarium.cct.CctComputers;
import site.siredvin.testiarium.cct.CctFixtureCommands;
import site.siredvin.testiarium.fixture.client.ForgeClientTestHooks;

@Mod("peripheralworks_testmod")
public final class ForgePeripheralWorksTestMod {
    public ForgePeripheralWorksTestMod() {
        CctComputers.INSTANCE.initialize();
        MinecraftForge.EVENT_BUS.addListener((ServerStartingEvent event) -> {
            CctComputers.INSTANCE.reset();
            CctFixtureCommands.INSTANCE.importFiles(event.getServer());
        });
        Testiarium.register(PeripheralWorksGameTests.class);
        Testiarium.register(AE2ConfigurableObjectsGameTests.class);
        Testiarium.register(AE2WirelessTerminalGameTests.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientTests::register);
        ForgeTestiarium.registerTests();
    }

    private static final class ClientTests {
        private static void register() {
            Testiarium.register(NetworkManagerClientGameTests.class);
            ForgeClientTestHooks.register();
            // ponytail: Forge's headless loading overlay never opens a title screen, so trigger Testiarium after resources settle.
            CompletableFuture.delayedExecutor(20, TimeUnit.SECONDS).execute(() -> Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().setOverlay(null);
                site.siredvin.testiarium.fixture.client.ClientTestHooks.onOpenScreen(new TitleScreen());
            }));
        }
    }
}
