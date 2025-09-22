package site.siredvin.peripheralworks.integrations.ars_nouveau

import com.hollingsworth.arsnouveau.ArsNouveau
import com.hollingsworth.arsnouveau.api.source.ISourceTile
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import net.minecraft.resources.ResourceLocation
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.broccolium.modules.storage.energy.api.AgnosticEnergyStorageExtractor
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModPocketUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.xplat.ModPlatform

class Integration : Runnable {

    companion object {
        val NOVICE_UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, "novice_magic_tome")
        val APPRENTICE_UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, "apprentice_magic_tome")
        val ARCHMAGE_UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, "archmage_magic_tome")
        val CASTER_TOME_UPGRADE_ID = ResourceLocation(PeripheralWorksCore.MOD_ID, "caster_magic_tome")
        val MAGIC_TOME = ResourceLocation(PeripheralWorksCore.MOD_ID, "magic_tome")
    }

    override fun run() {
        PeripheralWorksConfig.registerIntegrationConfiguration(Configuration)
        if (Configuration.enableSourceStorage) {
            AgnosticEnergyStorageLookup.addEnergyStorageExtractor(
                AgnosticEnergyStorageExtractor { level, blockPos, blockEntity ->
                    if (blockEntity == null) return@AgnosticEnergyStorageExtractor null
                    val source = blockEntity as? ISourceTile ?: return@AgnosticEnergyStorageExtractor null
                    return@AgnosticEnergyStorageExtractor AgnosticSourceStorage(source)
                },
            )
        }
        val magicTomeUpgradeSerializer = ModPlatform.registerPocketUpgrade(
            MAGIC_TOME,
            PocketUpgradeSerialiser.simpleWithCustomItem { id, stack ->
                return@simpleWithCustomItem PocketMagicTomeUpgrade(id, stack)
            },
        )
        ModPocketUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                NOVICE_UPGRADE_ID,
                magicTomeUpgradeSerializer.get(),
                ItemsRegistry.NOVICE_SPELLBOOK.get(),
            ).requireMod(ArsNouveau.MODID)
        }
        ModPocketUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                APPRENTICE_UPGRADE_ID,
                magicTomeUpgradeSerializer.get(),
                ItemsRegistry.APPRENTICE_SPELLBOOK.get(),
            ).requireMod(ArsNouveau.MODID)
        }
        ModPocketUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                ARCHMAGE_UPGRADE_ID,
                magicTomeUpgradeSerializer.get(),
                ItemsRegistry.ARCHMAGE_SPELLBOOK.get(),
            ).requireMod(ArsNouveau.MODID)
        }
        ModPocketUpgradeDataProvider.hookUpgrade {
            it.simpleWithCustomItem(
                CASTER_TOME_UPGRADE_ID,
                magicTomeUpgradeSerializer.get(),
                ItemsRegistry.CASTER_TOME.get(),
            ).requireMod(ArsNouveau.MODID)
        }

        ModEnLanguageProvider.addHook {
            it.addPocket(MAGIC_TOME, "Casting")
            it.addPocket(NOVICE_UPGRADE_ID, "Casting")
            it.addPocket(CASTER_TOME_UPGRADE_ID, "Casting")
            it.addPocket(APPRENTICE_UPGRADE_ID, "Casting")
            it.addPocket(ARCHMAGE_UPGRADE_ID, "Casting")
        }
        ModUaLanguageProvider.addHook {
            it.addPocket(MAGIC_TOME, "Чаклуючий")
            it.addPocket(NOVICE_UPGRADE_ID, "Чаклуючий")
            it.addPocket(CASTER_TOME_UPGRADE_ID, "Чаклуючий")
            it.addPocket(APPRENTICE_UPGRADE_ID, "Чаклуючий")
            it.addPocket(ARCHMAGE_UPGRADE_ID, "Чаклуючий")
        }
    }
}
