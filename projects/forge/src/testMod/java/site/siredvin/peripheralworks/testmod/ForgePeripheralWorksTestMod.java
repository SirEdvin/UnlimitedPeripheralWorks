package site.siredvin.peripheralworks.testmod;

import net.neoforged.fml.common.Mod;
import site.siredvin.testiarium.Testiarium;

@Mod("peripheralworks_testmod")
public final class ForgePeripheralWorksTestMod {
    public ForgePeripheralWorksTestMod() {
        Testiarium.register(PeripheralWorksGameTests.class);
    }
}
