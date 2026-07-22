package site.siredvin.peripheralworks.testmod;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import site.siredvin.testiarium.Testiarium;
import site.siredvin.testiarium.cct.CctComputers;
import site.siredvin.testiarium.cct.CctFixtureCommands;

@Mod("peripheralworks_testmod")
public final class ForgePeripheralWorksTestMod {
    public ForgePeripheralWorksTestMod() {
        CctComputers.INSTANCE.initialize();
        NeoForge.EVENT_BUS.addListener(ForgePeripheralWorksTestMod::onServerStarting);
        Testiarium.register(PeripheralWorksGameTests.class);
    }

    private static void onServerStarting(ServerStartingEvent event) {
        CctComputers.INSTANCE.reset();
        CctFixtureCommands.INSTANCE.importFiles(event.getServer());
    }
}
