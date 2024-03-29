package site.siredvin.peripheralworks.computercraft.plugins.specific

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BeaconBlockEntity
import net.minecraft.world.level.block.entity.JukeboxBlockEntity
import net.minecraft.world.level.block.entity.LecternBlockEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

object SpecificPluginProvider : PeripheralPluginProvider {
    override val pluginType: String
        get() = "minecraft"

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        val state = level.getBlockState(pos)
        when (state.block) {
            Blocks.NOTE_BLOCK -> if (PeripheralWorksConfig.enableNoteBlock) return NoteBlockPlugin(level, pos)
            Blocks.POWERED_RAIL -> if (PeripheralWorksConfig.enablePoweredRail) return PoweredRailPlugin(level, pos)
        }
        val entity = level.getBlockEntity(pos) ?: return null
        return when (entity::class.java) {
            LecternBlockEntity::class.java -> if (PeripheralWorksConfig.enableLectern) LecternPlugin(entity as LecternBlockEntity) else null
            BeaconBlockEntity::class.java -> if (PeripheralWorksConfig.enableBeacon) BeaconPlugin(entity as BeaconBlockEntity) else null
            JukeboxBlockEntity::class.java -> if (PeripheralWorksConfig.enableJukebox) JukeboxPlugin(entity as JukeboxBlockEntity) else null
            else -> null
        }
    }
}

/**
 * So, this thing is required, because for some blocks we want to disable default ability to use
 * generate inventory or item_storage plugins.
 * For example, I guess my code or fabric code force Jukebox to run sound after transaction finished
 */
object SpecificProtectedPluginProviders : PeripheralPluginProvider {
    override val pluginType: String
        get() = "minecraft"

    override val conflictWith: Set<String>
        get() = setOf(PeripheralPluginUtils.Type.ITEM_STORAGE, PeripheralPluginUtils.Type.INVENTORY)

    override val priority: Int
        get() = 50
    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        val entity = level.getBlockEntity(pos) ?: return null
        return when (entity::class.java) {
            JukeboxBlockEntity::class.java -> if (PeripheralWorksConfig.enableJukebox) JukeboxPlugin(entity as JukeboxBlockEntity) else null
            else -> null
        }
    }
}
