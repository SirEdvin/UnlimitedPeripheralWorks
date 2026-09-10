package site.siredvin.peripheralworks.integrations.ars_nouveau

import com.hollingsworth.arsnouveau.api.source.ISourceTile
import com.hollingsworth.arsnouveau.common.block.tile.MobJarTile
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import site.siredvin.broccolium.modules.storage.energy.AgnosticEnergyStorageLookup
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.integration.ArsNouveauConfiguration
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import site.siredvin.peripheralworks.data.ModEnLanguageProvider
import site.siredvin.peripheralworks.data.ModUaLanguageProvider
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import java.util.function.Supplier

class Integration : Runnable {

    object MobJarPluginProvider : PeripheralPluginProvider {
        override val pluginType: String
            get() = "mob_jar"

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            val blockEntity = level.getBlockEntity(pos)
            if (!ArsNouveauConfiguration.enableMobJarPlugin || blockEntity == null) return null
            if (blockEntity is MobJarTile) {
                return MobJarPlugin(blockEntity)
            }
            return null
        }
    }

    companion object {
        val NOVICE_UPGRADE_ID = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "novice_magic_tome")
        val APPRENTICE_UPGRADE_ID = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "apprentice_magic_tome")
        val ARCHMAGE_UPGRADE_ID = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "archmage_magic_tome")
        val CASTER_TOME_UPGRADE_ID = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "caster_magic_tome")
        val MAGIC_TOME = ResourceLocation.fromNamespaceAndPath(PeripheralWorksCore.MOD_ID, "magic_tome")
        lateinit var magicTomeUpgradeType: Supplier<UpgradeType<PocketMagicTomeUpgrade>>
    }

    override fun run() {
        ComputerCraftProxy.addProvider(MobJarPluginProvider)
        if (ArsNouveauConfiguration.enableSourceStorage) {
            AgnosticEnergyStorageLookup.addBlockLookup { level, blockPos, blockEntity, direction ->
                if (blockEntity == null) return@addBlockLookup null
                val source = blockEntity as? ISourceTile ?: return@addBlockLookup null
                return@addBlockLookup AgnosticSourceStorage(source)
            }
        }
        val upgradeType = UpgradeType.simpleWithCustomItem { stack ->
            PocketMagicTomeUpgrade(MAGIC_TOME, stack)
        }
        magicTomeUpgradeType = ModPlatform.registerPocketUpgrade(
            MAGIC_TOME,
            upgradeType,
        )
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
